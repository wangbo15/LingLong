package edu.pku.sei.conditon.dedu.grammar.plain;

import java.util.ArrayList;
import java.util.List;

import edu.pku.sei.conditon.dedu.grammar.BoolNode;
import edu.pku.sei.conditon.dedu.grammar.Tree;

public class PlainTree extends Tree<BoolNode> {

	private List<BoolNode> bfsResultCache;
	
	public PlainTree(PlainBoolNode root) {
		super(root);
		
		assert root.getFirstVar() != null;
		
		this.hight = 2;
		this.leafNum = 1 + root.getRemainVars().size();
		this.nodeNum = 1 + leafNum;
	}

	@Override
	public List<BoolNode> broadFristSearchTraverse() {
		if(bfsResultCache != null) {
			return bfsResultCache;
		}
		List<BoolNode> result = new ArrayList<>();
		result.add(root);
		result.add(((PlainBoolNode) root).getFirstVar());
		result.addAll(((PlainBoolNode) root).getRemainVars());
		bfsResultCache = result;
		return result;
	}

	@Override
	public List<BoolNode> bottomUpTraverse() {
		return null;
	}

	
	@Override
	public void dump() {
		System.out.println("--------------------");
		System.out.println("NODENUM: " + this.nodeNum);		
		System.out.println(root.toString());
		System.out.println("--------------------");
	}

}
