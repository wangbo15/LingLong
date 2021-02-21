package edu.pku.sei.conditon.simple.expr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.csvreader.CsvReader;

import edu.pku.sei.conditon.util.StringUtil;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * May 10, 2017
 */

public class ExprNormalization {
	
	private static Set<String> allExprSet = new TreeSet<>();
	
	private static Set<String> allNormalizedExprSet = new TreeSet<>();
	
	private static List<OriginExpr> allOriExprList = new ArrayList<>();
	
	
	private static boolean isPrimitiveIntegerType(String type){
		return type.equals("int") ||  type.equals("long") || type.equals("short") || type.equals("char")
				|| type.equals("Integer") || type.equals("Long") || type.equals("Short") || type.equals("Character");
	}
	
	private static boolean isPrimitiveFloatType(String type){
		return type.equals("double") || type.equals("float") || type.equals("Double") || type.equals("Float");
	}
	
	public static String normalize(String expr, String leftType){
		if(isPrimitiveIntegerType(leftType)){
			
			String regex = "\\$[\\s][\\<|\\>][=][\\s][\\-]?[\\d]+$";
			
			Pattern pattern = Pattern.compile(regex);
			
			Matcher matcher = pattern.matcher(expr);
			
			if(matcher.find()){
				
				String numStr = expr.split("[\\<|\\>][=]?")[1];
				
				long num = new Long(numStr.trim()) - 1;
				
				String val = null;
				if(num <= Integer.MAX_VALUE){
					val = new Integer((int) num).toString();
				}else{
					val = new Long(num).toString();
				}
				
				numStr.replaceAll("[\\d]+", val);
				
				String normalized = null;
				
				if(expr.contains("<=")){
					normalized = StringUtil.connectMulty("< ", expr.split("<=")[0], val);
				}else if(expr.contains(">=")){
					normalized = StringUtil.connectMulty("> ", expr.split(">=")[0], val);
				}else{
					try {
						throw new Exception();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return normalized;
			}
			
			
		}else if(isPrimitiveFloatType(leftType)){
			String regex = "^\\$[\\s][\\<|\\>][=]?[\\s][\\-]?[0-9]*(\\.?)([0-9]*|[0-9]+[d|D]?)$";
			
			Pattern pattern = Pattern.compile(regex);
			
			Matcher matcher = pattern.matcher(expr);
			
			if(matcher.find()){
				String numStr = expr.split("[\\<|\\>][=]?")[1];
				
				double num = new Double(numStr.trim());
				
				String doubleIt = null;
				double eps = 1e-10;
				if(num - Math.floor(num) < eps){//the double is integer value
					doubleIt = new Integer((int) Math.floor(num)).toString();
				}else{
					doubleIt = new Double(num).toString();
				}
				
//				String left = expr.split("[\\<|\\>][=]?")[0];
				String left = expr.split("\\s")[0];
				
				String op = expr.split("\\s")[1];
				
				String normalized = left + " " + op + " " + doubleIt;
				
//				if(expr.equals(normalized) == false){
//					System.out.println("DOUBLE: " + expr + " >>>> " + normalized);
//				}
				
				return normalized;
			}
		}
		
		
		if(isPrimitiveFloatType(leftType) || isPrimitiveIntegerType(leftType)){
			
			if(expr.equals("$ >=1")){
				return isPrimitiveFloatType(leftType) ? "$ >= 1" : "$ > 0";
			}
			if(expr.equals("$ >=0")){
				return isPrimitiveFloatType(leftType) ? "$ >= 0" : "$ > -1";
			}
			
			
			String compactReg = "\\$\\=\\=[^\\s]+";
			Pattern comPat = Pattern.compile(compactReg);
			
			Matcher comMat = comPat.matcher(expr);
			
			if(comMat.find()){
				String[] splt = expr.split("\\=\\=");
				if(splt.length == 2){
					return splt[0] + " == " + splt[1];
				}
			}
			
			String compactReg1 = "\\$\\>[^\\s]+";
			Pattern comPat1 = Pattern.compile(compactReg1);
			
			Matcher comMat1 = comPat1.matcher(expr);
			
			if(comMat1.find()){
				String[] splt = expr.split("\\>");
				if(splt.length == 2){
					return splt[0] + " > " + splt[1];
				}
				
			}
		}
		
		
		return expr;
	}
	
	private static void statistic(OriginExpr oriExpr){
		
		String expr = oriExpr.getExpr();
		
		String norExpr = normalize(expr, oriExpr.getType());
		
		if(expr.equals(norExpr) == false){
			System.out.println(expr + "  ==>>  " + norExpr);
			allNormalizedExprSet.add(norExpr);

		}		
		
	}
	
	
	public static int collectAllExprOfFile(String path){
		
		int allLine = 0;
		
		try {
			File file = new File(path);
			CsvReader reader = new CsvReader(file.getAbsolutePath(), '\t');
			reader.readHeaders();
			
			while(reader.readRecord()){
				int id = new Integer(reader.get("id"));
				String var = reader.get("varname");
				String tp = reader.get("vartype");
				String expr = reader.get("right");
				
				OriginExpr oriExpr = new OriginExpr(id, var, tp, expr);
				
				statistic(oriExpr);
				
				allExprSet.add(expr);
				allOriExprList.add(oriExpr);
				allLine++;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allLine;
	}
	
	public static void main(String[] args){
		int allLine = collectAllExprOfFile("/home/nightwish/tmp/res/math_37.expr.csv");
		DecimalFormat df = new DecimalFormat("#.##");
		System.out.println(allExprSet.size() + " / " + allLine + " = " + df.format((double)allExprSet.size()/allLine));
		System.out.println(allNormalizedExprSet.size() + " / " + allExprSet.size());

	} 
	
}


class OriginExpr{
	private int id;
	private String varName;
	private String type;
	private String expr;
	public OriginExpr(int id, String varName, String type, String expr) {
		this.id = id;
		this.varName = varName;
		this.type = type;
		this.expr = expr;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getExpr() {
		return expr;
	}
	public void setExpr(String expr) {
		this.expr = expr;
	}
	
}
