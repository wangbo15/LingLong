package cn.edu.pku.sei.plde.hanabi.build.proj;

import java.io.File;
import java.util.Map;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.D4jTestRunner;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.main.constant.D4jConstant;
import cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.ProInfoUtil;
import cn.edu.pku.sei.plde.hanabi.utils.build.AntUtil;
import cn.edu.pku.sei.plde.hanabi.utils.fortest.Purification;

public class D4jProjectConfig extends ProjectConfig {
	
	public static final String TIME_OUT_CMD = "timeout " + D4jTestRunner.RUN_ALL_TIME_BUDGET_IN_MINUT + "m ";
	
	/* must follow the head-upper format, such as 'Math' */
	private String bugProject;
	private int bugId;

	private void init(String projectName, String root) {
		String[] tmp = projectName.split("_");
		//must follow the head-upper format, such as 'Math'
		String name = tmp[0].toLowerCase();
		this.bugProject = name.substring(0, 1).toUpperCase() + name.substring(1);
		
		this.bugId = Integer.valueOf(tmp[1]);
		
		Map<String, String> configure = AntUtil.config(projectName, root);
		configViaMap(root, configure);
		
		this.testOutputFile = new File(root + "/" + D4jConstant.TEST_OUTPUT);
		
		cleanTempFiles();
		//TODO:: for test
		resetAndRemoveTargetRoot();
		
		this.proInfo = ProInfoUtil.loadProInfo(projectName.toLowerCase(),
				srcRoot.getAbsolutePath(),
				testSrcRoot.getAbsolutePath(),
				testJdkLevel);
		
	}
	
	/**
	 * @param projectName
	 * @param root
	 * @param purify
	 */
	D4jProjectConfig(String projectName, String root, boolean purify){
		super(projectName, root);
		init(projectName, root);
		if(purify) {
			Purification purification = new Purification(this);
			purification.purify(true);
		}
		recompile();
	}
	
	D4jProjectConfig(String projectName, String root){
		this(projectName, root, false);
	}
	

	/**
	 * clean the temp folder, including temp src, temp cls and trace folder
	 */
	private void cleanTempFiles(){
		FileUtil.cleanFolder(Config.TEMP_SRC_BACKUP_PATH);
		FileUtil.cleanFolder(Config.TEMP_CLS_BACKUP_PATH);
		
		String tracerPath = MethodTestRunner.generateTraceFileContainingFolder(this.projectName);
		FileUtil.cleanFolder(tracerPath);
    }
	
	private void resetAndRemoveTargetRoot() {
		CmdUtil.runCmd(this.getResetCmd(), this.root, false);
		assert this.root.exists() && this.srcRoot.exists() && this.testSrcRoot.exists();
		if(targetRoot.exists()) {
			String rmcmd = "rm -rf " + targetRoot.getAbsolutePath();
			CmdUtil.runCmd(rmcmd, this.root, false);
			assert targetRoot.exists() == false;
		}
	}
	
	private void recompile() {
		if(targetRoot.exists() == false || testTargetRoot.exists() == false) {
			CmdUtil.runByJava7(this.getBuildSrcCmd(), this.root);
		}
		assert targetRoot.exists() && testTargetRoot.exists(): "NO TARGET FOLDER EXISTS";
		
		if(this.getProjectName().startsWith("time")) {
			String dataFilePath = this.targetRoot.getAbsolutePath() + "/org/joda/time/tz/data/";
			File data = new File(dataFilePath);
			if(data.exists() == false) {
//				String cpcmd = "cp -r ./resources/time/data/ " + dataFilePath;
//				CmdUtil.runCmd(cpcmd, this.root, false);
				File from = new File("resources/time/data/");
				FileUtil.copyDir(from.getAbsolutePath(), dataFilePath);
				
				assert data.exists();
			}
		}
	}
	
	@Override
	public String getCleanCmd() {
		//TODO: 
		return "mvn clean";
	}
	
	@Override
	public String getResetCmd() {
		return "git checkout .";
	}
	
	@Override
	public String getBuildSrcCmd() {
		return TIME_OUT_CMD + "defects4j compile";
	}

	@Override
	public String getBuildTestCmd() {
		return getBuildSrcCmd();
	}

	@Override
	public String getRunAllTestCmd() {
		return TIME_OUT_CMD + "defects4j test";
	}

	/**
	 * https://people.cs.umass.edu/~rjust/defects4j/html_doc/d4j/d4j-test.html
	 */
	@Override
	public String getRunSingleTestCmd() {
		return getRunAllTestCmd() + " -t";
	}

	/**
	 * https://people.cs.umass.edu/~rjust/defects4j/html_doc/d4j/d4j-test.html
	 */
	@Override
	public String getRunTestSuiteCmd() {
		return getRunAllTestCmd() + " -s";
	}

	public String getBugProject() {
		return bugProject;
	}

	public int getBugId() {
		return bugId;
	}
	
	public static ProjectConfig getProjConfig(String proj, int bugId, boolean purify) {
		BugType projType = BugType.D4J_TYPE;
		String projName = proj + "_" + bugId;
		String root = Config.D4J_SRC_ROOT + proj + "/" + proj + "_" + bugId + "_buggy/";
		
		ProjectConfig projectConfig;
		if (purify) {
			projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root, purify);
		} else {
			projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		}
		return projectConfig;
	}

}
