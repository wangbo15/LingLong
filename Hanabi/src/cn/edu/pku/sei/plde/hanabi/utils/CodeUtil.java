package cn.edu.pku.sei.plde.hanabi.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.main.Config;

public class CodeUtil {

	public static boolean javac(ProjectConfig projectConfig, File srcFile, File srcClsFile, List<String> exClassPath) {
		File bkCls = new File(Config.TEMP_CLS_BACKUP_PATH + "/" + srcClsFile.getName());
		FileUtil.deleteFile(bkCls);

		FileUtil.copyFile(srcClsFile, bkCls);
		FileUtil.deleteFile(srcClsFile);
		
		String cp = projectConfig.getClassPaths();
		if(exClassPath != null && exClassPath.size() > 0){
			StringBuffer sb = new StringBuffer();
			for(String s : exClassPath){
				if(s == null) {
					continue;
				}
				sb.append(s);
				sb.append(':');
			}
			sb.append(projectConfig.getClassPaths());
			cp = sb.toString();
		}else{
			cp = projectConfig.getClassPaths();
		}
		
		String level = projectConfig.getTestJdkLevel();
		
		//TODO: maybe related to jdk level
		String xlint = "-Xlint:unchecked"; //"-Xlint:none";
		
		String compileCmd = "javac "+ xlint + " -source " + level + " -target " + level + " -cp " + cp + " -d "
				+ projectConfig.getTargetRoot().getAbsolutePath() + " " + srcFile.getAbsolutePath();
		
		CmdUtil.runCmd(compileCmd, projectConfig.getRoot(), false);
		boolean res = srcClsFile.exists();
		if(!res) {//if compile failed, restore origin class
			System.err.println("FAIL TO COMPILE: " + srcFile.getAbsolutePath());
			FileUtil.copyFile(bkCls, srcClsFile);
		}
		return res;
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
			FileUtil.safeCloseCloseables(os, bo);
		}
	}

	public static void addCodeToFile(File file, String addingCode, int targetLine) {
		addCodeToFile(file, addingCode, Arrays.asList(targetLine));
	}

	public static void addCodeToFile(File file, String addingCode, List<Integer> targetLine) {
		File newFile = new File(file.getAbsolutePath() + ".temp");
		Map<Integer, Boolean> writedMap = new HashMap<>();
		for (int line : targetLine) {
			writedMap.put(line, false);
		}
		FileOutputStream os = null;
		BufferedOutputStream bo = null;
		BufferedReader reader = null;
		try {
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			os = new FileOutputStream(newFile);
			bo = new BufferedOutputStream(os);
			reader = new BufferedReader(new FileReader(file));
			String lineString = null;
			int line = 0;
			while ((lineString = reader.readLine()) != null) {
				line++;
				if (targetLine.contains(line + 1)) {
					if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{")
							&& !lineString.contains("}")) || lineString.contains("return ")
							|| lineString.contains("throw ")) {
						bo.write(addingCode.getBytes());
						writedMap.put(line + 1, true);

					}
				}
				if (targetLine.contains(line) && !writedMap.get(line)) {
					bo.write(addingCode.getBytes());
					writedMap.put(line, true);
				}
				bo.write((lineString + "\n").getBytes());
			}
			bo.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(os, bo, reader);
		}
		if (file.delete()) {
			newFile.renameTo(file);
		}
	}
	
	public static List<String> getPackageImportFromCode(String code){
		List<String> result = new ArrayList<>();
		for (String line: code.split("\n")){
			if (line.startsWith("import")){
				result.add(line);
			}
		}
		return result;
	}
	
    public static String getClassNameFromPackage(String packageName){
        String name = packageName.substring(packageName.lastIndexOf(".") + 1);
        if (name.endsWith(";")){
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
	
    public static String getClassNameOfImportClass(String code, String className){
        List<String> packages = getPackageImportFromCode(code);
        for (String packageName: packages){
            if (getClassNameFromPackage(packageName).equals(className)){
                if (packageName.startsWith("import ")){
                    packageName = packageName.substring(packageName.indexOf(" "));
                }
                if (packageName.endsWith(";")){
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                return packageName.trim();
            }
        }
        return "";
    }
    
    private static List<MethodDeclaration> getAllMethod(String code, boolean getInnerClassMethod){
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        if (unit.types().size() == 0){
            return new ArrayList<>();
        }
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        methodDeclarations.addAll(Arrays.asList(declaration.getMethods()));
        if (getInnerClassMethod){
            for (TypeDeclaration typeDeclaration: declaration.getTypes()){
                methodDeclarations.addAll(Arrays.asList(typeDeclaration.getMethods()));
            }
        }
        return methodDeclarations;
    }
    
    private static List<MethodDeclaration> getMethod(String code, String methodName) {
        methodName = methodName.trim();
        List<MethodDeclaration> result = new ArrayList<>();
        for (MethodDeclaration method : getAllMethod(code, true)) {
            if (method.getName().getIdentifier().equals(methodName)) {
                result.add(method);
            }
        }
        return result;
    }
    
    private static List<String> getMethodStrings(String code, String methodName){
        List<String> result = new ArrayList<>();
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            result.add(method.toString());
        }
        return result;
    }
    
    public static List<Integer> getMethodParamsCountInCode(String code, String methodName) {
        List<Integer> result = new ArrayList<>();
        List<String> methodCodes = getMethodStrings(code, methodName);
        for (String methodCode: methodCodes){
            result.add(getMethodParamsFromDefine(methodCode, methodName).size());
        }
        return result;
    }
    
    private static Map<String, String> getMethodParamsFromDefine(String methodCode, String methodName){
        Map<String, String> result = new HashMap<>();
        for (String line: methodCode.split("\n")){
            if (line.contains(" "+methodName+"(")){
                List<String> params = Arrays.asList(line.substring(line.indexOf("(")+1, line.lastIndexOf(")")).split(","));
                for (String param: params){
                    param = param.trim();
                    if (!param.contains(" ")){
                        continue;
                    }
                    result.put(param.split(" ")[1], param.split(" ")[0]);
                }
                break;
            }
        }
        return result;
    }
}
