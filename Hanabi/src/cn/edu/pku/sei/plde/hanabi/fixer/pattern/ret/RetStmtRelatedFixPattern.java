package cn.edu.pku.sei.plde.hanabi.fixer.pattern.ret;

import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fixer.pattern.FixPattern;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public abstract class RetStmtRelatedFixPattern extends FixPattern{
	
	public RetStmtRelatedFixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		super(projectConfig, suspect, iThSuspect, testRunner, testOutput, allFailTestOutputs);
	}

}
