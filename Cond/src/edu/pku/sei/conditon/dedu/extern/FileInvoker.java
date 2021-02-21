package edu.pku.sei.conditon.dedu.extern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.DeduFeatureGenerator;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.pred.ConcretTree;
import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.GenExprItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.Cmd;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;

public class FileInvoker extends AbsInvoker{

	
	public FileInvoker(Map<String, OriPredItem> allOriPredicates, Map<String, Integer> pos0TimeMap) {
		super(allOriPredicates, pos0TimeMap);
	}

	private static final File dir = new File(PREDICTOR_ROOT);
	

	@Override
	public void prepare() {
		
	}

	@Override
	public void finish() {
		
	}

	@Override
	public List<VarPredItem> predictVar(String direction, int n, List<String> featureLines, Map<String, VariableInfo> allVarInfoMap) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictExpr(String direction, String featureLine) {
		return null;
	}
	
	@Override
	public List<ExprPredItem> predictTDExprs(String ctxFea) {
		return null;
	}

	@Override
	public List<VarPredItem> predictTDVars(int n, List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap) {
		return null;
	}

	@Override
	public List<VarPredItem> predictBUV0(Map<String, VariableInfo> allVarInfoMap,
			Map<String, String> varToFeaPrefixMap) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictBUExprs(String v0Iter, String curVarFea) {
		return null;
	}

	@Override
	public List<VarPredItem> predictBUVars(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n) {
		return null;
	}
	
	@Override
	public List<RecurNodePredItem> predictForNodeTypes(String featureLine) {
		return null;
	}

	@Override
	public List<ExprPredItem> predictRecurExprs(String featureLine) {
		return null;
	}	
	
	@Override
	public List<VarPredItem> predictRecurVar(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n) {
		return null;
	}
	
	private static void writeRecurNodePred(String project, String baseBugId, String type, List<String> features) {
		List<String> result = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		if("expr".equals(type)){
			result.add(AbstractDeduVisitor.getRecurNodeExprHeader().trim());
		}else if("var".equals(type)){
			result.add(AbstractDeduVisitor.getRecurNodeVarHeader().trim());
		}else if("recur".equals(type)) {
			result.add(AbstractDeduVisitor.getRecurNodeTypeHeader().trim());
		}else{
			throw new Error();
		}
		
		result.addAll(features);
		
		String prefix = PREDICTOR_ROOT + "/input/" + project + "/" + project + "_" + baseBugId + "/pred/";
		
		File folder = new File(prefix);
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		prefix += project + "_" + baseBugId;
		
		FileWriter writer = null;
		BufferedWriter bf = null;
		try {
			writer = new FileWriter(prefix + "." + type + ".csv");
			bf = new BufferedWriter(writer);
			for(String s : result){
				bf.write(s);
				bf.newLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			FileUtil.closeInputStream(bf);
		}
	}
	
	
	private static void wirteBUPred(String project, String baseBugId, String type, List<String> features){
		List<String> result = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		if("v0".equals(type)){
			result.add(AbstractDeduVisitor.getButtomUpStepZeroHeader().trim());
		} else if("expr".equals(type)){
			result.add(AbstractDeduVisitor.getButtomUpStepOneHeader().trim());
		} else if("var".equals(type)){
			result.add(AbstractDeduVisitor.getButtomUpStepTwoHeader().trim());
		} else {
			throw new Error();
		}
		
		result.addAll(features);
		
		String prefix = PREDICTOR_ROOT + "/input/" + project + "/" + project + "_" + baseBugId + "/pred/";
		
		File folder = new File(prefix);
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		prefix += project + "_" + baseBugId;
		
		FileWriter writer = null;
		BufferedWriter bf = null;
		try {
			writer = new FileWriter(prefix + "." + type + ".csv");
			bf = new BufferedWriter(writer);
			for(String s : result){
				bf.write(s);
				bf.newLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			FileUtil.closeInputStream(bf);
		}
		
	}
	
	
	private static void wirteTDPred(String project, String baseBugId, String type, List<String> features){
		List<String> result = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		if("expr".equals(type)){
			result.add(AbstractDeduVisitor.getTopDownStepZeroHeader().trim());
		}else if("var".equals(type)){
			result.add(AbstractDeduVisitor.getTopDownStepOneHeader().trim());
		}else{
			throw new Error();
		}
		
		result.addAll(features);
		
		String prefix = PREDICTOR_ROOT + "/input/" + project + "/" + project + "_" + baseBugId + "/pred/";
		
		File folder = new File(prefix);
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		prefix += project + "_" + baseBugId;
		
		FileWriter writer = null;
		BufferedWriter bf = null;
		try {
			writer = new FileWriter(prefix + "." + type + ".csv");
			bf = new BufferedWriter(writer);
			for(String s : result){
				bf.write(s);
				bf.newLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			FileUtil.closeInputStream(bf);
		}
	}
	
	private static List<RecurNodePredItem> readRecurNodeCSV(String proj, String base){
		String path = PREDICTOR_ROOT + "/output/" + proj + "/" + base + "/" + base + ".recur_pred.csv";
		List<RecurNodePredItem> predNodes = new ArrayList<>();
		List<String> lines = FileUtil.readFileToStringList(path);
		for(String line : lines) {
			String[] columns = line.split("\t");
			assert columns.length == 2;
			int label = Integer.valueOf(columns[0]);
			double score = Double.valueOf(columns[1]);
			RecurNodePredItem item = new RecurNodePredItem(label, score);
			predNodes.add(item);
		}
		return predNodes;
	}
	
	private static List<VarPredItem> readVarCSV(boolean isBottomUp, Map<String, VariableInfo> allVarInfoMap, String proj, String base, int position){
				
		String path = PREDICTOR_ROOT + "/output/" + proj + "/" + base + "/" + base;
		if(isBottomUp && position == 0){
			path += ".v0_pred.csv";
		}else{
			path += ".var_pred.csv";
		}
		List<VarPredItem> predVars = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		File file = new File(path);
		BufferedReader bReader = null;
		FileReader fReader = null;
		try {
			fReader = new FileReader(file);
			bReader = new BufferedReader(fReader);
			String line = null;
			while ((line = bReader.readLine()) != null) {
				String[] columns = line.split("\t");
				assert columns.length == 2;
				String var = columns[0];
				
				Double score = new Double(columns[1]);
				VariableInfo info = allVarInfoMap.get(var);
				
				if(info == null) {
					continue;
				}
				
				VarPredItem item = new VarPredItem(var, position, info, score);
				predVars.add(item);
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorExit("NO EXPRSSION GENERATED !");
		} finally{
			FileUtil.closeInputStream(fReader, bReader);
		}
				
		VarPredItem.adjustVarsProbability(predVars);
		return predVars;
	}
	
	
	private static List<ExprPredItem> readExprCSV(String fileName, String proj, String base){
		String path = PREDICTOR_ROOT + "/output/" + proj + "/" + base + "/" + base;
		List<ExprPredItem> predExprs = new ArrayList<>(DEFAULT_EXPR_RES_NUM);
		List<String> lines = FileUtil.readFileToStringList(path + ".expr_pred.csv");
		int max = lines.size() > CONFIG.getExprLimit() ? CONFIG.getExprLimit() : lines.size();
		for(int l = 0; l < max; l++) {
			String line = lines.get(l);
			String[] columns = line.split("\t");
			assert columns.length == 2;
			String expr = columns[0];

			try {
				Expression astnode = ExprPredItem.generateASTNodeForDollarExpr(expr);
				Double score = new Double(columns[1]);
				
				ExprPredItem item = new ExprPredItem(fileName, expr, astnode, score);
				
				if(FileInvoker.designatedVars != null && !FileInvoker.designatedVars.isEmpty()) {
					if(item.getPositionNum() != FileInvoker.designatedVars.size()) {
						continue;
					}
				}
				
				predExprs.add(item);
			}catch (Exception e) {
				//System.err.println(expr);
				continue;
			}
		}
		return predExprs;	}
	
	private static void predictCurrNodeType(String model, String ctxFea, TreePredItem currNode, List<TreePredItem> roots) {
		
		if(currNode.getCurrentHightToRoot() > TreePredItem.MAX_TREE_HIGHT - 1) {
			return;
		}
		
		String currFeature = currNode.getFeature();
		List<RecurNodePredItem> dummyTypes = predictForNodeTypes(model, ctxFea, currFeature);
		
		TreePredItem root = currNode.getRoot();
		
		for(RecurNodePredItem type: dummyTypes) {
			
			if(type.getOpcode().equals(Opcode.NONE)) {// 'NONE' use the current dummy node
				currNode.setExpandItem(type);	
				continue;
			}
			TreePredItem newRoot = TreePredItem.deepCloneFromRoot(root);
			assert newRoot.toString().equals(root.toString());
			assert newRoot.getCurrentHight() == root.getCurrentHight();
			roots.add(newRoot);
			
			TreePredItem newCurrNode = TreePredItem.getCorrespondingNode(root, newRoot, currNode);
			assert newCurrNode != null;
			assert newCurrNode.getRoot() == newRoot;
			
			newCurrNode.setExpandItem(type);
			
			if(newCurrNode.canExpandChild0()) {
				TreePredItem child0 = new TreePredItem(newCurrNode);
				newCurrNode.setChild0(child0);
				predictCurrNodeType(model, ctxFea, child0, roots);
			}
			if(newCurrNode.canExpandChild1()) {
				TreePredItem child1 = new TreePredItem(newCurrNode);
				newCurrNode.setChild1(child1);
				predictCurrNodeType(model, ctxFea, child1, roots);
			}
		}
		
	}
	
	private static List<RecurNodePredItem> predictForNodeTypes(String model, String ctxFea, String recurNodeFea){
		String proj = model.split("_")[0];
		String baseBugId = model.split("_")[1];
		String predNodeCmd = "python run_predict.py " + proj + " " + baseBugId + " recurnode recur";
				
		writeRecurNodePred(proj, baseBugId, "recur", Arrays.asList(ctxFea + recurNodeFea + "?"));
		Cmd.runCmd(predNodeCmd , dir);
		
		List<RecurNodePredItem> types = readRecurNodeCSV(proj, model);
		return types;
	}
	
	private static List<TreePredItem> predTrees(String model, String ctxFea) {
		
		if(ctxFea == null) {
			throw new Error();
		}
		
		List<TreePredItem> roots = new ArrayList<>();
		TreePredItem root = new TreePredItem(null);
		roots.add(root);
		
		predictCurrNodeType(model, ctxFea, root, roots);
				
		List<TreePredItem> tobeRemoved = new ArrayList<>();
		
		for(TreePredItem it: roots) {
			if(!TreePredItem.isRecurNodeFullyExpanded(it)) {
				tobeRemoved.add(it);
			} else if(it.isNotNotRedundant()) {
				tobeRemoved.add(it);
			}
		}
		roots.removeAll(tobeRemoved);
		tobeRemoved.clear();
		
		for(TreePredItem it: roots) {
			it.getFinalScore();
		}
		
		final BigDecimal limit = new BigDecimal(String.valueOf(CONFIG.getTreeProbLimit()));
		for(TreePredItem it: roots) {
			if(it.getFinalScore().compareTo(limit) == -1) {
				tobeRemoved.add(it);
			}
		}
		roots.removeAll(tobeRemoved);
		tobeRemoved.clear();
		
		Collections.sort(roots, Collections.reverseOrder());
		
		// remain top k
		CollectionUtil.remainListFirstK(roots, CONFIG.getBeamSearchLimits());
		return roots;
	}
	
	
	private static List<GenExprItem> predTopDown(String model, String fileName, String ctxFea,
			Map<String, OriPredItem> allOriPredicates, Map<String, String> varToVarFeaMap, 
			Map<String, VariableInfo> allVarInfoMap) {
		
		String proj = model.split("_")[0];
		String baseBugId = model.split("_")[1];
		
		String predCmd = "python run_predict.py " + proj + " " + baseBugId + " expr top_down";
		String predVarCmd = "python run_predict.py " + proj + " " + baseBugId + " var top_down";
		
		
		if(ctxFea == null) {
			throw new Error();
		}
		
		wirteTDPred(proj, baseBugId, "expr", Arrays.asList(ctxFea + del + "?"));

		Cmd.runCmd(predCmd , dir);
		
		List<ExprPredItem> exprs = readExprCSV(fileName, proj, model);
		
		List<ExprPredItem> allExprs = new ArrayList<>(DEFAULT_EXPR_RES_NUM);

		for(ExprPredItem exprItem: exprs){
			String predFeature = exprItem.getExprFeature();
			Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list

			for(int n = 0; n < exprItem.getPositionNum(); n++){
				List<String> varFeatersAtN = new ArrayList<>(DEFAULT_VAR_RES_NUM);

				for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
					List<String> varLineList = new ArrayList<>();
					String varFeaPrefix = entry.getValue();
					varLineList.add(ctxFea);
					varLineList.add(varFeaPrefix);
					varLineList.add(predFeature);

					String varName = entry.getKey();
					boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), varName);
					varLineList.add("" + argued); //argused
					
					VariableInfo info = allVarInfoMap.get(varName);
					if(info == null) {
						continue;
					}
					boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, info, allOriPredicates); 
					varLineList.add("" + typeFit); //argused

					int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(varName, exprItem.getPred(), n, allOriPredicates);
					if(occuredTime == 0 && argued) {
						occuredTime ++;
					}
					varLineList.add("" + occuredTime); //occuredTime

					varLineList.add("" + n); //position
					varLineList.add("?"); //putin
					String currStr = StringUtil.join(varLineList, del);
					
					varFeatersAtN.add(currStr);
				}
				
				wirteTDPred(proj, baseBugId, "var", varFeatersAtN);
 
				Cmd.runCmd(predVarCmd , dir);

				List<VarPredItem> varsAtN = getSortedVarList(false, allVarInfoMap, proj, model, n);
				predVars.put(n, varsAtN);

			}
			
			exprItem.setPredVars(predVars);
			if(predVars.size() == exprItem.getPositionNum()){
				allExprs.add(exprItem);
			}
		}
		ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
		List<GenExprItem> res = generator.generateExpr();
		return res;
	}

	private static List<GenExprItem> predBottomUp(String model, String fileName, 
			Map<String, OriPredItem> allOriPredicates, Map<String, String> varToFeaPrefixMap, 
			Map<String, VariableInfo> allVarInfoMap) {
		
		String proj = model.split("_")[0];
		String baseBugId = model.split("_")[1];
		
		String predV0Cmd = "python run_predict.py " + proj + " " + baseBugId + " v0";
		String predExprCmd = "python run_predict.py " + proj + " " + baseBugId + " expr ";
		String predVarCmd = "python run_predict.py " + proj + " " + baseBugId + " var";
		
		
		Map<String, Integer> pos0TimeMap = getPos0TimeMap(allOriPredicates);
		
		//var features saver
		List<String> varFeatures = new ArrayList<>(DEFAULT_VAR_RES_NUM); 
		
		generateBottomUpV0Lines(varFeatures, varToFeaPrefixMap, pos0TimeMap);
		wirteBUPred(proj, baseBugId, "v0", varFeatures);
		
		Cmd.runCmd(predV0Cmd , dir);
		
		//System.out.println("FIRST VAR TIME: " + (endTime - startTime)/1000 + " s" );
		
		List<VarPredItem> vars = getSortedVarList(true, allVarInfoMap, proj, model, 0);
				
		List<ExprPredItem> allExprs = new ArrayList<>(DEFAULT_EXPR_RES_NUM);
		
		List<VarPredItem> varsForZero = vars;
		if(vars.size() > CONFIG.getVarLimit()){
			varsForZero = vars.subList(0, CONFIG.getVarLimit());
		}
		
		for(VarPredItem varItemAtZero: varsForZero){
			
			//if the variable of this position has been designated
			if(FileInvoker.designatedVars != null && !FileInvoker.designatedVars.isEmpty()) {
								
				String designatedVarName = FileInvoker.designatedVars.get(0);
				if(!varItemAtZero.getLiteral().equals(designatedVarName)) {
					continue;
				}
			}
			
			String curVarFea = varToFeaPrefixMap.get(varItemAtZero.getLiteral());
			if(curVarFea == null || curVarFea.length() == 0){
				continue;
			}
			//occpostime
			String posZeroTime = ""+(pos0TimeMap.containsKey(varItemAtZero.getLiteral()) ? pos0TimeMap.get(varItemAtZero.getLiteral()) : 0);
			
			String v0Line = StringUtil.connectMulty(del, curVarFea, posZeroTime, "?"); 
			wirteBUPred(proj, baseBugId, "expr", Arrays.asList(v0Line));
			
			Cmd.runCmd(predExprCmd , dir);
			
			//System.out.println("EXPR TIME: " + (endTime - startTime)/1000 + " s" );
			
			List<ExprPredItem> exprs = readExprCSV(fileName, proj, model);//sorted
			
			List<ExprPredItem> filtedExprs = new ArrayList<>();
			
			for(ExprPredItem exprItem: exprs){
			
//				System.out.println(exprItem);
				
				if(! ExprGenerator.isLegalExprForV0(varItemAtZero, exprItem, allOriPredicates)){
					continue;
				}
				
				String predFeature = exprItem.getExprFeature();
				Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list
				List<VarPredItem> v0List = new ArrayList<>(DEFAULT_VAR_RES_NUM);
				v0List.add(varItemAtZero);
				predVars.put(0, v0List);
				
				for(int n = 1; n < exprItem.getPositionNum(); n++){
					List<String> varFeatersAtN = new ArrayList<>(DEFAULT_VAR_RES_NUM);
					
					for(VarPredItem varItemLatter: vars){
						
						//if the variable of this position has been designated
						if(FileInvoker.designatedVars != null && !FileInvoker.designatedVars.isEmpty()) {
							if(n >= FileInvoker.designatedVars.size()) {
								break;
							}
							
							String designatedVarName = FileInvoker.designatedVars.get(n);
							if(!varItemLatter.getLiteral().equals(designatedVarName)) {
								continue;
							}
						}
						
						boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), varItemLatter.getLiteral());
						String argUsed = argued ? "true" : "false";
						
						boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, varItemLatter.getInfo(), allOriPredicates); 
						String vNfit = typeFit ? "true" : "false";
						
						String vNUsed = (varItemLatter == varItemAtZero) ? "true" : "false";

						String vNPrefix = varToFeaPrefixMap.get(varItemLatter.getLiteral());
						
						assert vNPrefix != null;
						
						int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(varItemLatter.getLiteral(), exprItem.getPred(), n, allOriPredicates);
						if(occuredTime == 0 && argued) {
							occuredTime ++;
						}
						
						String varNLine = StringUtil.connectMulty(del, vNPrefix, predFeature, argUsed, vNfit, "" + occuredTime, vNUsed, "" + n, "?");
						
						varFeatersAtN.add(varNLine);
					}
					
					wirteBUPred(proj, baseBugId, "var", varFeatersAtN);
					
					Cmd.runCmd(predVarCmd , dir);
					
					//System.out.println(varItemAtZero.getLiteral() + ": " + exprItem.getPred() + " POS " + n + " TIME: " + (endTime - startTime)/1000 + " s" );
					
					List<VarPredItem> varsAtN = getSortedVarList(true, allVarInfoMap, proj, model, n);
					predVars.put(n, varsAtN);
					
				}//for(int n = 1; n < exprItem.getPositionNum(); n++)
				
				exprItem.setPredVars(predVars);
				
				if(predVars.size() == exprItem.getPositionNum()){
					filtedExprs.add(exprItem);
				}

			}//for(ExprPredItem exprItem: exprs)
			
			allExprs.addAll(filtedExprs);
			if(allExprs.size() > 2 * DEFAULT_EXPR_RES_NUM) {//remain the top 2*DEFAULT_EXPR_RES_NUM result
				break;
			}
			
		}//for(VarPredItem varItemAtZero: vars)
		
		
		ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
		List<GenExprItem> res = generator.generateExpr();
				
		return res;
	}
	
	public static void getExprsByTopDown(String projAndBug, String srcRoot, String testRoot, String filePath, int line, int ithSuspicous) {
		String model = bugToModelMap.get(projAndBug);
		String proj = model.split("_")[0];
		
		String fileName = getFileName(filePath);
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();

		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		String path = PREDICTOR_ROOT + "/input/" + proj + "/" + model + "/expr/" + model + ".allpred.csv";
		Map<String, OriPredItem> allOriPredicates = loadAllOriPredicate(path);//TODO:: opt !!
		
		String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		List<GenExprItem> res = predTopDown(model, fileName, ctxFea, allOriPredicates, varToVarFeaMap, allVarInfoMap);
		
		String proj_Bug_ithSusp = projAndBug + "_" + ithSuspicous;
		dumpResult(proj, proj_Bug_ithSusp, res);
	}
	
	public static void getExprsByBottomUp(String projAndBug, String srcRoot, String testRoot, String filePath, int line, int ithSuspicous){
		String model = bugToModelMap.get(projAndBug);
		String proj = model.split("_")[0];
		
		String fileName = getFileName(filePath);
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		
		Map<String, String> varToFeaPrefixMap = DeduFeatureGenerator.getVarToCtxAndVarFeaPrefixMap(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		//pred to its oripreditem
		String path = PREDICTOR_ROOT + "/input/" + proj + "/" + model + "/expr/" + model + ".allpred.csv";
		Map<String, OriPredItem> allOriPredicates = loadAllOriPredicate(path);//TODO:: opt
		
		List<GenExprItem> res = predBottomUp(model, fileName, allOriPredicates, varToFeaPrefixMap, allVarInfoMap);
		
		String proj_Bug_ithSusp = projAndBug + "_" + ithSuspicous;
		
		dumpResult(proj, proj_Bug_ithSusp, res);
	}
	
	private static final Double LIMIT = Double.valueOf("0.001"); 
	
	private static void completeTreeByPredict(String model, String fileName, TreePredItem tree, String ctxFea,
			Map<String, OriPredItem> allOriPredicates, Map<String, String> varToVarFeaMap, Map<String, VariableInfo> allVarInfoMap) {
		
		String proj = model.split("_")[0];
		String baseBugId = model.split("_")[1];
		String predExprCmd = "python run_predict.py " + proj + " " + baseBugId + " expr recur";
		String predVarCmd = "python run_predict.py " + proj + " " + baseBugId + " var recur";
		
		List<TreePredItem> leafs = tree.getLeafsForCompleteTree();
		
		for(TreePredItem node: leafs) {
			String recurNodeFea = node.getFeature();
			
			String nodeLabel = "" + node.getExpandItem().getOpcode().toLabel();
			writeRecurNodePred(proj, baseBugId, "expr", Arrays.asList(ctxFea + recurNodeFea + nodeLabel + "\t?"));
			Cmd.runCmd(predExprCmd , dir);

			List<ExprPredItem> exprs = readExprCSV(fileName, proj, model);
			if(exprs.size() > CONFIG.getExprLimit()) {
				exprs = exprs.subList(0, CONFIG.getExprLimit());
			}
			
			List<ExprPredItem> allExprs = new ArrayList<>(DEFAULT_EXPR_RES_NUM);
			
			for(ExprPredItem exprItem: exprs) {
				
				if(Double.valueOf(exprItem.getScore()).compareTo(LIMIT) < 0) {
					break;
				}
				
				String predFeature = exprItem.getExprFeature();
				Map<Integer, List<VarPredItem>> predVars = new HashMap<>();//MAP: position => varitem list

				for(int n = 0; n < exprItem.getPositionNum(); n++) {
					List<String> varFeatersAtN = new ArrayList<>(DEFAULT_VAR_RES_NUM);
					
					for(Entry<String, String> entry: varToVarFeaMap.entrySet()) {
						List<String> varLineList = new ArrayList<>();
						String varFeaPrefix = entry.getValue();
						varLineList.add(ctxFea + recurNodeFea + nodeLabel); // recurNodeFea is starts with and end with 'TAB' !!
						
						varLineList.add(varFeaPrefix);
						varLineList.add(predFeature);
						
						String varName = entry.getKey();
						boolean argued = DeduConditionVisitor.usedAsParam(exprItem.getTemVarCompletPred(), varName);
						varLineList.add("" + argued); //argused
						
						VariableInfo info = allVarInfoMap.get(varName);
						if(info == null) {
							continue;
						}
						boolean typeFit = TypeUtil.isLegalVarAtPosition(exprItem.getPred(), n, info, allOriPredicates); 
						varLineList.add("" + typeFit); //argused

						int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(varName, exprItem.getPred(), n, allOriPredicates);
						if(occuredTime == 0 && argued) {
							occuredTime ++;
						}
						varLineList.add("" + occuredTime); //occuredTime

						varLineList.add("" + n); //position
						varLineList.add("?"); //putin
						String currStr = StringUtil.join(varLineList, del);
						
						varFeatersAtN.add(currStr);
					}
					writeRecurNodePred(proj, baseBugId, "var", varFeatersAtN);
					Cmd.runCmd(predVarCmd , dir);
					List<VarPredItem> varsAtN = getSortedVarList(false, allVarInfoMap, proj, model, n);
					predVars.put(n, varsAtN);
					
				}// END for(int n = 0; n < exprItem.getPositionNum(); n++)
				
				exprItem.setPredVars(predVars);
				if(predVars.size() == exprItem.getPositionNum()){
					allExprs.add(exprItem);
				}
				
			}// END for(ExprPredItem exprItem: exprs)
			
			ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
			List<GenExprItem> res = generator.generateExpr();
			
			if(res.size() > CONFIG.getExprLimit()) {
				res = res.subList(0, CONFIG.getExprLimit());
			}
			node.setExpressions(res);
		}// END for(TreePredItem node: leafs)
	}
	
	public static void getConditionByRecur(String projAndBug, String srcRoot, String testRoot, String filePath, int line, int ithSuspicous) {
		String model = bugToModelMap.get(projAndBug);
		String proj = model.split("_")[0];
		
		String fileName = getFileName(filePath);
		
		DeduFeatureGenerator.getHitNode(projAndBug, model, srcRoot, testRoot, filePath, line);
		Map<String, VariableInfo> allVarInfoMap = DeduFeatureGenerator.getAllVariablesMap();
		Map<String, String> varToVarFeaMap = DeduFeatureGenerator.getVarToVarFeatureMap(projAndBug, model, srcRoot, testRoot, filePath, line);

		String path = PREDICTOR_ROOT + "/input/" + proj + "/" + model + "/expr/" + model + ".allpred.csv";
		Map<String, OriPredItem> allOriPredicates = loadAllOriPredicate(path);//TODO:: opt !!
		
		String ctxFea = DeduFeatureGenerator.generateContextFeature(projAndBug, model, srcRoot, testRoot, filePath, line);
		
		List<TreePredItem> trees = predTrees(model, ctxFea);
		
		List<ConcretTree> results = new ArrayList<>(); 

		for(TreePredItem tree: trees) {
			completeTreeByPredict(model, fileName, tree, ctxFea, allOriPredicates, varToVarFeaMap, allVarInfoMap);
			
			//tree.remainHighScore();
			
			int preSize = results.size();
			List<ConcretTree> concretTrees = ConcretTree.getConcreteTrees(tree);
			
			results.addAll(concretTrees);
			
			if(preSize != 0 && results.size() > preSize) {// added new items, need re-sort
				// remain top EXPR_BEAM_NUM at each step
				Collections.sort(results, Collections.reverseOrder());
				if(results.size() > CONFIG.getExprLimit()) {
					results = results.subList(0, CONFIG.getExprLimit());
				}
			}
		} // END for(TreePredItem tree: trees)
		
		List<String> lines = ConcretTree.concretTreeToGenExprItem(results);

		String proj_Bug_ithSusp = projAndBug + "_" + ithSuspicous;
		dumpPlainResult(proj, proj_Bug_ithSusp, lines);
	}
	
	
	private static List<VarPredItem> getSortedVarList(boolean isBottomUp, Map<String, VariableInfo> allVarInfoMap, String proj, String model, int pos){
		List<VarPredItem> vars = readVarCSV(isBottomUp, allVarInfoMap, proj, model, pos);
		Collections.sort(vars, new Comparator<VarPredItem>(){
			@Override
			public int compare(VarPredItem o1, VarPredItem o2) {
				return o2.compareTo(o1);
			}
			
		});
		return vars;
	}
		
	private static void errorExit(String msg){
		System.err.println("PREDICTOR ERROR!");
		System.err.println(msg);
//		System.exit(-1);
		throw new Error("ERROR EXIT!");
	}
	
	public static void main(String[] args){
		/* 
		 * args0: Project_BugID
		 * args1: srcRoot
		 * args2: testRoot
		 * args3: filePath
		 * args4: line
		 * args5: i'th suspicious
		 */
		if(args.length != 6){
			errorExit("ERROR ARGS NUM: " + args.length);
		}
		
//		designatedVars = new ArrayList<String>();
//		designatedVars.add("y");

//		predict(args[0].toLowerCase(), args[1], args[2], args[3], new Integer(args[4]), new Integer(args[5]));
		predict("time_11", 
				"/home/nightwish/workspace/defects4j/src/time/time_11_buggy/src/main/java/",
				"/home/nightwish/workspace/defects4j/src/time/time_11_buggy/src/test/java/",
				"org/joda/time/DateTime.java",
				1388,
				9999);
	}
	
	public static List<String> designatedVars;
	
	public static void predict(String bugName, String srcRoot, String testRoot, String filePath, int line, int iThSuspect, List<String> designatedVars) {
		FileInvoker.designatedVars = designatedVars;
		predict(bugName, srcRoot, testRoot, filePath, line, iThSuspect);
		FileInvoker.designatedVars = null;
	}
	
	public static void predict(String bugName, String srcRoot, String testRoot, String filePath, int line, int iThSuspect) {
		double startTime = System.currentTimeMillis();
		
		System.out.println("\n>>>> ARGS FOR Invoker:");
		System.out.println(bugName);
		System.out.println(srcRoot);
		System.out.println(testRoot);
		System.out.println(filePath);
		System.out.println(line);
		System.out.println(iThSuspect);
		System.out.println(">>>>");
		
//		getExprsByTopDown(bugName.toLowerCase(), srcRoot, testRoot, filePath, line, iThSuspect);
//		getExprsByBottomUp(bugName.toLowerCase(), srcRoot, testRoot, filePath, line, iThSuspect);
		getConditionByRecur(bugName.toLowerCase(), srcRoot, testRoot, filePath, line, iThSuspect);
		
		System.out.println("SUCCESSFUL PREDICTED!!");
		double endTime = System.currentTimeMillis();
		System.out.println("INVOKER TIME: " + (endTime - startTime)/1000 + " s" );
		
	}

}
