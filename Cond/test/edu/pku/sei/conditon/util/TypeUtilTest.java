package edu.pku.sei.conditon.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TypeUtilTest {

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
	public void testIsSmpleDouble() {
		assertTrue(TypeUtil.isSmpleDouble("12.333"));
		assertFalse(TypeUtil.isSmpleDouble("1234567"));
		
		assertTrue(TypeUtil.isSmpleDouble("2.2204e-16"));
		assertTrue(TypeUtil.isSmpleDouble("1.0e-3"));

		
		assertTrue(TypeUtil.isSmpleDouble("0d"));
		assertTrue(TypeUtil.isSmpleDouble("-1d"));
		assertTrue(TypeUtil.isSmpleDouble("1D"));

		assertFalse(TypeUtil.isSmpleDouble("12dsdfs"));
		assertFalse(TypeUtil.isSmpleDouble("12e3"));
	}
	
	@Test
	public void testIsTailDouble() {
		assertTrue(TypeUtil.isTailDouble("0d"));
		assertTrue(TypeUtil.isTailDouble("-1d"));
		assertTrue(TypeUtil.isTailDouble("1D"));

		assertFalse(TypeUtil.isSmpleDouble("0dd"));
		assertFalse(TypeUtil.isSmpleDouble("1dd"));

		assertFalse(TypeUtil.isTailDouble("12"));
		assertFalse(TypeUtil.isTailDouble("12e3"));
	}
	
	@Test
	public void testIsSimpleNum() {
		assertTrue(TypeUtil.isSimpleNum("1"));

//		assertTrue(TypeUtil.isSimpleNum("1L"));

//		assertTrue(TypeUtil.isSimpleNum("0x123456"));
		
		assertTrue(TypeUtil.isSimpleNum("0.0"));
		
		assertTrue(TypeUtil.isSimpleNum("0.5"));
		
		assertTrue(TypeUtil.isSimpleNum("6.0"));
		
		assertFalse(TypeUtil.isSimpleNum("null"));

//		assertTrue(TypeUtil.isNum("-1d"));
//		assertTrue(TypeUtil.isNum("1D"));

		assertTrue(TypeUtil.isSimpleNum("12"));
		assertTrue(TypeUtil.isSimpleNum("12e3"));
	}
}
