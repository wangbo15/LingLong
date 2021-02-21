package edu.pku.sei.conditon.dedu.pred;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.util.CollectionUtil;

public class ConcretTree implements Comparable<ConcretTree> {
	
	private final TreePredItem root;
	private final Map<TreePredItem, GenExprItem> leafToExprMap;
	private final BigDecimal score;
	
	public ConcretTree(TreePredItem root, Map<TreePredItem, GenExprItem> leafToExprMap) {
		assert root.isRoot();
		this.root = root;
		this.leafToExprMap = leafToExprMap;
		this.score = computeScore();
	}
	
	public TreePredItem getRoot() {
		return root;
	}

	public BigDecimal getScore() {
		return score;
	}

	public Map<TreePredItem, GenExprItem> getLeafToExprMap() {
		return leafToExprMap;
	}

	private BigDecimal computeScore() {
		BigDecimal currScore = root.getFinalScore();
		for(Entry<TreePredItem, GenExprItem> entry: leafToExprMap.entrySet()) {
			GenExprItem leaf = entry.getValue();
			currScore = currScore.multiply(leaf.getScore());
		}
		return currScore;
	}
	
	@Override
	public int compareTo(ConcretTree o) {
		return this.score.compareTo(o.score);
	}
	
	@Override
	public String toString() {
		return root + "\n" + leafToExprMap + "\n" + score.setScale(16, BigDecimal.ROUND_HALF_UP) + "\n";
	}

	public static List<ConcretTree> getConcreteTrees(TreePredItem root) {
		List<TreePredItem> leafs = root.getLeafsForCompleteTree();
		if(leafs.isEmpty()) {
			return Collections.emptyList();
		}
		int leafsSize = leafs.size();
		
		int eachNodeExprNum;
		if(root.isLeaf()) {	// direct value
			eachNodeExprNum = ConditionConfig.getInstance().getExprLimit();
		}else {
			double exponent = (double) 1 / ((double) leafs.size());
			eachNodeExprNum = (int) Math.ceil(StrictMath.pow(ConditionConfig.getInstance().getExprLimit(), exponent));
		}
		
		List<List<GenExprItem>> allExprs = new ArrayList<>();
		for(int i = 0; i < leafsSize; i++) {
			TreePredItem leaf = leafs.get(i);
			int selections = leaf.getExpressions().size();
			int num = selections > eachNodeExprNum ? eachNodeExprNum : selections;
			if(num <= 0) {// illegal tree
				return Collections.emptyList();
			}
			allExprs.add(leaf.getExpressions().subList(0, selections));
		}
		List<Object[]> permutation = CollectionUtil.<GenExprItem>getPermutation(allExprs);
		List<ConcretTree> result = new ArrayList<>();

		for(Object[] singleResult: permutation) {
			Map<TreePredItem, GenExprItem> currentMap = new HashMap<>();
			for(int i = 0; i < leafsSize; i++) {
				TreePredItem leaf = leafs.get(i);
				GenExprItem genExprItem = (GenExprItem) singleResult[i];
				currentMap.put(leaf, genExprItem);
			}
			
			ConcretTree concretTree = new ConcretTree(root, currentMap);
			result.add(concretTree);
		}
		
		TreePredItem.dumpRoot(root);
		System.out.println("TREE COMPETITION NUM: " + result.size());
		
		Collections.sort(result, Collections.reverseOrder());
		return result;
	}
	
	
	public static List<String> concretTreeToGenExprItem(List<ConcretTree> trees){
		List<String> result = new ArrayList<>();
		for(ConcretTree concretTree : trees) {
			TreePredItem root = concretTree.getRoot();
			String literal = root.fullyComplementedLiteral(concretTree.leafToExprMap);
			// TODO: omit illeagal literals
			// assert literal.contains("#");
			String line = literal + "\t" + concretTree.getScore().setScale(32, BigDecimal.ROUND_HALF_UP).toPlainString();
			result.add(line);
			//System.out.println(line);
		}
		return result;
	}
	
}
