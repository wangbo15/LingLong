package edu.pku.sei.conditon.dedu.grammar.plain;

import java.util.ArrayList;
import java.util.List;

import edu.pku.sei.conditon.dedu.grammar.BoolNode;
import edu.pku.sei.conditon.dedu.grammar.GrammarNode;

public class PlainBoolNode extends BoolNode {
	
	//TODO:
	private Object predicate;
	
	private PlainVarNode firstVar;
	private List<PlainVarNode> vars = new ArrayList<>(5); 
	
	private PlainBoolNode(GrammarNode parent, PlainVarNode firstVar, List<PlainVarNode> vars) {
		super(parent);
		this.firstVar = firstVar;
		this.vars.addAll(vars);
	}
	
	public static PlainBoolNode getPlainPredNodeWithoutParent(PlainVarNode firstVar, List<PlainVarNode> vars) {
		PlainBoolNode pred = new PlainBoolNode(null, firstVar, vars);
		
		return pred;
	}

	public PlainVarNode getFirstVar() {
		return this.firstVar;
	}

	public List<PlainVarNode> getRemainVars() {
		return vars;
	}
	
}
