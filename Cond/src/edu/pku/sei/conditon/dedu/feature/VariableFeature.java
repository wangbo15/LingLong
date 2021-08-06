package edu.pku.sei.conditon.dedu.feature;

import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.util.TypeUtil;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 18, 2017
 */
public class VariableFeature extends Feature {
			
	private static final long serialVersionUID = -183085040510987393L;
	
	private String nameLiteral;
	private String type = NONE;
	
	private boolean isInt = false;
	private boolean isFloat = false;
	private boolean isArray = false;
	private boolean isCollection = false;
	
	private boolean isParam = false;
	private boolean isField = false;
	private boolean isSuperField = false;
	private boolean isFinal = false;
	private boolean isForStmtIndexer = false;
	
	private AssignInfo lastAssign = AssignInfo.getUnknowAssign();
	private int lastAssignDis = Integer.MAX_VALUE;
	
	private int preAssNum = 0;
	
	private String firstUseInBody = NONE;
	
	private String firstUseOutOfStmt = NONE;
	
	//for the if-var pair, specialVal means the occurence time in the condition
	private int specialVal = 0;
	
	private int ifCondNumber = 0;
	private int fileIfCondNumber = 0;
	
	private String previousCond;
	
	/*these docXxx are features extracted from javadoc*/
	private String docExcpiton = AbstractDeduVisitor.NONE;
	private String docOpeartor = AbstractDeduVisitor.NONE;
	private boolean docZero;
	private boolean docOne;
	private boolean docNullness;
	private boolean docRange;
	private boolean inDocCode; //this is previous commented
	
	public VariableFeature(String nameLiteral, String type, boolean isParam, boolean isField, boolean isFinal, boolean isForStmtIndexer){
		this.nameLiteral = nameLiteral;
		this.isParam = isParam;
		this.isField = isField;
		this.isFinal = isFinal;
		this.isForStmtIndexer = isForStmtIndexer;
		this.setType(type);
	}
	
	private void setTypeRelative(){
		String splTp = TypeUtil.removeGenericType(this.type);
		
		if(splTp.equals("int") || splTp.equals("long") || splTp.equals("short") || splTp.equals("char")){
			this.isInt = true;
		} else if(splTp.equals("double") || splTp.equals("float")){
			this.isFloat = true;
		} else if(splTp.endsWith("]")){
			this.isArray = true;
		} else if(splTp.endsWith("List") || splTp.endsWith("Set") || splTp.endsWith("Map")){
			this.isCollection = true;
		}		
	}
	
	
	public String getNameLiteral() {
		return nameLiteral;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(type != null){
			this.type = type;
			this.setTypeRelative();
		}
	}

	public boolean isInt() {
		return isInt;
	}

	public boolean isFloat() {
		return isFloat;
	}

	public boolean isArray() {
		return isArray;
	}

	public boolean isCollection() {
		return isCollection;
	}

	public boolean isForStmtIndexer() {
		return isForStmtIndexer;
	}
	
	public int getSpecialVal() {
		return specialVal;
	}

	public void setSpecialVal(int specialVal) {
		this.specialVal = specialVal;
	}
		
	public int getIfCondNumber() {
		return ifCondNumber;
	}

	public void setIfCondNumber(int ifCondNumber) {
		this.ifCondNumber = ifCondNumber;
	}

	public int getFileIfCondNumber() {
		return fileIfCondNumber;
	}

	public void setFileIfCondNumber(int fileIfCondNumber) {
		this.fileIfCondNumber = fileIfCondNumber;
	}

	public boolean isParam() {
		return isParam;
	}

	public boolean isField() {
		return isField;
	}
	
	public boolean isSuperField() {
		return isSuperField;
	}


	public void setSuperField(boolean isSuperField) {
		this.isSuperField = isSuperField;
	}


	public boolean isFinal() {
		return isFinal;
	}

	public AssignInfo getLastAssign() {
		return lastAssign;
	}

	public void setLastAssign(AssignInfo lastAssign) {
		this.lastAssign = lastAssign;
	}

	
	public int getPreAssNum() {
		return preAssNum;
	}


	public void setPreAssNum(int preAssNum) {
		this.preAssNum = preAssNum;
	}


	public String getFirstUseInBody() {
		return firstUseInBody;
	}

	public void setFirstUseInBody(String firstUseInBody) {
		this.firstUseInBody = firstUseInBody;
	}	
	
	public String getFirstUseOutOfStmt() {
		return firstUseOutOfStmt;
	}

	public void setFirstUseOutOfStmt(String firstUseOutOfStmt) {
		this.firstUseOutOfStmt = firstUseOutOfStmt;
	}

	public int getLastAssignDis() {
		return lastAssignDis;
	}

	public void setLastAssignDis(int lastAssignDis) {
		this.lastAssignDis = lastAssignDis;
	}
	
	public String getPreviousCond() {
		return previousCond;
	}

	public void setPreviousCond(String previousCond) {
		this.previousCond = previousCond;
	}

	public String getDocExcpiton() {
		return docExcpiton;
	}

	public void setDocExcpiton(String docExcpiton) {
		this.docExcpiton = docExcpiton;
	}

	public String getDocOpeartor() {
		return docOpeartor;
	}

	public void setDocOpeartor(String docOpeartor) {
		this.docOpeartor = docOpeartor;
	}

	public boolean isDocZero() {
		return docZero;
	}

	public void setDocZero(boolean docZero) {
		this.docZero = docZero;
	}

	public boolean isDocOne() {
		return docOne;
	}

	public void setDocOne(boolean docOne) {
		this.docOne = docOne;
	}

	public boolean isDocNullness() {
		return docNullness;
	}

	public void setDocNullness(boolean docNullness) {
		this.docNullness = docNullness;
	}

	public boolean isDocRange() {
		return docRange;
	}

	public void setDocRange(boolean docRange) {
		this.docRange = docRange;
	}

	public boolean isInDocCode() {
		return inDocCode;
	}

	public void setInDocCode(boolean inDocCode) {
		this.inDocCode = inDocCode;
	}

	@Override
	public String toString() {
		return "(" + nameLiteral + ", TP= " + type + ", FLD: " + isField + ")";
	}

	private static String cache;
	public static String getFeatureHeader() {
		if(cache != null) {
			return cache;
		}
		cache = Feature.genFeatureHeaderFromList("var");
		return cache;
	}
}
