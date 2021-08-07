package cn.edu.pku.sei.plde.hanabi.trace.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner;
import cn.edu.pku.sei.plde.hanabi.trace.collector.ValueCollector;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Pair;

public class RuntimeValues<T> {
	
	public static final String TRACE_HEAD = ">>";
	public static final String TRACE_TAIL = "<<";

	public static final int MAX_TRACED_ITEM = 10000;
	
	private static List<Pair<String, Object>> valueBuffer = new ArrayList<>(MAX_TRACED_ITEM);
	
	private static int dumpTime = 0;
	
	public static void putInStorage(Pair<String, Object> pair) {
		if(dumpTime < MAX_TRACED_ITEM) {
			valueBuffer.add(pair);
		}
	}
	
	public static void trace(String name, Object value){
        ValueCollector.collectFrom(name, value);
	}
	
	public static void dump(){
		if(valueBuffer.isEmpty()) {
			return;
		}
		dumpTime++;
		
		File traceFile = new File(MethodTestRunner.getTraceFilePath());
		StringBuffer sb = new StringBuffer(TRACE_HEAD + "\n");
		for(Pair<String, Object> p : valueBuffer){
			//System.out.println(p);
			//FileUtil.writeStringToFile(traceFile, p.toString() + "\n", true);
			sb.append(p.toString() + "\n");
		}
		sb.append(TRACE_TAIL + "\n");
		
		FileUtil.writeStringToFile(traceFile, sb.toString(), true);
		
		valueBuffer.clear();
	}
	
}
