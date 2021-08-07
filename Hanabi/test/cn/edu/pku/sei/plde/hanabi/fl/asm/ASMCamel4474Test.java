package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel4474Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_4474() {
		String proj = "camel";
		int issueID = 4474;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.component.file.FileConsumerAutoCreateDirectoryTest-testAutoCreateDirectoryWithDot-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.component.file.FileEndpoint", 54);
	}
}
