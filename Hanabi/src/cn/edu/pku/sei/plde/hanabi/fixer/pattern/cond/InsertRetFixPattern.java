package cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.trace.Tracer;
import cn.edu.pku.sei.plde.hanabi.utils.CodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JavaCodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;
import cn.edu.pku.sei.plde.hanabi.utils.PredictorUtil;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.FieldRepre;

public class InsertRetFixPattern extends ConditionRelatedFixPattern {
	
	private final static String PATCH_COMMENT =  "/*-- PATCH P1 --*/";
	
	private final static int MAX_TRIED_TIME = 200;
	
	private List<String> ifBodies = null;
	private Map<String, String> ifBodyMapMtdCode;
	
	private String[] testCodeLines;
	private CompilationUnit testCu;
		
	public InsertRetFixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		super(projectConfig, suspect, iThSuspect, testRunner, testOutput, allFailTestOutputs);
		
		this.testCodeLines = testCode.split("\n");
		testCu = (CompilationUnit) JdtUtil.genASTFromSource(this.testCode, projectConfig.getTestJdkLevel(),
				ASTParser.K_COMPILATION_UNIT);
		this.ifBodies = getIfBody();
	}

	@Override
	public FixResult implement() {
		if(canApplyPattern() == false) {
			return FixResult.IMPLEMENT_FAIL;
		}
		
		if(!legalIfBody()) {
			return FixResult.IMPLEMENT_FAIL;
		}
		
		if(processDirRlp()) {
			return FixResult.FIX_SUCC;
		}
		
		//get exception variable
		Tracer tracer = new Tracer(projectConfig, testRunner, testOutput, suspect, srcFile, srcCode, srcCodeLines, srcClsFile);
		Map<String, Set<String>> exceptionValues = tracer.getExceptionValue();
		if(exceptionValues.isEmpty()) {
			return FixResult.IMPLEMENT_FAIL;
		}
		
		Set<String> exceVars = getExceptionVariables(exceptionValues);
		
		List<String> allPreds = PredictorUtil.predictIfConds(projectConfig, suspect, iThSuspect);
		if (allPreds == null || allPreds.isEmpty()) {
			logger.info(">>>> EMPTY PREDICATION");
			return FixResult.IMPLEMENT_FAIL;
		}
		
		//restore
		List<String> patches = getPatches(exceVars, allPreds);
		
		List<String> plauPatches = null;
		
		if(EXHAUSTE_ALL_PATCH) {
			plauPatches = new ArrayList<>();
			recordAllPlauPatchHead("InsertRetFixPattern");
		}
		
		//TODO: extract to function with ModifiyIfCondFixPattern 
		int tried = 0;
		final int MAX_TRIED_PREDS_NUM = MAX_TRIED_TIME * ifBodies.size();
		// try each patch
		for (String patch : patches) {
			tried++;
			if (tried > MAX_TRIED_PREDS_NUM){
				break;
			}
			
			logger.info(">>>> InsertRetFixPattern fixing: " + patch);
			PatchResult patchRes = processPatch(srcClsFile, patch);
			if (patchRes == PatchResult.SUCCESS) {
				if(EXHAUSTE_ALL_PATCH) {
					plauPatches.add(patch);
					recordAllPlauPatch(plauPatches);
					restoreSrcFile();
				}else {
					recordPatch(PATCH_COMMENT);
					return FixResult.FIX_SUCC;
				}
			} else{
				restoreSrcFile();
			}
		}

		// restore
		restoreSrcFile();
		
		if(EXHAUSTE_ALL_PATCH) {
			return leftFirstPlauPatch(plauPatches, PATCH_COMMENT);
		}else {
			return FixResult.FIX_FAIL;
		}
		
	}

	private Set<String> getExceptionVariables(Map<String, Set<String>> exceptionValues) {
		Set<String> result = new HashSet<>();
		for(String key : exceptionValues.keySet()) {
			if(key.startsWith("this.")) {
				String subStr = key.substring("this.".length());
				result.add(subStr);
			}else if(key.endsWith(".length()")){
				String subStr = key.substring(0, key.length() - ".length()".length());
				result.add(subStr);
			}else {
				result.add(key);
			}
		}
		
		return result;
	}

	private boolean tracableErrorTestOutput() {
		if(this.testOutput.getFailMessage().startsWith("java.lang.ClassCastException:")) {
			return false;
		}
		return true;
	}

	private boolean canApplyPattern() {
		if (this.ifBodies == null || this.ifBodies.isEmpty()) {// no if body extracted 
			return false;
		}
		
		String errLineTmp = this.errLine.replaceAll(" ", "");
		if(errLineTmp.startsWith("}elseif(")) {
			return false;
		}
		if(errLineTmp.startsWith("if")) {
			int start = suspect.getLine() - 1;
			while(true) {//omit comments and blank lines
				start++;
				String hitLine = srcCodeLines[start].trim();
				if(hitLine.length() == 0 || hitLine.startsWith("//")) {
					continue;
				}else {
					break;
				}
			}
			String fst = srcCodeLines[start].trim();
			String snd = srcCodeLines[start + 1].trim();
			if(fst.startsWith("return ") && snd.equals("}")) {
				Suspect newSus = this.suspect.clone(start);
				this.suspect = newSus;
				this.errLine = fst;
				return true;
			}else {
				return false;
			}
		}
		return true;
	}

	protected boolean compilePatch(File srcClsFile, String patch){
		if(patch.startsWith("if(false)")) {
			CodeUtil.addCodeToFile(srcFile, patch, suspect.getLine());
			appendTestMtdCode(patch);
		}else if(patch.startsWith("if")) {
			int intsertLine = suspect.getLine();
			try {
				if(this.errLine.endsWith(";")) {
					ASTNode currNode = JavaFile.genASTFromSource(this.errLine, ASTParser.K_STATEMENTS, projectConfig.getTestJdkLevel());
					if(currNode != null && currNode instanceof Block) {
						Block block = (Block) currNode;
						if(block.statements().size() == 1) {
							Statement stmt = (Statement) block.statements().get(0);
							if(stmt instanceof VariableDeclarationStatement) {
								final Set<String> initialVal = new HashSet<>();
								VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
								for(Object obj : vds.fragments()) {
									VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
									initialVal.add(frag.getName().getIdentifier());
								}
								String oriexpr = patch.substring(3, patch.indexOf("{") - 1); 
								Expression cond = (Expression) JavaFile.genASTFromSource(oriexpr, ASTParser.K_EXPRESSION, projectConfig.getTestJdkLevel());
								class SplVisitor extends ASTVisitor{
									boolean hit = false;
									@Override
									public boolean visit(SimpleName node) {
										if(initialVal.contains(node.getIdentifier())) {
											hit = true;
										}
										return super.visit(node);
									}
								};
								SplVisitor visitor = new SplVisitor();
								cond.accept(visitor);
								if(visitor.hit) {
									intsertLine++;
								}
							}
						}
					}
					
				}
			}catch(Exception e) {
				//e.printStackTrace();
			}
			CodeUtil.addCodeToFile(srcFile, patch, intsertLine);
			
			appendTestMtdCode(patch);
		}else {
			CodeUtil.replaceCodeLine(srcFile, srcCodeLines, patch, this.suspect.getLine());
		}
		boolean res = false;
		try{
			res = CodeUtil.javac(projectConfig, srcFile, srcClsFile, null);
		}catch(Exception e){
			restoreSrcFile();
		}
		return res;
	}

	private List<String> getCheckPatch() {
		List<String> checkPatches = new ArrayList<>();
		for(String body : this.ifBodies) {
			if(this.ifBodies != null){
				checkPatches.add( "if(false){" + body + "}");
			}
		}
		return checkPatches;
	}
	
	private void appendTestMtdCode(String patch) {
		String ifBody = patch.substring(patch.indexOf('{') + 1, patch.indexOf('}'));
		if(this.ifBodyMapMtdCode != null && this.ifBodyMapMtdCode.containsKey(ifBody)) {
			String patchMtd = this.ifBodyMapMtdCode.get(ifBody);
			patchMtd = "\n/**\n* P1_PATCH_METHOD\n*/\n" + patchMtd;
			int lastLine = this.srcCodeLines.length;
			CodeUtil.addCodeToFile(srcFile, patchMtd, lastLine);
		}
	}
	
	private boolean legalIfBody() {
		if(this.ifBodies.isEmpty()) {
			return true;
		}
		if(this.ifBodies.size() == 1 && this.ifBodyMapMtdCode == null) {
			return true;
		}
		
		//if there are more than 1 patches, check whether the if-body is legal
		List<String> checkPatches = getCheckPatch();
		if(checkPatches.isEmpty()){
			return false;
		}
		List<String> illegalPatches = new ArrayList<>();
		for(String checkPatch : checkPatches) {
			if (!compilePatch(srcClsFile, checkPatch)) {
				restoreSrcFile();
				illegalPatches.add(checkPatch);
			}
		}
		//remove illegal if-bodies
		checkPatches.removeAll(illegalPatches);
		
		if (checkPatches.isEmpty()) {//there are no legal patches
			restoreSrcFile();
			return false;
		}
		restoreSrcFile();
		return true;
	}
	
	/**
	 * filter pradicate by exception values 
	 * @param exceptionValues
	 * @param pradicate
	 * @return
	 */
	private boolean predContainsExceptionValue(Set<String> exceptionValues, String pradicate) {
		for(String val : exceptionValues) {
			//TODO for instanceof cases
			
			if(val.endsWith("!=null")) {
				
				int idx = val.length() - "!=null".length();
				String realVal = val.substring(0, idx);
				if(pradicate.contains(realVal) && pradicate.endsWith("null")) {
					return true;
				}
				
			} else if(pradicate.contains(val)) {
				return true;
			}
		}
		return false;
	}
	
	private List<String> getPatches(Set<String> exceptionValues, List<String> allPredicates) {
		
		if(allPredicates == null || allPredicates.isEmpty()) {
			return Collections.<String>emptyList();
		}
		
		List<String> result = new ArrayList<>();
		
		assert this.ifBodies != null;
		
		for (String pred : allPredicates) {
			
			if(pred.contains(" != ")) {
				continue;
			}
			
			if("java.lang.NullPointerException".equals(testOutput.getExceptionName())) {
				if(!pred.contains("== null") && !pred.contains("!= null")) {
					continue;
				}
			}

			if(exceptionValues.isEmpty() == false && predContainsExceptionValue(exceptionValues, pred) == false) {
				continue;
			}
//			if (prad.contains("<=")) {
//				prad = prad.replaceAll("<=", "<");
//			}
//			if (prad.contains(">=")) {
//				prad = prad.replaceAll(">=", ">");
//			}
			
			pred = pred.replaceAll("(10|1\\.0|1)(e|E)-\\d+\\b", "1e-20");
			
			for(String body: this.ifBodies) {
				StringBuffer sb = new StringBuffer();
				if(pred.contains(" instanceof ")) {
					sb.append("if(!(");
					sb.append(pred);
					sb.append(")){");
					sb.append(body);
					sb.append("}");
					result.add(sb.toString());
				}else {
					sb.append("if(");
					sb.append(pred);
					sb.append("){");
					sb.append(body);
					sb.append("}");
					result.add(sb.toString());
					if(isSimpleVar(pred)) {
						StringBuffer sb1 = new StringBuffer();
						sb1.append("if(!");
						sb1.append(pred);
						sb1.append("){");
						sb1.append(body);
						sb1.append("}");
						result.add(sb1.toString());
					}
				}
				
			}
		}
		
		return result;
	}
	
	private void getRetBodyByFailedLine(String failedCodeLine, String mtdRetTp, List<String> bodyies) {
		if(failedCodeLine == null || failedCodeLine.trim().length() <= 1) {
			return;
		}
		
		Statement errStmt = null;
		try {
			errStmt = JdtUtil.genASTFromStmt(errLine, projectConfig.getTestJdkLevel());
		} catch(Exception e) {
			
		}
		
		if (failedCodeLine.contains("assertEquals") || failedCodeLine.contains("assertSame")) {
			for(String bd : processAssert(failedCodeLine)) {
				Expression bdExpr = null;
				try {
					bdExpr = JdtUtil.genASTFromExpression(bd, projectConfig.getTestJdkLevel());
				} catch(Exception e) {
					
				}
				
				if(!TypeUtil.isSimpleNum(bd) && !TypeUtil.isStrLiteral(bd)) {
					if(errStmt instanceof ReturnStatement && bdExpr instanceof QualifiedName) {
						if(((ReturnStatement) errStmt).getExpression() instanceof MethodInvocation) {
							continue;
						}
					}
					
					String rtn = "return " + bd + ";" ;
					
					String[] res = getInvockedMtdCodeAndParam(bd).split(">>>");
					if(res.length == 2) {
						String mtdCode = res[0];
						String mtdPara = res[1];
						if(!mtdCode.equals("") ) {
							if(this.ifBodyMapMtdCode == null) {
								this.ifBodyMapMtdCode = new HashMap<>();
							}
							rtn = "return patch_method(" + mtdPara + ");" ;
							this.ifBodyMapMtdCode.put(rtn, mtdCode);
						}
					}
					bodyies.add(rtn);
				}
			}//end for
		}
		else if (failedCodeLine.contains("assertNull")){
            bodyies.add("return null;");
        }
        else if (failedCodeLine.contains("assertFalse")){
        	bodyies.add("return false;");
        }
		
		if(bodyies.isEmpty() && this.suspect.getClassName().endsWith(mtdRetTp)) {
			
			ClassRepre clsRepre = projectConfig.getProInfo().getProjectRepre().fullNameToClazzesMap.get(suspect.getClassName());
			if(clsRepre == null || clsRepre.getFields() == null) {
				return;
			}
			for(FieldRepre fld: clsRepre.getFields()) {
				if(fld.isConstant() 
						&& clsRepre.getName().endsWith(fld.getType()) 
						&& failedCodeLine.contains(fld.getName())) {
					String rtn = "return " + fld.getName() + ";" ;
					bodyies.add(rtn);
				}
			}
		}
		
	}
	
	
	/**
	 * ifbodies can have return statments
	 * @return true if the defect method is not a constructor or void return-type.
	 */
	private String getRetType() {
		MethodDeclaration suspectMtd = JdtUtil.getMtdByLine(srcFile.getAbsolutePath(), projectConfig.getTestJdkLevel(),suspect.getLine());
		return JavaCodeUtil.getMethodRetType(suspectMtd);
	}

	
	private List<String> getIfBody() {
		List<String> ifBodies = new ArrayList<>();
		String failedLine = null;
		//TODO:: error line in loop or not
		String mtdRetType = getRetType();
		
		if(returnBodiesAreLegal(mtdRetType)) {
			failedLine = getAssertLineFromTestSrc();
			getRetBodyByFailedLine(failedLine, mtdRetType, ifBodies);
		}
		
		MethodDeclaration testMtd = null;
		if(testOutput.getFailAssertLine() > 0) {
			testMtd = JdtUtil.getMtdByLine(testSrcFile.getAbsolutePath(), projectConfig.getTestJdkLevel(), testOutput.getFailAssertLine());
		}else {
			testMtd = JdtUtil.getMtdByName(testSrcFile.getAbsolutePath(), projectConfig.getTestJdkLevel(), testOutput.getFailTestMethod());
		}
		if(testMtd != null) {
			String excpCls = getExpectedException(testMtd);
			if(excpCls != null) {
				String extp = excpCls.replace(".class", "").trim();
				if (extp != null) {
					String naiveThrowStmt = "throw new " + extp + "();";
					String parsedThrowStmt = parseThrowException(naiveThrowStmt);
					ifBodies.add(parsedThrowStmt);
				}
			}else {
				Set<String> exceps = getCatchedException(testMtd, failedLine);
				if(exceps != null && exceps.isEmpty() == false) {
					for(String exct : exceps) {
						String naiveThrowStmt = "throw new " + exct + "();";
						String parsedThrowStmt = parseThrowException(naiveThrowStmt);
						ifBodies.add(parsedThrowStmt);
					}
				}
			}
		}
		return ifBodies;
	}

	private boolean returnBodiesAreLegal(String retTp) {
		if("".equals(retTp) || "void".equals(retTp)) {
			return false;
		}else {
			return true;
		}
	}

	private List<String> processAssert(String failedLine) {
		
		List<String> expected = new ArrayList<>();
		
		List<String> args = getAssertParams(failedLine);

		if (failedLine.contains("assertEquals")) {// assertEquals([String message,]Object expected,Object actual);
			if (args.size() == 4) {
				expected.add(args.get(1));
			} else {
				expected.add(args.get(0));
				expected.add(args.get(1));
//				String testedCls = this.suspect.getClassName();
//				if(testedCls.contains(".")) {
//					int last = testedCls.lastIndexOf(".");
//					testedCls = testedCls.substring(last + 1);
//				}
//				for(int i = 0; i < 2; i++) {
//					String curArg = args.get(i);
//					if(!curArg.startsWith(testedCls) || curArg.endsWith(")")) {
//						expected.add(args.get(i));
//					}
//				}
			}

		} else if (failedLine.contains("assertNull")) {
			expected.add("null");
		}

		return expected;
	}

	private Set<String> getCatchedException(final MethodDeclaration testMtd, final String failedLine) {
		class TryVisitor extends ASTVisitor{
			private int line;
			Set<String> catchClauses = new HashSet<>();
			public TryVisitor(int ln){
				this.line = ln;
			}
			@Override
			public boolean visit(TryStatement node) {
				Block body = node.getBody();
				int start = testCu.getLineNumber(body.getStartPosition());
				int end = testCu.getLineNumber(body.getStartPosition() + body.getLength());
				
				if(failedLine != null && failedLine.startsWith("fail")) {//if the exception is caught by fail(), exceptions before the assert line should be reserved 
//					if(line > end) {
//						return false;
//					}
				}else {
					if(!(line >= start && line <= end)) {
						return false;
					}
				}
				for(Object obj : node.catchClauses()) {
					CatchClause catchClause = (CatchClause) obj;
					this.catchClauses.add(catchClause.getException().getType().toString());
				}
				return super.visit(node);
			}
		};
		
		int line = this.testOutput.getFailAssertLine();
		if(failedLine != null && failedLine.startsWith("assertTrue")) {
			String arg = failedLine.substring("assertTrue(".length(), failedLine.length() - 2);
			int head = this.testCu.getLineNumber(testMtd.getStartPosition()) + 1;
			
			boolean hitAssign = false;
			while(line > head) {
				line--;
				if(!hitAssign && testCodeLines[line - 1].contains(arg + "=")) {
					hitAssign = true;
				}
				if(hitAssign && testCodeLines[line - 1].trim().startsWith("catch")) {
					line--;
					break;
				}
			}
		}
		
		TryVisitor visitor = new TryVisitor(line);
		testMtd.accept(visitor);
		return visitor.catchClauses;
	}
	
	private String getExpectedException(MethodDeclaration testMtd) {
		String expected = null;
		if (testMtd.modifiers() != null) {
			for (Object modi : testMtd.modifiers()) {
				if (modi instanceof NormalAnnotation) {
					NormalAnnotation normal = (NormalAnnotation) modi;
					if (normal.getTypeName().toString().equals("Test")) {

						for (Object value : normal.values()) {
							if (value instanceof MemberValuePair) {
								if (((MemberValuePair) value).getName().toString().equals("expected")) {
									expected = ((MemberValuePair) value).getValue().toString();
								}
							}
						}
					}
				}
			}
		}
		return expected;
	}

	private String getAssertLineFromTestSrc() {
		if(testOutput.getFailAssertLine() <= 1) {
			return null;
		}
		List<String> testSrcLines = FileUtil.readFileToStringList(testSrcFile);
		String failAssertLine = testSrcLines.get(testOutput.getFailAssertLine() - 1).trim();
		if(! (failAssertLine.contains("assert") || failAssertLine.contains("fail") || failAssertLine.contains("expected")) ){
			return null;
		}
		if(!failAssertLine.endsWith(";")) {
			for(int i = testOutput.getFailAssertLine(); i < testSrcLines.size(); i++) {
				String curLine = testSrcLines.get(i).trim();
				failAssertLine += curLine;
				if(curLine.endsWith(";")) {
					break;
				}
			}
		}
		return failAssertLine;
	}

	private List<String> getAssertParams(String src) {
		List<String> result = new ArrayList<String>();
		try {
			ExpressionStatement assertStmt = (ExpressionStatement) JdtUtil.genASTFromStmt(src,
					projectConfig.getTestJdkLevel());
			MethodInvocation mtdCall = (MethodInvocation) assertStmt.getExpression();

			for (Object argu : mtdCall.arguments()) {
				result.add(argu.toString());
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println(src);
		}
		return result;
	}
	
    private String parseThrowException(String throwString){
        String exception = throwString.substring(throwString.lastIndexOf(" ") + 1, throwString.lastIndexOf("()"));
        String exceptionClass = CodeUtil.getClassNameOfImportClass(srcCode, exception);
        boolean needImport = false;
        if (exceptionClass.equals("")){
        	needImport = true;
            exceptionClass = CodeUtil.getClassNameOfImportClass(testCode, exception);
            if (exceptionClass.equals("")){
                return throwString;
            }
        }
        String exceptionCode = FileUtil.getCodeFromFile(projectConfig.getSrcRoot().getAbsolutePath(), exceptionClass);
        List<Integer> paramCount = CodeUtil.getMethodParamsCountInCode(exceptionCode, exception);
        int minParamCount = Collections.min(paramCount);
        if (minParamCount < 1){
            return throwString;
        }
        String param = "null";
        for (int i=1; i< minParamCount; i++){
            param += ",null";
        }
        if(needImport) {
            return "throw new " + exceptionClass + "(" + param + ");" ;
        }else {
            return "throw new " + exception + "(" + param + ");" ;
        }
    }
    
    private boolean needNotCond() {
    	//TODO:
    	int line = this.suspect.getLine() - 1;
    	if(this.errLine.startsWith("return ") || errLine.startsWith("throw ")) {
    		if(srcCodeLines[line + 1].trim().equals("}") && srcCodeLines[line - 1].trim().startsWith("if")) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean processDirRlp() {
    	if(!needNotCond()) {
    		return false;
    	}
		for(String body: this.ifBodies) {
			System.out.println("InsertRetFixPattern fixing: " + body);
			PatchResult patchRes = processPatch(srcClsFile, body);
			if (patchRes == PatchResult.SUCCESS) {
				recordPatch(PATCH_COMMENT);
				return true;
			} else{
				restoreSrcFile();
			}
		}
		return false;
	}
    
    private boolean isSimpleVar(String str) {
    	if(!Character.isLetter(str.charAt(0))){
			return false;
		}
    	for(int i = 1; i < str.length(); i++) {
    		if(!Character.isLetterOrDigit(str.charAt(i))){
    			return false;
    		}
    	}
    	return true;
    }
    
    private String getInvockedMtdCodeAndParam(String arg) {
    	String mtdInvo = JavaCodeUtil.getMtdInvo(arg);
		if(mtdInvo.equals("")) {
			return "";
		}
		
		class MtdVisitor extends ASTVisitor{
			private String searched;
			String mtdCode = "";
			String mtdParam = "";
			
			MtdVisitor(String searched){
				this.searched = searched;
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				
				if(node.isConstructor()) {
					return false;
				}
				
				if(!node.getName().getIdentifier().equals(searched)) {
					return false;
				}
				
				mtdCode = "private static " + node.getReturnType2().toString() + " patch_method(";
				
				for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
					SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
					mtdCode += v.toString();
					mtdParam += v.getName().toString();
					if (it.hasNext()) {
						mtdCode += ",";
						mtdParam += ","; 
					}
				}
				mtdCode += ")";
				mtdCode += node.getBody().toString();
				return false;
			}
			
		};
		
		MtdVisitor visitor = new MtdVisitor(mtdInvo);
		this.testCu.accept(visitor);
		
		return visitor.mtdCode + ">>>" + visitor.mtdParam;
    }
    
}
