package edu.pku.sei.conditon.util.csv;
/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 20, 2017
 */
public class VarInfoCSVItem {
	private int id;
	private int line;
	private int column;
	private String fileName;
	private String methodName;
	private String varName;
	private String varType;
	
	
	private String lassAss;
	private int dis0;
	private int preassnum;
	
	private boolean isParam;
	private boolean isfld;
	private boolean isfnl;
	private boolean inloop;

	private int inCondNum;
	private int filecondnum;
	private int totcondnum;
	private String bodyUse;
	private String outuse;
	private String bodyCtl;

	private boolean inIf;

	
	public VarInfoCSVItem(int id, int line, int column, String fileName, String methodName, String varName,
			String varType, boolean inIf) {
		this.id = id;
		this.line = line;
		this.column = column;
		this.fileName = fileName;
		this.methodName = methodName;
		this.varName = varName;
		this.varType = varType;
		this.inIf = inIf;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getVarType() {
		return varType;
	}
	public void setVarType(String varType) {
		this.varType = varType;
	}
	public String getLassAss() {
		return lassAss;
	}
	public void setLassAss(String lassAss) {
		this.lassAss = lassAss;
	}
	public boolean isParam() {
		return isParam;
	}
	public void setParam(boolean isParam) {
		this.isParam = isParam;
	}
	public int getInCondNum() {
		return inCondNum;
	}
	public void setInCondNum(int inCondNum) {
		this.inCondNum = inCondNum;
	}
	public String getBodyUse() {
		return bodyUse;
	}
	public void setBodyUse(String bodyUse) {
		this.bodyUse = bodyUse;
	}	
	public boolean isInIf() {
		return inIf;
	}
	public void setInIf(boolean inIf) {
		this.inIf = inIf;
	}
	
	public int getDis0() {
		return dis0;
	}

	public void setDis0(int dis0) {
		this.dis0 = dis0;
	}

	public int getPreassnum() {
		return preassnum;
	}

	public void setPreassnum(int preassnum) {
		this.preassnum = preassnum;
	}

	public boolean isIsfld() {
		return isfld;
	}

	public void setIsfld(boolean isfld) {
		this.isfld = isfld;
	}

	public boolean isIsfnl() {
		return isfnl;
	}

	public void setIsfnl(boolean isfnl) {
		this.isfnl = isfnl;
	}

	public boolean isInloop() {
		return inloop;
	}

	public void setInloop(boolean inloop) {
		this.inloop = inloop;
	}

	public int getFilecondnum() {
		return filecondnum;
	}

	public void setFilecondnum(int filecondnum) {
		this.filecondnum = filecondnum;
	}

	public int getTotcondnum() {
		return totcondnum;
	}

	public void setTotcondnum(int totcondnum) {
		this.totcondnum = totcondnum;
	}

	public String getOutuse() {
		return outuse;
	}

	public void setOutuse(String outuse) {
		this.outuse = outuse;
	}

	public String getBodyCtl() {
		return bodyCtl;
	}

	public void setBodyCtl(String bodyCtl) {
		this.bodyCtl = bodyCtl;
	}

	@Override
	public String toString() {
		return "IF: " + id + " @ " + line + " " + fileName + " " + methodName
				+ "() : " + varName + ", " + varType + ", " + lassAss + ", " + isParam
				+ ", " + inCondNum + ", " + bodyUse + ", " + inIf;
	}
	
	public boolean inThisIfStmt(int id, int line, String fileName, String mtdName){
		return this.id == id && this.line == line && this.fileName.equals(fileName) && this.methodName.equals(mtdName) ; 
	}
	
	public boolean pointsToSameVariable(ExprInfoCSVItem exprItem){
		return this.id == exprItem.getId() && this.line == exprItem.getLine() && this.column == exprItem.getColumn() &&
				this.fileName.equals(exprItem.getFileName()) && 
				this.methodName.equals(exprItem.getMethodName()) &&
				this.varName.equals(exprItem.getVarName()) && 
				this.varType.equals(exprItem.getVarType()) &&
				this.lassAss.equals(exprItem.getLassAss()) &&
				this.dis0 == exprItem.getDis0() &&
				this.preassnum == exprItem.getPreassnum() &&
				this.isParam == exprItem.isParam() &&
				this.isfld == exprItem.isIsfld() &&
				this.isfnl == exprItem.isIsfnl() &&
				this.inloop == exprItem.isInloop() &&
				this.bodyUse.equals(exprItem.getBodyUse()) &&
				this.outuse.equals(exprItem.getOutuse()) &&
				this.bodyCtl.equals(exprItem.getBodyCtl());
	}
}
