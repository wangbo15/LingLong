package edu.pku.sei.conditon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 5, 2017
 * 
 * Class used to collect java source files under a path. 
 * Note that the path can be a folder or a file.
 */
public class SubjectsUtil {

	public static List<File> getFileList(String strPath, ArrayList<File> filelist) {
		if(strPath == null){
			return filelist;
		}
		
		File dir = new File(strPath);
				
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (files[i].isDirectory()) {
						getFileList(files[i].getAbsolutePath(), filelist);
					} else if (fileName.endsWith(".java")) {
						String strFileName = files[i].getAbsolutePath();
						// System.out.println("\t" + strFileName);
						filelist.add(files[i]);
					} else {
						continue;
					}
				}
			}
		} else {
			filelist.add(dir);
		}
		return filelist;
	}
	
	public static List<File> getCsvFileList(String strPath, ArrayList<File> filelist) {
		File dir = new File(strPath);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (files[i].isDirectory()) {
						getFileList(files[i].getAbsolutePath(), filelist);
					} else if (fileName.endsWith(".csv")) {
						String strFileName = files[i].getAbsolutePath();
						// System.out.println("\t" + strFileName);
						filelist.add(files[i]);
					} else {
						continue;
					}
				}
			}
		} else {
			filelist.add(dir);
		}
		return filelist;
	}
	
}
