package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.D4jProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.main.constant.D4jConstant;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import edu.pku.sei.conditon.util.DateUtil;;

public class D4jTestRunner extends TestRunner {
	
    /** run all test time budget */
	public static final int RUN_ALL_TIME_BUDGET_IN_MINUT = 3;
	
	/** run single test time budget*/
	public static final int RUN_SINGLE_TIME_BUDGET_IN_SEC = 60;
	
	private static final String TIME_OUT_CMD = "timeout " + RUN_SINGLE_TIME_BUDGET_IN_SEC + "s ";
	
	public D4jTestRunner(ProjectConfig projectConfig) {
		super(projectConfig);
		this.runAllTestsTimeBudget = RUN_ALL_TIME_BUDGET_IN_MINUT * 60;
		this.runSingleTestTimeBudget = RUN_SINGLE_TIME_BUDGET_IN_SEC;
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public List<TestOutput> runAllAndGetErrorTestOutput() {
		runAllTestCMD();
		return getErrorTestOutput();
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void runAllTestCMD() {
		try{
			CmdUtil.runByJava7(this.projectConfig.getRunAllTestCmd(), this.projectConfig.getRoot());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("ERROR: D4jTestRunner.runAllTest()");
		}
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void runSingleTestSuiteCMD(String test) {
		try{
			CmdUtil.runByJava7(this.projectConfig.getRunTestSuiteCmd() + " " + test, this.projectConfig.getRoot());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("ERROR: D4jTestRunner.runSingleTestSuite()");
		}
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void runSingleTestCMD(String test) {
		assert test.split(TEST_CLS_MTD_DELIMITER).length == 2: "MUST BE THE FORMAT 'aaa.bb.cc.TestCls::testMtd'";
		try{
			CmdUtil.runByJava7(this.projectConfig.getRunSingleTestCmd() + " " + test, this.projectConfig.getRoot());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("ERROR: D4jTestRunner.runSingleTest()");
		}
	}
	
	/** 
	 * {@inheritDoc}
	 * runSingleTestMethod will not modify the output file
	 * @return true: test passes. false: failed 
	 */
	@Override
	public boolean runSingleTestMethodByMethodTestRunner(String bugName, String test, String method, String externCfg) {
		String newerJunitPath = System.getProperty("user.dir") + "/lib/junit-4.11.jar";
		String hanabiPath = System.getProperty("user.dir") + "/bin/"; // System.getProperty("user.dir") + "/lib/junitrunner/" + ":" + 
		String cmd = "java -cp " + newerJunitPath + ":" + hanabiPath + ":" + this.projectConfig.getClassPaths()
						+ " cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner " + bugName + " " + test + "#" + method + " " + externCfg;
		
		cmd = TIME_OUT_CMD + cmd;
		return CmdUtil.runByJava7(cmd, this.projectConfig.getRoot(), true) == 0;
	}

	@Override
	public List<TestOutput> getErrorTestOutput() {
		File traceFile = this.projectConfig.getTestOutputFile();
		if(!traceFile.exists()){
			System.err.println("NO ERROR FOR ALL TEST: " + this.projectConfig.toString());
		}
		
		List<TestOutput> result = new ArrayList<>();
		List<TestOutput> skipped = new ArrayList<>();
		
		TestOutput current = null;
		List<String> lines = FileUtil.readFileToStringList(traceFile);
		for(String line : lines){
			line = line.trim();
			if(isFirstLine(line)){
				
				current = new D4jTestOutput();
				
				String failTest = line.split(" ")[1];
				current.setFailTest(failTest);
				
				//assert failTest.contains(TEST_CLS_MTD_DELIMITER): failTest;
				if(failTest.contains(TEST_CLS_MTD_DELIMITER)) {
					String arr[] = failTest.split(TEST_CLS_MTD_DELIMITER);
					assert arr.length == 2;
					
					String failTestCls = arr[0];
					current.setFailTestCls(failTestCls);
					current.setFailTestSrcPath(FileUtil.classNameToItsSrcPath(failTestCls));
					current.setFailTestMethod(arr[1]);
					
					if(this.projectConfig.getProjectName().startsWith("time") && failTest.equals("org.joda.time.TestDateTimeZone::testPatchedNameKeysGazaHistoric")) {
						continue;
					}
					
					result.add(current);
				}else {
					if(this.projectConfig.getProjectName().startsWith("time")) {
//						current.setFailTestCls(failTest);
//						current.setFailTestSrcPath(FileUtil.classNameToItsSrcPath(failTest));
//						current.setFailTestMethod("");
//						result.add(current);
					}else {
						assert false: failTest;
					}
				}
				
			}else if(isStackTraceLine(line)){
				StackTraceItem stackTraceItem = analysisStackTraceLine(line);
				if(stackTraceItem == null) {
					continue;
				}
				current.getStackTrace().add(stackTraceItem);
				if(stackTraceItem.getFullMethod().equals(current.getFailTestCls() + "." + current.getFailTestMethod())){
					current.setFailAssertLine(stackTraceItem.getLine());
				}
			}else if(current.getFailMessage() == null){
				if(skipByD4j(line)){
					skipped.add(current);
				}
				current.setFailMessage(line);
			}
		}
		result.removeAll(skipped);
		return result;
	}
	
	private boolean skipByD4j(String failMessage){
//		return failMessage.contains("junit.framework.AssertionFailedError");
		//TODO:: read the perl to finish
		return false;
	}

	private static boolean isFirstLine(String line) {
		return line.startsWith(D4jConstant.FAIL_TEST_BAGINNING_LINE);
	}

}
