package edu.pku.sei.conditon.dedu.pred.evl.compiler;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

public class VirtualFileObjectManager extends ForwardingJavaFileManager<JavaFileManager> {//ForwardingJavaFileManager BELONGS TO JDK

    private Map<URI, VirtualSourceFileObject> sourceFiles;
    private Map<String, VirtualClassFileObject> classFiles;
    
    protected VirtualFileObjectManager(JavaFileManager fileManager) {
        super(fileManager);
        classFiles = new HashMap<>();
        sourceFiles = new HashMap<>();
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        URI fileURI = uriFor(location, packageName, relativeName);
        if (containsSourceFileFor(fileURI)) {
            return sourceFile(fileURI);
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile) throws IOException {
        VirtualClassFileObject classFile = new VirtualClassFileObject(qualifiedName, kind);
        classFiles().put(qualifiedName, classFile);
        return classFile;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (VirtualSourceFileObject.class.isInstance(file) || VirtualClassFileObject.class.isInstance(file)) {
            return file.getName();
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
        List<JavaFileObject> files = new LinkedList<>();
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            addMatchingFiles(Kind.CLASS, packageName, classFiles().values(), files);
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            addMatchingFiles(Kind.SOURCE, packageName, sourceFiles().values(), files);
        }
        for (JavaFileObject element : result) {
        	files.add(element);
        }
        return files;
    }

    private void addMatchingFiles(Kind kind, String packageName, Collection<? extends JavaFileObject> files, Collection<JavaFileObject> destination) {
        for (JavaFileObject file : files) {
            if (file.getKind() == kind && file.getName().startsWith(packageName)) {
                destination.add(file);
            }
        }
    }

    protected void addCompiledClasses(Map<String, byte[]> compiledClasses) {
        for (String qualifiedName : compiledClasses.keySet()) {
            classFiles().put(qualifiedName, new VirtualClassFileObject(qualifiedName, Kind.CLASS, compiledClasses.get(qualifiedName)));
        }
    }

    public void addSourceFile(Location location, String packageName, String simpleClassName, VirtualSourceFileObject sourceFile) {
        URI fileURI = uriFor(location, packageName, simpleClassName);
        sourceFiles().put(fileURI, sourceFile);
    }

    public int numberOfSourceFiles() {
        return sourceFiles().size();
    }

    public boolean containsSourceFileFor(URI fileURI) {
        return sourceFiles().containsKey(fileURI);
    }

    public VirtualSourceFileObject sourceFile(URI fileURI) {
        return sourceFiles().get(fileURI);
    }

    public int numberOfClassFiles() {
        return classFiles().size();
    }

    public boolean containsClassFileFor(String qualifiedName) {
        return classFiles().containsKey(qualifiedName);
    }

    public VirtualClassFileObject classFile(String qualifiedName) {
        return classFiles().get(qualifiedName);
    }

    private URI uriFor(Location location, String packageName, String simpleClassName) {
        String uriScheme = location.getName() + '/' + packageName + '/' + simpleClassName + ".java";
        return URI.create(uriScheme);
    }

    protected Map<String, VirtualClassFileObject> classFiles() {
        return classFiles;
    }

    private Map<URI, VirtualSourceFileObject> sourceFiles() {
        return sourceFiles;
    }
}
