package cn.edu.pku.sei.plde.hanabi.trace.collector;

import java.util.Collection;
import java.util.Map;

import cn.edu.pku.sei.plde.hanabi.utils.Pair;

import static java.util.Arrays.asList;

public class MapCollector extends ValueCollector {

    @Override
    protected Class<?> collectingClass() {
        return Map.class;
    }

    @Override
    protected Collection<Pair<String, Object>> collectedValues(String name, Object value) {
        Map<?, ?> map = (Map<?, ?>) value;
        Pair<String, Integer> size = Pair.from(name + ".size()", map.size());
        Pair<String, Boolean> isEmpty = Pair.from(name + ".isEmpty()", map.isEmpty());
        return (Collection) asList(size, isEmpty);
    }

}
