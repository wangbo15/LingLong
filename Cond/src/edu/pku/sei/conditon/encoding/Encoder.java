package edu.pku.sei.conditon.encoding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import edu.pku.sei.conditon.util.SubjectsUtil;


public class Encoder {
	
	public static void main(String[] args){
		String src = "/home/linglong/tmp/res/math_3_buggy.csv";
		String out = "/home/linglong/tmp/res/encode/";
		String fname = "math_3_buggy.csv";
		csvEncode(src, out);
	}
	
	
	public static void csvEncode(String resRootPath, String outRootPath){
		ArrayList<File> filelist = new ArrayList<File>();
		SubjectsUtil.getCsvFileList(resRootPath, filelist);
		
		CsvWriter writer = null;
		try {
			writer = new CsvWriter(new FileWriter(new File(outRootPath + "math_3_buggy.csv")), '\t');
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		
		for(File f : filelist){
			try {
				System.out.println("====== ENCODING-BEGIN CSV : " + f.getName() + " ======");
				CsvReader reader = new CsvReader(f.getAbsolutePath(), ',');
				
				
				reader.readHeaders();
				//String[] header = reader.getHeaders(); 
				int headerNum = reader.getHeaderCount();
				String[] record = new String[headerNum];
				while(reader.readRecord()){
					StringBuffer sb = new StringBuffer();
					record[0] = BagOfWord.parse(reader.get("methodname")).toByteArray();
					record[1] = BagOfWord.parse(reader.get("varname")).toByteArray();
					record[2] = new Integer(Type2Int.getLocation(reader.get("vartype"))).toString();
					
					if(reader.get("else").equals("true")){
						record[3] = "1";
					}else{
						record[3] = "0";
					}
					
					record[4] = new Integer(Predicate2Int.getLocation(reader.get("oper"))).toString();
					
					record[5] = new Integer(Predicate2Int.parseForSimple(reader.get("right"))).toString();

					record[6] = new Integer(Type2Int.getExceLocation(reader.get("return"))).toString();
					//record[5] = new Integer(Assignment2Int.parse(reader.get("operation"))).toString();

					//record[6] = new Integer(Type2Int.getExceLocation(reader.get("return"))).toString();
					
					writer.writeRecord(record);
					
					writer.flush();
				}
				System.out.println("====== ENCODING-FINISH : " + f.getName() + " ======\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			
			try {
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		writer.close();
		
	}
}
