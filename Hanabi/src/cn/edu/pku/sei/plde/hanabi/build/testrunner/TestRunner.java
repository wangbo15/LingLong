package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import edu.pku.sei.conditon.util.StringUtil;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TestRunner {
	
	// testClassName::mtdName
	public static final String TEST_CLS_MTD_DELIMITER = "::";
		
	protected final ProjectConfig projectConfig;
	
	protected int runAllTestsTimeBudget = -1;
	protected int runSingleTestTimeBudget = -1;
	
	public TestRunner(ProjectConfig projectConfig){
		this.projectConfig = checkNotNull(projectConfig);
	}
	
	public abstract void runAllTestCMD();
	
	public abstract void runSingleTestSuiteCMD(String test);
	
	public abstract void runSingleTestCMD(String test);
	
	public ProjectConfig getProjectConfig() {
		return projectConfig;
	}

	public int getRunAllTestsTimeBudget() {
		assert runAllTestsTimeBudget > 0;
		return runAllTestsTimeBudget;
	}

	public void setRunAllTestsTimeBudget(int runAllTestsTimeBudget) {
		assert runAllTestsTimeBudget > 0;
		this.runAllTestsTimeBudget = runAllTestsTimeBudget;
	}

	public int getRunSingleTestTimeBudget() {
		assert runSingleTestTimeBudget > 0;
		return runSingleTestTimeBudget;
	}

	public void setRunSingleTestTimeBudget(int runSingleTestTimeBudget) {
		assert runSingleTestTimeBudget > 0;
		this.runSingleTestTimeBudget = runSingleTestTimeBudget;
	}

	/**
	 * Run a single test method, such as "org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest#testIssue780".
	 * 
	 * @param test
	 *            test class name
	 * @param method
	 *            test method name
	 * @return success or fail
	 */
	public abstract boolean runSingleTestMethodByMethodTestRunner(String bugName, String test, String method, String externCfg);
	
	public abstract List<TestOutput> getErrorTestOutput();
	
	public abstract List<TestOutput> runAllAndGetErrorTestOutput();
	
	protected static boolean isStackTraceLine(String line) {
		boolean legalEnds = line.startsWith("at ") && line.endsWith(")");
		boolean legalParenthesisCounts = StringUtil.charOccurTimesInStr(line, '(') == 1 && StringUtil.charOccurTimesInStr(line, ')') == 1;
		boolean legalParenthesisOrder = line.indexOf(")") > line.indexOf("("); 
		return  legalEnds && legalParenthesisCounts && legalParenthesisOrder;
	}
	
	protected static StackTraceItem analysisStackTraceLine(String line) {
		StackTraceItem result = null;
		line = line.substring(3, line.length() - 1);//remove "at " and ")"
		String[] arr = line.split("\\(");
		if(arr.length != 2) {
			return null;
		}
		String mtdName = arr[0];
		if(arr[1].contains(":")){
			//normal trace
			String file = arr[1].split(":")[0];
			int lineNum = new Integer(arr[1].split(":")[1]);
			result = new StackTraceItem(mtdName, file, lineNum);
		}else{
			//Unknown Source or Native Method
			result = new StackTraceItem(mtdName, arr[1]);
		}
		return result;
	}
	
	
}
