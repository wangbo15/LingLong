package edu.pku.sei.conditon.dedu.extern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.dedu.DeduFeatureGenerator;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.pred.ConcretTree;
import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.GenExprItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.SearchMethod;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;

public class ConcretePredictor extends Predictor{
	
	public ConcretePredictor(String projAndBug, String srcRoot, String testRoot) {
		super(projAndBug, srcRoot, testRoot);
	}
	
	public void getExprsByTopDown(String filePath, int line, int ithSuspicous) {
		invoker.prepare();
		
		//String fileName = getFileName(filePath);
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();

		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);

		String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		List<ExprPredItem> exprs = invoker.predictTDExprs(ctxFea);
		
		List<ExprPredItem> allExprs = new ArrayList<>(CONFIG.getExprLimit());
		
		for(ExprPredItem exprItem: exprs){
			
			Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list
			for(int n = 0; n < exprItem.getPositionNum(); n++){
				List<String> varFeatersAtN = genTopDownVarFeatureAtPosN(n, ctxFea, varToVarFeaMap, exprItem, allVarInfoMap);
				
				assert varFeatersAtN.isEmpty() == false;
				
				List<VarPredItem> varsAtN = invoker.predictTDVars(n, varFeatersAtN, allVarInfoMap);
				
				if(!varsAtN.isEmpty()) {
					predVars.put(n, varsAtN);
				}
			}
			exprItem.setPredVars(predVars);
			if(predVars.size() == exprItem.getPositionNum()){
				allExprs.add(exprItem);
			}
		}
		
		invoker.generateAndDump(proj, projAndBug, ithSuspicous, allExprs, allOriPredicates);
		invoker.finish();
	}
	
	public void getExprsByBottomUp(String filePath, int line, int ithSuspicous){
		invoker.prepare();
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		//varname -> var feature prefix
		Map<String, String> varToFeaPrefixMap = DeduFeatureGenerator.getVarToCtxAndVarFeaPrefixMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		List<VarPredItem> varsForZero = invoker.predictBUV0(allVarInfoMap, varToFeaPrefixMap);

		List<ExprPredItem> allExprs = new ArrayList<>(CONFIG.getExprLimit());
		
		for(VarPredItem varItemAtZero: varsForZero){
			String v0Iter = varItemAtZero.getLiteral();
			String curVarFea = varToFeaPrefixMap.get(v0Iter);
			if(curVarFea == null || curVarFea.length() == 0){
				continue;
			}
			
			List<ExprPredItem> exprs = invoker.predictBUExprs(v0Iter, curVarFea);
			
			for(ExprPredItem exprItem: exprs){
				
				if(!ExprGenerator.isLegalExprForV0(varItemAtZero, exprItem, allOriPredicates)){
					continue;
				}
				
				Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list
				List<VarPredItem> v0List = new ArrayList<>();
				v0List.add(varItemAtZero);
				predVars.put(0, v0List);
				
				for(int n = 1; n < exprItem.getPositionNum(); n++){
					
					List<String> varFeatersAtN = getBottomUpVarFeatureAtPosN(varToFeaPrefixMap, allVarInfoMap, varItemAtZero, exprItem, n);					
					List<VarPredItem> varsAtN = invoker.predictBUVars(varFeatersAtN, allVarInfoMap, n);
					predVars.put(n, varsAtN);
				}
				exprItem.setPredVars(predVars);
				
				if(predVars.size() == exprItem.getPositionNum()){
					allExprs.add(exprItem);
				}
			}
		}
		invoker.generateAndDump(proj, projAndBug, ithSuspicous, allExprs, allOriPredicates);
		invoker.finish();
	}
	
	
	public void getConditionByRecur(String filePath, int line, int ithSuspicous) {
		
		invoker.prepare();
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
				
		List<ConcretTree> results = Collections.emptyList();
		if(CONFIG.getSearchMethod() == SearchMethod.GREEDY_BEAM) {
			results = completeTreeByBeam(ctxFea, allVarInfoMap, varToVarFeaMap);
		} else if(CONFIG.getSearchMethod() == SearchMethod.DFS) {
			results = completeTreeByDFS(ctxFea, allVarInfoMap, varToVarFeaMap);
		}
		
		List<String> lines = ConcretTree.concretTreeToGenExprItem(results);
		String proj_Bug_ithSusp = projAndBug + "_" + ithSuspicous;
		
		//TODO: move to invoker
		AbsInvoker.dumpPlainResult(proj, proj_Bug_ithSusp, lines);
		invoker.finish();
	}
	

	private List<ConcretTree> completeTreeByDFS(String ctxFea, Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToVarFeaMap){

		ConcretTree largestTree = getLargestConcretTree(ctxFea, allVarInfoMap, varToVarFeaMap);
		
		BigDecimal dfsProb = largestTree.getScore();

		List<TreePredItem> trees = predTrees(ctxFea, dfsProb);

		for(TreePredItem tree: trees) {
			completeTreeByPredictAndProb(tree, ctxFea, varToVarFeaMap, allVarInfoMap, dfsProb);
			
		}
		
		List<ConcretTree> results = new ArrayList<>(CONFIG.getGreedyBeamSearchLimits());

		return results;
	}
	
	private List<ConcretTree> completeTreeByBeam(String ctxFea, Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToVarFeaMap){
		List<TreePredItem> trees = predTrees(ctxFea, BigDecimal.ZERO);
		
		List<ConcretTree> results = new ArrayList<>(CONFIG.getGreedyBeamSearchLimits());
		
		for(TreePredItem tree: trees) {
			completeTreeByPredict(tree, ctxFea, varToVarFeaMap, allVarInfoMap);
			
			//tree.remainHighScore();
			
			int preSize = results.size();
			List<ConcretTree> concretTrees = ConcretTree.getConcreteTrees(tree);
			
			results.addAll(concretTrees);
			
			if(preSize != 0 && results.size() > preSize) {// added new items, need re-sort
				// remain top EXPR_BEAM_NUM at each step
				Collections.sort(results, Collections.reverseOrder());
				CollectionUtil.remainListFirstK(results, CONFIG.getExprLimit());
				
			}
		} // END for(TreePredItem tree: trees)
		
		return results;
	}
	
	@Deprecated
	private void completeTreeByPredictAndProb(TreePredItem tree, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap, BigDecimal limit) {
		
		BigDecimal treeScore = tree.getFinalScore();
		
		List<TreePredItem> leafs = tree.getLeafsForCompleteTree();
		for(TreePredItem node: leafs) {
			String recurNodeFea = node.getFeature() + node.getExpandItem().getOpcode().toLabel();
			String line = ctxFea + recurNodeFea + "\t?";
			List<ExprPredItem> exprs = invoker.predictRecurExprs(line);
			
			for(ExprPredItem exprItem: exprs) {
				BigDecimal se = BigDecimal.valueOf(exprItem.getScore());
				BigDecimal currProb = se.multiply(treeScore);
				if(currProb.compareTo(limit) < 0) {
					break;
				}
				System.out.println(exprItem);
			}
		}
		
	}

	private void completeTreeByPredict(TreePredItem tree, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap) {
		
		List<TreePredItem> leafs = tree.getLeafsForCompleteTree();

		for(TreePredItem node: leafs) {
			String recurNodeFea = node.getFeature() + node.getExpandItem().getOpcode().toLabel();
			String line = ctxFea + recurNodeFea + "\t?";
			List<ExprPredItem> exprs = invoker.predictRecurExprs(line);
			List<ExprPredItem> allExprs = new ArrayList<>(CONFIG.getExprLimit());

			for(ExprPredItem exprItem: exprs) {
				
				Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list
				
				for(int n = 0; n < exprItem.getPositionNum(); n++) {
					List<String> varFeatersAtN = genRecurVarFeatureAtPosN(varToVarFeaMap, allVarInfoMap, ctxFea, recurNodeFea, exprItem, n);
					
					List<VarPredItem> varsAtN = invoker.predictRecurVar(varFeatersAtN, allVarInfoMap, n);
					
					if(!varsAtN.isEmpty()) {
						predVars.put(n, varsAtN);
					}
				}
				
				exprItem.setPredVars(predVars);
				if(predVars.size() == exprItem.getPositionNum()){
					allExprs.add(exprItem);
				}
			}// END for(ExprPredItem exprItem: exprs)
			
			ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
			List<GenExprItem> res = generator.generateExpr();
			
			if(res.size() > CONFIG.getExprLimit()) {
				res = res.subList(0, CONFIG.getExprLimit());
			}
			node.setExpressions(res);
		}// END for(TreePredItem node: leafs)
	}

	
	private List<TreePredItem> predTrees(String ctxFea, BigDecimal limit) {
		
		if(ctxFea == null) {
			throw new Error();
		}
		
		List<TreePredItem> roots = new ArrayList<>();
		TreePredItem root = new TreePredItem(null);
		roots.add(root);
		
		predictCurrNodeType(ctxFea, root, roots);
				
		List<TreePredItem> tobeRemoved = new ArrayList<>();
		
		for(TreePredItem it: roots) {
			if(!TreePredItem.isRecurNodeFullyExpanded(it)) {
				tobeRemoved.add(it);
			} else if(it.isNotNotRedundant()) {
				tobeRemoved.add(it);
			}
		}
		roots.removeAll(tobeRemoved);
		tobeRemoved.clear();
		
		for(TreePredItem it: roots) {
			it.getFinalScore();
		}
		
		final BigDecimal treeLimit = new BigDecimal(String.valueOf(CONFIG.getTreeProbLimit()));

		if(limit.compareTo(treeLimit) < 0) {
			limit = treeLimit;
		}
		
		for(TreePredItem it: roots) {
			if(it.getFinalScore().compareTo(limit) == -1) {
				tobeRemoved.add(it);
			}
		}
		roots.removeAll(tobeRemoved);
		tobeRemoved.clear();
		
		Collections.sort(roots, Collections.reverseOrder());
		
		// remain top k
		CollectionUtil.remainListFirstK(roots, CONFIG.getGreedyBeamSearchLimits());
		return roots;
	}
	
	private TreePredItem predTheLargestTree(String ctxFea) {
		TreePredItem root = new TreePredItem(null);
		String feature = ctxFea + root.getFeature() + "?";
		List<RecurNodePredItem> rootTypes = invoker.predictForNodeTypes(feature);
		
		RecurNodePredItem none = new RecurNodePredItem(Opcode.NONE.toLabel(), 1);
		if(rootTypes.isEmpty()) {
			root.setExpandItem(none);
			return root;
		}
		
		RecurNodePredItem largest = rootTypes.get(0);
		root.setExpandItem(largest);
		
		if(largest.getOpcode().equals(Opcode.AND) || largest.getOpcode().equals(Opcode.OR)) {
			TreePredItem child0 = new TreePredItem(root);
			root.setChild0(child0);
			child0.setExpandItem(none);
			TreePredItem child1 = new TreePredItem(root);
			root.setChild1(child1);
			child1.setExpandItem(none);
		} else if (largest.getOpcode().equals(Opcode.NOT)){
			TreePredItem child0 = new TreePredItem(root);
			root.setChild0(child0);
			child0.setExpandItem(none);
		}
		return root;
	}
	
	
	private void predictCurrNodeType(String ctxFea, TreePredItem currNode, List<TreePredItem> roots) {
		
		if(currNode.getCurrentHightToRoot() > TreePredItem.MAX_TREE_HIGHT - 1) {
			return;
		}
		
		String currFeature = currNode.getFeature();
		String line = ctxFea + currFeature + "?";
		List<RecurNodePredItem> dummyTypes = invoker.predictForNodeTypes(line);
		
		TreePredItem root = currNode.getRoot();
		
		for(RecurNodePredItem type: dummyTypes) {
			
			if(type.getOpcode().equals(Opcode.NONE)) {// 'NONE' use the current dummy node
				currNode.setExpandItem(type);	
				continue;
			}
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			assert newRoot.toString().equals(root.toString());
			assert newRoot.getCurrentHight() == root.getCurrentHight();
			roots.add(newRoot);
			
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, currNode);
			assert newCurrNode != null;
			assert newCurrNode.getRoot() == newRoot;
			
			newCurrNode.setExpandItem(type);
			
			if(newCurrNode.canExpandChild0()) {
				TreePredItem child0 = new TreePredItem(newCurrNode);
				newCurrNode.setChild0(child0);
				predictCurrNodeType(ctxFea, child0, roots);
			}
			if(newCurrNode.canExpandChild1()) {
				TreePredItem child1 = new TreePredItem(newCurrNode);
				newCurrNode.setChild1(child1);
				predictCurrNodeType(ctxFea, child1, roots);
			}
		}
	}
	
	private ConcretTree getLargestConcretTree(String ctxFea, Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToVarFeaMap) {
		TreePredItem largest = predTheLargestTree(ctxFea);
		
		for(TreePredItem node: largest.getLeafsForCompleteTree()) {
			String recurNodeFea = node.getFeature() + node.getExpandItem().getOpcode().toLabel();
			String line = ctxFea + recurNodeFea + "\t?";
			List<ExprPredItem> exprs = invoker.predictRecurExprs(line);
			
			ExprPredItem largestExpr = null;
			for(ExprPredItem exprItem: exprs) {
				if(ExprGenerator.isLegalExprItem(exprItem, allOriPredicates)) {
					largestExpr = exprItem;
					break;
				}
			}
			exprs.clear();
			if(largestExpr == null) {
				return null;
			}
			
			Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list

			for(int n = 0; n < largestExpr.getPositionNum(); n++) {
				List<String> varFeatersAtN = genRecurVarFeatureAtPosN(varToVarFeaMap, allVarInfoMap, ctxFea, recurNodeFea, largestExpr, n);
				List<VarPredItem> varsAtN = invoker.predictRecurVar(varFeatersAtN, allVarInfoMap, n);
				if(!varsAtN.isEmpty()) {
					predVars.put(n, varsAtN);
				}
			}
			largestExpr.setPredVars(predVars);
			List<ExprPredItem> allExprs = new ArrayList<>(1);
			allExprs.add(largestExpr);
			ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
			List<GenExprItem> res = generator.generateExpr();
			node.setExpressions(res);
		}
		
		List<ConcretTree> concretTrees = ConcretTree.getConcreteTrees(largest);
		if(concretTrees.isEmpty()) {
			return null;
		} else {
			ConcretTree res = concretTrees.get(0);
			concretTrees.clear();
			return res;
		}
	}
	
}
