package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel3388Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_3388() {
		String proj = "camel";
		int issueID = 3388;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.component.bean.BeanWithHeadersAndBodyInject3Test-testInOnly-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.util.URISupport", 260);
	}
}
