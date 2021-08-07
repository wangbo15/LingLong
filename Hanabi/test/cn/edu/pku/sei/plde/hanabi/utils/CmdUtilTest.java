package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;

public class CmdUtilTest {

	@Test
	public void testRunByJava7_java_version() {
		String cmd = "java -version";
		File curr = new File(System.getProperty("user.dir"));
		
		System.out.println(">>>>>>>> CURRENT JAVA VERSION: ");
		int ret = CmdUtil.runByJava7(cmd, curr, true);
		
		assertTrue(ret == 0);
	}
	
	/**
	 * For math3, run by java 8 will get two failures, but only get one by java 7
	 */
	@Test
	public void testRunByJava7_math_3() {
		File math3 = new File("/home/nightwish/workspace/bug_repair/tmp/Math_3/");		
		CmdUtil.runCmd("git checkout .", math3);
		
		// if run by java 7, there is only one failure
		System.out.println(">>>>>>>> RUN BY JAVA 7");
		CmdUtil.runByJava7("defects4j test", math3, true);

		// if run by java 8, there are two failures
		System.out.println("\n>>>>>>>> RUN BY JAVA 8");
		CmdUtil.runCmd("defects4j test", math3, true);
	}
	
	@Test
	public void testRunBugsDotJar_Accumulo_151() {
		String accumuloPath = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		File root = new File(accumuloPath);
		CmdUtil.runCmd("git checkout .", root);
		CmdUtil.runCmd("git checkout bugs-dot-jar_ACCUMULO-151_b007b22e", root);
		
		// if run by java 7, there is only one failure
		// Tests run: 272, Failures: 1, Errors: 0, Skipped: 0
		System.out.println(">>>>>>>> RUN BY JAVA 7");
		int ret = CmdUtil.runByJava7("mvn test", root, true);
		assertTrue(ret == 1);
		
		ret = CmdUtil.runByJava7("timeout 2m mvn test", root, true);
		assertTrue(ret == 1);

		// if run by java 8, there are two failures
		// Tests run: 272, Failures: 2, Errors: 0, Skipped: 0
		System.out.println(">>>>>>>> RUN BY JAVA 8");
		CmdUtil.runCmd("mvn test", root, true);
	}
	
	@Test
	public void testRunBugsDotJar_Accumulo_907() {
		String accumuloPath = Config.BUGS_DOT_JAR_ROOT + "accumulo/";
		File root = new File(accumuloPath);
		//CmdUtil.runCmd("git checkout .", root);
		//CmdUtil.runCmd("git checkout bugs-dot-jar_ACCUMULO-907_4aeaeb2a", root);
		
		System.out.println(">>>>>>>> RUN BY JAVA 7");
		
		int ret = CmdUtil.runByJava7("timeout 2m mvn test -DfailIfNoTests=false -Dtest=org.apache.accumulo.core.util.shell.ShellTest", root, true);
//		int ret = CmdUtil.runByJava7("timeout 2m mvn test -Dmaven.test.failure.ignore=true", root, true);
//		assertTrue(ret == 1);
		
	}

	
	@Test
	public void test_output_in_getting_classpath() {
		String rootPath = Config.BUGS_DOT_JAR_ROOT + "camel/camel-core/";
		File root = new File(rootPath);
		
		CmdUtil.runCmd("git checkout .", root);
		CmdUtil.runCmd("git checkout bugs-dot-jar_CAMEL-7130_cc192f87", root);
		
		System.out.println(">>>>>>>> RUN BY JAVA 7");
		
		List<String> stdout = new ArrayList<String>();
		boolean verbose = true;
		CmdUtil.runByJava7("mvn dependency:build-classpath", root, verbose, stdout, null);
		
		for(String line: stdout) {
			System.out.println(line);
		}
		
	}
}
