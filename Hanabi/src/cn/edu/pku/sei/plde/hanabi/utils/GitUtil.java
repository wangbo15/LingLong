package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.File;

public class GitUtil {
	
	private static final boolean VERBOSE = true;
	
	public static void resetAndCleanRepo(File repo) {
		CmdUtil.runCmd("git checkout .", repo, VERBOSE);
		CmdUtil.runCmd("git clean -fd", repo, VERBOSE);
		CmdUtil.runCmd("git clean -fX", repo, VERBOSE);
	}
	
	public static void gitCheckout(String branchName, File repo) {
		CmdUtil.runCmd("git checkout " + branchName, repo, VERBOSE);
	}
	
	public static void gitCheckout(String branchName, File repo, boolean verbose) {
		CmdUtil.runCmd("git checkout " + branchName, repo, verbose);
	}
}
