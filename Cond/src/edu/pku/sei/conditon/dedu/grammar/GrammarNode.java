package edu.pku.sei.conditon.dedu.grammar;

import edu.pku.sei.conditon.dedu.feature.Feature;

public abstract class GrammarNode extends Feature {
	
	protected GrammarNode parent = null;
	
	public GrammarNode(GrammarNode parent) {
		this.parent = parent;
	}
		
	public GrammarNode getParent() {
		return this.parent;
	}
	
	public void setParent(GrammarNode parent) {
		this.parent = parent;
	}
	
}
