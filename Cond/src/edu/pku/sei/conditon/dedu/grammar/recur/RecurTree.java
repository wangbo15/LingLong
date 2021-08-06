package edu.pku.sei.conditon.dedu.grammar.recur;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import edu.pku.sei.conditon.dedu.grammar.Tree;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;

public class RecurTree extends Tree<RecurBoolNode> {

	private List<RecurBoolNode> bfsResultCache;
	private List<RecurBoolNode> buResultCache;
	
	RecurTree(RecurBoolNode root) {
		super(root);
		this.hight = getDeep(root);
		this.leafNum = leafNum(root);
		this.nodeNum = broadFristSearchTraverse().size();
		setDepthAndHight();
	}

	@Override
	public List<RecurBoolNode> broadFristSearchTraverse() {
		if(bfsResultCache != null) {
			return bfsResultCache;
		}
		
		List<RecurBoolNode> result = bfs(root);
		bfsResultCache = result;
		return result;
	}
	
	@Override
	public List<RecurBoolNode> bottomUpTraverse() {
		if(buResultCache != null) {
			return buResultCache;
		}
		List<RecurBoolNode> result = new ArrayList<>();
        Stack<RecurBoolNode> stack = new Stack<>();
        
        RecurBoolNode curr = root;
        while (curr != null){
            stack.push(curr);
            curr = curr.getChild0();
        }
        
        while (!stack.isEmpty()) {
        	RecurBoolNode top = stack.pop();
        	result.add(top);
        }
        List<RecurBoolNode> tmp = new ArrayList<>();
        for (RecurBoolNode node : result) {
        	if(node.getChild1() != null) {
        		tmp.addAll(bfs(node.getChild1()));
        	}
        }
		result.addAll(tmp);
		buResultCache = result;
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
	
	private int getHight(RecurBoolNode node) {
		if(node.getOpcode().equals(Opcode.NONE)) {
			assert node.getChild0() == null && node.getChild1() == null;
			return 1;
		} else {
			RecurBoolNode c0 = node.getChild0();
			int h0 = 0;
			if (c0 != null) {
				h0 = c0.getHight() + 1;
			}
			RecurBoolNode c1 = node.getChild1();
			int h1 = 0;
			if (c1 != null) {
				h1 = c1.getHight() + 1;
			}
			return h0 > h1 ? h0 : h1;
		}
	}
	
	private void setDepthAndHight() {
		List<RecurBoolNode> list = broadFristSearchTraverse();
		for(int i = 0; i < list.size(); i++) {
			RecurBoolNode n = list.get(i);
			int depth = getDepth(n);
			n.setDepthLevel(depth);
			
			int j = list.size() - 1 - i;
			RecurBoolNode m = list.get(j);
			int hight = getHight(m);
			m.setHight(hight);
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

	
	private static List<RecurBoolNode> bfs(RecurBoolNode root) {
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
		return result;
	}
}
