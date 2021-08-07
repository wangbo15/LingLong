package cn.edu.pku.sei.plde.hanabi.trace.collector;

public class NumberCollector extends PrimitiveTypeCollector {

    @Override
    protected Class<?> collectingClass() {
        return Number.class;
    }

}
