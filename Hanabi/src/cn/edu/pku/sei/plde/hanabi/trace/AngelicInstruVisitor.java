package cn.edu.pku.sei.plde.hanabi.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
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



public class AngelicInstruVisitor extends InstrumentVisitor{
	
	private String exceptionName;
	private boolean remainOriExpr = true;
	
	public AngelicInstruVisitor(int line, String exceptionName){
		this.line = line;
		this.exceptionName = exceptionName;
		this.remainOriExpr = (JdkUtil.isRunTimeExeception(exceptionName) == false);
	}

	@Override
	public boolean visit(IfStatement node) {
		if (outOfScope(node)) {
			return false;
		}
		if(hit) {
			return false;
		}
		
		hit = true;
		
		if(hasHitChild(node)){
			hit = false;
			return super.visit(node);
		}
		
		Block fatherBlock = getFatherBlock(node);
		if(fatherBlock == null) {
			return false;
		}
		//in switch case branches
		if(node.getParent() == null) {
			int size = fatherBlock.statements().size();
			node = (IfStatement) fatherBlock.statements().get(size - 1);
		}
		
		
		AST ast = node.getAST();//get the same ast of cu;
		
				
		Expression condition = node.getExpression();

		List<Statement> intruStmts = new ArrayList<>();
		VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
		SimpleName runtimeAngelicValue = ast.newSimpleName("runtimeAngelicValue");
		frag.setName(runtimeAngelicValue);
		
		List<ASTNode> instrutedArgs;
		if(this.remainOriExpr) {
			instrutedArgs = Arrays.asList((ASTNode) condition);
		}else {
			instrutedArgs = new ArrayList<>();
		}
		
		MethodInvocation initializer = genTracerInvocation(ast, 
				"cn.edu.pku.sei.plde.hanabi.trace.runtime.AngelicExecution", 
				"getAngelicValue",
				instrutedArgs);
		
		frag.setInitializer(initializer);
		
		VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(frag);
		decl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
		
		intruStmts.add(decl);
		
		String traceClsName = "cn.edu.pku.sei.plde.hanabi.trace.runtime.RuntimeValues";
		String traceMtd = "trace";
		
		StringLiteral msg;
		Statement newStatement;
		if(this.remainOriExpr) {
			msg = ast.newStringLiteral();
			msg.setLiteralValue(condition.toString());
			MethodInvocation condTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) condition));
			newStatement = ast.newExpressionStatement(condTrace);
			intruStmts.add(newStatement);
		}else {
			//if there is cast exception, remove casting and record the remain part
			if("java.lang.ClassCastException".equals(exceptionName) && condition instanceof CastExpression){
				Expression casted = ((CastExpression) condition).getExpression();
				msg = ast.newStringLiteral();
				msg.setLiteralValue(casted.toString());
				MethodInvocation condTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg,(ASTNode) casted));
				newStatement = ast.newExpressionStatement(condTrace);
				intruStmts.add(newStatement);
			}
		}
		
		Set<String> collected = new HashSet<>();
		if(condition instanceof InfixExpression){
			Expression left = ((InfixExpression) condition).getLeftOperand();
			if(!(left instanceof NullLiteral || left instanceof NumberLiteral || left instanceof StringLiteral)){
				msg = ast.newStringLiteral();
				msg.setLiteralValue(left.toString());
				MethodInvocation thisTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) left));
				newStatement = ast.newExpressionStatement(thisTrace);
				intruStmts.add(newStatement);
				
				collected.add(left.toString());
			}
			
			Expression right = ((InfixExpression) condition).getRightOperand();
			if(!(right instanceof NullLiteral || right instanceof NumberLiteral || right instanceof StringLiteral)){
				msg = ast.newStringLiteral();
				msg.setLiteralValue(right.toString());
				MethodInvocation thisTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) right));
				newStatement = ast.newExpressionStatement(thisTrace);
				intruStmts.add(newStatement);
				
				collected.add(right.toString());
			}
		}
		
		//Non-static methods should add this
		MethodDeclaration fatherMtd = (MethodDeclaration) ASTLocator.getSpecifiedTypeFather(node, MethodDeclaration.class);
		if(!isStaticMethod(fatherMtd)){
			msg = ast.newStringLiteral();
			msg.setLiteralValue("this");
			Expression thisExpr = ast.newThisExpression();
			MethodInvocation thisTrace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) thisExpr));
			newStatement = ast.newExpressionStatement(thisTrace);
			intruStmts.add(newStatement);
		}

		List<VariableInfo> localVarInfoList = new ArrayList<>();
		ASTLocator.getLocalVariables(node, localVarInfoList);
		ASTLocator.getParams(fatherMtd, localVarInfoList);
		for(VariableInfo local : localVarInfoList){
			if(collected.contains(local.getNameLiteral())){
				continue;
			}
			
			if(local.getDef() != null &&
					local.getDef().getParent() != null && 
					local.getDef().getParent() instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment declPoint = (VariableDeclarationFragment) local.getDef().getParent();
				if(declPoint.getInitializer() == null) {
					continue;
				}
			}
						
			SimpleName var = ast.newSimpleName(local.getNameLiteral());
			msg = ast.newStringLiteral();
			msg.setLiteralValue(var.toString());
			
			MethodInvocation trace = genTracerInvocation(ast, traceClsName, traceMtd, Arrays.asList((ASTNode) msg, (ASTNode) var));
			newStatement = ast.newExpressionStatement(trace);
			intruStmts.add(newStatement);
			
			collected.add(local.getNameLiteral());
		}
		
		String dumpMtd = "dump";
		MethodInvocation dump = genTracerInvocation(ast, traceClsName, dumpMtd, new ArrayList());
		newStatement = ast.newExpressionStatement(dump);
		intruStmts.add(newStatement);
		
		
		Block newFatherBlock = ast.newBlock();
		for (Object object : fatherBlock.statements()) {
			if(object == node){
				newFatherBlock.statements().addAll(intruStmts);
				IfStatement ifStmt = (IfStatement) object;
				ifStmt.setExpression((Expression) ASTNode.copySubtree(ast, runtimeAngelicValue));
			}
			
			ASTNode astNode = (ASTNode) object;
			Statement copy = (Statement)ASTNode.copySubtree(newFatherBlock.getAST(), astNode);
			newFatherBlock.statements().add(copy);
		}
		
					
//		node.setThenStatement((Statement) ASTNode.copySubtree(node.getAST(), newFatherBlock));
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
		
		return super.visit(node);
	}

	private boolean hasHitChild(final IfStatement node) {
		class HitVisitor extends ASTVisitor{
			public boolean hitCld = false;
			@Override
			public boolean visit(IfStatement inner) {
				if(node == inner){
					return super.visit(inner);
				}
				if(!outOfScope(inner)){
					hitCld = true;
					return false;
				}
				return super.visit(inner);
			}
		};
		HitVisitor visitor = new HitVisitor();
		node.accept(visitor);
		
		return visitor.hitCld;
	}
	
	
}
