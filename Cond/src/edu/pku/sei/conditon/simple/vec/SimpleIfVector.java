package edu.pku.sei.conditon.simple.vec;

import java.util.Queue;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

import edu.pku.sei.conditon.ds.MethodInfo;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 18, 2017
 */
public class SimpleIfVector implements Comparable<SimpleIfVector>{
	//file and method of the ifstmt
	protected long ifStmtID = -1;
	protected String fileName;
	protected String filePath;
	protected MethodInfo methodInfo;
	protected String methodName;
	
	//the location in the file
	protected int lineNumber;
	protected int columnNumber;
	
	//the conditon literal
	protected String ifCond;
	
	protected boolean inLoop = false;

	protected String bodyCtl = "DEF";
	
	public static final int BEFORE_SYNTACTIC_NUM = 6;
	public static final int AFTER_SYNTACTIC_NUM = 4;

	protected Queue<String> beforeSyntacticQueue;
	protected Queue<String> afterSyntacticQueue;
	
	public SimpleIfVector(long id, String fileName, String filePath,  MethodInfo mtdInfo, IfStatement ifstmt, boolean inLoop, String bodyCtl){
		this.ifStmtID = id;
		this.fileName = fileName;
		this.filePath = filePath;
		this.methodInfo = mtdInfo;
		
		CompilationUnit cu = (CompilationUnit) ifstmt.getRoot();
		this.lineNumber = cu.getLineNumber(ifstmt.getStartPosition());
		this.columnNumber = cu.getColumnNumber(ifstmt.getStartPosition());
		
		this.methodName = methodInfo.getName();
		
		this.ifCond = ifstmt.getExpression().toString();
		this.inLoop = inLoop;
		this.bodyCtl = bodyCtl;
	}
	
	//TODO:: refractor SplIfStmtVisitor
	public SimpleIfVector(){
		
	}
	
	public long getIfStmtID() {
		return ifStmtID;
	}

	public void setIfStmtID(long ifStmtID) {
		this.ifStmtID = ifStmtID;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public int getColumnNumber() {
		return columnNumber;
	}
	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getIfCond() {
		return ifCond;
	}
	public void setIfCond(String ifCond) {
		this.ifCond = ifCond;
	}
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}
	public void setMethodInfo(MethodInfo methodInfo) {
		this.methodInfo = methodInfo;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isInLoop() {
		return inLoop;
	}

	public void setInLoop(boolean inLoop) {
		this.inLoop = inLoop;
	}
	
	public String getBodyCtl() {
		return bodyCtl;
	}

	public void setBodyCtl(String bodyCtl) {
		this.bodyCtl = bodyCtl;
	}

	public Queue<String> getBeforeSyntacticQueue() {
		return beforeSyntacticQueue;
	}

	public void setBeforeSyntacticQueue(Queue<String> beforeSyntacticQueue) {
		this.beforeSyntacticQueue = beforeSyntacticQueue;
	}

	public Queue<String> getAfterSyntacticQueue() {
		return afterSyntacticQueue;
	}

	public void setAfterSyntacticQueue(Queue<String> afterSyntacticQueue) {
		this.afterSyntacticQueue = afterSyntacticQueue;
	}

	@Override
	public int compareTo(SimpleIfVector that) {
		if(that == this){
			return 0;
		}
		//1. sort by fileName
		//2. sort by line
		//3. sort by col
		int fileNameCmp = this.fileName.compareTo(that.getFileName());
		if(fileNameCmp != 0){
			return fileNameCmp;
		}else{
			int filePathCmp = this.filePath.compareTo(that.getFilePath());
			if(filePathCmp != 0){
				return filePathCmp;
			}
			
			Integer thisLine = new Integer(this.lineNumber);
			Integer thatLine = new Integer(that.getLineNumber());
			
			if(thisLine.compareTo(thatLine) != 0){
				return thisLine.compareTo(thatLine);
			}else{
				Integer thisCol = new Integer(this.columnNumber);
				Integer thatCol = new Integer(that.getColumnNumber());
				int colCmp = thisCol.compareTo(thatCol);
				
				assert colCmp != 0;
				
				return colCmp;
			}
		}
	}
}
