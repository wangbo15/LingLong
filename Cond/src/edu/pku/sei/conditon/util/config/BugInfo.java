package edu.pku.sei.conditon.util.config;

import java.util.List;

public class BugInfo {
	private String key;
	private String srcRoot;
	private List<String> buggyFile;
	private List<Integer> buggyLine;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSrcRoot() {
		return srcRoot;
	}
	public void setSrcRoot(String srcRoot) {
		this.srcRoot = srcRoot;
	}
	public List<String> getBuggyFile() {
		return buggyFile;
	}
	public void setBuggyFile(List<String> buggyFile) {
		this.buggyFile = buggyFile;
	}
	public List<Integer> getBuggyLine() {
		return buggyLine;
	}
	public void setBuggyLine(List<Integer> buggyLine) {
		this.buggyLine = buggyLine;
	}
	
	@Override
	public String toString() {
		return "BugInfo [key=" + key + ", srcRoot=" + srcRoot + ", buggyFile=" + buggyFile + ", buggyLine=" + buggyLine
				+ "]";
	}
	
	
}
