package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.util.CollectionUtil;

public class BeamSearchStrategy implements SearchStrategy {
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	private static BeamSearchStrategy instance;
	
	public static BeamSearchStrategy getInstance() {
		if(instance == null) {
			instance = new BeamSearchStrategy();
		}
		return instance;
	}
	
	private BeamSearchStrategy() {}
	
	@Override
	public int getResultLimits() {
		return CONFIG.getBeamSearchResultLimits();
	}

	@Override
	public void getElementWithinLimit(TreeSet<ProgramPoint> points, int resultSize) {
		int limit = CONFIG.getBeamSearchLimits() - resultSize;
		CollectionUtil.remainTreeSetFirstK(points, limit);
	}

	@Override
	public String toString() {
		return "BeamSearchStrategy with result limits: " + this.getResultLimits();
	}
}
