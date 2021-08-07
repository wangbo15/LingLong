package cn.edu.pku.sei.plde.hanabi.trace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;

public class InstrumentVisitor extends ASTVisitor{
	protected int line = -1;
	protected CompilationUnit cu = null;
	protected boolean hit = false;

	public boolean getHit(){
		return this.hit;
	}
	
	public static Block getFatherBlock(Statement node) {
		Block fatherBlock = null;
		ASTNode parent = node.getParent();
		if(parent instanceof Block){
			fatherBlock = (Block) node.getParent();
		} else if(parent instanceof SwitchStatement) {
			AST ast = node.getAST();
			fatherBlock = ast.newBlock();
			
			Statement nodeCopy = (Statement) ASTNode.copySubtree(ast, node);
			
			fatherBlock.statements().add(nodeCopy);
			
			SwitchStatement sw = (SwitchStatement) parent;
			
			List<Statement> stmtBackup = new ArrayList<>(sw.<Statement>statements());
			
			sw.statements().clear();
			for(Statement stmt: stmtBackup) {
				if(stmt == node) {
					sw.statements().add(fatherBlock);
				}else {
					sw.statements().add(stmt);
				}
			}
			
			node = nodeCopy;
			return fatherBlock;
			
		} else if(parent instanceof IfStatement && node.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY){
			AST ast = node.getAST();
			fatherBlock = ast.newBlock();
			Statement nodeCopy = (Statement) ASTNode.copySubtree(ast, node);
			
			fatherBlock.statements().add(nodeCopy);
			
			((IfStatement) parent).setElseStatement(fatherBlock);
			node = nodeCopy;
			return fatherBlock;
		} else {
			try {
				throw new Exception(node.getParent().toString());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return fatherBlock;
	}
	
	@Override
	public final boolean visit(CompilationUnit node) {
		this.cu = node;
		return true;
	}
	
	@Override
	public final boolean visit(MethodDeclaration node) {
		if (outOfScope(node)) {
			return false;
		}
		return super.visit(node);
	}
	
	protected boolean outOfScope(ASTNode node){
		int startLine = cu.getLineNumber(node.getStartPosition());
		int endLine = cu.getLineNumber(node.getStartPosition() + node.getLength());
		return startLine > line || endLine < line;
	}
	
	protected boolean isStaticMethod(MethodDeclaration mtd){
		for(Object obj : mtd.modifiers()){
			if(obj instanceof Modifier) {
				Modifier m = (Modifier) obj;
				if(m.isStatic()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 */
	protected MethodInvocation genTracerInvocation(AST ast, String qualifier, String funcName, List<ASTNode> args){
		MethodInvocation newInvocation = ast.newMethodInvocation();
		if(qualifier != null && qualifier.length() > 0){
			Expression expr = strToQualifiedName(ast, qualifier);
			newInvocation.setExpression(expr);
		}
		
		newInvocation.setName(ast.newSimpleName(funcName));
		
		for(int i = 0; i < args.size(); i++){
			ASTNode arg = args.get(i);
			
			if(arg instanceof StringLiteral){
				newInvocation.arguments().add(arg);
			}else if(arg instanceof Expression){
				Expression copy = (Expression)ASTNode.copySubtree(ast, arg);
				newInvocation.arguments().add(copy);
			}else{
				throw new IllegalArgumentException();
			}
		}
		
		return newInvocation;
	}
	
	private Name strToQualifiedName(AST ast, String fullName){
		String[] names = fullName.split("\\.");
		if(names.length == 1){
			return ast.newSimpleName(names[0]);
		}
		Name currentQualifier = ast.newSimpleName(names[0]);
		for(int i = 1; i < names.length; i++){
			SimpleName aft = ast.newSimpleName(names[i]);
			currentQualifier = ast.newQualifiedName(currentQualifier, aft);
			
		}
		return currentQualifier;
	}
	
}
