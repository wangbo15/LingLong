package edu.pku.sei.conditon.util.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.csvreader.CsvReader;

import edu.pku.sei.conditon.util.SubjectsUtil;

public class CSVChecker {
	
//	public static void main(String[] args){
//		checkAllCSV("/home/nightwish/tmp/res/math_3.var.csv");
//	}
	
	public static boolean checkAllCSV(String... paths){
		boolean res = true;
		for(String s : paths){
			res = res && checkAllCSV(s);
		}
		return res;
	}
	
	public static boolean checkAllCSV(String resRootPath){
		ArrayList<File> filelist = new ArrayList<File>();
		SubjectsUtil.getCsvFileList(resRootPath, filelist);
//		System.out.println(filelist.size());
		for(File f : filelist){
			try {
				System.out.println("\n====== PARSING CVS : " + f.getName() + " ======");
				CsvReader reader = new CsvReader(f.getAbsolutePath(), '\t');
				reader.readHeaders();
				int headerNum = reader.getHeaderCount();
				int ln = 0;
				while(reader.readRecord()){
					if(headerNum != reader.getColumnCount()){
						throw new Error(f.getName() + " @LINE: " + ln + " , HEADER " + headerNum + " != DATACOL " + reader.getColumnCount() + " => " + reader.getRawRecord());
					}
					ln++;
				}
				System.out.println("====== CORRECT : " + f.getName() + " ======\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
}
