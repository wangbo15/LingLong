package edu.pku.sei.conditon.dedu.pred;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
		TreePredItem root = new TreePredItem(null);
		root.setExpandItem(new RecurNodePredItem(1, 0.5));
		
		TreePredItem cld0 = new TreePredItem(root);
		root.setChild0(cld0);
		cld0.setExpandItem(new RecurNodePredItem(3, 0.25));
		
		TreePredItem cld00 = new TreePredItem(cld0);
		cld0.setChild0(cld00);
		cld00.setExpandItem(new RecurNodePredItem(0, 0.9));
		
		TreePredItem cld1 = new TreePredItem(root);
		root.setChild1(cld1);
		cld1.setExpandItem(new RecurNodePredItem(2, 0.5));
		
		TreePredItem cld10 = new TreePredItem(cld1);
		cld1.setChild0(cld10);
		cld10.setExpandItem(new RecurNodePredItem(0, 0.75));
		
		TreePredItem cld11 = new TreePredItem(cld1);
		cld1.setChild1(cld11);
		cld11.setExpandItem(new RecurNodePredItem(0, 0.7));
		
		return root;
	}
	
	/**
	 * @return a simple tree for test : (# && (? || ?))
	 */
	private TreePredItem getSimpleTree_1() {
		TreePredItem root = new TreePredItem(null);
		root.setExpandItem(new RecurNodePredItem(1, 0.5));
		
		TreePredItem cld0 = new TreePredItem(root);
		root.setChild0(cld0);
		cld0.setExpandItem(new RecurNodePredItem(0, 0.7));
		
		TreePredItem cld1 = new TreePredItem(root);
		root.setChild1(cld1);
		cld1.setExpandItem(new RecurNodePredItem(2, 0.5));
		
		TreePredItem cld10 = new TreePredItem(cld1);
		// not expandItem
		cld1.setChild0(cld10);
		
		TreePredItem cld11 = new TreePredItem(cld1);
		// not expandItem
		cld1.setChild1(cld11);
		
		return root;
	}
	
	@Test
	public void testExpandTreeCompleteTree() {
		TreePredItem root = new TreePredItem(null);
		TreePredItem.expandTreeCompleteTree(root);
		
		assertEquals(root.getCurrentHight(), TreePredItem.MAX_TREE_HIGHT);
		assertEquals(root.getChild0().getChild1().getCurrentHight(), 2);
		//assertNull(root.getChild0().getChild0().getChild0());
		
		assertEquals(root.getCurrentHightToRoot(), 0);
		assertEquals(root.getChild0().getCurrentHightToRoot(), 1);
		assertEquals(root.getChild0().getChild1().getCurrentHightToRoot(), 2);
		
		
	}
	
	@Test
	public void testCopyTreeBySharing() {
		TreePredItem.setInstanceNum(0);
		TreePredItem root = getSimpleTree_1();
		
		long afterInit = TreePredItem.getInstanceNum();
		assertEquals(afterInit, 5);
		
		TreePredItem expandPosition = root.getChild1().getChild0();
		TreePredItem newRoot = TreePredItem.sharedCopy(root, expandPosition);
		
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
		}
	}
	
	@Test
	public void testCopyTreeBySharing_1() {
		TreePredItem root = getSimpleTree_0();
				
		TreePredItem expandPosition = root.getChild0().getChild0();
		TreePredItem newRoot = TreePredItem.sharedCopy(root, expandPosition);
		
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
		}
	}
	
	@Test
	public void testDeepCloneFromRoot() {
		TreePredItem.setInstanceNum(0);
		TreePredItem root = getSimpleTree_0();
		
		long afterInit = TreePredItem.getInstanceNum();
		assertEquals(afterInit, 6);
		
		String oriStr = new String(root.toString());

		TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);	
		
		long afterDeepClone = TreePredItem.getInstanceNum();
		assertEquals(afterDeepClone, 12);

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
		TreePredItem root = new TreePredItem(null);
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
	public void testGetCurrentLargestDepth() {
		TreePredItem root = getSimpleTree_0();
		int height = root.getCurrentLargestDepth();
		assertEquals(height, 3);
	}

}
