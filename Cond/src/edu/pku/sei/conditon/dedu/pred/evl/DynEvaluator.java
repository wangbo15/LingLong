package edu.pku.sei.conditon.dedu.pred.evl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import edu.pku.sei.conditon.dedu.pred.evl.compiler.BytecodeClassLoaderBuilder;

public class DynEvaluator {
	
	public static Map<String, String> eval(String qualifiedName, String code){
		Map<String, String> result = new HashMap<>();
		ClassLoader loader = BytecodeClassLoaderBuilder.loaderFor(qualifiedName, code);
		Class<?> newClass;
		try {
			newClass = loader.loadClass(qualifiedName);
			Object newInstance = newClass.newInstance();
			for(Field f : newClass.getDeclaredFields()){
				f.setAccessible(true);
				
				String val = f.get(newInstance).toString();
				if(f.getType().equals(char.class)) {
					result.put(f.getName(), "'" + val + "'");
				} else if(f.getType().equals(String.class)) {
					result.put(f.getName(), "\"" + val + "\"");
				} else {
					result.put(f.getName(), val);
				}
				//System.out.println(f.getName() + " => " + f.get(newInstance).toString());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args){
		String qualifiedName = "Test";
		String code = "public class Test{private static final double ONE = 1d; private static final double MINUS_ONE = -ONE;}";
		eval(qualifiedName, code);
	}
}