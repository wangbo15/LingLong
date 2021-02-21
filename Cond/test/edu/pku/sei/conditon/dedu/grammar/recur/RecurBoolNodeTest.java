package edu.pku.sei.conditon.dedu.grammar.recur;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.pku.sei.conditon.dedu.grammar.Tree;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;

public class RecurBoolNodeTest {

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

	private static RecurBoolNode getNodeRoot(String expression) {
		Tree tree = RecurGrammarTest.getTree(expression);
		return (RecurBoolNode) tree.getRoot();
	}
	
	@Test
	public void test() {
		RecurBoolNode root = getNodeRoot("a + b > 10 && (a > 0 || b > 0)");
		RecurBoolNode newRoot = RecurBoolNode.deepCloneFromRootForSynthesis(root);
		assertEquals(newRoot.getOpcode(), Opcode.AND);
		assertEquals(newRoot.getChild0().getOpcode(), Opcode.NONE);
		assertEquals(newRoot.getChild1().getOpcode(), Opcode.OR);
		assertEquals(newRoot.toString(), root.toString());
		
		assertNotSame(newRoot, root);
		assertNotSame(newRoot.getChild0(), root.getChild0());
		assertNotSame(newRoot.getChild1().getChild1(), root.getChild1().getChild1());
		assertNotSame(newRoot.getChild1().getChild0(), root.getChild1().getChild0());
	}

}
