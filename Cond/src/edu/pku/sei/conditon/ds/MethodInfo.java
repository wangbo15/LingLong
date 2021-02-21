package edu.pku.sei.conditon.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.FieldRepre;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 18, 2017
 */
public class MethodInfo {
	
	private static Logger logger = Logger.getLogger(MethodInfo.class);  
	
	private String key;
	private String name;
	private List<VariableInfo> paramsList;
	
	private List<VariableInfo> fieldList;
	
	private VariableInfo thisPointer;
	private boolean isConstructor;
	private boolean isStatic;
	
	private Map<String, FieldRepre> constantFieldMap = new HashMap<>();
	
	public final static String UNKNOWN_CLS_NAME = "UNKNOWN";
	
	//TODO::remove !!!
//	public MethodInfo(MethodDeclaration node){}
	
	/**
	 * 
	 * @param node
	 * @param classRepre
	 * @param currentFieldsMap: collect name literal map to field decl simplename
	 */
	public MethodInfo(MethodDeclaration node, ClassRepre classRepre, Map<String, SimpleName> currentFieldsMap) {
		this.name = node.getName().toString();
		
		if(node.resolveBinding() != null){
			key = node.resolveBinding().getKey();// TODO: maybe null in the inter-class or inter-enum
		}else{
			StringBuffer argSb = new StringBuffer();
			for(Object param : node.parameters()){
				argSb.append((((ASTNode) param)).toString());
			}
			
			String className = null;
			if(node.getParent() instanceof TypeDeclaration){
				className = ((TypeDeclaration) node.getParent()).getName().getIdentifier();
			}else if(node.getParent() instanceof EnumDeclaration){
				className = ((EnumDeclaration) node.getParent()).getName().getIdentifier();
			}else{
				className = UNKNOWN_CLS_NAME;
				String msg = "Method Has No Parent: " + node.getName().getIdentifier() + " (" + argSb.toString() + ")";
				System.err.println(msg);
				logger.warn(msg);
			}
			
			key = "NULL-MTD-KEY: " + className + "." + node.getName().getIdentifier() + " (" + argSb.toString() + ")";
			System.err.println(key);
//			System.err.println(node.toString());
			logger.warn(key + '\n' + node.toString());
		}
		
		paramsList = new ArrayList<>();
		ASTLocator.getParams(node, paramsList);
		
		this.isConstructor = node.isConstructor();
		int flag = node.getModifiers();
		this.isStatic = Modifier.isStatic(flag);
		
		String fatherType = ASTLocator.getMethodDeclFather(node);
		
		this.thisPointer = new VariableInfo(null, "THIS", fatherType, false, false, false);
		
		
		fieldList = new ArrayList<>();
		
		ClassRepre tmpCls = classRepre;
		
//		ClassRepre fatherCls = tmpCls.getFatherCls();
//		while(fatherCls != null){
		
		
		while(true){
			//add all fld of the tmpCls
			for(FieldRepre field : tmpCls.getFields()){
				int fldFlag = field.getFlag();
				if(field.isConstant()){
					if(field.getType().equals("String") || TypeUtil.isPrimitiveType(field.getType())){
						constantFieldMap.put(field.getName(), field);
						continue;
					}
				}
				
				SimpleName fldDecl = null;
				boolean isSuper = false;
				if(currentFieldsMap.containsKey(field.getName())){
					fldDecl = currentFieldsMap.get(field.getName());
				}else{
					isSuper = true;
				}
				//decleartion, nameLiteral, type, isParam, isField, isFinal
				VariableInfo varInfo = new VariableInfo(fldDecl, field.getName(), field.getType(), false, true, Modifier.isFinal(fldFlag));
				varInfo.getVariableFeature().setSuperField(isSuper);
				fieldList.add(varInfo);
			}
			ClassRepre container = tmpCls.getContainerCls();
			if(container != null){
				tmpCls = container;
			}else{
				break;
			}
		}
		
		
	}
	
	public void resetParamAndThisCondition(){
		thisPointer.getVariableFeature().setSpecialVal(0);
		thisPointer.getVariableFeature().setLastAssign(AssignInfo.getUnknowAssign());
		for(VariableInfo info : paramsList){
			info.getVariableFeature().setSpecialVal(0);
			info.getVariableFeature().setLastAssign(AssignInfo.getUnknowAssign());
		}
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<VariableInfo> getParamsList() {
		return paramsList;
	}

	public void setParamsList(List<VariableInfo> paramsList) {
		this.paramsList = paramsList;
	}
	
	public List<VariableInfo> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<VariableInfo> fieldList) {
		this.fieldList = fieldList;
	}

	public VariableInfo getThisPointer() {
		return thisPointer;
	}

	public void setThisPointer(VariableInfo thisPointer) {
		this.thisPointer = thisPointer;
	}

	public boolean isConstructor() {
		return isConstructor;
	}

	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	
	public Map<String, FieldRepre> getConstantFieldMap() {
		return constantFieldMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodInfo other = (MethodInfo) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodInfo [key=" + key + ", name=" + name + ", paramsList=" + paramsList + "]";
	}
		
}
