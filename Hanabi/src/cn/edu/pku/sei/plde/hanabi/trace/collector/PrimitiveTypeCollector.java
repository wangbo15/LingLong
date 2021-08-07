package cn.edu.pku.sei.plde.hanabi.trace.collector;

import java.util.Collection;

import cn.edu.pku.sei.plde.hanabi.utils.Pair;

import static java.util.Arrays.asList;

public abstract class PrimitiveTypeCollector extends ValueCollector {

    @Override
    protected Collection<Pair<String, Object>> collectedValues(String name, Object object) {
        Pair<String, Object> value = Pair.from(name, object);
        return asList(value);
    }
}
