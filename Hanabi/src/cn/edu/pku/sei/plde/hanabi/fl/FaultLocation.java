package cn.edu.pku.sei.plde.hanabi.fl;

import java.io.File;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import edu.pku.sei.conditon.util.FileUtil;

public abstract class FaultLocation {
	
	protected ProjectConfig projectConfig;
	
	public ProjectConfig getProjectConfig(){
		return projectConfig;
	}
	
	public FaultLocation(ProjectConfig projectConfig){
		this.projectConfig = projectConfig;
	}
	
	/**
	 * @return all suspects to be fixed
	 */
	public abstract List<Suspect> getAllSuspects();
	
	
	/**
	 * backup all suspects in a text file, for debugging
	 * @param suspects
	 */
	protected void logAllSuspects(List<Suspect> suspects) {
		File resText = new File(Config.TEMP_SUSPICIOUS_PATH + projectConfig.getProjectName() + ".txt");
		if(!resText.getParentFile().exists()) {
			resText.getParentFile().mkdirs();
		}
		StringBuffer sb = new StringBuffer();
		for(Suspect sus: suspects) {
			sb.append(sus.toString() + "\n");
		}
		FileUtil.writeStringToFile(resText, sb.toString(), false);
	}
}
