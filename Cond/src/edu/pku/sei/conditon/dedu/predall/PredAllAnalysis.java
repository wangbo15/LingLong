package edu.pku.sei.conditon.dedu.predall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.Pair;

public class PredAllAnalysis {
	
	private static List<File> loadAllRes(String proj){
		List<File> result = new ArrayList<>();
		String resPath = AbsInvoker.PREDICTOR_ROOT + "output/" + proj + "/res/";
		FileUtil.getAllFilesByType(resPath, ".res.csv", result);
		
		Collections.sort(result);
		return result;
	}
	
	private static String getResultId(File f) {
		String pre = f.getName().split("\\.")[0];
		String[] tmp = pre.split("_");
		int last = tmp.length - 1;
		return tmp[last];
	}
	
	private static Map<Integer, Pair<String, Integer>> loadCmd(String bugName){
		String cmdFilePath = DeduMain.USER_HOME + "/tmp/res/real/" + bugName + ".cmd.txt";
		List<String> lines = FileUtil.readFileToStringList(cmdFilePath);
		
		Map<Integer, Pair<String, Integer>> result = new HashMap<>();
		for(String line: lines) {
			String[] tmp = line.split(" ");
			int id = Integer.valueOf(tmp[5]);
			String[] fileItems = tmp[3].split("/");
			String file = fileItems[fileItems.length - 1];
			int lineNum = Integer.valueOf(tmp[4]);
			Pair<String, Integer> p = new Pair(file, lineNum);
			result.put(id, p);
		}
		
		return result;
	}
	private static Map<Integer, Pair<String, Integer>> idToSrcLocation;
	private static Map<String, String> fullOracle;
	
	public static void main(String[] args) {
		String proj = "time";
		String id = "11";
		String bugName = proj + "_" + id;
		
		List<File> allFiles = loadAllRes("time");
		
		idToSrcLocation = loadCmd(bugName);
		fullOracle = loadFullOracle(bugName);
		
		int top1 = 0;
		int top10 = 0;
		int top50 = 0;
		int total = allFiles.size();
		int complex = 0;
		
		for(File f : allFiles) {
			String resultId = getResultId(f);
			
			String oracle = getOracle(Integer.valueOf(resultId));
			
			oracle = oracle.replaceAll("\\s", "");
			
			//System.out.println(oracle);
			
			List<String> exprs = FileUtil.readFileToStringList(f);
			
			for(int i = 0; i < exprs.size(); i++) {
				String expr = exprs.get(i).split("\t")[0];
				
				expr = expr.replaceAll("\\s", "");
				
				if(expr.equals(oracle)) {
					if(oracle.contains("&&") || oracle.contains("||") || oracle.contains("!")) {
						complex++;
					}
					
					if(i < 1) {
						System.out.println(resultId + " HIT TOP 1: " + oracle);
						top1++;
					}else if (i < 10){
						System.out.println(resultId + " HIT TOP 10: " + oracle);
						top10 ++;
					}else if(i < 50) {
						System.out.println(resultId + " HIT TOP 50: " + oracle);
						top50++;
					}
					break;
				}
			}
		}
		
		top10 = top1 + top10;
		top50 = top10 + top50;
		System.out.println("TOP1: " + top1);
		System.out.println("TOP10: " + top10);
		System.out.println("TOP50: " + top50);
		System.out.println("Complex: " + complex);
		
		System.out.println("TOP1: " + (double) top1/total);
		System.out.println("TOP10: " + (double) top10/total);
		System.out.println("TOP50: " + (double) top50/total);
		System.out.println("Complex: " +(double) complex / total);

	}

	private static Map<String, String> loadFullOracle(String bugName) {
		String path = DeduMain.USER_HOME + "/tmp/res/" + bugName + ".full.csv";
		List<String> lines = FileUtil.readFileToStringList(path);
		
		Map<String, String> result = new HashMap<>();
		for(String line : lines) {
			String[] items = line.split("\t");
			String fileName = items[1];
			String lineNum = items[2];
			String oracle = items[3];
			result.put(getKey(fileName, lineNum), oracle);
		}
		
		return result;
	}

	private static String getOracle(int resultId) {
		Pair<String, Integer> pair = idToSrcLocation.get(resultId);
		String key = getKey(pair.getFirst(), pair.getSecond());
		return fullOracle.get(key);
	}
	
	private static String getKey(String fileName, String line) {
		return fileName + "#" + line;
	}
	
	private static String getKey(String fileName, int line) {
		return fileName + "#" + line;
	}
	
}
