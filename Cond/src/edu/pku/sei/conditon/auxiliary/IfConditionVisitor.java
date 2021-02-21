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
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.MethodRepre;

public class IfConditionVisitor extends ASTVisitor{
	
	private static Logger logger = Logger.getLogger(IfConditionVisitor.class);  
	
	private Set<String> logWarnMsg = new HashSet<>();
	
	private CompilationUnit cu;
	private File file;
	private MethodDeclaration currentMtdDecl;
	
	private List<VariableInfo> paramVarInfoList;
	private List<VariableInfo> localVarInfoList;
	
	private List<VariableInfo> fieldVarInfoList;
	
	private VariableInfo thisPointer;
	
	private ClassRepre classRepre;
	
	public IfConditionVisitor(CompilationUnit cu, File file, MethodDeclaration currentMtdDecl,
			List<VariableInfo> paramVarInfoList, List<VariableInfo> localVarInfoList, 
			List<VariableInfo> fieldVarInfoList, VariableInfo thisPointer, ClassRepre classRepre){
		this.cu = cu;
		this.file = file;
		this.currentMtdDecl = currentMtdDecl;
		
		this.paramVarInfoList = paramVarInfoList;
		this.localVarInfoList = localVarInfoList;
		this.fieldVarInfoList = fieldVarInfoList;
		this.thisPointer = thisPointer;
		this.classRepre = classRepre;
	}
	
	private VariableInfo getField(SimpleName node){
		for(VariableInfo info : fieldVarInfoList){
			if(node.getIdentifier().equals(info.getNameLiteral())){
				return info;
			}
		}
		return null;
	}
	
	private void usedFieldInfo(SimpleName node){
		VariableInfo fldInfo = getField(node);
		if(fldInfo != null){
			int occuredTime = fldInfo.getVariableFeature().getSpecialVal();
			fldInfo.getVariableFeature().setSpecialVal(occuredTime + 1);
			fldInfo.addUse(node);
		}
	}
	
	private void usedThisPtr(SimpleName node){
		int occuredTime = thisPointer.getVariableFeature().getSpecialVal();
		thisPointer.getVariableFeature().setSpecialVal(occuredTime + 1);
		thisPointer.addUse(node);
	}
	
	private void usedThisPtr(ThisExpression node){
		int occuredTime = thisPointer.getVariableFeature().getSpecialVal();
		thisPointer.getVariableFeature().setSpecialVal(occuredTime + 1);
		thisPointer.addUse(null);
	}
	
	public boolean visit(SimpleName node) {
		//caller of thisPtr
		if(node.getLocationInParent() == MethodInvocation.NAME_PROPERTY){
			MethodInvocation parent = (MethodInvocation) node.getParent();
			Expression expr = parent.getExpression();
			if(expr == null || expr instanceof ThisExpression || expr instanceof SuperMethodInvocation){
				//TODO:: need test
				String invokedMtd = parent.getName().getIdentifier();
				boolean allStatic = true;
				//static method is not usage of thisptr
				for(MethodRepre mtdRepre : classRepre.getMethods()){
					int flag = mtdRepre.getFlag();
					if(invokedMtd.equals(mtdRepre.getName())){
						if(Modifier.isStatic(flag)){
							allStatic &= true;
						}else{
							allStatic &= false;
						}
					}
				}
				if(!allStatic){
					usedThisPtr(node);
				}
			}
			return false;
		}
		//field of thisPtr
		if(node.getLocationInParent() ==  SuperFieldAccess.NAME_PROPERTY){//TODO:: untested
//			usedThisPtr(node);
			//TODO:: fields of super class is not collected now 
			usedFieldInfo(node);
			return false;
		}
		if(node.getLocationInParent() == FieldAccess.NAME_PROPERTY){
			FieldAccess parent = (FieldAccess) node.getParent();
			if(parent.getExpression() instanceof ThisExpression){// parent.getExpression() can be null?
//				usedThisPtr(node);
				usedFieldInfo(node);
				return false;
			}
			
		}
		
		if(node.getLocationInParent() == QualifiedName.NAME_PROPERTY){
			return false;
		}
		
		if(ASTLocator.maybeConstant(node.getIdentifier())){
			return false;
		}
		
		IBinding binding = node.resolveBinding();
		
		if(binding == null){
			usedThisPtr(node);
			return false;
		}
		
		if((binding instanceof IVariableBinding == false)){
			return false;
		}
		
		//the SimpleName must be a variable
		IVariableBinding variableBinding = (IVariableBinding) binding;
		
		if(variableBinding.isField()){
//			usedThisPtr(node);
			usedFieldInfo(node);
			return false;
		}
		
		boolean found = false;
		
		for(VariableInfo param : paramVarInfoList){
			IBinding paramBinding = param.getDef().resolveBinding();
			if(paramBinding == null ){
				//assert paramBinding instanceof IVariableBinding;
				int line = cu.getLineNumber(node.getStartPosition());
				
				String location = file.getName() + " @ " + currentMtdDecl.getName().getIdentifier() + "(), VAR: " + node.getIdentifier() + " : " + line;
				
				if(!logWarnMsg.contains(location)){
					logWarnMsg.add(location);
					logger.warn("Null PARAM Binding @ IfConditionVisitor : " + location);	
				}
				continue;
			}
			if(paramBinding.isEqualTo(variableBinding)){
				found = true;
				int occuredTime = param.getVariableFeature().getSpecialVal();
				param.getVariableFeature().setSpecialVal(occuredTime + 1);
				param.addUse(node);
				break;
			}
		}
		
		if(found){
			return super.visit(node);
		}
		
		for(VariableInfo local : localVarInfoList){
			IBinding localBinding = local.getDef().resolveBinding();
			if(localBinding == null ){
//				assert localBinding instanceof IVariableBinding;
				int line = cu.getLineNumber(node.getStartPosition());
				String location = file.getName() + " @ " + currentMtdDecl.getName().getIdentifier() + "(), VAR: " + node.getIdentifier() + " : " + line;
				if(!logWarnMsg.contains(location)){
					logWarnMsg.add(location);
					logger.warn("Null VarBinding @ IfBodyVisitor : " + location);	
				}
				
				continue;
			}
			if(localBinding.isEqualTo(variableBinding)){
				found = true;
				int occuredTime = local.getVariableFeature().getSpecialVal();
				local.getVariableFeature().setSpecialVal(occuredTime + 1);
				local.addUse(node);
				break;
			}
		}
		//TODO:: debug for fields
//		assert found == true;
		if(found == false){
//			usedThisPtr(node);
			usedFieldInfo(node);
		}

		return false;
	}

	
	@Override
	public boolean visit(ThisExpression node) {
		if(node.getQualifier() == null){
			if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY || node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
				usedThisPtr(node);
			}
		}
		return super.visit(node);
	}
	
}