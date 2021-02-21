package edu.pku.sei.conditon.simple.expr;

public class TypeFilter {
	public static String filtType(String tp){
		//filt type parmas likes : T,E,K,V
		if(tp.equals("T") || tp.equals("E") || tp.equals("K") || tp.equals("V")){
			return "Object";
		}
		tp = tp.replaceAll("\\<[\\w\\?]\\>", "");
//		System.out.println(tp);
		return tp;
	}
	
	
}
