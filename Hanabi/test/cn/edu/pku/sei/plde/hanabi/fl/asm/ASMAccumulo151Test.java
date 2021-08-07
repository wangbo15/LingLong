package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;

public class ASMAccumulo151Test extends ASMFaultLocationTest {

	@Test
	public void testAccumulo_151() {
		String proj = "accumulo";
		int issueID = 151;
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);

		Set<String> errFileSet = new HashSet<>();
		errFileSet.add("org.apache.accumulo.core.iterators.CombinerTest-test1-false.txt");
		innerTest(projectConfig, errFileSet, "org.apache.accumulo.core.iterators.Combiner", 163);
	}

}
