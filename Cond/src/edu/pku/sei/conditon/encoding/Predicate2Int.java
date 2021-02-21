package edu.pku.sei.conditon.encoding;

import java.util.LinkedHashSet;

public class Predicate2Int {
	
	public static String[] preTypes = {"==", "!=", ">", "<", ">=", "<=", "&&", "||", "&", "|", "equals"};
	
	public static LinkedHashSet<String> allPreSet = new LinkedHashSet<String>();

	
	static{
		for(String s : preTypes){
			allPreSet.add(s);
		}
	}
	
	public static int getLocation(String s){
		int i = 0;
		for(String t : preTypes){
			if(t.equals(s)){
				return i;
			}
			i++;
		}
		return preTypes.length;
	}
	
	public static int parse(String pres){
		pres = pres.substring(1, pres.length() - 1);

		String presArr[] = pres.split(";");
		int res = 0;
		for(String pre : presArr){
			for(String op : preTypes){
				if(pre.contains(op)){
					res += getLocation(op);
				}else{
					res += preTypes.length - 1;
				}
			}
			res *= preTypes.length;
		}
		res /= preTypes.length;
		return res;
	}
	
	public static int parseForSimple(String pre){
		allPreSet.add(pre);
		int i = 0;
		for(String t : allPreSet){
			if(t.equals(pre)){
				return i;
			}
			i++;
		}
		
		try {
			throw new Exception("ERROR OF ESPRESSION ENCODING");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
