package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.MathUtil;

public class RecurBUPathFinder extends PathFinder {

	public RecurBUPathFinder(String projAndBug, String srcRoot, String testRoot, String filePath, int line, int sid,
			SearchStrategy searchStrategy) {
		super(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);
	}

	@Override
	protected TreeSet<ProgramPoint> expand(ProgramPoint start, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap) {
		
		// first try to expand upward
		
		// then try to expand downward
		if(start.isComplete()) {
			return CollectionUtil.emptyTreeSet();
		}
		
		Map<String, String> varToCtxAndVarFeaMap = BUPathFinder.connectCtxFeaAndVarFea(ctxFea, varToVarFeaMap);

		TreeSet<ProgramPoint> all = CollectionUtil.<ProgramPoint>newSortedSet();
		
		// expandVarUpward
		List<ProgramPoint> v0 = (List<ProgramPoint>) expandV0(start, varToCtxAndVarFeaMap, allVarInfoMap);
		all.addAll(v0);
		
		// expandExprUpward
		List<ProgramPoint> e0 = (List<ProgramPoint>) expandE0(start, varToCtxAndVarFeaMap);
		all.addAll(e0);
		
		// expandRecurUpward
		List<ProgramPoint> r0 = (List<ProgramPoint>) expandR0(start, ctxFea);
		all.addAll(r0);
		
		// expandRecurDownward
		List<ProgramPoint> r1 = (List<ProgramPoint>) expandR1(start, ctxFea);
		all.addAll(r1);
		
		// expandExprDownward
		List<ProgramPoint> e1 = (List<ProgramPoint>) expandE1(start, ctxFea);
		all.addAll(e1);

		// expandVar
		List<ProgramPoint> v1 = (List<ProgramPoint>) expandV1(start, ctxFea, varToVarFeaMap, allVarInfoMap);
		all.addAll(v1);
		
		return all;
	}
	
	protected ProgramPoint makeStart() {
		final boolean isTao = false;
		TreePredItem root = TreePredItem.getRootInstance(isTao);
		RecurNodePredItem expandItem = new RecurNodePredItem(Opcode.NONE.toLabel(), 0.0);
		root.setExpandItem(expandItem);
		
		ProgramPoint startPoint = new ProgramPoint(null, root, 0.0D, 1);
		return startPoint;
	}
	
	
	private List<ProgramPoint> expandV0(ProgramPoint start, Map<String, String> v0FeaMap, Map<String, VariableInfo> allVarInfoMap) {
		TreePredItem root = start.getAstRoot();
		
		if(!root.isBottomLeftMost()) {
			return Collections.emptyList();
		}
		if (!root.isRecurNodeComplete()) {
			return Collections.emptyList();
		}
		
		if(root.getExprItem() != null) {
			return Collections.emptyList();
		} else if (root.getVarList() != null && !root.getVarList().isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		
		List<VarPredItem> varsForZero = invoker.predictRCBUV0(allVarInfoMap, v0FeaMap);
		for(int i = 0; i < varsForZero.size(); i++) {
			VarPredItem var = varsForZero.get(i);
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, root);
			assert newCurrNode.getVarList() == null;

			List<VarPredItem> newVars = new ArrayList<VarPredItem>();
			newVars.add(var);
			newCurrNode.setVarList(newVars);
			double probLog = MathUtil.getLog10(var.getScore());
			double score = start.getScore() + probLog;
			ProgramPoint next = new ProgramPoint(start, newRoot, score, 1);				
			results.add(next);
		}
		return results;
	}
	
	
	private List<ProgramPoint> expandE0(ProgramPoint start, Map<String, String> varToFeaPrefixMap){
		TreePredItem root = start.getAstRoot();
		
		if(!root.isBottomLeftMost()) {
			return Collections.emptyList();
		}
		if (!root.isRecurNodeComplete()) {
			return Collections.emptyList();
		}
		
		if(root.getVarList() == null || root.getVarList().size() != 1 || root.getExprItem() != null) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		
		assert root.getExpandItem() != null && root.getExpandItem().getOpcode() == Opcode.NONE;
		assert root.getVarList() != null && root.getVarList().size() == 1;
		
		VarPredItem v0Item = root.getVarList().get(0);
		String v0Iter = v0Item.getLiteral();
		String curVarFea = varToFeaPrefixMap.get(v0Iter);
		
		List<ExprPredItem> exprs = invoker.predictRCBUE0(curVarFea);
		
		for(int i = 0; i < exprs.size(); i++) {
			ExprPredItem expr = exprs.get(i);
			if(expr.getPositionNum() == 0) {
				continue;
			}
			
			if(CONFIG.isTypeConstraint()) {
				if(! ExprGenerator.isFit(expr, v0Item, 0, allOriPredicates)){
					continue;
				}
			}
			
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, root);
			newCurrNode.setExprItem(expr);
			
			int posNum = start.getRemainingPosNum() - 2 + expr.getPositionNum();
			
			double logProb = MathUtil.getLog10(expr.getScore());
			double score = start.getScore() + logProb;
			ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);
			results.add(next);
		}
		return results;
	}
	
	private List<ProgramPoint> expandR0(ProgramPoint start, String ctxFea){
		// get expand node
		TreePredItem root = start.getAstRoot();
		if (root.isTau()) {
			return Collections.emptyList();
		}
		RecurNodePredItem recurNodePredItem = root.getExpandItem();
		assert recurNodePredItem != null;
		
		List<ProgramPoint> results = new ArrayList<>();
		String line = ctxFea + root.getUpwardFeature();
		
		List<RecurNodePredItem> nodeTypes = invoker.predictRCBUR0(line);
		
		assert nodeTypes.size() == 4;
		
		for(RecurNodePredItem type: nodeTypes) {
			if(type.getScore() < CONFIG.getRnProbLimit()) {
				break;
			}
			
			TreePredItem rootCopy = TreePredItem.deepCloneFromRoot(root);
			
			TreePredItem upwardExtendedNode;
			if (type.isNone()) {
				// the tree is restricted to grow upward
				rootCopy.setTau(true);
				upwardExtendedNode = rootCopy;
			} else {
				upwardExtendedNode = TreePredItem.getRootInstance(false);
				rootCopy.setParent(upwardExtendedNode);
				upwardExtendedNode.setChild0(rootCopy);
				
				assert upwardExtendedNode.isRoot();
				
				upwardExtendedNode.setExpandItem(type);
				if(type.isNot()) {
					if(upwardExtendedNode.isNotNotRedundant()) {
						continue;
					}
				}
				if (type.isAnd() || type.isOr()) {
					TreePredItem newCld1 = TreePredItem.getInstance(upwardExtendedNode);
					upwardExtendedNode.setChild1(newCld1);
					// newCld1 has no `expandItem`
					assert newCld1.getExpandItem() == null;
				}
			}
			int posNum = start.getRemainingPosNum() - 1 + type.getOpcode().getPositionNum();				
			double probLog = MathUtil.getLog10(type.getScore());
			double score = start.getScore() + probLog;
			ProgramPoint next = new ProgramPoint(start, upwardExtendedNode, score, posNum);
			results.add(next);
		}
		return results;
	}
	
	private List<ProgramPoint> expandR1(ProgramPoint start, String ctxFea){
		// get expand node
		TreePredItem root = start.getAstRoot();
		if(root.isRecurNodeComplete() || !root.isTau()) {
			return Collections.emptyList();
		}
		
		List<TreePredItem> expansions = root.recurNodeExpansionPositions();
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			assert treeItem.getExpandItem() == null;
			
			// predict next treeItem
			String currFeature = treeItem.getDownwardFeature();
			String line = ctxFea + currFeature + "?";
			List<RecurNodePredItem> dummyTypes = invoker.predictRCBUR1(line);
			
			assert dummyTypes.size() == 4;
			
			for(RecurNodePredItem type: dummyTypes) {
				if(type.getScore() < CONFIG.getRnProbLimit()) {
					break;
				}
				TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
				TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, treeItem);
				newCurrNode.setExpandItem(type);
				
				if(newCurrNode.canExpandChild0()) {
					TreePredItem child0 = TreePredItem.getInstance(newCurrNode);
					newCurrNode.setChild0(child0);
				}
				if(newCurrNode.canExpandChild1()) {
					TreePredItem child1 = TreePredItem.getInstance(newCurrNode);
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
	
	private List<ProgramPoint> expandE1(ProgramPoint start, String ctxFea){
		TreePredItem root = start.getAstRoot();
		if(!root.isTau() || !root.isRecurNodeComplete() || root.isBottomLeftMost()) {
			return Collections.emptyList();
		}
		
		List<TreePredItem> expansions = root.exprExpansionPositions();
		if(expansions.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			assert treeItem.getExpandItem() != null && treeItem.getExpandItem().getOpcode() == Opcode.NONE;
			String recurNodeFea = treeItem.getDownwardFeature();
			String line = ctxFea + recurNodeFea + "?";
			List<ExprPredItem> exprs = invoker.predictRCBUE1(line);
			
			for(ExprPredItem expr: exprs) {
				if(expr.getScore() < CONFIG.getExprProbLimit()) {
					break;
				}
				
				if(expr.getPositionNum() == 0) {
					continue;
				}
				TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
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

	private List<ProgramPoint> expandV1(ProgramPoint start, String ctxFea, Map<String, String> varToVarFeaMap, Map<String, VariableInfo> allVarInfoMap) {
		TreePredItem root = start.getAstRoot();
		if(!root.isTau() || !root.isRecurNodeComplete() || root.isBottomLeftMost()) {
			return Collections.emptyList();
		}
		
		if(root.getExprItem() != null) {
			return Collections.emptyList();
		} else if (root.getVarList() != null && !root.getVarList().isEmpty()) {
			return Collections.emptyList();
		}
		
		List<TreePredItem> expansions = root.varExpansionPositions();
		if(expansions.isEmpty())
			return Collections.emptyList();
		
		List<ProgramPoint> results = new ArrayList<>();
		for(TreePredItem treeItem: expansions) {
			String recurNodeFea = treeItem.getDownwardFeature();
			ExprPredItem expr = treeItem.getExprItem();
			assert expr != null;
			
			//TODO get filtered exprs
			
			if(treeItem.getVarList() == null)
				treeItem.setVarList(new ArrayList<VarPredItem>());
			
			int n = treeItem.getVarList().size();
			
			List<String> varFeatersAtN = genRCBUV1FeatureAtPosN(varToVarFeaMap, allVarInfoMap, ctxFea, recurNodeFea, expr, n);
			List<VarPredItem> varsAtN = invoker.predictRCBUV1(varFeatersAtN, allVarInfoMap, n);
			
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
					newRoot = TreePredItem.sharedCopyFromLeaf(root, treeItem);
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
