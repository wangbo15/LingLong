package edu.pku.sei.conditon.dedu.extern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;

public class Predictor {
	
	protected static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	protected static final String del = AbstractDeduVisitor.del;

	protected String projAndBug;
	protected String model;
	protected String proj;
	protected String srcRoot;
	protected String testRoot;
	
	protected Map<String, OriPredItem> allOriPredicates;
	protected Map<String, Integer> pos0TimeMap;
	
	protected AbsInvoker invoker;
	
	/** mapping project name to its server port */
	public static final Map<String, Integer> PROJ_TO_PORT = new HashMap<>();
	{
		PROJ_TO_PORT.put("math", SocketInvoker.DEFAULT_PORT);
		PROJ_TO_PORT.put("lang", SocketInvoker.DEFAULT_PORT + 1);
		PROJ_TO_PORT.put("chart", SocketInvoker.DEFAULT_PORT + 2);
		PROJ_TO_PORT.put("time", SocketInvoker.DEFAULT_PORT + 3);
	}
	
	protected Predictor(String projAndBug, String srcRoot, String testRoot) {
		this.projAndBug = projAndBug;
		
		if(CONFIG.isPredAll()) {
			// for pred all
			this.model = projAndBug;		
		} else {
			// for repair
			this.model = AbsInvoker.bugToModelMap.get(projAndBug);		
		}
		
		this.proj = model.split("_")[0];
		this.srcRoot = srcRoot;
		this.testRoot = testRoot;
		
		String allpredPath = AbsInvoker.PREDICTOR_ROOT + "/input/" + proj + "/" + model + "/expr/" + model + ".allpred.csv";
		
		this.allOriPredicates = AbsInvoker.loadAllOriPredicate(allpredPath);
		this.pos0TimeMap = AbsInvoker.getPos0TimeMap(allOriPredicates);
		
		if(CONFIG.isUseSocket()) {
			assert PROJ_TO_PORT.containsKey(proj): ("ERROR PROJ: " + proj);
			int port = PROJ_TO_PORT.get(proj);
			this.invoker = new SocketInvoker(port, allOriPredicates, pos0TimeMap);
		} else {
			//TODO: For FileInvoker
			throw new Error();
		}
	}

	protected List<String> genRecurVarFeatureAtPosN(Map<String, String> varToVarFeaMap, Map<String, VariableInfo> allVarInfoMap, 
			String ctxFea, String recurNodeFea, ExprPredItem exprItem, int n){
		
		List<String> varFeatersAtN = new ArrayList<>(CONFIG.getVarLimit());
		String predFeature = exprItem.getExprFeature();

		for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
			List<String> varLineList = new ArrayList<>();
			String varFeaPrefix = entry.getValue();
			varLineList.add(ctxFea + recurNodeFea); // recurNodeFea is starts with and end with 'TAB' !!
			
			varLineList.add(varFeaPrefix);
			varLineList.add(predFeature);
			
			String varName = entry.getKey();
			boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), varName);
			varLineList.add("" + argued); //argused
			
			VariableInfo info = allVarInfoMap.get(varName);
			if(info == null) {
				info = allVarInfoMap.get(varName + "#F");
				if(info == null) {
					continue;
				}
			}
			boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, info, allOriPredicates); 
			varLineList.add("" + typeFit); //argused

			int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(varName, exprItem.getPred(), n, allOriPredicates);
			if(occuredTime == 0 && argued) {
				occuredTime ++;
			}
			varLineList.add("" + occuredTime); //occuredTime

			varLineList.add("" + n); //position
			varLineList.add("?"); //putin
			String currStr = StringUtil.join(varLineList, del);
			
			varFeatersAtN.add(currStr);
		}
		return varFeatersAtN;
	}
	
	protected List<String> genRCBUV1FeatureAtPosN(Map<String, String> varToVarFeaMap, Map<String, VariableInfo> allVarInfoMap, 
			String ctxFea, String recurNodeFea, ExprPredItem exprItem, int n){
		
		List<String> varFeatersAtN = new ArrayList<>(CONFIG.getVarLimit());
		String predFeature = exprItem.getExprFeature();

		for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
			List<String> varLineList = new ArrayList<>();
			String varFeaPrefix = entry.getValue();
			varLineList.add(ctxFea + recurNodeFea); // recurNodeFea is starts with and end with 'TAB' !!
			
			varLineList.add(varFeaPrefix);
			varLineList.add(predFeature);
			
			String varName = entry.getKey();
			VariableInfo info = allVarInfoMap.get(varName);
			if(info == null) {
				info = allVarInfoMap.get(varName + "#F");
				if(info == null) {
					continue;
				}
			}

			varLineList.add("" + n); //position
			varLineList.add("?"); //putin
			String currStr = StringUtil.join(varLineList, del);
			
			currStr = currStr.replaceAll("\t\t", "\t");
			varFeatersAtN.add(currStr);
		}
		return varFeatersAtN;
	}
	
	protected List<String> genTopDownVarFeatureAtPosN(int n, String contextFeature, Map<String, String> varToVarFeaMap, ExprPredItem exprItem, Map<String, VariableInfo> allVarInfoMap) {
		List<String> varFeatersAtN = new ArrayList<>(CONFIG.getVarLimit());
		String predFeature = exprItem.getExprFeature();
		for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
			List<String> varLineList = new ArrayList<>();
			String varFeaPrefix = entry.getValue();
			varLineList.add(contextFeature);
			varLineList.add(varFeaPrefix);
			varLineList.add(predFeature);

			String varName = entry.getKey();
			boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), varName);
			varLineList.add("" + argued); //argused
			
			VariableInfo info = allVarInfoMap.get(varName);
			if(info == null) {
				info = allVarInfoMap.get(varName + "#F");
			}
			if(info == null) {
				continue;
			}
			boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, info, allOriPredicates); 
			varLineList.add("" + typeFit); //argused

			int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(varName, exprItem.getPred(), n, allOriPredicates);
			if(occuredTime == 0 && argued) {
				occuredTime ++;
			}
			varLineList.add("" + occuredTime); //occuredTime

			varLineList.add("" + n); //position
			varLineList.add("?"); //putin
			String currStr = StringUtil.join(varLineList, del);
			varFeatersAtN.add(currStr);
		}
		return varFeatersAtN;
	}
	
	protected List<String> getBottomUpVarFeatureAtPosN(Map<String, String> varToFeaPrefixMap, Map<String, VariableInfo> allVarInfoMap, VarPredItem varItemAtZero, ExprPredItem exprItem, int n){
		List<String> varFeatersAtN = new ArrayList<>(CONFIG.getVarLimit());
				
		for(VariableInfo info: allVarInfoMap.values()){
			String infoLit = info.getNameLiteral();
			boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), infoLit);
			String argUsed = argued ? "true" : "false";
			boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, info, allOriPredicates); 
			String vNfit = typeFit ? "true" : "false";
			String vNUsed = (infoLit.equals(varItemAtZero.getLiteral())) ? "true" : "false";
			String vNPrefix = varToFeaPrefixMap.get(infoLit);
			assert vNPrefix != null;
			int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(infoLit, exprItem.getPred(), n, allOriPredicates);
			if(occuredTime == 0 && argued) {
				occuredTime ++;
			}
			String predFeature = exprItem.getExprFeature();
			String varNLine = StringUtil.connectMulty(del, vNPrefix, predFeature, argUsed, vNfit, "" + occuredTime, vNUsed, "" + n, "?");
			varFeatersAtN.add(varNLine);
		}
		return varFeatersAtN;
	}
	
}
