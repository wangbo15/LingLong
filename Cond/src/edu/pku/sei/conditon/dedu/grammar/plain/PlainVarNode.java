package edu.pku.sei.conditon.dedu.grammar.plain;

import edu.pku.sei.conditon.dedu.grammar.BoolNode;
import edu.pku.sei.conditon.dedu.grammar.GrammarNode;
import edu.pku.sei.conditon.ds.VariableInfo;

public class PlainVarNode extends BoolNode {
	
	private VariableInfo info;
	
	private PlainVarNode(GrammarNode parent, VariableInfo info) {
		super(parent);
		this.info = info;
	}
	
	public static PlainVarNode getPlainVarNodeWithoutParent(VariableInfo info) {
		PlainVarNode node = new PlainVarNode(null, info);
		return node;
	}

	public VariableInfo getVarInfo() {
		return this.info;
	}
	
}
