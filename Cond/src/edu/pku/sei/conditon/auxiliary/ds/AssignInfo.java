package edu.pku.sei.conditon.auxiliary.ds;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class AssignInfo {
	
	public static enum AssignType{
		UNKNOWN_ASSIGN,
		FIELD_ASSIGN,
		PARAMETER_ASSIGN,
		INFIX,
		PREFIX,
		POSTFIX,
		ARRAY_ACCESS,
		ARRAY_CREATION,
		CONSTRUCTOR_INVOCATION,
		CLASS_INSTANCE_CREATION,
		FIELD_ACCESS,
		METHOD_INVOCATION,
		CAST_EXPRESSION,
		QUALIFIED_NAME,
		SIMPLE_NAME,
		NULL_LITERAL,
		BOOLEAN_LITERAL,
		STRING_LITERAL,
		TYPE_LITERAL,
		CHARACTER_LITERAL,
		NUMBER_LITERAL,
		OTHERS
	}
	
	private AssignType assignType = AssignType.UNKNOWN_ASSIGN;
	private String msg = "NIL";
	
	private static AssignInfo unknowAssign;
	public static AssignInfo getUnknowAssign() {
		if(unknowAssign == null) {
			unknowAssign = new AssignInfo();
			unknowAssign.assignType = AssignType.UNKNOWN_ASSIGN;
		}
		return unknowAssign;
	}
	
	private static AssignInfo fieldAssign;
	public static AssignInfo getFieldAssign() {
		if(fieldAssign == null) {
			fieldAssign = new AssignInfo();
			fieldAssign.assignType = AssignType.FIELD_ASSIGN;
		}
		return fieldAssign;
	}
	
	private static AssignInfo parameterAssign;
	public static AssignInfo getParameterAssign() {
		if(parameterAssign == null) {
			parameterAssign = new AssignInfo();
			parameterAssign.assignType = AssignType.PARAMETER_ASSIGN;
		}
		return parameterAssign;
	}
	
	private AssignInfo() {}
	
	
	public AssignInfo(ASTNode lastAssignExpr) {
		switch (lastAssignExpr.getNodeType()) {
		case ASTNode.INFIX_EXPRESSION:
			InfixExpression infix = (InfixExpression) lastAssignExpr;
			this.assignType = AssignType.INFIX;
			this.msg = infix.getOperator().toString();
			break;
		case ASTNode.PREFIX_EXPRESSION:
			PrefixExpression prefix = (PrefixExpression) lastAssignExpr;
			this.assignType = AssignType.PREFIX;
			this.msg = prefix.getOperator().toString();
			break;
		case ASTNode.POSTFIX_EXPRESSION:
			PostfixExpression postfix = (PostfixExpression) lastAssignExpr;
			this.assignType = AssignType.POSTFIX;
			this.msg = postfix.getOperator().toString();
			break;
		case ASTNode.ARRAY_ACCESS:
			this.assignType = AssignType.ARRAY_ACCESS;
			break;
		case ASTNode.ARRAY_CREATION:
			this.assignType = AssignType.ARRAY_CREATION;
			break;
		case ASTNode.CONSTRUCTOR_INVOCATION:
		case ASTNode.CLASS_INSTANCE_CREATION:
			this.assignType = AssignType.CLASS_INSTANCE_CREATION;
			break;
		case ASTNode.FIELD_ACCESS:
			this.assignType = AssignType.FIELD_ACCESS;
			break;
		case ASTNode.METHOD_INVOCATION:
			this.assignType = AssignType.METHOD_INVOCATION;
			MethodInvocation invo = (MethodInvocation) lastAssignExpr;
			this.msg = invo.getName().getIdentifier();
			break;
		case ASTNode.CAST_EXPRESSION:
			this.assignType = AssignType.CAST_EXPRESSION;
			break;
		case ASTNode.QUALIFIED_NAME:
			this.assignType = AssignType.QUALIFIED_NAME;
		case ASTNode.SIMPLE_NAME:
			this.assignType = AssignType.SIMPLE_NAME;
			this.msg = lastAssignExpr.toString();
			break;
		case ASTNode.NULL_LITERAL:
			this.assignType = AssignType.NULL_LITERAL;
			break;
		case ASTNode.BOOLEAN_LITERAL:
			this.assignType = AssignType.BOOLEAN_LITERAL;
			break;
		case ASTNode.STRING_LITERAL:
			this.assignType = AssignType.STRING_LITERAL;
			break;
		case ASTNode.TYPE_LITERAL:
			this.assignType = AssignType.TYPE_LITERAL;
			break;
		case ASTNode.CHARACTER_LITERAL:
			this.assignType = AssignType.CHARACTER_LITERAL;
			break;
		case ASTNode.NUMBER_LITERAL:
			this.assignType = AssignType.NUMBER_LITERAL;
			NumberLiteral num = (NumberLiteral) lastAssignExpr;
			this.msg = num.toString();
			break;
		default:
			this.assignType = AssignType.OTHERS;
		}
	}
	
	public AssignType getAssignType() {
		return assignType;
	}
	public String getMsg() {
		return msg;
	}
}
