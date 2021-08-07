package cn.edu.pku.sei.plde.hanabi.utils.build;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.build.BuildUtil;
import cn.edu.pku.sei.plde.hanabi.utils.build.MavenUtil;

public class MavenUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private static void clean(String root, String branch) {
		File rootFile = new File(root);
		CmdUtil.runCmd("git checkout .", rootFile, true);
		CmdUtil.runCmd("git clean -fd", rootFile, true);
		CmdUtil.runCmd("git clean -fX", rootFile, true);
		CmdUtil.runCmd("git checkout " + branch, rootFile, true);
		CmdUtil.runCmd("mvn clean", rootFile, true);
	}
	
	@Test
	public void test_Accumulo_151() {
		String root = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		clean(root, "bugs-dot-jar_ACCUMULO-151_b007b22e");
		
		Map<String, String> conf = MavenUtil.config("accumulo", root);
		
		assertTrue(BuildTestAbs.checkKeySet(conf.keySet()));
		
		String classPaths = conf.get(BuildUtil.KEY_CLASS_PATH);
		
		assertTrue(classPaths.contains("accumulo/src/core/target/classes"));
		assertTrue(classPaths.contains("src/core/target/test-classes"));
		assertTrue(classPaths.contains("Hanabi/bin"));
		
		String testJdkLevel = conf.get(BuildUtil.KEY_TEST_JDK_LEVEL);
		double jdkLevel = Double.valueOf(testJdkLevel);
		assertTrue(jdkLevel >= 1.5);
		assertTrue(jdkLevel <= 1.7);
		
		//run test again to check the existence of the targets 
		CmdUtil.runCmd("mvn test", new File(root), true);
		assertTrue(new File(root + conf.get(BuildUtil.KEY_TARGET_ROOT)).exists());
		assertTrue(new File(root + conf.get(BuildUtil.KEY_TEST_TARGET_ROOT)).exists());

		//output
		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		for(Entry<String, String> entry: conf.entrySet()) {
			System.out.println(entry.toString());
		}
	}
	
	@Test
	public void testGetModuleDependency() {
		String root = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		clean(root, "bugs-dot-jar_ACCUMULO-2659_019edb16");
		
		File pom = new File(root + "core/pom.xml");
		Set<String> results = MavenUtil.getModuleDependency(pom);
		
		assertFalse(results.isEmpty());
		assertTrue(results.contains("accumulo-fate"));
		assertTrue(results.contains("accumulo-start"));
		assertTrue(results.contains("accumulo-trace"));

	}
	
	@Test
	public void testRemoveUndependedModules() {
		String root = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		
		File rootFile = new File(root);
		clean(root, "bugs-dot-jar_ACCUMULO-2659_019edb16");
		
		File corePom = new File(root + "core/pom.xml");
		Set<String> depends = MavenUtil.getModuleDependency(corePom);
		
		File pom = new File(root +  "pom.xml");
		MavenUtil.removeUndependedModules("accumulo", pom, depends);
		
		CmdUtil.runCmd("git diff", rootFile, true);
		
		MavenProject project = MavenUtil.getMavenProject(pom);
		
		Set<String> mewModuels = new HashSet<>(project.getModules());

		assertTrue(mewModuels.contains("core"));
		assertTrue(mewModuels.contains("start"));
		assertTrue(mewModuels.contains("fate"));
		assertTrue(mewModuels.contains("start"));
		
		assertFalse(mewModuels.contains("examples/simple"));
		assertFalse(mewModuels.contains("assemble"));
		assertFalse(mewModuels.contains("proxy"));
		assertFalse(mewModuels.contains("test"));
		assertFalse(mewModuels.contains("minicluster"));
		assertFalse(mewModuels.contains("docs"));
		assertFalse(mewModuels.contains("maven-plugin"));
		assertFalse(mewModuels.contains("server/base"));
		assertFalse(mewModuels.contains("server/gc"));
		assertFalse(mewModuels.contains("server/master"));
	}
	
	@Test
	public void testModifyModulePomToSkipTest() {
		String root = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		
		File rootFile = new File(root);
		clean(root, "bugs-dot-jar_ACCUMULO-2659_019edb16");
		
		//first remove useless modules
		File corePom = new File(root + "core/pom.xml");
		Set<String> depends = MavenUtil.getModuleDependency(corePom);
		
		File pom = new File(root +  "pom.xml");
		MavenUtil.removeUndependedModules("accumulo", pom, depends);
		
		// skip accumulo-start
		File startPom = new File(root + "start/pom.xml");
		MavenUtil.modifyModulePomToSkipTest(startPom);
		
		// skip accumulo-trace
		File tracePom = new File(root + "trace/pom.xml");
		MavenUtil.modifyModulePomToSkipTest(tracePom);

		CmdUtil.runCmd("git diff", rootFile, true);
		
		// must fully executed
		CmdUtil.runByJava7("mvn test -Drat.ignoreErrors=true -fn", rootFile, true);
	}

}
