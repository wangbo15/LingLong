package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel7130Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_7130() {
		String proj = "camel";
		int issueID = 7130;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.component.xslt.XsltDTDTest-testSendingInputStreamMessage-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.builder.xml.XsltBuilder", 455);
	}
}
