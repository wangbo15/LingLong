package cn.edu.pku.sei.plde.hanabi.fl.asm.instru;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import edu.pku.sei.proj.ProInfo;

public class Instrumenter {
	
	public static void instrumentSrcClass(File clsFile) {
		assert clsFile.exists() && clsFile.getName().endsWith(".class"): clsFile.getAbsolutePath();
		
		CoverageInstrumenter.instrument(clsFile);
	}
	
	public static void instrumentTestClass(File clsFile, boolean isJunit3) {
		assert clsFile.exists() && clsFile.getName().endsWith(".class"): clsFile.getAbsolutePath();
		
		TestCaseInstrumenter.instrument(clsFile, isJunit3);
	}
	
	
	protected static void rewrite(File classFile, byte[] bytes) {
		OutputStream output = null;
		try {
			output = new FileOutputStream(classFile);
			output.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(output);
		}
	}
}
