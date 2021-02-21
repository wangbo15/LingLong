package edu.pku.sei.conditon.auxiliary;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class FieldCollectorVisitor extends ASTVisitor {
	public Map<String, SimpleName> fields;
	
	public FieldCollectorVisitor(Map<String, SimpleName> fields){
		this.fields = fields;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		for(Object obj : node.fragments()){
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) obj;
			fields.put(fragment.getName().getIdentifier(), fragment.getName());
		}
		
		return super.visit(node);
	}
	
}
