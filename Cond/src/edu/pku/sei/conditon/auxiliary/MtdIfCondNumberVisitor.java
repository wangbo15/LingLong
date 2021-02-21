package edu.pku.sei.conditon.auxiliary;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import edu.pku.sei.conditon.ds.VariableInfo;

public class MtdIfCondNumberVisitor extends ASTVisitor{
	private static Logger logger = Logger.getLogger(MtdIfCondNumberVisitor.class);  
	private Set<String> logWarnMsg = new HashSet<>();

	
	private CompilationUnit cu;
	private MethodDeclaration currentMtdDecl;
	private File file;
	
	
	//TODO:: collect this
	private VariableInfo thisPointer;
	private List<VariableInfo> allLocalVarInfo;
	
	public MtdIfCondNumberVisitor(CompilationUnit cu, MethodDeclaration currentMtdDecl, File file, VariableInfo thisPointer, List<VariableInfo> localVarInfoList){
		this.cu = cu;
		this.currentMtdDecl = currentMtdDecl;
		this.file = file;
		
		this.thisPointer = thisPointer;
		this.allLocalVarInfo = localVarInfoList;
	}
	
	private void thisPtrTimeInc(SimpleName node){
		IfStatement ifStmt = ASTLocator.getFatherIfStmt(node);
		if(ifStmt != null){
			thisPointer.getVariableFeature().setIfCondNumber(thisPointer.getVariableFeature().getIfCondNumber() + 1);
		}
	}
	private void thisPtrTimeInc(ThisExpression node) {
		IfStatement ifStmt = ASTLocator.getFatherIfStmt(node);
		if(ifStmt != null){
			thisPointer.getVariableFeature().setIfCondNumber(thisPointer.getVariableFeature().getIfCondNumber() + 1);
		}			
	}
	
	@Override
	public boolean visit(SimpleName node) {
		//caller of thisPtr
		if(node.getLocationInParent() == MethodInvocation.NAME_PROPERTY){
			MethodInvocation parent = (MethodInvocation) node.getParent();
			Expression expr = parent.getExpression();
			if(expr == null || expr instanceof ThisExpression || expr instanceof SuperMethodInvocation){
				thisPtrTimeInc(node);
			}
			return false;
		}
		/*
		//field of thisPtr
		if(node.getLocationInParent() ==  SuperFieldAccess.NAME_PROPERTY){
			thisPtrTimeInc(node);
			return false;
		}
		
		if(node.getLocationInParent() == FieldAccess.NAME_PROPERTY){
			FieldAccess parent = (FieldAccess) node.getParent();
			if(parent.getExpression() instanceof ThisExpression){// parent.getExpression() can be null?
				thisPtrTimeInc(node);
				return false;
			}
			
		}
		*/
		
		if(ASTLocator.maybeConstant(node.getIdentifier())){
			return false;
		}
		
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			thisPtrTimeInc(node);
			return false;
		}
		
		//TODO:: sth unexcepted ??????????????????????????????????
		if(binding instanceof IVariableBinding == false){
			return false;
		}
		
		//the SimpleName must be a variable
		IVariableBinding variableBinding = (IVariableBinding) binding;
		
//		if(variableBinding.isField()){
//			thisPtrTimeInc(node);
//			return false;
//		}
					
		VariableInfo hitInfo = null;
		for(VariableInfo varInfo: allLocalVarInfo){
			
			if(varInfo.getVariableFeature().isSuperField()){
				continue;
			}
			
			IVariableBinding infoBinding = (IVariableBinding) varInfo.getDef().resolveBinding();
			
			if(infoBinding == null){
				int line = cu.getLineNumber(node.getStartPosition());
				String location = file.getName() + " @ " + currentMtdDecl.getName().getIdentifier() + "(), VAR: " + node.getIdentifier() + " : " + line;
				
				if(!logWarnMsg.contains(location)){
					logWarnMsg.add(location);
					logger.warn("Null VarBinding @ IfCondNumberVisitor : " + location);	
				}
				continue;
			}
			
			if(infoBinding.isEqualTo(variableBinding)){
				hitInfo = varInfo;
				break;
			}
		}
		if(hitInfo == null){
			return false;
		}
		
		//whether in if condition
		IfStatement ifStmt = ASTLocator.getFatherIfStmt(node);
		
		if(ifStmt == null){
			return false;
		}else{
			int cur = hitInfo.getVariableFeature().getIfCondNumber();
			hitInfo.getVariableFeature().setIfCondNumber(cur + 1);
		}

		return false;
	}
	
	@Override
	public boolean visit(ThisExpression node) {
		if(node.getQualifier() == null){
			if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY || node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
				thisPtrTimeInc(node);
			}
		}
		return super.visit(node);
	}
	
}