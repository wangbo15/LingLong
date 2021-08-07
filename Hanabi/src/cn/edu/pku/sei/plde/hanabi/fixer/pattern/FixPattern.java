package cn.edu.pku.sei.plde.hanabi.fixer.pattern;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.CodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;
import edu.pku.sei.conditon.util.DateUtil;

public abstract class FixPattern {
	
	protected static Logger logger = Logger.getLogger(FixPattern.class);
	
	public enum PatchResult {
		COMPILING_FAIL, TEST_FAIL, SUCCESS
	};
	
	protected final static boolean EXHAUSTE_ALL_PATCH = false;
	
	public enum FixResult{FIX_SUCC, FIX_FAIL, IMPLEMENT_FAIL};
	
	public abstract FixResult implement();
	
	protected abstract boolean compilePatch(File srcClsFile, String patch);

	protected ProjectConfig projectConfig;
	protected Suspect suspect;
	protected int iThSuspect;
	protected TestRunner testRunner;
	protected TestOutput testOutput;
	protected List<TestOutput> allFailTestOutputs;
	protected File srcFile;
	protected File srcBackup;
	protected File testSrcFile;
	protected File srcClsFile;
	
	protected String srcCode;
	protected String testCode;
	protected String[] srcCodeLines;
	
	protected String errLine = "";

	protected List<TestOutput> remainedFailedTests = null;//only recorded when patch success
	
	protected File plauRecoderFile;
	
	/* suspect.getClassName() -> CU */
	protected static Map<String, CompilationUnit> compilationUnitCache = new HashMap<>();
	
	protected static CompilationUnit getCompilationUnitFromCache(String key, String srcCode, String jdkLevel) {
		CompilationUnit cu;
		// opt by cache
		if(compilationUnitCache.containsKey(key)) {
			cu = compilationUnitCache.get(key);
		}else {
			cu = (CompilationUnit) JdtUtil.genASTFromSource(srcCode, jdkLevel, ASTParser.K_COMPILATION_UNIT);
			compilationUnitCache.put(key, cu);
		}
		return cu;
	}
		
	public FixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		this.projectConfig = projectConfig;
		this.suspect = suspect;
		this.iThSuspect = iThSuspect;
		this.testRunner = testRunner;
		this.testOutput = testOutput;
		this.allFailTestOutputs = allFailTestOutputs;

		testSrcFile = new File(projectConfig.getTestSrcRoot() + "/" + testOutput.getFailTestSrcPath());
		assert testSrcFile.exists() : "TEST SOURCE NOT EXISTS: " + testSrcFile.getAbsolutePath();

		srcFile = new File(projectConfig.getSrcRoot() + "/" + suspect.getFile());
		assert srcFile.exists() : "SOURCE NOT EXISTS: " + srcFile.getAbsolutePath();

		srcBackup = FileUtil.copyFile(srcFile.getAbsolutePath(), Config.TEMP_SRC_BACKUP_PATH + "/" + srcFile.getName());
		
		String clsPath = FileUtil.classNameToItsClsPath(projectConfig.getTargetRoot().getAbsolutePath() + "/" + suspect.getClassName());
		srcClsFile = new File(clsPath);
		
		// load this.srcCode and this.srcCodeLines
		loadSrcFile();

		testCode = FileUtil.loadFileStrFromCache(testSrcFile);
		
		if(suspect.getLine() <= srcCodeLines.length) {
			errLine = srcCodeLines[suspect.getLine() - 1].trim();
		}
		
		plauRecoderFile = new File(Config.RESULT_PATH + "/" + projectConfig.getProjectName() + "/all_plau.txt");
	}
	
	public List<TestOutput> getRemainedFailedTests(){
		return remainedFailedTests;
	}
	
	protected void restoreSrcFile() {
		FileUtil.copyFile(srcBackup, srcFile);
	}
	
	protected PatchResult processPatch(File srcClsFile, String patch) {//TODO: change to in-memory compilation
		if(this.remainedFailedTests != null){
			this.remainedFailedTests.clear();
			this.remainedFailedTests = null;
		}
		
		if (!compilePatch(srcClsFile, patch)) {
			return PatchResult.COMPILING_FAIL;
		}

		// single test
		
		long startTime = System.currentTimeMillis();
		testRunner.runSingleTestCMD(testOutput.getFailTest());//TODO:: in the case of timeout
		long endTime = System.currentTimeMillis();
		double execTime = (double)(endTime - startTime)/1000;

		if(execTime > testRunner.getRunSingleTestTimeBudget()) {
			return PatchResult.TEST_FAIL;
		}
		
		if (testRunner.getErrorTestOutput().isEmpty()) {//current failed single test passed
			//logger.info(">>>> " + patch + " CAN PASS: " + testOutput.getFailTest());
			//run full test
			startTime = System.currentTimeMillis();
			List<TestOutput> remainedFailedTests = testRunner.runAllAndGetErrorTestOutput();
			endTime = System.currentTimeMillis();
			execTime = (double) (endTime - startTime)/1000;
			
			if(execTime > testRunner.getRunAllTestsTimeBudget() * 60) {//time out for run all tests
				return PatchResult.TEST_FAIL;
			}
			
			if(remainedFailedTests.isEmpty()) {
				logger.info(">>>> " + patch + " FIX ONE PLACE " + suspect.getClassName() + "#" + suspect.getLine());
				this.remainedFailedTests = remainedFailedTests;
				return PatchResult.SUCCESS;
			}
			
			if(remainedFailedTests.size() >= allFailTestOutputs.size()) {// imported other fail tests	//TODO: acceralate
				return PatchResult.TEST_FAIL;
			}
			
			if(involvedNewFailure(remainedFailedTests)) {
				return PatchResult.TEST_FAIL;
			}
			
			logger.info(">>>> " + patch + " FIX ONE PLACE " + suspect.getClassName() + "#" + suspect.getLine());
			this.remainedFailedTests = remainedFailedTests;
			return PatchResult.SUCCESS;
		} 
		return PatchResult.TEST_FAIL;
	}
	
	private boolean involvedNewFailure(List<TestOutput> remainedFailedTests) {
		for(TestOutput newRes: remainedFailedTests) {
			if(!TestOutput.contains(allFailTestOutputs, newRes)) {//the patch brings some new error tests
				boolean covered = false;
				for(String succTest : this.suspect.getCoveredSuccTest()) {
					String testCls = succTest.split("#")[0];
					String testMtd = succTest.split("#")[1];
					if(newRes.getFailTestMethod().equals(testMtd) && newRes.getFailTestCls().equals(testCls)) {
						System.err.println("LET SUCCESSED TEST FAIL: " + succTest);
						covered = true;
						break;
					}
				}
				if(!covered) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected void recordPatch(String patchComment) {
		String rootPath = projectConfig.getSrcRoot().getAbsolutePath();
		String filePath = srcFile.getAbsolutePath();
		int i;
		for(i = 0; i < rootPath.length() && i < filePath.length(); i++) {
			if(rootPath.charAt(i) != filePath.charAt(i)) {
				break;
			}
		}
		if(filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String name = filePath.substring(i).replaceAll("/", ".");
		
		File recorder = new File(Config.RESULT_PATH + "/" + projectConfig.getProjectName() + "/" + name);
		if (!recorder.getParentFile().exists()) {
			recorder.getParentFile().mkdirs();
		}
		CodeUtil.addCodeToFile(srcFile, patchComment, suspect.getLine());
		FileUtil.copyFile(srcFile, recorder);
		
		forceReloadSrcFile();
	}
	
	protected void recordAllPlauPatch(List<String> patches) {
		if(patches == null || patches.isEmpty()) {
			return;
		}
		StringBuffer sb = new StringBuffer();
		int size = patches.size();
		sb.append(size);
		sb.append("\t");
		sb.append(patches.get(size - 1));
		sb.append("\n");
		FileUtil.writeStringToFile(plauRecoderFile, sb.toString(), true);
	}
	
	protected void recordAllPlauPatchHead(String msg) {
		StringBuffer sb = new StringBuffer();
		sb.append("[" + DateUtil.getFormatedCurrDate("yyyy-MM-dd HH:mm:ss") + "] ");
		sb.append(this.suspect.getFile() + "#" + this.suspect.getLine());
		sb.append("\n");
		sb.append(msg);
		sb.append("\n");
		FileUtil.writeStringToFile(plauRecoderFile, sb.toString(), true);
	}
	
	protected FixResult leftFirstPlauPatch(List<String> plauPatches, String patchComment) {
		if(plauPatches.isEmpty()) {
			return FixResult.FIX_FAIL;
		} else{
			//recode the first plausible patch
			String firstPlau = plauPatches.get(0);
			PatchResult res = processPatch(srcClsFile, firstPlau);
			assert res == PatchResult.SUCCESS: "PLAUSIBLE PATCH IS NOT SUCC";
			recordPatch(patchComment);
			return FixResult.FIX_SUCC;
		}
	}
	
	private void forceReloadSrcFile() {
		FileUtil.removeFromFileStrCache(srcFile);
		loadSrcFile();
	}
	
	private void loadSrcFile() {
		srcCode = FileUtil.loadFileStrFromCache(srcFile);
		srcCodeLines = srcCode.split("\n");
	}
}
