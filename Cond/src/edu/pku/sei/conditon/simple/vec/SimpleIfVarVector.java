package edu.pku.sei.conditon.simple.vec;

import java.util.List;

import org.eclipse.jdt.core.dom.IfStatement;

import edu.pku.sei.conditon.ds.MethodInfo;
import edu.pku.sei.conditon.ds.VariableInfo;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 18, 2017
 */
public class SimpleIfVarVector extends SimpleIfVector{
	//acceptable local variables
	private List<VariableInfo> localVariables;
	private List<VariableInfo> fieldVariables;
	private VariableInfo thisPtr;
	
	public SimpleIfVarVector(long id, String fileName, String filePath, MethodInfo currentMethodInfo, 
			IfStatement node, boolean inLoop, String bodyCtl) {
		super(id, fileName, filePath, currentMethodInfo, node, inLoop, bodyCtl);
	}

	public List<VariableInfo> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(List<VariableInfo> localVariables) {
		this.localVariables = localVariables;
	}

	public VariableInfo getThisPtr() {
		return thisPtr;
	}

	public void setThisPtr(VariableInfo thisPtr) {
		this.thisPtr = thisPtr;
	}

	public List<VariableInfo> getFieldVariables() {
		return fieldVariables;
	}

	public void setFieldVariables(List<VariableInfo> fieldVariables) {
		this.fieldVariables = fieldVariables;
	}
		
}
