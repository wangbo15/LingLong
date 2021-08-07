package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Stack;

import cn.edu.pku.sei.plde.hanabi.main.Config;

public class ClassPathHacker {

	private static final URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	
	private static final Class<?> urlClass = URLClassLoader.class;
	
	private static final Class<?>[] parameters = new Class[] { URL.class };

	public static boolean isByteFile(String s) {
		return s.endsWith(".class") || s.endsWith(".jar") || s.endsWith(".zip");
	}
	
	public static boolean isByteFile(File f) {
		return isByteFile(f.getName());
	}
	
	public static void addFile(String path) throws IOException {	
		File f = new File(path);
		addFile(f);
	}

	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}
	
	public static void addURL(URL u) throws IOException {
		try {
			Method method = urlClass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysLoader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		} 

	}
	
	public static void removeFile(String path) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
		File f = new File(path);
		removeFile(f);
	}
	
	public static void removeFile(File f) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
		removeURL(f.toURI().toURL());
	}
	
	public static void removeURL(URL u) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		
		Field ucpField = urlClass.getDeclaredField("ucp");
		ucpField.setAccessible(true);
		
		Class<?> urlClasspathClass = sysLoader.loadClass("sun.misc.URLClassPath");
		
		//ucp type: URLClassPath, where is the jar file?
		Object ucp = ucpField.get(sysLoader); 
		Field urlsField = urlClasspathClass.getDeclaredField("urls");
		urlsField.setAccessible(true);
		Stack<?> urls = (Stack<?>) urlsField.get(ucp);
		
		urls.remove(u);
	}
	
	public static void removeClassPaths(String classPaths) {
		for(String target: classPaths.split(":")) {
			if(target.contains("/.m2/") || target.contains(Config.BUGS_DOT_JAR_ROOT)) {
				
				try {
					removeFile(target);
				} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
						| IllegalAccessException | MalformedURLException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
	public static void loadClassPaths(String classPaths){
		for(String target: classPaths.split(":")) {
			// skip already loaded paths
			// TODO: filter by System.getProperty("java.class.path")
			if(target.equals(".") 
					|| target.contains("/Hanabi/") 
					|| target.contains("/junit/")
					|| target.contains("/log4j/")) {
				
				continue;
			}
			
			try {
				addFile(target);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
