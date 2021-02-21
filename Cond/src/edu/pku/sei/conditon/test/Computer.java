package edu.pku.sei.conditon.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.pku.sei.conditon.util.FileUtil;

public class Computer {
	
	private static void process(String file) {
		List<String> lines  = FileUtil.readFileToStringList(file);
		int totalWeight = 0;
		int totalFeaNum = 0;
		int speWeight = 0;
		int speNum = 0;
		Set<String> set = new HashSet<>();
		for(String line : lines) {		
			if(line.startsWith("('")) {
				if(totalFeaNum == 100) {
					break;
				}
				
				totalFeaNum++;
				
				String tmp = line.split(",")[0];
				String feaName = tmp.substring(2, tmp.length() - 1);
				//System.out.println(feaName);
				
				String num = line.split(",")[1].trim();
				num = num.substring(0, num.length() - 1);
				int wei = Integer.valueOf(num);
				totalWeight += wei;
				
				if(line.startsWith("('p_")) {
					speWeight += wei;
					speNum++;
					set.add(line.split("_")[1]);
					
				} else {
					if(set.contains(feaName)) {
						System.out.println(">>>> " + feaName);
					}
					
				}
			}
		}
		System.out.println(speNum  + " / " + totalFeaNum);
		System.out.println(speWeight  + " / " + totalWeight);

		System.out.println((double) speNum/ totalFeaNum);
		System.out.println((double) speWeight/ totalWeight);

	}
	
	public static void main(String[] args) {
		String path = "/home/nightwish/chart_expr";
		process(path);
	}
}
