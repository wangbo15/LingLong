package cn.edu.pku.sei.plde.hanabi.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtil {
	
	public static <K, V extends List> void putInToValueList(Map<K, V> map, K key, Object valueItem) {
		List list = null;
		if(map.containsKey(key)) {
			list = map.get(key);
		}else {
			list = new ArrayList();
			map.put(key, (V) list);
		}
		list.add(valueItem);
	}
	
	public static <K, V extends Set> void putInToValueSet(Map<K, V> map, K key, Object valueItem) {
		Set set = null;
		if(map.containsKey(key)) {
			set = map.get(key);
		}else {
			set = new HashSet();
			map.put(key, (V) set);
		}
		set.add(valueItem);
	}
	
    public static <T> boolean hasOverlap(Collection<T> firstList, Collection<T> secondList) {
        for (T value : firstList) {
            if (secondList.contains(value)) {
                return true;
            }
        }
        return false;
    }
    
    public static <T> boolean hasDifferent(Collection<T> firstList, Collection<T> secondList) {
    	if(firstList == secondList) {
    		return false;
    	}
    	
    	if(firstList == null || secondList == null){
    		return true;
    	}
    	
    	if(firstList.size() != secondList.size()) {
    		return true;
    	}
        for (T value : firstList) {
            if (!secondList.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
