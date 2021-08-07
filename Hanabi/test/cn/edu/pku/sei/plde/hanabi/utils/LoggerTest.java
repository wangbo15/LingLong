package cn.edu.pku.sei.plde.hanabi.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Logger;

public class LoggerTest {
	private static final File TIME_REC_FILE = new File(Config.FIX_TIEM_LOG_FILE_PATH); 

	@Before
	public void setUp() throws Exception {
		if(TIME_REC_FILE.exists()) {
			TIME_REC_FILE.delete();
		}
	}

	@After
	public void tearDown() throws Exception {
		TIME_REC_FILE.delete();
	}

	@Test
	public void test() {
		Logger.recordTime("ALL\tSTART\tMATH_12", 5000);
		assertTrue(TIME_REC_FILE.exists());
		
		Logger.recordTime("FL\tSTART\tMATH_12", (3600 + 610) * 1000);
		Logger.recordTime("FL\tEND\tMATH_12", (4800 + 5) * 1000);
		Logger.recordTime("FIX\tSTART\tMATH_12", (7200) * 1000);
		Logger.recordTime("FIX\tEND\tMATH_12", (7200 + 3) * 1000);
		Logger.recordTime("ALL\tEND\tMATH_12", (7200 + 360) * 1000);

		String content = FileUtil.readFileToString(TIME_REC_FILE);
		System.out.println(content);
	}

}
