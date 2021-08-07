package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MvnRunner {
	
	public static class MvnLifeCycle{
		//A Build Lifecycle is Made Up of Phases
		public static final String VALIDATE = "validate";
		public static final String COMPILE = "compile";
		public static final String TEST = "test";
		public static final String PACKAGE = "package";
		public static final String VERIFY = "verify";
		public static final String INSTALL = "install";
		public static final String DEPLOY = "deploy";
		
		//three built-in build lifecycles: default, clean and site
		public static final String DEFAULT = "default";
		public static final String CLEAN = "clean";
		public static final String SITE = "site";
	}
	
	
	private final File mvnHome = new File("/home/nightwish/program_files/apache-maven-3.5.0/");
	
	private InvocationRequest request;
	private Invoker invoker;
	
	public MvnRunner(String root) {
		File pomFile = new File(root + "/pom.xml");
		
		assert mvnHome.exists();
		assert pomFile.exists();
		
		request = new DefaultInvocationRequest();
		request.setPomFile(pomFile);
		
		invoker = new DefaultInvoker();
		invoker.setMavenHome(mvnHome);
	}

	/**
	 * @param cmd, the mvn target tobe executed
	 * @return the exit code
	 */
	public int run(String cmd) {
		int exitCode = 1;
		request.setGoals(Arrays.asList(cmd));
		try {
			InvocationResult result = invoker.execute(request);
			
			//System.out.println("EXIT CODE " + result.getExitCode());
			exitCode = result.getExitCode();
			
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
		return exitCode;
	}
}
