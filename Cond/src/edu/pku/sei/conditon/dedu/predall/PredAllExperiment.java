package edu.pku.sei.conditon.dedu.predall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;

import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.feature.AbstractVector;
import edu.pku.sei.conditon.dedu.feature.TreeVector;
import edu.pku.sei.conditon.dedu.pf.ProgramPoint;
import edu.pku.sei.conditon.dedu.pred.TreePredItem;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.SearchMethod;
import edu.pku.sei.conditon.dedu.real.RealBugExpriment;
import edu.pku.sei.conditon.util.DateUtil;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.config.Subject;

public class PredAllExperiment {
	
	public static Set<Integer> condIdTestSet;
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	private static final boolean RECUR = CONFIG.isRecur(); 
	
	private static final boolean BOTTOM_UP = CONFIG.isBottomUp();
	
	public static final String REPORT_PATH = DeduMain.USER_HOME + "/cond-report.txt";
	public static final String ALL_RES_POSTFIX = "allres.txt";
	public static final String RANKING_POSTFIX = "ranking.csv";
	public static final String TIME_POSTFIX = "time.txt";
	public static final String PRED_SEQ_POSTFIX = "seq.csv";
	
	private static Date startDate = new Date();
	
	private static String prefix;
	
	static {
		prefix = "";
		if(RECUR) {
			if(BOTTOM_UP) {
				prefix += "recurbu.";
			} else {
				prefix += "recur.";
			}
		} else {
			if (BOTTOM_UP){
				prefix +=  "bu.";
			} else {
				prefix += "td.";
			}
		}
		prefix += CONFIG.getSearchMethod().toString().toLowerCase();
		if(CONFIG.getSearchMethod() == SearchMethod.BEAM) {
			prefix = prefix + "." + CONFIG.getBeamSearchResultLimits() + "." + CONFIG.getBeamSearchLimits();
		} else if(CONFIG.getSearchMethod() == SearchMethod.DIJKSTRA) {
			prefix = prefix + "." + CONFIG.getDijkstraSearchLimits();
		}
	}
	
	public static void main(String[] args) {
		String proj = args[0];
		int bugid = Integer.valueOf(args[1]);
		if(CONFIG.isPredAllPreparing()) {
			prepare(proj, bugid);
			System.out.println("-------- PREPARE FINISH --------");
		}else {
			Set<String> activated = new HashSet<>();
//			String ids = "34, 35, 140, 303, 458, 478, 496, 505, 553, 627, 699, "
//					+ "873, 956, 1008, 1042, 1046, 1189, 1192, 1382, 1383, 1409, 1499, 1727, 1756, 1889";
//			for(String str : ids.split(",")) {
//				activated.add(str.trim());
//			}
			
			String bugName = proj + "_" + bugid;
			
			
			
			predAll(bugName, activated);
		}
	}
	
	public static String getAllPredPath(String bugName) {
		if(RECUR) {
			return DeduMain.OUTPUT_ROOT + bugName + ".full.csv";
		} else {
			return DeduMain.OUTPUT_ROOT + bugName + ".allpred.csv";
		}
	}
	
	public static String getRecordFilePath(String bugName, String postFix) {
		String path = DeduMain.USER_HOME + "/Documents/predall/" + bugName + "/" + prefix + "-";
		path += DateUtil.getFormatedDateForFileName(startDate) + "-" + postFix;
		File f = new File(path);
		File p = f.getParentFile();
		if(!p.exists()) {
			p.mkdirs();
		}
		assert p.isDirectory();
		return path;
	}
	
	private static void modifyAndBackupFile(String path) {
		FileUtil.copyFile(path, path + ".bk");
		List<String> lines = FileUtil.readFileToStringList(path);
		List<String> tobeRemoved = new ArrayList<>();
		for(int i = 1; i < lines.size(); i++) { // skip header
			String line = lines.get(i);
			int id = Integer.valueOf(line.split("\t")[0]);
			if(condIdTestSet.contains(id)) {
				tobeRemoved.add(line);
			}
		}
		lines.removeAll(tobeRemoved);
		FileUtil.writeStringListToFile(path, lines, false);
	}
	
	/**
	 * @param proj
	 * @param bugid
	 */
	private static void modifyAndBackupAllTrainingData(String proj, int bugid) {
		String bugName = proj + "_" + bugid;
		List<String> paths = new ArrayList<>();
		
		String prefix = DeduMain.OUTPUT_ROOT + bugName;
		
		String recur = prefix + ".recur.csv";
		String recurExpr = prefix + ".recur.expr.csv";
		String recurVar = prefix + ".recur.var.csv";

		String buExpr = prefix + ".expr.csv";
		String buV0 = prefix + ".v0.csv";
		String buVar = prefix + ".var.csv";

		String tdExpr = prefix + ".topdown.expr.csv";
		String tdVar = prefix + ".topdown.var.csv";
		
		String rbV0 = prefix + ".recurbu.v0.csv";
		String rbE0 = prefix + ".recurbu.e0.csv";
		String rbR0 = prefix + ".recurbu.r0.csv";
		String rbR1 = prefix + ".recurbu.r1.csv";
		String rbE1 = prefix + ".recurbu.e1.csv";
		String rbV1 = prefix + ".recurbu.v1.csv";

		paths.add(buExpr);
		paths.add(buV0);
		paths.add(buVar);
		
		paths.add(tdExpr);
		paths.add(tdVar);
		
		paths.add(recur);
		paths.add(recurExpr);
		paths.add(recurVar);
		
		paths.add(rbV0);
		paths.add(rbE0);
		paths.add(rbR0);
		paths.add(rbR1);
		paths.add(rbE1);
		paths.add(rbV1);

		for(String path: paths) {
			File f = new File(path);
			assert f.exists();
			
			modifyAndBackupFile(path);
		}
	}
	
	private static void prepare(String proj, int bugid) {
		
		Subject subject = DeduMain.processProcessForPredAll(proj, bugid);
		
		List<Integer> allIdList = getAllIdList(proj, bugid);
		
		List<Integer> selectedCondId = generateRandom(allIdList, 10);
		
		recordTestSet(subject, selectedCondId);
		
		condIdTestSet = new HashSet<>(selectedCondId);
		
		modifyAndBackupAllTrainingData(proj, bugid);
				
		writePredCmds(subject);
		
		System.out.println("ALL ID SIZE: " + allIdList.size());
		System.out.println("TEST ID SIZE: " + selectedCondId.size());
	}
	
	private static List<Integer> getAllIdList(String proj, int bugid) {
		List<Integer> allIdList = new ArrayList<>(5000);
		String path = DeduMain.OUTPUT_ROOT + proj + "_" + bugid + ".full.csv";
		try {
			CsvReader reader = new CsvReader(path, '\t');
			reader.readHeaders();
			while(reader.readRecord()){
				int id = Integer.valueOf(reader.get(0));
				String fileName = reader.get(1);
				if(fileName.contains("Test")) {
					continue;
				}
				
				if(!allIdList.contains(id)) {
					allIdList.add(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allIdList;
	}
	
	private static List<Integer> generateRandom(List<Integer> allIdList, int fraction) {
		assert fraction > 1 && fraction <= 10;
		List<Integer> selected = new ArrayList<>(500);
		int size = allIdList.size();
		Set<Integer> testSet = new HashSet<>();
		PredAllExperiment.randomSet(0, size, size/fraction, testSet);
		for(int index : testSet) {
			selected.add(allIdList.get(index));
		}
		Collections.sort(selected);
//		for(int i : selected) {
//			System.out.println(i);
//		}
		return selected;
	}
	
	private static void randomSet(int min, int max, int n, Set<Integer> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num);
		}
		int setSize = set.size();
		if (setSize < n) {
			randomSet(min, max, n - setSize, set);		
		}
	}
	
	private static void writePredCmds(Subject subject) {
		String subName = subject.getName();
		int bugId = subject.getId();
		String nameAndId = subject.getName() + "_" + bugId;

		String srcRoot = DeduMain.DEFECTS4J_ROOT + subName + "/" + nameAndId + "_buggy" + subject.getPath();
		String testRoot = DeduMain.DEFECTS4J_ROOT + subName + "/" + nameAndId + "_buggy" + subject.getTestPath();
		
		File folder = new File(DeduMain.OUTPUT_ROOT + "real/");
		if(folder.exists() == false) {
			folder.mkdirs();
		}
		
		assert !DeduConditionVisitor.plainCondVec.isEmpty();
		assert !DeduConditionVisitor.recurTreeList.isEmpty();
		
		List<String> lines = new ArrayList<>();
//		for(CondVector vec: DeduConditionVisitor.plainCondVec) {
//			if(skip(vec)) {
//				continue;
//			}
//			String line = getCmdByVector(vec, nameAndId, srcRoot, testRoot);
//			lines.add(line);
//		}
//		
//		String plainPath = folder.getAbsolutePath() + "/" + nameAndId + ".cmd.txt";
//		FileUtil.writeStringListToFile(plainPath, lines, false);
		
		lines = new ArrayList<>();
		for(TreeVector vec: DeduConditionVisitor.recurTreeList) {
			if(skip(vec)) {
				continue;
			}
			String line = getCmdByVector(vec, nameAndId, srcRoot, testRoot);
			lines.add(line);
		}
		
		String recurPath = folder.getAbsolutePath() + "/" + nameAndId + ".recur.cmd.txt";
		FileUtil.writeStringListToFile(recurPath, lines, false);
	}
	
	private static boolean skip(AbstractVector vec) {
		if(!condIdTestSet.contains(vec.getId())){
			return true;
		}
		//OMIT
//		if(vec.getContextFeature().getFileName().contains("Test")){
//			return true;
//		}
		return false;
	}
	
	private static String getCmdByVector(AbstractVector vec, String nameAndId, String srcRoot, String testRoot) {
		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(nameAndId);
		cmdLine.add(srcRoot);
		cmdLine.add(testRoot);
		
		String full = vec.getContextFeature().getFile().getAbsolutePath();
		String relative = full.substring(srcRoot.length() + 1);
		cmdLine.add(relative);

		cmdLine.add("" + vec.getLine());
		
		cmdLine.add("" + vec.getId());
		
		String line = StringUtil.join(cmdLine, " ");
		return line;
	}
	
	public static Map<String, String> loadOriResForRecur(String path) {
		List<String> allPreds = FileUtil.readFileToStringList(path);
		
		Map<String, String> map = new HashMap<>();
		for(String s: allPreds) {
			String[] items = s.split("\t");
			String key = items[0];// id
			String val = items[3];// oracle
			val = processExpr(val);
			map.put(key, val);
		}
		return map;
	} 
	
	public static Map<String, String> loadOriResForNonRecur(String path) {
		List<String> allPreds = FileUtil.readFileToStringList(path);
		
		Map<String, String> map = new HashMap<>();
		for(String s: allPreds) {
			String[] items = s.split("\t");
			String key = items[0];// id
			String val = items[5];// oracle
			val = processExpr(val);
			
			if(map.containsKey(key)) {
				if(val.contains("&&") || val.contains("||") || val.contains("!")) {
					//update
					map.put(key, val);
				}
			}else {
				map.put(key, val);
			}
		}
		return map;
	}
	
	private static void recordTestSet(Subject sub, List<Integer> idList) {
		StringBuffer sb = new StringBuffer();
		for(int i : idList) {
			sb.append(i);
			sb.append("\n");
		}
		String path = DeduMain.OUTPUT_ROOT + "/real/" + sub.getName() + "_" + sub.getId() + ".id.txt";
		File file = new File(path);
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		FileUtil.writeStringToFile(path, sb.toString(), false);
	}
	
	public static void predAll(String bugName, Set<String> activated) {
		String recordFilePath = getRecordFilePath(bugName, RANKING_POSTFIX);
		String allpredPath = getAllPredPath(bugName);
		String cmdPath = DeduMain.OUTPUT_ROOT + "real/"+ bugName + ".recur.cmd.txt";
		
		FileUtil.writeStringToFile(recordFilePath, "calender\tcondid\ttime\trank\tori\n", false);
		
		if(CONFIG.isDebug()) {
			String recordAllRes = getRecordFilePath(bugName, ALL_RES_POSTFIX);
			FileUtil.writeStringToFile(recordAllRes, "", false);
			String predSeq = getRecordFilePath(bugName, PRED_SEQ_POSTFIX);
			FileUtil.writeStringToFile(predSeq, "id\toracle\tranking\tscore\ts0\tr0\tp0\ts1\tr1\tp1\ts2\tr2\tp2\n", false);
		}
		
		Map<String, String> oraclesMap;
		if(RECUR) {
			oraclesMap = loadOriResForRecur(allpredPath);
		}else{
			oraclesMap = loadOriResForNonRecur(allpredPath);
		}
		List<String> cmds = FileUtil.readFileToStringList(cmdPath);
		
		System.out.println(bugName + " CMDS NUM: " + cmds.size() + ", " + CONFIG.dumpOrder());
		System.out.println("----------------------------------------------------------------");

		int top1 = 0, top5 = 0, top10 = 0, top25 = 0, top50 = 0, top100 = 0, top200 = 0, top400 = 0;
		int complex = 0, complexHit = 0, hitSum = 0;
		
		long totalCompileTime = 0;
		long totalCompileFailingTime = 0;
		long startAll = System.currentTimeMillis();
		
		try {
			for(int cmdIdx = 0; cmdIdx < cmds.size(); cmdIdx++){
				
				String str = cmds.get(cmdIdx);
				String[] argsForInvoker = str.split(" ");
				
				String id = argsForInvoker[argsForInvoker.length - 1];
				
				if(!activated.isEmpty() && !activated.contains(id)) {
					continue;
				}
				
				String oracle = oraclesMap.get(id);
				if(oracle == null) {
					continue;
				}
				System.out.println("\n>>>>>>>>>> " + id + " >>>>>>>>>>\t" + oracle);
				
				long start = System.currentTimeMillis();
				
				// the main entrance
				PredAllResult result = RealBugExpriment.loadPredRes(argsForInvoker, oracle);
				
				long end = System.currentTimeMillis();
				
				totalCompileTime += result.getCompileTime();
				totalCompileFailingTime += result.getCompileFailingTime();
				
				int hit = getHit(result);
				if(hit >= 0) {
					hitSum += hit;
				}
				if(hit == 0) {
					top1++;
				}
				if(hit >= 0 && hit < 5) {
					top5++;
				}
				if(hit >= 0 && hit < 10) {
					top10++;
				}
				if(hit >= 0 && hit < 25) {
					top25++;
				}
				if(hit >= 0 && hit < 50) {
					top50++;
				}
				if(hit >= 0 && hit < 100) {
					top100++;
				}
				if(hit >= 0 && hit < 200) {
					top200++;
				}
				if(hit >= 0 && hit < 400) {
					top400++;
					
					if(oracle.contains("||") || oracle.contains("&&") || (oracle.contains("!") && !oracle.contains("!="))) {
						complexHit++;
					}
				}
				
				if(oracle.contains("||") || oracle.contains("&&") || (oracle.contains("!") && !oracle.contains("!="))) {
					complex++;
				}
				
				long time = end - start;
				StringBuffer recordMsg = new StringBuffer();
				recordMsg.append("[" + DateUtil.getFormatedCurrDateForFileName() + "]\t");
				recordMsg.append(id + "\t" + time + '\t' + hit + "\t" + oracle + "\n");
				
				FileUtil.writeStringToFile(recordFilePath, recordMsg.toString(), true);
				
				if(CONFIG.isDebug()) {
					recordPredResult(bugName, id, result);
				}
			
			} // end for each cmd
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		long endAll = System.currentTimeMillis();
		
		int all = cmds.size();
		
		StringBuffer sb = new StringBuffer(CONFIG.getDumpStr());
		int limit = 200;
		if(CONFIG.getSearchMethod() == SearchMethod.BEAM) {
			limit = CONFIG.getBeamSearchResultLimits();
		} else if(CONFIG.getSearchMethod() == SearchMethod.DIJKSTRA) {
			limit = CONFIG.getDijkstraSearchLimits();
		} else if(CONFIG.getSearchMethod() == SearchMethod.GREEDY_BEAM) {
			limit = CONFIG.getGreedyBeamSearchLimits();
		}
		
		sb.append("Top 1: " + top1 + " / " + all + " = " + ((double) top1)/all + "\n");
		sb.append("Top 5: " + top5 + " / " + all + " = " + ((double) top5)/all + "\n");
		if(limit >= 10)
			sb.append("Top 10: " + top10 + " / " + all + " = " + ((double) top10)/all + "\n");
		if(limit >= 25)
			sb.append("Top 25: " + top25 + " / " + all + " = " + ((double) top25)/all + "\n");
		if(limit >= 50)
			sb.append("Top 50: " + top50 + " / " + all + " = " + ((double) top50)/all + "\n");
		if(limit >= 100)	
			sb.append("Top 100: " + top100 + " / " + all + " = " + ((double) top100)/all + "\n");
		if(limit >= 200)
			sb.append("Top 200: " + top200 + " / " + all + " = " + ((double) top200)/all + "\n");
		if(limit >= 400)
			sb.append("Top 400: " + top400 + " / " + all + " = " + ((double) top400)/all + "\n");

		sb.append("Top AVG: " + hitSum + " / " + all + " = " + ((double) hitSum)/all + "\n");
		sb.append("Complex Hit: " + complexHit + " / " + complex + " = " + ((double) complexHit)/complex + "\n");
		
		if(CONFIG.isRecur()) {
			sb.append("RECUR NODE PREDICTING TIME: " + AbsInvoker.getRecurNodePredictTime() + "\n");
		}
		
		sb.append("EXPR PREDICTING TIME: " + AbsInvoker.getExprPredictTime() + "\n");
		sb.append("VAR PREDICTING TIME: " + AbsInvoker.getVarPredictTime() + "\n");
		sb.append("ALL PREDICT: " + AbsInvoker.getPredictTime() + "\n");
		
		if(CONFIG.isDebug()) {
			sb.append("TreePredItem INSTANCE NUM: " + TreePredItem.getInstanceNum() + "\n");
			sb.append("ProgramPoint INSTANCE NUM: " + ProgramPoint.getInstanceNum() + "\n");
		}
		
		sb.append("COMPILE TIME: " + totalCompileTime + "\n");
		sb.append("COMPILE FAILING TIME: " + totalCompileFailingTime + "\n");
		
		sb.append("TOTAL TIME USAGE: ");
		long ms = endAll - startAll;
		if(ms >= 1000) {
			sb.append(ms / 1000);
		} else {
			double d = ((double) ms) / 1000;
			sb.append(String.format("%.2f", d));
		}
		sb.append(" s\n");
		sb.append("RESULT SAVED AS: " + recordFilePath + "\n\n");
		
		String report = sb.toString();
		FileUtil.writeStringToFile(REPORT_PATH, report, true);
		System.out.println(report);
	}
	
	private static void recordPredResult(String bugName, String id, PredAllResult result) {
		String path = getRecordFilePath(bugName, ALL_RES_POSTFIX);
		StringBuffer sb = new StringBuffer();
		sb.append(">>>>>>>>>>>>>>>>>>>>>>>> " + id + "\n");
		
		List<String> predicatedList = result.getConditions();
		for(int i = 0; i < predicatedList.size(); i++) {
			String line = predicatedList.get(i);
			String pred = line.split("\t")[0];
			sb.append(line);
			sb.append('\n');
		}
		FileUtil.writeStringToFile(path, sb.toString(), true);
		
		List<String> predSeq = result.getPredSequence();
		if(predSeq != null && !predSeq.isEmpty()) {
			path = getRecordFilePath(bugName, PRED_SEQ_POSTFIX);
			
			String s0 = "NIL";
			if(predSeq.size() >= 1) {
				s0 = predSeq.get(0);
			}
			String s1 = "NIL";
			if(predSeq.size() >= 2) {
				s1 = predSeq.get(1);
			}
			
			String scoreStr = result.getScore().equals("-∞") ? "-inf" : result.getScore();
			String dumpRes = id + "\t" + result.getOracle() + "\t" + result.getRanking() + "\t" + scoreStr
						+ "\t" + s0 + "\t" + s1 + "\t";
			dumpRes = dumpRes + ((predSeq.size() > 2) ? predSeq.get(2) : "-\t-\t-" ) + "\n";
			FileUtil.writeStringToFile(path, dumpRes, true);
		}
	}

	private static int getHit(PredAllResult result) {
		List<String> predicatedList = result.getConditions();
		String oracle = result.getOracle();
		
		if(oracle == null) {
			return -1;
		}
		
		int hit = -1;
		String score = "0";
		for(int i = 0; i < predicatedList.size(); i++) {
			String[] temp = predicatedList.get(i).split("\t");
			String pred = temp[0];
			pred = processExpr(pred);
			
			if(oracle.endsWith(".0")) {
				oracle = oracle.substring(0, oracle.length() - 2);
			}
			if(oracle.endsWith("0d") || oracle.endsWith("1d") || oracle.endsWith("2d") || oracle.endsWith("0f") || oracle.endsWith("1f") || oracle.endsWith("2f")) {
				oracle = oracle.substring(0, oracle.length() - 1);
			}
						
			if(oracle.equals(pred)) {
				hit = i;
				score = temp[1];
				break;
			}
		}
		result.setRanking(hit);
		result.setScore(score);
		return hit;
	}
	
	private static String processExpr(String expr) {
		expr = expr.trim().replaceAll(" +", " ");
		expr = expr.replaceAll("\\<\\?\\>", "");
		return expr;
	}
}
