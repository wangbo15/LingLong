package edu.pku.sei.conditon.dedu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.pku.sei.conditon.auxiliary.StatisticVisitor;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.proj.ProInfo;

public class DeduFeatureGenerator {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	private static Pair<String, CompilationUnit> cuCache;
	
	private static CompilationUnit getCU(String subjectSrcPath, File javaSrcFile) {
		
		if(cuCache != null && cuCache.getFirst().equals(javaSrcFile.getAbsolutePath())) {
			return cuCache.getSecond();
		}
		
		byte[] input = null;
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaSrcFile));
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

		parser.setResolveBindings(true);

		char[] rawSource = new String(input).toCharArray();

		parser.setSource(rawSource);

		parser.setStatementsRecovery(true);

		parser.setBindingsRecovery(true);

		parser.setIgnoreMethodBodies(false);

		parser.setCompilerOptions(JavaCore.getOptions());

		String fileName = javaSrcFile.getName();

		parser.setUnitName(fileName);

		String absPath = javaSrcFile.getParentFile().getAbsolutePath();
		String[] sources = { subjectSrcPath, absPath };

		String javaHome = System.getProperty("java.home");

		String[] classpath = { javaHome + "/lib/rt.rar" };

		parser.setEnvironment(classpath, sources, new String[] { "UTF-8", "UTF-8" }, true);

		CompilationUnit cu = (CompilationUnit) (parser.createAST(null));
		
		cuCache = new Pair<String, CompilationUnit>(javaSrcFile.getAbsolutePath(), cu);
		
//		System.out.println("NEW CompilationUnit");
		
		return cu;
	}
	
	private static DeduLineToFeatureVisitor visitorBuffer = null;
		
	
	private static DeduLineToFeatureVisitor getVisitor(String sid, String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line) {
				
		String absPath = subjectSrcPath + "/" + relativePath;
		
		if(visitorBuffer != null && visitorBuffer.getFile().getAbsolutePath().equals(absPath) && visitorBuffer.getLine() == line){
			return visitorBuffer;
		}
		
		File javaSrcFile = new File(absPath);

		CompilationUnit compilationUnit = getCU(subjectSrcPath, javaSrcFile);

		currProjAndBug = currProjAndBug.toLowerCase();
		
		ProInfo proInfo = loadProInfo(currProjAndBug, subjectSrcPath, subjectTestPath);
		
		StatisticVisitor statisticVisitor = new StatisticVisitor(compilationUnit, javaSrcFile, proInfo);
		compilationUnit.accept(statisticVisitor);
		
		StatisticVisitor.loadTotalIfCondTimeMap(baseProjAndBug, subjectSrcPath, subjectTestPath);
		
		
		DeduLineToFeatureVisitor visitor = new DeduLineToFeatureVisitor(compilationUnit, javaSrcFile, proInfo, line, sid);

		compilationUnit.accept(visitor);
		
		visitorBuffer = visitor;

		return visitor;
	}
	
	public static List<VariableInfo> getAllVariables(){
		assert visitorBuffer != null;
		return visitorBuffer.getVarInfos();
	}
	
	public static Map<String, VariableInfo> getAllVariablesMap(){
		assert visitorBuffer != null;
		Map<String, VariableInfo> result = new HashMap<>();
		for(VariableInfo info : visitorBuffer.getVarInfos()){
			String fldSign = "";
			if(info.getVariableFeature().isField()){//same logic with DeduLineToFeatureVisitor.generateRes()
				if(!StatisticVisitor.totalIfCondTimeMap.containsKey(info.getNameLiteral())){
					if(!CONFIG.isPredAll()) {
						continue;
					}
				}
				fldSign = "#F";
			}
			
			result.put(info.getNameLiteral() + fldSign, info);
		}
		return result;
	}
	
	public static ASTNode getHitNode(String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line, String sid){
		return getVisitor(sid, currProjAndBug, baseProjAndBug, subjectSrcPath, subjectTestPath, relativePath, line).getHitNode();
	}
	
	public static ASTNode getHitNode(String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line){
		return getVisitor("0", currProjAndBug, baseProjAndBug, subjectSrcPath, subjectTestPath, relativePath, line).getHitNode();
	}
	
	public static String generateContextFeature(String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line) {
		return getVisitor("0", currProjAndBug, baseProjAndBug, subjectSrcPath, subjectTestPath, relativePath, line).getContextResult();
	}

	/**
	 * @param currProjAndBug
	 * @param baseProjAndBug
	 * @param subjectSrcPath
	 * @param subjectTestPath
	 * @param relativePath
	 * @param line
	 * @return varname -> ctx feature + var feature
	 */
	public static Map<String, String> getVarToCtxAndVarFeaPrefixMap(String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line) {
		return getVisitor("0", currProjAndBug, baseProjAndBug, subjectSrcPath, subjectTestPath, relativePath, line).getVarToCtxAndVarFeaPrefix();
	}
	
	/**
	 * @param currProjAndBug
	 * @param baseProjAndBug
	 * @param subjectSrcPath
	 * @param subjectTestPath
	 * @param relativePath
	 * @param line
	 * @return varname -> var feature only
	 */
	public static Map<String, String> getVarToVarFeatureMap(String currProjAndBug, String baseProjAndBug, String subjectSrcPath, String subjectTestPath, String relativePath, int line) {
		String sid = "0";
		return getVisitor(sid, currProjAndBug, baseProjAndBug, subjectSrcPath, subjectTestPath, relativePath, line).getVarToVarFea();
	}
	
	public static ProInfo proInfoTmp;
	
	private static ProInfo loadProInfo(String bugName, String srcRoot, String testRoot){
		ProInfo proInfo = null;
		String path = DeduMain.OUTPUT_ROOT + bugName + ".pro";
		File file = new File(path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(!file.exists()){
			System.out.println("NO EXSITING PROINFO, GENERATTE FOR " + bugName);
			DeduMain.setAndSaveProjInfo(bugName, srcRoot, testRoot);
		}
				
		try {
			FileInputStream fs = new FileInputStream(path);
			ObjectInputStream os = new ObjectInputStream(fs);
			
//			ObjectStreamClass.getSerialVersionUID(clazz);
			
			proInfo = (ProInfo) os.readObject();
			fs.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		//BUG!! math-15's pkg is `org.apache.commons.math3.util.FastMath`, while math-37's is `org.apache.commons.math.util.FastMath`
		//assert proInfo.getProjectRepre().fullNameToClazzesMap.get("org.apache.commons.math.util.FastMath") != null;
		
		proInfoTmp = proInfo;
		
		return proInfo;
	}

}
