package edu.pku.sei.conditon.dedu.pred.evl.compiler;



import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.StringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;


public class DynamicClassCompiler {

    private List<String> options;
    private JavaCompiler compiler;  //FROM JDK
    private VirtualFileObjectManager fileManager;   //EXTENDS FROM JDK
    private DiagnosticCollector<JavaFileObject> diagnostics;    //FROM JDK
	
    public DynamicClassCompiler(URL[] classpath) {
        this();
        options = optionsWithClasspath(classpath);
    }

    public DynamicClassCompiler() {
        options = asList("-nowarn");
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager standardFileManager = compiler().getStandardFileManager(diagnostics(), null, null);
        fileManager = new VirtualFileObjectManager(standardFileManager);
    }

    public synchronized byte[] javaBytecodeFor(String qualifiedName, String sourceContent) {
        return javaBytecodeFor(qualifiedName, sourceContent, new HashMap<String, byte[]>());
    }

    public synchronized byte[] javaBytecodeFor(String qualifiedName, String sourceContent, Map<String, byte[]> compiledDependencies) {
        Map<String, String> adHocMap = new HashMap();
        adHocMap.put(qualifiedName, sourceContent);
        return javaBytecodeFor(adHocMap, compiledDependencies).get(qualifiedName);
    }

    public synchronized Map<String, byte[]> javaBytecodeFor(Map<String, String> qualifiedNameAndContent) {
        return javaBytecodeFor(qualifiedNameAndContent, new HashMap<String, byte[]>());
    }

    public synchronized Map<String, byte[]> javaBytecodeFor(Map<String, String> qualifiedNameAndContent, Map<String, byte[]> compiledDependencies) {//INSTRU
        //logDebug(logger(), format("[Compiling %d source files]", qualifiedNameAndContent.size()));
        Collection<JavaFileObject> units = addCompilationUnits(qualifiedNameAndContent);//SOURCES
        fileManager().addCompiledClasses(compiledDependencies);
        CompilationTask task = compiler().getTask(null, fileManager(), diagnostics(), options(), null, units);//CompilationTask -> JDK
        runCompilationTask(task);
        Map<String, byte[]> bytecodes = collectBytecodes(qualifiedNameAndContent);  //ALREADY COMPILED
        //logDebug(logger(), format("[Compilation finished successfully (%d classes)]", bytecodes.size()));
        return bytecodes;
    }

    protected Collection<JavaFileObject> addCompilationUnits(Map<String, String> qualifiedNameAndContent) {
        Collection<JavaFileObject> units = new ArrayList();
        for (String qualifiedName : qualifiedNameAndContent.keySet()) {
            String sourceContent = qualifiedNameAndContent.get(qualifiedName);
            JavaFileObject sourceFile = addCompilationUnit(qualifiedName, sourceContent);
            units.add(sourceFile);
        }
        return units;
    }

    protected JavaFileObject addCompilationUnit(String qualifiedName, String sourceContent) {
        String simpleClassName = StringUtil.lastAfterSplit(qualifiedName, '.');
        String packageName = StringUtil.stripEnd(qualifiedName, '.' + simpleClassName);
        VirtualSourceFileObject sourceFile = new VirtualSourceFileObject(simpleClassName, sourceContent);
        fileManager().addSourceFile(StandardLocation.SOURCE_PATH, packageName, simpleClassName, sourceFile);
        return sourceFile;
    }

    protected boolean runCompilationTask(CompilationTask task) {
        boolean success = task.call();
        if (!success) {
            Collection<String> errors = Arrays.asList("[Compilation errors]");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics().getDiagnostics()) {
                errors.add(diagnostic.toString());
                System.out.println(diagnostic.toString());
            }
            throw new DynamicCompilationException("Aborting: dynamic compilation failed");
        }
        return success;
    }

    private Map<String, byte[]> collectBytecodes(Map<String, String> qualifiedNameAndContent) {
        Map<String, byte[]> bytecodes = new HashMap<>();
        Map<String, VirtualClassFileObject> classFiles = fileManager().classFiles();
        for (String qualifiedName : classFiles.keySet()) {
            String topClassName = topClassName(qualifiedName);
            if (qualifiedNameAndContent.containsKey(topClassName)) {
                bytecodes.put(qualifiedName, classFiles.get(qualifiedName).byteCodes());
            }
        }
        return bytecodes;
    }

    private String topClassName(String qualifiedName) {
        return qualifiedName.split("[$]")[0];
    }

    private List<String> optionsWithClasspath(URL[] classpath) {
        List<String> options = new ArrayList(options());
        options.add("-cp");
        options.add(JavaFile.asClasspath(classpath));
        return options;
    }

    protected VirtualFileObjectManager fileManager() {//VirtualFileObjectManager EXTENDS FROM JDK
        return fileManager;
    }

    private List<String> options() {
        return options;
    }

    private JavaCompiler compiler() {// IMPORTANT: JavaCompiler IS BELONGS TO JDK
        return compiler;
    }

    private DiagnosticCollector<JavaFileObject> diagnostics() {
        return diagnostics;
    }

}