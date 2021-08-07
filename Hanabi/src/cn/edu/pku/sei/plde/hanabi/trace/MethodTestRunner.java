package cn.edu.pku.sei.plde.hanabi.trace;

import java.io.File;
import java.util.List;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.main.Config;


public class MethodTestRunner {
	
	private static String traceFilePath;
	
	public static String getTraceFilePath() {
		return traceFilePath;
	}
	
	/**
	 * Uniform rule to generate the path of the current trace file
	 * @param bugName
	 * @param mtdFullName
	 * @param line
	 * @return
	 */
	public static String generateTraceFilePath(String bugName, String mtdFullName, int line) {
		return generateTraceFileContainingFolder(bugName) + mtdFullName.replaceAll("#", ".") + "." + line + ".txt";
	}
	
	/**
	 * Uniform interface the get the containing folder of the trace files of the current project
	 * @param bugName
	 * @return
	 */
	public static String generateTraceFileContainingFolder(String bugName) {
		return Config.TEMP_TRACE_FOLDER + bugName.toLowerCase() + "/";
	}
	
	/**
	 * COMPILE:
	 * Directly compiled by eclipse
	 * 
	 * RUN ANGELIC:
	 * java -cp path/to/testclasses:path/to/junit.jar MethodTestRunner MATH_1 com.mycompany.product.MyTest#testB ANGELIC line true true
	 * 
	 * RUN TRACE:
	 * java -cp path/to/testclasses:path/to/junit.jar MethodTestRunner MATH_1 com.mycompany.product.MyTest#testB TRACE line
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String... args) throws ClassNotFoundException {
		
		System.err.println("MethodTestRunner >>>> BEGIN >>>>");
		System.err.println("BUG_NM: " + args[0]);
		System.err.println("METHOD: " + args[1]);
		System.err.println("MISSON: " + args[2]);
		System.err.println("LINE  : " + args[3]);
		
		String bugName = args[0];
		String mtdFullName = args[1];
		String missionType = args[2];

		switch(missionType) {
		case "ANGELIC":
			System.err.println("ENABLE: " + args[4]);
			System.err.println("AG_VAL: " + args[5]);
			String enabled = args[4];
			String angelVal = args[5];
			processAngelic(enabled, angelVal);
			break;
		case "TRACE":
			break;
		default:
			errorExit("MISSON TYPE ERROR");
		}
		
		int line = Integer.valueOf(args[3]);
		
		String folderPath = Config.TEMP_TRACE_FOLDER + bugName.toLowerCase() + "/";
		File folder = new File(folderPath);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		
		traceFilePath = generateTraceFilePath(bugName, mtdFullName, line);
		File traceFile = new File(traceFilePath);
		//make the trace file to be the newest always
		if(traceFile.exists()) {
			traceFile.delete();
		}
		
		System.err.println("TR_FILE: " + traceFilePath);
		
		// set the property for log4j: ${log.base}
		if(ProjectConfig.isBugsDotJarProject(bugName)) {
			System.setProperty("log.base", Config.FIXER_ROOT_PATH);
		}
		
		String[] classAndMethod = mtdFullName.split("#");
		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);

		JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
		Result result = junit.run(request);
		
		if(missionType.equals("ANGELIC")) {
			List<Failure> failures = result.getFailures();
			for (Failure failure : failures) {
				Throwable throwable = failure.getException();
				if (throwable instanceof ClassCastException) {
					// args[5] is the angelic value
					boolean angel = Boolean.valueOf(args[5]);
					System.exit(angel ? 0 : 1);
				}
			}
		}
		
		boolean succ = result.wasSuccessful();
		if(succ){
			System.err.println("MethodTestRunner >>>> SUCC " + bugName + "  " + mtdFullName + " >>>>\n");
		}else{
			System.err.println("MethodTestRunner >>>> FAIL " + bugName + "  " + mtdFullName + " >>>>\n");
		}
		
		System.exit(succ ? 0 : 1);
	}

	private static void errorExit(String msg) {
		System.err.println("MethodTestRunner ERROR EXIT >>>> " + msg + " >>>>");
		System.exit(-1);
	}
	
	private static void processAngelic(String enabled, String angelValue) {
		switch(enabled) {
		case "true":
			cn.edu.pku.sei.plde.hanabi.trace.runtime.AngelicExecution.enable();
			break;
		case "false":
			cn.edu.pku.sei.plde.hanabi.trace.runtime.AngelicExecution.disable();
			break;
		default:
			errorExit("ANGELIC ENABLED");
		}
		
		switch(angelValue) {
		case "true":
			cn.edu.pku.sei.plde.hanabi.trace.runtime.AngelicExecution.setBooleanValue(true);
			break;
		case "false":
			cn.edu.pku.sei.plde.hanabi.trace.runtime.AngelicExecution.setBooleanValue(false);
			break;
		default:
			errorExit("ANGELIC VALUE");
		}
	} 
}
