package cn.edu.pku.sei.plde.hanabi.main;

public class Config {
	
	public static final String USER_HOME = System.getProperties().getProperty("user.home") + "/";
	public static final String LINGLONG_ROOT = USER_HOME + "workspace/eclipse/LingLong/";
	
	//TODO:: move to file
	public static final String FIXER_ROOT_PATH = LINGLONG_ROOT + "Hanabi/";
	
	public static final String D4J_ROOT = USER_HOME + "workspace/defects4j/";
	
	public static final String D4J_SRC_ROOT = D4J_ROOT + "src/";

	public static final String BUGS_DOT_JAR_ROOT = USER_HOME + "workspace/gitrepos/apache/";
			// USER_HOME + "workspace/bug_repair/bugs-dot-jar/";
	
    public static final String RESULT_PATH = FIXER_ROOT_PATH + "resultMessage";
    public static final String PATCH_SOURCE_PATH = RESULT_PATH + "/patchSource";
    
    public static final String FIX_TIEM_LOG_FILE_PATH = RESULT_PATH + "/used_time.log";
	
	public static final String JAVA7_PATH = USER_HOME + "program_files/jdk1.7.0_79/bin/";
	
    public static final String PREDICTOR_OUT_ROOT = LINGLONG_ROOT + "Cond/python/output/";
    
    public static final String TEMP_FILES_PATH = ".temp/";
    
    public static final String TEMP_SRC_BACKUP_PATH = TEMP_FILES_PATH + "src/";
    
    public static final String TEMP_CLS_BACKUP_PATH = TEMP_FILES_PATH + "cls/";
    
    public static final String TEMP_SUSPICIOUS_PATH = TEMP_FILES_PATH + "sus/";
    
    public static final String TEMP_TRACE_FOLDER = FIXER_ROOT_PATH + ".temp/trace/"; 
    
    public static final String ASM_TRACE_FOLDER = TEMP_FILES_PATH + "asm/";
    
//    /** run all test time budget */
//    public static final int RUN_ALL_TIME_BUDGET_IN_MINUT = 2;
//    public static final int RUN_SINGLE_TIME_BUDGET_IN_SEC = 20; 

}
