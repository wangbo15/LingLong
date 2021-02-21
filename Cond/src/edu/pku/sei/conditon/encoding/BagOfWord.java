package edu.pku.sei.conditon.encoding;

import java.util.ArrayList;
import java.util.Arrays;

public class BagOfWord {
	
	public static final int STEP_LEN = 2;
	
	public static ArrayList<String> camelCaseNaming(String s){
		char array[] = s.toCharArray();
		ArrayList<String> res = new ArrayList<String>();

		if(Character.isUpperCase(array[0])){
			res.addAll(Arrays.asList(s.split("_")));
			return res;
		}
		
		if(s.length() == 1){
			res.add(s);
			return res;
		}
		
		for(int i = 0; i < array.length - 1; i++){
			if(! Character.isLetter(array[i])){
				continue;
			}
						
			int j = i + 1;
			for(; j < array.length; j++){
				if(Character.isUpperCase(array[j])){
					break;
				}

			}
			
			
			String word = new String(array, i, j - i);
			res.add(word);
			i = j - 1;

		}
		
		return res;
	}
	
	
	public static BitVector parse(String word){
		word = word.replace("_", "");
		word = word.toLowerCase();
		BitVector res = new BitVector();
		
		if(word.length() < STEP_LEN){
			res.insertUncomplete(word.charAt(0));
			return res;
		}
		
		char array[] = word.toCharArray();
		
		for(int i = 0; i <= array.length - STEP_LEN; i++){
			char bag[] = new char[STEP_LEN];
			for(int j = 0; j < STEP_LEN; j++){
				bag[j] = array[i + j];
			}
			System.out.println("BAG: " + new String(bag));
			res.insert(bag);
			
		}
		
		return res;
	}
	
	
	public static void main(String args[]){
		String test[] = {"getStaticName", "MAX_INT_VALUE", "a", "index" , "MIN", "a0"};
		for(String s : test){
//			for(String word : camelCaseNaming(s)){
//				System.out.println("CURRENT WORD: " + word);
//				BitVector bv = parse(word);
//				System.out.println("BV: " + bv.toString());
//			}
			BitVector bv = parse(s);
			System.out.println("BV: " + bv.toString());
			
			System.out.println("VECTOR: " + bv.toByteArray());
		}
		
	}
	
}
