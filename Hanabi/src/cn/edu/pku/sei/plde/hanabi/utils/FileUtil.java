package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;


public final class FileUtil {
	
	public static String getFileAddressOfJava(String srcPath, String className){
		if (className.contains("$")){
			className = className.substring(0, className.lastIndexOf("$"));
        }
		if (className.contains("<") && className.contains(">")){
			className = className.substring(0, className.indexOf("<"));
		}
		return  srcPath.trim() + System.getProperty("file.separator") + className.trim().replace('.',System.getProperty("file.separator").charAt(0))+".java";
	}
	
	public static String getCodeFromFile(String srcRootPath, String classFullName) {
		String srcFilePath = getFileAddressOfJava(srcRootPath, classFullName);
		return readFileToString(srcFilePath);
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
	
	/**
	 * this method is a copy of Condition
	 * @param closeables
	 */
	public static void safeCloseCloseables(final Closeable... closeables){
		if(closeables == null) {
			return;
		}
		for(Closeable stream: closeables){
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

	private static final int CACHE_SIZE = 16; 
	private static Map<File, String> fileStrCache = new HashMap<>(CACHE_SIZE);
	
	/**
	 * Load file content from the private cache FileUtil.fileStrCache
	 * @param file
	 * @return
	 */
	public static String loadFileStrFromCache(File file) {
		if (file == null) {
			return null;
		}
		if(fileStrCache.containsKey(file)) {
			return fileStrCache.get(file);
		} else {
			String str = FileUtil.readFileToString(file);
			if(fileStrCache.size() >= CACHE_SIZE) {
				fileStrCache.clear();
			}
			fileStrCache.put(file, str);
			return str;
		}
	}
	
	public static void removeFromFileStrCache(File file) {
		if (file == null) {
			return;
		}
		if(fileStrCache.containsKey(file)) {
			fileStrCache.remove(file);
		}
	}
	
	public static String loadFileStrFromCache(String filePath) {
		if (filePath == null) {
			return null;
		}
		File file = getExistingFile(filePath);
		return loadFileStrFromCache(file);
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
			return null;
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		return readFileToStringList(file);
	}
	
	public static List<String> readFileToStringList(File file) {
		if (file == null || file.exists() == false) {
			return null;
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
		return result;
	}
	
	public static boolean writeStringToFile(String file, String string, boolean append) {
		return writeStringToFile(new File(file), string, append);
	}
	
	public static boolean writeStringToFile(File file, String string, boolean append) {
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
	
	public static boolean inputStreamToFile(InputStream ins, File file) {
		if(ins == null) {
			return false;
		}
		
		OutputStream os = null;
		try {
			File folder = file.getParentFile();
			if(!folder.exists()) {
				folder.mkdirs();
			}
			
			os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		}catch(IOException e) {
			e.printStackTrace();
		} finally {
			safeCloseCloseables(os, ins);
		}
		
		return file.exists();
	}
	
	
	/**
	 * this method is a copy of Condition
	 * @param file
	 * @return
	 */
	public static Object loadObjeceFromFile(File file) {
		return loadObjeceFromFile(file.getAbsolutePath());
	}
	
	/**
	 * this method is a copy of Condition
	 * @param path
	 * @return
	 */
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
			safeCloseCloseables(fs, os);
		}
		return res;
	}
	
	/**
	 * this method is a copy of Condition
	 * @param path
	 * @param obj
	 */
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
			FileUtil.safeCloseCloseables(totFs, totOs);
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
    
    public static void deleteFile(String path) {
    	deleteFile(new File(path));
    }
    
    public static void deleteFile(File file) {
    	if(file.exists()) {
    		try {
    			file.delete();
    		}catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void deleteDirectoryContents(final File dir) {
        if ((dir == null) || !dir.isDirectory()) {
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryContents(f);
                } else {
                    deleteFile(f);
                }
            }
        }
        
    }

    public static File openFrom(String path) {
        File file = new File(path);
        if (!file.exists()) {
            fail("File does not exist in: '" + path + "'");
        }
        return file;
    }
    
    public static URL urlFrom(String path) {
        URL url = null;
        try {
            url = openFrom(path).toURI().toURL();
        } catch (MalformedURLException e) {
            fail("Illegal name for '" + path + "' while converting to URL");
        }
        return url;
    }
    
    public static void cleanFolder(String folder){
    	cleanFolder(new File(folder));
    }
    
	public static void cleanFolder(File file) {
		if (!file.exists()) {
			return;
		}
		if(!file.isDirectory()) {
			deleteFile(file);
			return;
		}
		for (File subFile : file.listFiles()) {
			if (subFile.isDirectory()) {
				cleanFolder(subFile);
			}else {
				deleteFile(subFile);
			}
		}
	}
    
	/**
	 * Get the file list containing all the files in a the same folder
	 * @return
	 */
	public static List<File> getFilesWithinSameFolder(File file, String postFix){
		File fatherFolder = file.getParentFile();
		assert fatherFolder.isDirectory();
		
		List<File> result = new ArrayList<>();
		for(File f : fatherFolder.listFiles()) {
			String name = f.getName();
			if(name.endsWith(postFix)) {
				result.add(f);
			}
		}
		return result;
	}
	
	/**
	 * Collect all the files in the folder, the file names end with the postfix 
	 * @param dir
	 * @param result
	 */
	public static void getAllSubFilesInFolder(File dir, String postFix, List<File> result) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) {
					getAllSubFilesInFolder(files[i], postFix, result);
				} else if (fileName.endsWith(postFix)) {
					result.add(files[i]);
				}
			}
		}
	}
	
    public static void copyDir(String from, String to) {
    	File fromFile = new File(from);
    	File[] fs = fromFile.listFiles();
    	
    	File toFile = new File(to);
    	
    	if(!toFile.exists()) {
    		toFile.mkdirs();
    	}
    	for (File f : fs) {
            if(f.isFile()){
            	try {
					Files.copy(f, new File(to + "/" + f.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
            }else if(f.isDirectory()){
            	copyDir(f.getPath(), to + "/" + f.getName());
            }
        }
    }
    
    private static void fail(String message) {
        throw new IllegalArgumentException(message);
    }
}
