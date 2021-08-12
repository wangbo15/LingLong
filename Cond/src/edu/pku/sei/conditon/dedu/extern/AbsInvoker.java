package edu.pku.sei.conditon.dedu.extern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.pred.ExprGenerator;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.GenExprItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.dedu.writer.AllPredWriter;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.StringUtil;

public abstract class AbsInvoker {
	public static final String PREDICTOR_ROOT = DeduMain.USER_HOME + "/workspace/eclipse/Condition/python/";
	
	protected static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	public static Map<String, String> bugToModelMap = new HashMap<>();
	
	static{
		String subject_ = "math_";
		for(int i = 1; i <= 12; i++){//2013
			bugToModelMap.put(subject_ + i, "math_12");//
		}
		for(int i = 13; i <= 37; i++){//2012
			bugToModelMap.put(subject_ + i, "math_37");
		}
		for(int i = 38; i <= 59; i++){//2011
			bugToModelMap.put(subject_ + i, "math_59");
		}
		for(int i = 60; i <= 67; i++){//2010, 7-12
			bugToModelMap.put(subject_ + i, "math_67");
		}
		for(int i = 68; i <= 75; i++){//2010, 1-6
			bugToModelMap.put(subject_ + i, "math_75");
		}
		for(int i = 76; i <= 84; i++){//2009, 7-12
			bugToModelMap.put(subject_ + i, "math_84");
		}
		for(int i = 85; i <= 94; i++){//2009, 1-6
			bugToModelMap.put(subject_ + i, "math_94");
		}
		for(int i = 95; i <= 102; i++){//2008
			bugToModelMap.put(subject_ + i, "math_102");
		}
		for(int i = 103; i <= 104; i++){//2007
			bugToModelMap.put(subject_ + i, "math_104");
		}
		for(int i = 105; i <= 106; i++){//2006
			bugToModelMap.put(subject_ + i, "math_106");
		}
		
		
		subject_ = "chart_";
		for(int i = 1; i <= 4; i++){//2009
			bugToModelMap.put(subject_ + i, "chart_4");
		}
		for(int i = 5; i <= 16; i++){//2008
			bugToModelMap.put(subject_ + i, "chart_16");
		}

		for(int i = 17; i <= 26; i++){//2007
			bugToModelMap.put(subject_ + i, "chart_26");
		}

        subject_ = "time_";
		for(int i = 1; i <= 11; i++){//2013
			bugToModelMap.put(subject_ + i, "time_11");
		}
		for(int i = 12; i <= 17; i++){//2012
			bugToModelMap.put(subject_ + i, "time_17");
		}
        for(int i = 18; i <= 24; i++){//2011
            bugToModelMap.put(subject_ + i, "time_24");
        }
        for(int i = 25; i <= 27; i++){//2010
            bugToModelMap.put(subject_ + i, "time_27");
        }
        
        subject_ = "lang_";
		for(int i = 1; i <= 5; i++){//2013
			bugToModelMap.put(subject_ + i, "lang_5");
		}
		for(int i = 6; i <= 14; i++){//2012
			bugToModelMap.put(subject_ + i, "lang_14");
		}
        for(int i = 15; i <= 24; i++){//2011
            bugToModelMap.put(subject_ + i, "lang_24");
        }
        for(int i = 25; i <= 35; i++){//2010
            bugToModelMap.put(subject_ + i, "lang_35");
        }
        for(int i = 36; i <= 43; i++){//2009
            bugToModelMap.put(subject_ + i, "lang_43");
        }
        for(int i = 44; i <= 48; i++){//2008
            bugToModelMap.put(subject_ + i, "lang_48");
        }
        for(int i = 49; i <= 55; i++){//2007
            bugToModelMap.put(subject_ + i, "lang_55");
        }
        for(int i = 56; i <= 65; i++){//2006
            bugToModelMap.put(subject_ + i, "lang_65");
        }
        
        // for accumulo
        // 2011
        bugToModelMap.put("accumulo_151", "accumulo_151");
        // 2012
        bugToModelMap.put("accumulo_907", "accumulo_151");
        // 2013
        bugToModelMap.put("accumulo_1544", "accumulo_151");
        // 2014
        bugToModelMap.put("accumulo_1661", "accumulo_151");
        bugToModelMap.put("accumulo_2659", "accumulo_151");
        bugToModelMap.put("accumulo_2713", "accumulo_151");
        bugToModelMap.put("accumulo_2748", "accumulo_151");
        bugToModelMap.put("accumulo_3150", "accumulo_151");
        bugToModelMap.put("accumulo_3218", "accumulo_151");
        bugToModelMap.put("accumulo_3229", "accumulo_151");
        // 2015
        bugToModelMap.put("accumulo_3746", "accumulo_151");
        bugToModelMap.put("accumulo_3897", "accumulo_151");
        bugToModelMap.put("accumulo_3945", "accumulo_151");
        bugToModelMap.put("accumulo_4029", "accumulo_151");
        // 2016
        bugToModelMap.put("accumulo_4098", "accumulo_151");
        bugToModelMap.put("accumulo_4138", "accumulo_151");
        
        // for camel
        // 2010
        bugToModelMap.put("camel_3388", "camel_3388");
        // 2011
        bugToModelMap.put("camel_3690", "camel_3690");
        bugToModelMap.put("camel_4388", "camel_3690");
        bugToModelMap.put("camel_4474", "camel_3690");
        bugToModelMap.put("camel_4542", "camel_3690");
        bugToModelMap.put("camel_4682", "camel_3690");
        // 2012
        bugToModelMap.put("camel_5570", "camel_5570");
        bugToModelMap.put("camel_5704", "camel_5570");
        bugToModelMap.put("camel_5707", "camel_5570");
        bugToModelMap.put("camel_5720", "camel_5570");
        // 2013
        bugToModelMap.put("camel_6987", "camel_6987");
        bugToModelMap.put("camel_7016", "camel_7016");
        // 2014
        bugToModelMap.put("camel_7130", "camel_7130");
        bugToModelMap.put("camel_7125", "camel_7130");
        bugToModelMap.put("camel_7209", "camel_7130");
        bugToModelMap.put("camel_7241", "camel_7130");
        bugToModelMap.put("camel_7344", "camel_7130");
        bugToModelMap.put("camel_7359", "camel_7130");
        bugToModelMap.put("camel_7459", "camel_7130");
        bugToModelMap.put("camel_7611", "camel_7130");
        bugToModelMap.put("camel_7448", "camel_7130");
        bugToModelMap.put("camel_7795", "camel_7130");
        bugToModelMap.put("camel_7883", "camel_7130");
        bugToModelMap.put("camel_7990", "camel_7130");
        bugToModelMap.put("camel_8081", "camel_7130");
        bugToModelMap.put("camel_8106", "camel_7130");
        // 2015
        bugToModelMap.put("camel_8584", "camel_8584");
        bugToModelMap.put("camel_8592", "camel_8584");
        bugToModelMap.put("camel_9217", "camel_8584");
        bugToModelMap.put("camel_9243", "camel_8584");
        bugToModelMap.put("camel_9238", "camel_8584");
        bugToModelMap.put("camel_9269", "camel_8584");
        bugToModelMap.put("camel_9340", "camel_8584");

	}		
	protected Map<String, OriPredItem> allOriPredicates;
	protected Map<String, Integer> pos0TimeMap;
	
	protected static int predictTime = 0;
	protected static int varPredictTime = 0;
	protected static int exprPredictTime = 0;
	protected static int recurNodePredictTime = 0;
	
	
	public AbsInvoker(Map<String, OriPredItem> allOriPredicates, Map<String, Integer> pos0TimeMap) {
		this.allOriPredicates = allOriPredicates;
		this.pos0TimeMap = pos0TimeMap;
	}
	
	public static int getPredictTime() {
		return predictTime;
	}
	
	public static int getVarPredictTime() {
		return varPredictTime;
	}
	
	public static int getExprPredictTime() {
		return exprPredictTime;
	}
	
	public static int getRecurNodePredictTime() {
		return recurNodePredictTime;
	}
	
	public abstract void prepare();
	
	public abstract void finish();
	
	public abstract List<ExprPredItem> predictExpr(String direction, String featureLine);
	
	public abstract List<VarPredItem> predictVar(String direction, int n, List<String> featureLines, Map<String, VariableInfo> allVarInfoMap);
	
	public abstract List<ExprPredItem> predictTDExprs(String ctxFea);
	
	public abstract List<VarPredItem> predictTDVars(int n, List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap);
	
	public abstract List<VarPredItem> predictBUV0(Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToFeaPrefixMap);

	public abstract List<ExprPredItem> predictBUExprs(String v0Iter, String curVarFea);
	
	public abstract List<VarPredItem> predictBUVars(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n);
	
	public abstract List<RecurNodePredItem> predictRecurNodes(String featureLine);
	
	public abstract List<ExprPredItem> predictRecurExprs(String featureLine);

	public abstract List<VarPredItem> predictRecurVar(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n);	

	public abstract List<VarPredItem> predictRCBUV0(Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToFeaPrefixMap);
	
	public abstract List<VarPredItem> predictRCBUV1(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n);

	public abstract List<ExprPredItem> predictRCBUE0(String v0Feature);
	
	public abstract List<ExprPredItem> predictRCBUE1(String v0Feature);
	
	public abstract List<RecurNodePredItem> predictRCBUR0(String featureLine);
	
	public abstract List<RecurNodePredItem> predictRCBUR1(String featureLine);
	
	private static Pair<String, Map<String, OriPredItem>> path2AllOriPredCache = null;
	
	public static Map<String, OriPredItem> loadAllOriPredicate(String path){
		if(path2AllOriPredCache != null && path2AllOriPredCache.getFirst().equals(path)) {
			return path2AllOriPredCache.getSecond();
		}
		
		File file = new File(path);
		
		assert file.exists(): "ORIGIAL FILE IS NOT FOUND: " + file.getAbsolutePath();
		
		FileReader fReader = null;
		BufferedReader bReader = null;
		
		Map<String, OriPredItem> result = new HashMap<>();
		
		try {
			fReader = new FileReader(file);
			bReader = new BufferedReader(fReader);
			String line = null;
			
			String header = AllPredWriter.getAllPredHeader();
			while ((line = bReader.readLine()) != null) {
				
				if(line.equals(header)) {
					continue;
				}
				
				String[] columns = line.split("\t");
				
				assert columns.length == header.split("\t").length;
				
				//int id = Integer.valueOf(columns[0]);
				
				String pred = columns[1].replaceAll("\\s", "");//repmove all spaces
				
				OriPredItem curItem = null;
				
				int slopNum = new Integer(columns[2]);
				String[] types = StringUtil.parseTypeListString(columns[3]);
				String[] vars = StringUtil.parseListString(columns[4]);
				
				assert types.length == vars.length;
				
				if(result.containsKey(pred)){
					curItem = result.get(pred);
					assert curItem.getSlopNum() == slopNum;
				}else{
					curItem = new OriPredItem(pred, slopNum);
					result.put(pred, curItem);
				}
				
				for(int i = 0; i < types.length; i++){
					Set<String> typesAtPos = null;
					if(curItem.getPosToTypesMap().containsKey(i)){
						typesAtPos = curItem.getPosToTypesMap().get(i);
					}else{
						typesAtPos = new HashSet<String>();
						curItem.getPosToTypesMap().put(i, typesAtPos);
					}
					typesAtPos.add(types[i].trim());
				}
				
				for(int i = 0; i < vars.length; i++) {
					Map<String, Integer> currMap = null;
					if(curItem.getPosToOccurredVarTimesMap().containsKey(i)){
						currMap = curItem.getPosToOccurredVarTimesMap().get(i);
					}else{
						currMap = new HashMap<>();
						curItem.getPosToOccurredVarTimesMap().put(i, currMap);
					}
					
					String currVar = vars[i].trim();
					int time = 0;
					if(currMap.containsKey(currVar)) {
						time = currMap.get(currVar);
					}
					currMap.put(currVar, time + 1);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			FileUtil.closeInputStream(fReader, bReader);
		}
		
		path2AllOriPredCache = new Pair<String, Map<String, OriPredItem>>(path, result);
		
		return result;
	}
	
	public static Map<String, Integer> getPos0TimeMap(Map<String, OriPredItem> allOriPredicates){
		Map<String, Integer> result = new HashMap<>();
		
		for(OriPredItem oriItem : allOriPredicates.values()) {
			Map<String, Integer> currPosZeroMap = oriItem.getPosToOccurredVarTimesMap().get(0);
			//assert currPosZeroMap.size() == 1; //???
			for(Entry<String, Integer> entry : currPosZeroMap.entrySet()) {
				String key = entry.getKey();
				int time = 0;
				if(result.containsKey(key)) {
					time = result.get(key);
				}
				time += entry.getValue();
				result.put(key, time);
			}
		}
		return result;
	}
	
	public static String getOutputFilePath(String proj, String proj_Bug_ithSusp){
		String output = PREDICTOR_ROOT + "/output/" + proj + "/res/";
		File f = new File(output);
		if(f.exists() == false){
			f.mkdirs();
		}
		return output + proj_Bug_ithSusp.toLowerCase() + ".res.csv";
	}
	
	protected static String getFileName(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("/"));
		fileName = fileName.substring(0, fileName.length() - 5);	//remove '.java'
		return fileName;
	}
	
	protected void generateAndDump(String proj, String projAndBug, int ithSus, List<ExprPredItem> allExprs, 
			Map<String, OriPredItem> allOriPredicates) {
		ExprGenerator generator = new ExprGenerator(allExprs, allOriPredicates);
		List<GenExprItem> res = generator.generateExpr();
		String proj_Bug_ithSusp = projAndBug + "_" + ithSus;
		dumpResult(proj, proj_Bug_ithSusp, res);
	}
	
	protected static void dumpResult(String proj, String proj_Bug_ithSusp, List<GenExprItem> res){
		try {
			String resPath = getOutputFilePath(proj, proj_Bug_ithSusp);
			FileWriter writer = new FileWriter(resPath);
			BufferedWriter bf = new BufferedWriter(writer);
			
			Map<String, Integer> upperBoundMap = new HashMap<>();
			
			Set<String> outputed = new HashSet<>(200);
			for(GenExprItem item : res){
				String pred = item.getExpr().getPred();
				
				if(upperBoundMap.containsKey(pred)){
					upperBoundMap.put(pred, upperBoundMap.get(pred) + 1);
				}else{
					upperBoundMap.put(pred, 1);
				}
				if(upperBoundMap.get(pred) > CONFIG.getPredResLimits()){
					continue;
				}
				
				if(outputed.contains(item.getGeneratedExpr())){
					continue;
				}
				outputed.add(item.getGeneratedExpr());
				
//				if(item.getGeneratedExpr().contains("$")){
//					continue;
//				}
				
				bf.write(item.getGeneratedExpr() + "\t" + item.getScore().toPlainString());
				bf.newLine();
			}
			writer.flush();
			bf.flush();
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void dumpPlainResult(String proj, String proj_Bug_ithSusp, List<String> lines){
		String resPath = getOutputFilePath(proj, proj_Bug_ithSusp);
		StringBuffer sb = new StringBuffer();
		for(String line : lines) {
			sb.append(line.trim() + "\n");
		}
		FileUtil.writeStringToFile(resPath, sb.toString(), false);	
	}

}
