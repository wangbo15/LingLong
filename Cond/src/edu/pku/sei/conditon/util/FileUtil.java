package edu.pku.sei.conditon.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FileUtil {
	
	public static void deleteSingleFile(String path) {
		File f = new File(path);
		deleteSingleFile(f);
	}
	
	public static void deleteSingleFile(File f) {
		if(f.isDirectory()) {
			return;
		}
		if(f.exists()) {
			f.delete();
		}
	}
	
	public static void closeInputStream(Closeable... streams){
		for(Closeable stream: streams){
			if(stream == null){
				return;
			}else{			
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String classNameToFilePath(String className){
		if(className.contains("$")){
			try {
				throw new Exception("Do not allow inner class currently: " + className);
			} catch (Exception e) {
//				e.printStackTrace();
				return null;
			}
		}
		return className.replace(".", "/").trim();
	}
	
	public static String classNameToItsSrcPath(String className){
		String file = className.replace(".", "/");
        if(file.contains("$")){
            int dolarIdx = file.indexOf('$');
            file = file.substring(0, dolarIdx) + ".java";
        }else{
        	file += ".java";
        }
        return file;
	}
	
	public static String classNameToItsClsPath(String className) {
		String file = className.replace(".", "/");
		if (file.contains("$")) {
			int dolarIdx = file.indexOf('$');
			file = file.substring(0, dolarIdx) + ".class";
		} else {
			file += ".class";
		}
		return file;
	}
	
	public static File getExistingFile(String path){
		File file = new File(path);
		if (!file.exists()) {
			throw new Error("NO FILE EXITS @ " + path);
		}
		return file;
	}
	
	public static String readFileToString(String filePath) {
		if (filePath == null) {
			return null;
		}
		File file = getExistingFile(filePath);
		
		return readFileToString(file);
	}
	
	public static String readFileToString(File file) {
		if (file == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		FileReader fReader = null;
		BufferedReader bReader = null;
		try {
			fReader = new FileReader(file);
			bReader = new BufferedReader(fReader);
			String line = null;
			while ((line = bReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

		} catch (Exception e) {
			if (fReader != null) {
				try {
					fReader.close();
				} catch (IOException e1) {
					return null;
				}
			}
			if (bReader != null) {
				try {
					bReader.close();
				} catch (IOException e1) {
					return null;
				}
			}
		}
		return sb.toString();
	}
	
	public static List<String> readFileToStringList(String filePath) {
		if (filePath == null) {
			return Collections.emptyList();
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return Collections.emptyList();
		}
		return readFileToStringList(file);
	}
	
	public static List<String> readFileToStringList(File file) {
		if (file == null || file.exists() == false) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		
		FileReader fReader = null;
		BufferedReader bReader = null;
		
		try {
			fReader = new FileReader(file);
			bReader = new BufferedReader(fReader);
			String line = null;
			while ((line = bReader.readLine()) != null) {
				result.add(line);
			}

		} catch (Exception e) {
			if (fReader != null) {
				try {
					fReader.close();
				} catch (IOException e1) {
					return Collections.emptyList();
				}
			}
			if (bReader != null) {
				try {
					bReader.close();
				} catch (IOException e1) {
					return Collections.emptyList();
				}
			}
		}
		return result;
	}
	
	public static boolean writeStringListToFile(String file, List<String> lines, boolean append) {
		return writeStringListToFile(new File(file), lines, append);
	}
	
	public static boolean writeStringListToFile(File file, List<String> lines, boolean append) {
		String contents = StringUtil.join(lines, "\n");
		return writeStringToFile(file, contents, append);
	}
	
	public static boolean writeStringToFile(String file, String string, boolean append) {
		return writeStringToFile(new File(file), string, append);
	}
	
	public static boolean writeStringToFile(File file, String string, boolean append) {
//		if (!file.exists()) {
//			throw new Error();
//		}
		assert file != null;
		assert string != null;
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bufferedWriter.write(string);
			bufferedWriter.close();
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
	
	public static Object loadObjeceFromFile(File file) {
		return loadObjeceFromFile(file.getAbsolutePath());
	}
	
	public static Object loadObjeceFromFile(String path) {
		Object res = null;
		FileInputStream fs = null;
		ObjectInputStream os = null;
		try {
			fs = new FileInputStream(path);
			os = new ObjectInputStream(fs);
			res =  os.readObject();
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(fs, os);
		}
		return res;
	}
	
	public static void writeObjectToFile(String path, Object obj) {
		FileOutputStream totFs = null;
		ObjectOutputStream totOs =  null;
		try {
			
			totFs = new FileOutputStream(path);
			totOs =  new ObjectOutputStream(totFs);
			totOs.writeObject(obj);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			FileUtil.closeInputStream(totFs, totOs);
		}
	}
	
	public static File copyFile(File src, File dst){
		if(!src.getParentFile().exists()){
			src.getParentFile().mkdirs();
		}
		
		if(!dst.getParentFile().exists()){
			dst.getParentFile().mkdirs();
		}
		
		FileInputStream input=null;
		FileOutputStream output=null;
        try{
            input = new FileInputStream(src);
            output = new FileOutputStream(dst);
            int in=input.read();
            while(in!=-1){
                output.write(in);
                in=input.read();
            }
            output.flush();
        }catch (IOException e){
            System.out.println(e.toString());
        }  finally {
			if (input != null){
				try {
					input.close();
				} catch (IOException e){
				}
			}
			if (output != null){
				try {
					output.close();
				} catch (IOException e){
				}
			}
		}
        return dst;
	}
	
    public static File copyFile(String srcPath, String dstPath){
    	return copyFile(new File(srcPath), new File(dstPath));
    }
    
    public static void getAllFilesByType(String root, String postfix, List<File> filelist){
		if(root == null) {
			return;
		}
    	File partent = new File(root);
		if (partent.isDirectory()) {
			File[] files = partent.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (files[i].isDirectory()) {
						getAllFilesByType(files[i].getAbsolutePath(), postfix, filelist);
					} else if (fileName.endsWith(postfix)) {
						String strFileName = files[i].getAbsolutePath();
						// System.out.println("\t" + strFileName);
						filelist.add(files[i]);
					} else {
						continue;
					}
				}
			}
		} else {
			filelist.add(partent);
		}
    } 
}
