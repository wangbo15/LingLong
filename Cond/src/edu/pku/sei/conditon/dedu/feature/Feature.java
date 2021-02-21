package edu.pku.sei.conditon.dedu.feature;

import java.io.Serializable;
import java.util.List;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.util.FeatureConfiger;

public abstract class Feature implements Serializable {
	
	public static final String NONE = AbstractDeduVisitor.NONE;
	
	public static String genFeatureHeaderFromList(String key) {
		List<FeatureItem> items = FeatureConfiger.getFeatureConfigMap().get(key);
		StringBuffer sb = new StringBuffer();
		for(FeatureItem item : items){
			if(item.isCollected()){
				sb.append(item.getFeatureName());
				sb.append("\t");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
