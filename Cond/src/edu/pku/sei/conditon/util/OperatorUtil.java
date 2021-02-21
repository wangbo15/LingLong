package edu.pku.sei.conditon.util;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class OperatorUtil {
	public static boolean isComparing(String op) {
		if(op == null) {
			return false;
		}
		switch (op) {
		case ">":
		case ">=":
		case "<":
		case "<=":
		case "==":
		case "!=":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isBoolOp(String op) {
		if(op == null) {
			return false;
		}
		switch (op) {
		case "&&":
		case "||":
		case "!":
			return true;
		default:
			return false;
		}
	}
	
	
	private static final String INSTANCEOF_OP = "INSTANCEOF";
	private static final String POSITIVE_OP = "POS";
	private static final String NEGTIVE_OP = "NEG";
	private static final String CAST_OP = "CAST";
	
	/**
	 * http://www.cs.bilkent.edu.tr/~guvenir/courses/CS101/op_precedence.html
	 * @param op
	 * @return
	 */
	public static int getPrecedence(String op) {
		if(op == null) {
			return 0;
		}
		switch (op) {
		case "++":
		case "--":
			return 14;
		case "!":
		case "~":
		case POSITIVE_OP: // '+10'
		case NEGTIVE_OP:	// '-10'
		case CAST_OP:	// (int) a
			return 13;
		case "*":
		case "/":
		case "%":
			return 12;
		case "+":
		case "-":
			return 11;
		case "<<":
		case ">>":
		case ">>>":
			return 10;
		case "<":
		case "<=":
		case ">":
		case ">=":
		case INSTANCEOF_OP:
			return 9;
		case "==":
		case "!=":
			return 8;
		case "&":
			return 7;
		case "^":
			return 6;
		case "|":
			return 5;
		case "&&":
			return 4;
		case "||":
			return 3;
		//level 2 is ternary conditional expression "? :"
		case "=":
		case "+=":
		case "-=":
		case "*=":
		case "/=":
		case "%=":
			return 1;
		default:
			return 0;
		}
	}
	
	
	public static int getPrecedence(Expression astNode) {
		if(astNode == null) {
			return 0;
		}
		
		if(astNode instanceof InfixExpression) {
			InfixExpression expr = (InfixExpression) astNode;
			return getPrecedence(expr.getOperator().toString());
		}
		if(astNode instanceof InstanceofExpression) {
			return getPrecedence(INSTANCEOF_OP);
		}
		if(astNode instanceof CastExpression) {
			return getPrecedence(CAST_OP);
		}
		if(astNode instanceof PrefixExpression) {
			PrefixExpression expr = (PrefixExpression) astNode;
			String op = expr.getOperator().toString();
			if(op.equals("+")) {
				return getPrecedence(POSITIVE_OP); 
			}else if(op.equals("-")) {
				return getPrecedence(NEGTIVE_OP); 
			}else {
				return getPrecedence(op);
			}
		}
		if(astNode instanceof PostfixExpression) {
			PostfixExpression expr = (PostfixExpression) astNode;
			String op = expr.getOperator().toString();
			return getPrecedence(op);
		}
		if(astNode instanceof Assignment){
			Assignment expr = (Assignment) astNode;
			String op = expr.getOperator().toString();
			return getPrecedence(op);
		}
		return 0;
	}
}
