package edu.pku.sei.conditon.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class JavaFile {

	/**
	 * generate {@code CompilationUnit} from {@code ICompilationUnit}
	 * 
	 * @param icu
	 * @return
	 */
	public static CompilationUnit genASTFromICU(ICompilationUnit icu) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		return (CompilationUnit) astParser.createAST(null);
	}

	/**
	 * generate {@code CompilationUnit} from source code based on the specific
	 * type (e.g., {@code ASTParser.K_COMPILATION_UNIT})
	 * 
	 * @param icu
	 * @param type
	 * @return
	 */
	public static ASTNode genASTFromSourceAsJava7(String icu, int type) {
		return genASTFromSource(icu, type, JavaCore.VERSION_1_7);
	}
	
	public static ASTNode genASTFromSource(String icu, int type, String javaVer) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVer, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		return astParser.createAST(null);
	}
	
	public static ASTNode genASTFromSourceWithType(String icu, int type, String filePath, String javaVer, String srcRoot) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVer, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		astParser.setEnvironment(getClassPath(), new String[] {srcRoot}, null, true);
		astParser.setUnitName(filePath);
		astParser.setBindingsRecovery(true);
		try {
			return astParser.createAST(null);
		}catch(IllegalStateException e) {
			e.printStackTrace();
			System.err.println("\nERR_FILE: " + srcRoot + filePath + "\n");
		}
		throw new Error();
	}
	
	
	private static String[] getClassPath() {
		String property = System.getProperty("java.class.path", ".");
		return property.split(File.pathSeparator);
	}
	
	public static CompilationUnit genCompilationUnit(File javaSrcFile, String javaVersion, String subjectSrcPath) {
		byte[] input = null;
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					new FileInputStream(javaSrcFile));
			input = new byte[bufferedInputStream.available()];
			bufferedInputStream.read(input);
			bufferedInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVersion, options);
		parser.setCompilerOptions(options);
		
		parser.setResolveBindings(true);
		
		char[] rawSource = new String(input).toCharArray();
		
		parser.setSource(rawSource);
		
		parser.setStatementsRecovery(true);
		
		parser.setBindingsRecovery(true);

		parser.setIgnoreMethodBodies(false);

		parser.setCompilerOptions(JavaCore.getOptions());
		
		String fileName = javaSrcFile.getName();
		
		//TODO:: seems set a unique name is all right
		parser.setUnitName(fileName);
		
		//TODO:: it seems that setting the absPath does not work
		String absPath = javaSrcFile.getParentFile().getAbsolutePath();
		String[] sources = {subjectSrcPath, absPath};
		
		String javaHome = System.getProperty("java.home");

		String[] classpath = {javaHome + "/lib/rt.rar"};
		
		parser.setEnvironment(classpath, sources, new String[]{"UTF-8", "UTF-8"}, true);
		
		//logger.info("PARSING: " + javaSrcFile.getAbsolutePath());
		
		CompilationUnit cu = (CompilationUnit) (parser.createAST(null));
		return cu;
	}
	
	
	/**
	 * write {@code string} into file with mode as "not append"
	 * 
	 * @param filePath
	 *            : path of file
	 * @param string
	 *            : message
	 * @return
	 */
	public static boolean writeStringToFile(String filePath, String string) {
		return writeStringToFile(filePath, string, false);
	}

	/**
	 * write {@code string} to file with mode as "not append"
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @param string
	 *            : message
	 * @return
	 */
	public static boolean writeStringToFile(File file, String string) {
		return writeStringToFile(file, string, false);
	}

	/**
	 * write {@code string} into file with specific mode
	 * 
	 * @param filePath
	 *            : file path
	 * @param string
	 *            : message
	 * @param append
	 *            : writing mode
	 * @return
	 */
	public static boolean writeStringToFile(String filePath, String string, boolean append) {
		if (filePath == null) {
			return false;
		}
		File file = new File(filePath);
		return writeStringToFile(file, string, append);
	}

	/**
	 * write {@code string} into file with specific mode
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @param string
	 *            : message
	 * @param append
	 *            : writing mode
	 * @return
	 */
	public static boolean writeStringToFile(File file, String string, boolean append) {
		if (file == null || string == null) {
			return false;
		}
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8"));
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

	public static List<String> readFileToStringList(File file) {
		if (file == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		BufferedReader bReader = null;
		FileReader fReader = null;
		try {
			fReader = new FileReader(file);
			bReader = new BufferedReader(fReader);
			String line = null;
			while ((line = bReader.readLine()) != null) {
				result.add(line);
			}
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(bReader != null){
				try {
					bReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fReader != null){
				try {
					fReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * read string from file
	 * 
	 * @param filePath
	 *            : file path
	 * @return : string in the file
	 */
	public static String readFileToString(String filePath) {
		if (filePath == null) {
			return "";
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return new String();
		}
		return readFileToString(file);
	}

	/**
	 * read string from file
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @return : string in the file
	 */
	public static String readFileToString(File file) {
		if (file == null) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		InputStream in = null;
		InputStreamReader inputStreamReader = null;
		try {
			in = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(in, "UTF-8");
			char[] ch = new char[1024];
			int readCount = 0;
			while ((readCount = inputStreamReader.read(ch)) != -1) {
				stringBuffer.append(ch, 0, readCount);
			}
			inputStreamReader.close();
			in.close();

		} catch (Exception e) {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e1) {
					return new String();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					return new String();
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * iteratively search files with the root as {@code file}
	 * 
	 * @param file
	 *            : root file of type {@code File}
	 * @param fileList
	 *            : list to save all the files
	 * @return : a list of all files
	 */
	public static List<File> ergodic(File file, List<File> fileList) {
		if (file == null) {
			return fileList;
		}
		File[] files = file.listFiles();
		if (files == null)
			return fileList;
		for (File f : files) {
			if (f.isDirectory()) {
				ergodic(f, fileList);
			} else if (f.getName().endsWith(".java"))
				fileList.add(f);
		}
		return fileList;
	}

	/**
	 * iteratively search the file in the given {@code directory}
	 * 
	 * @param directory
	 *            : root directory
	 * @param fileList
	 *            : list of file
	 * @return : a list of file
	 */
	public static List<String> ergodic(String directory, List<String> fileList) {
		if (directory == null) {
			return fileList;
		}
		File file = new File(directory);
		File[] files = file.listFiles();
		if (files == null)
			return fileList;
		for (File f : files) {
			if (f.isDirectory()) {
				ergodic(f.getAbsolutePath(), fileList);
			} else if (f.getName().endsWith(".java"))
				fileList.add(f.getAbsolutePath());
		}
		return fileList;
	}

	
    public static String simpleClassName(String qualifiedClassName) {
        String simpleClassName = qualifiedClassName;
        int lastPackageSeparator = qualifiedClassName.lastIndexOf('.');
        if (lastPackageSeparator > -1) {
            simpleClassName = qualifiedClassName.substring(lastPackageSeparator + 1);
        }
        int lastNestingSeparator = simpleClassName.lastIndexOf('$');
        if (lastNestingSeparator > -1) {
            simpleClassName = simpleClassName.substring(lastNestingSeparator + 1);
        }
        return simpleClassName;
    }
    

    public static String packageName(String qualifiedClassName) {
        int lastPackageSeparator = qualifiedClassName.lastIndexOf('.');
        if (lastPackageSeparator > -1) {
            return qualifiedClassName.substring(0, lastPackageSeparator);
        }
        return "";
    }
    
    public static String asClasspath(URL[] urls) {
        Collection<String> paths = new LinkedList<String>();
        for (URL url : urls) {
            paths.add(url.getPath());
        }
        return StringUtil.join(paths, ":");
    }
    
	public static void replaceCodeLine(File file, String[] oriLines, String replacement, int line) {
		assert oriLines != null;
		FileOutputStream os = null;
		BufferedOutputStream bo = null;
		try {
			os = new FileOutputStream(file, false);
			bo = new BufferedOutputStream(os);
			
			for(int i = 0; i < oriLines.length; i++) {
				if(i != line - 1) {
					bo.write(oriLines[i].getBytes());
				}else {
					bo.write(replacement.getBytes());
				}
				bo.write("\n".getBytes());
			}
			bo.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
				bo.close();
			} catch (IOException e) {
			}
		}
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
	
}