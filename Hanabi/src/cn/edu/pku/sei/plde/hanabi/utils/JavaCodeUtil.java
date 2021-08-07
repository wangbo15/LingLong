package cn.edu.pku.sei.plde.hanabi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class JavaCodeUtil {
	
	private static final Pattern  mtdInvocationPattern = Pattern.compile("\\w+\\(.*\\)");
	
	public static String getMtdInvo(String code) {
        Matcher match = mtdInvocationPattern.matcher(code);
        while (match.find()) {
        	String caller = match.group();
        	return caller.substring(0, caller.indexOf("(")).trim();
        }
        return "";
	}
	
	public static String getMethodRetType(MethodDeclaration suspectMtd){
		if(suspectMtd == null) {
			return "";
		}
		if(suspectMtd.isConstructor()) {
			return "";
		}
		String retType = suspectMtd.getReturnType2().toString();
		return retType;
	}
	
	/**
	 * @param classFullName: aaa.bbb.ccc.ClsXxx
	 * @return aaa.bbb.ccc
	 */
	public static String getPackageNameFromClsFullName(String classFullName) {
		assert classFullName != null && !classFullName.trim().equals("");
		if(!classFullName.contains(".")) {
			return classFullName;
		}
		int idx = classFullName.lastIndexOf(".");
		return classFullName.substring(0, idx);
	}
	
	/**
	 * @param cls0: aaa.bbb.ccc.ddd.Cls1
	 * @param cls1: aaa.bbb.ccc.eee.Cls2
	 * @return aaa.bbb.ccc
	 */
	public static String getLongestCommonPackge(String cls0, String cls1) {
		assert cls0 != null && !cls0.trim().equals("");
		assert cls1 != null && !cls1.trim().equals("");
		
		String[] arr0 = cls0.split("\\.");
		String[] arr1 = cls1.split("\\.");
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < arr0.length && i < arr1.length; i++) {
			if(arr0[i].equals(arr1[i])) {
				sb.append(arr0[i]);
				sb.append(".");
			}
		}
		int len = sb.length();
		if(len > 0) {
			sb.deleteCharAt(len - 1);
		}
		return sb.toString();
	}
	
	
	private static final Pattern pkgPattern = Pattern.compile("^[a-z]+(\\.[a-z0-9]+)*[a-z0-9]$");
	/**
	 * Whether {pkg} is a legal package name
	 * @param pkg
	 * @return
	 */
	public static boolean isPackageName(String pkg) {
		if(pkg == null) {
			return false;
		}
		
		Matcher matcher = pkgPattern.matcher(pkg);
		return matcher.find();
	}

	
	private static final Pattern testPattern = Pattern.compile("^[A-Z]+.*Test$");
	/**
	 * @param className, aaa.bbb.ccc.Clazz or Clazz
	 * @return
	 */
	public static boolean isTestCase(String className) {
		if(className == null) {
			return false;
		}
		
		int lastIdx = className.lastIndexOf(".");
		if(lastIdx > 0) {
			String pkg = className.substring(0, lastIdx);
			if(!isPackageName(pkg)) {
				return false;
			}
			
			className = className.substring(lastIdx + 1);
		}
		
		Matcher matcher = testPattern.matcher(className);
		return matcher.find();
	}
}
