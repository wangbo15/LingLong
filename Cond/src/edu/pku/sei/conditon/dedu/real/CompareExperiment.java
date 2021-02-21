package edu.pku.sei.conditon.dedu.real;

import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.extern.SocketInvoker;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.predall.PredAllExperiment;
import edu.pku.sei.conditon.util.FileUtil;

public class CompareExperiment {
	
	private static String ORDER = "TD";
	private static String OUTPUT_FILE = "/home/nightwish/tmp/" + ORDER.toLowerCase() + ".res.csv";
	
	public static void main(String[] args) {
		
		List<String> items = FileUtil.readFileToStringList("/home/nightwish/tmp/bu.pred.csv");
		
		
		String projAndBug = "time_27";
		String model = AbsInvoker.bugToModelMap.get(projAndBug);		
		String proj = model.split("_")[0];

		String allpredPath = PredAllExperiment.getAllPredPath(projAndBug);

		Map<String, String> oraclesMap = PredAllExperiment.loadOriResForNonRecur(allpredPath);
		
		Map<String, OriPredItem> allOriPredicates = AbsInvoker.loadAllOriPredicate(allpredPath);
		Map<String, Integer> pos0TimeMap = AbsInvoker.getPos0TimeMap(allOriPredicates);
		
		SocketInvoker invoker = new SocketInvoker(allOriPredicates, pos0TimeMap);
		
		double scoreSum = 0.0D;
		int rankSum = 0;
		
		FileUtil.writeStringToFile(OUTPUT_FILE, "id\trank\tpred\tscore\n", false);

		for(String featureLine: items) {
			String id = featureLine.split("\t")[0];
			String oracle = oraclesMap.get(id);
			
			String predOracle = DollarilizeVisitor.parse(oracle).get(0);
			List<ExprPredItem> exprs = invoker.predictExpr(ORDER, featureLine);
			
			for(int i = 0; i < exprs.size(); i++) {
				ExprPredItem expr = exprs.get(i);
				if(expr.getPred().equals(predOracle)) {
					String line = id + "\t" + i + "\t" + expr.getPred() + "\t" + expr.getScore() + "\n";
					System.out.print(line);
					
					FileUtil.writeStringToFile(OUTPUT_FILE, line, true);
					
					scoreSum += expr.getScore();
					rankSum += i;
				}
			}
			
		}
		System.out.println("SCORE SUM: " + scoreSum);
		System.out.println("RANK SUM: " + rankSum);
	}
	
}
