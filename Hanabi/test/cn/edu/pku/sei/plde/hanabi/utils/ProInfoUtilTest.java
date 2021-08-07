package cn.edu.pku.sei.plde.hanabi.utils;

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
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.main.Main;
import cn.edu.pku.sei.plde.hanabi.utils.ProInfoUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.MethodRepre;
import edu.pku.sei.proj.ProInfo;

public class ProInfoUtilTest {

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
	
	private File deleteInfo(String project) {
		String infoFilePath = ProInfoUtil.PROINFO_ROOT + project + ".pro";
		File infoFile = new File(infoFilePath);
		if(infoFile.exists()) {
			infoFile.delete();
		}
		return infoFile;
	}

	@Test(timeout = 60*1000)
	public void test_accumulo() {
		String srcRoot = Config.BUGS_DOT_JAR_ROOT + "accumulo/src/core/src/main/java";
		String testRoot = Config.BUGS_DOT_JAR_ROOT + "accumulo/src/core/src/test/java";
		String project = "accumulo_151";
		String testJdkLevel = "1.7";
		
		File deletedInfo = deleteInfo(project);
		
		ProInfo proInfo = ProInfoUtil.loadProInfo(project,
				srcRoot,
				testRoot,
				testJdkLevel);
		
		assertNotNull(proInfo);
		
		assertTrue(deletedInfo.exists());
		
		for (ClassRepre cls : proInfo.getProjectRepre().fullNameToClazzesMap.values()) {
			System.out.println(cls);
		}
		ClassRepre combinerCls = proInfo.getProjectRepre().fullNameToClazzesMap.get("org.apache.accumulo.core.iterators.Combiner");
		assertNotNull(combinerCls);
		
		List<MethodRepre> mtds  = combinerCls.getMethodRepreByName("findTop");
		assertEquals(mtds.size(), 1);
		
		MethodRepre mtd = mtds.get(0);
		assertNotNull(mtd);
		assertEquals(mtd.getReturnType(), "void");
	}
	
	@Test(timeout = 60*1000)
	public void testCamelProInfoOnlyForTest() {
		String project = "camel_3388";
		File deletedInfo = deleteInfo(project);
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig("camel", 3388, "camel-core");
		
		ProInfo proInfo = projectConfig.getProInfo();
		
		assertNotNull(proInfo);

		assertTrue(deletedInfo.exists());
		
		ClassRepre combinerCls = proInfo.getProjectRepre().fullNameToClazzesMap.get("org.apache.camel.component.bean.BeanWithHeadersAndBodyInject3Test");
		assertEquals("ContextTestSupport", combinerCls.getFatherCls().getName());
		
		ClassRepre testCase = combinerCls.getTopFatherCls();
		assertEquals("TestCase", testCase.getName());
	}

}
