package edu.pku.sei.conditon.dedu.feature;

import java.util.List;

import edu.pku.sei.conditon.ds.VariableInfo;
public class CondVector extends AbstractVector{
	
	private Predicate predicate;


	public CondVector(int condId, String fileName, int line, int col, List<VariableInfo> locals, ContextFeature contextFeature) {
		super(condId, fileName, line, col, contextFeature, locals);
	}
	
	public Predicate getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	
}