package cn.edu.pku.sei.plde.hanabi.fl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.edu.pku.sei.plde.hanabi.build.proj.D4jProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.fl.visitor.RefineVisitor;
import cn.edu.pku.sei.plde.hanabi.main.constant.D4jConstant;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;

class OffTheShellFL extends FaultLocation {

	OffTheShellFL(ProjectConfig projectConfig) {
		super(projectConfig);

		assert this.getProjectConfig() instanceof D4jProjectConfig;
	}

	@Override
	public List<Suspect> getAllSuspects() {
		List<Suspect> result = loadAll();
		filtrate(this.projectConfig, result);
		return result;
	}
	/**
	 * @param projectConfig
	 * @param all
	 * return: a cache of cu
	 */
	protected static void filtrate(ProjectConfig projectConfig, List<Suspect> all){
		// file to it's suspect list 
		Map<String, List<Suspect>> classfyByFile = classfyByFile(all);
		
		for(Entry<String, List<Suspect>> entry : classfyByFile.entrySet()){
			String path = FileUtil.classNameToFilePath(entry.getKey());
			if(path == null){
				continue;
			}
			path = projectConfig.getSrcRoot() + "/" + path + ".java";
			File file = new File(path);
			CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(file), projectConfig.getTestJdkLevel(), ASTParser.K_COMPILATION_UNIT);
			
			RefineVisitor visitor = new RefineVisitor(entry.getKey(), cu, all);
			cu.accept(visitor);
		}
		List<Suspect> toBeRemoved = new ArrayList<>();
		for(Suspect sus : all) {
			if(sus.getClassName().toLowerCase().contains("exception")) {
				toBeRemoved.add(sus);
			}
		}
		all.removeAll(toBeRemoved);
		
	}
	
	private static Map<String, List<Suspect>> classfyByFile(List<Suspect> all){
		Map<String, List<Suspect>> classfyByFile = new HashMap<>();
		for(Suspect sus : all){
			String clsName = sus.getClassName();
			List<Suspect> withinFile = null;
			if(classfyByFile.containsKey(clsName)){
				withinFile = classfyByFile.get(clsName);
			}else{
				withinFile = new ArrayList<>();
				classfyByFile.put(clsName, withinFile);
			}
			withinFile.add(sus);
		}
		return classfyByFile;
	}
	
	private List<Suspect> loadAll(){
		List<Suspect> result = new ArrayList<>();
		List<String> locRes = FileUtil.readFileToStringList(this.getLocationFile());
		for (String line : locRes) {
			// sth like:
			// 'org.apache.commons.math3.exception.MathIllegalStateException#80,1.0,(1,6)'
			String[] columns = line.split(",");
			String key = columns[0];
			Double score = Double.valueOf(columns[1]);
			if (score > 0.0D) {
				//key: ClassName#LineNumber
				String[] arr = key.split("#");
				Suspect sus = new Suspect(arr[0], Integer.valueOf(arr[1]), score, null, null);
				result.add(sus);
			}
		}
		
		return result;
	}

	private File getLocationFile() {
		D4jProjectConfig d4jProject = (D4jProjectConfig) this.projectConfig;
		String path = D4jConstant.D4J_FL_ROOT + d4jProject.getBugProject() + "/" + d4jProject.getBugId() + ".txt";
		File file = new File(path);
		assert file.exists(): file.getAbsolutePath();
		return file;
	}
	
}
