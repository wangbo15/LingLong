package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.utils.JavaCodeUtil;

public class JavaCodeUtilTest {

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
	public void testIsPackageName() {
		assertFalse(JavaCodeUtil.isPackageName("RedeliveringIssueTest"));
		assertFalse(JavaCodeUtil.isPackageName(".abc"));
		assertFalse(JavaCodeUtil.isPackageName("..abc"));
		assertFalse(JavaCodeUtil.isPackageName("abc."));
		assertFalse(JavaCodeUtil.isPackageName("abc.."));
		assertFalse(JavaCodeUtil.isPackageName("abc..abc"));
		assertFalse(JavaCodeUtil.isPackageName("org.Apache.camel"));

		assertTrue(JavaCodeUtil.isPackageName("org.apache.camel.language.simple.ast"));
		assertTrue(JavaCodeUtil.isPackageName("org"));
		assertTrue(JavaCodeUtil.isPackageName("org.apache"));
	}

	@Test
	public void testIsTestCase() {
		assertTrue(JavaCodeUtil.isTestCase("RedeliveringIssueTest"));
		assertTrue(JavaCodeUtil.isTestCase("org.apache.camel.impl.DefaultCamelContextWithLifecycleStrategyRestartTest"));
		
		assertFalse(JavaCodeUtil.isTestCase("Test"));
		assertFalse(JavaCodeUtil.isTestCase("RedeliveringTestIssue"));
		assertFalse(JavaCodeUtil.isTestCase("redeliveringTest"));
		assertFalse(JavaCodeUtil.isTestCase("AAA.abc.redeliveringTest"));
		assertFalse(JavaCodeUtil.isTestCase(".abc.redeliveringTest"));
	}

}
