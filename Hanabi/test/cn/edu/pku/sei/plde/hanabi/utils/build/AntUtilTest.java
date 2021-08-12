package cn.edu.pku.sei.plde.hanabi.utils.build;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.build.AntUtil;

public class AntUtilTest extends BuildTestAbs{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String root = Config.D4J_SRC_ROOT + "/math/math_32_buggy/";
		Map<String, String> conf = AntUtil.config("math_32", root);
		
		assertTrue(BuildTestAbs.checkKeySet(conf.keySet()));
		
		for(Entry<String, String> entry: conf.entrySet()) {
			System.out.println(entry);
		}
	}

}
