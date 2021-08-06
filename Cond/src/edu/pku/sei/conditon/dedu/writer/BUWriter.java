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
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.PROCESSING_TYPE;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.conditon.util.csv.CSVChecker;

public class BUWriter extends Writer {

	private List<CondVector> plainCondVec;
	private Set<CondVector> duplicatedCondVec = Collections.emptySet();
	private Map<String, OriPredItem> allOriPredicates;
	
	public BUWriter(String outFilePrefix, List<CondVector> plainCondVec) {
		super(outFilePrefix);
		this.plainCondVec = plainCondVec;
		this.allOriPredicates = AbsInvoker.loadAllOriPredicate(outFilePrefix + ".allpred.csv");
	}
	
	public BUWriter(String outFilePrefix, List<CondVector> plainCondVec, Set<CondVector> skippedCondVec) {
		this(outFilePrefix, plainCondVec);
		this.duplicatedCondVec = skippedCondVec;
	}
	
	private static String v0Header;
	private static String exprHeader;
	private static String varHeader;

	static {
		String varHead = VariableFeature.getFeatureHeader().replaceAll("\ttpfit", "");
		varHead = varHead.replaceAll("\toccpostime", "");
		varHead = varHead.replaceAll("\tused", "");
		List<String> headers0 = Arrays.asList(ContextFeature.getFeatureHeader(), varHead, "p0time", "putin");
		v0Header = StringUtil.join(headers0, del);
		
		String varHead1 = VariableFeature.getFeatureHeader().replaceAll("\ttpfit", "");
		varHead1 = varHead1.replaceAll("\tused", "");
		List<String> headers1 = Arrays.asList(ContextFeature.getFeatureHeader(), varHead1, "pred");
		exprHeader = StringUtil.join(headers1, del);

		List<String> headers = Arrays.asList(ContextFeature.getFeatureHeader(), varHead,
		PredicateFeature.getFeatureHeader(), "argused", "tpfit", "occpostime", "used", "position", "putin");
		
		varHeader = StringUtil.join(headers, del);
	}
	
	public final static String getButtomUpStepZeroHeader() {
		return v0Header;
	}

	public final static String getButtomUpStepOneHeader() {
		return exprHeader;
	}

	public final static String getButtomUpStepTwoHeader() {
		return varHeader;
	}

	@Override
	public void write() {
		writeButtomUpStep0AndStep1();
		writeButtomUpStep2();
	}

	private void writeButtomUpStep0AndStep1() {
		String varZeroPath = outFilePrefix + ".v0.csv";
		String exprPath = outFilePrefix + ".expr.csv";

		FileOutputStream fos0 = null;
		BufferedOutputStream bs0 = null;

		FileOutputStream fos1 = null;
		BufferedOutputStream bs1 = null;

		Map<String, Integer> pos0TimeMap = AbsInvoker.getPos0TimeMap(allOriPredicates);

		try {
			// step 0
			fos0 = new FileOutputStream(varZeroPath, false);
			bs0 = new BufferedOutputStream(fos0);

			// step 1
			fos1 = new FileOutputStream(exprPath, false);
			bs1 = new BufferedOutputStream(fos1);

			// header v0
			String header0 = getButtomUpStepZeroHeader() + "\n";
			bs0.write(header0.getBytes());

			// header expr
			String header1 = getButtomUpStepOneHeader() + "\n";
			bs1.write(header1.getBytes());

			int scanedVar = 0;
			for (int index = 0; index < plainCondVec.size(); index++) {
				CondVector vec = plainCondVec.get(index);

				if (duplicatedCondVec.contains(vec)) {
					continue;
				}

				if (omit(vec)) {
					continue;
				}

				Predicate pred = vec.getPredicate();

				String contextFeature = vec.genContextFeatureStr();

				for (VariableInfo info : vec.getLocals()) {

					String varFeaPrefix = info.genVarFeature();

					List<String> varZeroLineList = new ArrayList<>();
					varZeroLineList.add(contextFeature);
					varZeroLineList.add(varFeaPrefix);

					int posZeroTime = pos0TimeMap.containsKey(info.getNameLiteral())
							? pos0TimeMap.get(info.getNameLiteral())
							: 0;
					varZeroLineList.add("" + posZeroTime);// occurred at pos0's time

					List<PositionFeature> positionFeatureList = info.getPositionFeatures();
					if (positionFeatureList.size() > 0) {

						for (int i = 0; i < positionFeatureList.size(); i++) {// i'th occurrence of the var

							boolean used = (i > 0) ? true : false;
							if (used) {
								break;
							}

							PositionFeature pf = positionFeatureList.get(i);

							if (pf.getPosition() == 0) {// appeared at position zero
								/* for var zero */
								varZeroLineList.add("true"); // putin

								/* for expr */
								// int occuredTime = getVarOccurredTimeAtTheExprPosion(info.getNameLiteral(),
								// pred.getLiteral(), 0, allOriPredicates);

								List<String> exprLineList = new ArrayList<>();
								exprLineList.add(contextFeature);
								exprLineList.add(varFeaPrefix);
								exprLineList.add("" + posZeroTime);// occpostime

								String exprForCsv = predForCSV(pred);
								exprLineList.add(exprForCsv);
								String exprLine = StringUtil.join(exprLineList, del) + "\n";
								bs1.write(exprLine.getBytes());

							} else {
								varZeroLineList.add("false"); // putin
							}

						}

					} else {
						varZeroLineList.add("false"); // putin

						if (SKIP) {
							scanedVar++;
							if (scanedVar % SKIP_STEP == 0) {
								continue;
							}
						}
					}

					String varZeroLine = StringUtil.join(varZeroLineList, del) + "\n";
					bs0.write(varZeroLine.getBytes());

				} // end for(VariableInfo info: vec.getLocals())
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs0, bs1, fos0, fos1);
		}
		CSVChecker.checkAllCSV(varZeroPath, exprPath);
	}
	
	private void writeButtomUpStep2() {
		String varPath = outFilePrefix + ".var.csv";
		FileOutputStream fos = null;
		BufferedOutputStream bs = null;
		try {
			// step 0
			fos = new FileOutputStream(varPath, false);
			bs = new BufferedOutputStream(fos);

			String header = getButtomUpStepTwoHeader() + "\n";
			bs.write(header.getBytes());

			int scanedVar = 0;
			for (int index = 0; index < plainCondVec.size(); index++) {
				CondVector vec = plainCondVec.get(index);

				if (duplicatedCondVec.contains(vec)) {
					continue;
				}

				if (omit(vec) || (missionType != PROCESSING_TYPE.D4J && index % 4 == 1)) {
					continue;
				}

				Predicate pred = vec.getPredicate();

				String contextFeature = vec.genContextFeatureStr();
				String predicateFeature = pred.genPartialProgramFeature();

				for (int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++) {
					for (VariableInfo info : vec.getLocals()) {

						String varFeaPrefix = info.genVarFeature();

						List<String> varLineList = new ArrayList<>();
						varLineList.add(contextFeature);
						varLineList.add(varFeaPrefix);
						varLineList.add(predicateFeature);
						String currStr = StringUtil.join(varLineList, del);

						boolean argUsed = DeduConditionVisitor.usedAsParam(pred.getOriLiteral(), info.getNameLiteral());
						currStr = StringUtil.connect(currStr, "" + argUsed, del); // argused

						boolean typeFit = TypeUtil.isLegalVarAtPosition(pred.getLiteral(), slopIndex, info,
								allOriPredicates);
						currStr = StringUtil.connect(currStr, "" + typeFit, del); // tpfit

						int occuredTime = DeduConditionVisitor.getVarOccurredTimeAtTheExprPosion(info.getNameLiteral(), pred.getLiteral(),
								slopIndex, allOriPredicates);

						currStr = StringUtil.connect(currStr, "" + occuredTime, del); // occuredTime

						List<PositionFeature> positionFeatureList = info.getPositionFeatures();

						if (positionFeatureList.size() > 0) {

							for (int i = 0; i < positionFeatureList.size(); i++) {// i'th occurrence of the var
								boolean used = (i > 0) ? true : false;
								String outputStr = StringUtil.connect(currStr, "" + used, del);
								outputStr = StringUtil.connect(outputStr, "" + slopIndex, del);// index

								PositionFeature positionFeature = positionFeatureList.get(i);
								if (positionFeature.getPosition() == slopIndex) {
									outputStr = StringUtil.connect(outputStr, "true", del);// putin
								} else {
									outputStr = StringUtil.connect(outputStr, "false", del);// putin
								}
								bs.write((outputStr + "\n").getBytes());
							}

						} else {
							if (SKIP) {
								scanedVar++;
								if (scanedVar % SKIP_STEP == 0) {
									continue;
								}
							}

							String outputStr = StringUtil.connect(currStr, "false", del); // used
							outputStr = StringUtil.connect(outputStr, "" + slopIndex, del); // index
							outputStr = StringUtil.connect(outputStr, "false", del);// putin
							bs.write((outputStr + "\n").getBytes());
						}
					} // for(VariableInfo info: vec.getLocals())

				} // for(int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++)
			} // for(int index = 0; index < condVectorVectorList.size(); index++)
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeInputStream(bs, fos);
		}
		CSVChecker.checkAllCSV(varPath);
	}

}
