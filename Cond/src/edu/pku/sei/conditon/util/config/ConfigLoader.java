package edu.pku.sei.conditon.util.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 5, 2017
 */
public class ConfigLoader {
	
	public static void getBugConfigure(Map<String, BugInfo> d4jBugs){
		File file = new File("python/bug.conf");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			BugInfo info = null;
			while((line = reader.readLine()) != null){
				if(line.startsWith("[")){
					info = new BugInfo();
					String key = line.substring(1, line.length() - 1);
					info.setKey(key);
					d4jBugs.put(key, info);
				}else if(line.startsWith("src_root")){
					String home = System.getProperty("user.home");   
					
					info.setSrcRoot(home + "/" + line.split("=")[1].trim());
				}else if(line.startsWith("file_path")){
					String allPath = line.split("=")[1].trim();
					List<String> paths = new ArrayList<>();
					for(String s : allPath.split("\\;")){
						paths.add(s);
					}
					info.setBuggyFile(paths);
				}else if(line.startsWith("line")){
					String allLine = line.split("=")[1].trim();
					List<Integer> lines = new ArrayList<>();
					for(String s : allLine.split("\\;")){
						lines.add(new Integer(s));
					}
					info.setBuggyLine(lines);
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void getDefects4JSubjectsInfo(List<Subject> subjects){
				
		SAXReader reader = new SAXReader();
		File file = new File("config/defect4j_config2.xml");
		Document document;
		try {
			document = reader.read(file);
			Element root = document.getRootElement();
			List<Element> childElements = root.elements();
			for (Element child : childElements) {
				int id = new Integer(child.attributeValue("id"));
				Subject sub = new Subject(child.attributeValue("name"), id, child.elementText("path"), child.elementText("test_path"));
				subjects.add(sub);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public static Subject getSingleDefects4JSubject(String project, int bugID){
		
		Subject res = null;
		
		SAXReader reader = new SAXReader();
		File file = new File("config/defect4j_config.xml");
		Document document;
		try {
			document = reader.read(file);
			Element root = document.getRootElement();
			List<Element> childElements = root.elements();
			for (Element child : childElements) {
				String name = child.attributeValue("name");
				int id = new Integer(child.attributeValue("id"));
				if(project.equals(name) && id == bugID){
					res = new Subject(name, id, child.elementText("path"), child.elementText("test_path"));
					break;
				}

			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		assert res != null;
		return res;
	}

	private static void loadConfigFile(List<Subject> subjects, String path) {
		SAXReader reader = new SAXReader();
		File file = new File(path);
		Document document;
		try {
			document = reader.read(file);
			Element root = document.getRootElement();
			List<Element> childElements = root.elements();
			for (Element child : childElements) {
				Subject sub = new Subject(child.attributeValue("name"), child.elementText("path"), child.elementText("root"));
				subjects.add(sub);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}		
	}
	
	public static void getOtherSubjectsInfo(List<Subject> subjects) {
		loadConfigFile(subjects, "config/github_config.xml");
	}
	
	public static void getBugsDotJarSubjectsInfo(List<Subject> subjects) {
		loadConfigFile(subjects, "config/bugs_dot_jar_config.xml");
	}
	
	
	public static void main(String[] args) {
		List<Subject> subjects  = new ArrayList<>();
		getDefects4JSubjectsInfo(subjects);
		
		getOtherSubjectsInfo(subjects);
		
		for(Subject s : subjects){
			System.out.println("NAME: " + s.getName() + " 	ID: " + s.getId());
			System.out.println("PATH: " + s.getPath());
			System.out.println("TEST: " + s.getTestPath());

			System.out.println("ROOT: " + s.getRoot());
			System.out.println();

		}
		
	}

	
}
