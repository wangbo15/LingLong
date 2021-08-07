package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.sei.plde.hanabi.fl.asm.metric.Metric;
/**
 * Representing a statement, collected by line number.
 * @author nightwish
 *
 */
public class FLStmt {
	
	private SourceLocation sourceLocation;
	
	private int executedFailedTestNum;
	private int executedPassTestNum;
	private int noExecutedFailedTestNum;
	private int noExecutedPassTestNum;
	
	private List<String> triggerTests = new ArrayList<>();
	
	private List<String> coveredSuccTest = new ArrayList<>();
	
	private Metric metric;
	
	private static final double INIT_VAL = Double.MIN_VALUE;
	
	private double suspiciousness = INIT_VAL;
	
	private FLStmt(Metric metric, SourceLocation sourceLocation) {
		this.metric = metric;
		this.sourceLocation = sourceLocation;
	}

	private static final Map<SourceLocation, FLStmt> cache = new HashMap<>();
	
	public static FLStmt getFLStmtFromCache(Metric metric, SourceLocation sourceLocation) {
		if(cache.containsKey(sourceLocation)) {
			return cache.get(sourceLocation);
		}else {
			FLStmt stmt = new FLStmt(metric, sourceLocation);
			cache.put(sourceLocation, stmt);
			return stmt;
		}
		
	}
	
	public void computeSuspiciousness() {
		if(suspiciousness == INIT_VAL) {
			suspiciousness = this.metric.value(executedFailedTestNum, executedPassTestNum, noExecutedFailedTestNum, noExecutedPassTestNum);
		}
	}
	
	public double getSuspiciousness() {
		assert suspiciousness != INIT_VAL: this.toString();
		return this.suspiciousness;
	}

	public int getExecutedFailedTestNum() {
		return executedFailedTestNum;
	}

	public void setExecutedFailedTestNum(int executedFailedTestNum) {
		this.executedFailedTestNum = executedFailedTestNum;
	}

	public int getExecutedPassTestNum() {
		return executedPassTestNum;
	}

	public void setExecutedPassTestNum(int executedPassTestNum) {
		this.executedPassTestNum = executedPassTestNum;
	}

	public int getNoExecutedFailedTestNum() {
		return noExecutedFailedTestNum;
	}

	public void setNoExecutedFailedTestNum(int noExecutedFailedTestNum) {
		this.noExecutedFailedTestNum = noExecutedFailedTestNum;
	}

	public int getNoExecutedPassTestNum() {
		return noExecutedPassTestNum;
	}

	public void setNoExecutedPassTestNum(int noExecutedPassTestNum) {
		this.noExecutedPassTestNum = noExecutedPassTestNum;
	}

	public SourceLocation getSourceLocation() {
		return sourceLocation;
	}

	public String getClassName() {
		return sourceLocation.getContainingClassName();
	}
	
	public int getLineNum() {
		return sourceLocation.getLineNumber();
	}
	
	public Metric getMetric() {
		return metric;
	}

	public List<String> getTriggerTests() {
		return triggerTests;
	}

	public List<String> getCoveredSuccTest() {
		return coveredSuccTest;
	}

	@Override
	public String toString() {
		return sourceLocation + "," + suspiciousness;
	}
	
	
}
