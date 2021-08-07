package cn.edu.pku.sei.plde.hanabi.utils.build;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cn.edu.pku.sei.plde.hanabi.utils.CmdUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;

public class MavenUtil implements BuildUtil{
	
	public static final String KEY_SUREFIRE_REPORT_PATH = "surefireReportPath";
	
	public static final String KEY_CORE_MODULE_NAME = "coreModuleName";
	
	public static final String KEY_OTHER_MODULES_NAMES = "otherModulesNames";
	
	public static Map<String, String> config(String projectName, String root) {
		
		switch(projectName) {
		case "accumulo": return processAccumulo(root);
		
		case "camel": return processCamel(root);
		
		default:
			throw new Error(projectName);
		}
	}

	public static void modifyModulePomToSkipTest(File pom) {
		assert pom.exists(): pom.getAbsolutePath();
		
		MavenProject project = getMavenProject(pom);
		
		// TODO: waiting for the bug-issue of maven : 
		boolean oriHasBuild = project.getModel().getBuild() != null;
		
		Build build = project.getBuild();
		
		final String artifactId = "maven-surefire-plugin";
		
		final String skipTestTag = "skipTests";
		
		Plugin surefire = getCompilePlugin(build, artifactId);	
		
		if(surefire == null) {
			surefire = new Plugin();
			surefire.setArtifactId(artifactId);
			
			// generate key for the plugin
			surefire.getKey();
			
			Xpp3Dom config = new Xpp3Dom("configuration");
			
			Xpp3Dom skipTest = new Xpp3Dom(skipTestTag);
			skipTest.setValue("true");
			
			config.addChild(skipTest);
			skipTest.setParent(config);
			
			surefire.setConfiguration(config);
			build.addPlugin(surefire);
			
		} else {
			Xpp3Dom config = (Xpp3Dom) surefire.getConfiguration();
			
			if(config == null) {
				config = new Xpp3Dom("configuration");
				surefire.setConfiguration(config);
			}
			
			// if not specified skip test, open it 
			if(config.getChild(skipTestTag) == null) {
				Xpp3Dom skipTest = new Xpp3Dom(skipTestTag);
				skipTest.setValue("true");
				
				config.addChild(skipTest);
				skipTest.setParent(config);
			}
		}// end if(surefire == null)
		
		/* rewritePomBySerializer(pom, project); */
		
		rewritePomByStringIO(pom, build, oriHasBuild);
	}

	public static Set<String> getModuleDependency(File pom) {
		if(!pom.exists()) {
			return Collections.emptySet();
		}
		MavenProject project = getMavenProject(pom);
		
		List<Dependency> dependencies = project.getDependencies();
		if(dependencies == null || dependencies.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<String> results = new HashSet<>();
		for(Dependency dp : dependencies) {
			String atf = dp.getArtifactId();
			results.add(atf);
		}
		
		return results;
	}

	/**
	 * @param subject: like 'accumulo'
	 * @param pom
	 * @param dependencies
	 */
	public static void removeUndependedModules(String subject, File pom, Set<String> dependencies) {
		List<String> lines = FileUtil.readFileToStringList(pom);
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			
			if(line.startsWith("<module>") && line.endsWith("</module>") && !line.contains("/")) {
				int start = "<module>".length();
				int end = line.length() - "</module>".length();
				String module = line.substring(start, end);
				String key = subject + "-" + module;
				
				// comment the modules not depending
				if(!dependencies.contains(key) && !module.contains("core")) {
					sb.append("<!-- ");
					sb.append(lines.get(i));
					sb.append(" -->");
					sb.append("\n");
					continue;
				}
			} 
			
			sb.append(lines.get(i));
			sb.append("\n");
		}
		
		FileUtil.writeStringToFile(pom, sb.toString(), false);
	}
	
	public static void changeTestRangeToPkg(File pom) {
		List<String> oriList = FileUtil.readFileToStringList(pom);
		boolean begin = false;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < oriList.size(); i++) {
			String line = oriList.get(i);
			if(line.trim().equals("<artifactId>maven-surefire-plugin</artifactId>")) {
				begin = true;
				sb.append(line + "\n");
				continue;
			}
			if(begin) {
				String lineTmp = line.trim();
				if(lineTmp.startsWith("<include>") && lineTmp.endsWith("</include>")) {
					// modify 
					
					String head = lineTmp.substring(0, lineTmp.length() - "</include>".length());
					head = head.substring(0, head.lastIndexOf('/'));
					head += "/*Test.java</include>";
					sb.append("\t" + head + "\n");
					continue;
				}
				if(line.trim().equals("</configuration>")) {
					begin = false;
				}
				
			} // end if(begin)
			
			sb.append(line + "\n");
		}
		
		FileUtil.writeStringToFile(pom, sb.toString(), false);
	}
	
	/**
	 * Add test <includes> tags into pom file, used by camel
	 */
	public static void insertTestsToInclude(String inlcudeStr, File pom) {
		MavenProject project = getMavenProject(pom);
		final String artifactId = "maven-surefire-plugin";
		
		Plugin surefire = getCompilePlugin(project.getBuild(), artifactId);
		
		assert surefire != null;
		
		Xpp3Dom config = (Xpp3Dom) surefire.getConfiguration();
		
		Xpp3Dom includes = config.getChild("includes");
		
		List<String> oriList = FileUtil.readFileToStringList(pom);
		StringBuffer sb = new StringBuffer();
		boolean begin = false;
		for(int i = 0; i < oriList.size(); i++) {
			String line = oriList.get(i);
			if(line.trim().equals("<artifactId>maven-surefire-plugin</artifactId>")) {
				begin = true;
				sb.append(line + "\n");
				continue;
			}
			
			if(begin) {
				if(includes == null && line.trim().equals("<configuration>")) {
					sb.append(line + "\n");
					sb.append("<includes>\n");
					sb.append(inlcudeStr);
					sb.append("</includes>\n");
					continue;
				}
				
				if(includes != null && line.trim().equals("<includes>")) {
					sb.append(line + "\n");
					sb.append(inlcudeStr);
					continue;
				}
				
				if(line.trim().equals("</configuration>")) {
					begin = false;
				}
				
			}
			sb.append(line + "\n");
		}
		FileUtil.writeStringToFile(pom, sb.toString(), false);
	}
	
	private static String getClassPath(String root) {
		File file = new File(root);
		final String cmd = "mvn dependency:build-classpath compile -fn";
		
		List<String> stdout = new ArrayList<String>();
		boolean verbose = true;
		CmdUtil.runByJava7(cmd, file, verbose, stdout, null);
		
		String classPath = "";
		for(int i = 0; i < stdout.size() - 2; i++) {
			String line = stdout.get(i);
			
			if(line.startsWith("Downloaded:") || line.startsWith("Downloading:")) {
				continue;
			}
			
			if(line.contains("core") && line.contains("build-classpath")) {
				if(stdout.get(i + 1).contains("Dependencies classpath:")) {
					classPath += stdout.get(i+2);					
				}
			}
		}
		assert classPath != "";
		return classPath;
	}
	
	/**
	 * Add target, test target and bin of this project 
	 * @param classPath
	 * @param root
	 * @param configuer
	 * @return
	 */
	private static String complementClassPath(String classPath, String root, Map<String, String> configuer) {
		// add target, test target and bin of this project 
		if(!classPath.contains(configuer.get(KEY_TARGET_ROOT))) {
			classPath += (":" + root + configuer.get(KEY_TARGET_ROOT));
		}
		if(!classPath.contains(configuer.get(KEY_TEST_TARGET_ROOT))) {
			classPath += (":" + root + configuer.get(KEY_TEST_TARGET_ROOT));
		}
		classPath += (":" + System.getProperty("user.dir") + "/bin/");
		
		return classPath;
	}
	
	protected static MavenProject getMavenProject(File pomFile) {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		FileReader fileReader = null;
		Model model = null;
		MavenProject project = null;
		
		try {
			fileReader = new FileReader(pomFile);
			model = reader.read(fileReader);
			project = new MavenProject(model);
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(fileReader);
		}
		
		return project;
	}
	
	private static Map<String, String> processAccumulo(String root) {
		Map<String, String> configuer = new HashMap<>();
		
		File pomFile = new File(root + "pom.xml");

		MavenProject project = getMavenProject(pomFile);
		
		List<String> modules = project.getModules();
		
		String coreModuleName = null;
		
		StringBuffer otherModules = new StringBuffer();
		
		for(String module: modules) {
			//System.out.println(module);
			if(module.contains("core")) {
				coreModuleName = module;
			} else {
				otherModules.append(module);
				otherModules.append(":");
			}
		}
		assert coreModuleName != null;
		
		if(otherModules.length() != 0) {
			// remove the last ':'
			otherModules.deleteCharAt(otherModules.length() - 1);
		}
		
		configuer.put(KEY_CORE_MODULE_NAME, coreModuleName);
		configuer.put(KEY_OTHER_MODULES_NAMES, otherModules.toString());
		
		configuer.put(KEY_SUREFIRE_REPORT_PATH, coreModuleName + "/target/surefire-reports/");

		configuer.put(KEY_SRC_ROOT, coreModuleName + "/src/main/java/");
		configuer.put(KEY_TARGET_ROOT, coreModuleName + "/target/classes/");
		configuer.put(KEY_TEST_SRC_ROOT, coreModuleName + "/src/test/java/");
		configuer.put(KEY_TEST_TARGET_ROOT, coreModuleName + "/target/test-classes/");


		String testJdkLevel = getJdkVersionFromMvnProject(project);
		
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);

		String classPath = getClassPath(root);
		
		// add target, test target and bin of this project 
		classPath = complementClassPath(classPath, root, configuer);
		
		configuer.put(KEY_CLASS_PATH, classPath);

		return configuer;
	}
	
	private static Map<String, String> processCamel(String subRoot) {
		Map<String, String> configuer = new HashMap<>();
		
		File subRootFile = new File(subRoot);

		String coreModuleName = subRootFile.getName();
		configuer.put(KEY_CORE_MODULE_NAME, coreModuleName);
		configuer.put(KEY_SUREFIRE_REPORT_PATH, "target/surefire-reports/");
		configuer.put(KEY_SRC_ROOT, "src/main/java/");
		configuer.put(KEY_TARGET_ROOT, "target/classes/");
		configuer.put(KEY_TEST_SRC_ROOT, "src/test/java/");
		configuer.put(KEY_TEST_TARGET_ROOT, "target/test-classes/");
		
		String root = subRootFile.getParentFile().getAbsolutePath();
		File rootPomFile = new File(root + "/pom.xml");
		MavenProject project = getMavenProject(rootPomFile);
		
		String testJdkLevel = getJdkVersionFromMvnProject(project);
		configuer.put(KEY_TEST_JDK_LEVEL, testJdkLevel);

		String classPath = getClassPath(subRoot);

		classPath = complementClassPath(classPath, subRoot, configuer);
		
		configuer.put(KEY_CLASS_PATH, classPath);

		return configuer;
	}
	
	/**
	 * @param build
	 * @param pluginName
	 * @return null if not found
	 */
	private static Plugin getCompilePlugin(Build build, String pluginName) {
		Map<String, Plugin> map = build.getPluginsAsMap();
		
		Plugin plugin = null;
		
		String compilerKey = "org.apache.maven.plugins:" + pluginName;
		if(map.containsKey(compilerKey)) {
			// for older versions of accumulo, such as accumulo-151 and accumulo-907
			plugin = map.get(compilerKey);
		} else if(build.getPluginManagement() != null){
			// for newer versions  
			map = build.getPluginManagement().getPluginsAsMap();
			if(map.containsKey(compilerKey)) {
				plugin = map.get(compilerKey);
			}
		}
		
		return plugin;
	}
	
	/**
     * @deprecated Use {@link #rewritPomByStringIO()}.
     */
	@Deprecated
	private static void rewritePomBySerializer(File pom, MavenProject project) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(pom);
			project.writeModel(writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(writer);
		}
	}
	
	private static void rewritePomByStringIO(File pom, Build build, boolean oriHasBuild) {
		List<String> lines = FileUtil.readFileToStringList(pom);
		StringBuffer sb = new StringBuffer();
		String buildStr = MavenBuildWriter.mvnBuildModelToString(build);
		
		boolean inserted = false;
		if(oriHasBuild) {
			boolean met = false;
			for(String line : lines) {
				if(!inserted && line.trim().equals("<build>")) {
					sb.append(buildStr);
					sb.append("\n");
					met = true;
					inserted = true;
				} else if(met && line.trim().equals("</build>")) {
					met = false;
				} else if(!met){
					sb.append(line);
					sb.append("\n");
				}
			}
		}else {
			for(String line : lines) {
				sb.append(line);
				sb.append("\n");
				
				// insert after </dependencies>
				if(!inserted && line.trim().equals("</dependencies>")) {
					sb.append(buildStr);
					sb.append("\n");
					inserted = true;
				}
			}
		}
		assert inserted: pom.getAbsolutePath();
		FileUtil.writeStringToFile(pom, sb.toString(), false);
	}
	
	
	private static String getJdkVersionFromMvnProject(MavenProject project) {
		
		// first find in properties
		if(project.getProperties() != null){
			// for accumulo
			String val = project.getProperties().getProperty("maven.compiler.target");
			
			if(val == null) {
				//for camel
				val = project.getProperties().getProperty("jdk.version");
			}
			
			if(val != null) {
				assert val.equals("1.5") || val.equals("1.6") || val.equals("1.7") : val;				
				return val;
			}
		}
		
		// then find in plugins
		Build build = project.getBuild();
		
		Plugin compiler = getCompilePlugin(build, "maven-compiler-plugin");
		
		assert compiler != null: "THERE IS NO COMPILER PLUGIN IN pom.xml !!!";
		
		Xpp3Dom config = (Xpp3Dom) compiler.getConfiguration();
		
		Xpp3Dom child = config.getChild("target");
		
		String testJdkLevel = "";

		if(child != null) {
			testJdkLevel = child.getValue();
			
			// if is ${java.ver}
			if(testJdkLevel.startsWith("${") && testJdkLevel.endsWith("}")) {
				String key = testJdkLevel.substring(2, testJdkLevel.length() - 1);
				assert project.getProperties() != null;
				testJdkLevel = project.getProperties().getProperty(key);
				assert testJdkLevel != null;
			}
			
		} 
		assert !testJdkLevel.equals(""); 

		return testJdkLevel;
	}

}
