package cn.edu.pku.sei.plde.hanabi.utils.fortest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.fortest.Purification;

public class PurificationTest {

	private ProjectConfig getProjectConfig(String proj, int bugId) {
		BugType projType = BugType.D4J_TYPE;
		String projName = proj + "_" + bugId;
		String root = Config.D4J_SRC_ROOT + proj + "/" + proj + "_" + bugId + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		return projectConfig;
	}
	
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
	public void test() {
		ProjectConfig config = getProjectConfig("lang", 35);
		Purification purification = new Purification(config);
		purification.purify(true);
	}

}
