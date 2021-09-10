package cn.edu.pku.sei.plde.hanabi.fixer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.FixPattern.FixResult;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond.InsertRetFixPattern;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond.ModifyIfCondFixPattern;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.ret.ConstantReplaceFixPattern;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class MLConditionFixer extends Fixer{
	
	private static Logger logger;
	static {
		System.setProperty("log.base", System.getProperty("user.dir"));
		logger = Logger.getLogger(MLConditionFixer.class);
	}
	
	public MLConditionFixer(ProjectConfig projectConfig, TestRunner testRunner){
		super(projectConfig, testRunner);
	}

	@Override
	public boolean fixAllSespcts(List<Suspect> suspects){
		
		assert suspects.isEmpty() == false;
		
		int max = (suspects.size() > MAX_TRIED_SUSPECT) ? MAX_TRIED_SUSPECT : suspects.size();
		
		logger.info("######## FIXING PROJECT: " + projectConfig.getProjectName() + " ON "+ max + " SUSPECTS " + " ########");
		
		List<TestOutput> allFailedTestsTemp = null;
		
		for(int i = 0; i < max; i++){
			
			Suspect currSuspect = suspects.get(i);
			
			List<TestOutput> allFailedRes;
			if(allFailedTestsTemp == null) {
				allFailedRes = testRunner.runAllAndGetErrorTestOutput();
			} else {
				allFailedRes = allFailedTestsTemp;
			}
			
			if(allFailedRes.isEmpty()){
				logger.info(">> NO ERROR TEST NOW !! " + this.projectConfig.getProjectName());
				allFailedTestsTemp = null;
				return true;
			}
			
			logger.info(">> CURRENT SUSPTCT: " + projectConfig.getProjectName() + "-" + i + "'th " + currSuspect.toString());

			List<TestOutput> outputs = filtTestResultBySuspect(allFailedRes, currSuspect);
			
			if(outputs.isEmpty()){
				logger.info(">> NO RELATED ERROR TEST FOR: " + currSuspect.toString());
				allFailedTestsTemp = allFailedRes;
				continue;
			}
			
			for(TestOutput currTestOutput : outputs) {
				logger.info(">> FIXING: " + currTestOutput.getFailTest() + " : " + currTestOutput.getFailMessage() + " @ " + currTestOutput.getFailAssertLine());
				FixResult fixRes;
				
				if(!currSuspect.getInapplicablePattern().contains(ConstantReplaceFixPattern.class)) {
					logger.info(">> Trying ConstantReplaceFixPattern");
					ConstantReplaceFixPattern thirdPattern = new ConstantReplaceFixPattern(projectConfig, currSuspect, i, testRunner, currTestOutput, allFailedRes);
					fixRes = thirdPattern.implement();
					if(fixRes == FixResult.FIX_SUCC){
						allFailedTestsTemp = thirdPattern.getRemainedFailedTests();
						if(allFailedTestsTemp.isEmpty()) {
							break;
						}
						continue;
					}else {//FIX_FAIL
						logger.info(">> ConstantReplaceFixPattern FAIL");
						allFailedTestsTemp = null;
					}
				}
				
				if(!currSuspect.getInapplicablePattern().contains(InsertRetFixPattern.class)) {
					logger.info(">> Trying InsertRetFixPattern");
					InsertRetFixPattern firstPattern = new InsertRetFixPattern(projectConfig, currSuspect, i, testRunner, currTestOutput, allFailedRes);
					fixRes = firstPattern.implement();
					if(fixRes == FixResult.FIX_SUCC){
						allFailedTestsTemp = firstPattern.getRemainedFailedTests();
						if(allFailedTestsTemp.isEmpty()) {
							break;
						}
						continue;
					}else {//FIX_FAIL
						logger.info(">> InsertRetFixPattern FAIL");
						allFailedTestsTemp = null;
					}
				}
				
				if(!currSuspect.getInapplicablePattern().contains(ModifyIfCondFixPattern.class)) {
					logger.info(">> Trying ModifiyIfCondFixPattern");
					ModifyIfCondFixPattern secondPattern = new ModifyIfCondFixPattern(projectConfig, currSuspect, i, testRunner, currTestOutput, allFailedRes);
					fixRes = secondPattern.implement();
					if(fixRes == FixResult.FIX_SUCC){
						allFailedTestsTemp = secondPattern.getRemainedFailedTests();
						if(allFailedTestsTemp.isEmpty()) {
							break;
						}
						continue;
					}else {//FIX_FAIL
						logger.info(">> ModifiyIfCondFixPattern FAIL");
						allFailedTestsTemp = null;
					}
				}
			
			}// end FOR_EACH_TESTOUT
			
		}//end FOR_EACH_SUSPECT
		
		allFailedTestsTemp = testRunner.runAllAndGetErrorTestOutput();
		if(allFailedTestsTemp != null && allFailedTestsTemp.isEmpty()) {
			logger.info(">> NO ERROR TEST NOW !! " + this.projectConfig.getProjectName());//TODO:: check logic
			return true;
		}
		
		return false;
	}
	
	
	
	/**
	 * if all the test outputs of a suspect are fixed, then continue to the next suspect
	 * @param oriFailedTests
	 * @param remainedFailedTests
	 * @return
	 */
	private boolean remainFailedTest(List<TestOutput> oriFailedTests, List<TestOutput> remainedFailedTests) {
		assert remainedFailedTests != null;
		int remainedSize = remainedFailedTests.size();
		if(remainedSize < 1) {//no more remaining test
			return false;
		}
		int oriSize = oriFailedTests.size();
		assert oriSize >= remainedSize;
		
		List<TestOutput> reamins = new ArrayList<>();
		for(TestOutput oriTest: oriFailedTests) {
			if(TestOutput.contains(remainedFailedTests, oriTest)) {
				reamins.add(oriTest);
			}
		}
		oriFailedTests.removeAll(reamins);
		if(oriFailedTests.isEmpty()) {
			return false;
		}else {
			return true;
		}
		
	}
	
	private List<TestOutput> filtTestResultBySuspect(List<TestOutput> oriOutput, Suspect suspect){
		List<TestOutput> result = new ArrayList<>();
		for(TestOutput currOutput : oriOutput){
			for(String trigger : suspect.getTriggerTests()) {
				String triggerTestCls = trigger.split("#")[0];
				String triggerTestMtd = trigger.split("#")[1];
				if(currOutput.getFailTestCls().equals(triggerTestCls) && currOutput.getFailTestMethod().equals(triggerTestMtd)) {
					result.add(currOutput);
				}
			}
		}
		
		return result;
	}
	
	
}
