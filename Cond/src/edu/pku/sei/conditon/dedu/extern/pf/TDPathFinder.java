package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
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
import edu.pku.sei.conditon.util.TypeUtil;

public class TDPathFinder extends PathFinder{
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	public TDPathFinder(String projAndBug, String srcRoot, String testRoot, String filePath,
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
		RecurNodePredItem expandItem = new RecurNodePredItem(Opcode.NONE.toLabel(), 1.0);
		root.setExpandItem(expandItem);
		
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
		all.addAll(expandExpr(start, ctxFea));
		all.addAll(expandVar(start, ctxFea, varToVarFeaMap, allVarInfoMap));
		return all;
	}
	
	private List<ProgramPoint> expandExpr(ProgramPoint start, String ctxFea){
		TreePredItem root = start.getAstRoot();

		if(root.getExprItem() != null) {
			return Collections.emptyList();
		}
		
		List<ProgramPoint> results = new ArrayList<>();
		assert root.getExpandItem() != null && root.getExpandItem().getOpcode() == Opcode.NONE;

		List<ExprPredItem> exprs = invoker.predictTDExprs(ctxFea);
		
		for(int i = 0; i < exprs.size(); i++) {
			ExprPredItem expr = exprs.get(i);
			if(expr.getPositionNum() == 0) {
				continue;
			}
			if(CONFIG.isDebug() && predOracle != null) {
				if(expr.getPred().equals(predOracle)) {
					predSequence.add(expr.getPred() + "\t" + i + "\t" + expr.getScore());
				}
			}
			
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, root);
			newCurrNode.setExprItem(expr);
			
			int posNum = start.getRemainingPosNum() - 1 + expr.getPositionNum();
			
			double probLog = MathUtil.getLog10(expr.getScore());
			double score = start.getScore() + probLog;
			ProgramPoint next = new ProgramPoint(start, newRoot, score, posNum);
			results.add(next);
		}
		
		return results;
	}
	
	private List<ProgramPoint> expandVar(ProgramPoint start, String ctxFea, Map<String, String> varToVarFeaMap,
			Map<String, VariableInfo> allVarInfoMap){
		
		TreePredItem root = start.getAstRoot();
		List<ProgramPoint> results = new ArrayList<>();
		ExprPredItem expr = root.getExprItem();

		if(expr == null) {
			return Collections.emptyList();
		} else if(root.getVarList() != null && root.getVarList().size() == expr.getPositionNum()) {
			return Collections.emptyList();
		}
		
		if(root.getVarList() == null)
			root.setVarList(new ArrayList<VarPredItem>());
		
		int n = root.getVarList().size();
		
		List<String> varFeatersAtN = genTopDownVarFeatureAtPosN(n, ctxFea, varToVarFeaMap, expr, allVarInfoMap);
		//List<VarPredItem> varsAtN = invoker.predictVar("BU", n, varFeatersAtN, allVarInfoMap);
		List<VarPredItem> varsAtN = invoker.predictTDVars(n, varFeatersAtN, allVarInfoMap);
		
		boolean conjuntive = expr.getPred().contains("||");
		boolean disjunctive = expr.getPred().contains("&&");
		
		for(int i = 0; i < varsAtN.size(); i++) {
			VarPredItem var = varsAtN.get(i);
			
			if(CONFIG.isTypeConstraint()) {
				if(! ExprGenerator.isFit(expr, var, n, allOriPredicates)) {
					continue;
				}
			}
			
			if(CONFIG.isDebug() && varOracle != null && varOracle.isEmpty() == false) {
				if(expr.getPred().equals(predOracle)) {
					boolean allMatch = true;
					for(int j = 0; j < n; j++) {
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
}
