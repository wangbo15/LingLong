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
public class BugsDotJarCamelTest implements MLConditionFixerTest {

	private static final String PROJECT = "camel";

	private static final String MODULE = "camel-core";
	
	
	/**
	 * remember to run 'mvn clean' first
	 */
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_3388_0919a0f6() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 3388, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_4474_06a8489a() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 4474, MODULE));
	}

	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7130_cc192f87() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7130, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7448_35bde2b2() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 7448, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void testCamel_7459() {
		assertTrue(Main.fixBugsDotJar(PROJECT, 7459, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_8081_2e985f9b() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 8081, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_8592_57f72cd9() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 8592, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_9217_e7ac45b6() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 9217, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_9243_1957a828() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 9243, MODULE));
	}
	
	/****************************************************/
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_3690_2a3f3392() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 3690, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_4388_f39bc60d() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 4388, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_4542_c408c3ed() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 4542, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_4682_1e54865c() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 4682, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_5570_a57830ed() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 5570, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_5704_708e756d() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 5704, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_5707_3f70d612() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 5707, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_5720_4a05eccf() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 5720, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_6987_37e0e6bb() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 6987, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7016_4ed448c7() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7016, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7125_6641f182() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7125, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7209_5f78c646() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7209, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7241_18c23fa8() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7241, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7344_91228815() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7344, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7359_9cb09d14() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7359, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7611_e30f1c53() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7611, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7795_19b2aa31() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7795, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7883_d57f402b() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7883, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_7990_d581c4a4() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 7990, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_8106_39ccf5d6() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 8106, MODULE));
	}
	 
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_8584_dd0f74c0() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 8584, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_9238_169b981e() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 9238, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_9269_62b2042b() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 9269, MODULE));
	}
	
	@Test(timeout=TIME_OUT)
	public void test_CAMEL_9340_1cab39f6() {		
		assertTrue(Main.fixBugsDotJar(PROJECT, 9340, MODULE));
	}
	
}
