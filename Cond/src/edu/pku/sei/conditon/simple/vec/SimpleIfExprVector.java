package edu.pku.sei.conditon.simple.vec;

import java.util.ArrayList;
import java.util.List;
/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Feb 27, 2017
 */
public class SimpleIfExprVector extends SimpleIfVector{
	
	private long ifId = -1;
	
	private boolean hasElse = false;
	private List<SimpleExpr> simpleExprList = new ArrayList<>();
	
	private List<String> oracleList;// = new ArrayList<>();
	
	public long getIfId() {
		return ifId;
	}
	public void setIfId(long ifId) {
		this.ifId = ifId;
	}
	public boolean isHasElse() {
		return hasElse;
	}
	public void setHasElse(boolean hasElse) {
		this.hasElse = hasElse;
	}
	
	public List<SimpleExpr> getSimpleExprList() {
		return simpleExprList;
	}
	public void setSimpleExprList(List<SimpleExpr> simpleExprList) {
		this.simpleExprList = simpleExprList;
	}
			
	public List<String> getOracleList() {
		return oracleList;
	}
	public void setOracleList(List<String> oracleList) {
		this.oracleList = oracleList;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(SimpleExpr expr : simpleExprList){
			String line = ifCond + " ==>> " + methodName + " " + expr.toString().trim() + " " + hasElse + " "+ this.bodyCtl + "\n";
			sb.append(line.trim());
		}
		return sb.toString();
	}
		
	
}
