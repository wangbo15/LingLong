package cn.edu.pku.sei.plde.hanabi.utils;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import edu.pku.sei.conditon.util.DateUtil;
import edu.pku.sei.conditon.util.FileUtil;

public class Logger {
	
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static void recordTime(String msg, long time) {
		
		assert msg != null && msg.length() > 0;
		assert time >= 0;
		
		StringBuffer recordMsg = new StringBuffer();
		String currentTime = DateUtil.getFormatedCurrDate(FORMAT);
		String usedTime = DateUtil.millisecToTimeStr(time);
		recordMsg.append("[" + currentTime + "]\t" + msg + "\t" + usedTime + "\n");
		FileUtil.writeStringToFile(Config.FIX_TIEM_LOG_FILE_PATH, recordMsg.toString(), true);
	}
	
	public static void recordMassage(String msg) {
		
		assert msg != null && msg.length() > 0;
		
		StringBuffer recordMsg = new StringBuffer();
		String currentTime = DateUtil.getFormatedCurrDate(FORMAT);
		recordMsg.append("[" + currentTime + "]\t" + msg + "\n");
		FileUtil.writeStringToFile(Config.FIX_TIEM_LOG_FILE_PATH, recordMsg.toString(), true);
	}
	
}
