package cn.edu.pku.sei.plde.hanabi.fixer.bdj.acc;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.fixer.MLConditionFixerTest;
import cn.edu.pku.sei.plde.hanabi.main.Main;

/**
 * Test suite for  bugs-dot-jar accumulo
 * Total: 16
 * Ignore: 5
 *
 */
public class BugsDotJarAccumuloTest implements  MLConditionFixerTest{

	private static final String PROJECT = "accumulo";
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_151_b007b22e() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 151));
	}
	
	/**
	 * Flaky-test
	 */
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_907_4aeaeb2a() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 907));
	}
	
	/**
	 * The failure test is beyond the core module
	 */
	@Ignore
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_1544_0cf2ff72() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 1544));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_1730_872b6db3() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 1730));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_1661_13eb19c2() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 1661));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_2659_019edb16() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 2659));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_2713_6138a80f() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 2713));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_2748_ff8c2383() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 2748));
	}
	
	/**
	 * The failure test is in the module MiniCluster, which beyond the core module,
	 */
	@Ignore
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3150_72fd6bec() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3150));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3218_1b35d263() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3218));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3229_891584fb() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3229));
	}
	
	/**
	 * Still has error of test
	 */
	@Ignore
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3746_47c64d9a() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3746));
	}
	
	/**
	 * Still has error of test
	 */
	@Ignore
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3897_699b8bf0() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3897));
	}
	
	/**
	 * Still has error of test
	 */
	@Ignore
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_3945_36225565() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 3945));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_4029_5ca779a0() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 4029));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_ACCUMULO_4098_a2c2d38a() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 4098));
	}
	
}
