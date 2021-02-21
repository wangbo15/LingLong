package edu.pku.sei.conditon.simple.auxiliary;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.pku.sei.conditon.auxiliary.ArgumentNormolizeVisitor;
import edu.pku.sei.conditon.util.JavaFile;

public class ArgumentNormolizeVisitorTest {

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
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7("Math.abs(locn.x - prev.x) > hysteresis || Math.abs(locn.y - prev.y) > hysteresis", ASTParser.K_EXPRESSION);
		ArgumentNormolizeVisitor visitor = new ArgumentNormolizeVisitor();
		expr.accept(visitor);
		System.out.println(expr);
		assertEquals(expr.toString().trim(), "Math.abs(p_n) > hysteresis || Math.abs(q_n) > hysteresis");
	}

}
