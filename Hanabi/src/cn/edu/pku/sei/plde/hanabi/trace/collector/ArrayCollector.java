package cn.edu.pku.sei.plde.hanabi.trace.collector;


import java.lang.reflect.Array;
import java.util.Collection;

import cn.edu.pku.sei.plde.hanabi.utils.Pair;

import static java.util.Arrays.asList;

public class ArrayCollector extends ValueCollector {

    @Override
    protected Class<?> collectingClass() {
        return Array.class;
    }

    @Override
    public boolean handlesClassOf(Object value) {
        return value.getClass().isArray();
    }

    @Override
    protected Collection<Pair<String, Object>> collectedValues(String name, Object value) {
        Pair<String, Integer> length = Pair.from(name + ".length", Array.getLength(value));
        return (Collection) asList(length);
    }

}
