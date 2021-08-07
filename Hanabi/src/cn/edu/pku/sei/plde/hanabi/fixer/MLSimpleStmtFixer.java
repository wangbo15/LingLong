package cn.edu.pku.sei.plde.hanabi.fixer;

import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class MLSimpleStmtFixer extends Fixer {

	protected MLSimpleStmtFixer(ProjectConfig projectConfig, TestRunner testRunner) {
		super(projectConfig, testRunner);
	}

	@Override
	public boolean fixAllSespcts(List<Suspect> suspects){
		// TODO Auto-generated method stub
		return false;
	}

}
