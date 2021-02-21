package edu.pku.sei.conditon.dedu.real;

import java.util.List;

import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.extern.ConcretePredictor;
import edu.pku.sei.conditon.dedu.extern.pf.BUPathFinder;
import edu.pku.sei.conditon.dedu.extern.pf.BeamSearchStrategy;
import edu.pku.sei.conditon.dedu.extern.pf.DJSearchStrategy;
import edu.pku.sei.conditon.dedu.extern.pf.PathFinder;
import edu.pku.sei.conditon.dedu.extern.pf.PathFindingException;
import edu.pku.sei.conditon.dedu.extern.pf.RecurPathFinder;
import edu.pku.sei.conditon.dedu.extern.pf.SearchStrategy;
import edu.pku.sei.conditon.dedu.extern.pf.TDPathFinder;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.SearchMethod;
import edu.pku.sei.conditon.dedu.predall.PredAllResult;
import edu.pku.sei.conditon.util.FileUtil;

public class RealBugExpriment {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	/**
	 * open interface for pred_all experiment
	 * @param argsForInvoker
	 * @param oracle
	 * @return
	 */
	public static PredAllResult loadPredRes(String[] argsForInvoker, String oracle) {
		assert argsForInvoker.length == 6;
		
		String projAndBug = argsForInvoker[0].toLowerCase();
		String srcRoot = argsForInvoker[1];
		String testRoot = argsForInvoker[2];
		int line = Integer.valueOf(argsForInvoker[4]);
		int sid = Integer.valueOf(argsForInvoker[5]);
		
		String outPath = AbsInvoker.getOutputFilePath(projAndBug.split("_")[0], projAndBug + "_" + sid);
		FileUtil.deleteSingleFile(outPath);

		String filePath = argsForInvoker[3];
		
		PredAllResult result = new PredAllResult(oracle);

		if(CONFIG.isUseConcrete()) {
			
			assert CONFIG.getSearchMethod() == SearchMethod.GREEDY_BEAM;
			
			ConcretePredictor predictor = new ConcretePredictor(projAndBug, srcRoot, testRoot);
			if(CONFIG.isRecur()) {
				predictor.getConditionByRecur(filePath, line, sid);
			} else if(CONFIG.isBottomUp()) {
				predictor.getExprsByBottomUp(filePath, line, sid);
			}else {
				predictor.getExprsByTopDown(filePath, line, sid);
			}
		} else {
			
			assert CONFIG.getSearchMethod() != SearchMethod.GREEDY_BEAM;
			
			SearchStrategy searchStrategy = null;
			if(CONFIG.getSearchMethod() == SearchMethod.DIJKSTRA) {
				searchStrategy = DJSearchStrategy.getInstance();
			} else if(CONFIG.getSearchMethod() == SearchMethod.BEAM) {
				searchStrategy = BeamSearchStrategy.getInstance();
			}
			
			assert searchStrategy != null;
			
			try {
				PathFinder pf;
				if(CONFIG.isRecur()) {
					pf = new RecurPathFinder(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);
				} else if(CONFIG.isBottomUp()){
					pf = new BUPathFinder(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);
				} else {
					pf = new TDPathFinder(projAndBug, srcRoot, testRoot, filePath, line, sid, searchStrategy);
				}
				
				if(CONFIG.isDebug() && oracle != null) {
					pf.setOracle(oracle);
				}
			
				pf.entry();
				
				result.setCompileTime(pf.getCompileTime());
				result.setCompileFailingTime(pf.getCompileFailingTime());
				
				if(CONFIG.isDebug()) {
					List<String> predSeq = pf.getPredSequence();
					result.setPredSequence(predSeq);
				}
				
			} catch (PathFindingException e) {
				e.printStackTrace();
			}
			
		}
		List<String> conditions = FileUtil.readFileToStringList(outPath);
		
//		for(String cond: conditions) {
//			System.out.println(cond);
//		}
		
		result.setConditions(conditions);
		
		return result;
	}
	
	public static  void main(String[] args) {
		if(args.length != 6) {
			System.out.println("Error ARGS");
			return;
		}
		PredAllResult result = loadPredRes(args, null);
		List<String> conditions = result.getConditions();
		if(conditions == null) {
			return;
		}
		for(int i = 0; i < conditions.size(); i++) {
			String cond = conditions.get(i);
			System.out.println(i + "\t" + cond);
		}
	}
}
