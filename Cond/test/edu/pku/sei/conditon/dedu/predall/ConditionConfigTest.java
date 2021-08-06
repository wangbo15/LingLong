package edu.pku.sei.conditon.dedu.predall;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConditionConfigTest {
		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testLoadPythonFile() {
		ConditionConfig c = ConditionConfig.getInstance();
		System.out.println(c.getDumpStr());
	}

}
