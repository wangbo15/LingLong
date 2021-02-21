package edu.pku.sei.conditon.simple.vec;

import edu.pku.sei.conditon.ds.VariableInfo;
/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Feb 27, 2017
 */
public class SimpleExpr {
	private String left;
	private String right;	//exceptions
	private String leftType;
	private VariableInfo leftVarInfo;
	
	public SimpleExpr(VariableInfo leftVarInfo){
		this.leftVarInfo = leftVarInfo;
	}
	
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}

	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}
	
	public String toString(){
		return left + " " + right;
	}
	public String getLeftType() {
		return leftType;
	}
	public void setLeftType(String leftType) {
		this.leftType = leftType;
	}

	public VariableInfo getLeftVarInfo() {
		return leftVarInfo;
	}

	public void setLeftVarInfo(VariableInfo leftVarInfo) {
		this.leftVarInfo = leftVarInfo;
	}

}
