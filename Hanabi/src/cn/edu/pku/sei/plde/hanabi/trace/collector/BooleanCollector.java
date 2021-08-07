package cn.edu.pku.sei.plde.hanabi.trace.collector;

public class BooleanCollector extends PrimitiveTypeCollector {

    @Override
    protected Class<?> collectingClass() {
        return Boolean.class;
    }

}
