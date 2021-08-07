package cn.edu.pku.sei.plde.hanabi.fixer;

import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public abstract class Fixer {
	
	public final static int MAX_TRIED_SUSPECT = 200;
	
	public abstract boolean fixAllSespcts(List<Suspect> suspects);
	
	protected ProjectConfig projectConfig;
	protected TestRunner testRunner;
	
	protected Fixer(ProjectConfig projectConfig, TestRunner testRunner) {
		this.projectConfig = projectConfig;
		this.testRunner = testRunner;
	}
	
}
