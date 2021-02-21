package edu.pku.sei.conditon.dedu.feature;

public class FeatureItem {
	
	private String featureName;
	private String featureType;
	private boolean collected;
	private boolean trained;
	private boolean bagged;
	
	public FeatureItem(String featureName, String featureType, boolean collected, boolean trained, boolean bagged) {
		this.featureName = featureName;
		this.featureType = featureType;
		this.collected = collected;
		this.trained = trained;
		this.bagged = bagged;
	}

	public String getFeatureName() {
		return featureName;
	}
	
	public String getFeatureType() {
		return featureType;
	}
	
	public boolean isCollected(){
		return collected;
	}
	
	public boolean isTrained() {
		return trained;
	}
	
	public boolean isBagged() {
		return bagged;
	}

	@Override
	public String toString() {
		return "FeatureItem [featureName=" + featureName + ", featureType=" + featureType + ", collected=" + collected
				+ ", trained=" + trained + ", bagged=" + bagged + "]";
	}

}
