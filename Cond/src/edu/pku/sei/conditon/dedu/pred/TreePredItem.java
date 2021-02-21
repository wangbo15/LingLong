package edu.pku.sei.conditon.dedu.pred;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Location;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.util.OperatorUtil;
import edu.pku.sei.conditon.util.Pair;

public class TreePredItem implements Comparable<TreePredItem> {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	public static final int MAX_TREE_HIGHT = CONFIG.getTreeDepth();
	public static final int MAX_TREE_SIZE = (1 << MAX_TREE_HIGHT) - 1;
	
	private RecurNodePredItem expandItem;
	
	private TreePredItem parent;
	private TreePredItem child0;
	private TreePredItem child1;
	
	// only use in synthesis
	private BigDecimal finalScore;
	
	private ExprPredItem exprItem;
	private List<VarPredItem> varList;
	
	// only used in previous complete tree
	@Deprecated
	private List<GenExprItem> expressions;
	
	private static long instanceNum = 0L;
	
	public TreePredItem(TreePredItem parent) {
		this.parent = parent;
		
		instanceNum++;
	}
	
	public static void setInstanceNum(long insNum) {
		TreePredItem.instanceNum = insNum;
	}
	
	public static long getInstanceNum(){
		return instanceNum;
	}
	
	public TreePredItem getSibling() {
		if(parent == null || parent.expandItem == null) {
			return null;
		}
		if(parent.expandItem.getOpcode() == Opcode.NONE || parent.expandItem.getOpcode() == Opcode.NOT) {
			return null;
		}
		if(this == parent.child0) {
			return parent.child1;
		} else {
			return parent.child0;
		}
	}
	
	public TreePredItem getParent() {
		return this.parent;
	}
	
	public TreePredItem getChild0() {
		return child0;
	}
	public void setChild0(TreePredItem child0) {
		this.child0 = child0;
	}
	public TreePredItem getChild1() {
		return child1;
	}
	public void setChild1(TreePredItem child1) {
		this.child1 = child1;
	}
	
	public RecurNodePredItem getExpandItem() {
		return expandItem;
	}
	
	public void setExpandItem(RecurNodePredItem expandItem) {
		this.expandItem = expandItem;
	}
	
	public ExprPredItem getExprItem() {
		return exprItem;
	}

	public void setExprItem(ExprPredItem exprItem) {
		this.exprItem = exprItem;
	}

	public List<VarPredItem> getVarList() {
		return varList;
	}

	public void setVarList(List<VarPredItem> varList) {
		assert varList != null;
		this.varList = varList;
	}

	@Deprecated
	public List<GenExprItem> getExpressions() {
		assert this.isLeaf();
		return expressions;
	}
	
	@Deprecated
	public void setExpressions(List<GenExprItem> expressions) {
		assert this.isLeaf();
		this.expressions = expressions;
	}
	
	public boolean isUnknowRecodeNodeType() {
		return this.expandItem == null;
	}
	
	public boolean isLeaf() {
		return this.expandItem.getOpcode() == Opcode.NONE;
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
	
	@Deprecated
	public BigDecimal getFinalScore() {
		if(!isRoot()) {
			return BigDecimal.ZERO;
		}
		if(finalScore != null) {
			return finalScore;
		}
		
		finalScore = BigDecimal.ONE;
		List<TreePredItem> allNodes = broadFristSearchTraverse();
		for(TreePredItem item: allNodes) {
			BigDecimal itemScore = BigDecimal.valueOf(item.getExpandItem().getScore());
			assert itemScore.compareTo(BigDecimal.ONE) == -1;
			assert itemScore.compareTo(BigDecimal.ZERO) == 1;
			
			finalScore = finalScore.multiply(itemScore);
			
			assert finalScore.compareTo(BigDecimal.ONE) == -1;
			assert finalScore.compareTo(BigDecimal.ZERO) == 1;
		}

		return finalScore;
	}
	
	public boolean canExpandChild0() {
		assert expandItem != null;
		return !expandItem.getOpcode().equals(Opcode.NONE);
	}
	
	public boolean canExpandChild1() {
		assert this.expandItem != null;
		return this.expandItem.getOpcode().equals(Opcode.AND) || this.expandItem.getOpcode().equals(Opcode.OR);
	}
	
	public boolean isChild0() {
		if(this.getParent() == null) {
			return false;
		}
		return this.getParent().getChild0() == this;
	}
	
	public boolean isChild1() {
		if(this.getParent() == null) {
			return false;
		}
		return this.getParent().getChild1() == this;
	}
	
	public TreePredItem getRoot() {
		TreePredItem node = this;
		while(node.getParent() != null) {
			node = node.getParent();
		}
		return node;
	}
	
	private String feature;
	
	public String getFeature() {
		if(feature != null) {
			return feature;
		}
		
		if(this.getParent() == null) {
			feature = "\ttrue\tNONE\tNIL\tDIRECT\t0\t";
			return feature;
		}
		StringBuilder sb = new StringBuilder("\tfalse\t");	//is root
		sb.append(this.getParent().getExpandItem().getOpcode().toString());
		sb.append("\t");
		if(this.isChild1()) {
			String sibling;
			if(this.parent.child0.expandItem != null) {
				sibling = this.parent.child0.expandItem.getOpcode().toString();
			}else {
				sibling = Opcode.NONE.toString();
			}
			sb.append(sibling);
			sb.append("\t");
			sb.append(Location.LEFT);
			sb.append("\t");
		}else {
			if(this.parent.child1 == null) {
				sb.append("NIL");
				sb.append("\t");
				sb.append(Location.DIRECT);
				sb.append("\t");
			}else {
				String sibling;
				if(this.parent.child1.expandItem != null) {
					sibling = this.parent.child1.expandItem.getOpcode().toString();
				}else {
					sibling = Opcode.NONE.toString();
				}
				sb.append(sibling);
				sb.append("\t");
				sb.append(Location.LEFT);	// location in parent
				sb.append("\t");
			}
		}
		sb.append(this.getCurrentHightToRoot());
		sb.append("\t");
		feature = sb.toString();
		return feature;
	}
	
	public int getChildNodeNum() {
		if(this.getParent() == null) {
			return 2;
		}
		
		if(this.expandItem.getOpcode().equals(Opcode.AND)
				|| this.expandItem.getOpcode().equals(Opcode.OR)) {
			return 2;
		}if(this.expandItem.getOpcode().equals(Opcode.NOT)) {
			return 1;
		}else {
			return 0;
		}
	}
	
	public int getCurrentHight() {
		if(this.child0 == null && this.child1 == null) {
			return 1;
		}
		int h0 = 0, h1 = 0;
		if(this.child0 != null) {
			h0 = child0.getCurrentHight();
		}
		if(this.child1 != null) {
			h1 = child1.getCurrentHight();
		}
		return (h0 < h1) ? h1 + 1 : h0 + 1;
	}
	
	public int getCurrentHightToRoot() {
		if(this.getParent() == null) {
			return 0;
		}
		return this.getParent().getCurrentHightToRoot() + 1;
	}
	
	public int getCurrentLargestDepth() {
		int left = (child0 == null) ? 0 : child0.getCurrentLargestDepth();
		int right = (child1 == null) ? 0 : child1.getCurrentLargestDepth();
        return (left > right) ? (left + 1) : (right + 1);
	}
	
	private static boolean needParenthesesForLeaf(TreePredItem node, GenExprItem genExprItem) {
		assert node.isLeaf();
		if(node.isRoot()){
			return false;
		}
		Expression expandedLeafExpr = genExprItem.getExpr().getAstNode();
		assert expandedLeafExpr != null;
		TreePredItem parent = node.getParent();
		String parentOp = parent.getExpandItem().getOpcode().toString();
		if(OperatorUtil.getPrecedence(expandedLeafExpr) < OperatorUtil.getPrecedence(parentOp)) {
			return true;
		}else {
			return false;
		}
	}
	
	private static boolean needParenthesesForNonLeaf(TreePredItem node) {
		assert !node.isLeaf();
		if(node.isRoot()){
			return false;
		} 
	
		TreePredItem parent = node.getParent();
		String nodeOp = node.getExpandItem().getOpcode().toString();
		String parentOp = parent.getExpandItem().getOpcode().toString();
		if(OperatorUtil.getPrecedence(nodeOp) < OperatorUtil.getPrecedence(parentOp)) {
			return true;
		}else {
			return false;
		}
	}
	
	public String fullyComplementedLiteral() {
		if(expandItem.getOpcode() == Opcode.NONE) {
			
			assert exprItem != null;
			assert varList != null; 
			assert !varList.isEmpty();
			
			GenExprItem genExprItem = new GenExprItem(exprItem, varList);
			boolean needParentheses = needParenthesesForLeaf(this, genExprItem);
			
			String ori = genExprItem.getGeneratedExpr();
			
			assert ori != null && ori.length() > 0;
			
			if(needParentheses) {
				return "(" + ori + ")";
			}else {
				return ori;
			}
		} else if(expandItem.getOpcode() == Opcode.NOT) {
			boolean needParentheses = needParenthesesForNonLeaf(this);
			String result = Opcode.NOT +  child0.fullyComplementedLiteral();
			if(needParentheses) {
				return "(" + result + ")";
			}else {
				return result;
			}
			
		} else {
			boolean needParentheses = needParenthesesForNonLeaf(this);
			String result = child0.fullyComplementedLiteral() + " " + expandItem.getOpcode() + " " + child1.fullyComplementedLiteral();
			if(needParentheses) {
				return "(" + result + ")";
			}else {
				return result;
			}
		}
	}

	
	public String fullyComplementedLiteral(Map<TreePredItem, GenExprItem> leafToExprMap) {
		if(expandItem.getOpcode() == Opcode.NONE) {
			GenExprItem genExprItem = leafToExprMap.get(this);
			assert genExprItem != null;
			
			boolean needParentheses = needParenthesesForLeaf(this, genExprItem);
			
			String ori = genExprItem.getGeneratedExpr();
			
			assert ori != null && ori.length() > 0;
			
			if(needParentheses) {
				return "(" + ori + ")";
			}else {
				return ori;
			}
		} else if(expandItem.getOpcode() == Opcode.NOT) {
			boolean needParentheses = needParenthesesForNonLeaf(this);
			String result = Opcode.NOT +  child0.fullyComplementedLiteral(leafToExprMap);
			if(needParentheses) {
				return "(" + result + ")";
			}else {
				return result;
			}
			
		} else {
			boolean needParentheses = needParenthesesForNonLeaf(this);
			String result = child0.fullyComplementedLiteral(leafToExprMap) + " " + expandItem.getOpcode() + " " + child1.fullyComplementedLiteral(leafToExprMap);
			if(needParentheses) {
				return "(" + result + ")";
			}else {
				return result;
			}
		}
	}
		
	@Override
	public String toString() {
		final String unset = "?";
		final String hasBeenSet = "#";
		if(expandItem == null) {
			return unset;
		}
		
		if(expandItem.getOpcode() == Opcode.NONE) {
			
			StringBuffer sb = new StringBuffer(hasBeenSet);
			if(exprItem != null) {
				sb.append(":" + exprItem.getPred());
				if(varList != null && !varList.isEmpty()) {
					sb.append('[');
					for(VarPredItem v: varList) {
						sb.append(v.getLiteral());
						sb.append(',');
					}
					sb.deleteCharAt(sb.length() - 1);
					sb.append(']');
				}
			} else {
				 if(CONFIG.isBottomUp() && varList != null && varList.size() == 1) {
					 sb.append("[^");
					 sb.append(varList.get(0).getLiteral());
					 sb.append(']');
				 }
			}
			return sb.toString();
		} else if(expandItem.getOpcode() == Opcode.NOT) {
			return "(" + Opcode.NOT + " " 
					+ (child0 == null ? unset : child0.toString()) + ")";
		} else {
			return "(" + (child0 == null ? unset : child0.toString())
					+ " " + expandItem.getOpcode() + " " 
					+ (child1 == null ? unset : child1.toString()) + ")";
		}
	}
	
	@Override
	public int compareTo(TreePredItem o) {
		return this.getFinalScore().compareTo(o.getFinalScore());
	}
	
	public static void expandTreeCompleteTree(TreePredItem root) {
		if(root.getCurrentHightToRoot() < MAX_TREE_HIGHT - 1) {
			root.setChild0(new TreePredItem(root));
			root.setChild1(new TreePredItem(root));
			expandTreeCompleteTree(root.getChild0());
			expandTreeCompleteTree(root.getChild1());
		}
	}
	
	/* only used in getLeafs */
	private List<TreePredItem> bfsListForCompleteTree;
	/* only used in getLeafs */
	private List<TreePredItem> leafsTmp;
	/*
	 * can only be used after set all exprs
	 */
	public List<TreePredItem> getLeafsForCompleteTree(){
		assert this.isRoot();
		assert isRecurNodeFullyExpanded(this);
		if(leafsTmp != null) {
			return leafsTmp;
		}
		if(bfsListForCompleteTree == null) {
			bfsListForCompleteTree = broadFristSearchTraverse();
		}
		List<TreePredItem> tmp = new ArrayList<>();
		for(TreePredItem item : bfsListForCompleteTree) {
			if(item.isLeaf()) {
				tmp.add(item);
			}
		}
		leafsTmp = tmp;
		return leafsTmp;
	}
	
	public List<TreePredItem> broadFristSearchTraverse() {
		List<TreePredItem> result = new ArrayList<>();
		Queue<TreePredItem> queue = new LinkedList<>();
		queue.offer(this);
		while (!queue.isEmpty()) {
			TreePredItem head = queue.poll();
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
	
	public List<TreePredItem> depthFirstSearchTraverse(){
		List<TreePredItem> result = new ArrayList<>();
		
		Stack<TreePredItem> stack = new Stack<>();
		TreePredItem root = this;
		while(root != null || !stack.isEmpty()) {
			while(root != null) {
				result.add(root);
				stack.push(root);
				root = root.getChild0();
			}
			if(!stack.isEmpty()) {
				root = stack.pop();
				root = root.getChild1();
			}
		}
		
		return result;
	}
	
	public boolean isRecurNodeComplete() {
		List<TreePredItem> list = broadFristSearchTraverse();
		for(TreePredItem node: list) {
			RecurNodePredItem rn = node.getExpandItem();
			if(rn == null) {
				return false;
			} else {
				if(rn.getOpcode() == Opcode.AND || rn.getOpcode() == Opcode.OR) {
					if(node.child0 == null)
						return false;
					if (node.child1 == null)
						return false;
				} else if (rn.getOpcode() == Opcode.NOT) {
					if(node.child0 == null)
						return false;
				}
			}
		}
		return true;
	}

	public void remainHighScore() {
		assert this.isRoot();
		List<TreePredItem> leafs = this.getLeafsForCompleteTree();
		int remain = CONFIG.getExprLimit() / leafs.size();
		
		for(TreePredItem leaf : leafs) {
			if(leaf.getExpressions().size() > remain) {
				List<GenExprItem> subList = leaf.getExpressions().subList(0, remain);
				leaf.setExpressions(subList);
			}
		}
	}
	
	/**
	 * @return true if the tree has (! (! #) form
	 */
	public boolean isNotNotRedundant() {
		if(isUnknowRecodeNodeType()) {
			return false;
		} else if(isLeaf()) {
			return false;
		}
		
		if(expandItem.getOpcode() == Opcode.NOT) {
			if(this.getParent() != null && this.getParent().getExpandItem().getOpcode() == Opcode.NOT) {
				return true;
			}
		}
		
		if(child1 != null) {
			return child0.isNotNotRedundant() || child1.isNotNotRedundant();
		}
		return child0.isNotNotRedundant();
	}
	
	public boolean isCommutativeRedundant() {
		// filter (A || A) or (A && A)
		TreePredItem sibling = getSibling();
		if(sibling != null && !sibling.isUnknowRecodeNodeType() && sibling.isLeaf()) {
			if(exprItem != null && sibling.exprItem != null && varList != null && sibling.varList != null) {
				if(exprItem.getPred().equals(sibling.exprItem.getPred())) {
					int sibVarNum = sibling.getVarList().size();
					int curVarNum = getVarList().size();
					if(sibVarNum == curVarNum && sibVarNum == sibling.getExprItem().getPositionNum()) {
						boolean different = false;
						for(int j = 0; j < sibVarNum; j++) {
							String sibLit = sibling.varList.get(j).getLiteral();
							String curLit = varList.get(j).getLiteral();
							if(!sibLit.equals(curLit)) {
								different = true;
								break;
							}
						}
						if(!different) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static TreePredItem getCorrespondingNode(TreePredItem root, TreePredItem copyRoot, TreePredItem node){
		List<TreePredItem> rootList = root.broadFristSearchTraverse();
		List<TreePredItem> copyRootList = copyRoot.broadFristSearchTraverse();
		
		assert rootList.size() == copyRootList.size();
		for(int i = 0; i < rootList.size(); i++) {
			if(rootList.get(i).equals(node)) {
				return copyRootList.get(i);
			}
		}
		return null;
	}
	
	public List<TreePredItem> recurNodeExpansionPositions(){
		List<TreePredItem> list = this.depthFirstSearchTraverse();
		List<TreePredItem> result = new ArrayList<>();
		for(TreePredItem item: list) {
			RecurNodePredItem rcNode = item.getExpandItem();
			if(rcNode == null) {
				if(item.isRoot() || item.isChild0()) {
					result.add(item);
				} else {
					TreePredItem sibling = item.getSibling();
					if(sibling != null && sibling.getExpandItem() != null) {
						result.add(item);
					} 
				}
			}
		}
		return result;
	}
	
	public List<TreePredItem> exprExpansionPositions(){
		List<TreePredItem> list = broadFristSearchTraverse();
		List<TreePredItem> result = new ArrayList<>();
		for(TreePredItem item: list) {
			RecurNodePredItem rcNode = item.getExpandItem();
			if(rcNode != null && rcNode.getOpcode() == Opcode.NONE){
				if(item.exprItem == null) {
					result.add(item);
				}
			}
		}
		return result;
	}
	
	public List<TreePredItem> varExpansionPositions(){
		List<TreePredItem> list = broadFristSearchTraverse();
		List<TreePredItem> result = new ArrayList<>();
		for(TreePredItem item: list) {
			RecurNodePredItem rcNode = item.getExpandItem();
			if(rcNode != null && rcNode.getOpcode() == Opcode.NONE){
				if(item.exprItem != null) {
					if(item.varList == null || item.varList.isEmpty()) {
						result.add(item);
					} else 	if(item.exprItem.getPositionNum() > item.varList.size()){
						result.add(item);
					}
				}
			}
		}
		return result;
	}
	
	
	/**
	 * @param ori: the root to be copied
	 * @param copy: the newly cloned root
	 */
	private static void cloneChild(final TreePredItem ori, final TreePredItem copy) {
		copy.setExpandItem(ori.getExpandItem());
		
		if(ori.exprItem != null) {
			copy.exprItem = ori.exprItem;
		}
		if(ori.varList != null) {
			copy.varList = new ArrayList<>(ori.varList);//TODO: change to immutable
		}
		
		if(ori.child0 != null) {
			copy.child0 = new TreePredItem(copy);
			cloneChild(ori.child0, copy.child0);
		}
		
		if(ori.child1 != null) {
			copy.child1 = new TreePredItem(copy);
			cloneChild(ori.child1, copy.child1);
		}
	}
	
	public static TreePredItem sharedCopy(final TreePredItem root, final TreePredItem expansion) {
		assert root.getParent() == null;//must be a tree root
		Stack<Integer> path = new Stack<>();
		// Integer: 0: isChild0, 1: isChild1, -1: the leaf node
		path.push(-1);
		TreePredItem curr = expansion;
		while(curr != null) {
			if(!curr.isRoot()) {
				int pos = (curr.isChild0() ? 0 : 1);
				path.push(pos);
			}
			curr = curr.getParent();
		}
		TreePredItem newItem = null, newParent = null;
		TreePredItem currItem = root;
		while(!path.isEmpty()) {
			int pos = path.pop();
			newItem = new TreePredItem(newParent);
			if(currItem.isChild0()) {
				newParent.setChild0(newItem);
			} else if(currItem.isChild1()) {
				newParent.setChild1(newItem);
			}
			newItem.setExpandItem(currItem.getExpandItem());
			if(currItem.exprItem != null) {
				newItem.exprItem = currItem.exprItem;
			}
			if(currItem.varList != null) {
				newItem.varList = new ArrayList<>(currItem.varList);
			}
			if(pos == 0) { // is child 0, then reuse child 1
				newItem.setChild1(currItem.getChild1());
				currItem = currItem.getChild0();
			} else if(pos == 1){ // otherwise reuse child 0
				newItem.setChild0(currItem.getChild0());
				currItem = currItem.getChild1();
			}
			newParent = newItem;
		}
		return newItem.getRoot();
	}
	
	public static TreePredItem deepCloneFromRoot(final TreePredItem root) {
		assert root.getParent() == null;//must be a tree root
		TreePredItem newRoot = new TreePredItem(null);
		cloneChild(root, newRoot);
		return newRoot;
	}
	
	public static boolean isRecurNodeFullyExpanded(TreePredItem root) {
		List<TreePredItem> bfsList = root.broadFristSearchTraverse();
		for(TreePredItem item: bfsList) {
			if(item == null) {
				return false;
			}
			if(item.getExpandItem() == null) {
				return false;
			}
			if(item.canExpandChild0() && item.getChild0() == null) {
				return false;
			}
			if(item.canExpandChild1() && item.getChild1() == null) {
				return false;
			}
		}
		return true;
	}
	
	public static void dumpRoot(TreePredItem root) {
		assert root.getParent() == null;
		System.out.println("--------------------");
		System.out.println("HIGHT: " + root.getCurrentHight());
		System.out.println("SCORE: " + root.getFinalScore().setScale(20, BigDecimal.ROUND_HALF_UP));
		System.out.println(root.toString());
		System.out.println("--------------------");
	}
}
