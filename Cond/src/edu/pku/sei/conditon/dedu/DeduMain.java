package edu.pku.sei.conditon.dedu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.pku.sei.conditon.auxiliary.StatisticVisitor;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.PROCESSING_TYPE;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.SubjectsUtil;
import edu.pku.sei.conditon.util.config.ConfigLoader;
import edu.pku.sei.conditon.util.config.Subject;
import edu.pku.sei.proj.ProInfo;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Feb 27, 2017
 */
public class DeduMain {
	
	private static Logger logger = Logger.getLogger(DeduMain.class);
	
	//TODO: move to configfile
	public static final String DEFECTS4J_ROOT = "/home/nightwish/workspace/defects4j/src/";
				
	public static final String OUTPUT_ROOT = "/home/nightwish/tmp/res/";
	public static final String TEST_OUTPUT_ROOT = "/home/nightwish/tmp/test/";
	
	private static String subjectSrcPath = "";
	
	public static String JAVA_VERSION = JavaCore.VERSION_1_7;
	private static ProInfo proInfo;
	
	private static PROCESSING_TYPE missionType = ConditionConfig.getInstance().getProcessType();
		
	public static void resetVisitors(){
		
		DeduConditionVisitor.reset();
		StatisticVisitor.reset();
		
		subjectSrcPath = "";
		JAVA_VERSION = JavaCore.VERSION_1_7;
		proInfo = null;
	}
	
	public static void setAndSaveProjInfo(String bugName, String filePath, String fileTestPath){
		proInfo = new ProInfo(bugName , filePath, fileTestPath, JAVA_VERSION);
		proInfo.collectProInfo2();

		String dumpPath = OUTPUT_ROOT + bugName + ".pro";
		
		try {
			File file = new File(dumpPath);
			if(file.getParentFile().exists() == false){
				file.getParentFile().mkdirs();
			}
			
			FileOutputStream fs = new FileOutputStream(dumpPath);
			ObjectOutputStream os =  new ObjectOutputStream(fs);
			os.writeObject(proInfo);
			
			fs.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Subject processProcessForPredAll(String proj, int bug) {
		List<Subject> subjects  = new ArrayList<>();
		ConfigLoader.getDefects4JSubjectsInfo(subjects);
		for(Subject subject : subjects){			
			String subName = subject.getName();
			int bugId = subject.getId();
			
			if(subName.equalsIgnoreCase(proj) && bugId == bug){
				processDefects4JSubjects(subject);
				return subject;
			}
		}
		return null;
	}
	
	public static void forEachJavaFile(ArrayList<File> srcFileList, Class<?> visitorCls, String output){
				
		for(File javaSrcFile : srcFileList){
			
			if(!javaSrcFile.getName().endsWith(".java")) {
				continue;
			}
						
			if(javaSrcFile.getName().equals("package-info.java")) {
				continue;
			}
			
			String absPath = javaSrcFile.getAbsolutePath();
			String code = JavaFile.readFileToString(javaSrcFile);
			CompilationUnit cu = (CompilationUnit) JavaFile.genASTFromSourceWithType(code, ASTParser.K_COMPILATION_UNIT, absPath, JAVA_VERSION, subjectSrcPath);
			
			
			char[] rawSource = code.toCharArray();
			
			ASTVisitor visitor;
			try {
								
				if (visitorCls.equals(DeduConditionVisitor.class)) {

					visitor = new DeduConditionVisitor(cu, javaSrcFile, rawSource, proInfo);

				} else if (visitorCls.equals(StatisticVisitor.class)){
					
					if(javaSrcFile.getName().contains("Test")) {
						continue;
					}
					
					visitor = new StatisticVisitor(cu, javaSrcFile, proInfo);
					
					/*
					String fileTokens = JavaParser.lexer(javaSrcFile);
					FileUtil.writeStringToFile(output + ".token.txt", fileTokens, true);
					*/
					
				} else {
					throw new Error("UNKNOWN VISITOR CLASS");
				}
				
				cu.accept(visitor);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
		}//END for(File javaSrcFile : srcFileList)
		

		if (visitorCls.equals(StatisticVisitor.class)) {
			
			StatisticVisitor.dump(output);
			
		}  else if (visitorCls.equals(DeduConditionVisitor.class)) {
			
			DeduConditionVisitor.writeVectors(output);
		}
	}
	
	
	private static void processDefects4JSubjects(Subject subject) {
		double startTime = System.currentTimeMillis();
		
		resetVisitors();
					
		String subName = subject.getName();
		int bugId = subject.getId();
		
		if(subName.equals("lang") && bugId >= 42){
			JAVA_VERSION = JavaCore.VERSION_1_3;
		}else{
			JAVA_VERSION = JavaCore.VERSION_1_7;
		}
		
		String bugName = subName + "_" + bugId;
		
		String filePath = DEFECTS4J_ROOT + subName + "/" + bugName + "_buggy" + subject.getPath();
		
		String fileTestPath = DEFECTS4J_ROOT + subName + "/" + bugName + "_buggy" + subject.getTestPath();
				
		subjectSrcPath = filePath;
		
		setAndSaveProjInfo(bugName , filePath, fileTestPath);

		String outprefix = OUTPUT_ROOT + bugName; 
		File tokenFile = new File(outprefix + ".token.txt");
		if(tokenFile.exists()) {
			tokenFile.delete();
		}
		
		ArrayList<File> srcFileList = new ArrayList<File>(128);
		
		SubjectsUtil.getFileList(filePath, srcFileList);
		
		ArrayList<File> testFileList = new ArrayList<File>(128);
		SubjectsUtil.getFileList(fileTestPath, testFileList);
					
		logger.info(subName.toUpperCase() + "_" + bugId + " TOTAL JAVA FILE : " + srcFileList.size() + "  TOTAL TEST FILE : " + testFileList.size());
		
		srcFileList.addAll(testFileList);

		//1, statistic pass, get the total condition time of simpleNames and tokens of all files
		forEachJavaFile(srcFileList, StatisticVisitor.class, outprefix);
		
		forEachJavaFile(srcFileList, DeduConditionVisitor.class, outprefix);
		
		srcFileList.clear();
		testFileList.clear();
		
		double endTime = System.currentTimeMillis();
		
		logger.info(subName.toUpperCase() + "_" + bugId + " USED TIME: " + (endTime - startTime)/1000 + " s" );
	}
	
	private static void processDefects4J(){
		
		assert missionType == PROCESSING_TYPE.D4J;
		
		List<Subject> subjects  = new ArrayList<>();
		
		ConfigLoader.getDefects4JSubjectsInfo(subjects);
		
		for(Subject subject : subjects){
					
			//resetVisitors();
			
			String subName = subject.getName();
			int bugId = subject.getId();
			
			if(!(subName.equalsIgnoreCase("chart") && bugId == 16)){
				continue;
			}
			
			processDefects4JSubjects(subject);
		}
	}
	
	private static void processBugsDotJar() {
		assert missionType == PROCESSING_TYPE.BUS_DOT_JAR;
		List<Subject> subjects  = new ArrayList<>();

		ConfigLoader.getBugsDotJarSubjectsInfo(subjects);
		
		for(Subject subject : subjects){
			
			if(!(subject.getName().startsWith("camel_3690"))) {
				continue;
			}
			
			String root = System.getProperty("user.home") + "/" + subject.getRoot();
			
			// TODO: automatically checkout
			//checkoutToBranch(root, subject.getName(), subject.getId());
			
			double startTime = System.currentTimeMillis();
			
			resetVisitors();

			String subName = subject.getName();
			String filePath = root + subject.getPath();
			subjectSrcPath = filePath;
			
			ArrayList<File> srcFileList = new ArrayList<File>(500);
			
			SubjectsUtil.getFileList(filePath, srcFileList);
			
			logger.info(subName.toUpperCase() + " TOTAL JAVA FILE : " + srcFileList.size());

			String outprefix = OUTPUT_ROOT;
			
			File outFolder = new File(outprefix);
			if(!outFolder.exists()) {
				outFolder.mkdirs();
			}
			
			outprefix += subName;
			
			setAndSaveProjInfo(subject.getName(), System.getProperty("user.home") + "/" + subject.getRoot(), null);
			
			forEachJavaFile(srcFileList, StatisticVisitor.class, outprefix);
						
			forEachJavaFile(srcFileList, DeduConditionVisitor.class, outprefix);
			
			srcFileList.clear();

			double endTime = System.currentTimeMillis();
			
			logger.info(subName.toUpperCase() + " USED TIME: " + (endTime - startTime)/1000 + " s" );
			
			// MUST BREAK
			break;
		}

	}

	
	private static void processOtherProject() {
		
		assert missionType == PROCESSING_TYPE.GIT_REPOS;
		
		List<Subject> subjects  = new ArrayList<>();
		ConfigLoader.getOtherSubjectsInfo(subjects);
		for(Subject subject : subjects){
			
			if(!subject.getName().equals("jdk7_math"))	//jdk7_math, JSci
				continue;
			
			double startTime = System.currentTimeMillis();
			
			resetVisitors();
			
			String subName = subject.getName();
			String filePath = System.getProperty("user.home") + "/" + subject.getRoot() + subject.getPath();
			subjectSrcPath = filePath;
			
			//for debug
//			filePath = "/home/nightwish/workspace/analogic_projects/jdk_1.7/java/awt/image/AreaAveragingScaleFilter.java";

			ArrayList<File> srcFileList = new ArrayList<File>(500);
			
			SubjectsUtil.getFileList(filePath, srcFileList);

			logger.info(subName.toUpperCase() + " TOTAL JAVA FILE : " + srcFileList.size());
			
			String outprefix = OUTPUT_ROOT + "github/";
			
			File outFolder = new File(outprefix);
			if(!outFolder.exists()) {
				outFolder.mkdirs();
			}
			
			outprefix += subName;
			
			/*forEachJavaFile(srcFileList, VariableVisitor.class, varOutput);*/

			setAndSaveProjInfo(subject.getName(), System.getProperty("user.home") + "/" + subject.getRoot(), null);
			
			forEachJavaFile(srcFileList, StatisticVisitor.class, outprefix);
						
			forEachJavaFile(srcFileList, DeduConditionVisitor.class, outprefix);
			
			srcFileList.clear();

			double endTime = System.currentTimeMillis();
			
			logger.info(subName.toUpperCase() + " USED TIME: " + (endTime - startTime)/1000 + " s" );

		}
	}
	
	public static void main(String[] args){
		
		double startTime = System.currentTimeMillis();
		
		if(missionType == PROCESSING_TYPE.D4J) {
			processDefects4J();
		} else if(missionType == PROCESSING_TYPE.BUS_DOT_JAR) {
			processBugsDotJar();
		} else {
			processOtherProject();
		}
		
		double endTime = System.currentTimeMillis();
		
		logger.info("ALL USED TIME: " + (endTime - startTime)/1000 + " s" );
		
	}
}
