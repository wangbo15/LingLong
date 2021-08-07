package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel9217Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_9217() {
		String proj = "camel";
		int issueID = 9217;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.component.file.FileConsumerPathWithAmpersandTest-testPathWithAmpersand-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.impl.DefaultComponent", 200);
	}
}
