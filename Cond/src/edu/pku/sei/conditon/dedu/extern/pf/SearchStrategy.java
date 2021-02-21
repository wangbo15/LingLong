package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.pf.ProgramPoint;

public interface SearchStrategy {
		
	public int getResultLimits();
	
	public void getElementWithinLimit(TreeSet<ProgramPoint> points, int resultSize);
	
}
