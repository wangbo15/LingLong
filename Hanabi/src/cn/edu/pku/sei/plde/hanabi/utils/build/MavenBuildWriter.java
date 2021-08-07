package cn.edu.pku.sei.plde.hanabi.utils.build;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.eclipse.core.internal.utils.FileUtil;

public class MavenBuildWriter {

	private static final String NAMESPACE = null;
	
	/**
	 * Convert a maven build model to an xml format String
	 * @param build
	 * @return
	 */
	public static String mvnBuildModelToString(Build build) {
		StringWriter writer = new StringWriter();
		
		XmlSerializer serializer = new MXSerializer();
		serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
		try {
			serializer.setOutput(writer);
			writeBuild(build, "build", serializer);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeClose(writer);
		}
		return writer.toString();
	}
	
	
	/**
	 * Only use to write maven build tag to an XML String
	 * 
	 * @param build
	 * @param tagName
	 * @throws java.io.IOException
	 */
	private static void writeBuild(Build build, String tagName, XmlSerializer serializer) throws java.io.IOException {

        serializer.startTag( NAMESPACE, tagName );
		
		if (build.getSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "sourceDirectory").text(build.getSourceDirectory()).endTag(NAMESPACE,
					"sourceDirectory");
		}
		if (build.getScriptSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "scriptSourceDirectory").text(build.getScriptSourceDirectory())
					.endTag(NAMESPACE, "scriptSourceDirectory");
		}
		if (build.getTestSourceDirectory() != null) {
			serializer.startTag(NAMESPACE, "testSourceDirectory").text(build.getTestSourceDirectory()).endTag(NAMESPACE,
					"testSourceDirectory");
		}
		if (build.getOutputDirectory() != null) {
			serializer.startTag(NAMESPACE, "outputDirectory").text(build.getOutputDirectory()).endTag(NAMESPACE,
					"outputDirectory");
		}
		if (build.getTestOutputDirectory() != null) {
			serializer.startTag(NAMESPACE, "testOutputDirectory").text(build.getTestOutputDirectory()).endTag(NAMESPACE,
					"testOutputDirectory");
		}
		if ((build.getExtensions() != null) && (build.getExtensions().size() > 0)) {
			serializer.startTag(NAMESPACE, "extensions");
			for (Iterator iter = build.getExtensions().iterator(); iter.hasNext();) {
				Extension o = (Extension) iter.next();
				writeExtension(o, "extension", serializer);
			}
			serializer.endTag(NAMESPACE, "extensions");
		}
		if (build.getDefaultGoal() != null) {
			serializer.startTag(NAMESPACE, "defaultGoal").text(build.getDefaultGoal()).endTag(NAMESPACE, "defaultGoal");
		}
		if ((build.getResources() != null) && (build.getResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "resources");
			for (Iterator iter = build.getResources().iterator(); iter.hasNext();) {
				Resource o = (Resource) iter.next();
				writeResource(o, "resource", serializer);
			}
			serializer.endTag(NAMESPACE, "resources");
		}
		if ((build.getTestResources() != null) && (build.getTestResources().size() > 0)) {
			serializer.startTag(NAMESPACE, "testResources");
			for (Iterator iter = build.getTestResources().iterator(); iter.hasNext();) {
				Resource o = (Resource) iter.next();
				writeResource(o, "testResource", serializer);
			}
			serializer.endTag(NAMESPACE, "testResources");
		}
		if (build.getDirectory() != null) {
			serializer.startTag(NAMESPACE, "directory").text(build.getDirectory()).endTag(NAMESPACE, "directory");
		}
		if (build.getFinalName() != null) {
			serializer.startTag(NAMESPACE, "finalName").text(build.getFinalName()).endTag(NAMESPACE, "finalName");
		}
		if ((build.getFilters() != null) && (build.getFilters().size() > 0)) {
			serializer.startTag(NAMESPACE, "filters");
			for (Iterator iter = build.getFilters().iterator(); iter.hasNext();) {
				String filter = (String) iter.next();
				serializer.startTag(NAMESPACE, "filter").text(filter).endTag(NAMESPACE, "filter");
			}
			serializer.endTag(NAMESPACE, "filters");
		}
		if (build.getPluginManagement() != null) {
			writePluginManagement((PluginManagement) build.getPluginManagement(), "pluginManagement", serializer);
		}
		if ((build.getPlugins() != null) && (build.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (Iterator iter = build.getPlugins().iterator(); iter.hasNext();) {
				Plugin o = (Plugin) iter.next();
				writePlugin(o, "plugin", serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writeExtension(Extension extension, String tagName, XmlSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (extension.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(extension.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (extension.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(extension.getArtifactId()).endTag(NAMESPACE,
					"artifactId");
		}
		if (extension.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(extension.getVersion()).endTag(NAMESPACE, "version");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writePluginManagement(PluginManagement pluginManagement, String tagName, XmlSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if ((pluginManagement.getPlugins() != null) && (pluginManagement.getPlugins().size() > 0)) {
			serializer.startTag(NAMESPACE, "plugins");
			for (Iterator iter = pluginManagement.getPlugins().iterator(); iter.hasNext();) {
				Plugin o = (Plugin) iter.next();
				writePlugin(o, "plugin", serializer);
			}
			serializer.endTag(NAMESPACE, "plugins");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writePlugin(Plugin plugin, String tagName, XmlSerializer serializer) throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if ((plugin.getGroupId() != null) && !plugin.getGroupId().equals("org.apache.maven.plugins")) {
			serializer.startTag(NAMESPACE, "groupId").text(plugin.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (plugin.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(plugin.getArtifactId()).endTag(NAMESPACE, "artifactId");
		}
		if (plugin.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(plugin.getVersion()).endTag(NAMESPACE, "version");
		}
		if (plugin.getExtensions() != null) {
			serializer.startTag(NAMESPACE, "extensions").text(plugin.getExtensions()).endTag(NAMESPACE, "extensions");
		}
		if ((plugin.getExecutions() != null) && (plugin.getExecutions().size() > 0)) {
			serializer.startTag(NAMESPACE, "executions");
			for (Iterator iter = plugin.getExecutions().iterator(); iter.hasNext();) {
				PluginExecution o = (PluginExecution) iter.next();
				writePluginExecution(o, "execution", serializer);
			}
			serializer.endTag(NAMESPACE, "executions");
		}
		if ((plugin.getDependencies() != null) && (plugin.getDependencies().size() > 0)) {
			serializer.startTag(NAMESPACE, "dependencies");
			for (Iterator iter = plugin.getDependencies().iterator(); iter.hasNext();) {
				Dependency o = (Dependency) iter.next();
				writeDependency(o, "dependency", serializer);
			}
			serializer.endTag(NAMESPACE, "dependencies");
		}
		if (plugin.getGoals() != null) {
			((Xpp3Dom) plugin.getGoals()).writeToSerializer(NAMESPACE, serializer);
		}
		if (plugin.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(plugin.getInherited()).endTag(NAMESPACE, "inherited");
		}
		if (plugin.getConfiguration() != null) {
			((Xpp3Dom) plugin.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writeResource(Resource resource, String tagName, XmlSerializer serializer) throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (resource.getTargetPath() != null) {
			serializer.startTag(NAMESPACE, "targetPath").text(resource.getTargetPath()).endTag(NAMESPACE, "targetPath");
		}
		if (resource.getFiltering() != null) {
			serializer.startTag(NAMESPACE, "filtering").text(resource.getFiltering()).endTag(NAMESPACE, "filtering");
		}
		if (resource.getDirectory() != null) {
			serializer.startTag(NAMESPACE, "directory").text(resource.getDirectory()).endTag(NAMESPACE, "directory");
		}
		if ((resource.getIncludes() != null) && (resource.getIncludes().size() > 0)) {
			serializer.startTag(NAMESPACE, "includes");
			for (Iterator iter = resource.getIncludes().iterator(); iter.hasNext();) {
				String include = (String) iter.next();
				serializer.startTag(NAMESPACE, "include").text(include).endTag(NAMESPACE, "include");
			}
			serializer.endTag(NAMESPACE, "includes");
		}
		if ((resource.getExcludes() != null) && (resource.getExcludes().size() > 0)) {
			serializer.startTag(NAMESPACE, "excludes");
			for (Iterator iter = resource.getExcludes().iterator(); iter.hasNext();) {
				String exclude = (String) iter.next();
				serializer.startTag(NAMESPACE, "exclude").text(exclude).endTag(NAMESPACE, "exclude");
			}
			serializer.endTag(NAMESPACE, "excludes");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writeDependency(Dependency dependency, String tagName, XmlSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (dependency.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(dependency.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		if (dependency.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(dependency.getArtifactId()).endTag(NAMESPACE,
					"artifactId");
		}
		if (dependency.getVersion() != null) {
			serializer.startTag(NAMESPACE, "version").text(dependency.getVersion()).endTag(NAMESPACE, "version");
		}
		if ((dependency.getType() != null) && !dependency.getType().equals("jar")) {
			serializer.startTag(NAMESPACE, "type").text(dependency.getType()).endTag(NAMESPACE, "type");
		}
		if (dependency.getClassifier() != null) {
			serializer.startTag(NAMESPACE, "classifier").text(dependency.getClassifier()).endTag(NAMESPACE,
					"classifier");
		}
		if (dependency.getScope() != null) {
			serializer.startTag(NAMESPACE, "scope").text(dependency.getScope()).endTag(NAMESPACE, "scope");
		}
		if (dependency.getSystemPath() != null) {
			serializer.startTag(NAMESPACE, "systemPath").text(dependency.getSystemPath()).endTag(NAMESPACE,
					"systemPath");
		}
		if ((dependency.getExclusions() != null) && (dependency.getExclusions().size() > 0)) {
			serializer.startTag(NAMESPACE, "exclusions");
			for (Iterator iter = dependency.getExclusions().iterator(); iter.hasNext();) {
				Exclusion o = (Exclusion) iter.next();
				writeExclusion(o, "exclusion", serializer);
			}
			serializer.endTag(NAMESPACE, "exclusions");
		}
		if (dependency.getOptional() != null) {
			serializer.startTag(NAMESPACE, "optional").text(dependency.getOptional()).endTag(NAMESPACE, "optional");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writeExclusion(Exclusion exclusion, String tagName, XmlSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if (exclusion.getArtifactId() != null) {
			serializer.startTag(NAMESPACE, "artifactId").text(exclusion.getArtifactId()).endTag(NAMESPACE,
					"artifactId");
		}
		if (exclusion.getGroupId() != null) {
			serializer.startTag(NAMESPACE, "groupId").text(exclusion.getGroupId()).endTag(NAMESPACE, "groupId");
		}
		serializer.endTag(NAMESPACE, tagName);
	}

	private static void writePluginExecution(PluginExecution pluginExecution, String tagName, XmlSerializer serializer)
			throws java.io.IOException {
		serializer.startTag(NAMESPACE, tagName);
		if ((pluginExecution.getId() != null) && !pluginExecution.getId().equals("default")) {
			serializer.startTag(NAMESPACE, "id").text(pluginExecution.getId()).endTag(NAMESPACE, "id");
		}
		if (pluginExecution.getPhase() != null) {
			serializer.startTag(NAMESPACE, "phase").text(pluginExecution.getPhase()).endTag(NAMESPACE, "phase");
		}
		if ((pluginExecution.getGoals() != null) && (pluginExecution.getGoals().size() > 0)) {
			serializer.startTag(NAMESPACE, "goals");
			for (Iterator iter = pluginExecution.getGoals().iterator(); iter.hasNext();) {
				String goal = (String) iter.next();
				serializer.startTag(NAMESPACE, "goal").text(goal).endTag(NAMESPACE, "goal");
			}
			serializer.endTag(NAMESPACE, "goals");
		}
		if (pluginExecution.getInherited() != null) {
			serializer.startTag(NAMESPACE, "inherited").text(pluginExecution.getInherited()).endTag(NAMESPACE,
					"inherited");
		}
		if (pluginExecution.getConfiguration() != null) {
			((Xpp3Dom) pluginExecution.getConfiguration()).writeToSerializer(NAMESPACE, serializer);
		}
		serializer.endTag(NAMESPACE, tagName);
	}
}
