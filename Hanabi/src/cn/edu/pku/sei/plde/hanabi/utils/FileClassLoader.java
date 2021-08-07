package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class FileClassLoader extends URLClassLoader {
	
	public FileClassLoader(URLClassLoader classLoader) {
		super(classLoader.getURLs());
	}
	
	public FileClassLoader(URL[] urls) {
		super(urls);
	}

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
	
	public Class findClass(String qualifiedName, File classFile) {
		byte[] data = loadClassData(classFile);
		return defineClass(qualifiedName, data, 0, data.length);
	}

	private byte[] loadClassData(File classFile) {
		FileInputStream fis = null;
		byte[] data = null;
		try {
			fis = new FileInputStream(classFile);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int ch = 0;
			while ((ch = fis.read()) != -1) {
				baos.write(ch);
			}
			data = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}