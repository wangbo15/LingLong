package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMCamel8592Test extends ASMFaultLocationTest {

	@Test
	public void testCamel_8592() {
		String proj = "camel";
		int issueID = 8592;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.camel.processor.aggregator.CustomListAggregationStrategyEmptySplitTest-testCustomAggregationStrategy-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.camel.processor.aggregate.AbstractListAggregationStrategy", 65);
	}
}
