package edu.pku.sei.conditon.util;

import java.math.BigDecimal;

public class MathUtil {
	
	public static double getLog10(BigDecimal val) {
		double d = val.doubleValue();
		return Math.log10(d);
	}
	
	public static double getLog10(double val) {
		return Math.log10(val);
	}
}
