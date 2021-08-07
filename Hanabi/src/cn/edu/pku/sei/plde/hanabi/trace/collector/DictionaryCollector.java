package cn.edu.pku.sei.plde.hanabi.trace.collector;


import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Dictionary;

import cn.edu.pku.sei.plde.hanabi.utils.Pair;

public class DictionaryCollector extends ValueCollector {

    @Override
    protected Collection<Pair<String, Object>> collectedValues(final String name, final Object value) {
        Dictionary<?, ?> dictionary = (Dictionary<?, ?>) value;
        Pair<String, Integer> size = Pair.from(name + ".size()", dictionary.size());
        Pair<String, Boolean> isEmpty = Pair.from(name + ".isEmpty()", dictionary.isEmpty());
        return (Collection) asList(size, isEmpty);
    }

    @Override
    protected Class<?> collectingClass() {
        return Dictionary.class;
    }

}
