package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLClassLoader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.utils.ClassPathHacker;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;

public class ClassPathHackerTest {

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

	private ProjectConfig getProjectConfig() {
		String proj = "accumulo";
		int issueID = 151;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		return projectConfig;
	}
	
	@Test
	public void test_loadForInstrument() {
		
		// will 'mvn clean' while build projectConfig
		ProjectConfig projectConfig = this.getProjectConfig();
		
		//CmdUtil.runCmd("mvn clean", projectConfig.getRoot(), false);
		CmdUtil.runCmd("mvn test", projectConfig.getRoot(), false);
		
		try {
			String classPaths = projectConfig.getClassPaths();
			
			ClassPathHacker.loadClassPaths(classPaths);
						
			URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			
			/*for(URL u : sysLoader.getURLs()) {
				System.out.println(u);
			}*/
						
			String className = "org.apache.accumulo.core.iterators.FilteringIterator";
			
			Class<?> classToLoad = Class.forName(className, true, sysLoader);

			assertTrue(classToLoad != null);
			
			classToLoad = sysLoader.loadClass("org.apache.accumulo.core.iterators.FilteringIterator");
			
			assertTrue(classToLoad != null);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			fail("CLASS NOT FOUND");
		}
	}

	
	@Test
	public void test_loadThenRemove() {
		// will 'mvn clean' while build projectConfig
		ProjectConfig projectConfig = this.getProjectConfig();
		
		//CmdUtil.runCmd("mvn clean", projectConfig.getRoot(), false);
		CmdUtil.runCmd("mvn test", projectConfig.getRoot(), false);
		
		String classPaths = projectConfig.getClassPaths();
	
		ClassPathHacker.loadClassPaths(classPaths);
		
		String className = "org.apache.accumulo.core.iterators.FilteringIterator";
		
		Class<?> classToLoad = null;
		try {
			classToLoad = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		assertNotNull(classToLoad);
		
		ClassPathHacker.removeClassPaths(classPaths);

		classToLoad = null;
		
		for(int i = 0; i < 100; i++) {
			System.gc();
		}
		
		CmdUtil.runCmd("mvn clean", projectConfig.getRoot(), false);
		
		try {
			className = "org.apache.accumulo.core.iterators.Combiner";
			classToLoad = Class.forName(className);
			fail("MUST LOAD FAIL");
		} catch (ClassNotFoundException e) {
			
		}
		
	}
}
