package edu.pku.sei.conditon.auxiliary;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public class ClassInstanceCreationVisitor extends ASTVisitor{
	
	public boolean hit = false;

	@Override
	public boolean visit(ClassInstanceCreation node) {
		hit = true;
		return super.visit(node);
	}
}