package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.util.CollectionUtil;

public class DJSearchStrategy implements SearchStrategy {
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	private static DJSearchStrategy instance;
	
	public static DJSearchStrategy getInstance() {
		if(instance == null) {
			instance = new DJSearchStrategy();
		}
		return instance;
	} 
	
	private DJSearchStrategy() {}
	
	@Override
	public int getResultLimits() {
		return CONFIG.getDijkstraSearchLimits();
	}

	@Override
	public void getElementWithinLimit(TreeSet<ProgramPoint> points, int resultSize) {
		// pass
	}

	@Override
	public String toString() {
		return "DJSearchStrategy with result limits: " + this.getResultLimits();
	}
}
