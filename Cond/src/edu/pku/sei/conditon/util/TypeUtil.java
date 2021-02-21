package edu.pku.sei.conditon.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;

public class TypeUtil {
	
	public static boolean isStrLiteral(String str) {
		return str.startsWith("\"") && str.endsWith("\"");
	}
	
	public static boolean is64BitInt(String type) {
		switch (type) {
		case "long":
		case "Long":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean is32BitInt(String type) {
		switch (type) {
		case "int":
		case "Integer":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPrimitiveIntType(String type){
		switch (type) {
		case "int":
		case "char":
		case "short":
		case "long":
		case "byte":
		case "Integer":
		case "Long":
		case "Character":
		case "Short":
		case "Byte":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPrimitiveFloatType(String type){
		switch (type) {
		case "float":
		case "double":
		case "Double":
		case "Float":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPrimitiveNumType(String type) {
		switch (type) {
		case "int":
//		case "char":
		case "short":
		case "long":
		case "float":
		case "double":
		case "byte":
		case "Integer":
		case "Long":
		case "Double":
		case "Float":
		case "Character":
		case "Short":
		case "Byte":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPrimitiveType(String type) {
		switch (type) {
		case "int":
		case "char":
		case "short":
		case "long":
		case "float":
		case "double":
		case "boolean":
		case "byte":
		case "Integer":
		case "Long":
		case "Double":
		case "Float":
		case "Character":
		case "Short":
		case "Boolean":
		case "Byte":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPurePrimitiveType(String type) {
		switch (type) {
		case "int":
		case "char":
		case "short":
		case "long":
		case "float":
		case "double":
		case "boolean":
		case "byte":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isStringClassType(String type){
		switch (type) {
		case "String":
		case "java.lang.String":
		case "StringBuffer":
		case "java.lang.StringBuffer":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isJavaLangOrJavaUtilType(String type) {
		switch (type) {
		case "Appendable":
		case "AutoCloseable":
		case "CharSequence":
		case "Cloneable":
		case "Comparable":
		case "Iterable":
		case "Readable":
		case "Runnable":
		case "Thread.UncaughtExceptionHandler":
		case "Boolean":
		case "Byte":
		case "Character":
		case "Character.Subset":
		case "Character.UnicodeBlock":
		case "Class":
		case "ClassLoader":
		case "ClassValue":
		case "Compiler":
		case "Double":
		case "Enum":
		case "Float":
		case "InheritableThreadLocal":
		case "Integer":
		case "Long":
		case "Math":
		case "Number":
		case "Object":
		case "Package":
		case "Process":
		case "ProcessBuilder":
		case "ProcessBuilder.Redirect":
		case "Runtime":
		case "RuntimePermission":
		case "SecurityManager":
		case "Short":
		case "StackTraceElement":
		case "StrictMath":
		case "String":
		case "StringBuffer":
		case "StringBuilder":
		case "System":
		case "Thread":
		case "ThreadGroup":
		case "ThreadLocal":
		case "Throwable":
		case "Void":
		case "Character.UnicodeScript":
		case "ProcessBuilder.Redirect.Type":
		case "Thread.State":
		case "ArithmeticException":
		case "ArrayIndexOutOfBoundsException":
		case "ArrayStoreException":
		case "ClassCastException":
		case "ClassNotFoundException":
		case "CloneNotSupportedException":
		case "EnumConstantNotPresentException":
		case "Exception":
		case "IllegalAccessException":
		case "IllegalArgumentException":
		case "IllegalMonitorStateException":
		case "IllegalStateException":
		case "IllegalThreadStateException":
		case "IndexOutOfBoundsException":
		case "InstantiationException":
		case "InterruptedException":
		case "NegativeArraySizeException":
		case "NoSuchFieldException":
		case "NoSuchMethodException":
		case "NullPointerException":
		case "NumberFormatException":
		case "ReflectiveOperationException":
		case "RuntimeException":
		case "SecurityException":
		case "StringIndexOutOfBoundsException":
		case "TypeNotPresentException":
		case "UnsupportedOperationException":
		case "Errors":
		case "AbstractMethodError":
		case "AssertionError":
		case "BootstrapMethodError":
		case "ClassCircularityError":
		case "ClassFormatError":
		case "Error":
		case "ExceptionInInitializerError":
		case "IllegalAccessError":
		case "IncompatibleClassChangeError":
		case "InstantiationError":
		case "InternalError":
		case "LinkageError":
		case "NoClassDefFoundError":
		case "NoSuchFieldError":
		case "NoSuchMethodError":
		case "OutOfMemoryError":
		case "StackOverflowError":
		case "ThreadDeath":
		case "UnknownError":
		case "UnsatisfiedLinkError":
		case "UnsupportedClassVersionError":
		case "VerifyError":
		case "VirtualMachineError":
		case "Annotation Types":
		case "Deprecated":
		case "Override":
		case "SafeVarargs":
		case "SuppressWarnings":

			// java.lang.ref
		case "PhantomReference":
		case "Reference":
		case "ReferenceQueue":
		case "SoftReference":
		case "WeakReference":

			// java.util
			// Interface Summary
		case "List":
		case "Map":
		case "Entry":
		case "Queue":
		case "Set":
		case "SortedMap":
		case "SortedSet":
			// Class Summary
		case "ArrayList":
		case "Arrays":
		case "Calendar":
		case "Date":
		case "HashMap":
		case "HashSet":
		case "Hashtable":
		case "LinkedList":
		case "Stack":
		case "Vector":
			return true;
		default:
			return false;
		}
	}

	public static String removeGenericType(String tp){
		if(tp.contains("<") && tp.contains(">")){
			tp = tp.split("<")[0];
		}
		return tp;
	}
	
	public static boolean isPackageType(String tp) {
		switch (tp) {
		case "Integer":
		case "Double":
		case "Float":
		case "Character":
		case "Long":
		case "Short":
		case "Boolean":
		case "Byte":
			return true;
		default:
			return false;
		}
	}
	
	
	public static String getPackageType(String tp) {
		switch (tp) {
		case "int":
			return "Integer";
		case "double":
			return "Double";
		case "float":
			return "Float";
		case "char":
			return "Character";
		case "long":
			return "Long";
		case "short":
			return "Short";
		case "boolean":
			return "Boolean";
		case "byte":
			return "Byte";
		default:
			return tp;
		}
	}
	
	public static String getPrimitiveType(String tp) {
		switch (tp) {
		case "Integer":
			return "int";
		case "Double":
			return "double";
		case "Float":
			return "float";
		case "Character":
			return "char";
		case "Long":
			return "long";
		case "Short":
			return "short";
		case "Boolean":
			return "boolean";
		case "Byte":
			return "byte";
		default:
			return tp;
		}
	}
	
	
	public static boolean mayMatchingTypeVariable(String tp, VariableInfo info){
		tp = removeGenericType(tp);
			
//		//maybe constant
//		if (ASTLocator.maybeConstant(info.getNameLiteral()) || Character.isUpperCase(info.getNameLiteral().charAt(0))){
//			continue;
//		}
		
		String infoTp = removeGenericType(info.getType());
		
		if(isPrimitiveType(tp) != isPrimitiveType(infoTp)) {
			return false;
		}
		
		if (tp.equalsIgnoreCase("float") || tp.equalsIgnoreCase("double")) {
			if (infoTp.equalsIgnoreCase("float") || infoTp.equalsIgnoreCase("double") || infoTp.equalsIgnoreCase("long") || infoTp.equalsIgnoreCase("int")) {
				return true;
			}

		} else if (tp.equals("int") || tp.equals("Integer")) {
			if (infoTp.equals("int") || infoTp.equals("Integer") || infoTp.equals("long") || infoTp.equals("Long")) {
				return true;
			}

		} else if(tp.equalsIgnoreCase("long")){
			if(infoTp.equals("int") || infoTp.equals("Integer") || infoTp.equalsIgnoreCase("long")){
				return true;
			}
		} else if(tp.equals("char") || tp.equals("Character")){
			if(infoTp.equals("char") || infoTp.equals("Character")){
				return true;
			}
		} else if(infoTp.equals(tp)){
			return true;
		}
		
		return false;
	}
	
	public static boolean isLegalVarAtPosition(String expr, int position, VariableInfo info, Map<String, OriPredItem> allOriPreds) {
		String litTmp = ExprGenerator.getProcessedExpr(expr);
		boolean isPrim = TypeUtil.isPurePrimitiveType(info.getType());
		if(litTmp.equals("$==null") || litTmp.equals("$!=null")){
			if(isPrim){
				return false;
			}
			return true;
		}
		OriPredItem oriPredItem = allOriPreds.get(litTmp);
		
		if(oriPredItem == null) {
			return false;
		}
		
		//assert oriPredItem != null: "ILLEGAL ORIPREDITEM: " + (expr + " " + position + " " + info);
		assert oriPredItem.getPosToTypesMap() != null;
		
		Set<String> types = oriPredItem.getPosToTypesMap().get(position);
		if(types == null) {
			return false;
		}
		for(String t: types){
			t = TypeUtil.removeGenericType(t);
			String infoTp = TypeUtil.removeGenericType(info.getType());
			if(t.equals(infoTp)) {//if(TypeUtil.matchingTypeVariable(t, info))
				return true;
			}
		}
		return false;
	}
	
    public static boolean isTailDouble(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+[dD]");
        
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
	
    public static boolean isSmpleDouble(String str) {
    	if(isTailDouble(str)) {
    		return true;
    	}
    	
        Pattern pattern = Pattern.compile("-?[0-9]+\\.[0-9]*(E-)?[0-9]*");
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;
        }
        Matcher isNum = pattern.matcher(bigStr);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
    
    public static boolean isSimpleNum(String str) {
         try {
             new BigDecimal(str).toString();
         } catch (Exception e) {
             return false;
         }
         return true;
    }
    
    /**
     * @param num
     * @return true, if num is like '.5'
     */
    private static boolean isDotSingleNum(String num) {
    	return num.length() == 2 && num.charAt(0) == '.' && Character.isDigit(num.charAt(1));
    }
    
    public static String modifyUselessFormat(String num) {
    	if(num.toLowerCase().endsWith(".0d") || num.toLowerCase().endsWith(".0f")) {
    		int idx = num.length() - 3;
    		return num.substring(0, idx);
    	}else if(num.endsWith(".") || num.endsWith("0d") || num.endsWith("1d") || num.endsWith("2d") ||
    			num.endsWith("0f") || num.endsWith("1f") || num.endsWith("2f") 
    			|| num.toLowerCase().endsWith("0l") || num.toLowerCase().endsWith("1l") || num.toLowerCase().endsWith("2l")) {
    		int idx = num.length() - 1;
    		return num.substring(0, idx);
    	}else if (num.toLowerCase().endsWith(".0")) {
    		int idx = num.length() - 2;
    		return num.substring(0, idx);
    	} else if (isDotSingleNum(num)) {
    		return "0" + num;
    	}
    	return num;
    }
    
}
