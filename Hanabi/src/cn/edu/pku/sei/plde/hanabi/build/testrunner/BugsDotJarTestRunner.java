package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig.TestRange;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.build.MavenUtil;
import edu.pku.sei.conditon.util.FileUtil;;

public class BugsDotJarTestRunner extends TestRunner {
	
    /** run all test time budget */
	public static final int RUN_ALL_TIME_BUDGET_IN_MINUT = 5;
	
	/** run single test time budget*/
	public static final int RUN_SINGLE_TIME_BUDGET_IN_SEC = 60; 
	
	private static final boolean VERBOSE_CMD = false; 
	
	public BugsDotJarTestRunner(ProjectConfig projectConfig) {
		super(projectConfig);
		
		assert projectConfig instanceof BugsDotJarProjectConfig;
		
		long start = System.currentTimeMillis();
		this.runAllAndGetErrorTestOutput();
		long end = System.currentTimeMillis();
				
		this.runAllTestsTimeBudget = RUN_ALL_TIME_BUDGET_IN_MINUT * 60;
		this.runSingleTestTimeBudget = RUN_SINGLE_TIME_BUDGET_IN_SEC;
	}
	
	@Override
	public void runAllTestCMD() {
		try {
			CmdUtil.runByJava7(this.projectConfig.getRunAllTestCmd(), this.projectConfig.getRoot(), VERBOSE_CMD);
		}catch(Exception e) {
			e.printStackTrace();
			throw new Error("ERROR: BugsDotJarTestRunner.runAllTest()");
		}
	}

	@Override
	public void runSingleTestSuiteCMD(String test) {
		try {
			CmdUtil.runByJava7(this.projectConfig.getRunTestSuiteCmd() + test, this.projectConfig.getRoot(), VERBOSE_CMD);
		}catch(Exception e) {
			e.printStackTrace();
			throw new Error("ERROR: BugsDotJarTestRunner.runSingleTestSuite()");
		}
	}
	
	@Override
	public void runSingleTestCMD(String test) {
		assert test.split(TEST_CLS_MTD_DELIMITER).length == 2: "MUST BE THE FORMAT 'aaa.bb.cc.TestCls::testMtd'";
		test = test.replaceAll("::", "#");
		try{
			CmdUtil.runByJava7(this.projectConfig.getRunSingleTestCmd() + test, this.projectConfig.getRoot(), VERBOSE_CMD);
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("ERROR: BugsDotJarTestRunner.runSingleTest()");
		}
	}
	
	private static final String TIME_OUT_CMD = "timeout 5s ";
	@Override
	public boolean runSingleTestMethodByMethodTestRunner(String bugName, String test, String method, String externCfg) {
		String cmd = "java -cp " + this.projectConfig.getClassPaths() 
				+ " cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner " + bugName + " " + test + "#" + method + " " 
				+ externCfg;

		cmd = TIME_OUT_CMD + cmd;
		return CmdUtil.runByJava7(cmd, this.projectConfig.getRoot(), true) == 0;
	}

	/**
	 * A bugs.jar failure test output file example:
	 * -------------------------------------------------------------------------------
	 * Test set: org.apache.accumulo.core.iterators.CombinerTest
	 * -------------------------------------------------------------------------------
	 * Tests run: 10, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.017 sec <<< FAILURE!
	 * test1(org.apache.accumulo.core.iterators.CombinerTest)  Time elapsed: 0.003 sec  <<< FAILURE!
	 * org.junit.ComparisonFailure: expected:<[4]> but was:<[9]>
	 * 	at org.junit.Assert.assertEquals(Assert.java:99)
	 * 	at org.junit.Assert.assertEquals(Assert.java:117)
	 * 	at org.apache.accumulo.core.iterators.CombinerTest.test1(CombinerTest.java:103)
	 * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	 */
	@Override
	public List<TestOutput> getErrorTestOutput() {
		File outFolder = projectConfig.getTestOutputFile();
		return processFolder(outFolder);
	}

	@Override
	public List<TestOutput> runAllAndGetErrorTestOutput() {
		/*
		BugsDotJarProjectConfig config = (BugsDotJarProjectConfig) this.projectConfig;
		if(config.getTestRange() == TestRange.SMALL_PKG) {
			runAllTestCMD();
			List<TestOutput> subsetRes = getErrorTestOutput();
			
			if(subsetRes.isEmpty()) {
				// if currently correct, need run more
				File pom = config.getRunnerPom();
				String oriContent = FileUtil.readFileToString(pom);
				
				MavenUtil.changeTestRangeToPkg(pom);
				
				// re-run
				runAllTestCMD();
				subsetRes = getErrorTestOutput();
				
				// revert to small pkg test range
				FileUtil.writeStringToFile(pom, oriContent, false);
			}
			return subsetRes;
		} else {
			// for other ranges, directly return
			runAllTestCMD();
			return getErrorTestOutput();
		}*/
		runAllTestCMD();
		return getErrorTestOutput();
	}
	
	
	/**
	 * @param summary:
	 * Tests run: 10, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 0.017 sec <<< FAILURE!
	 * 
	 * or
	 * 
	 * Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 sec
	 * 
	 * @return an integer array with (all, failure, err)
	 */
	public static int[] processSummaryLine(String summary) {
		int[] res = new int[3];
		String[] items = summary.split(",");
		for(String item: items) {
			if(item.contains("Tests run: ")) {
				int all = Integer.valueOf(item.split(":")[1].trim());
				res[0] = all;
			}else if(item.contains("Failures: ")) {
				int failure = Integer.valueOf(item.split(":")[1].trim());
				res[1] = failure;
			}else if(item.contains("Errors: ")) {
				int err = Integer.valueOf(item.split(":")[1].trim());
				res[2] = err;
			}
		}
		return res;
	}

	/**
	 * 	-------------------------------------------------------------------------------
	 *	Test set: org.apache.accumulo.core.iterators.CombinerTest
	 *	-------------------------------------------------------------------------------
	 *	Tests run: 10, Failures: 0, Errors: 8, Skipped: 0, Time elapsed: 0.008 sec <<< FAILURE!
	 *	test1(org.apache.accumulo.core.iterators.CombinerTest)  Time elapsed: 0 sec  <<< ERROR!
	 *	...
	 */
	private static boolean isFirstLine(String line) {
		return (line.contains("<<< FAILURE!") || line.contains("<<< ERROR!")) && line.contains("(") && line.contains(")");
	}
	
	private static List<TestOutput> processFolder(final File outFolder) {
		
		assert outFolder.exists(): "MVN OUTPUT FOLDER DOES NOT EXISTS: " + outFolder.getAbsolutePath();
		assert outFolder.isDirectory() : "MVN TEST OUTPUT MUST BE A FOLDER: " + outFolder.getAbsolutePath();
		
        final File[] files = outFolder.listFiles();
        assert files != null: "TEST OUT PUT FOLDER MUST NOT BE EMPTY";
		
		List<TestOutput> result = new ArrayList<>();
		int sum = 0;
		
        for(final File file : files) {
    		if(file.getName().endsWith(".txt") && !file.getName().endsWith("-output.txt")) {
    			List<String> lines = FileUtil.readFileToStringList(file);
    			if(lines.size() <= 4) {
    				continue;
    			}
    			String testSet = lines.get(1);
    			assert testSet.contains("Test set: "): "ERROR OUTPUT FILE FORMAT: " + file.getAbsolutePath();
    			String testClass = testSet.split(":")[1].trim();
    			
    			String summary = lines.get(3);
    			//there are failed test
    			if(summary.contains(" <<< FAILURE!")) {
    				int[] resultNumbers = processSummaryLine(summary);
    				//int allTestMtdNum = resultNumbers[0];
    				int failureNum = resultNumbers[1];
    				int errNum = resultNumbers[2];
    				
    				assert failureNum + errNum > 0;
    				sum += (failureNum + errNum);
    				
    				TestOutput current = null;
    				for(int i = 4; i < lines.size(); i++) {
    					String line = lines.get(i).trim();
    					if(line.length() == 0) {
    						continue;
    					}
    					
    					if(isFirstLine(line)) {
    						current = new BugsDotJarTestOutput();
    						current.setFailTestCls(testClass);
    						current.setFailTestSrcPath(FileUtil.classNameToItsSrcPath(testClass));
    						
    						String mtd = line.split("\\(")[0].trim();
    						current.setFailTestMethod(mtd);
    						
    						String failTest = testClass + TEST_CLS_MTD_DELIMITER + mtd;
    						current.setFailTest(failTest);
    						
    						//add to result
    						result.add(current);
    					}else if(isStackTraceLine(line)) {
    						StackTraceItem stackTraceItem = analysisStackTraceLine(line);
    						if(stackTraceItem == null) {
    							continue;
    						}
    						current.getStackTrace().add(stackTraceItem);
    						if(stackTraceItem.getFullMethod().equals(current.getFailTestCls() + "." + current.getFailTestMethod())){
    							current.setFailAssertLine(stackTraceItem.getLine());
    						}
    					}else if(current != null){
    						current.setFailMessage(line);
    					}
    				}
    				
    			}
    		}// end if
    	}// end for
        
        assert result.size() == sum;
		return result;
	}
	
//	public static void main(String[] args) {
//		File folder = new File("/home/nightwish/workspace/bug_repair/bugs-dot-jar/camel/camel-core/target/surefire-reports/");
//		List<TestOutput> results = processFolder(folder);
//		
//		System.out.println(results.size());
//		
//		for(TestOutput output : results) {
//			System.out.println(output);
//		}
//	}
	
}
