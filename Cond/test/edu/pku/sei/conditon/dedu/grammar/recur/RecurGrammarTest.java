package edu.pku.sei.conditon.dedu.grammar.recur;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.pku.sei.conditon.dedu.grammar.Tree;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.util.JavaFile;

public class RecurGrammarTest {

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

	public static Tree getTree(String cond) {
		String condition = cond;
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		
		RecurGrammar grammar = new RecurGrammar();
		
		Tree tree = grammar.generateTree(expr);
		tree.dump();
		return tree;
	}
	
	@Test
	public void testBFSTraverse() {
		Tree tree;
		tree = getTree("a != null && !a.equals(b)");
		assertEquals(tree.broadFristSearchTraverse().size(), 4);
		
		tree = getTree("a + b < c");
		List<RecurBoolNode> list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 1);
		assertEquals(tree.toString(), "#");
		assertEquals(list.get(0).getExpr().toString(), "a + b < c");
		
		tree = getTree("a + b < c || a + b > c");
		assertEquals(tree.broadFristSearchTraverse().size(), 3);
	}
	
	@Test
	public void testInfix() {
		Tree tree;
		RecurBoolNode root;
		tree = getTree("a + b > 10 && (a > 0 || b > 0)");
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(3, tree.getHight());
		assertEquals(3, tree.getLeafNum());
		assertEquals(5, tree.getNodeNum());
		assertEquals(tree.getNodeNum(), tree.broadFristSearchTraverse().size());
		assertEquals("(# && (# || #))", tree.toString());
		assertEquals(root.getChild0().getOpcode(), Opcode.NONE);
		assertEquals(root.getChild1().getOpcode(), Opcode.OR);
		assertEquals(root.getChild1().getChild1().getOpcode(), Opcode.NONE);
		
		tree = getTree("a + b > 10 && !(a > 0 || b > 0)");
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(4, tree.getHight());
		assertEquals(3, tree.getLeafNum());
		assertEquals(6, tree.getNodeNum());
		assertEquals("(# && (! (# || #)))", tree.toString());
		assertEquals(root.getChild1().getOpcode(), Opcode.NOT);
		assertEquals(root.getChild1().getChild0().getOpcode(), Opcode.OR);
		assertEquals(root.getChild1().getChild0().getChild0().getOpcode(), Opcode.NONE);
		assertEquals(root.getChild1().getChild0().getChild1().getOpcode(), Opcode.NONE);
	}
	
	@Test
	public void testPrefix() {
		Tree tree;
		tree = getTree("!a");
		assertEquals(2, tree.getHight());
		assertEquals(1, tree.getLeafNum());
		
		tree = getTree("!(a > b)");

		assertEquals(2, tree.getHight());
		assertEquals(1, tree.getLeafNum());

		tree = getTree("!!(a > b)");
		assertEquals(3, tree.getHight());
	}
	
	@Test
	public void testStaticMtdInvo() {
		Tree tree;
		tree = getTree("DateTimeUtils.isContiguous(partial)");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testParenthesized() {
		Tree tree;
		tree = getTree("lise.size() < 100 || (list.isEmpty())");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		
		tree = getTree("(list.isEmpty())");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testMtdInvo() {
		Tree tree;
		tree = getTree("list.size() > i");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());

		tree = getTree("list.isEmpty()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("lise.size() < 100 || list.isEmpty()");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		
		tree = getTree("Math.abs(x - s.lastX) > s.dX");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("filename.substring(filename.length() - 5, filename.length()).equals(\".jpeg\")");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testMtdInvo_1 (){
		Tree tree = getTree("index < (input.length() - 1) && Character.isDigit(input.charAt(index + 1))");
		
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		
		assertEquals(bfsList.size(), 3);

		assertEquals(bfsList.get(1).getExpr().toString(), "index < (input.length() - 1)");
		assertEquals(bfsList.get(2).getExpr().toString(), "Character.isDigit(input.charAt(index + 1))");

	}
	
	@Test
	public void testName() {
		Tree tree;
		tree = getTree("a.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("iConvertByWeekyear");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testThis() {
		Tree tree;
		tree = getTree("this.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("this.isCut()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("this.name.equals(library.name) ");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testSuper() {
		Tree tree;
		tree = getTree("super.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		tree = getTree("super.isCut()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		
	}
	
	@Test
	public void testArray() {
		Tree tree;
		tree = getTree("this.showYear[month]");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());		
	}
	
	@Test
	public void testInstanceof() {
		Tree tree;
		tree = getTree("partial instanceof LocalDateTime");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());

		tree = getTree("obj != null && obj instanceof String");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		RecurBoolNode root = (RecurBoolNode) tree.getRoot();
		assertEquals(0, root.getDepthLevel());
		assertEquals(Opcode.AND, root.getOpcode());
		assertEquals("(# && #)", tree.toString());
	}
	
	@Test
	public void testArrayAccess_0() {
		Tree<RecurBoolNode> tree = getTree("mat[i]");
		assertEquals("#", tree.toString());
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();

		assertEquals(bfsList.size(), 1);
	}

	@Test
	public void testArrayAccess_1() {
		Tree<RecurBoolNode> tree;

		String expr = "i == (realEigenvalues.length - 1) || Precision.equals(matT[i + 1][i],0.0,EPSILON)";

		tree = getTree(expr);
		assertEquals("(# || #)", tree.toString());
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();

		assertEquals(bfsList.size(), 3);

		assertEquals(bfsList.get(0).getExpr().toString(), expr);
		assertEquals(bfsList.get(1).getExpr().toString(), "i == (realEigenvalues.length - 1)");
		assertEquals(bfsList.get(2).getExpr().toString(), "Precision.equals(matT[i + 1][i],0.0,EPSILON)");
	}

	@Test
	public void testCastExpression() {
		Tree<RecurBoolNode> tree = getTree("(Boolean) a.isArray()");
		assertEquals("#", tree.toString());
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();

		assertEquals(bfsList.size(), 1);
	}


}
