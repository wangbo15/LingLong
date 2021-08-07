package cn.edu.pku.sei.plde.hanabi.trace.collector;


import static java.util.Arrays.asList;

import java.util.Collection;

import cn.edu.pku.sei.plde.hanabi.utils.Pair;

public class CharSequenceCollector extends ValueCollector {

    @Override
    protected Collection<Pair<String, Object>> collectedValues(final String name, final Object value) {
        CharSequence string = (CharSequence) value;
        Pair<String, Integer> length = Pair.from(name + ".length()", string.length());
        Pair<String, Boolean> isEmpty = Pair.from(name + ".length()==0", string.length() == 0);
        //Pair<String, CharSequence> content = Pair.from(name, string);
        return (Collection) asList(length, isEmpty);
    }

    @Override
    protected Class<?> collectingClass() {
        return CharSequence.class;
    }

}
