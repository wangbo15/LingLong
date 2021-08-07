package cn.edu.pku.sei.plde.hanabi.fl.constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.fl.constructor.InvocationCollector;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Pair;
import edu.pku.sei.conditon.util.JavaFile;

public class InvocationCollectorTest {

	private ProjectConfig projectConfig;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_35";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
	}
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String testCls = "org.apache.commons.math3.genetics.ElitisticListPopulationTest";
		String testMtd = "testChromosomeListConstructorTooLow";
		String rootPath = projectConfig.getTestSrcRoot().getAbsolutePath();
		String testSrcPath = FileUtil.getFileAddressOfJava(rootPath, testCls);
		File testClsFile = new File(testSrcPath);

		assertTrue(testClsFile.exists());
		
		CompilationUnit testCu = JavaFile.genCompilationUnit(testClsFile, projectConfig.getTestJdkLevel(), projectConfig.getSrcRoot().getAbsolutePath());
		InvocationCollector collector = new InvocationCollector(testCls, testMtd);
		
		assertEquals(collector.getTobeTestedCls(), "ElitisticListPopulation");

	}
	
	@Test
	public void test_interface() {
		String testCls = "org.apache.commons.math3.genetics.ElitisticListPopulationTest";
		String testMtd = "testChromosomeListConstructorTooLow";
		
		Pair<String, List<Integer>> result = InvocationCollector.getInvokedConstructorStmts(projectConfig, testCls, testMtd);
		String srcCls = result.first();
		List<Integer> errProneLines = result.second();
		
		assertEquals(srcCls, "org.apache.commons.math3.genetics.ElitisticListPopulation");		
		assertEquals(errProneLines.size(), 1);
		assertEquals((int) errProneLines.get(0), 51);
	}

}
