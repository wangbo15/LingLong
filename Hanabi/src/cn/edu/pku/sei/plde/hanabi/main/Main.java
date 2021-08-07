package cn.edu.pku.sei.plde.hanabi.main;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.D4jProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.D4jTestRunner;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fixer.MLConditionFixer;
import cn.edu.pku.sei.plde.hanabi.fl.ASMFaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.FaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.GZoltarFaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.utils.Logger;
import edu.pku.sei.conditon.util.DateUtil;
import edu.pku.sei.conditon.util.FileUtil;

public class Main {
	
	/**
	 * @param args:
	 * database: d4j (defects4j) | bdj (bugs.jar)
	 * proj: math
	 * id: 3
	 * d4j -> purify | bdj -> subModule
	 */
	public static void main(String[] args){
		
		String dataeBase = args[1];
		assert "d4j".equals(dataeBase) || "bdj".equals(dataeBase);
		
		String proj = args[2];
		int id = Integer.valueOf(args[3]);
		
		boolean success = false;
		long start = System.currentTimeMillis();
		if("d4j".equals(dataeBase)) {
			boolean purify = false;
			if(args.length == 4) {
				purify = Boolean.valueOf(args[4]);
			}
			success = fixDefects4J(proj, id, purify);
		} else {
			String subModule = "";
			if(args.length == 4) {
				subModule =args[4];
			}
			success = fixBugsDotJar(proj, id, subModule);
		}
		long end = System.currentTimeMillis();
		String time = DateUtil.millisecToTimeStr(end - start);

		System.out.println("###########################################");
		if(success) {
			System.out.println("------ " + proj.toUpperCase() + "_" + id + " FIX SUCCESS !!");
		} else {
			System.out.println("!!!!!! " + proj.toUpperCase() + "_" + id + " FIX FAILED !!");
		}
		System.out.println("################ ALL USED TIME: " + time);

	}
	
	private static boolean fix(ProjectConfig projectConfig, TestRunner runner, FaultLocation fl) {
		String proj = projectConfig.getProjectName().toUpperCase();
		long allStart = System.currentTimeMillis();
		Logger.recordMassage(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		Logger.recordTime("ALL\tSTART\t" + proj, 0);

		MLConditionFixer fixer = new MLConditionFixer(projectConfig, runner);
		
		List<Suspect> suspects = fl.getAllSuspects();
		
		long flEnd = System.currentTimeMillis();
		Logger.recordTime("FL\tUSED\t" + proj, flEnd - allStart);

		boolean fixedAll = fixer.fixAllSespcts(suspects);
		
		long allEnd = System.currentTimeMillis();
		
		Logger.recordTime("FIX\tUSED\t" + proj, allEnd - flEnd);
		Logger.recordTime("ALL\tFINISH\t" + proj, allEnd - allStart);
		
		return fixedAll;
	}
	
	/*******************************************************************************************/
	/********************************** FOR DEFECTS4J ******************************************/
	/*******************************************************************************************/
	
	public static boolean fixDefects4J(String proj, int bugId){
		return fixDefects4J(proj, bugId, false);
	}
	
	public static boolean fixDefects4J(String proj, int bugId, boolean purify){
		ProjectConfig projectConfig = D4jProjectConfig.getProjConfig(proj, bugId, purify);
		TestRunner runner = new D4jTestRunner(projectConfig);
		FaultLocation fl = new GZoltarFaultLocation(projectConfig);
		
		return fix(projectConfig, runner, fl);
	}
	
	/*******************************************************************************************/
	/********************************** FOR BUGS.JAR *******************************************/
	/*******************************************************************************************/

	public static boolean fixBugsDotJar(String proj, int issueID) {
		return fixBugsDotJar(proj, issueID, "");
	}
	
	/**
	 * @param proj
	 * @param issueID
	 * @param subModule: Fix sub-module, like 'camel-core'
	 * @return
	 */
	public static boolean fixBugsDotJar(String proj, int issueID, String subModule) {
		ProjectConfig projectConfig;
		if(subModule == null || subModule.length() == 0) {
			projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		} else {
			projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, subModule);
		}

		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		FaultLocation fl = new ASMFaultLocation(projectConfig, runner);
		return fix(projectConfig, runner, fl);
	}
		
}
