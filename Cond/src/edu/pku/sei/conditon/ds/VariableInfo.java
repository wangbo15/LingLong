package edu.pku.sei.conditon.ds;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.PositionFeature;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 18, 2017
 */
public class VariableInfo {
		
	public static final String UNKNOWN_ASSIGN = "UNKNOWN_AS";
	public static final String FIELD_ASSIGN = "FIELD_AS";
	public static final String PARAMETER_ASSIGN = "PARAM_AS";
	
	
	public static final String UNKNOWN_USE = "UNKNOWN_USE";

	public static final String DIVISIOR_USE = "DIVISIOR_USE";	// (a / x) or (a % x)
	public static final String SUBTRAHEND_USE = "SUBTRAHEND_USE";	// (a - x)
	
	public static final String THROW_USE = "THROW_USE";
	public static final String RET_USE = "RET_USE";

	public static final String CALLER_USE = "CALLER_USE";
	public static final String FIELD_USE = "FIELD_USE";
	
	public static final String PARAM_USE = "PARAM_USE";
	public static final String CONS_PARAM_USE = "CONS_PARAM_USE";
	
	public static final String ARRIDX_USE = "ARRIDX_USE";
	public static final String ARR_USE = "ARR_USE";
	public static final String OTHER_USE = "OTHER_USE";
	public static final String ASSTO_USE = "ASSTO_USE";
	public static final String INSTANCEOF_USE = "INSTANCEOF_USE";
	public static final String CAST_USE = "CAST_USE";

	public static final String NO_USE = "NO_USE";

	public static final String UNKNOWN_TYPE = "UNKNOWN_TP";
	
	private SimpleName def;
	private List<Expression> uses;
	
	private VariableFeature variableFeature;
	
	public VariableInfo(SimpleName decleartion, String nameLiteral, String type, 
			boolean isParam, boolean isField, boolean isFinal){
		this.def = decleartion;
		
		boolean isForIndexer = ASTLocator.isForIndexer(decleartion);
		
		if(type == null || type.equals("")){
			type = UNKNOWN_TYPE;
		}
		this.variableFeature = new VariableFeature(nameLiteral, type, isParam, isField, isFinal, isForIndexer);
	}
	

	public void addUse(Expression use){
		if(this.uses == null){
			this.uses = new ArrayList<Expression>();
		}
		this.uses.add(use);
	}
	
	public SimpleName getDef() {
		return def;
	}
	public void setDef(SimpleName def) {
		this.def = def;
	}
	public List<Expression> getUses() {
		return uses;
	}
	public void setUses(List<Expression> uses) {
		this.uses = uses;
	}
	
	public String getNameLiteral() {
		return variableFeature.getNameLiteral();
	}
	
	public String getType() {
		return variableFeature.getType();
	}
	
	public void setType(String tp){
		this.variableFeature.setType(tp);
	}
	
	public VariableFeature getVariableFeature(){
		return variableFeature;
	}
	
	/*
	 * Fields for deductive condations
	 */
	private PredicateFeature predicateFeature = null; //predicated
	
	private List<PositionFeature> positionFeatures = new ArrayList<>(1);// positions

	public PredicateFeature getPredicateFeature(){
		return predicateFeature;
	}
	
	public void setPredicateFeature(PredicateFeature predicateFeature){
		this.predicateFeature = predicateFeature;
	}
	
	public List<PositionFeature> getPositionFeatures() {
		return positionFeatures;
	}

	@Override
	public String toString() {
		return this.variableFeature.toString();
	}
	

	public static final String del = AbstractDeduVisitor.del;
	public static final String NONE = AbstractDeduVisitor.NONE;
	
	public String genVarFeature(){
		String literal = this.getNameLiteral();
		int fileCondNum = this.getVariableFeature().getFileIfCondNumber();
		int totCondNum = AbstractDeduVisitor.getTotalCondNum(literal);
		//String previousCond = getPreviousCondForVar(this);
		VariableFeature varfea = this.getVariableFeature();
		List<String> variableFeatureList = new ArrayList<>();
		
		String type = TypeUtil.removeGenericType(this.getType());
		
		variableFeatureList.add(this.getNameLiteral());
		variableFeatureList.add(type);
		
		//varname related features
		List<String> words = StringUtil.camelDivision(literal);
		assert words.isEmpty() == false: "AT LEAST CONTAINS ONE WORD";
		
		variableFeatureList.add("" + literal.length());	//the len of vname
		variableFeatureList.add("" + (literal.length() <= 3));	//is short name
		variableFeatureList.add("" + words.size()); //words of the vname
		variableFeatureList.add("" + literal.charAt(0));	//1st letter
		variableFeatureList.add("" + ((literal.length() >=2) ? literal.charAt(1) : NONE));	//2nd letter
		variableFeatureList.add("" + ((literal.length() >=3) ? literal.charAt(2) : NONE));	//3rd letter

		variableFeatureList.add(((words.size() > 1) ? words.get(0).toLowerCase() : NONE)); //fst word, only aaaBbb recorded
		variableFeatureList.add("" + ((words.size() >= 2) ? words.get(1).toLowerCase() : NONE)); //2nd word
		variableFeatureList.add("" + ((words.size() >= 3) ? words.get(2).toLowerCase() : NONE)); //3rd word
		
		//type related features
		variableFeatureList.add("" + varfea.isInt());
		variableFeatureList.add("" + varfea.isFloat());
		variableFeatureList.add("" + varfea.isArray());
		variableFeatureList.add("" + varfea.isCollection());
		variableFeatureList.add("" + (varfea.isArray() && TypeUtil.isPurePrimitiveType(type.replaceAll("\\[\\]", ""))));
		boolean primitiveAndSimple = (literal.length() <= 3) && TypeUtil.isPurePrimitiveType(type.replaceAll("\\[\\]", ""));
		variableFeatureList.add("" + primitiveAndSimple);
		
		//type to words
		List<String> typeWords = StringUtil.camelDivision(type);
		assert typeWords.isEmpty() == false: "AT LEAST CONTAINS ONE WORD";
		int lastIndex = typeWords.size() - 1;
		variableFeatureList.add("" + ((typeWords.size() > 1) ? typeWords.get(lastIndex).toLowerCase() : NONE)); //last 1st word
		
		// ass_op
		if (varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.INFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.PREFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.POSTFIX)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		} else {
			variableFeatureList.add(NONE);
		}
		// ass_mtd
		if (varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.METHOD_INVOCATION)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		} else {
			variableFeatureList.add(NONE);
		}
		// ass_name
		if (varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.QUALIFIED_NAME)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.SIMPLE_NAME)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		} else {
			variableFeatureList.add(NONE);
		}
		// ass_num
		if (varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.NUMBER_LITERAL)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		} else {
			variableFeatureList.add(NONE);
		}
		
		variableFeatureList.add(varfea.getLastAssign().getAssignType().toString());
		variableFeatureList.add("" + varfea.getLastAssignDis());
		variableFeatureList.add("" + (varfea.getLastAssignDis() <= 10));//dis0 [0,10]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 10 && varfea.getLastAssignDis() <= 20));//dis0 [11,20]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 20));//dis0 (20, +infinite)
		variableFeatureList.add("" + varfea.getPreAssNum());
		variableFeatureList.add("" + varfea.isParam());
		variableFeatureList.add("" + varfea.isField());
		variableFeatureList.add("" + varfea.isFinal());
		variableFeatureList.add("" + varfea.isForStmtIndexer());

		/*
		String bduse = varfea.getFirstUseInBody();
		boolean casted = false;
		String castedTp = NONE;
		if(bduse.startsWith(VariableInfo.CAST_USE)) {
			casted = true;
			if(bduse.contains("#")) {
				castedTp = bduse.split("#")[1];
			}
			bduse = VariableInfo.CAST_USE;
		}
		variableFeatureList.add(bduse);
		variableFeatureList.add("" + casted);
		variableFeatureList.add(castedTp);*/
		
		variableFeatureList.add(varfea.getFirstUseInBody());
		variableFeatureList.add(varfea.getFirstUseOutOfStmt());
		
		variableFeatureList.add("" + varfea.getIfCondNumber());
		variableFeatureList.add("" + fileCondNum);
		variableFeatureList.add("" + totCondNum);
		
		variableFeatureList.add(varfea.getPreviousCond());

		/*
		//var pred times
		List<String> predSortedByVarTime = getPredListSortedByVarTime(literal);
		variableFeatureList.add(((predSortedByVarTime.size() >= 1) ? predSortedByVarTime.get(0) : NONE));
		variableFeatureList.add(((predSortedByVarTime.size() >= 2) ? predSortedByVarTime.get(1) : NONE));
//		variableFeatureList.add(((predSortedByVarTime.size() >= 3) ? predSortedByVarTime.get(2) : NONE));
		//type pred times
		if(TypeUtil.isPurePrimitiveType(type)) {
			variableFeatureList.add(NONE);
			variableFeatureList.add(NONE);
			variableFeatureList.add(NONE);
		}else {
			List<String> predSortedByTpTime = getPredListSortedByTypeTime(type);
			variableFeatureList.add(((predSortedByTpTime.size() >= 1) ? predSortedByTpTime.get(0) : NONE));
			variableFeatureList.add(((predSortedByTpTime.size() >= 2) ? predSortedByTpTime.get(1) : NONE));
			variableFeatureList.add(((predSortedByTpTime.size() >= 3) ? predSortedByTpTime.get(2) : NONE));
		}*/
		
		
		//features from javadoc
		variableFeatureList.add(varfea.getDocExcpiton());
		variableFeatureList.add(varfea.getDocOpeartor());
		variableFeatureList.add("" + varfea.isDocZero());
		variableFeatureList.add("" + varfea.isDocOne());
		variableFeatureList.add("" + varfea.isDocNullness());
		variableFeatureList.add("" + varfea.isDocRange());
		variableFeatureList.add("" + varfea.isInDocCode());

		//return StringUtil.join(variableFeatureList, del);
		return StringUtil.join(variableFeatureList, del);
	}
}
