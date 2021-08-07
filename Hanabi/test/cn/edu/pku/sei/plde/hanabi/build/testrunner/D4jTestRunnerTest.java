package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.D4jTestRunner;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;

public class D4jTestRunnerTest {

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
	public void test_Math_3() {
		String root = "/home/nightwish/workspace/bug_repair/tmp/Math_3/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(
				BugType.D4J_TYPE, 
				"math_3", 
				root);
		
		TestRunner runner = new D4jTestRunner(projectConfig);
		
		List<TestOutput> outputs = runner.runAllAndGetErrorTestOutput();

		File traceFile = projectConfig.getTestOutputFile();
		
		assertTrue(traceFile.exists());
		
		long firstRunTime = traceFile.lastModified();
		
		assertEquals(outputs.size(), 1);
		
		TestOutput output = outputs.get(0);
		
		assertEquals(output.getFailTest(), "org.apache.commons.math3.util.MathArraysTest::testLinearCombinationWithSingleElementArray");
		assertEquals(output.getFailAssertLine(), 591);
		assertEquals(output.getExceptionName(), "java.lang.ArrayIndexOutOfBoundsException");
		assertEquals(output.getFailMessage(), "java.lang.ArrayIndexOutOfBoundsException: 1");
		//System.out.println(output);
		
		//run again
		List<TestOutput> newOutputs = runner.runAllAndGetErrorTestOutput();
		assertTrue(traceFile.exists());
		//the trace file update time 
		long secondRunTime = traceFile.lastModified();
		assertTrue(newOutputs.size() == 1);
		//the trace file must be updated
		assertTrue(secondRunTime > firstRunTime);
		assertTrue(output.equals(newOutputs.get(0)));
	}

}
