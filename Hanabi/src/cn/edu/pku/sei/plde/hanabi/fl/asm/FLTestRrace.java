package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

public class FLTestRrace {
	private String test;
	private String testClass;
	private String testMtd;
	private boolean success;
	
	private Set<SourceLocation> coveredSrcLocs = new HashSet<>();

	public FLTestRrace(String testClass, String testMtd, boolean success) {
		this.testClass = testClass;
		this.testMtd = testMtd;
		this.test = testClass + "#" + testMtd;
		this.success = success;
	}
	
	public String getTest() {
		return test;
	}

	public String getTestClass() {
		return testClass;
	}

	public String getTestMtd() {
		return testMtd;
	}
	
	public boolean isSuccess() {
		return success;
	}

	public Set<SourceLocation> getCoveredSrcLocs() {
		return coveredSrcLocs;
	}

	@Override
	public String toString() {
		return "TEST: " + test + " RES: " + success + ":\n" 
				+ "COVERD: \n[\n" + coveredSrcLocs + "\n]\n";
	}
	
}
