package edu.pku.sei.conditon.test;
/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Mar 31, 2017
 */
public class LocalTest {
	
	void foo(){
		int j;
		if(true){
			int i;
			if(false){}
		}
		
		int i;
		
		if(false)
			return;
	}
	
	void f1(String p1){
		String str;
		while( (str = p1) != null){//can be done
			
		}
		
	}
	
}
