package edu.pku.sei.conditon.dedu.extern.datagenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.pku.sei.conditon.dedu.writer.BUWriter;
import edu.pku.sei.conditon.util.StringUtil;

public class BUDataGenerator extends DataGenerator {
	
	protected static int predictTime = 0;
	protected static int varPredictTime = 0;
	protected static int exprPredictTime = 0;
	
	private static void generateBottomUpV0Lines(List<String> result, Map<String, String> varToFeaPrefixMap, Map<String, Integer> pos0TimeMap) {
		assert result != null;
		
		for(Entry<String, String> entry: varToFeaPrefixMap.entrySet()) {
			String var = entry.getKey();
			int at0Time = 0;
			if(pos0TimeMap.containsKey(var)) {
				at0Time = pos0TimeMap.get(var);
			}
			String line = StringUtil.connectMulty(del, entry.getValue(), "" + at0Time, "?");
			result.add(line);
		}
	}
	
	public static String genBottomUpV0DataForServer(Map<String, String> varToFeaPrefixMap, Map<String, Integer> pos0TimeMap) {
		List<String> varFeatures = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		varFeatures.add(BU_V0_MSG);
		String header = BUWriter.getButtomUpStepZeroHeader().trim();
		varFeatures.add(header);
		generateBottomUpV0Lines(varFeatures, varToFeaPrefixMap, pos0TimeMap);
		
		if(varFeatures.size() == 2) {
			return null;
		}
		assert checkLines(varFeatures.subList(1, varFeatures.size()));
		
		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
	
	public static String genBottomUpExprDataForServer(String v0Iter, String v0Fea, Map<String, Integer> pos0TimeMap) {
		String posZeroTime = "" + (pos0TimeMap.containsKey(v0Iter) ? pos0TimeMap.get(v0Iter) : 0);
		String exprLine = StringUtil.connectMulty(del, v0Fea, posZeroTime, "?"); 

		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(BU_EXPR_MSG);
		String header = BUWriter.getButtomUpStepOneHeader().trim();
		exprFeatures.add(header);
		exprFeatures.add(exprLine);
		
		assert checkLines(exprFeatures.subList(1, exprFeatures.size()));
		
		//FileUtil.writeStringToFile("/home/nightwish/tmp/bu.pred.csv", exprLine + "\n", true);
		
		predictTime++;
		exprPredictTime++;
		
		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}
	
	public static String genBottomUpVarDataForServer(List<String> features){
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(BU_VAR_MSG);
		String header = BUWriter.getButtomUpStepTwoHeader().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);
		
		assert checkLines(varFeatures.subList(1, varFeatures.size()));
		
		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
}
