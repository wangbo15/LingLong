package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel8801Test extends ASMFaultLocationTest {
	/**
	 * Compilation error
	 */
	@Test
	public void testCamel_8081() {
		String proj = "camel";
		int issueID = 8081;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.processor.MulticastParallelAllTimeoutAwareTest-testMulticastParallelAllTimeoutAware-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.util.URISupport", 158);
	}
}
