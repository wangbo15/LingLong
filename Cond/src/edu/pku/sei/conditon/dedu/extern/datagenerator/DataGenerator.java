package edu.pku.sei.conditon.dedu.extern.datagenerator;

import java.util.List;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;

public abstract class DataGenerator {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	public static final int DEFAULT_VAR_RES_NUM = CONFIG.getVarLimit() * 4;
	public static final int DEFAULT_EXPR_RES_NUM = CONFIG.getExprLimit();
	
	protected static final String del = AbstractDeduVisitor.del;

	public static final String CLOSE_MSG = "#@!CLOSE\n";
	
	public static final String BU_V0_MSG = "#@!>>BU_V0";
	public static final String BU_EXPR_MSG = "#@!>>BU_EXPR";
	public static final String BU_VAR_MSG = "#@!>>BU_VAR";
	
	public static final String TD_EXPR_MSG = "#@!>>TD_EXPR";
	public static final String TD_VAR_MSG = "#@!>>TD_VAR";
	
	public static final String RECUR_NODE_MSG = "#@!>>RC_NODE";
	public static final String RECUR_EXPR_MSG = "#@!>>RC_EXPR";
	public static final String RECUR_VAR_MSG = "#@!>>RC_VAR";
	
	public static final String RCBU_V0_MSG = "#@!>>RCBU_V0";
	public static final String RCBU_V1_MSG = "#@!>>RCBU_V1";
	public static final String RCBU_E0_MSG = "#@!>>RCBU_E0";
	public static final String RCBU_E1_MSG = "#@!>>RCBU_E1";
	public static final String RCBU_R0_MSG = "#@!>>RCBU_R0";
	public static final String RCBU_R1_MSG = "#@!>>RCBU_R1";

	public static final String MSG_END = "#@!<<<<";
	
	public static boolean checkLines(List<String> lines) {
		if(lines.size() <= 1) {
			return false;
		}
		int head = lines.get(0).split("\t").length;
		for(int i = 1; i < lines.size(); i++) {
			int curr = lines.get(i).split("\t").length;
			if(curr != head) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean checkSingleLine(String line) {
		if (line.contains("\t\t")) {
			return false;
		}
		if(!line.endsWith("?")) {
			return false;
		}
		return true;
	}
	
}
