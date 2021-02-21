package edu.pku.sei.conditon.test;

import java.util.Iterator;
import java.util.List;


public class Test {
	
	private boolean array[] = new boolean[]{true, false};
	
	private double[] lowerBound;

	private double[] upperBound;
	
	private static boolean staticArray[] = new boolean[]{true, false};

	private static String str = "123456";
	
	int a = 1;
	int b = -1;
	
	private static boolean isObj = true;
	
	private static boolean staticRetBool(){
		return false;
	}
	
	private static boolean checkStr(int i){return false;}

	private boolean retBool(){
		return false;
	}
	
	public void assign(){
		a += str.length();
		
		if(a > 5){
			return;
		}
	}
	
	public void forStmt(List list){
		for(int i = 0; i < 5; i++){
			
		}
		for(Iterator it = list.iterator(); it.hasNext();){
			
		}
		
	}
	
	public void ifStmt(int a, boolean isM, String s, Test t, String[] arr) throws Exception{
		int b = 2;
		//1
		if(a > 0 || b < 6 && isM){
			
		}
		//2
		if(isM){
			
		}
		//3
		if(s.length() > 5){
			
		}
		//4
		if(retBool()){
			
		}
		//5
		if(this.retBool()){
			return;
		}
		//6
		if(staticRetBool()){
			
		}		
		//7
		if(Test.staticRetBool()){
			
		}	
		//8
		if(array[0]){
			
		}
		//9
		if(staticArray[0]){
			
		}
		//10
		if(Test.staticArray[0]){
			return;

		}
		//11
		if(true){}
		//12
		if(s instanceof Object){}
		//13
		if((isM)){}
		//14
		if(edu.pku.sei.conditon.test.Test.str instanceof Object){}
		//15
		if(checkStr(a)){
			
		}
		//16
		if(t.equals(t)){}
		//17
		if(lowerBound != null){
			throw new Exception();
		}
		//18
		if(array.length > 0){
			if(isM){
				throw new Exception();
			}
			return;
		}
		
		if(a*b > 0){}
		
		if(a < -1){return;}
		
		if(arr.length > 0){
			
		}
		
		
	}
	
//	public static void main(String[] args){
//		String s = "    111   \t\n";
//		char c = ' ';
//		s = s.replaceAll(" ", "#SPACE#");
//		s = s.replaceAll("\t", "#TABLE#");
//		s = s.replaceAll("\n", "#NEWLINE#");
//		System.out.println(s);
//	}
//	
	
	public static void main(String[] args){
		double c1 = 1.2;
		double c2 = 0.0;
		double c3 = 1.2;
		c3++;
		System.out.println(c3);
		
		String s = "\"abc\"";
		System.out.println(s);
		System.out.println(s.replaceAll("\"", ""));
	}
}
