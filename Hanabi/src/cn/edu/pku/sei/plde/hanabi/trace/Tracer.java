package cn.edu.pku.sei.plde.hanabi.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.trace.runtime.RuntimeValues;
import cn.edu.pku.sei.plde.hanabi.utils.CodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.CollectionUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;

public class Tracer {
	
	private enum TraceRetState{TRACE_SUCC, TRACE_IMPL_ERR, NO_SUCC_TEST};
	
	private ProjectConfig projectConfig;
	private TestRunner testRunner;
	private TestOutput testOutput;
	private Suspect suspect;
	private File srcFile;
	private String srcCode;
	private String[] srcCodeLines;
	private File srcClsFile;

	private List<TraceResult> successfulTraces;
	private List<TraceResult> failedTraces;
	
	private boolean modifiedSrc;
	
	public Tracer(ProjectConfig projectConfig, TestRunner testRunner, TestOutput testOutput, Suspect suspect, File srcFile, String srcCode, String[] srcCodeLines, File srcClsFile) {
		this.projectConfig = projectConfig;
		this.testRunner = testRunner;
		this.testOutput = testOutput;
		this.suspect = suspect;
		this.srcFile = srcFile;
		this.srcCode = srcCode;
		this.srcCodeLines = srcCodeLines;
		this.srcClsFile = srcClsFile;
	}
	
	public Map<String, Set<String>> getExceptionValue(){
		TraceRetState traceRes = getTrace();
		
		if(modifiedSrc) {//restore source file when it has been changed
			FileUtil.writeStringToFile(this.srcFile, srcCode, false);
		}
		
		Map<String, Set<String>> excepValueMap = new HashMap<>();

		if(traceRes == TraceRetState.TRACE_IMPL_ERR) {
			System.out.println("ERROR IN TRACE IMPL!");
		}else if(traceRes == TraceRetState.NO_SUCC_TEST) {
			System.out.println("NO SUCC TEST!");
			
			//if there is not failed test, still need to watch each occurred variable
			for(TraceResult failed : failedTraces) {
				if(hasNoRelationWithTheSuspect(failed)) {
					continue;
				}
				excepValueMap.putAll(failed.getResults());
			}
			
		}else {
			Map<String, Set<String>> allSuccMap = combineAllSuccTraces();
			addExcepValByNoneValueOverlap(allSuccMap, excepValueMap);
			//if collected none, select by frequency
		}
		
		return excepValueMap;
	}
	
	private void addExcepValByNoneValueOverlap(Map<String, Set<String>> allSuccMap, Map<String, Set<String>> excepValueMap) {
		for(TraceResult failed : failedTraces) {
			if(hasNoRelationWithTheSuspect(failed)) {
				continue;
			}
			Map<String, Set<String>> failedMap = failed.getResults();
			for(Entry<String, Set<String>> failedEntry: failedMap.entrySet()) {
				String varName = failedEntry.getKey();
				if(varName.contains(" instanceof ")) {
					continue;
				}
				if(!allSuccMap.containsKey(varName)) {
					continue;
				}
				if(varName.endsWith(".length()")) {// is string
					if(!allSuccMap.containsKey(varName)) {
						continue;
					}
					Set<String> failedTrace = failedMap.get(varName);
					if(CollectionUtil.hasDifferent(allSuccMap.get(varName), failedTrace)) {
						if(!failedTrace.contains("0")) {
							excepValueMap.put(varName, failedEntry.getValue());
						}
					}
					
				} 
				if(varName.endsWith("!=null")) {
					Set<String> succValueSet = allSuccMap.get(varName);
					Set<String> failedValueSet = failedEntry.getValue();
					if(CollectionUtil.hasDifferent(succValueSet, failedValueSet)) {
						excepValueMap.put(varName, failedEntry.getValue());
					}
				} else {
					Set<String> succValueSet = allSuccMap.get(varName);
					Set<String> failedValueSet = failedEntry.getValue();
					if(failedValueSet.toString().contains(".")) {//is float value
						if(CollectionUtil.hasOverlap(succValueSet, failedValueSet)) {
							continue;
						}
					}else {//is integer
						if(!CollectionUtil.hasDifferent(succValueSet, failedValueSet)) {
							continue;
						}
					}
					
					excepValueMap.put(varName, failedEntry.getValue());
				}
			}
		}
	}
	
	private void addExcepValByValueFrequency(Map<String, Set<String>> allSuccMap, Map<String, Set<String>> excepValueMap) {
		for(TraceResult failed : failedTraces) {
			if(hasNoRelationWithTheSuspect(failed)) {
				continue;
			}
			Map<String, Set<String>> failedMap = failed.getResults();
			for(Entry<String, Set<String>> failedEntry: failedMap.entrySet()) {
				String varName = failedEntry.getKey();
				if(!allSuccMap.containsKey(varName)) {
					continue;
				}
				//traced values is overflow
				if(failedEntry.getValue().size() < (RuntimeValues.MAX_TRACED_ITEM / 100)) {
					excepValueMap.put(varName, failedEntry.getValue());
				}
			}
		}
	}
	
	
	private boolean hasNoRelationWithTheSuspect(TraceResult failedTrace) {
		String tracedMtd = failedTrace.getTestClass() + "#" + failedTrace.getTestMethod();
		for(String trigger : this.suspect.getTriggerTests()) {
			if(trigger.equals(tracedMtd)) {
				return false;
			}
		}
		return true;
	}

	private Map<String, Set<String>> combineAllSuccTraces() {
		Map<String, Set<String>> allSuccedMap = new HashMap<>();
		for(TraceResult succed : successfulTraces) {
			Map<String, Set<String>> succedMap = succed.getResults();
			for(Entry<String, Set<String>> entry: succedMap.entrySet()) {
				String curValName = entry.getKey();
				Set<String> curSet;
				if(allSuccedMap.containsKey(curValName)) {
					curSet = allSuccedMap.get(curValName);
				} else {
					curSet = new HashSet<String>();
				}
				curSet.addAll(entry.getValue());
				allSuccedMap.put(curValName, curSet);
			}
		}
		return allSuccedMap;
	}
	
	
	
	/**
	 * @return TraceRetState
	 */
	private TraceRetState getTrace() {
		if(!instrument()) {
			return TraceRetState.TRACE_IMPL_ERR;
		}
		
		if(!compile()) {
			return TraceRetState.TRACE_IMPL_ERR;
		}
		
		List<String> coveredTests = suspect.getCoveredSuccTest();
		List<String> failedTest = suspect.getTriggerTests();
		
		failedTraces = forEachTest(failedTest, false);
		if(failedTraces.isEmpty()) {
			return TraceRetState.TRACE_IMPL_ERR;
		}
		
		successfulTraces = forEachTest(coveredTests, true);
		if(successfulTraces.isEmpty()) {
			return TraceRetState.NO_SUCC_TEST;
		}
		
		return TraceRetState.TRACE_SUCC;
	}
	
	private List<TraceResult> forEachTest(List<String> tests, boolean successful) {
		List<TraceResult> results = new ArrayList<>();
		int line = suspect.getLine();
		for(String test : tests) {
			try {
				runTestMethod(test, line);
				TraceResult tr = loadTrace(successful, test, line);
				if(tr != null) {
					results.add(tr);
				}
			}catch (Exception e) {
				System.err.println("\nRUNNING ERROR IN TRACER");
				continue;
				
			}
		}
		return results;
	}
	
	private TraceResult loadTrace(boolean successful, String test, int line) {
		File file = new File(MethodTestRunner.generateTraceFilePath(this.projectConfig.getProjectName(), test, line));
		if(!file.exists()) {
			System.out.println("NO TRACE EXISTS FOR: " + test + " @ " + line);
			return null;
		}
		String testCls = test.split("#")[0];
		String testMtd = test.split("#")[1];
		TraceResult result = new TraceResult(successful, line, testCls, testMtd);
		result.putinTrace(file);
		return result;
	}

	private boolean instrument(){
		try{
			int fixedLine = fixLine(suspect.getLine());
			String excepName = this.testOutput.getExceptionName();
			ExceptionValueTraceVisitor visitor = new ExceptionValueTraceVisitor(fixedLine, excepName);
			//trace the condition
			CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(this.srcCode, projectConfig.getTestJdkLevel(), ASTParser.K_COMPILATION_UNIT);
			cu.accept(visitor);
			if(visitor.getHit() == false){//suspect not in if statement
				return false;
			}
			FileUtil.writeStringToFile(this.srcFile, cu.toString(), false);
			modifiedSrc = true;
		}catch(Throwable t){
			return false;
		}
		return true;
	}
	
	private int fixLine(int line){
		int currentLine = -1;
		for(currentLine = srcCodeLines.length; currentLine >= 1; currentLine--){
			if(currentLine > line){
				continue;
			}else{
				String curLineStr = srcCodeLines[currentLine - 1].trim();
				if(skipNoneMeaningLine(curLineStr)){
					continue;
				}else{
					break;
				}
			}
			
		}
		
		if(line != currentLine) {
			System.out.println("LINE NUM CHANGED: " + this.srcCodeLines[line - 1] + " ==>> " + this.srcCodeLines[currentLine - 1]);
		}
		
		return currentLine;
	}
	
	private boolean skipNoneMeaningLine(String line) {
		return line.length() == 0 || line.equals("}") || line.equals("{") || line.startsWith("//") || line.startsWith("/*") 
				|| (line.contains("}") && line.contains("{") && !line.contains("[]")) || line.startsWith("throw ");
	}
	
	private boolean compile(){
		try {
			String cp = System.getProperty("user.dir") + "/bin/";
			if(projectConfig.getClassPaths().contains(cp)) {
				cp = null;
			}
			return CodeUtil.javac(projectConfig, srcFile, srcClsFile, Arrays.asList(cp));
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param test : TEST_CLASS#TEST_METHOD
	 * @param tracedLine
	 * @throws Exception
	 */

	private boolean runTestMethod(String test, int tracedLine) throws Exception{
		File traceFile = new File(MethodTestRunner.generateTraceFilePath(this.projectConfig.getProjectName(), test, tracedLine));
		if(traceFile.exists()) {
			traceFile.delete();
		}
		String testCls = test.split("#")[0];
		String testMtd = test.split("#")[1];
		String config = "TRACE " + tracedLine;
		String bugName = this.projectConfig.getProjectName();
		return testRunner.runSingleTestMethodByMethodTestRunner(bugName, testCls, testMtd, config);
	}
	
}
