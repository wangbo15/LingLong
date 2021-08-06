package edu.pku.sei.conditon.dedu.extern.datagenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.dedu.writer.RecurBUWriter;
import edu.pku.sei.conditon.util.StringUtil;

public class RecurBUDataGenerator extends DataGenerator {
	
	public static String genRCBUV0DataForServer(Map<String, String> varToFeaPrefixMap) {
		assert !varToFeaPrefixMap.isEmpty();
		List<String> varFeatures = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		varFeatures.add(RCBU_V0_MSG);
		
		String header = RecurBUWriter.getRecurBUV0Header().trim();
		varFeatures.add(header);
		
		for(String val: varToFeaPrefixMap.values()) {
			String line = val + del + "?";
			varFeatures.add(line);
		}
		
		assert checkLines(varFeatures.subList(1, varFeatures.size()));
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
	
	public static String genRCBUV1DataForServer(List<String> features) {
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(RCBU_V1_MSG);
		String header = RecurBUWriter.getRecurBUV1Header().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);

		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		data = data.replaceAll("\t\t", "\t");
		return data;
	}
	
	public static String genRCBUE0DataForServer(String v0Fea) {
		assert v0Fea != null && !v0Fea.isEmpty();
		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(RCBU_E0_MSG);
		String header = RecurBUWriter.getRecurBUE0Header().trim();
		exprFeatures.add(header);
		String line = StringUtil.connect(v0Fea, "?", del);
		exprFeatures.add(line);
		
		assert checkLines(exprFeatures.subList(1, exprFeatures.size()));
		
		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}
	
	public static String genRCBUE1DataForServer(String line) {
		assert line != null && !line.isEmpty();
		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(RCBU_E1_MSG);
		String header = RecurBUWriter.getRecurBUE1Header().trim();
		exprFeatures.add(header);
		exprFeatures.add(line);
		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}
	
	public static String genRCBUR0DataForServer(String fea) {
		assert fea != null && !fea.isEmpty();
		List<String> features = new ArrayList<>(5);
		features.add(RCBU_R0_MSG);
		String header = RecurBUWriter.getRecurBUR0Header().trim();
		features.add(header);
		String line = fea + "?";
		features.add(line);
		
		assert checkLines(features.subList(1, features.size()));
		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}
	
	public static String genRCBUR1DataForServer(String line) {
		assert line != null && !line.isEmpty();
		List<String> features = new ArrayList<>(5);
		features.add(RCBU_R1_MSG);
		String header = RecurBUWriter.getRecurBUR1Header().trim();
		features.add(header);
		features.add(line);
		
		assert checkLines(features.subList(1, features.size()));
		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}

}
