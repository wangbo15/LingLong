package edu.pku.sei.conditon.simple.expr;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExprNormalizationTest {

	@Test
	public void testNegtiveNum() {
		String ori = ExprNormalization.normalize("$ >= -1d", "double");
		assertEquals(ori, "$ >= -1");
		
		ori = ExprNormalization.normalize("$ >= -23.1d", "double");
		assertEquals(ori, "$ >= -23.1");
		
		ori = ExprNormalization.normalize("$ >= -1.01", "double");
		assertEquals(ori, "$ >= -1.01");

		
		ori = ExprNormalization.normalize("$ >= -1", "int");
		assertEquals(ori, "$ > -2");
		
	}
	
	@Test
	public void testPositiveNum() {
		String ori = ExprNormalization.normalize("$ >= 123.0d", "double");
		assertEquals(ori, "$ >= 123");
		
		ori = ExprNormalization.normalize("$ >= 23.1d", "double");
		assertEquals(ori, "$ >= 23.1");
		
		ori = ExprNormalization.normalize("$ >= 1.0", "double");
		assertEquals(ori, "$ >= 1");

		
		ori = ExprNormalization.normalize("$ >= 1", "int");
		assertEquals(ori, "$ > 0");
	}

	@Test
	public void testCompactExpr(){
		String ori = ExprNormalization.normalize("$==0", "double");
		assertEquals(ori, "$ == 0");

		
		ori = ExprNormalization.normalize("$==Integer.MIN_VALUE", "int");
		assertEquals(ori, "$ == Integer.MIN_VALUE");

		
//		ori = ExprNormalization.normalize("$>trial", "int");
//		assertEquals(ori, "$ > trial");

		
		ori = ExprNormalization.normalize("$>1e-6", "double");
		assertEquals(ori, "$ > 1e-6");

		
//		ori = ExprNormalization.normalize("$.numerator==Integer.MIN_VALUE", "Fraction");
//		assertEquals(ori, "$.numerator == Integer.MIN_VALUE");

		
		ori = ExprNormalization.normalize("$ >=1", "double");
		assertEquals(ori, "$ >= 1");

		
		ori = ExprNormalization.normalize("$ >=0", "int");
		assertEquals(ori, "$ > -1");


	}
	
}
