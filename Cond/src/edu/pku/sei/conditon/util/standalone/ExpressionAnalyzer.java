package edu.pku.sei.conditon.util.standalone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.csvreader.CsvReader;

import edu.pku.sei.boparser.JavaParser;
import edu.pku.sei.boparser.Token;
import edu.pku.sei.conditon.dedu.DeduMain;

public class ExpressionAnalyzer {

	private static int totalExprNum = 0;
	private static int totalTokenNum = 0;
	
	private static List<Integer> totalMedianList = new ArrayList<>();
	
	private static String[] projects = {"math_106", "lang_65", "time_27", "chart_26"};
	
	private static void dump(int exprNum, int tokenNum, List<Integer> list) {
		System.out.println("EXPR NUM: " + exprNum + " TOKEN: " + tokenNum);
		double agv = ((double) tokenNum )/exprNum;
		System.out.println("AVG: " + String.format("%.2f", agv));
		int idx = list.size() / 2;
		int median = list.get(idx);
		System.out.println("MED: " + median);
	}
	
	public static int analyzeExpr(String str){
		List<Token> list;
		try {
			list = JavaParser.parseExpression(str);
			//JavaParser.dump();
			return list.size();
		} catch (edu.pku.sei.boparser.ParseException e) {
			//e.printStackTrace();
		}
		return 0;
	}
	
	
	public static void analyzeFullCsv(File file) {
		CsvReader reader;
		try {
			reader = new CsvReader(file.getAbsolutePath(), '\t');
			reader.readHeaders();
			int exprNum = 0;
			int tokenNum = 0;
			List<Integer> list = new ArrayList<>();
			while(reader.readRecord()) {
				String exprLiteral = reader.get("cond");
				//System.out.println(exprLiteral);
				int res = analyzeExpr(exprLiteral);
				if(res > 0) {
					exprNum++;
					tokenNum += res;
					list.add(res);
				}
				
//				if(res < 3) {
//					System.out.println(exprLiteral);
//				}
			}
			Collections.sort(list);
			dump(exprNum, tokenNum, list);
			totalExprNum += exprNum;
			totalTokenNum += tokenNum;
			totalMedianList.addAll(list);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		for(String project : projects) {
			System.out.println("Analyzing " + project);
			File f = new File(DeduMain.OUTPUT_ROOT + project + ".full.csv");
			analyzeFullCsv(f);
		}
		
		System.out.println(">>>> FINAL RESULT: ");
		Collections.sort(totalMedianList);
		dump(totalExprNum, totalTokenNum, totalMedianList);
	}
}
