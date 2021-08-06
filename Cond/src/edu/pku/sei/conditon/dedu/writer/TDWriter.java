package edu.pku.sei.conditon.dedu.writer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.PositionFeature;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.conditon.util.csv.CSVChecker;

public class TDWriter extends Writer {
	
	private List<CondVector> plainCondVec;
	private Set<CondVector> duplicatedCondVec = Collections.emptySet();
	
	public TDWriter(String outFilePrefix, List<CondVector> plainCondVec) {
		super(outFilePrefix);
		this.plainCondVec = plainCondVec;
	}
	
	public TDWriter(String outFilePrefix, List<CondVector> plainCondVec, Set<CondVector> skippedCondVec) {
		this(outFilePrefix, plainCondVec);
		this.duplicatedCondVec = skippedCondVec;
	}

	private static String exprHeader;
	private static String varHeader;
	static {
		List<String> headers = Arrays.asList(ContextFeature.getFeatureHeader(), "pred");
		exprHeader = StringUtil.join(headers, del);
		
		String varHead = VariableFeature.getFeatureHeader().replaceAll("\ttpfit", "");
		varHead = varHead.replaceAll("\toccpostime", "");
		varHead = varHead.replaceAll("\tused", "");
		
		List<String> headers1 = Arrays.asList(ContextFeature.getFeatureHeader(), varHead, 
				PredicateFeature.getFeatureHeader(), "argused", "tpfit", "occpostime", "position", "putin");
		
		varHeader = StringUtil.join(headers1, del);
	}
	
	public final static String getTopDownStepZeroHeader(){
		return exprHeader;
	}
	
	public final static String getTopDownStepOneHeader(){
		return varHeader;
	}
	
	@Override
	public void write() {
		writeTopDown();
	}
	
	private void writeTopDown(){
		String exprPath = outFilePrefix + ".topdown.expr.csv";
		String varPath = outFilePrefix + ".topdown.var.csv";

		FileOutputStream fos0 = null;
		BufferedOutputStream bs0 = null;
		
		FileOutputStream fos1 = null;
		BufferedOutputStream bs1 = null;
		Map<String, OriPredItem> allOriPredicates = AbsInvoker.loadAllOriPredicate(outFilePrefix + ".allpred.csv");

		try {
			//step 0
			fos0 = new FileOutputStream(exprPath, false);
			bs0 = new BufferedOutputStream(fos0);
			
			//step 1
			fos1 = new FileOutputStream(varPath, false);
			bs1 = new BufferedOutputStream(fos1);
						
			//header expr
			String header0 = getTopDownStepZeroHeader() + "\n";
			bs0.write(header0.getBytes());
			
			//header all var
			String header1 = getTopDownStepOneHeader() + "\n";
			bs1.write(header1.getBytes());
			
			int scanedVar = 0;
			for(int index = 0; index < plainCondVec.size(); index++){
				CondVector vec = plainCondVec.get(index);
				
				if(duplicatedCondVec.contains(vec)) {
					continue;
				}
				
				if(omit(vec)) {
					continue;
				}
				
				Predicate pred = vec.getPredicate();
				
				if(pred.getSlopNum() == 0) {
					continue;
				}
								
				String contextFeature = vec.genContextFeatureStr();
				
				String exprForCsv = predForCSV(pred);
				
				String exprLine = StringUtil.connect(contextFeature, exprForCsv, del);
				exprLine += "\n";
				bs0.write(exprLine.getBytes());
				
				String predicateFeature = pred.genPartialProgramFeature();
				
				for(int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++){
					for(VariableInfo info: vec.getLocals()){
						
						String varFeaPrefix = info.genVarFeature();
						
						List<String> varLineList = new ArrayList<>();
						varLineList.add(contextFeature);
						varLineList.add(varFeaPrefix);
						varLineList.add(predicateFeature);
						String currStr = StringUtil.join(varLineList, del);
						
						boolean argUsed = DeduConditionVisitor.usedAsParam(pred.getOriLiteral(), info.getNameLiteral());
						currStr = StringUtil.connect(currStr, "" + argUsed, del); //argused
						
						boolean typeFit = TypeUtil.isLegalVarAtPosition(pred.getLiteral(), slopIndex, info, allOriPredicates); 
						currStr = StringUtil.connect(currStr, "" + typeFit, del); //tpfit
						
						int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(
								info.getNameLiteral(), 
								pred.getLiteral(), 
								slopIndex, 
								allOriPredicates);
						
						currStr = StringUtil.connect(currStr, "" + occuredTime, del); //occuredTime
						
						List<PositionFeature> positionFeatureList = info.getPositionFeatures();
						
						if(positionFeatureList.size() > 0){
							
							for(int i = 0; i < positionFeatureList.size(); i++){//i'th occurrence of the var
//								boolean used = (i > 0) ? true : false;
								 String outputStr = StringUtil.connect(currStr, "" + slopIndex, del);//index

								PositionFeature positionFeature = positionFeatureList.get(i);
								if(positionFeature.getPosition() == slopIndex){
									outputStr = StringUtil.connect(outputStr, "true", del);//putin
								}else{
									outputStr = StringUtil.connect(outputStr, "false", del);//putin
								}
								bs1.write((outputStr + "\n").getBytes());
							}
							
						}else{
							if(SKIP) {
								scanedVar++;
								if(scanedVar % SKIP_STEP == 0){
									continue;
								}
							}
							
							String outputStr = StringUtil.connect(currStr, "" + slopIndex, del); //index
							outputStr = StringUtil.connect(outputStr, "false", del);//putin
							bs1.write((outputStr + "\n").getBytes());
						}
					}//for(VariableInfo info: vec.getLocals())
				
				}//for(int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++)
				
				bs0.flush();
				bs1.flush();
			}//for(int index = 0; index < condVectorVectorList.size(); index++)
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs0, bs1, fos0, fos1);
		}
		CSVChecker.checkAllCSV(varPath, exprPath);
	}	
	
}
