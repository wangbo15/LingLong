package cn.edu.pku.sei.plde.hanabi.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cn.edu.pku.sei.plde.hanabi.utils.JdkUtil;
import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.TypeUtil;

public class ExceptionValueTraceVisitor extends InstrumentVisitor{	
	
	private String exceptionName = "";
	
	public ExceptionValueTraceVisitor(int line, String exceptionName) {
		this.line = line;
		if(exceptionName != null) {
			this.exceptionName = exceptionName;
		}
	}
	
	private void instrument(Statement node) {
		if (outOfScope(node)) {
			return;
		}
		
		if(this.hit) {
			return;
		}
		
		this.hit = true;
		
		Block fatherBlock = getFatherBlock(node);
		if(fatherBlock == null) {
			return;
		}
		//in switch case branches
		if(node.getParent() == null) {
			int size = fatherBlock.statements().size();
			node = (Statement) fatherBlock.statements().get(size - 1);
		}
		
		AST ast = node.getAST();//get the same ast of cu;
		
		String traceClsName = "cn.edu.pku.sei.plde.hanabi.trace.runtime.RuntimeValues";
		String traceMtd = "trace";
		
		StringLiteral msg;
		Statement newStatement;

		List<Statement> intruStmts = new ArrayList<>();

		MethodDeclaration fatherMtd = (MethodDeclaration) ASTLocator.getSpecifiedTypeFather(node, MethodDeclaration.class);
		if(!isStaticMethod(fatherMtd)){
			msg = ast.newStringLiteral();
			msg.setLiteralValue("this");
			Expression thisExpr = ast.newThisExpression();
			MethodInvocation thisTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) thisExpr));
			newStatement = ast.newExpressionStatement(thisTrace);
			intruStmts.add(newStatement);
		}
		
		
		boolean insertBefore = false;
		String skipName = "";
		if(node instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement decl = (VariableDeclarationStatement) node;
			if(exceptionName.endsWith("NullPointerException") && decl.fragments().size() == 1) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) decl.fragments().get(0);
				if(frag.getInitializer() instanceof MethodInvocation) {
					insertBefore = true;
					skipName = frag.getName().getIdentifier();
				}
			}
		} else {
			insertBefore = needInsertBefore(node);
		}
		
		
		List<VariableInfo> localVarInfoList = new ArrayList<>();
		ASTLocator.getLocalVariables(node, localVarInfoList);
		ASTLocator.getParams(fatherMtd, localVarInfoList);
		
		for(VariableInfo local : localVarInfoList){
			String tp = TypeUtil.removeGenericType(local.getType());
			if(tp.equals("Class")) {
				continue;
			}
			if(local.getNameLiteral().equals(skipName)) {
				continue;
			}
			
			SimpleName var = ast.newSimpleName(local.getNameLiteral());
			msg = ast.newStringLiteral();
			msg.setLiteralValue(var.toString());
			
			MethodInvocation trace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) var));
			newStatement = ast.newExpressionStatement(trace);
			intruStmts.add(newStatement);
		}
		
		String dumpMtd = "dump";
		MethodInvocation dump = genTracerInvocation(ast, traceClsName, dumpMtd, new ArrayList());
		newStatement = ast.newExpressionStatement(dump);
		intruStmts.add(newStatement);
		
		Block newFatherBlock = ast.newBlock();
		
		
		for (Object object : fatherBlock.statements()) {
			if(insertBefore == true && object == node){
				newFatherBlock.statements().addAll(intruStmts);
			}
			
			ASTNode astNode = (ASTNode) object;
			Statement copy = (Statement)ASTNode.copySubtree(newFatherBlock.getAST(), astNode);
			newFatherBlock.statements().add(copy);
			
			if(insertBefore == false && object == node){
				newFatherBlock.statements().addAll(intruStmts);
			}
		}
		
		if(fatherBlock.getParent() instanceof MethodDeclaration){
			((MethodDeclaration) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof ForStatement){
			((ForStatement) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof WhileStatement){
			((WhileStatement) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof DoStatement){
			((DoStatement) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof EnhancedForStatement){
			((EnhancedForStatement) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof CatchClause) {
			((CatchClause) fatherBlock.getParent()).setBody(newFatherBlock);
		}else if(fatherBlock.getParent() instanceof IfStatement){
			
			if(fatherBlock.getLocationInParent() == IfStatement.THEN_STATEMENT_PROPERTY){
				((IfStatement) fatherBlock.getParent()).setThenStatement(newFatherBlock);

			}else if(fatherBlock.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY){
				((IfStatement) fatherBlock.getParent()).setElseStatement(newFatherBlock);

			}else{
				throw new IllegalArgumentException("UNSUPPORT ASTNODE LOCATION: " + fatherBlock.getLocationInParent().getNodeClass().getName());
			}
		}else if(fatherBlock.getParent() instanceof SwitchStatement){
			SwitchStatement sw = (SwitchStatement) fatherBlock.getParent();
			
			List<Statement> stmtBackup = new ArrayList<>(sw.<Statement>statements());
			
			sw.statements().clear();
			for(Statement stmt: stmtBackup) {
				if(stmt == fatherBlock) {
					sw.statements().add(newFatherBlock);
				}else {
					sw.statements().add(stmt);
				}
			}
		
		}else{
			throw new IllegalArgumentException("UNSUPPORT ASTNODE TYPE: " + fatherBlock.getParent().getClass().getName());
		}
	}
	
	@Override
	public boolean visit(BreakStatement node) {
		instrument(node);
	    
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		instrument(node);
		
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		instrument(node);

		return super.visit(node);
	}
	
	
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		instrument(node);

		return super.visit(node);
	}
	
	
	/**
	 * @param node
	 * @return ture is the node is instance of some control flow statements, or some runtime exceptions failures
	 */
	private boolean needInsertBefore(Statement node) {
		return node instanceof ReturnStatement 
				|| node instanceof BreakStatement 
				|| node instanceof ContinueStatement 
				|| JdkUtil.isRunTimeExeception(exceptionName);
	}
	
}
