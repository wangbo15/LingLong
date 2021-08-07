package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CmdUtil {
	
	private final static int BUFFER_SIZE = 1024 * Byte.SIZE;

	private final static String BREAK_LINE = System.getProperty("line.separator");
	
	public static int runByJava7(final String cmd, final File dir, final boolean verbose, 
			final List<String> stdout, final List<String> stderr) {
		
		int ret = -1;
		String[] args = new String[]{"/bin/bash", "-c", cmd};
		//String[] args = cmd.split("\\s+"); // ERROR, DO NOT WORK

		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			
			Map<String, String> env = pb.environment();
			env.remove("JAVA_HOME");
			env.remove("PATH");
			env.remove("CLASSPATH");
			env.remove("JRE_HOME");

			final String del = ":";

			// TODO:: move to config file
			env.put("JAVA_HOME", "/home/nightwish/program_files/jdk1.7.0_79/");
			env.put("ANT_HOME", "/home/nightwish/program_files/apache-ant-1.9.13/");
			env.put("D4J_HOME", "/home/nightwish/workspace/defects4j/framework/");
			env.put("MAVEN_HOME", "/home/nightwish/program_files/apache-maven-3.5.0/");
			env.put("CLASSPATH", "." + del + env.get("JAVA_HOME") + "lib");

			String orcPath = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin";
			env.put("PATH", env.get("JAVA_HOME") + "bin" + del 
					+ env.get("ANT_HOME") + "bin" + del
					+ env.get("D4J_HOME") + "bin" + del 
					+ env.get("MAVEN_HOME") + "bin" + del
					+ orcPath);
			env.put("JRE_HOME", env.get("JAVA_HOME") + "jre");
			
			pb.directory(dir);
			// File log = new File("log");
			// pb.redirectErrorStream(true);
			// pb.redirectOutput(Redirect.appendTo(log));
			final Process process = pb.start();
			
			String stdoutStr = getInputAsString(process.getInputStream());
			if(verbose) {
				System.out.println(stdoutStr);
			}
			if(stdout != null) {
				stdout.addAll(Arrays.asList(stdoutStr.split(BREAK_LINE)));
			}
			
			String stderrStr = getInputAsString(process.getErrorStream());
			if(verbose) {
				System.err.print(stderrStr);
			}
			if(stderr != null) {
				stderr.addAll(Arrays.asList(stderrStr.split(BREAK_LINE)));
			}
			
			process.waitFor();
			ret = process.exitValue();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static int runByJava7(final String cmd, final File dir) {
		return runByJava7(cmd, dir, false);
	}
	
	public static int runByJava7(final String cmd, final File dir, final boolean verbose) {
		return runByJava7(cmd, dir, verbose, null, null);
	}
	
	/**
	 * @param cmd
	 * @param dir
	 * @param verbose
	 * @return [0]: stdout, [1]: stderr
	 */
	public static List<String>[] runCmd(final String cmd, final File dir, final boolean verbose) {

		String stdoutStr = "";
		String stderrStr = "";
		try {
			final Process process = Runtime.getRuntime().exec(cmd, null, dir);

			stdoutStr = getInputAsString(process.getInputStream());
			if(verbose) {
				System.out.println(stdoutStr);
			}
			
			stderrStr = getInputAsString(process.getErrorStream());
			if(verbose) {
				System.err.print(stderrStr);
			}
			
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> stdout = Arrays.asList(stdoutStr.split(BREAK_LINE));
		List<String> stdErr = Arrays.asList(stderrStr.split(BREAK_LINE));

		List<String>[] array = new List[]{stdout, stdErr}; 
		return array;
	}

	/**
	 * @param cmd
	 * @param dir
	 * @return [0]: stdout, [1]: stderr
	 * verbose is ture
	 */
	public static List<String>[] runCmd(final String cmd, final File dir) {
		return runCmd(cmd, dir, true);
	}
	
	private static String getInputAsString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder builder = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = builder.toString();
		return result;
	}
}
