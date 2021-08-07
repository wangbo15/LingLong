package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel3690Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_3690() {
		String proj = "camel";
		int issueID = 3690;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.impl.EndpointShutdownOnceTest-testEndpointShutdown-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.impl.DefaultCamelContext", 894);
	}
}
