package cn.edu.pku.sei.plde.hanabi.fixer.pattern.ret;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.utils.CodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.PredictorUtil;
import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.dedu.extern.pf.ConstTrue;
import edu.pku.sei.conditon.util.JavaFile;

public class ConstantReplaceFixPattern extends RetStmtRelatedFixPattern{

	private final static String PATCH_COMMENT =  "/*-- PATCH RC --*/";

	private CompilationUnit cu;

	public ConstantReplaceFixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		super(projectConfig, suspect, iThSuspect, testRunner, testOutput, allFailTestOutputs);
		
		cu = getCompilationUnitFromCache(suspect.getClassName(), this.srcCode, projectConfig.getTestJdkLevel());
	}
	
	@Override
	public FixResult implement() {
		Block generatedBlk;
		try {
			generatedBlk = (Block) JavaFile.genASTFromSource(this.errLine, ASTParser.K_STATEMENTS, projectConfig.getTestJdkLevel());//BLOCK: {return xxx;}
		}catch (Exception e){
			return FixResult.IMPLEMENT_FAIL;
		}
		if(generatedBlk.statements().size() != 1) {
			return FixResult.IMPLEMENT_FAIL;
		}
		Statement errStmt = (Statement) generatedBlk.statements().get(0);
		if(errStmt instanceof ReturnStatement == false && errStmt instanceof ThrowStatement == false) {
			return FixResult.IMPLEMENT_FAIL;
		}
		List<String> patches = genPatches(errStmt);
		if(patches.isEmpty()) {
			return FixResult.IMPLEMENT_FAIL;
		}
		
		List<String> allPreds = PredictorUtil.predictIfConds(projectConfig, suspect, iThSuspect);
		if (allPreds == null || allPreds.isEmpty()) {
			logger.info(">>>> EMPTY PREDICATION");
			return FixResult.IMPLEMENT_FAIL;
		}
		
		if(!allPreds.get(0).equals(ConstTrue.CONSTANT_TRUE)) {
			return FixResult.IMPLEMENT_FAIL;			
		}
		
		List<String> plauPatches = null;
		if(EXHAUSTE_ALL_PATCH) {
			plauPatches =  new ArrayList<>();
			recordAllPlauPatchHead("ConstantReplaceFixPattern");
		}
		
		for (String patch : patches) {
			logger.info(">>>> ConstantReplaceFixPattern fixing: " + patch);
			if (processPatch(srcClsFile, patch) == PatchResult.SUCCESS) {
				if(EXHAUSTE_ALL_PATCH) {
					plauPatches.add(patch);
					recordAllPlauPatch(plauPatches);
					restoreSrcFile();
				}else {
					recordPatch(PATCH_COMMENT);
					return FixResult.FIX_SUCC;
				}
			}else{
				restoreSrcFile();
			}
		}
		if(EXHAUSTE_ALL_PATCH) {
			return leftFirstPlauPatch(plauPatches, PATCH_COMMENT);
		}else {
			return FixResult.FIX_FAIL;
		}		
	}
	
	private List<String> genPatches(Statement ret){
		List<String> patches = new ArrayList<>();
		StmtConstantVisitor stmtVisitor = new StmtConstantVisitor();
		ret.accept(stmtVisitor);
		if(stmtVisitor.returnedBool != null) {
			patches.add(getBoolPatch(stmtVisitor.returnedBool));
		}
		for(String con : stmtVisitor.constantSet) {
			String patch = "return " + con + ";";
			if(patch.equals(ret.toString().trim()) == false) {
				patches.add(patch);
			}
		}
		
		if(! patches.isEmpty()) {
			return patches;
		}
		
		ClassConstantVisitor clsVisitor = new ClassConstantVisitor();
		this.cu.accept(clsVisitor);
		for(String con : clsVisitor.constantList) {
			String patch = "return " + con + ";";
			if(patch.equals(ret.toString().trim()) == false) {
				patches.add(patch);
			}
		}
		return patches;
	}
	
	
	private String getBoolPatch(boolean oriTrue) {
		String newRet;
		if(oriTrue) {
			newRet = "return false;";
		}else {
			newRet = "return true;";
		}
		return newRet;
	}

	protected boolean compilePatch(File srcClsFile, String patch) {
		//restoreSrcFile();//TODO:: check, need this line?
		CodeUtil.replaceCodeLine(this.srcFile, this.srcCodeLines, patch, this.suspect.getLine());
		boolean res = false;
		try{
			res = CodeUtil.javac(projectConfig, srcFile, srcClsFile, null);
		}catch(Exception e){
			restoreSrcFile();
		}
		return res;
	}
	
	private class StmtConstantVisitor extends ASTVisitor{

		public Boolean returnedBool = null;
		public Set<String> constantSet = new HashSet<>(); 
		
		@Override
		public boolean visit(BooleanLiteral node) {
			returnedBool = Boolean.valueOf(node.booleanValue());
			return super.visit(node);
		}

		@Override
		public boolean visit(SimpleName node) {
			String name = node.getIdentifier();
			if(Character.isUpperCase(name.charAt(0))) {
				if(! edu.pku.sei.conditon.util.TypeUtil.isJavaLangOrJavaUtilType(name)) {
					constantSet.add(node.getIdentifier());
				}
			}
			return super.visit(node);
		}
		
	}
	
	private class ClassConstantVisitor extends ASTVisitor {
		public List<String> constantList = new ArrayList<>();

		@Override
		public boolean visit(FieldDeclaration node) {
			int mod = node.getModifiers();
			if((Modifier.isPublic(mod) && Modifier.isFinal(mod) && Modifier.isStatic(mod)) == false) {
				return super.visit(node);
			}
			
			for(Object obj : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
				String name = vdf.getName().getIdentifier();
				if(ASTLocator.maybeConstant(name) == false) {
					continue;
				}
				constantList.add(name);
				
			}
			return super.visit(node);
		} 
		
		
	}
}
