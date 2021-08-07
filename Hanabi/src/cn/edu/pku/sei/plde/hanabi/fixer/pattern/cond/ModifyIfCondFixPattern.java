package cn.edu.pku.sei.plde.hanabi.fixer.pattern.cond;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestOutput;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.TestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.trace.AngelicInstruVisitor;
import cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner;
import cn.edu.pku.sei.plde.hanabi.utils.CodeUtil;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdkUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;
import cn.edu.pku.sei.plde.hanabi.utils.PredictorUtil;
import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;


public class ModifyIfCondFixPattern extends ConditionRelatedFixPattern {
	
	private final static String PATCH_COMMENT =  "/*-- PATCH P2 --*/";

	private final static int MAX_TRIED_TIME = 200;
		
	private CompilationUnit cu;
	private String exceptionName;
	private String oriCondition;
	private Expression oriCondExpr;
	private Set<String> invokersInIfBody;

	private int condStartCol = -1;
	private int condEndCol = -1;
	
	private List<String> usedVars;
	
	private HitVisitor hitVisitor;
	
	private static final String TRACE_RES_PREFIX = "----";
	
	public ModifyIfCondFixPattern(ProjectConfig projectConfig, Suspect suspect, int iThSuspect, TestRunner testRunner,
			TestOutput testOutput, List<TestOutput> allFailTestOutputs) {
		super(projectConfig, suspect, iThSuspect, testRunner, testOutput, allFailTestOutputs);
		
		cu = getCompilationUnitFromCache(suspect.getClassName(), this.srcCode, projectConfig.getTestJdkLevel());
		
		exceptionName = this.testOutput.getExceptionName();
		
		// init hit visitor
		hitVisitor = new HitVisitor(suspect.getLine());
		cu.accept(hitVisitor);
	}

	@Override
	public FixResult implement() {
		if(!withinIfStmt()) {
			logger.info(">>>> DOES NOT HIT IF EXPR LINE");
			return FixResult.IMPLEMENT_FAIL;
		}
		
		if(!instrument()){
			logger.info(">>>> PREDICATE SWITCHING INSTRUMENT FAIL");
			return FixResult.IMPLEMENT_FAIL;
		}
		
		List<String> plauPatches = null;

		try {
			if(!compile()) {
				logger.info(">>>> PREDICATE SWITCHING COMPILE FAIL");
				return FixResult.IMPLEMENT_FAIL;
			}
		} catch (Exception e) {
			System.err.println("COMPILE EXCEPTION!");
			restoreSrcFile();
			restoreSrcClsFile();
			return FixResult.IMPLEMENT_FAIL;
		}
		
		boolean resTrue;
		boolean resFalse;
		try {
			//run tracer for angelix
			//angelix value := true
			boolean angVal = true;
			resTrue = runTracer(true, angVal);
			//angelix value := false
			angVal = false;
			resFalse = runTracer(true, angVal);
			
			if(resTrue == resFalse){
				logger.info(">>>> ANGELIX VALUE DO NOT WORK ! BOTH " + (resTrue ? "PASS " : "FAIL ") + suspect.getFile() + "#" + suspect.getLine());
				if(!hitVisitor.inLoop) {
					return FixResult.IMPLEMENT_FAIL;
				} else {
					logger.info(">>>> BUT LEFT FOR IN LOOP");
				}
			}
			
			if(hitVisitor.skip) {
				logger.info(">>>> SKIP LARGE IF BODY");
				return FixResult.IMPLEMENT_FAIL;
			}
			
		} catch (Exception e) {
			System.err.println("COMPILE EXCEPTION!");
			return FixResult.IMPLEMENT_FAIL;
		} finally {
			restoreSrcFile();
			restoreSrcClsFile();
		}
		
		
		List<String> allPredicates;
		if(oriHasMtdInvOrConjunction()) {
			allPredicates = PredictorUtil.predictIfConds(projectConfig, suspect, iThSuspect);
		} else if(maybeThrow()) {
			allPredicates = PredictorUtil.predictIfConds(projectConfig, suspect, iThSuspect);
		}  else {
			allPredicates = PredictorUtil.predictIfConds(projectConfig, suspect, iThSuspect, usedVars);
		}
		
		if (allPredicates == null || allPredicates.isEmpty()) {
			logger.info(">>>> EMPTY PREDICATION");
			return FixResult.IMPLEMENT_FAIL;
		}
				
		// choose modify direct, looser of tighter
		List<String> patchConditions = getPatches(resTrue, allPredicates);
 		if(patchConditions.isEmpty()){
			return FixResult.IMPLEMENT_FAIL;
		}
		
		if(EXHAUSTE_ALL_PATCH) {
			plauPatches =  new ArrayList<>();
			recordAllPlauPatchHead("ModifiyIfCondFixPattern");
		}
		
		//apply & check
		int tried = 0;
		for (String patch : patchConditions) {
  			tried++;
			if (tried > MAX_TRIED_TIME){
				break;
			}
//			if(!(patch.equals("FastMath.abs(a0) > FastMath.abs(overflow)") || patch.equals("Math.abs(p2) > overflow || Math.abs(q2) > overflow") )) { 
//				continue;
//			}
			
			logger.info(">>>> ModifiyIfCondFixPattern fixing: " + patch);
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
	
	private boolean oriHasMtdInvOrConjunction() {
		class MIVisitor extends ASTVisitor{
			boolean hit;
			@Override
			public boolean visit(MethodInvocation node) {
				hit = true;
				return super.visit(node);
			}
			@Override
			public boolean visit(InfixExpression node) {
				String op = node.getOperator().toString().trim();
				if(op.equals("&&") || op.equals("||")) {
					hit = true;
				}
				return super.visit(node);
			}
			
		}
		MIVisitor visitor = new MIVisitor();
		this.oriCondExpr.accept(visitor);
		return visitor.hit;
	}
	
	private boolean maybeThrow() {
		int line = this.suspect.getLine();
		String nextLine = srcCodeLines[line].trim();
		return nextLine.startsWith("throw new ");
	}

	private String getPatchLine(String cond) {
		String oriLine = srcCodeLines[suspect.getLine() - 1];
		String head = oriLine.substring(0, this.condStartCol);
		String rear = oriLine.substring(this.condEndCol);
		return StringUtil.connectMulty(" ", head, cond, rear);
	}
	
	protected boolean compilePatch(File srcClsFile, String patch) {
		//restoreSrcFile();//TODO:: check, need this line?
		String newLine = getPatchLine(patch);
		CodeUtil.replaceCodeLine(this.srcFile, this.srcCodeLines, newLine, this.suspect.getLine());
		boolean res = false;
		try{
			res = CodeUtil.javac(projectConfig, srcFile, srcClsFile, null);
		}catch(Exception e){
			restoreSrcFile();
		}
		return res;
	}

	private boolean runTracer(boolean enable, boolean angVal) throws Exception {
		boolean res = runTestMethod(enable, angVal);
		String failedTestFullName = testOutput.getFailTest().replaceAll("::", "#");
		String traceFilePath = MethodTestRunner.generateTraceFilePath(projectConfig.getProjectName(), failedTestFullName, suspect.getLine());
		
		String lastRecord;
		if(enable) {
			lastRecord = TRACE_RES_PREFIX + "\t" + angVal + "\t" + res + "\n";
		} else {
			lastRecord = TRACE_RES_PREFIX + "\tDISABLE\t" + res + "\n";
		}
		FileUtil.writeStringToFile(traceFilePath, lastRecord, true);
		return res;
	}
	
	private boolean withinIfStmt() {
		
		if(hitVisitor.exprLineNum > 0) {
			this.oriCondition = hitVisitor.condition;
			this.oriCondExpr = hitVisitor.condExpr;
			this.condStartCol = hitVisitor.exprColBegin;
			this.condEndCol = hitVisitor.exprColEnd;
			Suspect newSus = this.suspect.clone(hitVisitor.exprLineNum);
			
			System.out.println("MODIFY SUSPECT FROM " + suspect.getLine() + " TO " + newSus.getLine() + " IN ModifiyIfCondFixPattern");
			this.suspect = newSus;
			
			usedVars = collectVar(oriCondExpr);
			if(usedVars.isEmpty()) {
				return false;
			}
			
			class InvokerCollectorVisitor extends ASTVisitor{
				Set<String> invokers = new HashSet<>();
				@Override
				public boolean visit(FieldAccess node) {
					invokers.add(node.getExpression().toString());
					return super.visit(node);
				}

				@Override
				public boolean visit(MethodInvocation node) {
					if(node.getExpression() != null) {
						   invokers.add(node.getExpression().toString());
					}
					return super.visit(node);
				}
				
			}
			
			InvokerCollectorVisitor invokerCollectorVisitor = new InvokerCollectorVisitor();
			hitVisitor.ifThenBody.accept(invokerCollectorVisitor);
			
			invokersInIfBody = invokerCollectorVisitor.invokers;
			
			return true;
		}else {
			return false;
		}
		
	}
	
	private boolean skipByTrace() {
		if (!hitVisitor.inLoop) {
			return true;
		}
		boolean skip = false;
		try {
			String failedTestFullName = testOutput.getFailTest().replaceAll("::", "#");
			String traceFilePath = MethodTestRunner.generateTraceFilePath(projectConfig.getProjectName(),
					failedTestFullName, suspect.getLine());
			File traceFile = new File(traceFilePath);
			// clear file
			FileUtil.writeStringToFile(traceFile, "", false);

			// re-trace
			runTracer(false, false);

			List<String> lines = FileUtil.readFileToStringList(traceFile);

			boolean start = false;
			List<Boolean> values = new ArrayList<>();
			boolean switched = false;
			for (String line : lines) {
				line = line.trim();
				if (line.equals(">>")) {
					start = true;
					continue;
				}
				if (start) {
					start = false;

					boolean currValue;
					if (line.endsWith(", true>")) {
						currValue = true;
					} else {
						assert line.endsWith(", false>");
						currValue = false;
					}
					if (!values.isEmpty()) {
						boolean last = values.get(values.size() - 1);
						if (last != currValue) {
							if (switched) {
								skip = true;
								break;
							} else {
								switched = true;
							}
						}
					}
					values.add(currValue);
				}
			}

		} catch (Exception e) {
			skip = true;
		}
		return skip;
	}
	

	/**
	 * TODO: refactor to Tracer!
	 * @return
	 * @throws Exception
	 */
	private boolean compile() throws Exception{
		String cp = System.getProperty("user.dir") + "/bin/";
		if(projectConfig.getClassPaths().contains(cp)) {
			cp = null;
		}
		return CodeUtil.javac(projectConfig, srcFile, srcClsFile, Arrays.asList(cp));
	}

	/**
	 * 
	 * @param enabled
	 * @param angelixVal
	 * @return pass or fail
	 * @throws Exception
	 */
	private boolean runTestMethod(boolean enabled, boolean angelixVal) throws Exception{
//		String config = "-Dsgfix.angelic.enable=\"true\" -Dsgfix.angelic.boolval=" + (angelixVal ? "\"true\"" : "\"false\"");
		String config = "ANGELIC " + suspect.getLine() + " " + enabled + " " + angelixVal;
		String bugName = this.projectConfig.getProjectName();
		return testRunner.runSingleTestMethodByMethodTestRunner(bugName, testOutput.getFailTestCls(), testOutput.getFailTestMethod(), config);
	}
	
	/**
	 * @return false if the suspect line dose not hit if-condition expression, or instrument fail. 
	 */
	private boolean instrument(){
		String exceptionName = this.exceptionName;
		try{
			AngelicInstruVisitor visitor = new AngelicInstruVisitor(suspect.getLine(), exceptionName);
			//trace the condition
			CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(this.srcCode, projectConfig.getTestJdkLevel(), ASTParser.K_COMPILATION_UNIT);
			cu.accept(visitor);
			
			if(visitor.getHit() == false){//suspect not in if statement
				return false;
			}
			FileUtil.writeStringToFile(this.srcFile, cu.toString(), false);
		}catch(Throwable t){
			restoreSrcFile();
			return false;
		}
		return true;
	}
	
	private Map<String, Object> getInmutableValueInAllTraces() {
		Map<String, Object> sameInAllTraces = new HashMap<>();
		List<String> lines = FileUtil.readFileToStringList(Config.TEMP_TRACE_FOLDER);
		if(lines == null) {
			return sameInAllTraces;
		}
		Map<String, Object> tobeRemove = new HashMap<>();

		for(String line : lines) {
			if(line.startsWith("<")) {
				StringBuffer sb = new StringBuffer(line.trim());
				sb.deleteCharAt(0);
				sb.deleteCharAt(sb.length() - 1);
				String[] arr = sb.toString().split(",");
				String key = arr[0].trim();
				String val = arr[1].trim();
				if(key.endsWith("!=null")) {
					Object snd = new Boolean(val);
					if(sameInAllTraces.containsKey(key)) {
						Boolean previous = (Boolean) sameInAllTraces.get(key);
						if(!previous.equals(snd)) {
							tobeRemove.put(key, snd);
						}
					}else {
						sameInAllTraces.put(key, snd);
					}
				}
			}
		}//end for(String line : lines)
		
		for(Entry<String, Object> entry: tobeRemove.entrySet()) {
			if(sameInAllTraces.containsKey(entry.getKey())) {
				sameInAllTraces.remove(entry.getKey());
			}
		}// end for(Entry...
		return sameInAllTraces;
	} 
	
	private List<String> removeUselessAndIllegal(List<String> allPredicates) {
		List<String> tobeRemoved = new ArrayList<>();
		for(String pred: allPredicates) {
			if(pred.contains(" != null")) {
				tobeRemoved.add(pred);
				continue;
			}
			if(oriCondition.equals(pred) || oriCondition.equals("(" + pred + ")")) {
				tobeRemoved.add(pred);
				continue;
			}
			
			if(pred.endsWith(" == null")) {
				String left = pred.replaceAll("== null", "").trim();
				if(this.invokersInIfBody.contains(left)) {
					tobeRemoved.add(pred);
				}
			}
			
			if(! legalPatterns(pred)) {
				tobeRemoved.add(pred);
				continue;
			}
		}
		return tobeRemoved;
	}
	
	private List<String> getPatches(boolean excepted, List<String> allPredicates) {
		
		if(allPredicates == null || allPredicates.isEmpty()) {
			return Collections.<String>emptyList();
		}
		List<String> tobeRemoved = removeUselessAndIllegal(allPredicates);
		allPredicates.removeAll(tobeRemoved);
		
		Set<String> notNull = analyOriCond();
		//in case of exceptions such as NPT or cast exception
		if(JdkUtil.isRunTimeExeception(exceptionName) || (excepted == false && maybeThrow())) {
			List<String> results = new ArrayList<>();
			boolean npe = "java.lang.NullPointerException".equals(exceptionName);
			for(String pred : allPredicates) {
				if(!fitRuntime(pred, excepted, notNull)) {//TODO pred.eq(oriCondition)
					continue;
				} 
//				if(npe && !pred.contains("== null")) {
//					continue;
//				}
				String newCond = StringUtil.connectMulty(" ", pred, "&&", oriCondition);
				results.add(newCond);
			}
			return results;
		} else {
			return allPredicates;
		}
	}
	
	private Set<String> analyOriCond(){
		assert this.oriCondExpr != null;
		class NotNullVisitor extends ASTVisitor{
			Set<String> notNull = new HashSet<>();
			@Override
			public boolean visit(MethodInvocation node) {
				if(node.getExpression() instanceof SimpleName) {
					notNull.add(node.getExpression().toString().trim());
				}
				return super.visit(node);
			}
			@Override
			public boolean visit(CastExpression node) {
				String tp = node.getType().toString();
				if(TypeUtil.isPrimitiveType(tp)) {
					Expression res = node.getExpression();
					if(res instanceof ParenthesizedExpression) {
						res = ((ParenthesizedExpression) res).getExpression();
					}
					notNull.add(res.toString().trim());
				}
				return super.visit(node);
			}
		};
		NotNullVisitor visitor = new NotNullVisitor();
		this.oriCondExpr.accept(visitor);
		return visitor.notNull;
	}
	
	private boolean fitRuntime(String cond, boolean excepted, Set<String> notNull) {
		cond = cond.replaceAll(" ", "");
		if(cond.endsWith("==null")) {
			String expr = cond.substring(0, cond.length() - 6);
			if(excepted == false) {
				if(notNull.contains(expr)) {
					return false;
				}
			}
		}
		/*
		else if(cond.endsWith("!=null")) {
			String expr = cond.substring(0, cond.length() - 6);
			if(excepted == true) {
				if(notNull.contains(expr)) {
					return false;
				}
			}
		}*/
		return true;
	}
	
	private void restoreSrcClsFile() {
		boolean res = false;
		try{
			res = CodeUtil.javac(projectConfig, srcFile, srcClsFile, null);
		}catch(Exception e){
			restoreSrcFile();
		}
		assert res: "RESTORE FAILED";
	}
	
	private boolean legalPatterns(String patchCond) {
		assert this.oriCondExpr != null;

		/**
		 * inner ast visitor
		 */
		class PatternCollector extends ASTVisitor{
			int positionNum = 0;
			String mtdName = null;
			String caller = null;
			
			Set<String> cmpSet = new HashSet<>();
			
			@Override
			public boolean visit(InfixExpression node) {
				if(ASTLocator.isComparing(node.getOperator())) {
					cmpSet.add(node.getOperator().toString());
				}
				return super.visit(node);
			}
			@Override
			public boolean visit(MethodInvocation node) {
				mtdName = node.getName().getIdentifier();
				if(node.getExpression() != null) {
					caller = node.getExpression().toString();
				}
				return super.visit(node);
			}			
		}
		
		try {
			PatternCollector oriVisitor = new PatternCollector();
			oriCondExpr.accept(oriVisitor);
			
			Expression patch = (Expression) JavaFile.genASTFromSource(patchCond, ASTParser.K_EXPRESSION, projectConfig.getTestJdkLevel());
			PatternCollector patchVisitor = new PatternCollector();
			patch.accept(patchVisitor);
			if(oriVisitor.mtdName != null) {
				
				if(JdkUtil.isRunTimeExeception(exceptionName)) {
					if(oriVisitor.caller != null && patchCond.contains(oriVisitor.caller)) {
						return true;
					}
				}
				if(!oriVisitor.mtdName.equals(patchVisitor.mtdName)){
					return false;
				}
				
			}
			if(!oriVisitor.cmpSet.contains("==") && !oriVisitor.cmpSet.contains("!=") && patchVisitor.cmpSet.contains("==")) {
				return false;
			}
			
			return true;
		
		} catch(Exception e) {
			return false;
		}
		
	}
	
	private List<String> collectVar(Expression cond) {
		if(cond instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression) cond;
			Expression left = infix.getLeftOperand();
			Expression right = infix.getRightOperand();
			String op = infix.getOperator().toString();
			if(op.equals("||") || op.equals("&&")) {
				if(left instanceof InfixExpression && right instanceof InfixExpression) {
					InfixExpression leftInfix = (InfixExpression) left;
					InfixExpression rightInfix = (InfixExpression) right;
					if(leftInfix.getOperator().toString().equals(">") && rightInfix.getOperator().toString().equals("<")
						|| leftInfix.getOperator().toString().equals(">=") && rightInfix.getOperator().toString().equals("<=") 
						|| leftInfix.getOperator().toString().equals("<") && rightInfix.getOperator().toString().equals(">")
						|| leftInfix.getOperator().toString().equals("<=") && rightInfix.getOperator().toString().equals(">=")){
						
						if(leftInfix.getLeftOperand().toString().equals(rightInfix.getLeftOperand().toString())){
							if(leftInfix.getRightOperand() instanceof SimpleName && rightInfix.getRightOperand() instanceof PrefixExpression) {
								PrefixExpression prefix = (PrefixExpression)rightInfix.getRightOperand();
								if(prefix.getOperator().toString().equals("-") 
										&& leftInfix.getRightOperand().toString().equals(prefix.getOperand().toString()) 
										&& leftInfix.getLeftOperand() instanceof SimpleName){
									List<String> collected = new ArrayList<>();
									collected.add(leftInfix.getLeftOperand().toString());
									return collected;
								}
							}
						}
					}
				}
			}
		}
		class VarCollector extends ASTVisitor{
			List<String> simpleNameList = new ArrayList<>();
			@Override
			public boolean visit(SimpleName node) {
				if(ASTLocator.notVarLocation(node)) {
					return false;
				}
				if(Character.isUpperCase(node.getIdentifier().charAt(0))) {
					return false;
				}
				
				this.simpleNameList.add(node.getIdentifier());
				return super.visit(node);
			}
			
		};
		
		VarCollector visitor = new VarCollector();
		cond.accept(visitor);
		
		return visitor.simpleNameList;
	}
	
	/**
	 * Analysis hitting if statement
	 *
	 */
	class HitVisitor extends ASTVisitor{
		int exprLineNum = -1;
		String condition = null;
		Expression condExpr = null;
		Statement ifThenBody = null;
		
		int ifThenBodySize = 0;
		int blockNum;
		
		boolean inLoop =false;
		
		boolean nestIf = false;
		boolean nestFor = false;
		
		boolean skip = false;
		
		int exprColBegin = -1;
		int exprColEnd = -1;
		
		final int line;
		HitVisitor(int line){
			this.line = line;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			int head = cu.getLineNumber(node.getStartPosition());
			int tail = cu.getLineNumber(node.getStartPosition() + node.getLength());
			if(line < head || line > tail) {
				return false;
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(IfStatement node) {
			Expression expr = node.getExpression();
			int head = cu.getLineNumber(node.getStartPosition());
			int tail = cu.getLineNumber(node.getStartPosition() + node.getLength());
			if(line >= head && line <= tail) {
				exprLineNum = cu.getLineNumber(expr.getStartPosition());
				
				if(expr instanceof ParenthesizedExpression) {
					condition = expr.toString();
				}else {
					condition = "(" + expr.toString() + ")";//enclosing with braces
				}
				condExpr = expr;
				ifThenBody = node.getThenStatement();
				
				if(ifThenBody instanceof Block) {
					List<?> stmts = ((Block) ifThenBody).statements();
					ifThenBodySize = stmts.size();
					Iterator<?> it = stmts.iterator();
					while (it.hasNext()) {
						Statement currStmt = (Statement) it.next();
						if (currStmt instanceof IfStatement) {
							nestIf = true;
						}
						if(currStmt instanceof ForStatement) {
							nestFor = true;
						}
					}
					
				} else {
					ifThenBodySize = 1;
				}
				
				if(((nestIf || nestFor) && ifThenBodySize > 5)) {
					skip = true;
				}
				
				if(node.getParent() instanceof Block) {
					Block parent = (Block) node.getParent();
					if(parent.getLocationInParent() == ForStatement.BODY_PROPERTY) {
						inLoop = true;
					}
				}
				
				exprColBegin = cu.getColumnNumber(expr.getStartPosition());
				exprColEnd = cu.getColumnNumber(expr.getStartPosition() + expr.getLength());
				
				int exprStartLine = cu.getLineNumber(expr.getStartPosition());
				int exprEndLine = cu.getLineNumber(expr.getStartPosition() + expr.getLength());
				if(exprEndLine > exprStartLine) {
					skip = true;
				}
			} 
			return super.visit(node);
		}
	}
}
