package edu.pku.sei.conditon.auxiliary;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;


public class PredicateFeatureVisitor extends ASTVisitor {
	private Expression rootExpr;
	private PredicateFeature collectedFeature;
	
	public PredicateFeature getCollectedFeature(){
		assert collectedFeature != null;
		return collectedFeature;
	}
	
	public PredicateFeatureVisitor(String clsName, Expression rootExpr){
		this.rootExpr = rootExpr;
		String rootTp = AbstractDeduVisitor.getPredType(rootExpr);
		this.collectedFeature = new PredicateFeature(clsName, rootExpr.toString(), rootTp);
	}

	@Override
	public boolean visit(InfixExpression node) {
		String op = node.getOperator().toString();
		if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%")){
			if(collectedFeature.getAriop().equals(AbstractDeduVisitor.NONE)){
				collectedFeature.setAriop(op);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		collectedFeature.setHasInstanceof(true);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(collectedFeature.getFirstMtd().equals(AbstractDeduVisitor.NONE)){
			collectedFeature.setFirstMtd(node.getName().toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		collectedFeature.setHasNull(true);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		/*only collect first two numbers*/
		if(collectedFeature.getFirstNum().equals(AbstractDeduVisitor.NONE)){
			collectedFeature.setFirstNum(node.toString());
		}else if(collectedFeature.getSecondNum().equals(AbstractDeduVisitor.NONE)){
			collectedFeature.setSecondNum(node.toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		int hight = computeHight(node);
		if(hight > collectedFeature.getHight()){
			collectedFeature.setHight(hight);
		}
		return super.visit(node);
	}
	
	private int computeHight(ASTNode node){
		int hight = 0;
		while(node != rootExpr){
			node = node.getParent();
			hight++;
		}
		return hight;
	}
}
