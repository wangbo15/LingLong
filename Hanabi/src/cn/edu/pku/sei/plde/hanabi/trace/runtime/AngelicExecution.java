package cn.edu.pku.sei.plde.hanabi.trace.runtime;

import java.util.ArrayList;
import java.util.List;

public class AngelicExecution {
	
    public static List<Boolean> previousValue = new ArrayList<>();

    private static boolean enabled = false;
    private static boolean booleanValue = false;
    
    
    //Because of the option can not figured out by ProcessBuilder, so change to args of main function
//    static {
//    	//trap of Boolean.getBoolean !!!
//    	//the method gets the property of system evn, only if the property equals to "ture", the method return true.
//		enabled = Boolean.getBoolean("sgfix.angelic.enable");		//SET BY CMD java -Dsgfix.angelic.enable="true"
//		booleanValue = Boolean.getBoolean("sgfix.angelic.boolval");	//SET BY CMD java -Dsgfix.angelic.boolval="true"
//    }
    
	public static void enable() {
        enabled = true;
        previousValue = new ArrayList<>();
    }
	
	public static void disable() {
        enabled = false;
        previousValue = new ArrayList<>();
    }
	
	public static boolean getAngelicValue() {
        previousValue.add(getBooleanValue());
        return getBooleanValue();
	}
	
	public static boolean getAngelicValue(boolean condition) {//CALLED BY INSTRUMENTER
		
		//System.out.println("ENABLED: " + isEnabled() + "  BOOL: " + getBooleanValue() + "  ORICOND: " + condition);
		
        if (isEnabled()) {
            previousValue.add(getBooleanValue());
            return getBooleanValue();
        }
        previousValue.add(condition);
        return condition;
    }
	
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void setBooleanValue(boolean b){
    	booleanValue = b;
    }
    
    public static boolean getBooleanValue() {
        return booleanValue;
    }

}
