package cn.edu.pku.sei.plde.hanabi.fl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond.InsertRetFixPattern;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond.ModifyIfCondFixPattern;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.ret.ConstantReplaceFixPattern;
import cn.edu.pku.sei.plde.hanabi.fl.asm.FLStmt;
import cn.edu.pku.sei.plde.hanabi.fl.asm.FLTestRrace;
import cn.edu.pku.sei.plde.hanabi.fl.asm.SourceLocation;
import cn.edu.pku.sei.plde.hanabi.fl.asm.instru.Instrumenter;
import cn.edu.pku.sei.plde.hanabi.fl.asm.instru.exrt.Tracer;
import cn.edu.pku.sei.plde.hanabi.fl.asm.metric.Metric;
import cn.edu.pku.sei.plde.hanabi.fl.asm.metric.Ochiai;
import cn.edu.pku.sei.plde.hanabi.trace.AngelicInstruVisitor;
import cn.edu.pku.sei.plde.hanabi.utils.ClassPathHacker;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.ProjectRepre;

public class ASMFaultLocation extends FaultLocation {

	private static final String KEY_SRC = "src";
	private static final String KEY_TEST = "test";

	public enum InstruRange {
		FILE, 		// SINGLE FILE
		PACKAGE, 	// THE PACKAGE
		ALL 		// INSTRU ALL
	}
	
	private InstruRange srcRange = InstruRange.ALL;
	private InstruRange testRange = InstruRange.PACKAGE;
	
	private List<TestOutput> testResults;
	private TestRunner runner;
	
	private static final Metric METRIC = Ochiai.getOchiaiInstance();
	
	/**
	 * Able to instrument at different source code range
	 * @param projectConfig
	 * @param runner
	 * @param srcRange
	 * @param testRange
	 */
	public ASMFaultLocation(ProjectConfig projectConfig, TestRunner runner, InstruRange srcRange, InstruRange testRange) {
		super(projectConfig);
		this.srcRange = srcRange;
		this.testRange = testRange;
		init(projectConfig, runner);
	}

	public ASMFaultLocation(ProjectConfig projectConfig, TestRunner runner) {
		super(projectConfig);
		init(projectConfig, runner);
	}
	
	/**
	 * copy cn.edu.pku.sei.plde.hanabi.fl.asm.instru.exrt.Tracer to 'test/resources'
	 */
	public static void copyTracerDotClassToResource(ProjectConfig projectConfig) {
		// copy cn.edu.pku.sei.plde.hanabi.fl.asm.instru.exrt.Tracer to 'test/resources'
		InputStream in = ASMFaultLocation.class.getClassLoader().getResourceAsStream("./cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer.class");
		
		File resource = new File(projectConfig.getTestSrcRoot().getParentFile().getAbsolutePath() + "/resources/");
		
		assert resource.exists();
		
		File tracer = new File(resource.getAbsolutePath() + "/cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer.class");
		
		boolean succ = FileUtil.inputStreamToFile(in, tracer);
		
		assert succ;
	}
	
	private static final long ONE_HOUR = 60 * 60 * 1000;
	
	public void filter(File srcRoot, List<Suspect> suspects) {
		// FileName to lines
		Map<File, List<String>> linesCache = new HashMap<>();
		boolean applyInsert = true;
		for(TestOutput output: this.testResults) {
			File testSrcFile = new File(projectConfig.getTestSrcRoot() + "/" + output.getFailTestSrcPath());
			String testCode = FileUtil.loadFileStrFromCache(testSrcFile);
			
			List<String> lines;
			if(linesCache.containsKey(testSrcFile)) {
				lines = linesCache.get(testSrcFile);
			}else {
				if(linesCache.size() > 12) {
					linesCache.clear();
				}
				lines = Arrays.asList(testCode.split("\n"));
				linesCache.put(testSrcFile, lines);
			}
			
			int failLine = output.getFailAssertLine();
			int tmpIdx = failLine - 1;
			if(lines.size() > tmpIdx && tmpIdx >= 0) {
				String line = lines.get(tmpIdx);
				if(!line.trim().startsWith("assertEquals")) {
					applyInsert = false;
				}
			}
			
		}
		
		Iterator<Suspect> it = suspects.iterator();
		
		long start = System.currentTimeMillis();
		
		int ifCount = 0;
		while(it.hasNext()) {
			
			Suspect sus = it.next();
			
			long currTime = System.currentTimeMillis();
			if(currTime - start > ONE_HOUR) {
				// remove until the end
				System.out.println("FILTER SUSPECT BY TIME: " + sus);
				it.remove();
				continue;
			}
			
			if(!applyInsert) {
				sus.getInapplicablePattern().add(InsertRetFixPattern.class);
			}
			
			String srcFileName = sus.getFile();
			File srcFile = new File(srcRoot.getAbsolutePath() + "/" + srcFileName);
			
			if(!srcFile.exists()) {
				it.remove();
				continue;
			}
			
			String srcCode = FileUtil.loadFileStrFromCache(srcFile);
			List<String> lines;
			if(linesCache.containsKey(srcFile)) {
				lines = linesCache.get(srcFile);
			}else {
				if(linesCache.size() > 12) {
					linesCache.clear();
				}
				lines = Arrays.asList(srcCode.split("\n"));
				linesCache.put(srcFile, lines);
			}
			
			int idx = sus.getLine() - 1;
			String line = lines.get(idx);
			
			boolean skipP2 = false;
			if(line.contains("if (") || line.contains("if(")) {
				ifCount++;
				if(ifCount > 1000) {
					skipP2 = true;
				} 
//				else if(processAngelic(sus, srcFile, srcCode) == false){
//					// can not pass angelic
//					skipP2 = true;
//				}
			} else {
				skipP2 = true;
			}
			if(skipP2) {
				sus.getInapplicablePattern().add(ModifyIfCondFixPattern.class);
			}
			
			if(line.trim().contains("return ")) {
				if(line.contains("(") && line.contains("")) {
					sus.getInapplicablePattern().add(ConstantReplaceFixPattern.class);
				}
			} else {
				sus.getInapplicablePattern().add(ConstantReplaceFixPattern.class);
			}

//			if(sus.getInapplicablePattern().contains(ModifiyIfCondFixPattern.class)) {
//				it.remove();
//				System.out.println("FILTER SUSPECT BY P2: " + sus);
//			}
			
			if(sus.getInapplicablePattern().size() == 3) {
				it.remove();
				System.out.println("FILTER SUSPECT BY ALL: " + sus);
			} else if(sus.getInapplicablePattern().contains(ModifyIfCondFixPattern.class)) {
				System.out.println("FILTER SUSPECT BY P2: " + sus);
			} 
		}
		
		linesCache.clear();
		linesCache = null;
	}
	
	@Override
	public List<Suspect> getAllSuspects() {
		//compute ochiai score and sort the stmts
		List<FLStmt> sortedFLStmts = loadTraceResults();
		
		List<Suspect> result = new ArrayList<>();
		
		// new Suspect for each FLStmt
		for(FLStmt stmt : sortedFLStmts) {
			Suspect sus = new Suspect(stmt.getClassName(), 
					stmt.getLineNum(), 
					stmt.getSuspiciousness(), 
					stmt.getTriggerTests(), 
					stmt.getCoveredSuccTest());
			
			result.add(sus);
		}
		
		// refine
		OffTheShellFL.filtrate(projectConfig, result);
		
		System.out.println(">>>>>>>> FILTER FROM SIZE: " + result.size());
		filter(projectConfig.getSrcRoot(), result);
		System.out.println(">>>>>>>> FILTER TO SIZE: " + result.size());
		
		logAllSuspects(result);
		
		// clean the instrumented classes and modified java files
		CmdUtil.runByJava7(projectConfig.getCleanCmd(), projectConfig.getRoot());
		CmdUtil.runCmd("git checkout **/*.java", projectConfig.getRoot());
		
		return result;
	}
	
	private boolean processAngelic(Suspect sus, File srcFile, String srcCode) {
		List<String> lines = Arrays.asList(srcCode.split("\n"));

		int idx = sus.getLine() - 1;
		String line = lines.get(idx);
		if(!(line.contains("if (") || line.contains("if("))) {
			// not a if stmt
			return false;
		}
		
		// either true angelix or false angelix, leave it
		boolean trueRes = mutatedAndRun(srcFile, srcCode, lines, idx, true);
		if(trueRes) {
			return true;
		}
		
		boolean falseRes = mutatedAndRun(srcFile, srcCode, lines, idx, false);
		if(falseRes) {
			return true;
		}
		
		return false;
	}
	
	private boolean mutatedAndRun(File srcFile, String srcCode, List<String> lines, int idx, boolean angelix) {
		String line = lines.get(idx);
		String lineTmp = line.replaceAll("\\s", "");
		
		if(lineTmp.contains("elseif")) {
			lineTmp = lineTmp.replaceAll("elseif", "else if");
		}
		
		int start = lineTmp.indexOf("if(");
		int end = lineTmp.lastIndexOf(")");
		int ifBound = start + 3;
		
		if(start < 0 || end < 0 || ifBound >= lineTmp.length()) {
			return false;
		}
		
		String newLine = lineTmp.substring(0, ifBound) + angelix + lineTmp.substring(end);
		lines.set(idx, newLine);
		
		boolean result = false;

		String newSrcCode = StringUtil.join(lines, "\n");
		FileUtil.writeStringToFile(srcFile, newSrcCode, false);
		
		for(TestOutput output: testResults) {
			runner.runSingleTestCMD(output.getFailTest());
		}
		
		if(!projectConfig.getTestOutputFile().exists()){
			result = false;
		} else {
			List<TestOutput> newRes = runner.getErrorTestOutput();
			if(newRes.isEmpty()) {
				result = true;
			}
		}
		
		// recover
		FileUtil.writeStringToFile(srcFile, srcCode, false);
		
		return result;
	}
	
	private void init(ProjectConfig projectConfig, TestRunner runner) {
		// first run all the tests
		testResults = runner.runAllAndGetErrorTestOutput();
		if(testResults == null || testResults.isEmpty()) {
			return;
		}
		
		assert runner != null;
		this.runner = runner;
		
		// add classpath dynamically
		String classPaths = projectConfig.getClassPaths();
		ClassPathHacker.loadClassPaths(classPaths);

		copyTracerDotClassToResource(this.projectConfig);

		instrument(testResults);
		
		if(this.testRange != InstruRange.FILE) {
			runner.runAllTestCMD();
		} else {
			for(TestOutput output: testResults) {
				runner.runSingleTestSuiteCMD(output.getFailTestCls());
			}
		}
		
		// clean the intrumented classes
		CmdUtil.runByJava7(projectConfig.getCleanCmd(), projectConfig.getRoot());
		
		// remove classpath dynamically
		//TODO: change to cmd runner, instrument externally
		System.gc();
		ClassPathHacker.removeClassPaths(classPaths);
		System.gc();
	}
	
	private void instrument(List<TestOutput> results) {
		//get instrumented sources and tests
		Map<String, Set<File>> insruFiles = getInstrumentedSrcAndTest(results);
		
		Set<File> srcList = insruFiles.get(KEY_SRC);
		assert srcList.isEmpty() == false; 
		for(File src: srcList) {
			System.out.println("INSTRUMENTING SRC: " + src.getAbsolutePath());
			Instrumenter.instrumentSrcClass(src);
		}

		System.out.println("################## SRC INSTRUMENT FINISHED #################");
		
		Set<File> testList = insruFiles.get(KEY_TEST);
		
		ProjectRepre projRep = this.projectConfig.getProInfo().getProjectRepre();
		assert testList.isEmpty() == false;
		for(File test: testList) {
			// skip inner-class files of tests
			if(test.getName().contains("$")) {
				continue;
			}
			
			String absPath = test.getAbsolutePath();
			
			System.out.println("INSTRUMENTING TEST: " + absPath);
			
			String rootPath = this.projectConfig.getTestTargetRoot().getAbsolutePath();
			
			assert absPath.startsWith(rootPath);
			
			//remove root + '/'
			String path = absPath.substring(rootPath.length() + 1);
			
			//remove '.class'
			path = path.substring(0, path.length() - ".class".length());
			String clsName = path.replaceAll("/", "\\.");
			
			boolean isJunit3 = false;			
			if(projRep.fullNameToClazzesMap.containsKey(clsName)) {
				ClassRepre cr = projRep.fullNameToClazzesMap.get(clsName);
				ClassRepre ancestor = cr.getTopFatherCls();
				if(ancestor != null && ancestor.getFullName().equals("junit.framework.TestCase")) {
					isJunit3 = true;
				}
			}
			Instrumenter.instrumentTestClass(test, isJunit3);
		}
	}
	
	/**
	 * @param results: outputs of failed test 
	 * @return get instrumented sources and tests, result.get(KEY_SRC), result.get(KEY_TEST)
	 */
	private Map<String, Set<File>> getInstrumentedSrcAndTest(List<TestOutput> results){
		Map<String, Set<File>> result = new HashMap<>();		
		Set<File> srcList = new HashSet<>();
		Set<File> testList = new HashSet<>();
		result.put(KEY_SRC, srcList);
		result.put(KEY_TEST, testList);
		
		for(TestOutput output : results) {
			String failedTestCls = output.getFailTestCls();
			
			assert failedTestCls.endsWith("Test");
			
			// process test instru
			if(this.testRange == InstruRange.ALL) {
				List<File> allTest = new ArrayList<>(50);
				FileUtil.getAllSubFilesInFolder(projectConfig.getTestTargetRoot(), ".class", allTest);
				testList.addAll(allTest);
			} else {
				File testClass = classNameToClassFile(failedTestCls, true);
				
				if(this.testRange == InstruRange.PACKAGE) {
					// add the package
					testList.addAll(FileUtil.getFilesWithinSameFolder(testClass, ".class"));

				} else {
					if(testClass.exists()) {
						testList.add(testClass);
					}
				}
			}
			
			// process src instru
			if(this.srcRange == InstruRange.ALL) {
				List<File> allSrcCls = new ArrayList<>(50);
				FileUtil.getAllSubFilesInFolder(projectConfig.getTargetRoot(), ".class", allSrcCls);
				srcList.addAll(allSrcCls);
			} else {
				String srcCls = failedTestCls.substring(0, failedTestCls.length() - "Test".length());
				File srcClass = classNameToClassFile(srcCls, false);
				
				if(this.srcRange == InstruRange.PACKAGE) {
					// add the package
					srcList.addAll(FileUtil.getFilesWithinSameFolder(srcClass, ".class"));
					
				} else {
					if(srcClass.exists()) {
						srcList.add(srcClass);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * @param className
	 * @param isTest: true: is test class, false: is source class 
	 * @return
	 */
	private File classNameToClassFile(String className, boolean isTest) {
		className = className.replaceAll("\\.", "/");
		File result;
		if(isTest) {
			result = new File(this.projectConfig.getTestTargetRoot().getAbsolutePath() + "/" + className + ".class");
		}else {
			result = new File(this.projectConfig.getTargetRoot().getAbsolutePath() + "/" + className + ".class");
			
			if(!result.exists()) {
				// find in the father folder
				int tailIdx = className.lastIndexOf("/");
				String tail = className.substring(tailIdx + 1);
				className = className.substring(0, tailIdx);
				int headIdx = className.lastIndexOf("/");
				String head = className.substring(0, headIdx);
				
				className = head + "/" + tail;
				
				result = new File(this.projectConfig.getTargetRoot().getAbsolutePath() + "/" + className + ".class");
			}
			
		}
		
		assert result.exists(): className + "\nIS_TEST: " + isTest;
		return result;
	}
	
	
	private List<FLStmt> loadTraceResults() {
		File[] files = Tracer.DUMP_FOLDER.listFiles();
		if(files == null || files.length == 0) {
			return Collections.emptyList();
		}
		
		List<FLTestRrace> traces = new ArrayList<>();
		
		Set<FLStmt> allFLStmts = new HashSet<>();
		
		Set<String> failureTestCls = new HashSet<>();
		
		int failedTestNum = 0;
		int allTestNum = 0;
		for(File f : files) {
			FLTestRrace tr = parseTraceFile(f);
			traces.add(tr);
			
			allTestNum++;
			
			boolean succ = tr.isSuccess();
			if(!succ) {
				failedTestNum++;
				String testName = tr.getTestClass();
				if(testName.endsWith("Test")) {
					failureTestCls.add(testName.substring(0, testName.length() - "Test".length()));
				}
			}
			
			// collect all the covered stmts
			for(SourceLocation sl : tr.getCoveredSrcLocs()) {
				FLStmt flStmt = FLStmt.getFLStmtFromCache(METRIC, sl);
				if(succ) {
					flStmt.getCoveredSuccTest().add(tr.getTest());
				}else {
					flStmt.getTriggerTests().add(tr.getTest());
				}
				allFLStmts.add(flStmt);
			}
		}
		
		List<FLStmt> sorted = new ArrayList<>();
		
		for(FLStmt flStmt : allFLStmts) {
			
			for(FLTestRrace tr : traces) {
				setVaulesOfFLStmt(flStmt, tr);
			}
			
			// (ef + nf) = the total number of failed test
			assert flStmt.getExecutedFailedTestNum() + flStmt.getNoExecutedFailedTestNum() == failedTestNum;
			// (ep + np) = all the passed test number 
			assert flStmt.getExecutedPassTestNum() + flStmt.getNoExecutedPassTestNum() == allTestNum - failedTestNum;
			
			flStmt.computeSuspiciousness();
			
			if(flStmt.getSuspiciousness() > 0) {
				sorted.add(flStmt);
			}
			
		}
		
		Collections.sort(sorted, new Comparator<FLStmt>() {
			@Override
			public int compare(FLStmt s1, FLStmt s2) {
				return Double.compare(s2.getSuspiciousness(), s1.getSuspiciousness());
			}
		});
		
		List<FLStmt> priorList = new ArrayList<>();
		for(FLStmt stmt : sorted) {
			if(failureTestCls.contains(stmt.getClassName())){
				priorList.add(stmt);
			}
		}
		
		sorted.removeAll(priorList);
		sorted.addAll(0, priorList);

		return sorted;
	}
	
	private void setVaulesOfFLStmt(FLStmt flStmt, FLTestRrace tr) {
		boolean isCovered = tr.getCoveredSrcLocs().contains(flStmt.getSourceLocation());
		if(isCovered) {
			if(tr.isSuccess()) {// pass
				int val = flStmt.getExecutedPassTestNum() + 1;
				flStmt.setExecutedPassTestNum(val);
			}else {// failure
				int val = flStmt.getExecutedFailedTestNum() + 1;
				flStmt.setExecutedFailedTestNum(val);
			}
		}else {// not covered
			if(tr.isSuccess()) {// pass
				int val = flStmt.getNoExecutedPassTestNum() + 1;
				flStmt.setNoExecutedPassTestNum(val);
			}else {// failure
				int val = flStmt.getNoExecutedFailedTestNum() + 1;
				flStmt.setNoExecutedFailedTestNum(val);
			}
		}
	}

	private FLTestRrace parseTraceFile(File traceFile) {
		String name = traceFile.getName();
		assert name.endsWith("-true.txt") || name.endsWith("-false.txt"): name;
		
		boolean success = name.endsWith("-true.txt") ? true : false;
		
		String[] arr = name.split("-");
		
		//testCls-testMtd-succ.txt
		assert arr.length == 3;
		String testCls = arr[0];
		String testMtd = arr[1];
		
		FLTestRrace tr = new FLTestRrace(testCls, testMtd, success);
		
		List<String> lines = FileUtil.readFileToStringList(traceFile);
		
		for(String line: lines) {
			line = line.trim();
			if(line.equals(">>") || line.equals("<<")) {
				continue;
			}
			String[] lineArr = line.split("#");
			
			assert lineArr.length == 2: "FILE: " + name + " LINE: " + line;
			
			int lineNum = Integer.valueOf(lineArr[1]);
			SourceLocation sl = SourceLocation.newSourceLocation(lineArr[0], lineNum);
			
			tr.getCoveredSrcLocs().add(sl);
		}
		
		return tr;
	}
}
