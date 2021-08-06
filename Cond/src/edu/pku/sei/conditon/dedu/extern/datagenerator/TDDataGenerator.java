package edu.pku.sei.conditon.dedu.extern.datagenerator;

import java.util.ArrayList;
import java.util.List;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.writer.TDWriter;
import edu.pku.sei.conditon.util.StringUtil;

public class TDDataGenerator extends DataGenerator {

	protected static int predictTime = 0;
	protected static int varPredictTime = 0;
	protected static int exprPredictTime = 0;

	public static String genTopDownExprDataForServer(String contextFeature) {
		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(TD_EXPR_MSG);
		String header = TDWriter.getTopDownStepZeroHeader().trim();
		exprFeatures.add(header);

		String line = contextFeature + AbstractDeduVisitor.del + "?";
		exprFeatures.add(line);

		assert checkLines(exprFeatures.subList(1, exprFeatures.size()));

		predictTime++;
		exprPredictTime++;

		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}

	public static String genTopDownVarDataForServer(List<String> features) {
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(TD_VAR_MSG);
		String header = TDWriter.getTopDownStepOneHeader().trim();
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
