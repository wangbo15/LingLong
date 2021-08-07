package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import edu.pku.sei.conditon.dedu.extern.pf.BUPathFinder;
import edu.pku.sei.conditon.dedu.extern.pf.BeamSearchStrategy;
import edu.pku.sei.conditon.dedu.extern.pf.PathFinder;
import edu.pku.sei.conditon.dedu.extern.pf.SearchStrategy;

public class PredictorUtil {
	
	protected static Logger logger = Logger.getLogger(PredictorUtil.class);
	
	private static final boolean INVOKE_BY_CMD_LINE = false;
	
	private static void directlyInvokePredictor(ProjectConfig config, Suspect suspect, int sid, List<String> designatedVars) {
        assert System.getProperty("java.version").startsWith("1.7."): "JDK VERSION MUST BE 1.7";

        String bugName = config.getProjectName();
		String srcRoot = config.getSrcRoot().getAbsolutePath();
		String testRoot = config.getTestSrcRoot().getAbsolutePath();
		String filePath = suspect.getFile();
        int line = suspect.getLine();
        try {
        	if(designatedVars == null || designatedVars.isEmpty()) {
        		SearchStrategy searchStrategy = BeamSearchStrategy.getInstance();
        		PathFinder pf = new BUPathFinder(bugName, srcRoot, testRoot, filePath, line, sid, searchStrategy);
				pf.entry();

        		//FileInvoker.predict(bugName, srcRoot, testRoot, filePath, line, ithSuspicous);
        	}else {
        		//FileInvoker.predict(bugName, srcRoot, testRoot, filePath, line, sid, designatedVars);
        		SearchStrategy searchStrategy = BeamSearchStrategy.getInstance();
        		PathFinder pf = new BUPathFinder(bugName, srcRoot, testRoot, filePath, line, sid, searchStrategy);
        		pf.setDesignatedVars(designatedVars);
				pf.entry();
        	}
        }catch(Exception e) {
        	e.printStackTrace();
        	System.out.println("EXCEPTION ON PREDICTOR");
        }
	}
	
	private static String getPredCmd(ProjectConfig config, Suspect suspect, int ithSuspicous){
		String srcRoot = config.getSrcRoot().getAbsolutePath();
		String testSrcRoot = config.getTestSrcRoot().getAbsolutePath();
		
		String filePath = suspect.getFile();
		
		File srcFile = new File(srcRoot + "/" + filePath);
		assert srcFile.exists(): "NO SUCH SOUCE JAVA FILE: " + srcRoot + "/" + filePath;
        
        int lineNumber = suspect.getLine();
        
        assert System.getProperty("java.version").startsWith("1.7."): "JDK VERSION MUST BE 1.7";
        
        String jre = System.getProperty("java.home");

        String predCmd = jre + "/bin/java -jar ./lib/Condition.jar " + config.getProjectName() + 
                " " + srcRoot + " " + testSrcRoot + " " + filePath + " " + lineNumber + " " + ithSuspicous;
        
        return predCmd;
	}
	
	private static List<String> loadIfConditions(String projectAndBug, int ith){
        List<String> res = new ArrayList<>();
        String project = projectAndBug.split("_")[0].toLowerCase();

        List<String> thisExprs = new ArrayList<>();
//        String bugID = projectAndBug.split("_")[1];

        String filePath = Config.PREDICTOR_OUT_ROOT + project + "/res/" + projectAndBug.toLowerCase() + "_" + ith + ".res.csv";

        File rslFile = new File(filePath);
        if(rslFile.exists() == false){//TODO:: assert 
        	return res;
        }
        
        FileReader freader = null;
        BufferedReader bReader = null;
        try {
        	freader = new FileReader(rslFile);
            bReader = new BufferedReader(freader);
        
            String line = null;
            while ((line = bReader.readLine()) != null) {
                line = line.split("\t")[0];
                if(line.contains(" & ") || line.contains(" | ")){
                    continue;
                }
                if(line.contains("this.")){
                    thisExprs.add(line);
                }else{
                    res.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	FileUtil.safeCloseCloseables(bReader, freader);
        }
        //add this exprs to the end
        res.addAll(thisExprs);
        return res;
    }
	
	public static List<String> predictIfConds(ProjectConfig config, Suspect suspect, int ithSuspicous, List<String> designatedVars) {
		//TODO:: for debug
		directlyInvokePredictor(config, suspect, ithSuspicous, designatedVars);
		
		return loadPredictions(config, ithSuspicous);
	}
	

	public static List<String> predictIfConds(ProjectConfig config, Suspect suspect, int ithSuspicous) {
		
		//TODO::debug
		if(INVOKE_BY_CMD_LINE) {
			try {
				String cmd = PredictorUtil.getPredCmd(config, suspect, ithSuspicous);
				if(runPredictor(cmd) == false){
					//TODO:: need log
					System.err.println("Fail to run predictor !!");
					return null;
				}
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else {
			//TODO:: for debug
			directlyInvokePredictor(config, suspect, ithSuspicous, null);
		}
        
		return loadPredictions(config, ithSuspicous);
	}
	
	private static List<String> loadPredictions(ProjectConfig config, int ithSuspicous){
		List<String> allConditions = PredictorUtil.loadIfConditions(config.getProjectName(), ithSuspicous);
        
        if(allConditions.size() == 0){
        	return Collections.<String>emptyList();
        }
		
		logger.info("PREDICATED COND NUM: " + allConditions.size());
		return allConditions;
	}
	
	private static boolean runPredictor(String predCmd){
		List<String>[] msgs = CmdUtil.runCmd(predCmd, null);
        List<String> errMsgs = msgs[1];
        for(String s : errMsgs){
            if(s.startsWith("PREDICTOR ERROR!")){
                return false;
            }
        }
        return true;
    }
}
