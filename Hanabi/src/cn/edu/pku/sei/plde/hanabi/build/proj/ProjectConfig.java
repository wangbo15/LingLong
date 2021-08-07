package cn.edu.pku.sei.plde.hanabi.build.proj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.build.BuildUtil;
import edu.pku.sei.proj.ProInfo;

public abstract class ProjectConfig {

	protected String projectName;
	protected File root;
	protected File srcRoot;
	protected File targetRoot;
	protected File testSrcRoot;
	protected File testTargetRoot;

	protected File testOutputFile;
	
	protected String classPaths;//all paths connected by ':' 

	protected String testJdkLevel;
	
	protected ProInfo proInfo;

	protected List<String> skipTestFolders;
	
	/**
	 * @param projectName: such as 'math_3'(for d4j) and 'accumulo_151'(for bugs.jar)
	 * @param root
	 */
	protected ProjectConfig(String projectName, String root) {
		this.projectName = projectName;
		this.root = new File(root);
	}
	
	public abstract String getBuildSrcCmd();

	public abstract String getBuildTestCmd();

	public abstract String getRunAllTestCmd();

	public abstract String getRunSingleTestCmd();

	public abstract String getRunTestSuiteCmd();

	public abstract String getCleanCmd();
	
	public abstract String getResetCmd();

	/**
	 * @return projectName: such as 'math_3'(for d4j) and 'accumulo_151'(for bugs.jar)
	 */
	public String getProjectName() {
		return projectName;
	}

	public File getRoot() {
		return root;
	}


	public File getSrcRoot() {
		return srcRoot;
	}

	
	public File getTargetRoot() {
		return targetRoot;
	}

	
	public File getTestSrcRoot() {
		return testSrcRoot;
	}


	public File getTestTargetRoot() {
		return testTargetRoot;
	}

	
	public String getClassPaths() {
		return classPaths;
	}

	public String getTestJdkLevel() {
		return testJdkLevel;
	}
	
	public ProInfo getProInfo() {
		return proInfo;
	}
	
	public File getTestOutputFile() {
		return testOutputFile;
	}
	
	public List<String> getSkipTestFolders() {
		if(skipTestFolders == null) {
			skipTestFolders = new ArrayList<>();
		}
		return skipTestFolders;
	}
	
	@Override
	public String toString() {
		return "PROJECT: " + projectName + " , AT: " + root;
	}
	
	protected void configViaMap(String root, Map<String, String> configure) {
		assert configure.get(BuildUtil.KEY_SRC_ROOT) != null;
		assert configure.get(BuildUtil.KEY_TARGET_ROOT) != null;
		assert configure.get(BuildUtil.KEY_TEST_SRC_ROOT) != null;
		assert configure.get(BuildUtil.KEY_TEST_TARGET_ROOT) != null;

		this.srcRoot = new File(root + "/" + configure.get(BuildUtil.KEY_SRC_ROOT));
		this.targetRoot = new File(root + "/" + configure.get(BuildUtil.KEY_TARGET_ROOT));
		this.testSrcRoot = new File(root + "/" + configure.get(BuildUtil.KEY_TEST_SRC_ROOT));
		this.testTargetRoot = new File(root + "/" + configure.get(BuildUtil.KEY_TEST_TARGET_ROOT));
		
		this.classPaths = configure.get(BuildUtil.KEY_CLASS_PATH);
		this.testJdkLevel = configure.get(BuildUtil.KEY_TEST_JDK_LEVEL);
		
		double jdk = Double.valueOf(testJdkLevel);
		assert jdk >= 1.5 && jdk <= 1.7 : "ONLY SUPPORT JDK 1.5~1.7, UNABLE TO SUPPORT " + testJdkLevel;
	}
	
	public static boolean isBugsDotJarProject(String proj) {
		proj = proj.toLowerCase();
		if(proj.startsWith("accumulo")
				|| proj.startsWith("camel") 
				|| proj.startsWith("flink")
				|| proj.startsWith("maven")
				|| proj.startsWith("wicket")) {
			return true;
		}else {
			return false;
		}
	}

}
