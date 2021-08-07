package cn.edu.pku.sei.plde.hanabi.fl.asm.instru;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.core.internal.utils.FileUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import cn.edu.pku.sei.plde.hanabi.build.proj.BugsDotJarProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.fl.ASMFaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.asm.instru.Instrumenter;
import cn.edu.pku.sei.plde.hanabi.fl.asm.instru.exrt.Tracer;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.ClassPathHacker;
import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileClassLoader;
	
/**
 * In eclipse, must be run with JVM option: "-XX:-UseSplitVerifier -ea"
 */
public class InstrumenterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private String output = "./test_resource/";
	
	private static boolean compiler(List<String> paths, String outputPath, String classPath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
		StandardLocation oLocation = StandardLocation.CLASS_OUTPUT;
		
		List<String> optionList = null;
		
		if(classPath != null && classPath.trim().length() > 0) {
			optionList = new ArrayList<String>();
			optionList.addAll(Arrays.asList("-cp", classPath));
		}

		// set compiler's classpath to be same as the runtime's
		//optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")));
		// any other options you want
		//optionList.addAll(Arrays.asList(options));
		
		boolean result = false;
		try {
			fileManager.setLocation(oLocation, Arrays.asList(new File[] { new File(outputPath) }));
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(paths);
			
			JavaCompiler.CompilationTask task = compiler.getTask(null, 
					fileManager, 
					diagnosticsCollector, 
					optionList, 
					null,
					compilationUnits);
			
			result = task.call();
			
		    if (!result) {
		        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
		        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
		            // read error dertails from the diagnostic object
		            System.out.println(diagnostic.getMessage(null));
		        }
		    }
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeClose(fileManager);
		}
		
		return result;
	}

	@Test
	public void testNormalClass() {
		String className = "Hello";
		String classFullName = "org.pkg.Hello";
		String srcPath = "./test_resource/org/pkg/" + className + ".java";
		
		boolean result = compiler(Arrays.asList(srcPath), output, null);
		assertTrue(result);
		
		//ClassLoader cl = Thread.currentThread().getContextClassLoader();

		File binFile = new File("./bin");
		
		
		assertTrue(binFile.exists());
		
		String classPath = "./test_resource/org/pkg/" + className + ".class";
		File classFile = new File(classPath);
		
		assertTrue(classFile.exists());
		
		
		try {
			FileClassLoader loader = new FileClassLoader(new URL[] {binFile.toURI().toURL()});
			
			Class clazz = loader.findClass(classFullName, classFile);
			
			Object obj = clazz.newInstance();
			//get main method
            Method mainMethod = clazz.getMethod("main", String[].class);
            
            System.out.println(">>>> INVOKE THE ORIGINAL CLASS");
            
            //invoke first
            mainMethod.invoke(null,(Object) new String[]{});
			
            System.out.println(">>>> BEGIN TO INSTRUMENT");
            
            //instrument
            Instrumenter.instrumentSrcClass(classFile);
            
			FileClassLoader loaderNew = new FileClassLoader(new URL[] {binFile.toURI().toURL()});
            
			clazz = loaderNew.findClass(classFullName, classFile);
			obj = clazz.newInstance();
			//get main method
            mainMethod = clazz.getMethod("main", String[].class);
            
            System.out.println(">>>> INVOKE THE INSTRUMENTED CLASS");
            Tracer.setVerbose(true);
            
            //invoke first
            mainMethod.invoke(null,(Object) new String[]{});
            
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  
	}
	
	/**
	 * For jdk7 verifier, we cannot compute stack by asm: 
	 * https://stackoverflow.com/questions/26733946/java-lang-verifyerror-expecting-a-stackmap-frame-at-branch-target
	 * 
	 * We must set the JVM option: -XX:-UseSplitVerifier
	 * 
	 * Set in Eclipes: 
	 * Run->Run Configurations-> Chooes the InstrumenterTest.testTestClass->
	 * Arguments -> VM arguments -> set to "-XX:-UseSplitVerifier -ea"
	 * 
	 * But it seems setting 'ClassWriter.COMPUTE_FRAMES' makes it runnable without the option '"-XX:-UseSplitVerifier' 
	 */
	@Test
	public void testTestClass() {
        String currClassPath = System.getProperty("java.class.path") + ":.";

//        System.out.println(currClassPath);
		
		String testClassName = "HelloTest";
		String testClassFullName = "org.pkg.HelloTest";
		
		String className = "Hello";
		String classFullName = "org.pkg.Hello";				
		
		String testSrcPath = "./test_resource/org/pkg/" + testClassName + ".java";
		String srcPath = "./test_resource/org/pkg/" + className + ".java";

		File testSrcFile = new File(testSrcPath);
		assertTrue(testSrcFile.exists());
		
		boolean result = compiler(Arrays.asList(srcPath, testSrcPath), output, currClassPath);
		assertTrue(result);
		
		//ClassLoader cl = Thread.currentThread().getContextClassLoader();

		File junitFile = null;
		File hamcrestFile = null;
		for(String path : currClassPath.split(":")) {
			if(path.endsWith("jar")) {
				if(path.contains("eclipse") && path.contains("org.junit")) {
					junitFile = new File(path);
				
				}else if(path.contains("eclipse") && path.contains("org.hamcrest.core")) {
					hamcrestFile = new File(path);
				}
			}
		}
		
		assertNotNull(junitFile);
		assertNotNull(hamcrestFile);

		File binFile = new File("./bin");
		File testResFile = new File("./test_resource");
		
		assertTrue(binFile.exists());
		assertTrue(junitFile.exists());
		assertTrue(hamcrestFile.exists());
		
		File testClassFile = new File("./test_resource/org/pkg/" + testClassName + ".class");
		File classFile = new File("./test_resource/org/pkg/" + className + ".class");
		
		assertTrue(testClassFile.exists());
		assertTrue(classFile.exists());

		try {
			FileClassLoader loader = new FileClassLoader(new URL[] {binFile.toURI().toURL(), 
					testResFile.toURI().toURL(),
					junitFile.toURI().toURL(), 
					hamcrestFile.toURI().toURL()});
			
			runTestDynamiclly(loader, testClassFullName, 
					testClassFile, classFullName, classFile);
			
			System.out.println("\n>>>> BEGIN TO INSTRUMENT, BOTH SRC AND TEST");

			boolean isJunit3 = false;
			// instrument
			Instrumenter.instrumentSrcClass(classFile);
			Instrumenter.instrumentTestClass(testClassFile, isJunit3);
			
			// run test again
            
			System.out.println("\n>>>> RUN THE INSTRUMENTED TEST");
			loader = new FileClassLoader(new URL[] {binFile.toURI().toURL(), 
					testResFile.toURI().toURL(),
					junitFile.toURI().toURL(), 
					hamcrestFile.toURI().toURL()});
			
			runTestDynamiclly(loader, testClassFullName, 
					testClassFile, classFullName, classFile);
			
			//TODO: check dump file correctness
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
	}
	
	private void runTestDynamiclly(FileClassLoader loader, String testClassFullName,
			File testClassFile, String classFullName, File classFile) {
		
		try {
			
			Class testClazz = loader.findClass(testClassFullName, testClassFile);
			Class testedClazz = loader.findClass(classFullName, classFile);
			
			Object testObject = testClazz.newInstance();
			
			RunNotifier notifier = new RunNotifier();
			RunListener listener = new TextListener(new RealSystem());
			notifier.addListener(listener);
			
            System.out.println("\n>>>> RUN THE ORIGINAL TEST");
			for (Method method : testClazz.getMethods()) {
				if (method.isAnnotationPresent(Test.class)) {
					notifier.fireTestStarted(Description.createTestDescription(testClazz, method.getName()));
					method.invoke(testObject);
					notifier.fireTestFinished(Description.createTestDescription(testClazz, method.getName()));
				}
			}
            
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void test_Accumulo_151_FilteringIteratorTest() {
		String proj = "accumulo";
		int issueID = 151;
		
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID);
		
		CmdUtil.runCmd("mvn test", projectConfig.getRoot(), false);
		
		ClassPathHacker.loadClassPaths(projectConfig.getClassPaths());
		
		File file = new File(Config.BUGS_DOT_JAR_ROOT + "/accumulo/src/core/target/test-classes/org/apache/accumulo/core/iterators/FilteringIteratorTest.class");
		boolean isJunit3 = false;
		Instrumenter.instrumentTestClass(file, isJunit3);
		
		CmdUtil.runCmd("mvn test -DfailIfNoTests=false -Dtest=org.apache.accumulo.core.iterators.FilteringIteratorTest", projectConfig.getRoot());
	}
	
	/**
	 * Test to omit empty test methods
	 */
	@Test
	public void test_Camel_3388_BeanMethodWithMultipleParametersTest() {
		String proj = "camel";
		int issueID = 3388;
				
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		
		String testCmd = "mvn test -DfailIfNoTests=false -Dtest=org.apache.camel.component.bean.BeanMethodWithMultipleParametersTest";
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
		ClassPathHacker.loadClassPaths(projectConfig.getClassPaths());
		
		File file = new File(Config.BUGS_DOT_JAR_ROOT + "/camel/camel-core/target/test-classes/org/apache/camel/component/bean/BeanMethodWithMultipleParametersTest.class");
		
		ASMFaultLocation.copyTracerDotClassToResource(projectConfig);
		
		boolean isJunit3 = true;
		
		Instrumenter.instrumentTestClass(file, isJunit3);
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
	}
	
	@Test
	public void test_Camel_8592_IntrospectionSupport_setProperty() {
		String proj = "camel";
		int issueID = 8592;
				
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		
		String testCmd = "mvn test -Dtest=org.apache.camel.processor.aggregator.AggregateForceCompletionOnStopParallelTest";
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
		ClassPathHacker.loadClassPaths(projectConfig.getClassPaths());
		
		File file = new File(Config.BUGS_DOT_JAR_ROOT + "camel/camel-core/target/classes/org/apache/camel/util/IntrospectionSupport.class");
		
		assertTrue(file.exists());
		
		ASMFaultLocation.copyTracerDotClassToResource(projectConfig);
		
		Instrumenter.instrumentSrcClass(file);
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
	}
	
	@Test
	public void test_Camel_8592_SimpleBackwardsCompatibleParser_doParseExpression() {
		String proj = "camel";
		int issueID = 8592;
				
		ProjectConfig projectConfig = BugsDotJarProjectConfig.loadProjectConfig(proj, issueID, "camel-core");
		
		String testCmd = "mvn test -Dtest=org.apache.camel.processor.aggregator.AggregateGroupedExchangeBatchSizeTest";
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
		ClassPathHacker.loadClassPaths(projectConfig.getClassPaths());
		
		File file = new File(Config.BUGS_DOT_JAR_ROOT + "camel/camel-core/target/classes/org/apache/camel/language/simple/SimpleTokenizer.class");
		
		assertTrue(file.exists());
		
		ASMFaultLocation.copyTracerDotClassToResource(projectConfig);
		
		Instrumenter.instrumentSrcClass(file);
		
		CmdUtil.runByJava7(testCmd, projectConfig.getRoot(), true);
		
	}
}
