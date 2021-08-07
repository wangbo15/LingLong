package cn.edu.pku.sei.plde.hanabi.build.proj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;

public class BugsDotJarProjectConfigTest {

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

	@Test
	public void test_Accumulo_1730() {
		String proj = "accumulo";
		int issueID = 1730;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		
		List<String> tobeRemove = projectConfig.getSkipTestFolders();
		
		assertNotNull(tobeRemove);
		assertEquals(tobeRemove.size(), 1);
		
		String path = tobeRemove.get(0);
		
		File removed = new File(projectConfig.getTestSrcRoot().getAbsolutePath() + "/" + path);
		
		for(File f : removed.listFiles()){
			assertTrue(f.isDirectory());
		}
		
		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

		CmdUtil.runByJava7(projectConfig.getRunAllTestCmd(), projectConfig.getRoot(), true);
		
		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
		CmdUtil.runCmd("git status", projectConfig.getRoot(), true);
		
	}
	
	@Test
	public void test_Calmel_8592() {
		String proj = "camel";
		int issueID = 8592;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");

		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

		CmdUtil.runByJava7(projectConfig.getRunAllTestCmd(), projectConfig.getRoot(), true);
		
		System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
		CmdUtil.runCmd("git status", projectConfig.getRoot(), true);
		
	}
	
	@Test
	public void test_Calmel_7130() {
		String proj = "camel";
		int issueID = 7130;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");

		assertTrue(projectConfig.getClassPaths().contains("slf4j-api-1.6.6.jar"));
		assertTrue(projectConfig.getClassPaths().contains("slf4j-log4j12-1.7.5.jar"));
	}

}
