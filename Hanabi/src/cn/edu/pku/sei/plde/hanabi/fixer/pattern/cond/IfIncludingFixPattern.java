package cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond;

import java.io.File;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class IfIncludingFixPattern extends ConditionRelatedFixPattern {

	private static final int MAX_INCLUDING_STMTS = 3;
	

	public IfIncludingFixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		super(projectConfig, suspect, iThSuspect, testRunner, testOutput, allFailTestOutputs);
	}

	@Override
	public FixResult implement() {
		//location at a semantic-block
		
		//get patch
		
		//include
		for(int i = 0; i < MAX_INCLUDING_STMTS; i++){
			
		}
		
		return FixResult.FIX_FAIL;
	}

	@Override
	protected boolean compilePatch(File srcClsFile, String patch) {
		// TODO Auto-generated method stub
		return false;
	}

}
