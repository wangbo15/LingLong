package cn.edu.pku.sei.plde.hanabi.fl.asm.instru.exrt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is for self-implemented fault localization. 
 */
public class Tracer {
	
	private static final boolean OPT = true;
	
	private static final boolean DEFAULT_PASSED = true;
	
	public final static File DUMP_FOLDER = new File("/home/nightwish/workspace/eclipse/Hanabi/.temp/asm/");

	private static boolean passed = DEFAULT_PASSED;
	
	private static boolean verbose = true;
	
	private static String testCls;
	private static String testMtd;
		
	/** 
	 * KEY: file_name#line_number, such as org.apache.commons.math3.exception.MathIllegalStateException#80
	 * VALUE: covered time, must be no less than 1 
	 * */
	private final static Map<String, Integer> coverageMap = new ConcurrentHashMap<>();
	
	public static void setVerbose(boolean verbose) {
		Tracer.verbose = verbose;
	}
	
	/**
	 * Called at the beginning of each test method
	 */
	public synchronized static void init(final String testCls, final String testMtd) {
		
		assert testCls != null: "NULL TEST CLASS NAME";
		assert testMtd != null: "NULL TEST METHOD NAME";
		
		if(!DUMP_FOLDER.exists()) {
			DUMP_FOLDER.mkdirs();
		}
		
		Tracer.testCls = testCls;
		Tracer.testMtd = testMtd;
		
		passed = DEFAULT_PASSED;
		
		if(verbose) {
			String msg = "\n>>>>>>>> INIT: " + testCls + "#" + testMtd + 
					(coverageMap.isEmpty() ? "" : " WITH " + coverageMap.size() + " STMTS ");
			System.out.println(msg);
		}
		
	}
	
	/**
	 * invoked by instrumented classes
	 * @param file
	 * @param line
	 */
	public synchronized static void trace(final String file, final int line) {
		String key = file + "#" + line;
		if(coverageMap.containsKey(key)) {
			if(OPT) {
				return;
			}
			int val = coverageMap.get(key) + 1;
			coverageMap.put(key, val);
		}else {
			coverageMap.put(key, 1);
			
			if(verbose) {
				System.out.println("COVERED: " + key);				
			}
		}
	}
	
	/**
	 * Called at the catch block, in the instrumented test method 
	 */
	public synchronized static void setFailure() {
		passed = false;
	}
	
	/**
	 * Called at the finally block, write coverage information to a file.
	 */
	public synchronized static void dump() {
		
		String fileName = testCls + "-" + testMtd + "-" + passed + ".txt";
		File outputFile = new File(DUMP_FOLDER.getAbsolutePath() + "/" + fileName);

		
		StringBuffer sb = new StringBuffer(">>\n");
		for(String s : coverageMap.keySet()) {
			sb.append(s);
			sb.append('\n');
		}
		
		sb.append("<<\n");
		writeStringToFile(outputFile, sb.toString(), true);
		
		if(verbose) {
			System.out.println(">>>>>>>> COV STMT NUM: " + coverageMap.size());
			System.out.println(">>>>>>>> DUMP TO: " + fileName);
		}
		
		coverageMap.clear();
	}
	
	/**
	 * Copy from cn.edu.pku.sei.plde.hanabi.utils.FileUtil
	 */
	
	private synchronized static boolean writeStringToFile(File file, String string, boolean append) {
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
			bufferedWriter.write(string);
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
			
}
