package edu.pku.sei.conditon.auxiliary;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.pku.sei.conditon.ds.VariableInfo;

public class UsageVisitor extends ASTVisitor{
	
	private static Logger logger = Logger.getLogger(UsageVisitor.class);  
	private Set<String> logWarnMsg = new HashSet<>();

	private int begin = -1;
	private int end = Integer.MAX_VALUE;
	
	private CompilationUnit cu;
	private MethodDeclaration currentMtdDecl;
	private File file;

	
	private boolean hit[] = null;
	
	private boolean collectVarUsedByCond = false;
	
	private List<VariableInfo> allLocalVarInfo;
	private VariableInfo thisPointer;

	public UsageVisitor(boolean collectVarUsedByCond, CompilationUnit cu, MethodDeclaration currentMtdDecl,
			File file, VariableInfo thisPointer, List<VariableInfo> localVarInfoList){
		
		this.collectVarUsedByCond = collectVarUsedByCond;
		this.cu = cu;
		this.currentMtdDecl = currentMtdDecl;
		this.file = file;
		
		if(this.begin == -1 && this.end == Integer.MAX_VALUE){
			thisPointer.getVariableFeature().setFirstUseInBody(VariableInfo.NO_USE); //TODO:: why need this before?
		}
		
		this.thisPointer = thisPointer;
		this.allLocalVarInfo = localVarInfoList;
		hit = new boolean[localVarInfoList.size() + 1];
	}
	
	
	public UsageVisitor(boolean collectVarUsedByCond, CompilationUnit cu, MethodDeclaration currentMtdDecl,
			File file, VariableInfo thisPointer, List<VariableInfo> localVarInfoList,
			int from, int to){
				
		this.begin = from;
		this.end = to;
		
		this.collectVarUsedByCond = collectVarUsedByCond;
		this.cu = cu;
		this.currentMtdDecl = currentMtdDecl;
		this.file = file;
		this.thisPointer = thisPointer;
		this.allLocalVarInfo = localVarInfoList;
		hit = new boolean[localVarInfoList.size() + 1];
	}
	
	public void setNoUse() {
		assert hit != null;
		for(int i = 0; i < allLocalVarInfo.size(); i++){
			if(hit[i] == false){
				setUsage(allLocalVarInfo.get(i), VariableInfo.NO_USE);
			}
		}
		int thisIdx = allLocalVarInfo.size();
		if(hit[thisIdx] == false){
			setUsage(thisPointer, VariableInfo.NO_USE);
		}
	}

	public boolean hitAll(){
		assert hit != null;
		
		boolean hitAll = true;
		for(boolean b : hit){
			hitAll &= b;
		}
		return hitAll;
	}
	
	private void setUsage(VariableInfo info, String usage){
		if(this.begin == -1 && this.end == Integer.MAX_VALUE){
			info.getVariableFeature().setFirstUseInBody(usage);
		}else{
			info.getVariableFeature().setFirstUseOutOfStmt(usage);
		}
	}
	
	private void setThisPtrUse(String usage){
		int thisIdx = hit.length - 1;
		if(hit[thisIdx] == false){
			setUsage(thisPointer, usage);
			hit[thisIdx] = true;
		}
	}
	
	private void setLocalUse(VariableInfo info, int index, String usage){
		if(hit[index] == false){
			setUsage(info, usage);
			hit[index] = true;
		}
		
	}
	
	private void addUseTime(VariableInfo info, Expression node){
		
		assert node instanceof SimpleName || node instanceof ThisExpression;
		
		int occuredTime = info.getVariableFeature().getSpecialVal();
		info.getVariableFeature().setSpecialVal(occuredTime + 1);
		info.addUse(node);
	}
	
	private String getUsage(SimpleName node) {
				
		ASTNode throwStmt = ASTLocator.getSpecifiedTypeFather(node, ThrowStatement.class);
		if(throwStmt != null){
			return VariableInfo.THROW_USE;
		}
		
		if(node.getParent() != null && node.getParent() instanceof InfixExpression){
			InfixExpression infix = (InfixExpression) node.getParent();
			String oper = infix.getOperator().toString();
			if(oper.equals("/") || oper.equals("%")){
				if(node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
					return VariableInfo.DIVISIOR_USE;
				}
			}
			if(oper.equals("-") && node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
				return VariableInfo.SUBTRAHEND_USE;
			}
			return oper;
		}else if(node.getLocationInParent() == PrefixExpression.OPERAND_PROPERTY){
			PrefixExpression prefix = (PrefixExpression) node.getParent();
			return prefix.getOperator().toString() + "x";
		}else if(node.getLocationInParent() == PostfixExpression.OPERAND_PROPERTY){
			PostfixExpression postfix = (PostfixExpression) node.getParent();
			return "x" + postfix.getOperator().toString();
		}else if(node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY){
			return VariableInfo.PARAM_USE;
		}else if(node.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| node.getLocationInParent() == ConstructorInvocation.ARGUMENTS_PROPERTY
				|| node.getLocationInParent() == SuperConstructorInvocation.ARGUMENTS_PROPERTY ){
			return VariableInfo.CONS_PARAM_USE;
		}else if(node.getLocationInParent() == FieldAccess.NAME_PROPERTY &&
				((FieldAccess) node.getParent()).getExpression() instanceof ThisExpression == false){
			return VariableInfo.FIELD_USE;
		}else if(node.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY){
			return VariableInfo.RET_USE;
		}else if(node.getLocationInParent() == ArrayAccess.INDEX_PROPERTY){
			return VariableInfo.ARRIDX_USE;
		}else if(node.getLocationInParent() == ArrayAccess.ARRAY_PROPERTY){
			return VariableInfo.ARR_USE;
		}else if(node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY || node.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY){
			return VariableInfo.NO_USE;
		}else if(node.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY || node.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY){
			return VariableInfo.ASSTO_USE;
		}else if(node.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY){
			CastExpression parent = (CastExpression) node.getParent();
			return VariableInfo.CAST_USE + "#" + parent.getType().toString();
		}else if(node.getLocationInParent() == InstanceofExpression.LEFT_OPERAND_PROPERTY || node.getLocationInParent() == InstanceofExpression.RIGHT_OPERAND_PROPERTY){
			return VariableInfo.INSTANCEOF_USE;
		}else if(ASTLocator.getSpecifiedTypeFather(node, MethodInvocation.class) != null){//MUST AT LAST
			
			if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY || node.getParent().getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
				return VariableInfo.CALLER_USE;
			}
			
		}

		return VariableInfo.OTHER_USE;
	}

	/** enum the usage Expression*/
	@Override
	public boolean visit(SimpleName node) {

		int ln = cu.getLineNumber(node.getStartPosition());
		
		if(ln <= this.begin || ln >= end){
			return false;
		}
		
		if(ASTLocator.isAssignLeft(node)){
			return false;
		}
		
		//caller of thisPtr
		if(node.getLocationInParent() == MethodInvocation.NAME_PROPERTY){
			MethodInvocation parent = (MethodInvocation) node.getParent();
			Expression expr = parent.getExpression();
			if(expr == null || expr instanceof ThisExpression || expr instanceof SuperMethodInvocation){
				if(this.collectVarUsedByCond){
					addUseTime(thisPointer, node);
					return false;
				}else{
					setThisPtrUse(VariableInfo.CALLER_USE);
					return false;
				}
			}
		}
		/*
		//field of thisPtr
		if(node.getLocationInParent() ==  SuperFieldAccess.NAME_PROPERTY){
			if(this.collectVarUsedByCond){
				addUseTime(thisPointer, node);
				return false;
			}else{
				if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
//					setThisPtrUse(VariableInfo.FIELD_USE);
					setThisPtrUse(VariableInfo.CALLER_USE);
					return false;
				}
			}
		}
		
		if(node.getLocationInParent() == FieldAccess.NAME_PROPERTY){
			FieldAccess parent = (FieldAccess) node.getParent();
			if(parent.getExpression() instanceof ThisExpression){// parent.getExpression() can be null?
				if(this.collectVarUsedByCond){
					addUseTime(thisPointer, node);
					return false;
				}else{
					
					if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
//						setThisPtrUse(VariableInfo.FIELD_USE);
						setThisPtrUse(VariableInfo.CALLER_USE);
						return false;
					}
				}
			}
		}
		*/
		if(node.getLocationInParent() == QualifiedName.NAME_PROPERTY){
			return false;
		}
		
		if(ASTLocator.maybeConstant(node.getIdentifier())){
			return false;
		}
		
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			if(this.collectVarUsedByCond){
				addUseTime(thisPointer, node);
				return false;
			}else{
				if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
//					setThisPtrUse(VariableInfo.FIELD_USE);
					setThisPtrUse(VariableInfo.CALLER_USE);
					return false;
				}
			}
		}
		
		if((binding instanceof IVariableBinding == false)){
			return false;
		}
		
		IVariableBinding variableBinding = (IVariableBinding) binding;
		
//		if(variableBinding.isField()){
//			
//			if(this.collectVarUsedByCond){// collecting use in the condition
//				addUseTime(thisPointer, node);
//			}else{
//				if(node.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY){
//					setThisPtrUse(VariableInfo.FIELD_USE);
//				}else{
//					setThisPtrUse(VariableInfo.CALLER_USE);
//				}
//			}
//			return false;
//		}
		
		for(int i = 0; i < allLocalVarInfo.size(); i++){
			VariableInfo varInfo = allLocalVarInfo.get(i);
			
			if(varInfo.getVariableFeature().isSuperField()){
				continue;
			}
			
			IVariableBinding infoBinding = (IVariableBinding) varInfo.getDef().resolveBinding();
			
			if(infoBinding == null){
				int line = cu.getLineNumber(node.getStartPosition());
				String location = file.getName() + " @ " + currentMtdDecl.getName().getIdentifier() + "(), VAR: " + node.getIdentifier() + " : " + line;
				
				if(!logWarnMsg.contains(location)){
					logWarnMsg.add(location);
					logger.warn("Null VarBinding @ IfBodyVisitor : " + location);	
				}
				
				continue;
			}
						
			if(variableBinding.getVariableDeclaration() != null){
				variableBinding = variableBinding.getVariableDeclaration();
			}
			//`infoBinding.toString().equals(variableBinding.toString())` is // just in case
			if(infoBinding.isEqualTo(variableBinding) || infoBinding.toString().equals(variableBinding.toString())){
				if(this.collectVarUsedByCond){
					addUseTime(varInfo, node);
				}else{
					String usage = getUsage(node);
					setLocalUse(varInfo, i, usage);
				}
			}
		}
		
		return super.visit(node);
	}


	@Override
	public boolean visit(ThisExpression node) {
		if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY || node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
			if(this.collectVarUsedByCond){
				addUseTime(thisPointer, node);
				return false;
			}else{
				if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
					InfixExpression infix = (InfixExpression) node.getParent();
					String oper = infix.getOperator().toString();
					setThisPtrUse(oper);
					return false;
				}
			}
		}
		
		return super.visit(node);
	}

	
	
}