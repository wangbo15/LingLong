package edu.pku.sei.conditon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class CollectionUtil {
	public static <K, V> Map<K, V> sortByValue(Map<K, V> map, final boolean reverse) {
	    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	        	if(reverse) {
		            return -((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
	        	}else {
	        		return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
	        	}
	        }
	    });

	    Map<K, V> result = new LinkedHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}
	
	
	public static void cleanMapsInTwoDepth(Map... maps) {
		for(Map<String, Map> fatherMap: maps) {
			if(fatherMap == null) {
				continue;
			}
			for(Map childMap: fatherMap.values()){
				if(childMap == null) {
					continue;
				}
				childMap.clear();
			}
			fatherMap.clear();
		}
	}
	
	public static <T> List<Object[]> getPermutation(List<List<T> > all) {
		int size = all.size();
		int[] currentIndex = new int[size];
		int[] limits = new int[size];
		
		int allNum = 1;
		for(int i = 0; i < size; i++) {
			int num =  all.get(i).size();
			if(num <= 0) {// illegal tree
				return Collections.emptyList();
			}
			allNum *= num;
			limits[i] = num;
		}
		
		List<Object[]> results = new ArrayList<>();
		
		for(int i = 0; i < allNum; i++) {
			
			T[] result = (T[]) new Object[size];
			for(int j = 0; j < size; j++) {
				List<T> currentList = all.get(j);
				result[j] = currentList.get(currentIndex[j]);
			}
		
			results.add(result);
			
			int carryBit = 1;
			for(int j = size - 1; j >= 0; j--) {
				int tmp = currentIndex[j] + carryBit;
				if(tmp == limits[j]) {
					currentIndex[j] = 0;
				} else {
					currentIndex[j] = tmp;
					carryBit = 0;
					break;
				}
			}
			
		}
		
		return results;
	}
	
	public static void remainListFirstK(List<?> list, int k) {
		int s = list.size();
		if(s <= k)
			return;
		
		List<Object> toBeRemove = new ArrayList<>(s - k);
		for(int i = k; i < s; i++) {
			toBeRemove.add(list.get(i));
		}
		
		list.removeAll(toBeRemove);
		toBeRemove.clear();
		
		if(list instanceof ArrayList) {
			((ArrayList<?>) list).trimToSize();
		}
	}
	
	public static void remainTreeSetFirstK(TreeSet<?> treeSet, int k) {
		
		assert k >= 0;
		
		int s = treeSet.size();
		if(s <= k)
			return;
		
		while(treeSet.size() > k) {
			treeSet.pollLast();
		}
	}

	public static <T extends Comparable> TreeSet<T> newSortedSet(){
		return new TreeSet<>(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return  - o1.compareTo(o2);
			}
		});
	}
	
	private static final TreeSet emptyTreeSet = new TreeSet<>();
	public static <T extends Comparable> TreeSet<T> emptyTreeSet(){
		return emptyTreeSet;
	}
}
