package edu.pku.sei.conditon.dedu.predall;

import java.util.Collections;
import java.util.List;

public class PredAllResult {
	private String oracle = "";
	private String score = Double.NEGATIVE_INFINITY + "";
	private int ranking = -1;
	private List<String> conditions = Collections.emptyList();
	private List<String> predSequence = Collections.emptyList();

	private int compileTime = 0;
	private int compileFailingTime = 0;
	
	public PredAllResult(String oracle) {
		this.oracle = oracle;
	}
	
	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public int getRanking() {
		return ranking;
	}
	
	public int getCompileTime() {
		return compileTime;
	}

	public void setCompileTime(int compileTime) {
		this.compileTime = compileTime;
	}

	public int getCompileFailingTime() {
		return compileFailingTime;
	}

	public void setCompileFailingTime(int compileFailingTime) {
		this.compileFailingTime = compileFailingTime;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public List<String> getConditions() {
		return conditions;
	}
	
	public List<String> getPredSequence() {
		return predSequence;
	}
	
	public String getOracle() {
		return oracle;
	}
	
	public void setOracle(String oracle) {
		this.oracle = oracle;
	}
	
	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
	}
	
	public void setPredSequence(List<String> predSequence) {
		this.predSequence = predSequence;
	}
	
}
