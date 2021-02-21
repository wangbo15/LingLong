package edu.pku.sei.conditon.dedu.extern.pf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;


public class ConstTrue {
	public final static String CONSTANT_TRUE = "constantTrue"; 
	public final static String CONSTANT_TRUE_TYPE = "boolean";
	
	private final static String NONE = AbstractDeduVisitor.NONE;
	private final static String del = AbstractDeduVisitor.del;
	
	public static VariableInfo getTheTrueConstVariableInfo() {
		VariableInfo trueConstInfo = new VariableInfo(null, CONSTANT_TRUE, CONSTANT_TRUE_TYPE, false, true, true);
		VariableFeature varFeature = trueConstInfo.getVariableFeature();
		varFeature.setIfCondNumber(1);
		varFeature.setFileIfCondNumber(1);
		
		return trueConstInfo;
	}
	
	public static Predicate getTheTrueConstPredicate(String clsName) {
		List<String> positionTypeList = new ArrayList<>(1);
		positionTypeList.add(CONSTANT_TRUE_TYPE);
				
		List<String> positionVarList = new ArrayList<>(1);
		positionVarList.add(CONSTANT_TRUE);
		
		PredicateFeature predicateFeature = new PredicateFeature(clsName, CONSTANT_TRUE, CONSTANT_TRUE_TYPE);
		Predicate pred = new Predicate("$", CONSTANT_TRUE, "BooleanLiteral", 1, positionTypeList, 
				positionVarList, predicateFeature);
		
		return pred;
	}
	
	public static boolean isTargetRetThrow(ASTNode node) {
		if(node == null) {
			return false;
		}
		if(node instanceof ThrowStatement) {
			if(node.getParent().getLocationInParent() == IfStatement.THEN_STATEMENT_PROPERTY) {
				return true;
			} else {
				return false;
			}
		} else if(node instanceof ReturnStatement) {
			Expression expr = ((ReturnStatement) node).getExpression();
			if(expr instanceof BooleanLiteral) {
				return true;
			} else if(expr instanceof SimpleName) {
				String name = ((SimpleName) expr).getIdentifier();
				if(ASTLocator.maybeConstant(name)) {
					return true;
				}
			}
		} 
		return false;
	}
	
	public static String genVarFeature(VariableInfo info){
		String literal = info.getNameLiteral();
		int fileCondNum = info.getVariableFeature().getFileIfCondNumber();
		int totCondNum = 10;
		String previousCond = NONE;
		VariableFeature varfea = info.getVariableFeature();
		List<String> variableFeatureList = new ArrayList<>();
		
		String type = info.getType();
		if(type.contains("<") && type.contains(">")){
			type = type.split("<")[0];
		}
		
		variableFeatureList.add(literal);
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
		
		//lastassign
		variableFeatureList.add(varfea.getLastAssign().getAssignType().toString());
		//ass_op
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.INFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.PREFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.POSTFIX)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_mtd
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.METHOD_INVOCATION)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_name
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.QUALIFIED_NAME)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.SIMPLE_NAME)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_num
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.NUMBER_LITERAL)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		
		variableFeatureList.add("" + varfea.getLastAssignDis());
		variableFeatureList.add("" + (varfea.getLastAssignDis() <= 10));//dis0 [0,10]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 10 && varfea.getLastAssignDis() <= 20));//dis0 [11,20]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 20));//dis0 (20, +infinite)
		variableFeatureList.add("" + varfea.getPreAssNum());
		variableFeatureList.add("" + varfea.isParam());
		variableFeatureList.add("" + varfea.isField());
		variableFeatureList.add("" + varfea.isFinal());
		variableFeatureList.add("" + varfea.isForStmtIndexer());

		
		String bduse = varfea.getFirstUseInBody();
//		boolean casted = false;
//		String castedTp = NONE;
//		if(bduse.startsWith(VariableInfo.CAST_USE)) {
//			casted = true;
//			if(bduse.contains("#")) {
//				castedTp = TypeUtil.removeGenericType(bduse.split("#")[1]);
//				//change all primitive type to its package type
//				castedTp = TypeUtil.getPackageType(castedTp);
//			}
//			bduse = VariableInfo.CAST_USE;
//		}
		variableFeatureList.add(bduse);
//		variableFeatureList.add("" + casted);
//		variableFeatureList.add(castedTp);
		
		variableFeatureList.add(varfea.getFirstUseOutOfStmt());
		
		variableFeatureList.add("" + varfea.getIfCondNumber());
		variableFeatureList.add("" + fileCondNum);
		variableFeatureList.add("" + totCondNum);
		
		variableFeatureList.add(previousCond);

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
