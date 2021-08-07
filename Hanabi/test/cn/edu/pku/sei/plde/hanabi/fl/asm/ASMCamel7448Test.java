package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel7448Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_7448() {
		String proj = "camel";
		int issueID = 7448;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.processor.ThrottlerNullEvalTest-testNullEvalTest-false.txt");
		errFileSet.add("org.apache.camel.processor.ThrottlerNullEvalTest-testNoHeaderTest-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.processor.Throttler", 111);
	}
}
