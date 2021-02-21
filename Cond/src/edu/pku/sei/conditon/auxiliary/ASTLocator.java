package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.simple.expr.TypeFilter;
import edu.pku.sei.conditon.util.Pair;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Mar 28, 2017
 */
public class ASTLocator {
	
	public static boolean notVarLocation(SimpleName node) {
		return node.getLocationInParent() == MethodInvocation.NAME_PROPERTY
				|| node.getLocationInParent() == FieldAccess.NAME_PROPERTY 
				|| node.getLocationInParent() ==  SuperFieldAccess.NAME_PROPERTY
				|| node.getLocationInParent() == QualifiedName.NAME_PROPERTY
				//|| node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY
				|| node.getLocationInParent() == SuperMethodInvocation.NAME_PROPERTY
				|| node.getLocationInParent() == SimpleType.NAME_PROPERTY;
	}
	
	public static String getEncolsingMethod(ASTNode node){
		String enclosingMethodName = null;
		ASTNode parent = node.getParent();
		while(parent != null){
			if(parent instanceof MethodDeclaration){
				enclosingMethodName = ((MethodDeclaration) parent).getName().getIdentifier();
				break;
			}
			parent = parent.getParent();
		}
		return enclosingMethodName;
	}
	
	public static boolean maybeConstant(String name){
		if(name == null || name.length() == 0) {
			return false;
		}
		//the header must be capital
		if(! Character.isUpperCase(name.charAt(0))) {
			return false;
		}
		
		for(int i = 1; i < name.length(); i++){
			char ch = name.charAt(i);
			if(! (Character.isUpperCase(ch) || ch == '_' || Character.isDigit(ch))){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isFieldLike(SimpleName node){
		return (node.getLocationInParent() == FieldAccess.NAME_PROPERTY 
				|| node.getLocationInParent() == QualifiedName.NAME_PROPERTY);
	}
	
	public static IfStatement getFatherIfStmt(Name node){
		if(node.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY){
			return (IfStatement) node.getParent();
		}
		
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY){
				return (IfStatement) father.getParent();
			}
			if(Statement.class.isAssignableFrom(father.getClass())){
				return null;
			}
			curNode = father;
		}
		
		return null;
	}
	
	public static IfStatement getFatherIfStmt(Expression node){
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY){
				return (IfStatement) father.getParent();
			}
			if(Statement.class.isAssignableFrom(father.getClass())){
				return null;
			}
			curNode = father;
		}
		
		return null;
	}
	
	
	public static ForStatement getFatherForStmt(Name node){
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father.getLocationInParent() == ForStatement.EXPRESSION_PROPERTY){
				return (ForStatement) father.getParent();
			}
//			if(Statement.class.isAssignableFrom(father.getClass())){
//				return null;
//			}
			curNode = father;
		}
		return null;
	}
	
	public static WhileStatement getFatherWhileStmt(Name node){
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father.getLocationInParent() == WhileStatement.EXPRESSION_PROPERTY){
				return (WhileStatement) father.getParent();
			}
			if(Statement.class.isAssignableFrom(father.getClass())){
				return null;
			}
			curNode = father;
		}
		return null;
	}
	
	public static DoStatement getFatherDoStmt(Name node){
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father.getLocationInParent() == DoStatement.EXPRESSION_PROPERTY){
				return (DoStatement) father.getParent();
			}
			if(Statement.class.isAssignableFrom(father.getClass())){
				return null;
			}
			curNode = father;
		}
		return null;
	}
	
	
	public static ASTNode getSpecifiedTypeFather(ASTNode node, Class fatherClz){
		if(node.getClass().equals(fatherClz)){
			return node;
		}
		ASTNode father;
		while((father = node.getParent()) != null){
			if(fatherClz.isAssignableFrom(father.getClass())){ 	//father.getClass().isAssignableFrom(fatherClz) ||
				return father;
			}
			node = father;
		}
		
		return null;
	}
	
	
	/**
	 * @param node
	 * @param result: a pair of <simpleName and its modifiers>
	 */
	public static void getAvaliableVariableDeclStmts(ASTNode node, List<Pair<SimpleName, Integer>> result, Map<SimpleName, String> nameToType){
		
		assert node != null;
		
		if(node instanceof MethodDeclaration || node instanceof Initializer){
			return;
		}
		
		ASTNode parent = node.getParent();
		
		if(node instanceof ForStatement){
			ForStatement forStmt = (ForStatement) node;
			List initExprList = forStmt.initializers();
			for(Iterator it = initExprList.iterator(); it.hasNext(); ){
				Expression expr = (Expression) it.next();
				if(expr instanceof VariableDeclarationExpression){
					VariableDeclarationExpression decl = (VariableDeclarationExpression) expr;
					int flag = decl.getModifiers();
					String tp = decl.getType().toString();
					
					for(Iterator fragIt = decl.fragments().iterator(); fragIt.hasNext();){
						VariableDeclarationFragment frag = (VariableDeclarationFragment) fragIt.next();
						Pair<SimpleName, Integer> pair = new Pair(frag.getName(), flag);
						result.add(pair);
						
						nameToType.put(frag.getName(), tp);
					}			

					
				}
			}
		}
				
		if(node instanceof EnhancedForStatement){
			EnhancedForStatement enForStmt = (EnhancedForStatement) node;
			SingleVariableDeclaration decl = enForStmt.getParameter();
			int flag = decl.getModifiers();
			Pair<SimpleName, Integer> pair = new Pair(decl.getName(), flag);
			result.add(pair);
			
			String tp = decl.getType().toString();
			nameToType.put(decl.getName(), tp);
		}
		
		
		if(node instanceof CatchClause){
			CatchClause catchClause = (CatchClause) node;
			SingleVariableDeclaration decl = catchClause.getException();
			int flag = decl.getModifiers();
			Pair<SimpleName, Integer> pair = new Pair(decl.getName(), flag);
			result.add(pair);
			
			String tp = decl.getType().toString();
			nameToType.put(decl.getName(), tp);
		}
		
		assert parent != null;
		
		StructuralPropertyDescriptor location  = node.getLocationInParent();
		
		assert location != null;
		
		//1. the stme itself is decl
		if(node instanceof VariableDeclarationStatement){
			VariableDeclarationStatement decl = (VariableDeclarationStatement) node;
			int flag = decl.getModifiers();
			String tp = decl.getType().toString();

			for(Iterator it = decl.fragments().iterator(); it.hasNext();){
				VariableDeclarationFragment frag = (VariableDeclarationFragment) it.next();
				Pair<SimpleName, Integer> pair = new Pair(frag.getName(), flag);
				result.add(pair);
				
				nameToType.put(frag.getName(), tp);
			}
		}
		
		//2. parse the brother statements of its parent, only for Decl before its location
		if(location.isChildListProperty()){
			List list = (List) parent.getStructuralProperty(location);
			int index = list.indexOf(node);
			for(int i = 0; i < index; i++){
				if(list.get(i) instanceof VariableDeclarationStatement){
					VariableDeclarationStatement decl = (VariableDeclarationStatement) list.get(i);
					
					String tp = decl.getType().toString();
					int flag = decl.getModifiers();
					for(Iterator it = decl.fragments().iterator(); it.hasNext();){
						VariableDeclarationFragment frag = (VariableDeclarationFragment) it.next();
						Pair<SimpleName, Integer> pair = new Pair(frag.getName(), flag);
						result.add(pair);
						
						nameToType.put(frag.getName(), tp);
					}
				}
			}
		}
		//3. parse its parent in the same way
		getAvaliableVariableDeclStmts(parent, result, nameToType);		
	}
	
	public static void getLocalVariables(Statement stmt, List<VariableInfo> varDeclList){
		List<Pair<SimpleName, Integer>> accessableLocals = new ArrayList<>();
		Map<SimpleName, String> nameToType = new HashMap<>();//in case of bindings do not work
		
		ASTLocator.getAvaliableVariableDeclStmts(stmt, accessableLocals, nameToType);
		for(Pair<SimpleName, Integer> pair : accessableLocals){
			SimpleName sm = pair.getFirst();
			int flag = pair.getSecond();
			
			boolean isFinal = Modifier.isFinal(flag);
//			System.out.println(node.getParent().toString());
//			System.out.println(node.toString() + " => " + sm);
			
			IVariableBinding localBinding = (IVariableBinding) sm.resolveBinding();
			String tp = null;
			if(localBinding != null){
				tp = localBinding.getType().getName();
				tp = TypeFilter.filtType(tp);
			}else {
				if(nameToType.containsKey(sm)) {
					tp = nameToType.get(sm);
				}
			}
			
			//local variables, so set isField and isParam both false
			//if tp == null, the type String will be default value
			VariableInfo decl = new VariableInfo(sm, sm.getIdentifier(), tp, false, false, isFinal); 
			
			varDeclList.add(decl);
		}
	}
	
	public static void getParams(MethodDeclaration node, List<VariableInfo> paramsList){
		for(Iterator<SingleVariableDeclaration> it = node.parameters().iterator(); it.hasNext(); ){
			SingleVariableDeclaration decl = it.next();
			SimpleName name = decl.getName();
			String tpStr = decl.getType().toString();
			if(decl.isVarargs()){
				tpStr = tpStr + "[]";
			}
			tpStr = TypeFilter.filtType(tpStr);
			
			boolean isFinal = Modifier.isFinal(decl.getModifiers());
			VariableInfo varInfo = new VariableInfo(name, name.getIdentifier(), tpStr, true, false, isFinal);
			paramsList.add(varInfo);
		}
	}
	
	public static String getMethodDeclFather(MethodDeclaration node){
		ASTNode candidate = node;
		while (true) {
			ASTNode p = candidate.getParent();
			
			assert p != null;
			
			if (p instanceof TypeDeclaration){
				return ((TypeDeclaration) p).getName().getIdentifier();
			}
			if(p instanceof EnumDeclaration){
				return ((EnumDeclaration) p).getName().getIdentifier();
			}
			candidate = p;
		}	
	}
	
	public static boolean isAssignLeft(ASTNode node){
		
		boolean res = false;
		
		if(node.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY){
			return res;
		}
		
		ASTNode candidate = node;
		while (true) {
			ASTNode p = candidate.getParent();
			if(Statement.class.isAssignableFrom(p.getClass())){
				break;
			}
			if( p instanceof Assignment){
				if(node.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY){
					return true;
				}
			}
			candidate = p;
		}
		return res;
	}
	
	public static boolean inLoopStmt(final ASTNode node){
		ASTNode father = null;
		ASTNode curNode = node;
		while((father  = curNode.getParent()) != null){
			if(father instanceof ForStatement || father instanceof EnhancedForStatement || 
					father instanceof WhileStatement || father instanceof DoStatement){
				return true;
			}
			curNode = father;
		}
		return false;
	}
	
	public static IfStatement getPreviousIf(final MethodDeclaration method, final ASTNode node){
		class IfVisitor extends ASTVisitor{
			private int curMaxPos = 0;
			IfStatement lastIf;
			
			@Override
			public boolean visit(IfStatement ifNode) {
				if(ifNode.getStartPosition() >= node.getStartPosition()){
					return false;
				}
				if(ifNode.getStartPosition() > curMaxPos){
					curMaxPos = ifNode.getStartPosition();
					lastIf = ifNode;
				}
				return super.visit(ifNode);
			}
			
		};
		
		IfVisitor visitor = new IfVisitor();
		method.accept(visitor);
		return visitor.lastIf;
	}
	
	public static boolean isArithOp(InfixExpression.Operator oper) {
		switch(oper.toString()) {
		case "+":
		case "-":
		case "*":
		case "/":
		case "%":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isComparing(InfixExpression.Operator oper) {
		switch(oper.toString()) {
		case ">":
		case "<":
		case "<=":
		case ">=":
		case "==":
		case "!=":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isComparingBiggerOrSmaller(InfixExpression.Operator oper) {
		switch(oper.toString()) {
		case ">":
		case "<":
		case "<=":
		case ">=":
			return true;
		default:
			return false;
		}
	}
	
	
	public static boolean isForIndexer(final SimpleName name) {
		if(name == null) {
			return false;
		}
		
		ForStatement forStmt = (ForStatement) getSpecifiedTypeFather(name, ForStatement.class);
		if(forStmt == null) {
			return false;
		}
		for(Object obj : forStmt.initializers()) {
			Expression expr = (Expression) obj;
			
			class TmpVarVisitor extends ASTVisitor{
				boolean hit = false;
				@Override
				public boolean visit(SimpleName sn) {
					if(sn.getIdentifier().equals(name.getIdentifier())) {
						hit = true;
					}
					return super.visit(sn);
				}
				
			};
			TmpVarVisitor varVisitor = new TmpVarVisitor();
			expr.accept(varVisitor);
			if(varVisitor.hit) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isLiteralNode(ASTNode expr) {
		return expr instanceof NullLiteral 
				|| expr instanceof NumberLiteral 
				|| expr instanceof StringLiteral 
				|| expr instanceof BooleanLiteral
				|| expr instanceof CharacterLiteral;
	}

	public static Expression removeBraces(Expression expr){
		while(expr instanceof ParenthesizedExpression) {
			expr = ((ParenthesizedExpression) expr).getExpression();
		}
		return expr;
	}
}
