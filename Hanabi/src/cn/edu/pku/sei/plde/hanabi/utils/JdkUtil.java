package cn.edu.pku.sei.plde.hanabi.utils;

public class JdkUtil {
	
	public static Class<?> stringToClazz(String jdkClazz) {
		Class<?> cls = null;
		try {
			cls = Class.forName(jdkClazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return cls;
	}
	
	public static boolean isRunTimeExeception(String exception) {
		if(exception == null || exception.length() == 0) {
			return false;
		}
		if(exception.equals("java.lang.AssertionError")) {
			return false;
		}
		try {
			Class<?> exceCls = Class.forName(exception);
			if(RuntimeException.class.isAssignableFrom(exceCls)) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			//e.printStackTrace();
		} 
		return false;
	}
	
}
