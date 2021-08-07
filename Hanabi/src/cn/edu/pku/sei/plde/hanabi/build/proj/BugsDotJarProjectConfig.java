package cn.edu.pku.sei.plde.hanabi.build.proj;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.external.BugsDotJarTestAnalyser;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.GitUtil;
import cn.edu.pku.sei.plde.hanabi.utils.MvnRunner;
import cn.edu.pku.sei.plde.hanabi.utils.ProInfoUtil;
import cn.edu.pku.sei.plde.hanabi.utils.MvnRunner.MvnLifeCycle;
import cn.edu.pku.sei.plde.hanabi.utils.build.MavenUtil;

public class BugsDotJarProjectConfig extends ProjectConfig{

	private static final String TIME_OUT_CMD = "timeout " + BugsDotJarTestRunner.RUN_ALL_TIME_BUDGET_IN_MINUT + "m ";
	
	private static final String RAT_OPT = " -Drat.ignoreErrors=true";
	private static final String SKIP_FAIL_OPT = " -DfailIfNoTests=false";
	private static final String TAIL_OPT = " -fn";
	
	/** Branches that must be removed **/
	private static final Set<String> FIELD_RELATED_OR_MULTY_BRANCHES = new HashSet<>();
	static {
		FIELD_RELATED_OR_MULTY_BRANCHES.add("bugs-dot-jar_CAMEL-7130_7c9326f4");
		FIELD_RELATED_OR_MULTY_BRANCHES.add("bugs-dot-jar_CAMEL-7125_6641f182");
		FIELD_RELATED_OR_MULTY_BRANCHES.add("bugs-dot-jar_CAMEL-7359_e6fbbf04");

	}
	
	/** the field is lower case, like 'accumulo' */
	private String subject;
	
	private String coreModuleName;
	
	private String branchName;
	
	private MvnRunner mvnRunner;
	
	private static final int MAX_TEST_NUM = 200;
	
	public enum TestRange {
		ALL,
		PKG, 	
		SMALL_PKG
	}
	
	private File runnerPom;
	
	private TestRange testRange = TestRange.ALL;
	
	private void init(String projectName, String root) {
		String[] strs = projectName.split("_");
		String subject = strs[0];
		String issueID = strs[1];
		
		checkoutToBranch(subject, issueID);
		
		mvnRunner = new MvnRunner(root);
	
		// mvn clean
		reset();
		
		Map<String, String> configure = MavenUtil.config(subject, root);
				
		configViaMap(root, configure);
					
		cleanTempFiles();
		
		this.coreModuleName = configure.get(MavenUtil.KEY_CORE_MODULE_NAME);
		this.testOutputFile = new File(root + "/" + configure.get(MavenUtil.KEY_SUREFIRE_REPORT_PATH));
		
		initTestConfig(root, configure);
		
		this.proInfo = ProInfoUtil.loadProInfo(projectName.toLowerCase(),
				srcRoot.getAbsolutePath(),
				testSrcRoot.getAbsolutePath(),
				testJdkLevel);
	}
	
	/**
	 * Modify tests
	 * @param root
	 * @param configure
	 */
	private void initTestConfig(String root, Map<String, String> configure) {
		switch(subject) {
		case "accumulo":{ 
			initFromRoot(root, configure);
			break;
		}
		case "camel": {
			initFromSubRoot(root, configure);
			break;
		}
		default:
			throw new Error(projectName);
		}
	}
	
	private void initFromRoot(String root, Map<String, String> configure) {
		String path = root + "/" + coreModuleName + "/pom.xml";
		runnerPom = new File(path);
		Set<String> dependencies = getDependencies(runnerPom);
		removeUndepenededModules(dependencies);
		
		String otherModules = configure.get(MavenUtil.KEY_OTHER_MODULES_NAMES);
		insertSkipTestToModules(root, otherModules, dependencies);
		
		deleteSkipTests(subject);
	}
	

	private void initFromSubRoot(String subRoot, Map<String, String> configure) {
		String path = subRoot + "/pom.xml";
		
		runnerPom = new File(path);
		
		File resFile = new File(this.root.getAbsolutePath() + "/../.bugs-dot-jar/test-results.txt");
		
		assert resFile.exists();
		Set<String> summaryfailedList = BugsDotJarTestAnalyser.analysisTestOutput(this.branchName, resFile);
		
		String inlcudeStr = generateIncludeStr(summaryfailedList);
		
		MavenUtil.insertTestsToInclude(inlcudeStr, runnerPom);
	}
	
	private String generateIncludeStr(Set<String> failedTest) {
		Set<String> pkgs = new LinkedHashSet<>();
		Set<Character> firstLetters = new LinkedHashSet<>();
		for (String test : failedTest) {
			int clsIdx = test.lastIndexOf(".");
			char firstChar = test.charAt(clsIdx + 1);
			
			assert Character.isUpperCase(firstChar);
			
			firstLetters.add(firstChar);
			String pkg = test.substring(0, clsIdx);
			pkgs.add(pkg);
		}
		
		if(firstLetters.size() == 1) {
			char content = firstLetters.iterator().next();
			if(content > 'A' && content <= 'Z') {
				firstLetters.add((char) (content - 1));
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(String pkg : pkgs) {
			
			String pkgRelativePath = pkg.replaceAll("\\.", "/");
			File pkgFile = new File(testSrcRoot.getAbsolutePath() + "/" + pkgRelativePath);
			
			assert pkgFile.exists();
			
			// if there are too many tests in one folder
			if(pkgFile.listFiles().length > MAX_TEST_NUM) {
				for(char ch : firstLetters) {
					sb.append("\t<include>" + pkgRelativePath + "/" + ch + "*Test.java</include>\n");
				}
				
				this.testRange = TestRange.SMALL_PKG;
				
			}else {
				sb.append("\t<include>" + pkgRelativePath + "/*Test.java</include>\n");
				
				this.testRange = TestRange.PKG;
			}
		}		
		return sb.toString();
	}
	
	private Set<String> getDependencies(File pom) {
		assert pom.exists(): pom.getAbsolutePath();
		Set<String> depended = MavenUtil.getModuleDependency(pom);
		return depended;
	}
	
	private void removeUndepenededModules(Set<String> dependencies) {
		File rootPom = new File(this.root + "/pom.xml");
		MavenUtil.removeUndependedModules(this.subject, rootPom, dependencies);
	}

	private void deleteSkipTests(String subject) {
		switch(subject) {
		case "accumulo": {
				final String skipPath = "org/apache/accumulo/core/util/shell/";
				this.getSkipTestFolders().add(skipPath);
				
				File testFolder = new File(this.testSrcRoot.getAbsolutePath() + "/" + skipPath);
				if(testFolder.exists()) {
					FileUtil.cleanFolder(testFolder);
				}
				break;
			}
		default:
			throw new Error(projectName);
		}
		
	}

	private void insertSkipTestToModules(String root, String otherModules, Set<String> dependencies) {
		if(otherModules.length() == 0) {
			return;
		}
		String[] modules = otherModules.split(":");
		for(String mdl: modules) {
			
			assert mdl.contains("core") == false: "CANNOT SKIP CORE MODULE: " + mdl;
			
			// only modify the depended modules 
			if(!dependencies.contains(this.subject + "-" + mdl)) {
				continue;
			}
			
			// insert skip
			String path = root + "/" + mdl;
			File moduleFile = new File(path);
			if(moduleFile.exists()) {
				File pom = new File(path + "/pom.xml");
				MavenUtil.modifyModulePomToSkipTest(pom);
			}
		}
		
	}

	private void checkoutToBranch(String subject, String issueID) {
		List<String>[] outputs = null;
		List<String> branches = null;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			outputs = CmdUtil.runCmd("git branch -a", this.root, false);
			branches = outputs[0];
		}while(branches.size() < 3);
		
		// as least 3 branches: a master, a local branch and a remote branch
		assert branches.size() >= 3: "\n>>>>>>>> STDOUT:\n" + outputs[0] + "\n>>>>>>>> STDERR:\n" + outputs[1];
		
		String prefix = "bugs-dot-jar_" + subject.toUpperCase() + "-" + issueID + "_";
		String remotePrefix = "remotes/origin/" + prefix;
		
		String branchName = "";
		String currBranch = "";
		for(String line : branches) {
			if(line.trim().length() < 3) {
				continue;
			}
			
			if(line.startsWith("* ")) {
				currBranch = line.substring(2);
			}
			
			line = line.substring(2).trim(); //remove '  ' or '* '
			if(line.startsWith(prefix)) {
				if(!FIELD_RELATED_OR_MULTY_BRANCHES.contains(line)) {
					branchName = line;
					break;
				}
			}else if(line.startsWith(remotePrefix)) {
				int idx = line.indexOf(prefix);
				String tmpBranch = line.substring(idx);
				if(!FIELD_RELATED_OR_MULTY_BRANCHES.contains(tmpBranch)) {
					branchName = tmpBranch;
					break;
				}
			}
		}
		assert branchName.length() > 0: prefix; 
		
		this.subject = subject.toLowerCase();
		this.branchName = branchName;
		
		// clean changes and checkout to the branch		
		GitUtil.resetAndCleanRepo(root);
		
		if(!currBranch.equals(branchName)) {
			GitUtil.gitCheckout(branchName, root);
		}
	}

	/**
	 * @param projectName: For Bugs.jar, marked as issue ID, like 'accumulo_151'
	 * @param root
	 */
	BugsDotJarProjectConfig(String projectName, String root){
		super(projectName, root);
		init(projectName, root);
	}

	public static String generateProjectName(String proj, int issueID) {
		return proj + "_" + issueID;
	}
	
	@Override
	public String getCleanCmd() {
		return "mvn clean" + TAIL_OPT;
	}
	
	/**
	 * To remove directories, run git clean -f -d or git clean -fd
	 * To remove ignored files, run git clean -f -X or git clean -fX
	 */
	@Override
	public String getResetCmd() {
		return "git checkout . && git clean -fd && git clean -fX";
	}
	
	@Override
	public String getBuildSrcCmd() {
		return TIME_OUT_CMD + "mvn compile" + TAIL_OPT;
	}

	@Override
	public String getBuildTestCmd() {
		return getBuildSrcCmd();
	}
	
	@Override
	public String getRunAllTestCmd() {
		// "mvn test -Dmaven.test.failure.ignore=true -pl " + coreModuleName + " -am";
		// mvn test -Drat.ignoreErrors=true -fn
		return TIME_OUT_CMD + "mvn test" + RAT_OPT + TAIL_OPT;
	}
	
	/**
	 * Only run a test method
	 * Such as bugs-dot-jar_ACCUMULO-151_b007b22e, we want to run the failed test: 
	 * mvn test -DfailIfNoTests=false -Dtest=org.apache.accumulo.core.iterators.CombinerTest#test1
	 */
	@Override
	public String getRunSingleTestCmd() {
		return TIME_OUT_CMD + "mvn test" + SKIP_FAIL_OPT + RAT_OPT +  " -Dtest=";
	}
	
	/**
	 * Only run a test class: 
	 * mvn test -DfailIfNoTests=false -Dtest=org.apache.accumulo.core.iterators.CombinerTest
	 */
	@Override
	public String getRunTestSuiteCmd() {
		return TIME_OUT_CMD + "mvn test" + SKIP_FAIL_OPT + RAT_OPT + " -Dtest=";
	}
	
	public String getBranchName() {
		return this.branchName;
	}
	
	public MvnRunner getMvnRuner() {
		return mvnRunner;
	}
	
	public String getCoreModuleName() {
		return coreModuleName;
	}

	public TestRange getTestRange() {
		return testRange;
	}
	
	public File getRunnerPom() {
		assert runnerPom.exists():runnerPom.getAbsolutePath();
		return runnerPom;
	}
	
	/***************************************************************************************/
	public static ProjectConfig loadProjectConfig(String proj, int issueID) {
		
		assert proj.equals("accumulo");
		
		String root = Config.BUGS_DOT_JAR_ROOT + proj + "/";
		String projName = BugsDotJarProjectConfig.generateProjectName(proj, issueID);
		return ProjectConfigFactory.createPorjectConfig(BugType.BDJ_TYPE, projName, root);
	}
	
	/**
	 * Collect ProjectConfig for a sub-module
	 * @param proj
	 * @param issueID
	 * @param subModule
	 * @return
	 */
	public static ProjectConfig loadProjectConfig(String proj, int issueID, String subModule) {
		
		assert proj.equals("camel");
		
		String root = Config.BUGS_DOT_JAR_ROOT + proj + "/" + subModule + "/";
		String projName = BugsDotJarProjectConfig.generateProjectName(proj, issueID);
		
		return ProjectConfigFactory.createPorjectConfig(BugType.BDJ_TYPE, projName, root);
	}
	
	private void cleanTempFiles() {
		//TODO: check
		FileUtil.cleanFolder(Config.TEMP_SRC_BACKUP_PATH);
		FileUtil.cleanFolder(Config.TEMP_CLS_BACKUP_PATH);
		FileUtil.cleanFolder(Config.ASM_TRACE_FOLDER);
	}
	
	private void reset() {
		int res = mvnRunner.run(MvnLifeCycle.CLEAN);
		assert res == 0;
//		res = mvnRunner.run(MvnLifeCycle.TEST);
//		assert res != 0;
	}
	
}
