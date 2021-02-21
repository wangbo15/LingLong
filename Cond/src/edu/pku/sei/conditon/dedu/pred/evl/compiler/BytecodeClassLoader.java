package edu.pku.sei.conditon.dedu.pred.evl.compiler;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import edu.pku.sei.conditon.util.StringUtil;

public class BytecodeClassLoader extends URLClassLoader {
    private Map<String, byte[]> bytecodes;

    protected BytecodeClassLoader(URL[] classpath) {
        super(classpath);
    }

    protected BytecodeClassLoader(URL[] classpath, ClassLoader parentClassLoader) {
        super(classpath, parentClassLoader);
    }

    protected void setBytecodes(Map<String, byte[]> bytecodes) {
    	Map<String, byte[]> map = new HashMap<>();
        map.putAll(bytecodes);
        this.bytecodes = map;
    }

    @Override
    public Class<?> findClass(String qualifiedName) throws ClassNotFoundException {
        if (containsBytecodesFor(qualifiedName)) {
            byte[] bytes = bytecodesFor(qualifiedName);
            return defineClass(qualifiedName, bytes, 0, bytes.length);
        }
        return super.findClass(qualifiedName);
    }

    @Override
    public InputStream getResourceAsStream(String resourceName) {
        if (resourceName.endsWith(".class")) {
            String qualifiedName = StringUtil.stripEnd(resourceName, ".class").replace('/', '.');
            if (containsBytecodesFor(qualifiedName)) {
                return new ByteArrayInputStream(bytecodesFor(qualifiedName));
            }
        }
        return super.getResourceAsStream(resourceName);
    }

    private boolean containsBytecodesFor(String qualifiedName) {
        return bytecodes().containsKey(qualifiedName);
    }

    private byte[] bytecodesFor(String qualifiedName) {
        return bytecodes().get(qualifiedName);
    }

    private Map<String, byte[]> bytecodes() {
        return bytecodes;
    }
}
