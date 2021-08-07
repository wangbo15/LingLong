package cn.edu.pku.sei.plde.hanabi.trace.collector;

public class CharacterCollector extends PrimitiveTypeCollector {

    @Override
    protected Class<?> collectingClass() {
        return Character.class;
    }

}
