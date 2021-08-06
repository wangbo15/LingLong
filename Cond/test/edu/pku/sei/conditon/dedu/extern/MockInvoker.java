package edu.pku.sei.conditon.dedu.extern;

import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;

public class MockInvoker extends AbsInvoker {

	public MockInvoker(Map<String, OriPredItem> allOriPredicates, Map<String, Integer> pos0TimeMap) {
		super(allOriPredicates, pos0TimeMap);
	}

	@Override
	public void prepare() {

	}

	@Override
	public void finish() {

	}

	@Override
	public List<ExprPredItem> predictExpr(String direction, String featureLine) {
		return null;
	}

	@Override
	public List<VarPredItem> predictVar(String direction, int n, List<String> featureLines,
			Map<String, VariableInfo> allVarInfoMap) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictTDExprs(String ctxFea) {
		return null;
	}

	@Override
	public List<VarPredItem> predictTDVars(int n, List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap) {
		return null;
	}

	@Override
	public List<VarPredItem> predictBUV0(Map<String, VariableInfo> allVarInfoMap,
			Map<String, String> varToFeaPrefixMap) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictBUExprs(String v0Iter, String curVarFea) {
		return null;
	}

	@Override
	public List<VarPredItem> predictBUVars(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n) {
		return null;
	}

	@Override
	public List<RecurNodePredItem> predictRecurNodes(String featureLine) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictRecurExprs(String featureLine) {
		return null;
	}

	@Override
	public List<VarPredItem> predictRecurVar(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap,
			int n) {
		return null;
	}

	@Override
	public List<VarPredItem> predictRCBUV0(Map<String, VariableInfo> allVarInfoMap,
			Map<String, String> varToFeaPrefixMap) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictRCBUE0(String v0Feature) {
		return null;
	}

	@Override
	public List<RecurNodePredItem> predictRCBUR0(String featureLine) {
		return null;
	}

	@Override
	public List<RecurNodePredItem> predictRCBUR1(String featureLine) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictRCBUE1(String v0Feature) {
		return null;
	}

	@Override
	public List<VarPredItem> predictRCBUV1(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n) {
		return null;
	}

}
