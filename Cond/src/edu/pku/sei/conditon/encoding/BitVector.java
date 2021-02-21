package edu.pku.sei.conditon.encoding;

public class BitVector {
	/*
	 * 10 digits, 26 letters and 1 null placeholder
	 * 0-9, a-z, null
	 * */
	public static final int ALPH_SIZE = 37;
	public static final int VEC_SIZE = (int) Math.pow(ALPH_SIZE, BagOfWord.STEP_LEN);
	
	public boolean[] vector = new boolean[VEC_SIZE];
	//public Set<String> words;
	
	public void insertUncomplete(char c){
		int idx;
		if(Character.isDigit(c)){
			idx = (c - '0')*ALPH_SIZE + ALPH_SIZE - 1;
		}else{
			idx = (c - 'a' + 10)*ALPH_SIZE + ALPH_SIZE - 1;
		}
		vector[idx] = true;
	}
	
	public void insert(char arr[]){
		int idx = 0;
				
		for(char c : arr){
			if(Character.isDigit(c)){
				idx += (c - '0');
			}else if(Character.isLetter(c)){
				idx += (c - 'a' + 10);
			}else{
				idx = idx + ALPH_SIZE - 1; 
			}
//			System.out.println(">> " + idx);
			idx *= ALPH_SIZE;
		}
		
		idx /= ALPH_SIZE;
		System.out.println(new String(arr) + ": " + idx);
		vector[idx] = true;

		
	}
	
	public static String idxToString(int idx){	
		StringBuffer res = new StringBuffer();
		do{
			int ch = idx % ALPH_SIZE;
			if(ch < 10){
				res.insert(0, (char)('0' + ch));
			}else if(ch >=10 && ch < 36){
				res.insert(0, (char)('a' + ch - 10));
			}else{
				res.insert(0, '_');
			}
			res.append(' ');
			idx /= ALPH_SIZE;
		}while(idx > 0);
		return res.toString().trim();
	}
	public String toByteArray(){
		StringBuffer sb = new StringBuffer();
		//sb.append('<');
		for(int i = 0 ; i < vector.length; i++){
			if(vector[i]){
				sb.append("1,");
			}else{
				sb.append("0,");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		//sb.append('>');
		return sb.toString();
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < vector.length; i++){
			if(vector[i]){
				sb.append(idxToString(i));
				sb.append(' ');
			}
		}
		return sb.toString().trim();
	}
	
}
