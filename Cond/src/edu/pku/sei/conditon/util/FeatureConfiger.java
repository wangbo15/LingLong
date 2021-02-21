package edu.pku.sei.conditon.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.pku.sei.conditon.dedu.feature.FeatureItem;


public class FeatureConfiger {
	
	private static Map<String, List<FeatureItem>> featureConfigMap;
	
	public static Map<String, List<FeatureItem>> getFeatureConfigMap(){
		if(featureConfigMap == null){
			featureConfigMap = loadFeatureConfigFile();
		}
		return featureConfigMap;
	}
	
	private static Map<String, List<FeatureItem>> loadFeatureConfigFile() {
		final Map<String, List<FeatureItem>> result = new HashMap<>();
		SAXBuilder saxBuilder = new SAXBuilder();
		InputStream in = null;
		InputStreamReader isr = null;
		try {
			in = new FileInputStream("config/feature.xml");
			isr = new InputStreamReader(in, "UTF-8");
			Document document = saxBuilder.build(isr);
			Element rootElement = document.getRootElement();
			List<Element> features = rootElement.getChildren();
			for (Element feature : features) {
				List<FeatureItem> curTypeFeatureList = null;
				String featureType = feature.getName();
				if (result.containsKey(featureType)) {
					curTypeFeatureList = result.get(featureType);
				} else {
					curTypeFeatureList = new ArrayList<>();
					result.put(featureType, curTypeFeatureList);
				}

				String name = null;
				String type = null;
				boolean collected = false;
				boolean trained = false;
				boolean bagged = false;
				List<Element> featureChilds = feature.getChildren();
				for (Element child : featureChilds) {
					if (child.getName().equals("name")) {
						name = child.getValue();
					} else if (child.getName().equals("type")) {
						type = child.getValue();
					} else if (child.getName().equals("trained")) {
						trained = new Boolean(child.getValue());
					} else if (child.getName().equals("collected")) {
						collected = new Boolean(child.getValue());
					} else if (child.getName().equals("price")) {
						bagged = new Boolean(child.getValue());
					}
				}
				FeatureItem featureItem = new FeatureItem(name, type, collected, trained, bagged);
				curTypeFeatureList.add(featureItem);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(in, isr);
		}
		return result;
	}

}
