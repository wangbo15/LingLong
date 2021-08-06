package edu.pku.sei.conditon.dedu.feature;

public class PredicateFeature extends Feature {

	private static final long serialVersionUID = 34830432420614281L;

	private String className = "Object";
	private String currentPred;
	private String rootType;
	private String fatherNodeType;

	private String ariop = NONE;
	private int hight = 0;
	private String firstMtd = NONE;
	private boolean hasInstanceof;
	private String firstNum = NONE;
	private String secondNum = NONE;
	private boolean hasNull;

	public PredicateFeature(String clsName, String currentPred, String rootType) {
		this.className = clsName;
		this.currentPred = currentPred;
		this.rootType = rootType;
	}

	public String getClassName() {
		return className;
	}

	public String getCurrentPred() {
		return currentPred;
	}

	public void setCurrentPred(String currentPred) {
		this.currentPred = currentPred;
	}

	public String getRootType() {
		return rootType;
	}

	public void setRootType(String rootType) {
		this.rootType = rootType;
	}

	public String getAriop() {
		return ariop;
	}

	public void setAriop(String ariop) {
		this.ariop = ariop;
	}

	public int getHight() {
		return hight;
	}

	public void setHight(int hight) {
		this.hight = hight;
	}

	public String getFirstMtd() {
		return firstMtd;
	}

	public void setFirstMtd(String firstMtd) {
		this.firstMtd = firstMtd;
	}

	public boolean isHasInstanceof() {
		return hasInstanceof;
	}

	public void setHasInstanceof(boolean hasInstanceof) {
		this.hasInstanceof = hasInstanceof;
	}

	public String getFirstNum() {
		return firstNum;
	}

	public void setFirstNum(String firstNum) {
		this.firstNum = firstNum;
	}

	public String getSecondNum() {
		return secondNum;
	}

	public void setSecondNum(String secondNum) {
		this.secondNum = secondNum;
	}

	public boolean isHasNull() {
		return hasNull;
	}

	public void setHasNull(boolean hasNull) {
		this.hasNull = hasNull;
	}

	public String getFatherNodeType() {
		return fatherNodeType;
	}

	public void setFatherNodeType(String fatherNodeType) {
		this.fatherNodeType = fatherNodeType;
	}

	private static String cache;

	public static String getFeatureHeader() {
		if (cache != null) {
			return cache;
		}
		cache = Feature.genFeatureHeaderFromList("expr");
		return cache;
	}

}
