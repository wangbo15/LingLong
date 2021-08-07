package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.proj.ProInfo;

public class ProInfoUtil {
	
	public final static String PROINFO_ROOT = DeduMain.OUTPUT_ROOT;
	
	public static ProInfo loadProInfo(String bugName, String srcRoot, String testRoot, String jdkLevel){
		ProInfo proInfo = null;
		String path =  PROINFO_ROOT + bugName + ".pro";
		File file = new File(path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(!file.exists()){
			System.out.println("NO EXSITING PROINFO, GENERATTE FOR " + bugName);
			proInfo  = getProjInfo(bugName, srcRoot, testRoot, jdkLevel);
		}else {
			FileInputStream fs = null;
			ObjectInputStream os = null;
			try {
				fs = new FileInputStream(path);
				os = new ObjectInputStream(fs);
				proInfo = (ProInfo) os.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} finally{
				edu.pku.sei.conditon.util.FileUtil.closeInputStream(fs, os);
			}
		}
		return proInfo;
	}
	
	private static ProInfo getProjInfo(String bugName, String filePath, String fileTestPath, String jdkLevel){
		ProInfo proInfo = new ProInfo(bugName , filePath, fileTestPath, jdkLevel);
		proInfo.collectProInfo2();

		String dumpPath = PROINFO_ROOT + bugName + ".pro";
		
		FileOutputStream fs = null;
		ObjectOutputStream os = null;
		try {
			File file = new File(dumpPath);
			if(file.getParentFile().exists() == false){
				file.getParentFile().mkdirs();
			}
			
			fs = new FileOutputStream(dumpPath);
			os =  new ObjectOutputStream(fs);
			os.writeObject(proInfo);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			edu.pku.sei.conditon.util.FileUtil.closeInputStream(fs, os);
		}
		
		return proInfo;
	}
}
