package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;


public class CondDivisionVisitor extends ASTVisitor{
	
	private List<Expression> dividedExprList = new ArrayList<Expression>();
	
	private Set<String> collectedExpr = new HashSet<>();
	
	private int divideNum = 0;
	private int notNum = 0;
	
	private ASTNode stopPosition = null;
	
	/**
	 * To deal with invocations of <p>assertTure</p>
	 * @param stopPosition
	 */
	public CondDivisionVisitor(Expression fullCond, ASTNode stopPosition){
		this.stopPosition = stopPosition;
	}
	
	/**
	 * For normal cases
	 */
	public CondDivisionVisitor(){
		//first add the full origin condition
		
//		if(DeduMain.processingDefects4J) {
//			extandByDefUse(fullCond);
//		}
	}
	
	private void extandByDefUse(Expression fullCond) {
		if(isSimpleInfixComparing(fullCond)) {
			return;
		}
		InfixExpression infix = (InfixExpression) fullCond;
		SimpleName left = (SimpleName) infix.getLeftOperand();
		
		
		AST ast = fullCond.getAST();
		Expression copy = (Expression) fullCond.copySubtree(ast, fullCond);
		
		
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

	
	public List<Expression> getDividedExprList(){
		return dividedExprList;
	}
	
	private Expression getFatherSlpExpr(Expression expr){

		ASTNode candidate = expr;
		while (true) {
			ASTNode p = candidate.getParent();
			
			if(stopPosition != null){
				if(candidate.getStartPosition() == stopPosition.getStartPosition() && candidate.getLength() == stopPosition.getLength()){
					break;
				}
			}else{
				if(Statement.class.isAssignableFrom(p.getClass())){
					break;
				}
			}
			
			if(p instanceof InfixExpression){
				InfixExpression infix = (InfixExpression) p;
				if(infix.getOperator().toString().equals("&&") || infix.getOperator().toString().equals("||")){
					return (Expression) candidate;
				}
				
			}else if(p instanceof PrefixExpression){
				PrefixExpression prefix = (PrefixExpression) p;
				if(prefix.getOperator().toString().equals("!")){
					return (Expression) candidate;
				}
			}else if(p instanceof ConditionalExpression){
				Expression condExpr = ((ConditionalExpression) p).getExpression();
				if(condExpr instanceof ParenthesizedExpression){
					condExpr = ((ParenthesizedExpression) condExpr).getExpression();
				}
				return condExpr;
			}
			
			candidate = p;
		}
		
		return (Expression) candidate;
	}
	
	private void putInList(Expression expr) {
		String str = expr.toString();
		if(collectedExpr.contains(str)) {
			return;
		}
		dividedExprList.add(expr);			
		collectedExpr.add(str);
	}
	
	@Override
	public boolean visit(ThisExpression node) {// from leave 
		Expression slpFather = getFatherSlpExpr(node);
		putInList(slpFather);			
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SimpleName node) {// from leave
		IBinding binding = node.resolveBinding();
		
		if(binding instanceof IVariableBinding == false && Character.isUpperCase(node.getIdentifier().charAt(0))){
			return super.visit(node);
		}
		
		Expression slpFather = getFatherSlpExpr(node);
		putInList(slpFather);	
		return super.visit(node);
	}
	
	@Override
	public boolean visit(InfixExpression node) {// from root
		Operator op = node.getOperator();
		if(op.toString().equals("&&") || op.toString().equals("||")){
			divideNum ++;
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {// from root
		if(node.getOperator().toString().equals("!")){
			notNum++;
		}
		return super.visit(node);
	}
	
}
