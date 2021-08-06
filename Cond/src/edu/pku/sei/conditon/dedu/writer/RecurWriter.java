package edu.pku.sei.conditon.dedu.writer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.extern.AbsInvoker;
import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.PositionFeature;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.TreeVector;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurTree;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.conditon.util.csv.CSVChecker;

public class RecurWriter extends Writer {

	private List<TreeVector> recurTreeList;
	
	public RecurWriter(String outFilePrefix, List<TreeVector> recurTreeList) {
		super(outFilePrefix);
		this.recurTreeList = recurTreeList;
	}
	
	private static String recurHeader;
	private static String exprHeader;
	private static String varHeader;
	static {
		List<String> headers = Arrays.asList(ContextFeature.getFeatureHeader(), RecurBoolNode.getFeatureHeader(), "nodetp");
		recurHeader = StringUtil.join(headers, del);
		
		List<String> headers1 = Arrays.asList(ContextFeature.getFeatureHeader(), RecurBoolNode.getFeatureHeader(), "pred");
		exprHeader = StringUtil.join(headers1, del);
		
		String varHead = VariableFeature.getFeatureHeader().replaceAll("\ttpfit", "");
		varHead = varHead.replaceAll("\toccpostime", "");
		varHead = varHead.replaceAll("\tused", "");
		
		List<String> headers2 = Arrays.asList(ContextFeature.getFeatureHeader(), 
				RecurBoolNode.getFeatureHeader(), 
				varHead, 
				PredicateFeature.getFeatureHeader(), 
				"argused", "tpfit", "occpostime", "position", "putin");
		
		varHeader = StringUtil.join(headers2, del);
	}

	public final static String getRecurNodeTypeHeader(){
		return recurHeader;
	}
	
	public final static String getRecurNodeExprHeader(){
		return exprHeader;
	}
	
	public final static String getRecurNodeVarHeader(){
		return varHeader;
	}
	
	@Override
	public void write() {
		writeAllTree();
		writeRecurTree();
	}
	
	private void writeAllTree() {
		String path = outFilePrefix + ".full.csv";
		FileOutputStream fos = null;
		BufferedOutputStream bs = null;
		try {
			fos = new FileOutputStream(path, false);
			bs = new BufferedOutputStream(fos);
			bs.write("id	file	line	cond\n".getBytes()); // spilt by TAB
			for(TreeVector treeVec : recurTreeList) {
				String line = treeVec.getId() + "\t" + treeVec.getFileName() + "\t" + 
						treeVec.getLine() + "\t" + 
						treeVec.getExpr().toString().replaceAll("\n", " ") + "\n";
				bs.write(line.getBytes());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs, fos);
		}
		CSVChecker.checkAllCSV(path);
	}
	
	
	private void writeRecurTree() {
		String path = outFilePrefix + ".recur.csv";
		FileOutputStream fos0 = null;
		BufferedOutputStream bs0 = null;
		
		String exprPath = outFilePrefix + ".recur.expr.csv";
		FileOutputStream fos1 = null;
		BufferedOutputStream bs1 = null;
		
		String varPath = outFilePrefix + ".recur.var.csv";
		FileOutputStream fos2 = null;
		BufferedOutputStream bs2 = null;

		Map<String, OriPredItem> allOriPredicates = AbsInvoker.loadAllOriPredicate(outFilePrefix + ".allpred.csv");

		try {
			//recur node
			fos0 = new FileOutputStream(path, false);
			bs0 = new BufferedOutputStream(fos0);
			
			//top down expr
			fos1 = new FileOutputStream(exprPath, false);
			bs1 = new BufferedOutputStream(fos1);
			
			//top down var
			fos2 = new FileOutputStream(varPath, false);
			bs2 = new BufferedOutputStream(fos2);
			
			bs0.write((getRecurNodeTypeHeader() + "\n").getBytes());
			bs1.write((getRecurNodeExprHeader() + "\n").getBytes());
			bs2.write((getRecurNodeVarHeader() + "\n").getBytes());
			
			int scanedVar = 0;

			for(TreeVector treeVec : recurTreeList) {
				
				if(omit(treeVec)) {
					continue;
				}
				
				String contextFeature = treeVec.genContextFeatureStr();
				
				RecurTree tree = treeVec.getTree();
				
				List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();

				for(RecurBoolNode recurBoolNode: bfsList) {
					
					List<String> recurNodeTypeList = new ArrayList<>();
					recurNodeTypeList.add(contextFeature);
					String recurNodeFeature = recurBoolNode.genDownwardFeature();
					recurNodeTypeList.add(recurNodeFeature);
					
					String recurNodeY = "" + recurBoolNode.getOpcode().toLabel();
					recurNodeTypeList.add(recurNodeY);//Y, node type
					String recurNodeLine = StringUtil.join(recurNodeTypeList, del) + "\n";
					// recur node data
					bs0.write(recurNodeLine.getBytes());
					
					if(recurBoolNode.getOpcode() != Opcode.NONE) {// skip non-leaf node 
						continue;
					}
					List<String> exprLineList = new ArrayList<>();
					exprLineList.add(contextFeature);
					exprLineList.add(recurNodeFeature);
					
					Predicate pred = recurBoolNode.getCondVector().getPredicate();
					if(pred.getSlopNum() > UPPER_POS_BOUND || pred.getSlopNum() == 0) {
						continue;
					}
					String exprForCsv = predForCSV(pred);
					exprLineList.add(exprForCsv);
					
					String exprLine = StringUtil.join(exprLineList, del) + "\n";
					bs1.write(exprLine.getBytes());
					
					String predicateFeature = pred.genPartialProgramFeature();
					for(int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++){
						for(VariableInfo info: recurBoolNode.getCondVector().getLocals()){
							String varFeaPrefix = info.genVarFeature();
							List<String> varLineList = new ArrayList<>();
							varLineList.add(contextFeature);
							varLineList.add(recurNodeFeature);
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
//									boolean used = (i > 0) ? true : false;
									 String outputStr = StringUtil.connect(currStr, "" + slopIndex, del);//index

									PositionFeature positionFeature = positionFeatureList.get(i);
									if(positionFeature.getPosition() == slopIndex){
										outputStr = StringUtil.connect(outputStr, "true", del);//putin
									}else{
										outputStr = StringUtil.connect(outputStr, "false", del);//putin
									}
									bs2.write((outputStr + "\n").getBytes());
								}
							}else{
								scanedVar++;
								if(scanedVar % 4 == 0){
									continue;
								}
								
								String outputStr = StringUtil.connect(currStr, "" + slopIndex, del); //index
								outputStr = StringUtil.connect(outputStr, "false", del);//putin
								bs2.write((outputStr + "\n").getBytes());
							}
						}
					}
				}
				
				bs0.flush();
				bs1.flush();
				bs2.flush();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs0, fos0);
		}
		CSVChecker.checkAllCSV(path);
		CSVChecker.checkAllCSV(exprPath);
		CSVChecker.checkAllCSV(varPath);
	}
}
