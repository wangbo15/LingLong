/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package edu.pku.sei.conditon.simple.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.TypeUtil;

/**
 * @author Jiajun
 * @date May 27, 2017
 */
public class ExprFilter {

	private static Map<String, Pair<Set<String>, String>> _typeInfo = new HashMap<>();

	public static void init(String path) {
//		String path = subject.getHome() + subject.getSsrc();
		List<String> fileList = JavaFile.ergodic(path, new ArrayList<String>());
		MethodAndFieldCollectorVisitor visitor = new MethodAndFieldCollectorVisitor();
		for (String fileName : fileList) {
			CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSourceAsJava7(JavaFile.readFileToString(fileName),
					ASTParser.K_COMPILATION_UNIT);
			unit.accept(visitor);
		}
	}

	public static boolean isLegalExpr(String type, String varName, String condition, Set<String> locaLegalVarNames,
			String currentClassName) {
		if (! TypeUtil.isPrimitiveType(type)) {

			if (varName.equals("THIS")) {
				varName = "this";
				
				if(condition.equals("this != null") || condition.equals("this == null")){
					return false;
				}
				
				if(condition.equals("this instanceof " + type)){
					return false;
				}
			}

			if (condition.equals(varName) || condition.startsWith(varName + " >")
					|| condition.startsWith(varName + " <")) {
				return false;
			}
			if (condition.endsWith(">= " + varName) || condition.endsWith("<= " + varName)
					|| condition.endsWith("> " + varName) || condition.endsWith("< " + varName)) {
				return false;
			}
			if (condition.contains("[" + varName + "]")) {
				return false;
			}
			if (condition.startsWith(varName + " -") || condition.startsWith(varName + " +")) {
				return false;
			}

		}

		ASTNode node = JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		
		if(node instanceof InfixExpression){
			InfixExpression infixExpression = (InfixExpression) node;
			if(infixExpression.getLeftOperand().toString().equals(infixExpression.getRightOperand().toString())){
				return false;
			}
		}
		
		ExprAnalysisVisitor visitor = new ExprAnalysisVisitor(varName, type);
		node.accept(visitor);
		// user-defined class
		if (_typeInfo.containsKey(type) && !visitor.isLegal()) {
			return false;
		}
		Set<String> singleVars = visitor.getSingleVariables();
		for (String var : singleVars) {
			if (!locaLegalVarNames.contains(var) && !isField(var, currentClassName)) {
				return false;
			}
		}
		
		// condition expressions contains array access
		boolean isArrayAccess = condition.contains(varName + "[");
		// is primitive type, illegal field/method/array access
		if (TypeUtil.isPrimitiveType(type) && (condition.contains(varName + ".") || isArrayAccess)) {
			return false;
		}
		// array access non-array
		if (isArrayAccess && !type.contains("[")) {
			return false;
		}
		return true;
	}
	
	private static boolean isField(String varName, String type){
		Pair<Set<String>, String> pair = _typeInfo.get(type);
		while(pair != null){
			if(pair.getFirst().contains(varName)){
				return true;
			} else {
				String parent = pair.getSecond();
				if(parent != null){
					pair = _typeInfo.get(parent);
				} else {
					pair = null;
				}
			}
		}
		return false;
	}

	
	static class MethodAndFieldCollectorVisitor extends ASTVisitor {

		private final String __name__ = "@ExprFilter$MethodAndFieldCollectorVisitor ";

		@Override
		public boolean visit(TypeDeclaration node) {
			String clazzName = node.getName().getFullyQualifiedName();
			String parent = null;
			Type superType = node.getSuperclassType();
			if (superType != null) {
				parent = superType.toString();
			}
			if (_typeInfo.containsKey(clazzName)) {
				System.out.println(__name__ + "#visitor class name conflict : " + clazzName);
			}
			Set<String> methodAndVarSet = new HashSet<>();

			for (FieldDeclaration fieldDeclaration : node.getFields()) {
				for (Object frag : fieldDeclaration.fragments()) {
					if (frag instanceof VariableDeclarationFragment) {
						VariableDeclarationFragment vdf = (VariableDeclarationFragment) frag;
						String varName = vdf.getName().getFullyQualifiedName();
						methodAndVarSet.add(varName);
					}
				}
			}

			for (MethodDeclaration methodDeclaration : node.getMethods()) {
				String methodName = methodDeclaration.getName().getFullyQualifiedName();
				methodAndVarSet.add(methodName);
			}

			Pair<Set<String>, String> pair = new Pair<Set<String>, String>(methodAndVarSet, parent);
			_typeInfo.put(clazzName, pair);

			return false;
		}
	}

	static class ExprAnalysisVisitor extends ASTVisitor {
		private boolean _legal = true;
		private String _varName = null;
		private String _type = null;
		private Set<String> _singleVariableNames = new HashSet<>();

		public ExprAnalysisVisitor(String varName, String type) {
			_varName = varName;
			_type = type;
		}

		public boolean isLegal() {
			return _legal;
		}
		
		public Set<String> getSingleVariables(){
			return _singleVariableNames;
		}
		
		public boolean visit(ArrayAccess node){
			String name = node.getArray().toString();
			String index = node.getIndex().toString();
			if((name.equals(_varName) && !_type.contains("[")) || index.equals(_varName)){
				_legal = false;
				return false;
			}
			return true;
		}
		
		public boolean visit(QualifiedName node){
			String qualifier = node.getQualifier().toString();
			String name = node.getName().toString();
			if(_varName.equals(qualifier)){
				_legal = isField(name, _type);
				if(!_legal){
					return false;
				}
			}
			return true;
		}
		
		public boolean visit(SimpleName node){
			String name = node.getFullyQualifiedName();
			ASTNode parent = node.getParent();
			if(parent != null){
				String parentStr = parent.toString().replace("this.", "");
				if(!parentStr.contains("." + name)){
					_singleVariableNames.add(name);
				}
			}
			
			return true;
		}
		
		
		public boolean visit(MethodInvocation node){
			if(node.getExpression() != null){
				String var = node.getExpression().toString();
				String name = node.getName().toString();
				if(_varName.equals(var)){
					_legal = isField(name, _type);
					if(!_legal){
						return false;
					}
				}
			}
			return true;
		}
		
		public boolean visit(FieldAccess node){
			String qualifier = node.getExpression().toString();
			String name = node.getName().toString();
			if(_varName.equals(qualifier)){
				_legal = isField(name, _type);
				if(!_legal){
					return false;
				}
			}
			return true;
		}
		
		
		public boolean visit(PostfixExpression node) {
			String expr = node.getOperand().toString().trim();
			if(expr.equals(_varName) && TypeUtil.isPrimitiveType(_type) == false){
				_legal = false;
				return false;
			}
			return super.visit(node);
		}

		public boolean visit(PrefixExpression node) {
			String expr = node.getOperand().toString().trim();
			if(expr.equals(_varName) && TypeUtil.isPrimitiveType(_type) == false){
				_legal = false;
				return false;
			}
			return super.visit(node);
		}

		public boolean visit(InfixExpression node){
			String left = node.getLeftOperand().toString().trim();
			if(left.equals("this")){
				if(node.getRightOperand() instanceof NumberLiteral || node.getRightOperand() instanceof PrefixExpression || node.getRightOperand() instanceof PostfixExpression){
					_legal = false;
					return false;
				}
				
				
			}
			if(left.equals(_varName)){
				String rightStr = node.getRightOperand().toString().trim();
				if(TypeUtil.isPrimitiveIntType(_type) && node.getRightOperand() instanceof NumberLiteral){
					if(rightStr.contains(".")){
						_legal = false;
						return false;
					}
				}
			}
			
			String right = node.getRightOperand().toString().trim();
			if(right.equals("this")){
				if(node.getLeftOperand() instanceof NumberLiteral || node.getLeftOperand() instanceof PrefixExpression || node.getLeftOperand() instanceof PostfixExpression 
						|| left.equals("this")){
					_legal = false;
					return false;
				}
			}
			
			if(right.equals(_varName)){
				String leftStr = node.getLeftOperand().toString().trim();
				if(TypeUtil.isPrimitiveIntType(_type) && node.getLeftOperand() instanceof NumberLiteral){
					if(leftStr.contains(".")){
						_legal = false;
						return false;
					}
				}
			}
			return true;
		}

	}
	
}