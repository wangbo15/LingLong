package cn.edu.pku.sei.plde.hanabi.fixer.d4j;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.fixer.MLConditionFixerTest;
import cn.edu.pku.sei.plde.hanabi.main.Main;

public class MajorTest implements MLConditionFixerTest{
	
	@Test(timeout=TIME_OUT)
	public void testMath3() {
		assertTrue(Main.fixDefects4J("math", 3));
	}
	@Test(timeout=TIME_OUT)
	public void testMath4() {
		assertTrue(Main.fixDefects4J("math", 4));
	}
	@Test(timeout=TIME_OUT)
	public void testMath5() {
		assertTrue(Main.fixDefects4J("math", 5));
	}
	@Test(timeout=TIME_OUT)
	public void testMath15() {
		assertTrue(Main.fixDefects4J("math", 15));
	}
	@Test(timeout=TIME_OUT)
	public void testMath22() {
		assertTrue(Main.fixDefects4J("math", 22));
	}
	@Test(timeout=TIME_OUT)
	public void testMath25() {
		assertTrue(Main.fixDefects4J("math", 25));
	}
	@Test(timeout=TIME_OUT)
	public void testMath26() {
		assertTrue(Main.fixDefects4J("math", 26, true));
	}
	@Test(timeout=TIME_OUT)
	public void testMath32() {
		assertTrue(Main.fixDefects4J("math", 32));
	}
	@Test(timeout=TIME_OUT)
	public void testMath33() {
		assertTrue(Main.fixDefects4J("math", 33));
	}
	@Test(timeout=TIME_OUT)
	public void testMath35() {
		assertTrue(Main.fixDefects4J("math", 35));
	}
	@Test(timeout=TIME_OUT)
	public void testMath46() {
		assertTrue(Main.fixDefects4J("math", 46));
	}
	@Test(timeout=TIME_OUT)
	public void testMath48() {
		assertTrue(Main.fixDefects4J("math", 48));
	}
	@Test(timeout=TIME_OUT)
	public void testMath50() {
		assertTrue(Main.fixDefects4J("math", 50));
	}
	@Test(timeout=TIME_OUT)
	public void testMath53() {
		assertTrue(Main.fixDefects4J("math", 53));
	}
	@Test(timeout=TIME_OUT)
	public void testMath61() {
		assertTrue(Main.fixDefects4J("math", 61));
	}
	@Test(timeout=TIME_OUT)
	public void testMath63() {
		assertTrue(Main.fixDefects4J("math", 63));
	}
	@Test(timeout=TIME_OUT)
	public void testMath73() {
		assertTrue(Main.fixDefects4J("math", 73, true));
	}
	@Test(timeout=TIME_OUT)
	public void testMath82() {
		assertTrue(Main.fixDefects4J("math", 82));
	}
	@Test(timeout=TIME_OUT)
	public void testMath85() {
		assertTrue(Main.fixDefects4J("math", 85));
	}
	@Test(timeout=TIME_OUT)
	public void testMath89() {
		assertTrue(Main.fixDefects4J("math", 89));
	}
	@Test(timeout=TIME_OUT)
	public void testMath90() {
		assertTrue(Main.fixDefects4J("math", 90));
	}
	@Test(timeout=TIME_OUT)
	public void testMath93() {
		assertTrue(Main.fixDefects4J("math", 93));
	}
	@Test(timeout=TIME_OUT)
	public void testMath94() {
		assertTrue(Main.fixDefects4J("math", 94, true));
	}
	@Test(timeout=TIME_OUT)
	public void testMath99() {
		assertTrue(Main.fixDefects4J("math", 99));
	}
	@Test(timeout=TIME_OUT)
	public void testLang2() {
		assertTrue(Main.fixDefects4J("lang", 2));
	}
	@Test(timeout=TIME_OUT)
	public void testLang34() {
		assertTrue(Main.fixDefects4J("lang", 34));
	}
	
	@Test(timeout=TIME_OUT)
	public void testChart1() {
		assertTrue(Main.fixDefects4J("chart", 1));
	}
}
