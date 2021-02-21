package edu.pku.sei.conditon.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regexpr {
	
	private static boolean isValidClsFullName(String cls){
		String regex = "^(org|com).*";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(cls);
		return matcher.find();
	}
	
	private static void exprNormal(String expr){
		for(String s : expr.split("\\=\\="))
			System.out.println(s);
		
	}
	
	private static void varName(String var){
		String regex = "[a-zA-Z]*";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(var);
		System.out.println(matcher.group());
	}
	
	private static void numeral(String var) {
		String regex = "\\b1\\b";
		Pattern pattern = Pattern.compile(regex); 
		Matcher matcher = pattern.matcher(var);
		System.out.println(matcher.find());
	}
	
	public static void main(String[] args){
//		String s1 = "org.apache.ant";
//		String s2 = "com.google.clour";
//		String s3 = "apache.com";
//		
//		System.out.println(isValidClsFullName(s1)  + " " + isValidClsFullName(s2) + " " + isValidClsFullName(s3));
//		
//		exprNormal("$==1");
//		
//		varName("_mean");
		
		numeral("1.");
		numeral("10.");

	} 
}
