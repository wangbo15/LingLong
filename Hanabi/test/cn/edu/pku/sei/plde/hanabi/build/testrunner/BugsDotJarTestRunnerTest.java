package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;

public class BugsDotJarTestRunnerTest {

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
	public void test_Accumulo_151_runAllCMD() {
		String proj = "accumulo";
		int issueID = 151;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		
		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		
		List<TestOutput> results = runner.runAllAndGetErrorTestOutput();
		
		assertNotNull(results);
		
		assertTrue(results.size() == 1);
		
		TestOutput output = results.get(0);
		
		System.out.println(output);
		
		assertEquals(output.getFailTestCls(), "org.apache.accumulo.core.iterators.CombinerTest");
		assertEquals(output.getFailAssertLine(), 103);
		assertTrue(output.getFailMessage().contains("expected:<[4]> but was:<[9]>"));
		assertEquals(output.getFailTest(), "org.apache.accumulo.core.iterators.CombinerTest" + TestRunner.TEST_CLS_MTD_DELIMITER + "test1");
	}
	
	@Test
	public void test_Accumulo_151_runSingleTestSuiteCMD() {
		String proj = "accumulo";
		int issueID = 151;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		
		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		
		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
		runner.runSingleTestSuiteCMD("org.apache.accumulo.core.iterators.CombinerTest");
	}
	
	@Test
	public void test_Accumulo_2713_runAllCMD() {
		String proj = "accumulo";
		int issueID = 2713;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		
		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		
		List<TestOutput> results = runner.runAllAndGetErrorTestOutput();
		
		assertNotNull(results);
		
		assertTrue(results.size() == 1);
		
		TestOutput output = results.get(0);
		
		System.out.println(output);
		
		assertEquals(output.getFailTestCls(), "org.apache.accumulo.core.file.rfile.RFileTest");
		assertEquals(output.getFailAssertLine(), 1767);
		assertTrue(output.getFailMessage().contains("expected:<-1> but was:<1000>"));
		assertEquals(output.getFailTest(), "org.apache.accumulo.core.file.rfile.RFileTest" + TestRunner.TEST_CLS_MTD_DELIMITER + "testCryptoDoesntLeakInstanceSecret");
	}
	
	@Test
	public void test_Camel_7448_runAllCMD() {
		String proj = "camel";
		int issueID = 7448;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		
		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		
		List<TestOutput> results = runner.runAllAndGetErrorTestOutput();
		
		assertNotNull(results);
		
		assertTrue(results.size() == 2);
		
	}
	
}
