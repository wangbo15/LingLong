package edu.pku.sei.conditon.auxiliary;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * May 12, 2017
 */
public class AssignmentCollectorVisitor extends ASTVisitor {
	public List<ASTNode> result;
	public IVariableBinding targetBinding;

	private boolean isCollection;
	
	public AssignmentCollectorVisitor(List<ASTNode> result, IVariableBinding binding) {
		this.result = result;
		this.targetBinding = binding;
		
		if(binding != null){
			String typeStr = null;
			if(binding.getType().isParameterizedType()){
				typeStr = binding.getType().getBinaryName();
			}else{
				typeStr = binding.getType().getQualifiedName();
			}
//			System.out.println(typeStr);
			if(typeStr.endsWith("List") || typeStr.endsWith("Set") || typeStr.endsWith("Map")){
				isCollection = true;
			}
		}
		
		//can not be local var
//		assert targetBinding.isField() == false;
	}
	
	//for this pointer
	public AssignmentCollectorVisitor(List<ASTNode> result){
		this.result = result;
	}
	
	
	@Override
	public boolean visit(MethodInvocation node) {
		if(isCollection){
			String mtdName = node.getName().getIdentifier();
			if(mtdName.equals("add") || mtdName.equals("addAll") || mtdName.equals("put") || mtdName.equals("putAll")){
				InvokerCollector visitor = new InvokerCollector(targetBinding);
				Expression caller = node.getExpression();
				if(caller != null){
					caller.accept(visitor);
					if (visitor.hit) {
						result.add(node);
					}
				}
				
			}
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		Expression left = node.getLeftHandSide();
		
		LeftCollector leftVisitor = null;
		if(targetBinding != null){	// for local
			leftVisitor = new LeftCollector(targetBinding);	
		}else{						// for this pointer
			leftVisitor = new LeftCollector();	
		}
		
		left.accept(leftVisitor);
		if (leftVisitor.hit) {
			result.add(node.getRightHandSide());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		
		if(targetBinding == null){//useless for this pointer
			return super.visit(node);
		}
		
		if (node.getInitializer() == null) {
			return super.visit(node);
		}
		
		SimpleName left = node.getName();
		IBinding leftBinding = left.resolveBinding();

		if (leftBinding == null) {// TODO: anonymous class maybe null
			return super.visit(node);
		}

		if (leftBinding != null && leftBinding.isEqualTo(targetBinding)) {
			result.add(node.getInitializer());
		}

		return super.visit(node);
	}

	private class LeftCollector extends ASTVisitor {
		public boolean hit = false;
		private IVariableBinding binding = null;
		
		public LeftCollector(){}
		
		public LeftCollector(IVariableBinding binding) {
			this.binding = binding;
		}

		@Override
		public boolean visit(SimpleName node) {
			
			if(node.getLocationInParent() == MethodInvocation.NAME_PROPERTY){
				return false;
			}
			
			if(node.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY && node.getParent().getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY){
				return false;
			}
			
			if(this.binding != null){//for local var
				IBinding bd = node.resolveBinding();
				if (bd instanceof IVariableBinding) {
					IVariableBinding nodeBinding = (IVariableBinding) bd;
					if (this.binding.isEqualTo(nodeBinding)) {
						hit = true;
					}
				}
			}else{//for this ptr
				IBinding bd = node.resolveBinding();
				if(bd == null){//maybe super field
					hit = true;
				}
				if (bd instanceof IVariableBinding) {
					IVariableBinding nodeBinding = (IVariableBinding) bd;
					if(nodeBinding.isField()){
						hit = true;
					}
				}
				
			}
			return false;
		}

	}
	
	private class InvokerCollector extends ASTVisitor{
		public boolean hit = false;
		private IVariableBinding binding = null;
		
		public InvokerCollector(IVariableBinding binding){
			this.binding = binding;
		}
		@Override
		public boolean visit(SimpleName node) {
			ASTNode parent = node.getParent();
			if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY || (parent instanceof Expression && parent.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY)){				
				IBinding bd = node.resolveBinding();
				if (bd instanceof IVariableBinding) {
					IVariableBinding nodeBinding = (IVariableBinding) bd;
					if (this.binding.isEqualTo(nodeBinding)) {
						hit = true;
					}
				}
				
			}
			return false;
		}
	}

}