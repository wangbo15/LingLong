package cn.edu.pku.sei.plde.hanabi.utils.build;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.Path;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import edu.pku.sei.conditon.util.StringUtil;

public class AntUtil  implements BuildUtil{
	
	public static Map<String, String> config(String projName, String root) {
		projName = projName.split("_")[0].toLowerCase();
		switch(projName) {
		case "math": return processD4jMath(root);
		case "time": return processD4jTime(root);
		case "lang": return processD4jLang(root);
		case "chart": return processD4jChart(root);
		default:
			throw new Error(projName);
		}
	}
	
	private static Map<String, String> processD4jChart(String root) {
		String srcHome = "/source/";
		String testHome = "/tests/";
		String srcDesc = "/build/";
		String testDesc = "/build-tests/";
		
		String testJdkLevel = "1.5";

		checkExistence(srcHome, testHome);
		
		Map<String, String> configuer = new HashMap<>();
		configuer.put(KEY_SRC_ROOT, srcHome);
		configuer.put(KEY_TARGET_ROOT, srcDesc);
		configuer.put(KEY_TEST_SRC_ROOT, testHome);
		configuer.put(KEY_TEST_TARGET_ROOT, testDesc);
		
		List<String> paths = new ArrayList<>();
		paths.add(root + srcDesc);
		paths.add(root + testDesc);
		String clsPth = StringUtil.join(paths, ":");
		configuer.put(KEY_CLASS_PATH, clsPth);
		
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);
		
		return configuer;
	}
	

	private static Map<String, String> processD4jLang(String root) {
		File buildXml = new File(root + "/maven-build.xml");
		if (!buildXml.exists()) {
			buildXml = new File(root + "/build.xml");
		}
		
		if (!buildXml.exists()) {
			throw new Error("NO BUILD FILE @ " + root);
		}
		Project proj = new Project();

		proj.setUserProperty("ant.file", buildXml.getAbsolutePath());
		proj.setBaseDir(new File(root));

		
		proj.init();
		ProjectHelper.configureProject(proj, buildXml);

		
		Map<String, Object> properties = proj.getProperties();
		PropertyHelper propHelper = PropertyHelper.getPropertyHelper(proj);

		Map<String, Target> allTargets = proj.getTargets();

		Target compileTaret = allTargets.get("compile");
		String srcDesc = getJavacDest(compileTaret, propHelper);

		Target compileTestTaret = allTargets.get("compile.tests") == null ? allTargets.get("compile-tests")
				: allTargets.get("compile.tests");
		String testDesc = getJavacDest(compileTestTaret, propHelper);
		
		String srcHome = (String) properties.get("maven.build.srcDir.0");
		if(srcHome == null) {
			srcHome = (String) properties.get("source.home"); 
		}
		
		String testHome = (String) properties.get("maven.build.testDir.0");
		if(testHome == null) {
			testHome = (String) properties.get("test.home");
		}
		
		String testJdkLevel = (String) properties.get("compile.source");
		if(testJdkLevel == null) {
			testJdkLevel = getJavacJdkLevel(compileTestTaret, propHelper);
			assert testJdkLevel != null;
		}

		checkExistence(srcHome, testHome);
		
		Map<String, String> configuer = new HashMap<>();
		configuer.put(KEY_SRC_ROOT, srcHome);
		configuer.put(KEY_TARGET_ROOT, srcDesc);
		configuer.put(KEY_TEST_SRC_ROOT, testHome);
		configuer.put(KEY_TEST_TARGET_ROOT, testDesc);
		
		List<String> paths = new ArrayList<>();
		paths.add(root + "/" + srcDesc);
		paths.add(root + "/" + testDesc);
		paths.add(Config.D4J_ROOT + "framework/projects/Lang/lib/commons-io.jar");
		paths.add(Config.D4J_ROOT + "framework/projects/Lang/lib/easymock.jar");
		String clsPth = StringUtil.join(paths, ":");
		configuer.put(KEY_CLASS_PATH, clsPth);
		
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);
		
		return configuer;
	}
	
	private static Map<String, String> processD4jTime(String root){
		File buildXml = new File(root + "/maven-build.xml");
		
		if (!buildXml.exists()) {
			buildXml = new File(root + "/build.xml");
		}
		
		if (!buildXml.exists()) {
			throw new Error("NO BUILD FILE @ " + root);
		}

		Project proj = new Project();
		
		proj.setUserProperty("ant.file", buildXml.getAbsolutePath());
		proj.setBaseDir(new File(root));
		
		proj.init();
		ProjectHelper.configureProject(proj, buildXml);
		
		Map<String, Object> properties = proj.getProperties();
		PropertyHelper propHelper = PropertyHelper.getPropertyHelper(proj);

		Map<String, Target> allTargets = proj.getTargets();

		Target compileTaret = allTargets.get("compile");
		String srcDesc = getJavacDest(compileTaret, propHelper);
		if(srcDesc == null) {
			compileTaret = allTargets.get("compile.main");
			srcDesc = getJavacDest(compileTaret, propHelper);
		}
		
		Target compileTestTaret = allTargets.get("compile.tests") == null ? allTargets.get("compile-tests")
				: allTargets.get("compile.tests");
		String testDesc = getJavacDest(compileTestTaret, propHelper);

		String srcHome = (String) properties.get("maven.build.resourceDir.1");
		if(srcHome == null) {
			srcHome = (String) properties.get("source.home"); 
		}
		
		String testHome = (String) properties.get("maven.build.testDir.0");
		if(testHome == null) {
			testHome = (String) properties.get("test.home");
		}
		
		String testJdkLevel = "1.5";

		checkExistence(srcHome, testHome);
		
		Map<String, String> configuer = new HashMap<>();
		configuer.put(KEY_SRC_ROOT, srcHome);
		configuer.put(KEY_TARGET_ROOT, srcDesc);
		configuer.put(KEY_TEST_SRC_ROOT, testHome);
		configuer.put(KEY_TEST_TARGET_ROOT, testDesc);
		
		String converJarPath = Config.D4J_ROOT + "framework/projects/Time/lib/joda-convert-1.2.jar";
		configuer.put(KEY_CLASS_PATH, root + "/" + srcDesc + ":" + root + "/" + testDesc + ":" + converJarPath);
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);
		return configuer;
	}
	
	private static Map<String, String> processD4jMath(String root){
		File buildXml = new File(root + "/build.xml");
		if (!buildXml.exists()) {
			throw new Error("NO BUILD FILE @ " + root);
		}

		Project proj = new Project();
		
		proj.setUserProperty("ant.file", buildXml.getAbsolutePath());
		proj.setBaseDir(new File(root));
		
		proj.init();
		ProjectHelper.configureProject(proj, buildXml);
		
		Map<String, Object> properties = proj.getProperties();
		PropertyHelper propHelper = PropertyHelper.getPropertyHelper(proj);

		Map<String, Target> allTargets = proj.getTargets();

		Target compileTaret = allTargets.get("compile");
		String srcDesc = getJavacDest(compileTaret, propHelper);

		Target compileTestTaret = allTargets.get("compile.tests") == null ? allTargets.get("compile-tests")
				: allTargets.get("compile.tests");
		String testDesc = getJavacDest(compileTestTaret, propHelper);

		String srcHome = (String) properties.get("source.home");
		if(srcHome == null) {
			srcHome = "src/java";
		}
		
		String testHome = (String) properties.get("test.home");
		if(testHome == null) {
			testHome = "src/test";
		}
		
		Path testClassPath = (Path) proj.getReference("test.classpath");
		String testClassPathStr;
		if(testClassPath != null) {
			testClassPathStr = testClassPath.toString();
		}else {
			List<String> paths = new ArrayList<>();
			paths.add(root + "/" + srcDesc);
			paths.add(root + "/" + testDesc);
			paths.add(Config.D4J_ROOT + "framework/projects/Math/lib/commons-discovery-0.5.jar");
			testClassPathStr = StringUtil.join(paths, ":");
		}
		
		String testJdkLevel = (String) properties.get("compile.source");
		if(testJdkLevel == null) {
			testJdkLevel = "1.5";
		}
		
		checkExistence(srcHome, testHome);
		
		Map<String, String> configuer = new HashMap<>();
		configuer.put(KEY_SRC_ROOT, srcHome);
		configuer.put(KEY_TARGET_ROOT, srcDesc);
		configuer.put(KEY_TEST_SRC_ROOT, testHome);
		configuer.put(KEY_TEST_TARGET_ROOT, testDesc);
		configuer.put(KEY_CLASS_PATH, testClassPathStr);
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);
		return configuer;
	}

	private static String getJavacDest(Target target, PropertyHelper propHelper) {
		for (Task t : target.getTasks()) {
			if (t.getTaskName().equals("javac")) {
				UnknownElement javac = (UnknownElement) t;
				Map<String, Object> map = javac.getWrapper().getAttributeMap();
				// String src = completeAttributeByPropetyValue((String) map.get("srcdir"),
				// propHelper);
				return completeAttributeByPropetyValue((String) map.get("destdir"), propHelper);
			}
		}
		return null;
	}
	
	private static String getJavacJdkLevel(Target target, PropertyHelper propHelper) {
		for (Task t : target.getTasks()) {
			if (t.getTaskName().equals("javac")) {
				UnknownElement javac = (UnknownElement) t;
				Map<String, Object> map = javac.getWrapper().getAttributeMap();
				return (String) map.get("target");
			}
		}
		return null;
	}

	private static boolean checkExistence(String... folders) {
		for(String path: folders) {
			if(path == null) {
				return false;
			}
			File f = new File(path);
			if(!f.exists()) {
				return false;
			}
		}
		return true;
	}
	
	private static String completeAttributeByPropetyValue(String s, PropertyHelper helper) {
		if (s.length() >= 3 && '$' == s.charAt(0) && '{' == s.charAt(1)) {
			int start = 2;
			// defer to String.indexOf() for protracted check:
			int end = s.indexOf('}', start);
			if (end < 0) {
				throw new Error();
			}
			String property = (start == end) ? "" : s.substring(start, end);
			String tail = (end + 1 > s.length() - 1) ? "" : s.substring(end + 1);
			return (String) helper.getProperty(property) + tail;
		}
		return null;
	}
}
