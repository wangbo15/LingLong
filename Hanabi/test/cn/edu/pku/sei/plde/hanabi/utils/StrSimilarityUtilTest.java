package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.utils.StrSimilarityUtil;

public class StrSimilarityUtilTest {

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
		String str1 = "localset > 0";
		String str2 = "localset >= 0";
		assertEquals(1, StrSimilarityUtil.ld(str1, str2));
		assertTrue(StrSimilarityUtil.sim(str1, str2) > 0.9);
	}

}
