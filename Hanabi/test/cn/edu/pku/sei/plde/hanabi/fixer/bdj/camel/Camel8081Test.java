package cn.edu.pku.sei.plde.hanabi.fixer.bdj.camel;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.fixer.MLConditionFixerTest;
import cn.edu.pku.sei.plde.hanabi.main.Main;

/**
 * Change to the official repo
 */
public class Camel8081Test implements MLConditionFixerTest {

	private static final String PROJECT = "camel";

	private static final String MODULE = "camel-core";
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_8081() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 8081, MODULE));
	}
}
