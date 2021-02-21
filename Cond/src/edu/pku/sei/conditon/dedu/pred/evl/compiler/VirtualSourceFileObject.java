package edu.pku.sei.conditon.dedu.pred.evl.compiler;


import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class VirtualSourceFileObject extends SimpleJavaFileObject {

    public VirtualSourceFileObject(String simpleClassName, String sourceContent) {
        super(URI.create(simpleClassName + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceContent = sourceContent;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceContent;
    }

    private String sourceContent;
}
