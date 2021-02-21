package edu.pku.sei.conditon.dedu.pred;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OriPredItem {
	private String literal;
	private int slopNum = -1;
	
	private Map<Integer, Set<String>> posToTypesMap = new HashMap<>();
	
	private Map<Integer, Map<String, Integer>> posToOccurredVarTimesMap = new HashMap<>();
	
	public OriPredItem(String literal, int slopNum) {
		this.literal = literal;
		this.slopNum = slopNum;
	}
	
	public String getLiteral() {
		return literal;
	}

	public int getSlopNum() {
		return slopNum;
	}

	public Map<Integer, Set<String>> getPosToTypesMap() {
		return posToTypesMap;
	}

	public Map<Integer, Map<String, Integer>> getPosToOccurredVarTimesMap(){
		return posToOccurredVarTimesMap;
	}
	
	@Override
	public String toString() {
		return literal + " " + slopNum + " " + posToTypesMap;
	}
	
}
