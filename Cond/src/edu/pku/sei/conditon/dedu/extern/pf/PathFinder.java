package edu.pku.sei.conditon.dedu.extern.pf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.dedu.DeduFeatureGenerator;
import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.extern.Predictor;
import edu.pku.sei.conditon.dedu.pf.Path;
import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.GenExprItem;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.dedu.predall.PredAllExperiment;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.Cmd;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.StringUtil;

public abstract class PathFinder extends Predictor{
	
	private SearchStrategy searchStrategy;
	
	protected String filePath;
	protected int line;
	protected String sid;
	
	protected File srcFile;
	protected String srcCode;
	protected File srcClsFile;
	protected String[] srcCodeLines;
	protected String targetLine = "";
	protected File rootFile;
	
	protected String oracle;
	protected String predOracle;
	protected List<String> varOracle;
	
	protected List<String> designatedVars = Collections.emptyList();
	
	protected List<String> predSequence = new ArrayList<>();
	
	protected long initTime = System.currentTimeMillis();
	
	protected int compileTime = 0;
	protected int compileFailingTime = 0;
	
	private static final String D4J_FRAME_BASE = DeduMain.USER_HOME + "/workspace/defects4j/framework/projects";
	
	private static final String LANG_BASE = DeduMain.USER_HOME + "/workspace/defects4j/src/lang";
	private static final String LANG_65_CLS_PATH = LANG_BASE + "/lang_65_buggy/target/classes:"
			+ LANG_BASE + "/lang_65_buggy/target/tests:"
			+ D4J_FRAME_BASE + "/Lang/lib/commons-io.jar:"
			+ D4J_FRAME_BASE + "/Lang/lib/easymock.jar";
	
	private static final String MATH_BASE = DeduMain.USER_HOME + "/workspace/defects4j/src/math";
	private static final String MATH_106_CLS_PATH = MATH_BASE + "/math_106_buggy/target/classes:"
			+ MATH_BASE + "/math_106_buggy/target/test-classes:"
			+ D4J_FRAME_BASE + "/Math/lib/commons-discovery-0.5.jar";
	
	private static final String CHART_BASE = DeduMain.USER_HOME + "/workspace/defects4j/src/chart";
	private static final String CHART_26_CLS_PATH = CHART_BASE + "/chart_26_buggy/build/:"
			+ CHART_BASE + "/chart_26_buggy/build-tests/";
	
	private static final String TIME_BASE = DeduMain.USER_HOME + "/workspace/defects4j/src/time";
	private static final String TIME_27_CLS_PATH = TIME_BASE + "/time_27_buggy/build/classes:"
			+ TIME_BASE + "/time_27_buggy/build/tests:"
			+ D4J_FRAME_BASE + "/Time/lib/joda-convert-1.2.jar";
	
	private static final String LANG_65_ROOT = LANG_BASE + "/lang_65_buggy/";
	private static final String MATH_106_ROOT = MATH_BASE + "/math_106_buggy/";
	private static final String CHART_26_ROOT = CHART_BASE + "/chart_26_buggy/";
	private static final String TIME_27_ROOT = TIME_BASE + "/time_27_buggy/";
	
	private static final String LANG_65_TARGET = LANG_BASE + "/lang_65_buggy/target/classes/";
	private static final String MATH_106_TARGET = MATH_BASE + "/math_106_buggy/target/classes/";
	private static final String CHART_26_TARGET = CHART_BASE + "/chart_26_buggy/build/";
	private static final String TIME_27_TARGET = TIME_BASE + "/time_27_buggy/build/classes/";

	
	public PathFinder(String projAndBug, String srcRoot, String testRoot, String filePath,
			int line, int sid, SearchStrategy searchStrategy) {
		super(projAndBug, srcRoot, testRoot);
		
		this.filePath = filePath;
		this.line = line;
		this.sid = Integer.toString(sid);
		this.searchStrategy = searchStrategy;
		
		this.srcFile = new File(srcRoot + "/" + filePath);
		assert srcFile.exists(): "SRC FILE DOSE NOT EXIST!";
		
		this.srcCode = FileUtil.readFileToString(srcFile);
		this.srcCodeLines = srcCode.split("\n");
		
		assert srcCodeLines.length > line - 1: "EMPTY SRC FILE";
		this.targetLine = srcCodeLines[line - 1].trim();
		
		String targetRoot = "";
		String root = "";
		switch(proj) {
		case "lang":{
			targetRoot = LANG_65_TARGET;
			root = LANG_65_ROOT;
			break;
		}
		case "math":{
			targetRoot = MATH_106_TARGET;
			root = MATH_106_ROOT;
			break;
		}
		case "chart":{
			targetRoot = CHART_26_TARGET;
			root = CHART_26_ROOT;
			break;
		}
		case "time":{
			targetRoot = TIME_27_TARGET;
			root = TIME_27_ROOT;
			break;
		}
		default:
			throw new Error("ERROR PROJ NAME");
		}
		
		assert root != "";
		this.rootFile = new File(root);
		assert rootFile.exists(): "ROOT FILE DOES NOT EXIST!";
		assert targetRoot != "";
		
		String clsName = filePath.substring(0, filePath.length() - 5);
		String clsPath = JavaFile.classNameToItsClsPath(clsName);
		this.srcClsFile = new File(targetRoot + clsPath);
		
		checkoutRootRepo();
		compile();
		
		List<String> cmds = Arrays.asList(projAndBug, srcRoot, testRoot, filePath, Integer.toString(line), Integer.toString(sid));
		System.out.println(">>>> PathFinder CMDS:\n" + StringUtil.join(cmds, " ") + "\n");
	}
	
	public void setOracle(String oracle) {
		if(oracle == null)
			return;
		
		this.oracle = oracle;
		List<String> result = DollarilizeVisitor.parse(oracle);
		this.predOracle = result.get(0);
		this.varOracle = result.subList(1, result.size());
	}
	
	public int getCompileTime() {
		return compileTime;
	}

	public int getCompileFailingTime() {
		return compileFailingTime;
	}

	public long getInitTime() {
		return initTime;
	}

	public void entry() throws PathFindingException{
		// prepare
		invoker.prepare();
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		if(!varToVarFeaMap.isEmpty()) {
			String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
			
			// make start
			ProgramPoint start = makeStart();
			
			TreeSet<Path> results = getResults(start, ctxFea, varToVarFeaMap, allVarInfoMap);
	
			List<String> lines = Path.getResultLines(results);
	
			String proj_Bug_ithSusp = projAndBug + "_" + sid;
			AbsInvoker.dumpPlainResult(proj, proj_Bug_ithSusp, lines);
		}
		invoker.finish();
	}
	
	abstract protected ProgramPoint makeStart();
	
	abstract protected TreeSet<ProgramPoint> expand(ProgramPoint start, String ctxFea, 
			Map<String, String> varToVarFeaMap, Map<String, VariableInfo> allVarInfoMap);
	
	protected TreeSet<Path> getResults(ProgramPoint start, String ctxFea, 
			Map<String, String> varToVarFeaMap, 
			Map<String, VariableInfo> allVarInfoMap){
		
		if (allVarInfoMap.isEmpty()) {
			return CollectionUtil.emptyTreeSet();
		}
		
		List<Long> timeResults = new ArrayList<>(searchStrategy.getResultLimits());
		
		TreeSet<Path> results = CollectionUtil.<Path>newSortedSet();
		TreeSet<ProgramPoint> frontier = CollectionUtil.<ProgramPoint>newSortedSet();
		
		while(results.size() < searchStrategy.getResultLimits()) {
			TreeSet<ProgramPoint> newPoints = expand(start, ctxFea, varToVarFeaMap, allVarInfoMap);
			frontier.addAll(newPoints);
			newPoints.clear();
			if(frontier.isEmpty()) {
				break;
			}
			searchStrategy.getElementWithinLimit(frontier, results.size());
			start = frontier.pollFirst();
			//System.out.println(">>>> EXPANDIND:\n" + start.toString());
			if(start.isComplete()) {
				Path p = genPath(start);
				if(p.getScore() != 0) {
					if(CONFIG.isCompilationFilter()) {
						if(evaluateByCompiler(p)) {
							results.add(p);
						}
					} else {
						if(CONFIG.isBottomUp() && CONFIG.isRecur()) {
							if(p.getLast().getAstRoot().isTau()) {
								results.add(p);
							}
						} else {
							results.add(p);
						}
					}
					
				}
				//System.out.println("==== COMPLETE: " + results.size() + "\n" + p.toString());
				timeResults.add(System.currentTimeMillis() - initTime);
				
			}
		}
		dumpTimeResult(timeResults);
		
		return results;
	}

	private void dumpTimeResult(List<Long> timeResults) {
		String path = PredAllExperiment.getRecordFilePath(projAndBug, PredAllExperiment.TIME_POSTFIX);	
		FileUtil.writeStringToFile(path, sid + "\t" + timeResults.toString() + "\n", true);
	}

	protected Path genPath(ProgramPoint largest) {
		Path p = new Path();
		Stack<ProgramPoint> stack = new Stack<>();
		ProgramPoint curr = largest;
		while(curr != null) {
			stack.push(curr);
			curr = curr.getPreviousStep();
		}
		while(!stack.isEmpty()) {
			curr = stack.pop();
			p.append(curr);
		}
		return p;
	}
	
	protected boolean isLiterallyCommutive(ExprPredItem expr, List<VarPredItem> vars, boolean conj, boolean disj) {
		GenExprItem genExprItem = new GenExprItem(expr, vars);
		if(conj) {
			String[] arr = genExprItem.getGeneratedExpr().split("\\|\\|");
			if(arr[0].trim().equals(arr[1].trim())) {
				return true;
			}
		}
		if(disj) {
			String[] arr = genExprItem.getGeneratedExpr().split("&&");
			if(arr[0].trim().equals(arr[1].trim())) {
				return true;
			}
		}
		return false;
	}

	public List<String> getDesignatedVars() {
		return designatedVars;
	}

	public void setDesignatedVars(List<String> designatedVars) {
		assert designatedVars != null;
		this.designatedVars = designatedVars;
	}

	public String getOracle() {
		return oracle;
	}

	public String getPredOracle() {
		return predOracle;
	}

	public List<String> getVarOracle() {
		return varOracle;
	}

	public List<String> getPredSequence() {
		return predSequence;
	}
	
	public boolean evaluateByCompiler(Path p) {
		TreePredItem root = p.getLast().getAstRoot();
		String literal = root.fullyComplementedLiteral();
		
		int ifStart = targetLine.indexOf("if");
		int exprStart = targetLine.indexOf("(", ifStart);
		int exprEnd = targetLine.lastIndexOf(")");
		if(exprEnd < 0 || exprStart < 0) {
			return true;
		}
		
		String head = targetLine.substring(0, exprStart + 1);
		String rear = targetLine.substring(exprEnd);
		String newLine = StringUtil.connectMulty(" ", head, literal, rear);
		
		JavaFile.replaceCodeLine(srcFile, srcCodeLines, newLine, line);
		
		String cp = "";
		String level = "";
		String targetRoot = "";
		
		switch(proj) {
			case "lang":{
				cp = LANG_65_CLS_PATH;
				level = "1.4";
				targetRoot = LANG_65_TARGET;
				break;
			}
			case "math":{
				cp = MATH_106_CLS_PATH;
				level = "1.7";
				targetRoot = MATH_106_TARGET;
				break;
			}
			case "chart":{
				cp = CHART_26_CLS_PATH;
				level = "1.5";
				targetRoot = CHART_26_TARGET;
				break;
			}
			
			case "time":{
				cp = TIME_27_CLS_PATH;
				level = "1.7";
				targetRoot = TIME_27_TARGET;
				break;
			}
			default:
				throw new Error("ERROR PROJ NAME");
		}
		
		
		if(srcClsFile.exists()) {
			srcClsFile.delete();
		}
		
		//TODO: maybe related to jdk level
		String xlint = "-Xlint:unchecked"; //"-Xlint:none";
		String compileCmd = "javac "+ xlint + " -source " + level + " -target " + level + " -cp " + cp + " -sourcepath " + srcRoot +  " -d " + targetRoot + " " + srcFile.getAbsolutePath();
		
		compileTime++;
		
		File targetRootFile = new File(targetRoot);
		Cmd.runCmd(compileCmd, targetRootFile);
		boolean res = srcClsFile.exists();
		if(!res) {//if compile failed, restore origin class
			System.out.println("FAIL TO COMPILE: " + srcFile.getAbsolutePath() + " => " + srcClsFile.getAbsolutePath() + " @ " + line + ":\n\t" + targetLine + " =>  " + literal);
			compileFailingTime++;
		}
		
		checkoutRootRepo();
		
		return res;
	}
	
	private void checkoutRootRepo() {
		// restore
		final String checkout = "git checkout .";
		Cmd.runCmd(checkout, rootFile);
	}
	
	private void compile() {
		final String d4jCml = "defects4j compile";
		Cmd.runCmd(d4jCml, rootFile);
	}
}
