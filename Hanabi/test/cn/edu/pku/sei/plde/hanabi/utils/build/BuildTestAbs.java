package cn.edu.pku.sei.plde.hanabi.utils.build;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import cn.edu.pku.sei.plde.hanabi.utils.build.BuildUtil;

public abstract class BuildTestAbs {
	
	public static final boolean checkKeySet(Set<String> keySet){
		Field[] fields = BuildUtil.class.getFields();
		
		Set<String> fieldValues = new HashSet<>();
		for(Field f : fields) {
			try {
				Object instance = f.get(null);
				if(instance instanceof String) {
					String str = (String) instance;
					fieldValues.add(str);
				}
				
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		
		for(String str : fieldValues) {
			if(!keySet.contains(str)) {
				return false;
			}
		}
		
		return true;
	}
	
}
