package edu.pku.sei.conditon.dedu.extern.datagenerator;

import java.util.ArrayList;
import java.util.List;

import edu.pku.sei.conditon.dedu.writer.RecurWriter;
import edu.pku.sei.conditon.util.StringUtil;

public class RecurDataGenerator extends DataGenerator {
	
	protected static int predictTime = 0;
	protected static int varPredictTime = 0;
	protected static int exprPredictTime = 0;
	protected static int recurNodePredictTime = 0;

	public static String genRecurExprDataForServer(String line) {
		List<String> features = new ArrayList<>(5);
		features.add(RECUR_EXPR_MSG);
		String header = RecurWriter.getRecurNodeExprHeader().trim();
		features.add(header);
		// String line = ctxFea + recurNodeFea + "\t?";
		features.add(line);

		predictTime++;
		exprPredictTime++;

		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}

	public static String genRecurVarDataForServer(List<String> features) {
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(RECUR_VAR_MSG);
		String header = RecurWriter.getRecurNodeVarHeader().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);

		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;

		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}

	public static String genRecurNodeDataForServer(String featureLine) {
		List<String> features = new ArrayList<>(5);
		features.add(RECUR_NODE_MSG);
		String header = RecurWriter.getRecurNodeTypeHeader().trim();
		features.add(header);
		
		// String line = contextFeature + recurNodeFea + "?";
		features.add(featureLine);

		predictTime++;
		recurNodePredictTime++;

		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}

}
