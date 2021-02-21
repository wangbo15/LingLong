package edu.pku.sei.conditon.auxiliary;

import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class PlainSyntacticVisitor extends ASTVisitor {
	TreeMap<Integer, String> synMap;
	
	public PlainSyntacticVisitor(TreeMap<Integer, String> positionToSyntacticMap) {
		this.synMap = positionToSyntacticMap;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		synMap.put(node.getStartPosition(), "for");
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		synMap.put(node.getStartPosition(), "for");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		synMap.put(node.getStartPosition(), node.getName().getIdentifier() + "()");
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		synMap.put(node.getStartPosition(), "case");
		return super.visit(node);
	}


	@Override
	public boolean visit(SwitchStatement node) {
		synMap.put(node.getStartPosition(), "switch");
		return super.visit(node);
	}


	@Override
	public boolean visit(TypeDeclarationStatement node) {
		synMap.put(node.getStartPosition(), "class");
		return super.visit(node);
	}



	@Override
	public boolean visit(EnumDeclaration node) {
		synMap.put(node.getStartPosition(), "enum");
		return super.visit(node);
	}
	

	@Override
	public boolean visit(IfStatement node) {
		synMap.put(node.getStartPosition(), "if");
		return super.visit(node);
	}


	@Override
	public boolean visit(TryStatement node) {
		synMap.put(node.getStartPosition(), "try");
		return super.visit(node);
	}


	@Override
	public boolean visit(CatchClause node) {
		synMap.put(node.getStartPosition(), "catch");
		return super.visit(node);
	}

	

	@Override
	public boolean visit(BreakStatement node) {
		synMap.put(node.getStartPosition(), "break");
		return super.visit(node);
	}


	@Override
	public boolean visit(ContinueStatement node) {
		synMap.put(node.getStartPosition(), "break");
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		synMap.put(node.getStartPosition(), "do");
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		synMap.put(node.getStartPosition(), "return");
		return super.visit(node);
	}


	@Override
	public boolean visit(ThrowStatement node) {
		synMap.put(node.getStartPosition(), "throw");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if(node.isInterface()){
			synMap.put(node.getStartPosition(), "interface");
		}else{
			synMap.put(node.getStartPosition(), "class");
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		synMap.put(node.getStartPosition(), "MTDDECL:" + node.getName().getFullyQualifiedName());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		synMap.put(node.getStartPosition(), "while");
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		synMap.put(node.getStartPosition(), "new");
		return super.visit(node);
	}
}
