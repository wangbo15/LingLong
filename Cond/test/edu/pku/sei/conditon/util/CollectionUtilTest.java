package edu.pku.sei.conditon.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CollectionUtilTest {

	@Test
	public void testGetPermutation() {
		List<String> list0 = Arrays.asList("a", "b", "c");
		List<String> list1 = Arrays.asList("1", "2", "3", "4");
		List<String> list2 = Arrays.asList("X", "Y");

		List<List<String>> listAll = Arrays.asList(list0, list1, list2);
		
		
		List<Object[]> permutation = CollectionUtil.<String>getPermutation(listAll);
		
		assertEquals(permutation.size(), 3*4*2);
		
		List<Object[]> oracles = new ArrayList<>();
		for(String s0: list0) {
			for(String s1: list1) {
				for(String s2: list2) {
					Object[] array = new Object[] {s0, s1, s2};
					oracles.add(array);
				}
			}
		}
		
		for(int i = 0; i < permutation.size(); i++) {
			Object[] arr = permutation.get(i);
			Object[] oracleItem = oracles.get(i);
			
			assertEquals(arr.length, oracleItem.length);
			
			for(int j = 0; j < arr.length; j++) {
				String x = (String) arr[j];
				String y = (String) oracleItem[j];
				System.out.print(x);
				System.out.print(" ");
				
				assertEquals(x, y);
			}
			
			System.out.println();
		}
	}

}
