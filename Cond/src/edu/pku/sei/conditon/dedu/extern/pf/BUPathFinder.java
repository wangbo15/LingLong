package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;

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
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.MathUtil;

public class BUPathFinder extends PathFinder{
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
		
	public BUPathFinder(String projAndBug, String srcRoot, String testRoot, String filePath,
			int line, int sid, SearchStrategy searchStrategy) {
		super(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);		
	}
	
	@Override
	public void entry() throws PathFindingException{
		// prepare
		invoker.prepare();
		
		ASTNode hitNode = DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line, sid);
	
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		if(!varToVarFeaMap.isEmpty()) {
			if(!CONFIG.isPredAll() && ConstTrue.isTargetRetThrow(hitNode)) {
				VariableInfo theTrueInfo = ConstTrue.getTheTrueConstVariableInfo();			
				allVarInfoMap.put(ConstTrue.CONSTANT_TRUE, theTrueInfo);
				String varFeature = ConstTrue.genVarFeature(theTrueInfo);
				varToVarFeaMap.put(ConstTrue.CONSTANT_TRUE, varFeature);
			}
			
			String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
			
			// make start
			ProgramPoint start = makeStart();
			
			TreeSet<Path> results = getResults(start, ctxFea, varToVarFeaMap, allVarInfoMap);
			
			List<String> lines = Path.getResultLines(results);
	
			String proj_Bug_ithSusp = projAndBug + "_" + sid;
			AbsInvoker.dumpPlainResult(proj, proj_Bug_ithSusp, lines);
		}
		invoker.finish();
	}

	protected ProgramPoint makeStart() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		RecurNodePredItem expandItem = new RecurNodePredItem(Opcode.NONE.toLabel(), 1.0);
		root.setExpandItem(expandItem);
		root.setTau(true);
		
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
		
		Map<String, String> varToCtxAndVarFeaMap = connectCtxFeaAndVarFea(ctxFea, varToVarFeaMap);
		
		TreeSet<ProgramPoint> all = CollectionUtil.<ProgramPoint>newSortedSet();
		
		all.addAll(expandV0(start, varToCtxAndVarFeaMap, allVarInfoMap));
		all.addAll(expandExpr(start, varToCtxAndVarFeaMap));
		all.addAll(expandVar(start, ctxFea, varToCtxAndVarFeaMap, allVarInfoMap));
		return all;
	}
	
	private Collection<? extends ProgramPoint> expandV0(ProgramPoint start,  Map<String, String> v0FeaMap, Map<String, VariableInfo> allVarInfoMap) {

		TreePredItem root = start.getAstRoot();
		
		if(root.getExprItem() != null) {
			return Collections.emptyList();
		} else if (root.getVarList() != null && !root.getVarList().isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		
		List<VarPredItem> varsForZero = invoker.predictBUV0(allVarInfoMap, v0FeaMap);
		
		for(int i = 0; i < varsForZero.size(); i++) {
			VarPredItem var = varsForZero.get(i);
			
			if(!CONFIG.isPredAll() && ! designatedVars.isEmpty()) {
				if(!var.getLiteral().equals(designatedVars.get(0))) {
					continue;
				}
			}
			
			if(CONFIG.isDebug() && varOracle != null && varOracle.isEmpty() == false) {
				String oracle = varOracle.get(0);
				if(oracle.equals(var.getLiteral())) {
					predSequence.add(var.getLiteral() + "\t" + i + "\t" + var.getScore());
				}
			}
//			else {
//				continue;
//			}
			
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

	private List<ProgramPoint> expandExpr(ProgramPoint start, Map<String, String> varToFeaPrefixMap){
		TreePredItem root = start.getAstRoot();
		
		if(root.getVarList() == null || root.getVarList().size() != 1 || root.getExprItem() != null) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		
		assert root.getExpandItem() != null && root.getExpandItem().getOpcode() == Opcode.NONE;
		assert root.getVarList() != null && root.getVarList().size() == 1;
		
		VarPredItem v0Item = root.getVarList().get(0);
		String v0Iter = v0Item.getLiteral();
		String curVarFea = varToFeaPrefixMap.get(v0Iter);
		
//		if(CONFIG.isDebug() && predOracle != null) {
//			if(!v0Oracle.equals(v0Iter)){
//				return results;
//			}
//		}
		
		List<ExprPredItem> exprs = invoker.predictBUExprs(v0Iter, curVarFea);
		
//		String posZeroTime = "" + (pos0TimeMap.containsKey(v0Iter) ? pos0TimeMap.get(v0Iter) : 0);		
//		String exprLine = StringUtil.connectMulty(del, curVarFea, posZeroTime, "?"); 
//		List<ExprPredItem> exprs = invoker.predictExpr("TD", exprLine);
		
		String v0Oracle = "";
		if(CONFIG.isDebug() && varOracle.isEmpty() == false) {
			v0Oracle = varOracle.get(0);
		}
		
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
			
			if(!CONFIG.isPredAll() && ! designatedVars.isEmpty()) {
				if(expr.getPositionNum() != designatedVars.size()) {
					continue;
				}
			}
			
			if(CONFIG.isDebug() && predOracle != null) {
				if(v0Oracle.equals(v0Iter) && expr.getPred().equals(predOracle)) {
					predSequence.add(expr.getPred() + "\t" + i + "\t" + expr.getScore());
				}
//				else {
//					continue;
//				}
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
	
	private List<ProgramPoint> expandVar(ProgramPoint start, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap){
		TreePredItem root = start.getAstRoot();
		
		List<VarPredItem> rootVars = root.getVarList();
		ExprPredItem expr = root.getExprItem();

		if(expr == null || rootVars == null || rootVars.size() == expr.getPositionNum())
			return Collections.emptyList();
		
		List<ProgramPoint> results = new ArrayList<>();
		
		int n = rootVars.size();
		
		VarPredItem varItemAtZero = rootVars.get(0);
		List<String> varFeatersAtN = getBottomUpVarFeatureAtPosN(varToVarFeaMap, allVarInfoMap, varItemAtZero, expr, n);					
		
		List<VarPredItem> varsAtN = invoker.predictBUVars(varFeatersAtN, allVarInfoMap, n);
		
		boolean conjuntive = expr.getPred().contains("||");
		boolean disjunctive = expr.getPred().contains("&&");
		
		for(int i = 0; i < varsAtN.size(); i++) {
			VarPredItem var = varsAtN.get(i);
			
			if(!CONFIG.isPredAll() && ! designatedVars.isEmpty()) {
				if(!var.getLiteral().equals(designatedVars.get(n))) {
					continue;
				}
			}
			
			if(CONFIG.isTypeConstraint()) {
				if(! ExprGenerator.isFit(expr, var, n, allOriPredicates)) {
					continue;
				}
			}
			if(isRedundant(root, var)) {
				continue;
			}
			
			if(CONFIG.isDebug() && varOracle != null && varOracle.isEmpty() == false) {
				String v0Oracle = varOracle.get(0);
				if(v0Oracle.equals(varItemAtZero.getLiteral()) && expr.getPred().equals(predOracle)) {
					boolean allMatch = true;
					for(int j = 1; j < n; j++) {
						String predicted = root.getVarList().get(j).getLiteral();
						if(!varOracle.get(j).equals(predicted)) {
							allMatch = false;
						}
					}
					String vNOracle = varOracle.get(n);
					if(allMatch && vNOracle.equals(var.getLiteral())) {
						predSequence.add(var.getLiteral() + "\t" + i + "\t" + var.getScore());
					}
				}
			}
			
			if(!CONFIG.isPredAll()) {
				if(varItemAtZero.getLiteral().equals(var.getLiteral())) {
					continue;
				}
			}
			
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, root);
			List<VarPredItem> newVars = newCurrNode.getVarList();
			assert newCurrNode.getVarList() != root.getVarList();
			assert newVars.size() == n;
			newVars.add(var);
			newCurrNode.setVarList(newVars);
			
			if(isLiterallyCommutive(expr, newVars, conjuntive, disjunctive)) {
				continue;
			}
			
			int posNum = start.getRemainingPosNum() - 1;
			double probLog = MathUtil.getLog10(var.getScore());
			double score = start.getScore() + probLog;
			ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);				
			results.add(next);
		}
		
		return results;
	}
	
	public static Map<String, String> connectCtxFeaAndVarFea(String ctxFea, Map<String, String> varToVarFeaMap) {
		Map<String, String> varToCtxAndVarFeaMap = new HashMap<>();
		for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
			varToCtxAndVarFeaMap.put(entry.getKey(), ctxFea + "\t" + entry.getValue());
		}
		return varToCtxAndVarFeaMap;
	}

	private static Set<String> twoPosSet = new HashSet<>();
	static {
		twoPosSet.add("$ == $");
		twoPosSet.add("$ != $");
		twoPosSet.add("$ >= $");
		twoPosSet.add("$ > $");
		twoPosSet.add("$ <= $");
		twoPosSet.add("$ < $");
		twoPosSet.add("Math.abs($) > $");
		twoPosSet.add("Math.abs($) < $");
		twoPosSet.add("Math.abs($) > Math.abs($)");
		twoPosSet.add("Math.abs($) < Math.abs($)");
		twoPosSet.add("FastMath.abs($) > $");
		twoPosSet.add("FastMath.abs($) < $");
		twoPosSet.add("FastMath.abs($) > FastMath.abs($)");
		twoPosSet.add("FastMath.abs($) < FastMath.abs($)");

	}
	
	private static Set<String> fourPosSet = new HashSet<>();
	static {
		fourPosSet.add("$ == $ || $ == $");
		fourPosSet.add("$ != $ || $ != $");
		fourPosSet.add("$ > $ || $ > $");
		fourPosSet.add("($ > $) || ($ > $)");
		fourPosSet.add("$ < $ || $ > $");
		fourPosSet.add("($ < $) || ($ > $)");


		fourPosSet.add("Math.abs($) > $ || Math.abs($) > $");
		fourPosSet.add("$ == $ && $ == $");
		fourPosSet.add("$ != $ && $ != $");
		fourPosSet.add("$ > $ && $ > $");
		fourPosSet.add("($ > $) && ($ > $)");
		fourPosSet.add("($ < $) && ($ > $)");
		fourPosSet.add("Math.abs($) > $ && Math.abs($) > $");
	}
	
	protected static boolean isRedundant(TreePredItem root, VarPredItem item) {
		ExprPredItem expr = root.getExprItem();
		String pred = expr.getPred();
		List<VarPredItem> rootVars = root.getVarList();
		int n = rootVars.size();
		String itemLiteral = item.getLiteral();
		if(twoPosSet.contains(pred)) {
			if (n == 1) {
				if(rootVars.get(0).getLiteral().equals(itemLiteral)) {
					return true;
				}
			}
		} else if(fourPosSet.contains(pred)) {
			if (n == 1 || n == 3) {
				if(rootVars.get(n - 1).getLiteral().equals(itemLiteral)) {
					return true;
				}
			}
		}
		return false;
	}
}
