package edu.pku.sei.conditon.auxiliary;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class IfBodyControlVisitor extends ASTVisitor{
	public boolean hasReturn = false;
	public boolean hasThrow = false;
	
	public boolean hasBreak = false;
	public boolean hasContinue = false;
	
	public String exceName = null;

	@Override
	public boolean visit(ReturnStatement node) {
		hasReturn = true;
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ThrowStatement node) {
		hasThrow = true;
		
		Expression expr = node.getExpression();
		
		expr.accept(new ASTVisitor(){

			@Override
			public boolean visit(ClassInstanceCreation node) {
				exceName = node.getType().toString();
				return super.visit(node);
			}
		});
		
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		hasBreak = true;
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		hasContinue = true;
		return super.visit(node);
	}

	public String getEncodeStr(){
		if(exceName != null){
			return exceName;
		}else if(hasReturn){
			return "RET";
		}else if(hasBreak){
			return "BRK";
		}else if(hasContinue){
			return "CTN";
		}else{
			return "DEF";
		}
	}	
}