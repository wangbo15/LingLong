package edu.pku.sei.conditon.util.config;

import java.util.List;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 5, 2017
 */
public class Subject {
	private String name;
	private int id;
	
	//for defects4j
	private String path;		
	//for github
	private String root;
	
	
	//test case root path
	private String testPath;
	//relative subjects for training
	private List<Subject> relatedSubjects;
	
	//for defects4j
	public Subject(String name, int id, String path, String testPath){
		this.name = name;
		this.id = id;
		this.path = path;
		this.testPath = testPath;
	}
	
	//for github
	public Subject(String name, String path, String root){
		this.name = name;
		this.path = path;
		this.root = root;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}
	
	public String getTestPath() {
		return testPath;
	}

	public void setTestPath(String testPath) {
		this.testPath = testPath;
	}

	public List<Subject> getRelatedSubjects() {
		return relatedSubjects;
	}

	public void setRelatedSubjects(List<Subject> relatedSubjects) {
		this.relatedSubjects = relatedSubjects;
	}

	@Override
	public String toString() {
		return "Subject [ " + name + "_" + id + ", src=" + path + ", root="+ root + " ]";
	}
		
}
