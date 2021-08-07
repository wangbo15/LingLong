package cn.edu.pku.sei.plde.hanabi.fixer.bdj.camel;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.fixer.MLConditionFixerTest;
import cn.edu.pku.sei.plde.hanabi.main.Main;

/**
 * Test suite for  bugs-dot-jar camel
 * 
 * Need to use aliyun maven repo
 *
 */
public class Camel5704Test implements MLConditionFixerTest {

	private static final String PROJECT = "camel";

	private static final String MODULE = "camel-core";
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_5704() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 5704, MODULE));
	}
}
