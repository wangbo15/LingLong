package edu.pku.sei.conditon.dedu.pred;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;

public class TreePredItemTest {

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

	/**
	 * @return a simple tree for test : ((! ?) && (? || ?))
	 */
	private TreePredItem getSimpleTree_0() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		root.setExpandItem(new RecurNodePredItem(1, 0.5));
		
		TreePredItem cld0 = TreePredItem.getInstance(root);
		root.setChild0(cld0);
		cld0.setExpandItem(new RecurNodePredItem(3, 0.25));
		
		TreePredItem cld00 = TreePredItem.getInstance(cld0);
		cld0.setChild0(cld00);
		cld00.setExpandItem(new RecurNodePredItem(0, 0.9));
		
		TreePredItem cld1 = TreePredItem.getInstance(root);
		root.setChild1(cld1);
		cld1.setExpandItem(new RecurNodePredItem(2, 0.5));
		
		TreePredItem cld10 = TreePredItem.getInstance(cld1);
		cld1.setChild0(cld10);
		cld10.setExpandItem(new RecurNodePredItem(0, 0.75));
		
		TreePredItem cld11 = TreePredItem.getInstance(cld1);
		cld1.setChild1(cld11);
		cld11.setExpandItem(new RecurNodePredItem(0, 0.7));
		
		return root;
	}
	
	/**
	 * @return a simple tree for test : (# && (? || ?))
	 */
	private TreePredItem getSimpleTree_1() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		root.setExpandItem(new RecurNodePredItem(1, 0.5));
		
		TreePredItem cld0 = TreePredItem.getInstance(root);
		root.setChild0(cld0);
		cld0.setExpandItem(new RecurNodePredItem(0, 0.7));
		
		TreePredItem cld1 = TreePredItem.getInstance(root);
		root.setChild1(cld1);
		cld1.setExpandItem(new RecurNodePredItem(2, 0.5));
		
		TreePredItem cld10 = TreePredItem.getInstance(cld1);
		// not expandItem
		cld1.setChild0(cld10);
		
		TreePredItem cld11 = TreePredItem.getInstance(cld1);
		// not expandItem
		cld1.setChild1(cld11);
		
		return root;
	}
	
	/**
	 * @return a simple tree for test : #
	 */
	private TreePredItem getSimpleTree_2() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		root.setExpandItem(new RecurNodePredItem(Opcode.NONE.toLabel(), 0.7));
		return root;
	}
	
	/**
	 * @return a simple tree for test : ?
	 */
	private TreePredItem getSimpleTree_3() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		return root;
	}
	
	private ExprPredItem getExprPredItemForTest(String fileName, String expr, double score) {
		Expression astnode = ExprPredItem.generateASTNodeForDollarExpr(expr);
		ExprPredItem res = new ExprPredItem(fileName, expr, astnode, score);
		return res;
	}
	/********************************************************************/
	@Test
	public void testGetInstance() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		assertTrue(root.isRoot());
		assertNotEquals(root, null);
		
		TreePredItem newRoot = TreePredItem.getRootInstance(true);
		assertNotEquals(root, newRoot);
	}
	

	@Test
	public void testExpandTreeCompleteTree() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		TreePredItem.expandTreeCompleteTree(root);
		
		assertEquals(root.getCurrentHight(), TreePredItem.MAX_TREE_HIGHT);
		assertEquals(root.getChild0().getChild1().getCurrentHight(), 2);
		//assertNull(root.getChild0().getChild0().getChild0());
		
		assertEquals(root.getCurrentHightToRoot(), 0);
		assertEquals(root.getChild0().getCurrentHightToRoot(), 1);
		assertEquals(root.getChild0().getChild1().getCurrentHightToRoot(), 2);
	}
	
	@Test
	public void testSharedCopyFromLeaf() {
		TreePredItem.setInstanceNum(0);
		//root: ((! ?) && (? || ?))
		TreePredItem root = getSimpleTree_0();
		
		long afterInit = TreePredItem.getInstanceNum();
		// six nodes
		assertEquals(afterInit, 6);
		
		// 'cld00' is the leaf of (! ?)
		TreePredItem cld00 = root.getChild0().getChild0();
		TreePredItem newRoot = TreePredItem.sharedCopyFromLeaf(root, cld00);
		assertEquals(newRoot.isTau(), root.isTau());
		
		long afterCopyCld00 = TreePredItem.getInstanceNum();
		// only instance the node of the left side
		assertEquals(afterCopyCld00, afterInit + 3);
		
		assertEquals(root.toString(), newRoot.toString());
		assertEquals(root.getCurrentLargestDepth(), newRoot.getCurrentLargestDepth());
		assertTrue(root.getChild1() == newRoot.getChild1());
		assertTrue(root != newRoot);
		
		List<TreePredItem> list0 = root.broadFristSearchTraverse();
		List<TreePredItem> list1 = newRoot.broadFristSearchTraverse();
		assertEquals(list0.size(), list1.size());
		for(int i = 0; i < list0.size(); i++) {
			TreePredItem t0 = list0.get(i);
			TreePredItem t1 = list1.get(i);
			assertEquals(t0.getExpandItem(), t1.getExpandItem());
			assertEquals(t0.getCurrentHight(), t1.getCurrentHight());
			assertEquals(t0.getCurrentHightToRoot(), t1.getCurrentHightToRoot());
		}
		
		TreePredItem deepRoot = TreePredItem.deepCloneFromRoot(root);	
		assertEquals(deepRoot.toString(), newRoot.toString());
		
		TreePredItem cld10 = root.getChild1().getChild0();
		TreePredItem newRoot1 = TreePredItem.sharedCopyFromLeaf(root, cld10);
		assertEquals(newRoot1.toString(), root.toString());
		assertEquals(newRoot1.isTau(), root.isTau());
		assertEquals(deepRoot.toString(), newRoot.toString());

		TreePredItem cld11 = root.getChild1().getChild1();
		TreePredItem newRoot2 = TreePredItem.sharedCopyFromLeaf(root, cld11);
		assertEquals(newRoot2.toString(), root.toString());
		assertEquals(newRoot2.isTau(), root.isTau());
		assertEquals(deepRoot.toString(), newRoot.toString());
	}
	
	@Test
	public void testSharedCopyFromLeaf_1() {
		TreePredItem.setInstanceNum(0);
		// root: (# && (? || ?))
		TreePredItem root = getSimpleTree_1();
		
		long afterInit = TreePredItem.getInstanceNum();
		assertEquals(afterInit, 5);
		
		TreePredItem expandPosition = root.getChild1().getChild0();
		TreePredItem newRoot = TreePredItem.sharedCopyFromLeaf(root, expandPosition);
		
		long afterSharedCopy = TreePredItem.getInstanceNum();
		assertEquals(afterSharedCopy, 8);
		
		assertEquals(root.toString(), newRoot.toString());
		assertEquals(root.getCurrentLargestDepth(), newRoot.getCurrentLargestDepth());
		assertTrue(root.getChild0() == newRoot.getChild0());
		assertTrue(root != newRoot);
		assertTrue(root.getChild1().getChild1() == newRoot.getChild1().getChild1());
		
		List<TreePredItem> list0 = root.broadFristSearchTraverse();
		List<TreePredItem> list1 = newRoot.broadFristSearchTraverse();
		assertEquals(list0.size(), list1.size());
		for(int i = 0; i < list0.size(); i++) {
			TreePredItem t0 = list0.get(i);
			TreePredItem t1 = list1.get(i);
			assertEquals(t0.getExpandItem(), t1.getExpandItem());
			assertEquals(t0.getCurrentHight(), t1.getCurrentHight());
			assertEquals(t0.getCurrentHightToRoot(), t1.getCurrentHightToRoot());
		}
	}
	
	@Test
	public void testSharedCopyFromLeaf_2() {
		// root: #
		TreePredItem root = getSimpleTree_2();
		
		TreePredItem newRoot = TreePredItem.sharedCopyFromLeaf(root, root);
		assertEquals(root.toString(), newRoot.toString());
		assertEquals(root.getCurrentLargestDepth(), newRoot.getCurrentLargestDepth());
		assertEquals(root.getCurrentHight(), newRoot.getCurrentHight());
		assertEquals(newRoot.isTau(), root.isTau());
	}
	
	@Test
	public void testSharedCopyFromLeaf_3() {
		// root: ?
		TreePredItem root = getSimpleTree_3();
		
		TreePredItem newRoot = TreePredItem.sharedCopyFromLeaf(root, root);
		assertEquals(root.toString(), newRoot.toString());
		assertEquals(root.getCurrentLargestDepth(), newRoot.getCurrentLargestDepth());
		assertEquals(root.getCurrentHight(), newRoot.getCurrentHight());
		assertEquals(newRoot.isTau(), root.isTau());
	}
	
	@Test
	public void testSharedCopyFromLeaf_4() {
		//root: ((! ?) && (? || ?))
		TreePredItem root = getSimpleTree_0();
		
		TreePredItem cld00 = root.getChild0().getChild0();
		TreePredItem newRoot = TreePredItem.sharedCopyFromLeaf(root, cld00);
		
		ExprPredItem expr00 = getExprPredItemForTest("Cld00.java", "$ < 0", 0.6);
		cld00.setExprItem(expr00);
		TreePredItem newCld00 = newRoot.getChild0().getChild0();
		assertNull(newCld00.getExprItem());

		// set the right side of the root
		TreePredItem cld11 = root.getChild1().getChild1();
		assertEquals(cld11.getParent().getExpandItem().getOpcode(), Opcode.OR);
		
		ExprPredItem expr11 = getExprPredItemForTest("Cld11.java", "$ > 0", 0.7);
		cld11.setExprItem(expr11);
		
		TreePredItem newCld11 = newRoot.getChild1().getChild1();
		assertNull(newCld11.getExprItem());
	}
	
	@Test
	public void testSharedCopyFromRoot() {
		//root: ((! ?) && (? || ?))
		TreePredItem root = getSimpleTree_0();
		TreePredItem.setInstanceNum(0);
		
		TreePredItem newRoot = TreePredItem.sharedCopyFromRoot(root);
		long afterSharedCopy = TreePredItem.getInstanceNum();
		assertEquals(afterSharedCopy, 1);
		
		assertEquals(newRoot.toString(), root.toString());
	}
	
	@Test
	public void testDeepCloneFromRoot() {
		TreePredItem.setInstanceNum(0);
		//root: ((! ?) && (? || ?))
		TreePredItem root = getSimpleTree_0();
		
		long afterInit = TreePredItem.getInstanceNum();
		assertEquals(afterInit, 6);
		
		TreePredItem cld11 = root.getChild1().getChild1();
		assertEquals(cld11.getParent().getExpandItem().getOpcode(), Opcode.OR);
		
		ExprPredItem expr11 = getExprPredItemForTest("Cld11.java", "$ > 0", 0.7);
		cld11.setExprItem(expr11);
		
		TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);	
		
		long afterDeepClone = TreePredItem.getInstanceNum();
		assertEquals(afterDeepClone, 12);
		
		String oriStr = new String(root.toString());
		assertEquals(oriStr, root.toString());
		
		List<TreePredItem> bfsList = root.broadFristSearchTraverse();
		List<TreePredItem> newBfsList = newRoot.broadFristSearchTraverse();
		assertEquals(bfsList.size(), newBfsList.size());
		
		for(int i = 0; i < bfsList.size(); i++) {
			TreePredItem node = bfsList.get(i);
			TreePredItem newNode = newBfsList.get(i);
			assertNotSame(node, newNode);
			assertEquals(node.getCurrentHight(), newNode.getCurrentHight());
			assertEquals(node.getExpandItem(), newNode.getExpandItem());
			assertEquals(node.getCurrentHightToRoot(), newNode.getCurrentHightToRoot());
		}
		
		ExprPredItem expr00 = getExprPredItemForTest("Cld00.java", "$ == 0", 0.8);
		TreePredItem cld00 = root.getChild0().getChild0();
		cld00.setExprItem(expr00);
		TreePredItem newCld00 = newRoot.getChild0().getChild0();
		assertNull(newCld00.getExprItem());
	}
	
	@Test
	public void testIsRootAndIsLeaf() {
		TreePredItem root = getSimpleTree_0();
		assertTrue(root.isRoot());
		assertFalse(root.isLeaf());
		assertFalse(root.getChild0().isLeaf());
		assertFalse(root.getChild1().isLeaf());
		assertTrue(root.getChild0().getChild0().isLeaf());
		assertTrue(root.getChild1().getChild0().isLeaf());
		assertTrue(root.getChild1().getChild1().isLeaf());
		
		root = getSimpleTree_2();
		assertTrue(root.isRoot());
		assertTrue(root.isLeaf());
		
		root = getSimpleTree_3();
		assertTrue(root.isRoot());
		assertTrue(root.isLeaf());
	}

	@Test
	public void testGetFinalScore() {
		TreePredItem root = getSimpleTree_0();
		String[] rootScores = new String[]{"0.5", "0.25", "0.9", "0.5", "0.75", "0.7"};
		BigDecimal rootScore = BigDecimal.ONE;
		for(String str : rootScores) {
			BigDecimal curr = new BigDecimal(str);
			rootScore = rootScore.multiply(curr);
		}
		rootScore = rootScore.setScale(10, BigDecimal.ROUND_HALF_UP);
		BigDecimal score = root.getFinalScore().setScale(10, BigDecimal.ROUND_HALF_UP);
		assertEquals(score, rootScore);
	}
	
	@Test
	public void testBroadFristSearchTraverse() {
		TreePredItem root = TreePredItem.getRootInstance(true);
		TreePredItem.expandTreeCompleteTree(root);
		List<TreePredItem> bfsList = root.broadFristSearchTraverse();
		System.out.println(root);
		
		for(TreePredItem item : bfsList) {
			System.out.println(item);
		}
		
		assertEquals(bfsList.size(), TreePredItem.MAX_TREE_SIZE);
	}
	
	@Test
	public void testDepthFristSearchTraverse() {
		TreePredItem root = getSimpleTree_0();
		List<TreePredItem> list = root.depthFirstSearchTraverse();
		System.out.println(root);
		
		for(TreePredItem item : list) {
			System.out.println(item);
		}
	}
	
	@Test
	public void testRecurNodeExpansionPositions() {
		TreePredItem root = getSimpleTree_0();
		List<TreePredItem> result = root.recurNodeExpansionPositions();
		
		assertEquals(result.size(), 0);
		for(TreePredItem item : result) {
			assertTrue(item.isLeaf());
		}
	}
	
	@Test
	public void testRecurNodeExpansionPositions_1() {
		TreePredItem root = getSimpleTree_1();
		List<TreePredItem> result = root.recurNodeExpansionPositions();
		
		assertEquals(result.size(), 2);
		for(TreePredItem item : result) {
			assertTrue(item.isLeaf());
		}
	}
	
	@Test
	public void testRecurNodeExpansionPositions_2() {
		TreePredItem root = getSimpleTree_2();
		List<TreePredItem> result = root.recurNodeExpansionPositions();
		assertEquals(result.size(), 0);
	}
	
	public void testRecurNodeExpansionPositions_3() {
		TreePredItem root = getSimpleTree_3();
		List<TreePredItem> result = root.recurNodeExpansionPositions();
		assertEquals(result.size(), 1);
	}
	
	
	@Test
	public void testGetCurrentLargestDepth() {
		TreePredItem root = getSimpleTree_0();
		int height = root.getCurrentLargestDepth();
		assertEquals(height, 3);
	}

	@Test 
	public void testIsBottomLestMost_0() {
		TreePredItem root0 = getSimpleTree_0();
		TreePredItem cld00 = root0.getChild0().getChild0();
		TreePredItem cld1 = root0.getChild1();
		TreePredItem cld10 = root0.getChild1().getChild0();
		TreePredItem cld11 = root0.getChild1().getChild1();
		assertFalse(root0.isBottomLeftMost());
		assertFalse(cld1.isBottomLeftMost());
		assertFalse(cld10.isBottomLeftMost());
		assertFalse(cld11.isBottomLeftMost());
		assertTrue(cld00.isBottomLeftMost());
		
		int sum = 0;
		for(TreePredItem item: root0.broadFristSearchTraverse()) {
			if (item.isBottomLeftMost()) {
				sum++;
			}
		}
		assertEquals(sum, 1);
	}
	
	@Test 
	public void testIsBottomLestMost_1() {
		TreePredItem root1 = getSimpleTree_1();
		TreePredItem cld0 = root1.getChild0();
		TreePredItem cld1 = root1.getChild1();
		TreePredItem cld10 = root1.getChild1().getChild0();
		TreePredItem cld11 = root1.getChild1().getChild1();

		assertFalse(root1.isBottomLeftMost());
		assertFalse(cld1.isBottomLeftMost());
		assertFalse(cld10.isBottomLeftMost());
		assertFalse(cld11.isBottomLeftMost());
		assertTrue(cld0.isBottomLeftMost());
		
		int sum = 0;
		for(TreePredItem item: root1.broadFristSearchTraverse()) {
			if (item.isBottomLeftMost()) {
				sum++;
			}
		}
		assertEquals(sum, 1);
	}
	
	@Test 
	public void testIsBottomLestMost_2() {
		TreePredItem root2 = getSimpleTree_2();
		assertTrue(root2.isBottomLeftMost());
	}
}
