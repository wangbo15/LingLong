package edu.pku.sei.conditon.dedu.grammar.recur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
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

	public static Tree<RecurBoolNode> getTree(String cond) {
		String condition = cond;
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);
		
		RecurGrammar grammar = new RecurGrammar();
		
		Tree<RecurBoolNode> tree = grammar.generateTree(expr);
		tree.dump();
		return tree;
	}
	
	@Test
	public void testBFSTraverse() {
		Tree<RecurBoolNode> tree0 = getTree("a != null && !a.equals(b)");
		assertEquals(tree0.broadFristSearchTraverse().size(), 4);
		
		Tree<RecurBoolNode> tree1 = getTree("a + b < c");
		List<RecurBoolNode> list = tree1.broadFristSearchTraverse();
		assertEquals(list.size(), 1);
		assertEquals(tree1.toString(), "#");
		assertEquals(list.get(0).getExpr().toString(), "a + b < c");
		
		Tree<RecurBoolNode> tree2 = getTree("a + b < c || a + b > c");
		assertEquals(tree2.broadFristSearchTraverse().size(), 3);
		
		// test cache
		assertEquals(tree2.broadFristSearchTraverse().hashCode(), tree2.broadFristSearchTraverse().hashCode());
		assertNotEquals(tree1.broadFristSearchTraverse().hashCode(), tree2.broadFristSearchTraverse().hashCode());
	}
	
	@Test
	public void testBUTraverse() {
		//
		Tree<RecurBoolNode> tree0 = getTree("a != null && !a.equals(b)");
		assertEquals(tree0.bottomUpTraverse().size(), 4);
		
		//
		Tree<RecurBoolNode> tree1 = getTree("a + b < c");
		List<RecurBoolNode> list = tree1.bottomUpTraverse();
		assertEquals(list.size(), 1);
		assertEquals(tree1.toString(), "#");
		assertEquals(list.get(0).getExpr().toString(), "a + b < c");
		
		//
		Tree<RecurBoolNode> tree2 = getTree("a + b < c || a + b > c");
		assertEquals(tree2.bottomUpTraverse().size(), 3);
		
		//
		Tree<RecurBoolNode> tree3 = getTree("( a || !b) && (!c || d)");
		List<RecurBoolNode> list1 = tree3.bottomUpTraverse();
		assertEquals(tree3.getHight(), 4);
		assertEquals(list1.size(), 9);
		assertEquals(list1.get(0).getExpr().toString(), "a");
		assertEquals(list1.get(1).getExpr().toString(), "a || !b");
		assertEquals(list1.get(3).getExpr().toString(), "!b");
		assertEquals(list1.get(6).getExpr().toString(), "!c");
		assertEquals(list1.get(7).getExpr().toString(), "d");
		assertEquals(list1.get(8).getExpr().toString(), "c");
		
		//
		Tree<RecurBoolNode> tree4 = getTree("(a + 1 > 0) && (! (c.foo() || d.bar()) || e)");
		List<RecurBoolNode> list2 = tree4.bottomUpTraverse();
		assertEquals(tree4.getHight(), 5);
		assertEquals(list2.size(), 8);
		assertTrue(RecurBoolNode.isRoot(list2.get(1)));
		assertTrue(RecurBoolNode.isRoot(list2.get(0).getParent()));
		assertTrue(RecurBoolNode.isRoot(list2.get(2).getParent()));
		
		// test cache
		assertEquals(tree3.broadFristSearchTraverse().hashCode(), tree3.broadFristSearchTraverse().hashCode());
		assertNotEquals(tree1.broadFristSearchTraverse().hashCode(), tree2.broadFristSearchTraverse().hashCode());
	}
	
	@Test
	public void testInfix() {
		Tree<RecurBoolNode> tree;
		RecurBoolNode root;
		List<RecurBoolNode> list;
		
		//
		tree = getTree("a + b > 10 && (a > 0 || b > 0)");
		root = (RecurBoolNode) tree.getRoot();
		list = tree.broadFristSearchTraverse();
		assertEquals(3, tree.getHight());
		assertEquals(3, tree.getLeafNum());
		assertEquals(5, tree.getNodeNum());
		assertEquals(tree.getNodeNum(), tree.broadFristSearchTraverse().size());
		assertEquals("(# && (# || #))", tree.toString());
		assertEquals(root.getChild0().getOpcode(), Opcode.NONE);
		assertEquals(root.getChild1().getOpcode(), Opcode.OR);
		assertEquals(root.getChild1().getChild1().getOpcode(), Opcode.NONE);
		assertEquals(list.get(0).getHight(), 3);
		assertEquals(list.get(1).getHight(), 1);
		assertEquals(list.get(2).getHight(), 2);
		assertEquals(list.get(3).getHight(), 1);
		assertEquals(list.get(4).getHight(), 1);
		
		//
		tree = getTree("a + b > 10 && (c || d.foo())");
		list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 5);
		assertEquals(root.getChild1().getOpcode(), Opcode.OR);
		assertTrue(list.get(0).getExpr() instanceof InfixExpression);
		assertTrue(list.get(1).getExpr() instanceof InfixExpression);
		assertTrue(list.get(2).getExpr() instanceof InfixExpression);
		assertTrue(list.get(3).getExpr() instanceof SimpleName);
		assertTrue(list.get(4).getExpr() instanceof MethodInvocation);
		
		//
		tree = getTree("a + b > 10 && !(a > 0 || b > 0)");
		root = (RecurBoolNode) tree.getRoot();
		list = tree.broadFristSearchTraverse();
		assertEquals(4, tree.getHight());
		assertEquals(3, tree.getLeafNum());
		assertEquals(6, tree.getNodeNum());
		assertEquals("(# && (! (# || #)))", tree.toString());
		assertEquals(root.getChild1().getOpcode(), Opcode.NOT);
		assertEquals(root.getChild1().getChild0().getOpcode(), Opcode.OR);
		assertEquals(root.getChild1().getChild0().getChild0().getOpcode(), Opcode.NONE);
		assertEquals(root.getChild1().getChild0().getChild1().getOpcode(), Opcode.NONE);
		
		assertEquals(root.getHight(), 4);
		assertEquals(root.getChild0().getHight(), 1);
		assertEquals(root.getChild1().getHight(), 3);
		assertEquals(root.getChild1().getChild0().getHight(), 2);
		assertEquals(root.getChild1().getChild0().getChild1().getHight(), 1);

		assertTrue(list.get(0).getExpr() instanceof InfixExpression);
		assertTrue(list.get(1).getExpr() instanceof InfixExpression);
		assertTrue(list.get(2).getExpr() instanceof PrefixExpression);
		
		//
		tree = getTree("expansionMode != MULTIPLICATIVE_MODE && expansionMode != ADDITIVE_MODE");
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(2, tree.getHight());
		assertEquals("(# && #)", tree.toString());
		assertEquals(2, tree.getLeafNum());
		
		//
		tree = getTree("denominator == Integer.MIN_VALUE && (numerator & 1) == 0");
		root = (RecurBoolNode) tree.getRoot();
		list = tree.broadFristSearchTraverse();
		assertEquals(2, tree.getHight());
		assertEquals("(# && #)", tree.toString());
		assertEquals(2, tree.getLeafNum());
		assertTrue(list.get(0).getExpr() instanceof InfixExpression);
		assertTrue(list.get(1).getExpr() instanceof InfixExpression);
		assertTrue(list.get(2).getExpr() instanceof InfixExpression);
	}
	
	@Test
	public void testPrefix() {
		Tree<RecurBoolNode> tree;
		//RecurBoolNode root;
		List<RecurBoolNode> list;
		
		//
		tree = getTree("!a");
		list = tree.broadFristSearchTraverse();
		assertEquals(2, tree.getHight());
		assertEquals(1, tree.getLeafNum());
		assertEquals(2, list.size());
		assertEquals(tree.getRoot().getHight(), 2);
		assertEquals(tree.getRoot().getDepthLevel(), 0);
		assertEquals(tree.getRoot().getChild0().getDepthLevel(), 1);
		
		//
		tree = getTree("!(a > b)");
		list = tree.broadFristSearchTraverse();
		assertEquals(2, tree.getHight());
		assertEquals(1, tree.getLeafNum());
		assertEquals(2, list.size());
		assertTrue(list.get(1).getExpr() instanceof InfixExpression);

		//
		tree = getTree("!!(a > b)");
		list = tree.broadFristSearchTraverse();
		assertEquals(3, tree.getHight());
		assertEquals(3, list.size());
		Expression tmpA = list.get(1).getExpr(); // ! (a > b)
		Expression tmpB = list.get(2).getExpr(); // a > b
		assertTrue(tmpA instanceof PrefixExpression);
		assertTrue(tmpB instanceof InfixExpression);
		Expression tmpC = (Expression) tmpB.getParent(); // (a > b)
		assertEquals(tmpC.getParent(), list.get(1).getExpr());
	}
	
	@Test
	public void testStaticMtdInvo() {
		Tree<RecurBoolNode> tree;
		tree = getTree("DateTimeUtils.isContiguous(partial)");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testMtdInvo() {
		Tree<RecurBoolNode> tree;
		List<RecurBoolNode> list;
		
		//
		tree = getTree("list.size() > i");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());

		//
		tree = getTree("list.isEmpty()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("lise.size() < 100 || list.isEmpty()");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 3);
		
		//
		tree = getTree("Math.abs(x - s.lastX) > s.dX");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 1);
		
		//
		tree = getTree("filename.substring(filename.length() - 5, filename.length()).equals(\".jpeg\")");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 1);
		assertTrue(list.get(0).getExpr() instanceof MethodInvocation);
	}
	
	@Test
	public void testMtdInvo_1 (){
		Tree<RecurBoolNode> tree;
		List<RecurBoolNode> list;
		
		//
		tree = getTree("index < (input.length() - 1) && Character.isDigit(input.charAt(index + 1))");
		list = tree.broadFristSearchTraverse();
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals(list.size(), 3);
		assertEquals(list.get(1).getExpr().toString(), "index < (input.length() - 1)");
		assertEquals(list.get(2).getExpr().toString(), "Character.isDigit(input.charAt(index + 1))");

		//
		tree = getTree("Double.isInfinite(p2) || Double.isInfinite(q2)");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		list = tree.broadFristSearchTraverse();
		assertEquals(list.size(), 3);
		RecurBoolNode left = list.get(1);
		assertEquals(left.getExpr().toString(), "Double.isInfinite(p2)");
		RecurBoolNode right = list.get(2);
		assertEquals(right.getExpr().toString(), "Double.isInfinite(q2)");
	}
	
	@Test
	public void testName() {
		Tree<RecurBoolNode> tree;
		
		//
		tree = getTree("a.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("a.b.c.d || e");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals(tree.getRoot().getChild1().getExpr().toString(), "e");
		assertEquals("(# || #)", tree.toString());
		
		//
		tree = getTree("iConvertByWeekyear");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testThis() {
		Tree<RecurBoolNode> tree;
		//
		tree = getTree("this.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("this.isCut()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("this.name.equals(library.name)");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
	}
	
	@Test
	public void testSuper() {
		Tree<RecurBoolNode> tree;
		
		//
		tree = getTree("super.b");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("super.isCut()");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("super.isCut() && this.name.equals(library.name)");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# && #)", tree.toString());
	}
	
	@Test
	public void testArray() {
		Tree<RecurBoolNode> tree;
		
		//
		tree = getTree("this.showYear[month]");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		//
		tree = getTree("showYear[1]");
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		assertTrue(tree.getRoot().getExpr() instanceof ArrayAccess);
	}
	
	@Test
	public void testInstanceof() {
		Tree<RecurBoolNode> tree;
		RecurBoolNode root;
		List<RecurBoolNode> bfsList;
		
		//
		tree = getTree("partial instanceof LocalDateTime");
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		assertTrue(root.getExpr() instanceof InstanceofExpression);
		
		//
		tree = getTree("obj != null && obj instanceof String");
		bfsList = tree.broadFristSearchTraverse();
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals(0, root.getDepthLevel());
		assertEquals(Opcode.AND, root.getOpcode());
		assertEquals("(# && #)", tree.toString());
		assertEquals(1, bfsList.get(1).getDepthLevel());
		assertTrue(bfsList.get(0).getExpr() instanceof InfixExpression);
		assertTrue(bfsList.get(1).getExpr() instanceof InfixExpression);
		assertTrue(bfsList.get(2).getExpr() instanceof InstanceofExpression);
		assertEquals(bfsList.get(2).getHight(), 1);
		assertEquals(bfsList.get(2).getDepthLevel(), 1);

		//
		tree = getTree("o instanceof Number || o instanceof String");
		bfsList = tree.broadFristSearchTraverse();
		root = (RecurBoolNode) tree.getRoot();
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals(0, root.getDepthLevel());
		assertEquals(Opcode.OR, root.getOpcode());
		assertEquals("(# || #)", tree.toString());
		assertEquals(1, bfsList.get(1).getDepthLevel());
		assertTrue(bfsList.get(0).getExpr() instanceof InfixExpression);
		assertTrue(bfsList.get(1).getExpr() instanceof InstanceofExpression);
		assertTrue(bfsList.get(2).getExpr() instanceof InstanceofExpression);
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

	@Test
	public void testParenthesizedExpression_0() {
		String expr = "(a == null) || (b == null)";
		Tree<RecurBoolNode> tree = getTree(expr);
		assertEquals("(# || #)", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 3);
		
		RecurBoolNode left = bfsList.get(1);
		System.out.println(left.getExpr().toString());
		
		RecurBoolNode right = bfsList.get(2);
		System.out.println(right.getExpr().toString());
	}
	
	@Test
	public void testParenthesizedExpression_1() {
		String expr = "(a == null)";
		Tree<RecurBoolNode> tree = getTree(expr);
		assertEquals("#", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 1);
		RecurBoolNode root = bfsList.get(0);
		assertEquals(root.getOpcode(), Opcode.NONE);
		assertEquals(root.getDepthLevel(), 0);
		assertEquals(root.getHight(), 1);
	}
	
	@Test
	public void testParenthesizedExpression_2() {
		String expr = "((a == null) || (b == null))";
		Tree<RecurBoolNode> tree = getTree(expr);
		assertEquals("(# || #)", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 3);
	}

	@Test
	public void testParenthesizedExpression_3() {
		String expr = "!((a == null))";
		Tree<RecurBoolNode> tree = getTree(expr);
		assertEquals("(! #)", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 2);
	}
	
	@Test
	public void testParenthesizedExpression_4() {
		Tree<RecurBoolNode> tree = getTree("lise.size() < 100 || (list.isEmpty())");
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();

		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		
		assertEquals(bfsList.size(), 3);
		
		RecurBoolNode root = bfsList.get(0);
		RecurBoolNode left = bfsList.get(1);
		RecurBoolNode right = bfsList.get(2);
		
		assertEquals(0, root.getDepthLevel());
		assertEquals(2, root.getHight());
		assertEquals(1, left.getDepthLevel());
		assertEquals(1, left.getHight());
		assertEquals(1, right.getDepthLevel());
		assertEquals(1, right.getHight());
		
		assertEquals(root, tree.getRoot());
		InfixExpression rootExpr = (InfixExpression) root.getExpr();
		assertEquals(rootExpr.getOperator().toString(), "||");
		InfixExpression leftExpr = (InfixExpression) left.getExpr();
		assertEquals(leftExpr.getOperator().toString(), "<");
		assertTrue(right.getExpr() instanceof MethodInvocation);
	}
	
	@Test
	public void testParenthesizedExpression_5() {
		Tree<RecurBoolNode> tree = getTree("(list.isEmpty())");
		RecurBoolNode root = tree.getRoot();
		assertEquals(1, tree.getHight());
		assertEquals(1, tree.getNodeNum());
		assertEquals("#", tree.toString());
		
		System.out.println("ROOT: " + root.getExpr());
		assertTrue(root.getExpr() instanceof MethodInvocation);
		assertTrue(root.getExpr().getParent() instanceof ParenthesizedExpression);
	}
	
	@Test
	public void testParenthesizedExpression_6() {
		Tree<RecurBoolNode> tree = getTree("(empiricalDistribution == null) || (empiricalDistribution.getBinStats().size() == 0)");
		assertEquals(2, tree.getHight());
		assertEquals(3, tree.getNodeNum());
		assertEquals("(# || #)", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 3);
		
		RecurBoolNode root = bfsList.get(0);
		RecurBoolNode left = bfsList.get(1);
		RecurBoolNode right = bfsList.get(2);
		
		assertEquals(0, root.getDepthLevel());
		assertEquals(1, left.getDepthLevel());
		assertEquals(1, right.getDepthLevel());
		
		System.out.println();
		System.out.println("ROOT: " + root.getExpr());
		System.out.println("L-OP: " + left.getExpr());
		System.out.println("R-OP: " + right.getExpr());
		
		assertTrue(root.getExpr() instanceof InfixExpression);
		assertTrue(left.getExpr() instanceof InfixExpression);
		assertTrue(right.getExpr() instanceof InfixExpression);
		
		InfixExpression rootExpr = (InfixExpression) root.getExpr();
		assertEquals(rootExpr.getOperator().toString(), "||");
		
		InfixExpression leftExpr = (InfixExpression) left.getExpr();
		assertEquals(leftExpr.getOperator().toString(), "==");
		
		InfixExpression rightExpr = (InfixExpression) right.getExpr();
		assertEquals(rightExpr.getOperator().toString(), "==");
	}
	
	@Test
	public void testParenthesizedExpression_7() {
		String expr = "(!(!a.b))";
		Tree<RecurBoolNode> tree = getTree(expr);
		assertEquals("(! (! #))", tree.toString());
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		assertEquals(bfsList.size(), 3);
		RecurBoolNode last = bfsList.get(2);
		assertTrue(last.getExpr() instanceof QualifiedName);
		assertEquals(2, last.getDepthLevel());
	}
	
}
