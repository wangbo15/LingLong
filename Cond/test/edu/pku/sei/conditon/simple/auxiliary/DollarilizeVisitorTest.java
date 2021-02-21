package edu.pku.sei.conditon.simple.auxiliary;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.util.JavaFile;

public class DollarilizeVisitorTest {

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
	public void testInfix() {
		String condition = "var.getVar() > var2.getVar2() + 1000";
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		
		String dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "$.getVar() > $.getVar2() + 1000");
		
		condition = "abs < 1.0";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "$ < 1.0");

		condition = "var < DIM";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "$ < DIM");
		
		condition = "i < x.length";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "$ < $.length");
		
		condition = "j<mant.length";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "$ < $.length");
		
		condition = "FastMath.signum(y1) + FastMath.signum(y) == 0";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		assertEquals(dollar, "FastMath.signum($) + FastMath.signum($) == 0");

		
		condition = "(include0 ? t >= 0 : t > 0) && (include1 ? t <= 1 : t < 1) && (inflect == null || inflect[1] + 2*inflect[2]*t != 0)";
		expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		dollar = DollarilizeVisitor.naiveDollarilize(expr);
		System.out.println(dollar);
		assertEquals(dollar, "($ ? $ >= 0 : $ > 0) && ($ ? $ <= 1 : $ < 1) && ($ == null || $[1] + 2 * $[2] * $ != 0)");
	}

	@Test
	public void testInstanceof() {
		String condition = "obj1 instanceof Object";
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		
		String dollar = DollarilizeVisitor.naiveDollarilize(expr);
		
		assertEquals(dollar, "$ instanceof Object");
		
	}
	
	@Test
	public void testParse() {
		String condition = "a > b";
		List<String> results = DollarilizeVisitor.parse(condition);
		assertEquals(results.size(), 3);
		assertEquals(results.get(0), "$ > $");
		assertEquals(results.get(1), "a");
		assertEquals(results.get(2), "b");
		
		condition = "str == null";
		results = DollarilizeVisitor.parse(condition);
		assertEquals(results.size(), 2);
		assertEquals(results.get(0), "$ == null");
		assertEquals(results.get(1), "str");
		
		condition = "str.length == 2";
		results = DollarilizeVisitor.parse(condition);
		assertEquals(results.size(), 2);
		assertEquals(results.get(1), "str");
	}
	
}
