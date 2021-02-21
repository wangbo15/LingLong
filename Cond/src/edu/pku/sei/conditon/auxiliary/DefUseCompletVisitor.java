package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.pku.sei.conditon.util.TypeUtil;

public class DefUseCompletVisitor extends ASTVisitor {
	
	private static final boolean USE_MUT = false;
	
	private Statement parent; 
	
	public DefUseCompletVisitor(Statement parent) {
		this.parent = parent;
	}
	
	public List<Expression> extandByDefUse(Expression fullCond) {
		List<Expression> result = new ArrayList<>();

		if(!isSimpleInfixComparing(fullCond)) {
			return result;
		}
		InfixExpression infix = (InfixExpression) fullCond;
		SimpleName left = (SimpleName) infix.getLeftOperand();
		
		ITypeBinding binding = left.resolveTypeBinding();
		if(binding == null) {
			return result;
		}
		String tp = binding.getName();
		tp = TypeUtil.removeGenericType(tp);
		if(!TypeUtil.isPrimitiveNumType(tp) && !TypeUtil.isJavaLangOrJavaUtilType(tp)) {
			return result;
		}
		
		Expression init = getInitializerOfTempVar(left);
		if(init == null || isCompleInitializer(init)) {
			return result;
		}
		
		try {
			AST ast = fullCond.getAST();
			InfixExpression copy = (InfixExpression) fullCond.copySubtree(ast, fullCond);
			Expression copiedInit = (Expression) init.copySubtree(ast, init);
			copy.setLeftOperand(copiedInit);
			
			System.out.println(fullCond + " ==>> " + copy);
			result.add(copy);
			
			if(USE_MUT) {
				InfixExpression mut = (InfixExpression) copy.copySubtree(ast, copy);
				if(infix.getOperator().toString().equals(">=")) {
					mut.setOperator(Operator.GREATER);
					result.add(mut);
				}
				if(infix.getOperator().toString().equals("<=")) {
					mut.setOperator(Operator.LESS);
					result.add(mut);
				}
			}
		}catch(Exception e) {

		}
		return result;
	}

	private boolean isCompleInitializer(Expression init) {
		if(init instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression) init;
			if(isSimpleInfixArith(infix)) {
				return false;
			}
		}else if(init instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) init;
			if(mi.arguments().size() > 1) {
				return true;
			}
					
			if(!mi.arguments().isEmpty() && mi.arguments().get(0) instanceof SimpleName == false && mi.arguments().get(0) instanceof StringLiteral == false) {
				return true;
			}
			if(mi.getExpression() == null) {
				return true;
			}
			if(mi.getExpression() instanceof ThisExpression) {
				return true;
			}
//			if(Character.isUpperCase((mi.getExpression().toString().charAt(0)))) {//must follow sth like SomeClass.method()
//				return false;
//			}
//			return true;
			return false;
		}
		return true;
	}

	private boolean isSimpleInfixComparing(Expression expr) {
		if(expr instanceof InfixExpression == false) {
			return false;
		}
		InfixExpression infix = (InfixExpression) expr;
		if(ASTLocator.isComparing(infix.getOperator()) == false) {
			return false;
		}
		if(! ASTLocator.isLiteralNode((infix.getRightOperand()))
			&& ! ASTLocator.maybeConstant(infix.getRightOperand().toString())) {
			return false;
		}
		
		if(infix.getLeftOperand() instanceof SimpleName == false || ASTLocator.maybeConstant(infix.getLeftOperand().toString())) {
			return false;
		}
		
		return true;
	}
	
	private boolean isSimpleInfixArith(InfixExpression expr) {
		InfixExpression infix = (InfixExpression) expr;
		if(ASTLocator.isArithOp(infix.getOperator()) == false) {
			return false;
		}
		if(infix.getLeftOperand() instanceof SimpleName == false) {
			return false;
		}
		if(infix.getRightOperand() instanceof SimpleName == false) {
			return false;
		}
		return true;
	}
	
	
	private Expression getInitializerOfTempVar(SimpleName name) {
		Block block = (Block) ASTLocator.getSpecifiedTypeFather(this.parent, Block.class);
		boolean reached = false;
		for(Object obj : block.statements()) {
			Statement stmt = (Statement) obj;
			if(stmt == this.parent) {
				reached = true;
				break;
			}
			
			if(stmt instanceof ExpressionStatement) {
				Expression expr = ((ExpressionStatement) stmt).getExpression();
				if(expr instanceof Assignment) {
					String assigned = ((Assignment) expr).getLeftHandSide().toString();
					//been assigned, not temp var
					if(assigned.equals(name.getIdentifier())) {
						return null;
					}
				}else {
					continue;
				}
			}else if(stmt instanceof VariableDeclarationStatement) {
				List<Object> frags = ((VariableDeclarationStatement) stmt).fragments();
				if(frags.size() != 1) {				
					//not simple var declaration
					continue;
				}
				VariableDeclarationFragment frag = (VariableDeclarationFragment) frags.get(0);
				if(frag.getName().getIdentifier().equals(name.getIdentifier())) {
					return frag.getInitializer();
				}
			}
		}//end for
		
		if(!reached) {
			return null;
		}
		return null;
	}
}
