package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;

public class ArgumentNormolizeVisitor extends ASTVisitor{
	
	public static final String COMPLEMENTAL_UNKNOWN_TP = "X_TYPE";
	
	public static boolean isSimpleDisjunctionOrConjuntion(Expression expr) {
		if(expr instanceof InfixExpression == false){
			return false;
		} 
		InfixExpression infix = (InfixExpression) expr;
		
		if(infix.getLeftOperand() instanceof SimpleName && infix.getRightOperand() instanceof SimpleName) {
			return false;
		}
		
		String op = infix.getOperator().toString();
		if(!(op.equals("||") || op.equals("&&"))) {
			return false;
		}
		
		class VarVisitor extends ASTVisitor{
			public boolean simple = true;
			@Override
			public boolean visit(SimpleName node) {
				if(ASTLocator.notVarLocation(node)) {
					simple = false;
				}
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodInvocation node) {
				simple = false;
				return super.visit(node);
			}
			@Override
			public boolean visit(InfixExpression node) {
				String op = node.getOperator().toString();
				if(op.equals("||") || op.equals("&&")) {
					simple = false;
				}
				return super.visit(node);
			}
			@Override
			public boolean visit(FieldAccess node) {
				simple = false;
				return super.visit(node);
			}
		};
		
		VarVisitor leftVisitor = new VarVisitor();
		infix.getLeftOperand().accept(leftVisitor);
		VarVisitor rightVisitor = new VarVisitor();
		infix.getRightOperand().accept(rightVisitor);
		
		boolean result = leftVisitor.simple && rightVisitor.simple;
		return result;
	}
	
	public static boolean isComplexExprArgu(Expression expr) {
		
		class MtdVisitor extends ASTVisitor{
			public boolean nonSimpleArg; 
			@Override
			public boolean visit(MethodInvocation node) {
				for(Object arg : node.arguments()) {
					if(ASTLocator.isLiteralNode((ASTNode) arg)) {
						continue;
					}
					
					if(arg instanceof ThisExpression) {
						if(((ThisExpression) arg).getQualifier() == null) {
							continue;
						}
					}
					
					if(arg instanceof SimpleName == false) {
						this.nonSimpleArg = true;
					}
				}
				
				return super.visit(node);
			}
			
		};
		
		MtdVisitor visitor = new MtdVisitor();
		expr.accept(visitor);
		return visitor.nonSimpleArg;
	}
	
	private String[] nameSeq = {"p", "q", "a", "b", "x", "y", "u", "v"};
	
	private int globalCounter = 0;
	
	private Map<String, String> nameToTypeMap = new HashMap<>();
	
	public String getType(String var) {
		if(nameToTypeMap.containsKey(var)) {
			return nameToTypeMap.get(var);
		}
		return null;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		AST ast = node.getAST();
		
		List<Expression> newArgs = new ArrayList<>();
		for(int i = 0; i < node.arguments().size(); i++) {
			Expression e = (Expression) node.arguments().get(i);
			if(e instanceof SimpleName) {
				newArgs.add(e);
				continue;
			}
			String name = nameSeq[globalCounter++] + "_n";

			String type = analysisType(e);
			nameToTypeMap.put(name, type);

			SimpleName sim = ast.newSimpleName(name);
			newArgs.add(sim);
		}
		
		node.arguments().clear();
		node.arguments().addAll(newArgs);
		return super.visit(node);
	}

	private String analysisType(Expression expr) {
		if(expr instanceof InfixExpression) {
			return getTypeOfInfix((InfixExpression) expr);
		}
		if(expr instanceof MethodInvocation) {
			return getTypeOfMI((MethodInvocation) expr);
		}
		
		return COMPLEMENTAL_UNKNOWN_TP;
	}
	
	
	private String getTypeOfMI(MethodInvocation mi) {
		ITypeBinding bind = mi.getName().resolveTypeBinding();
		if(bind != null) {
			return bind.getName();
		}
		
		return COMPLEMENTAL_UNKNOWN_TP;
	}

	private String getTypeOfInfix(InfixExpression infix) {
		if(ASTLocator.isArithOp(infix.getOperator())) {
			ITypeBinding lb = infix.getLeftOperand().resolveTypeBinding();
			ITypeBinding rb = infix.getRightOperand().resolveTypeBinding();
			String leftTp = COMPLEMENTAL_UNKNOWN_TP;
			String rightTp = COMPLEMENTAL_UNKNOWN_TP;
			if(lb != null) {
				leftTp = lb.getName();
			}
			if(rb != null) {
				rightTp = rb.getName();
			}
			if(leftTp.equals("double") || rightTp.equals("double")) {
				return "double";
			}
			if(leftTp.equals("long") || rightTp.equals("long")) {
				return "long";
			}
			if(leftTp.equals("int") || rightTp.equals("int")) {
				return "int";
			}
			if(leftTp.equals("short") || rightTp.equals("short")) {
				return "short";
			}
		}
		return COMPLEMENTAL_UNKNOWN_TP;
	}
}
