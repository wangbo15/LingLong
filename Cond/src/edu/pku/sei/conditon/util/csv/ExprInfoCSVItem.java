package edu.pku.sei.conditon.util.csv;

public class ExprInfoCSVItem {
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
	
	private String bodyUse;
	private String outuse;
	private String bodyCtl;
	
	private boolean hasElse;
	
	private String right;
		
	
	public ExprInfoCSVItem(int id, int line, int column, String fileName, String methodName, String varName,
			String varType, String right) {
		super();
		this.id = id;
		this.line = line;
		this.column = column;
		this.fileName = fileName;
		this.methodName = methodName;
		this.varName = varName;
		this.varType = varType;
		this.right = right;
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
	public boolean isHasElse() {
		return hasElse;
	}
	public void setHasElse(boolean hasElse) {
		this.hasElse = hasElse;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}

	public String getLassAss() {
		return lassAss;
	}

	public void setLassAss(String lassAss) {
		this.lassAss = lassAss;
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

	public boolean isParam() {
		return isParam;
	}

	public void setParam(boolean isParam) {
		this.isParam = isParam;
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

	public String getBodyUse() {
		return bodyUse;
	}

	public void setBodyUse(String bodyUse) {
		this.bodyUse = bodyUse;
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
		return "ExprInfoCSVItem [id=" + id + ", line=" + line + ", fileName=" + fileName + ", methodName=" + methodName
				+ ", varName=" + varName + ", varType=" + varType + ", lassAss=" + lassAss + ", dis0=" + dis0
				+ ", preassnum=" + preassnum + ", isParam=" + isParam + ", isfld=" + isfld + ", isfnl=" + isfnl
				+ ", inloop=" + inloop + ", bodyUse=" + bodyUse + ", outuse=" + outuse + ", bodyCtl=" + bodyCtl
				+ ", hasElse=" + hasElse + ", right=" + right + "]";
	}
	
}
