package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.MvnRunner;
import cn.edu.pku.sei.plde.hanabi.utils.MvnRunner.MvnLifeCycle;

public class MvnRunnerTest {

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
		String root = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		
		String[] targetPaths = new String[] {
				"src/trace/target/",
				"src/start/target/",
				"src/core/target/",
				"src/server/target/",
				"src/examples/target/",
			};
		
		List<File> targetFiles = new ArrayList<>();
		for(String path: targetPaths) {
			File tar = new File(root + path);
			targetFiles.add(tar);
		}
		
		MvnRunner runner = new MvnRunner(root);
		
		assertEquals(runner.run(MvnLifeCycle.CLEAN), 0);
		for(File file: targetFiles) {
			assertFalse(file.exists());
		}
		
		assertEquals(runner.run(MvnLifeCycle.COMPILE), 0);
		for(File file: targetFiles) {
			assertTrue(file.exists());
		}
	}
	
}
