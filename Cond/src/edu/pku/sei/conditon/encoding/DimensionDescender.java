package edu.pku.sei.conditon.encoding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class DimensionDescender {
	
	
	public static boolean[] EXISITED_BIT = new boolean[BitVector.VEC_SIZE];
	
	public static int MTD_NAME_MAX_LEN = 0;
	
	public static int VAR_NAME_MAX_LEN = 0;

	public static void parseVec(String s, int varOrMth){
		String[] bits = s.split(",");
		int cur_max = 0;
		for(int i = 0; i < bits.length; i++){
			String b = bits[i];
			if(b.equals("1")){
				EXISITED_BIT[i] = true;
				cur_max++;
			}
		}
		if(varOrMth == 0){
			if(cur_max > MTD_NAME_MAX_LEN){
				MTD_NAME_MAX_LEN = cur_max;
			}
		}else if(varOrMth == 1){
			if(cur_max > VAR_NAME_MAX_LEN){
				VAR_NAME_MAX_LEN = cur_max;
			}
		}
	}
	
	public static String descend(String s, int max){
		String[] bits = s.split(",");
		
		StringBuffer res = new StringBuffer();
		//res.append('{');
		
		char separator = ',';
		int outputed = 0;
		for(int i = 0; i < EXISITED_BIT.length; i++){
			if(EXISITED_BIT[i] && bits[i].equals("1")){
				res.append(i);
				res.append(separator);
				outputed++;
			}
		}
		
		for(int i = outputed; i < max; i++){
			res.append("-1");
			res.append(separator);
		}
		
		res.deleteCharAt(res.length() - 1);
		//res.append('}');
		return res.toString();
	}
	
	public static void main(String[] args){
		String outRootPath = "/home/nightwish/tmp/res/encode/";

		String readerPath = outRootPath + "math_3_buggy.csv";
		
		try {
			CsvReader reader = new CsvReader(readerPath, '\t');
			
			while(reader.readRecord()){

				String s0 = reader.get(0);
				
				parseVec(s0, 0);
				
				String s1 = reader.get(1);
				
				parseVec(s1, 1);
			}
			
			reader.close();
			
			int seted = 0;
			for(Boolean b : EXISITED_BIT){
				if(b){
					seted ++ ;
				}
			}
			System.out.println(seted + " / " + EXISITED_BIT.length);
			System.out.println(MTD_NAME_MAX_LEN + " " + VAR_NAME_MAX_LEN);
			
			reader = new CsvReader(readerPath, '\t');
			CsvWriter writer = new CsvWriter(new FileWriter(new File(outRootPath + "math_3_buggyd.csv")), ',');
			
			writer.setUseTextQualifier(false);
			
//			System.out.println(writer.getTextQualifier());
			
			while(reader.readRecord()){
				int col = reader.getColumnCount();
				String[] record = new String[col];

				
				String s0 = reader.get(0);
				
				record[0] = descend(s0, MTD_NAME_MAX_LEN);
				
				String s1 = reader.get(1);
				
				record[1] = descend(s1, VAR_NAME_MAX_LEN);
				
				record[2] = reader.get(2);
				
				record[3] = reader.get(3);
				
				record[4] = reader.get(4);
				
				record[5] = reader.get(5);
				
				record[6] = reader.get(6);
				
				//writer.writeRecord(record);
				
				writer.writeRecord(record, false);

				writer.flush();
			}

			writer.close();
			
			System.out.println("FINISH");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
