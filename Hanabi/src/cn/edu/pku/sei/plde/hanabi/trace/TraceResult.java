package cn.edu.pku.sei.plde.hanabi.trace;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.pku.sei.plde.hanabi.trace.runtime.RuntimeValues;
import cn.edu.pku.sei.plde.hanabi.utils.CollectionUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;

public class TraceResult {
	
	private final static int MAX_RECORD_TIME = 100;
	
	private Map<String, Set<String>> results = new HashMap<String, Set<String>>(); // variable_str => value list
	private final boolean testSucced;
	public int traceLine;
	public final String testClass;
	public final String testMethod;
	
	public TraceResult(boolean testResult, int traceLine, String testClass, String testMethod) {
		this.testSucced = testResult;
		this.traceLine = traceLine;
		this.testClass = testClass;
		this.testMethod = testMethod;
	}
	
	/**
	 * @return Map: var name => its value list
	 */
	public Map<String, Set<String>> getResults() {
		return results;
	}

	public boolean isTestSucced() {
		return testSucced;
	}

	public int getTraceLine() {
		return traceLine;
	}

	public String getTestClass() {
		return testClass;
	}

	public String getTestMethod() {
		return testMethod;
	}
	
	public void putinTrace(File file) {
		List<String> traceFileLines = FileUtil.readFileToStringList(file);
		if(traceFileLines.size() <= 2) {
			return;
		}
		
		int runTime = 0;
		for(int i = 1; i < traceFileLines.size() - 1; i++) {
			String str = traceFileLines.get(i);
			if(str.equals(RuntimeValues.TRACE_HEAD)) {
				runTime++;
				continue;
			}
			if(str.equals(RuntimeValues.TRACE_TAIL)) {
				continue;
			}
			if(runTime > MAX_RECORD_TIME) {
				break;
			}
			str = str.substring(1, str.length() - 1);
			String key = str.split(",")[0].trim();
			String value = str.split(",")[1].trim();
			CollectionUtil.putInToValueSet(results, key, value);
		}
	}

	@Override
	public String toString() {
		return  (testSucced ? "SUCC-" : "FAIL-") + testClass + "#" + testMethod + ": " + results;
	}
	
}
