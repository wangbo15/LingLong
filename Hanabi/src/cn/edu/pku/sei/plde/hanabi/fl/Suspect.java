package cn.edu.pku.sei.plde.hanabi.fl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.pku.sei.plde.hanabi.fixer.pattern.FixPattern;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;

public class Suspect {
	
	private String file;
	private String className;
	private int line;
	private double score;
	
	private boolean inConstructor;
	
	//sth like 'org.apache.commons.math3.util.MathArraysTest#testLinearCombinationWithSingleElementArray'
	private List<String> triggerTests;
		
	private List<String> coveredSuccTest;
	
	private Set<Class<? extends FixPattern>> inapplicablePattern = new HashSet<>();
	
	public Suspect(String className, int line, double score, List<String> triggerTests, List<String> coveredSuccTest) {
		this.className = className;
		this.file = FileUtil.classNameToItsSrcPath(className);
		this.line = line;
		this.score = score;
		this.triggerTests = triggerTests;
		this.coveredSuccTest = coveredSuccTest;
	}
	
	public Suspect clone(int line) {
		Suspect newed = new Suspect(this.getClassName(), line, this.getScore(), this.getTriggerTests(), this.getCoveredSuccTest());
		newed.setInConstructor(this.inConstructor);
		return newed;
	}
	
	public String getClassName() {
		return className;
	}
	
	public int getLine() {
		return line;
	}
	
	public String getFile() {
		return file;
	}

	public double getScore() {
		return score;
	}
	
	public List<String> getTriggerTests() {
		return triggerTests;
	}
	
	public List<String> getCoveredSuccTest(){
		return coveredSuccTest;
	}
	
	public boolean isInConstructor() {
		return inConstructor;
	}

	public void setInConstructor(boolean inConstructor) {
		this.inConstructor = inConstructor;
	}

	/**
	 * @param triggerTests
	 */
	public void setTriggerTests(List<String> triggerTests) {
		this.triggerTests = triggerTests;
	}

	/**
	 * @param coveredSuccTest
	 */
	public void setCoveredSuccTest(List<String> coveredSuccTest) {
		this.coveredSuccTest = coveredSuccTest;
	}
	
	/**
	 * inapplicable patterns
	 * @return
	 */
	public Set<Class<? extends FixPattern>> getInapplicablePattern() {
		return inapplicablePattern;
	}

	@Override
	public String toString() {
		return className + " # " + line + " # " + score + " : " + triggerTests;
	}
	
	public String generateLocation() {
		return className + "#" + line;
	}
}
