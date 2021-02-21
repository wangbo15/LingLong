package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class VarCollectionVisitor extends ASTVisitor {
	private List<String> varList = new ArrayList<>();
	
	private List<SimpleName> simpleNameList = new ArrayList<>();
	
	public List<String> getVarList(){
		return varList;
	}
	
	public List<SimpleName> getSimpleNameList(){
		return simpleNameList;
	}
	
	public int getVarNum() {
		return varList.size();
	}
	
	@Override
	public boolean visit(SimpleName node) {
		if(Character.isUpperCase(node.getIdentifier().charAt(0)) || ASTLocator.maybeConstant(node.getIdentifier())){
			return super.visit(node);
		}
		
		if(ASTLocator.notVarLocation(node)){
			return super.visit(node);
		}
		simpleNameList.add(node);
		varList.add(node.getIdentifier());
		return super.visit(node);
	}

}
