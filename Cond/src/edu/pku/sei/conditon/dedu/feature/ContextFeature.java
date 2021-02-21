package edu.pku.sei.conditon.dedu.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ContextFeature extends Feature {

	private static final long serialVersionUID = -6438020769164049519L;
	
	private File file;
	private String fileName;
	private String tdName;

	private String mtdName;
	private int mtdModifier = -1;
	private int mtdLineNum = 0;
	
//	private List<VariableInfo> allVariables;
	
	private boolean inLoop;
	private String bodyCtl;
	
	public static final int BEFORE_SYNTACTIC_NUM = 6;
	public static final int BODY_SYNTACTIC_NUM = 4;
	public static final int AFTER_SYNTACTIC_NUM = 4;

	public static final int NEAREST_LOCAL_NUM = 4;
	
	private Queue<String> beforeSyntacticQueue;
	private List<String> befSynList;
	private String allBefStr;
	
	private Queue<String> bodySyntacticQueue;
	private List<String> bdSynList;
	private String allBdStr;
	
	private Queue<String> afterSyntacticQueue;
	private List<String> afSynList;
	private String allAfStr;

	private String previousStmt0;
	private String previousStmt1;

	private String nextStmt0;
	private String nextStmt1;

	private String previousCond = NONE;
	private String previousPred = NONE;
	
	
	public ContextFeature(File file, String tdName, String mtdName, int mtdModifier, int mtdLineNum, boolean inLoop) {
		this.file = file;
		this.fileName = file.getName();
		this.tdName = tdName;
		this.mtdName = mtdName;
		this.mtdModifier = mtdModifier;
		this.mtdLineNum = mtdLineNum;
		this.inLoop = inLoop;
	}

	public File getFile() {
		return file;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getTdName() {
		return tdName;
	}

	public String getMtdName() {
		return mtdName;
	}

	public int getMtdModifier() {
		return mtdModifier;
	}
	
	public int getMtdLineNum() {
		return mtdLineNum;
	}

//	public List<VariableInfo> getAllVariables() {
//		return allVariables;
//	}

	public boolean isInLoop() {
		return inLoop;
	}

	public String getBodyCtl() {
		return bodyCtl;
	}

	public void setBodyCtl(String bodyCtl) {
		this.bodyCtl = bodyCtl;
	}

	public List<String> getBefSynList() {
		if(befSynList != null){
			return befSynList;
		}
		befSynList = new ArrayList<String>(BEFORE_SYNTACTIC_NUM);
		for(int i = 0; i < BEFORE_SYNTACTIC_NUM; i++){
			if(! beforeSyntacticQueue.isEmpty()){
				befSynList.add(beforeSyntacticQueue.poll());
			}else{
				befSynList.add(NONE);
			}
		}
		beforeSyntacticQueue.clear();
		beforeSyntacticQueue = null;
		return befSynList;
	}

	public List<String> getBdSynList() {
		if(bdSynList != null){
			return bdSynList;
		}
		bdSynList = new ArrayList<String>(BODY_SYNTACTIC_NUM);
		for(int i = 0; i < BODY_SYNTACTIC_NUM; i++){
			if(! bodySyntacticQueue.isEmpty()){
				bdSynList.add(bodySyntacticQueue.poll());
			}else{
				bdSynList.add(NONE);
			}
		}
		bodySyntacticQueue.clear();
		bodySyntacticQueue = null;
		return bdSynList;
	}

	public List<String> getAfSynList() {
		if(afSynList != null){
			return afSynList;
		}
		afSynList = new ArrayList<String>(AFTER_SYNTACTIC_NUM);
		for(int i = 0; i < AFTER_SYNTACTIC_NUM; i++){
			if(! afterSyntacticQueue.isEmpty()){
				afSynList.add(afterSyntacticQueue.poll());
			}else{
				afSynList.add(NONE);
			}
		}
		afterSyntacticQueue.clear();
		afterSyntacticQueue = null;
		return afSynList;
	}

	public void setBeforeSyntacticQueue(Queue<String> beforeSyntacticQueue) {
		this.allBefStr = beforeSyntacticQueue.toString();
		this.beforeSyntacticQueue = beforeSyntacticQueue;
	}

	public void setBodySyntacticQueue(Queue<String> bodySyntacticQueue) {
		this.allBdStr = bodySyntacticQueue.toString();
		this.bodySyntacticQueue = bodySyntacticQueue;
	}


	public void setAfterSyntacticQueue(Queue<String> afterSyntacticQueue) {
		this.allAfStr = afterSyntacticQueue.toString();
		this.afterSyntacticQueue = afterSyntacticQueue;
	}
	
	public String getAllBefStr() {
		return allBefStr;
	}

	public String getAllBdStr() {
		return allBdStr;
	}

	public String getAllAfStr() {
		return allAfStr;
	}

	public String getPreviousStmt0() {
		return previousStmt0;
	}

	public void setPreviousStmt0(String previousStmt0) {
		this.previousStmt0 = previousStmt0;
	}

	public String getNextStmt0() {
		return nextStmt0;
	}

	public void setNextStmt0(String nextStmt0) {
		this.nextStmt0 = nextStmt0;
	}
	
	public String getPreviousStmt1() {
		return previousStmt1;
	}

	public void setPreviousStmt1(String previousStmt1) {
		this.previousStmt1 = previousStmt1;
	}

	public String getNextStmt1() {
		return nextStmt1;
	}

	public void setNextStmt1(String nextStmt1) {
		this.nextStmt1 = nextStmt1;
	}

	public String getPreviousCond() {
		return previousCond;
	}

	public void setPreviousCond(String previousCond) {
		this.previousCond = previousCond;
	}
	
	public String getPreviousPred() {
		return previousPred;
	}

	public void setPreviousPred(String previousPred) {
		this.previousPred = previousPred;
	}

	public static String getFeatureHeader() {
		return Feature.genFeatureHeaderFromList("context");
	}
	
}
