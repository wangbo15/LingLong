package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.DeduFeatureGenerator;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.pf.Path;
import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.MathUtil;
import edu.pku.sei.conditon.util.TypeUtil;

public class RecurPathFinder extends PathFinder{
	
	public RecurPathFinder(String projAndBug, String srcRoot, String testRoot, String filePath,
			int line, int sid, SearchStrategy searchStrategy) {
		super(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);
	}
	
	@Override
	public void entry() throws PathFindingException{
		// prepare
		invoker.prepare();
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		// make start
		ProgramPoint start = makeStart();
		
		TreeSet<Path> results = getResults(start, ctxFea, varToVarFeaMap, allVarInfoMap);

		List<String> lines = Path.getResultLines(results);

		String proj_Bug_ithSusp = projAndBug + "_" + sid;
		AbsInvoker.dumpPlainResult(proj, proj_Bug_ithSusp, lines);
		
		invoker.finish();
	}
	

	private ProgramPoint makeStart() {
		TreePredItem root = new TreePredItem(null);
		// score = log(1.0) = 0
		ProgramPoint startPoint = new ProgramPoint(null, root, 0.0D, 1);
		return startPoint;
	}
	
	@Override
	protected TreeSet<ProgramPoint> expand(ProgramPoint start, String ctxFea, 
			Map<String, String> varToVarFeaMap, 
			Map<String, VariableInfo> allVarInfoMap){
		
		if(start.isComplete()) {
			return CollectionUtil.emptyTreeSet();
		}
		if(start.getAstRoot().getCurrentLargestDepth() > CONFIG.getTreeDepth() - 1) {
			return CollectionUtil.emptyTreeSet();
		}
		
		TreeSet<ProgramPoint> all = CollectionUtil.<ProgramPoint>newSortedSet();
		
		List<ProgramPoint> recurNodeExpansions = expandRecurNode(start, ctxFea);
		all.addAll(recurNodeExpansions);
		
		List<ProgramPoint> exprExpansions = expandExpr(start, ctxFea);
		all.addAll(exprExpansions);
		
		List<ProgramPoint> varExpansions = expandVar(start, ctxFea, varToVarFeaMap, allVarInfoMap);
		all.addAll(varExpansions);
		return all;
	}
	
	private List<ProgramPoint> expandRecurNode(ProgramPoint start, String ctxFea){
		// get expand node
		TreePredItem root = start.getAstRoot();
		if(root.isRecurNodeComplete()) {
			return Collections.emptyList();
		}
		
		List<TreePredItem> expansions = root.recurNodeExpansionPositions();
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			assert treeItem.getExpandItem() == null;
			
			// predict next treeItem
			String currFeature = treeItem.getFeature();
			String line = ctxFea + currFeature + "?";
			List<RecurNodePredItem> dummyTypes = invoker.predictForNodeTypes(line);
			
			for(RecurNodePredItem type: dummyTypes) {
				if(type.getScore() < CONFIG.getRnProbLimit()) {
					break;
				}
				TreePredItem newRoot;
				if(CONFIG.isOpt()) {
					newRoot = TreePredItem.sharedCopy(root, treeItem);
				} else {
					newRoot = TreePredItem.deepCloneFromRoot(root);
				}
				TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, treeItem);
				newCurrNode.setExpandItem(type);
				
				if(newCurrNode.canExpandChild0()) {
					TreePredItem child0 = new TreePredItem(newCurrNode);
					newCurrNode.setChild0(child0);
				}
				if(newCurrNode.canExpandChild1()) {
					TreePredItem child1 = new TreePredItem(newCurrNode);
					newCurrNode.setChild1(child1);
				}
				
				// filter !!(#)
				if(newRoot.isNotNotRedundant()) {
					continue;
				}
				
				int posNum = start.getRemainingPosNum() - 1 + type.getOpcode().getPositionNum();				
				double probLog = MathUtil.getLog10(type.getScore());
				double score = start.getScore() + probLog;
				ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);
				results.add(next);
			}
			
		}
		return results;
	}
	
	private List<ProgramPoint> expandExpr(ProgramPoint start, String ctxFea){
		TreePredItem root = start.getAstRoot();
		List<TreePredItem> expansions = root.exprExpansionPositions();
		
		if(expansions.isEmpty())
			return Collections.emptyList();
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			assert treeItem.getExpandItem() != null && treeItem.getExpandItem().getOpcode() == Opcode.NONE;
			// predict expr
			String recurNodeFea = treeItem.getFeature() + treeItem.getExpandItem().getOpcode().toLabel();
			String line = ctxFea + recurNodeFea + "\t?";
			List<ExprPredItem> exprs = invoker.predictRecurExprs(line);
			
			for(ExprPredItem expr: exprs) {
				if(expr.getScore() < CONFIG.getExprProbLimit()) {
					break;
				}
				
				if(expr.getPositionNum() == 0) {
					continue;
				}
				TreePredItem newRoot;
				if(CONFIG.isOpt()) {
					newRoot = TreePredItem.sharedCopy(root, treeItem);
				} else {
					newRoot = TreePredItem.deepCloneFromRoot(root);
				}
				TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, treeItem);
				newCurrNode.setExprItem(expr);
				
				int posNum = start.getRemainingPosNum() - 1 + expr.getPositionNum();
				double probLog = MathUtil.getLog10(expr.getScore());
				double score = start.getScore() + probLog;
				ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);
				results.add(next);
			}
			
		}
		return results;
	}
	
	private List<ProgramPoint> expandVar(ProgramPoint start, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap){
		TreePredItem root = start.getAstRoot();
		List<TreePredItem> expansions = root.varExpansionPositions();
		
		if(expansions.isEmpty())
			return Collections.emptyList();
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			String recurNodeFea = treeItem.getFeature() + treeItem.getExpandItem().getOpcode().toLabel();
			ExprPredItem expr = treeItem.getExprItem();
			assert expr != null;
			
			//TODO get filtered exprs
			
			if(treeItem.getVarList() == null)
				treeItem.setVarList(new ArrayList<VarPredItem>());
			
			int n = treeItem.getVarList().size();
			
			List<String> varFeatersAtN = genRecurVarFeatureAtPosN(varToVarFeaMap, allVarInfoMap, ctxFea, recurNodeFea, expr, n);
			List<VarPredItem> varsAtN = invoker.predictRecurVar(varFeatersAtN, allVarInfoMap, n);
			
			for(VarPredItem var: varsAtN) {
				if(var.getScore() < CONFIG.getVarProbLimit()) {
					break;
				}
				
				if(CONFIG.isTypeConstraint()) {
					if(! ExprGenerator.isFit(expr, var, n, allOriPredicates)) {
						continue;
					}
				}
				
				TreePredItem newRoot;
				if(CONFIG.isOpt()) {
					newRoot = TreePredItem.sharedCopy(root, treeItem);
				} else {
					newRoot = TreePredItem.deepCloneFromRoot(root);
				}
				TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, treeItem);
				List<VarPredItem> newVars = newCurrNode.getVarList();
				assert newCurrNode.getVarList() != treeItem.getVarList();
				assert newVars.size() == n;
				newVars.add(var);
				newCurrNode.setVarList(newVars);
				
				// filter (A || A) or (A && A)
				if(newCurrNode.isCommutativeRedundant()) {
					//System.out.println(">>>> FILTER " + newRoot.toString());
					continue;
				}
				
				int posNum = start.getRemainingPosNum() - 1;
				
				double probLog = MathUtil.getLog10(var.getScore());
				double score = start.getScore() + probLog;
				ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);				
				results.add(next);
			}
		}
		return results;
	}
}
