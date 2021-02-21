package edu.pku.sei.conditon.encoding;

import java.util.LinkedHashSet;

public class Type2Int {
	public static String[] simpleType = {"byte", "short", "int", "long", 
			"float", "double", "boolean", "char", "byte[]", "short[]", "int[]", "long[]", 
			"float[]", "double[]", "boolean[]", "String"};
	
	public static String[] retType = {"DEFAULT", "RET"};
	
	public static LinkedHashSet<String> allTypeSet = new LinkedHashSet<String>();
	
	public static LinkedHashSet<String> allExceptionSet = new LinkedHashSet<String>();
	
	
	static{
		for(String s : simpleType){
			allTypeSet.add(s);
		}
		
		for(String s : retType){
			allExceptionSet.add(s);
		}
	}
		
	public static int getLocation(String s){
		allTypeSet.add(s);
		int i = 0;
		for(String t : allTypeSet){
			if(t.equals(s)){
				return i;
			}
			i++;
		}
		
		try {
			throw new Exception("ERROR OF TYPE ENCODING");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static int getExceLocation(String s){
		allExceptionSet.add(s);
		int i = 0;
		for(String t : allExceptionSet){
			if(t.equals(s)){
				return i;
			}
			i++;
		}
		
		try {
			throw new Exception("ERROR OF EXCEPTION ENCODING");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}
