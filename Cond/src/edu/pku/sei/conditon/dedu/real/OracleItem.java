package edu.pku.sei.conditon.dedu.real;

import java.util.ArrayList;
import java.util.List;

public class OracleItem {
	private String bug;
	private int ithSus;
	private List<String> mayList = new ArrayList<>();
	private List<String> mustList = new ArrayList<>();
	
	public OracleItem(String bug, int ithSus) {
		this.bug = bug;
		this.ithSus = ithSus;
	}
	
	public String getBug() {
		return bug;
	}
	public int getIthSus() {
		return ithSus;
	}
	public List<String> getMayList() {
		return mayList;
	}
	public List<String> getMustList() {
		return mustList;
	}

	@Override
	public String toString() {
		return "OracleItem [bug=" + bug + ", ithSus=" + ithSus + ", mayList=" + mayList + ", mustList=" + mustList
				+ "]";
	}
	
}
