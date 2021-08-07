package cn.edu.pku.sei.plde.hanabi.fl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Clazz;
import com.gzoltar.core.components.Method;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;

import cn.edu.pku.sei.plde.hanabi.build.proj.D4jProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.fl.common.TestClassesFinder;
import cn.edu.pku.sei.plde.hanabi.fl.constructor.InvocationCollector;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Pair;
import edu.pku.sei.conditon.util.DateUtil;
import edu.pku.sei.conditon.util.FileUtil;
import xxl.java.library.JavaLibrary;

public class GZoltarFaultLocation extends FaultLocation {

	private static final boolean USE_TEMP_FILE = false; 
	
	private Map<Statement, List<TestResult>> stmtToFailTests;
	private Map<Statement, List<TestResult>> stmtToSuccTests;
	
	private Set<TestResult> allFailedTests = new HashSet<>(); 
	
	public GZoltarFaultLocation(ProjectConfig projectConfig) {
		super(projectConfig);
		
		File tmpGzoltarResFile;
		if(USE_TEMP_FILE) {
			tmpGzoltarResFile = new File(Config.TEMP_SUSPICIOUS_PATH + "gzoltar/" + projectConfig.getProjectName() + ".gzoltar");
			if(!tmpGzoltarResFile.getParentFile().exists()) {
				tmpGzoltarResFile.getParentFile().mkdirs();
			}
			
			if(tmpGzoltarResFile.exists()) {
				long time = tmpGzoltarResFile.lastModified();
				if(DateUtil.in24HoursFromNow(time)) {
					List<Map<Statement, List<TestResult>>> results = (List<Map<Statement, List<TestResult>>>) FileUtil.loadObjeceFromFile(tmpGzoltarResFile);
					if(results != null && results.size() == 2) {
						stmtToFailTests = results.get(0);
						stmtToSuccTests = results.get(1);
						return;
					}
				} else {
					tmpGzoltarResFile.delete();
				}
			}
		}
		
		try {
			checkEnv();
			
			GZoltar gzoltar = new GZoltar(System.getProperty("user.dir"));
			
			StringBuffer sb = new StringBuffer();
			for(String path : this.projectConfig.getClassPaths().split(":")) {
				File tmpFile = new File(path);
				if(tmpFile.exists()) {
					sb.append(path);
					sb.append(":");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			URL[] classpaths = JavaLibrary.classpathFrom(sb.toString());
			
			String testTarFolder = projectConfig.getTestTargetRoot().getAbsolutePath();
			
			ArrayList<String> clspathList = new ArrayList<>();
			for (URL url : classpaths) {
				if ("file".equals(url.getProtocol())) {
					clspathList.add(url.getPath());
				} else {
					clspathList.add(url.toExternalForm());
				}
				if (url.getPath().endsWith(".jar")){
	                gzoltar.addClassNotToInstrument(url.getPath());
	                gzoltar.addPackageNotToInstrument(url.getPath());
	            }
			}
			
			gzoltar.setWorkingDirectory(this.projectConfig.getRoot().getAbsolutePath());
			gzoltar.setClassPaths(clspathList);
			gzoltar.addPackageNotToInstrument("org.junit");
			gzoltar.addPackageNotToInstrument("junit.framework");
			gzoltar.addTestPackageNotToExecute("junit.framework");
			gzoltar.addTestPackageNotToExecute("org.junit");
			gzoltar.addPackageToInstrument("");
			
			for (URL url: classpaths){
	            if (url.getPath().endsWith(".jar")){
	                gzoltar.addClassNotToInstrument(url.getPath());
	                gzoltar.addPackageNotToInstrument(url.getPath());
	            }
	        }
			
			String[] testClasses = new TestClassesFinder().findIn(JavaLibrary.classpathFrom(testTarFolder), false);
			
			//testClasses = new String[]{"org.apache.commons.math3.genetics.ElitisticListPopulationTest"};
			this.run(gzoltar, testClasses);
			
			if(USE_TEMP_FILE) {
				List<Map<Statement, List<TestResult>>> results = new ArrayList<>(2);
				results.add(stmtToFailTests);
				results.add(stmtToSuccTests);
				if(tmpGzoltarResFile != null) {
					FileUtil.writeObjectToFile(tmpGzoltarResFile.getAbsolutePath(), results);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Suspect> getAllSuspects() {
		
		assert stmtToFailTests != null && !stmtToFailTests.isEmpty();
		
		if(this.getProjectConfig() instanceof D4jProjectConfig) {
			OffTheShellFL offTheShellFL = new OffTheShellFL(projectConfig);
			List<Suspect> suspects = offTheShellFL.getAllSuspects();//TODO: need opt!
			List<Suspect> tobeRemoved = new ArrayList<>();
			for(Suspect suspect: suspects) {
				
				List<String> triggerTests = getFailedTests(suspect);
				
				if(triggerTests.isEmpty()) {
					if(! withinConstructor(suspect)) {
						tobeRemoved.add(suspect);
					}else {
						//for constructors
						for(TestResult testRes: allFailedTests) {
							triggerTests.add(testRes.getName());
						}
						triggerTests = new ArrayList<String>(new HashSet<String>(triggerTests));
						suspect.setTriggerTests(triggerTests);
					}
				}else {
					
					triggerTests = new ArrayList<String>(new HashSet<String>(triggerTests));
					suspect.setTriggerTests(triggerTests);
					
					List<String> succTests = getCoveredSuccedTests(suspect);
					suspect.setCoveredSuccTest(succTests);
				}
			}
			suspects.removeAll(tobeRemoved);
						
			logAllSuspects(suspects);
			return suspects;
		}
		return null;
	}
	

	private boolean withinConstructor(Suspect suspect) {
		File srcFile = new File(projectConfig.getSrcRoot() + "/" + suspect.getFile());
		if(!srcFile.exists()) {
			return true;
		}
		
		try {
			String srcCode = FileUtil.readFileToString(srcFile);
			CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(srcCode, projectConfig.getTestJdkLevel(), ASTParser.K_COMPILATION_UNIT);
	
			ConstructorVisitor visitor = new ConstructorVisitor(cu);
			cu.accept(visitor);
			int line = suspect.getLine();
			for(Pair<Integer, Integer> pair: visitor.construcotorLines) {
				if(line > pair.first() && line < pair.second()) {
					
					suspect.setInConstructor(true);
					return true;
				}
			}
		} catch (Exception e) {
			
		}
		
		return false;
	}

	private void run(final GZoltar gzoltar, final String... testClasses) {//TODO:: opt
		for (String className : checkNotNull(testClasses)) {
			gzoltar.addTestToExecute(className);
			gzoltar.addClassNotToInstrument(className);
		}
		gzoltar.run();
		
		List<Statement> suspiciousStatements = gzoltar.getSuspiciousStatements();		
		List<TestResult> testResults = gzoltar.getTestResults();
		List<Statement> constructors = new ArrayList<>();
		
		int failed = 0;
		for (int i = 0; i < testResults.size(); i++) {
			TestResult testResult = testResults.get(i);
			if (testResult.wasSuccessful() == false) {
				System.out.println("Error test: " + testResult.getTrace().split("\n")[0]);
				String[] traces = testResult.getTrace().split("\n");
				String errmsg = traces[0];
				if (errmsg.contains("java.lang.NoClassDefFoundError:")
						|| errmsg.contains("java.lang.ExceptionInInitializerError")
						|| errmsg.contains("java.lang.IllegalAccessError")
						|| errmsg.contains("java.lang.VerifyError")) {
					testResult.setSuccessful(true);
				} else {
					failed++;
					// check invoked constructors
					String[] resNames = testResult.getName().split("#");
					String testClsName = resNames[0];
					String functionName = resNames[1];
					
					if(testClsName.startsWith("junit.")) {
						continue;
					}
					
					Pair<String, List<Integer>> clsNameToConstructorLines = InvocationCollector.getInvokedConstructorStmts(this.projectConfig, testClsName, functionName);
					if(clsNameToConstructorLines == null) {
						continue;
					}
					String clsName = clsNameToConstructorLines.first();
					List<Integer> constructorLines = clsNameToConstructorLines.second();

					Clazz clazz = new Clazz(clsName);
					String shortName = InvocationCollector.getTestedClsShortNameFromTest(testClsName);
					Method method = new Method(clazz, shortName + "()");

					Set<String> added = new HashSet<>();
					for (int line : constructorLines) {
						Statement statement = new Statement(method, line);
						statement.setCount(i, 1);
						statement.setCoverage(i);
						constructors.add(statement);
						added.add(clsName + ":" + line);
					}
					
					for(String tr: traces) {
						if(tr.contains("<init>")) {
							String[] lines = tr.trim().split("\\s+");
							int lastLesser = lines[1].lastIndexOf('<');
							String trCls = lines[1].substring(0, lastLesser - 1);
							String tail = tr.split(":")[1];
							int trLine = new Integer(tail.substring(0, tail.length() - 1));
							if(added.contains(trCls + ":" + trLine)) {
								continue;
							}
							Clazz trClazz = new Clazz(trCls);
							String trShortName = InvocationCollector.getTestedClsShortNameFromTest(trCls);
							Method trMethod = new Method(trClazz, trShortName + "()");
							Statement statement = new Statement(trMethod, trLine);
							statement.setCount(i, 1);
							statement.setCoverage(i);
							constructors.add(statement);
							added.add(clsName + ":" + trLine);
						}
					}
				}
			}
		}
		
		suspiciousStatements.addAll(constructors);//add all constructor lines to tail
		
		assert failed > 0: "NO FAILED TEST";
		
		this.stmtToFailTests = new HashMap<>();
		this.stmtToSuccTests = new HashMap<>();
		
		for (Statement statement : suspiciousStatements) {
			
			//System.out.println("CURRENT STMT: " + statement.getLabel() + " " + statement.getLineNumber());
			
			BitSet coverage = statement.getCoverage();
			int executedAndPassedCount = 0;
			int executedAndFailedCount = 0;
			int nextTest = coverage.nextSetBit(0);
			while (nextTest != -1) {
				TestResult testResult = testResults.get(nextTest);
				if (testResult.wasSuccessful()) {
					executedAndPassedCount++;
					appendToValueList(stmtToSuccTests, statement, testResult);
				} else {
					executedAndFailedCount++;
					//System.out.println( testResult.getTrace().split("\n")[0]);
					
					allFailedTests.add(testResult);
					
					appendToValueList(stmtToFailTests, statement, testResult);
				}
				nextTest = coverage.nextSetBit(nextTest + 1);
			}
			
		}
		
		assert stmtToFailTests.isEmpty() == false;
		assert stmtToSuccTests.isEmpty() == false;
		
	}
	
	private static void checkEnv() {
		String jdkVersion = System.getProperty("java.version");
		assert jdkVersion.contains("1.7"): jdkVersion;
	}
	
	private static boolean getReallyTestResult(TestResult testResult) {
		if (!testResult.wasSuccessful()) {
			String errmsg = testResult.getTrace().split("\n")[0];
			if (errmsg.contains("java.lang.NoClassDefFoundError:")
					|| errmsg.contains("java.lang.ExceptionInInitializerError")
					|| errmsg.contains("java.lang.IllegalAccessError")) {

				testResult.setSuccessful(true);
				return true;
			}
			return false;
		}
		return true;
	}
	
	private List<String> getCoveredSuccedTests(Suspect suspect) {
		List<String> succTests = new ArrayList<>();
		String clazz = suspect.getClassName();
		int line = suspect.getLine();
		
		for(Statement stmt: stmtToSuccTests.keySet()) {
			if(line == stmt.getLineNumber() && clazz.equals(stmt.getMethod().getParent().getLabel())){
				List<TestResult> coveredTests = stmtToSuccTests.get(stmt);
				for(TestResult testRes : coveredTests) {
					succTests.add(testRes.getName());
				}
			}
		}
		
		return succTests;
	}	
	
	private List<String> getFailedTests(Suspect suspect){
		List<String> triggerTests = new ArrayList<>();
		String clazz = suspect.getClassName();
		int line = suspect.getLine();
		
		for(Statement stmt: stmtToFailTests.keySet()) {
			
			//System.out.println(stmt.getMethod().getParent().getLabel() + " # " + stmt.getLineNumber());
			
			if(line == stmt.getLineNumber() && clazz.equals(stmt.getMethod().getParent().getLabel())){
				List<TestResult> failedTests = stmtToFailTests.get(stmt);
				for(TestResult testRes : failedTests) {
					triggerTests.add(testRes.getName());
				}
			}
		}
		
		return triggerTests;
	}

	private void appendToValueList(Map<Statement, List<TestResult>> map, Statement statement, TestResult testResult) {
		List<TestResult> valueList = null;
		if(map.containsKey(statement)) {
			valueList = map.get(statement);
		}else {
			valueList = new ArrayList<>();
			map.put(statement, valueList);
		}
		valueList.add(testResult);
	}
	
	private class ConstructorVisitor extends ASTVisitor{
		private CompilationUnit cu;
		public List<Pair<Integer, Integer>> construcotorLines = new ArrayList<>();
		
		public ConstructorVisitor(CompilationUnit cu) {
			this.cu = cu;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			if(node.isConstructor()) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				Pair<Integer, Integer> linePair = Pair.from(start, end);
				construcotorLines.add(linePair);
			}
			return super.visit(node);
		}
	}

}
