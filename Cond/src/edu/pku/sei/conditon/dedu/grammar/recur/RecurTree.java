package edu.pku.sei.conditon.dedu.grammar.recur;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.pku.sei.conditon.dedu.grammar.GrammarNode;
import edu.pku.sei.conditon.dedu.grammar.Tree;

public class RecurTree extends Tree<RecurBoolNode> {

	private List<RecurBoolNode> bfsResultTmp;
	
	RecurTree(RecurBoolNode root) {
		super(root);
		this.hight = getDeep(root);
		this.leafNum = leafNum(root);
		this.nodeNum = broadFristSearchTraverse().size();
		setDepthLevel();
	}

	@Override
	public List<RecurBoolNode> broadFristSearchTraverse() {
		if(bfsResultTmp != null) {
			return bfsResultTmp;
		}
		
		List<RecurBoolNode> result = new ArrayList<>();
		Queue<RecurBoolNode> queue = new LinkedList<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
			RecurBoolNode head = queue.poll();
			if (head.getChild0() != null) {
				queue.offer(head.getChild0());
			}
			if (head.getChild1() != null) {
				queue.offer(head.getChild1());
			}
			result.add(head);
		}
		bfsResultTmp = result;
		return result;
	}

	@Override
	public void dump() {
		System.out.println("--------------------");
		System.out.println("NODENUM: " + this.nodeNum);		
		System.out.println("HIGHT: " + this.hight);
		System.out.println("LEAFNUM: " + this.leafNum);
		System.out.println(root.getExpr());
		System.out.println(root.toString());
		System.out.println("--------------------");
	}

	private int getDepth(RecurBoolNode node) {
		if(node == root) {
			return 0;
		}
		return getDepth(node.getParent()) + 1;
	}
	
	private void setDepthLevel() {
		for(RecurBoolNode node : broadFristSearchTraverse()) {
			int depth = getDepth(node);
			node.setDepthLevel(depth);
		}
		
	}
	
	private static int getDeep(RecurBoolNode node) {
		int h0, h1;
		if (node == null) {
			return 0;
		} else {
			h0 = getDeep(node.getChild0());
			h1 = getDeep(node.getChild1());
			return (h0 < h1) ? h1 + 1 : h0 + 1;
		}
	}
	
	private static int leafNum(RecurBoolNode node) {
		if (node == null) {
			return 0;
		}
		if (node.getChild0() == null && node.getChild1() == null) {
			return 1;
		}
		return leafNum(node.getChild0()) + leafNum(node.getChild1());
	}
	
}
