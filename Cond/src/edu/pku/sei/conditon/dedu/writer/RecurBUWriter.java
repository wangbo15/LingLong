package edu.pku.sei.conditon.dedu.writer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.PositionFeature;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.TreeVector;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurTree;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.csv.CSVChecker;

/**
 * Writer for the Recur Grammar under BU 
 * Step 0: predict v0 
 * Step 1: predict e0
 * Step 2: predict r0 upward, until meet root
 * Step 3: predict r1 downward, until complete the tree
 * Step 5: predict e1
 * Step 6: predict v1
 * 
 * @author nightwish
 *
 */
public class RecurBUWriter extends Writer {

	private List<TreeVector> recurTreeList;

	private final List<OutputStream> streams = new ArrayList<>();

	private final List<String> paths = new ArrayList<>();

	public RecurBUWriter(String outFilePrefix, List<TreeVector> recurTreeList) {
		super(outFilePrefix);
		this.recurTreeList = recurTreeList;

		paths.add(outFilePrefix + ".recurbu.v0.csv");
		paths.add(outFilePrefix + ".recurbu.e0.csv");
		paths.add(outFilePrefix + ".recurbu.r0.csv");
		paths.add(outFilePrefix + ".recurbu.r1.csv");
		paths.add(outFilePrefix + ".recurbu.e1.csv");
		paths.add(outFilePrefix + ".recurbu.v1.csv");

		for (String path : paths) {
			try {
				FileOutputStream fs = new FileOutputStream(path, false);
				BufferedOutputStream bs = new BufferedOutputStream(fs);
				streams.add(bs);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	private static String v0Header;
	private static String e0Header;
	private static String r0Header;
	private static String v1Header;

	static {
		String varHead = VariableFeature.getFeatureHeader().replaceAll("\ttpfit", "");
		varHead = varHead.replaceAll("\toccpostime", "");
		varHead = varHead.replaceAll("\tused", "");
		List<String> headers0 = Arrays.asList(ContextFeature.getFeatureHeader(), varHead, "putin");
		v0Header = StringUtil.join(headers0, del);

		List<String> headers1 = Arrays.asList(ContextFeature.getFeatureHeader(), varHead, "pred");
		e0Header = StringUtil.join(headers1, del);

		String upheader = "nodetp\tcld0tp\tcld1tp\thight";
		List<String> headers2 = Arrays.asList(ContextFeature.getFeatureHeader(), upheader, "parenttp");
		r0Header = StringUtil.join(headers2, del);

		List<String> headers3 = Arrays.asList(ContextFeature.getFeatureHeader(), RecurBoolNode.getFeatureHeader(),
				varHead, PredicateFeature.getFeatureHeader(), "position", "putin");
		v1Header = StringUtil.join(headers3, del);
	}

	public final static String getRecurBUV0Header() {
		return v0Header;
	}

	public final static String getRecurBUE0Header() {
		return e0Header;
	}

	public final static String getRecurBUR0Header() {
		return r0Header;
	}

	public final static String getRecurBUR1Header() {
		return RecurWriter.getRecurNodeTypeHeader();
	}

	public final static String getRecurBUE1Header() {
		return RecurWriter.getRecurNodeExprHeader();
	}
	
	public final static String getRecurBUV1Header() {
		return v1Header;
	}

	/**
	 * v0, e0, r0: upward r1, e1, v1: downward
	 */
	@Override
	public void write() {
		// v0: ctx + v0
		// e0: ctx + v0
		// r0: ctx + tree + node
		// r1: ctx + tree + node
		// e1: ctx + tree + node
		// v1: ctx + expr + tree + node + var
		writeTree();
		closeStreams();
		check();
	}

	private static long scanedVar = 0;

	private void writeHeaders() {
		String v0Header = getRecurBUV0Header() + "\n";
		String e0Header = getRecurBUE0Header() + "\n";
		String r0Header = getRecurBUR0Header() + "\n";
		String r1Header = getRecurBUR1Header() + "\n";
		String e1Header = getRecurBUE1Header() + "\n";
		String v1Header = getRecurBUV1Header() + "\n";

		try {
			streams.get(0).write(v0Header.getBytes());
			streams.get(1).write(e0Header.getBytes());
			streams.get(2).write(r0Header.getBytes());
			streams.get(3).write(r1Header.getBytes());
			streams.get(4).write(e1Header.getBytes());
			streams.get(5).write(v1Header.getBytes());

			for (OutputStream os : streams) {
				os.flush();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void writeTree() {

		writeHeaders();

		for (TreeVector treeVec : recurTreeList) {
			if (omit(treeVec)) {
				continue;
			}

			RecurTree tree = treeVec.getTree();

			List<RecurBoolNode> buList = tree.bottomUpTraverse();

			RecurBoolNode leftMost = buList.get(0);

			// v0 & e0
			writeV0E0(treeVec, leftMost);
			// r0
			wirteR0(treeVec, leftMost);
			// r1, e1 & v1
			writeOthers(treeVec, leftMost);
		}
	}

	private void writeV0E0(TreeVector treeVec, RecurBoolNode leftBottomMost) {

		String contextFeature = treeVec.genContextFeatureStr();

		List<VariableInfo> allLocals = leftBottomMost.getCondVector().getLocals();
		for (VariableInfo info : allLocals) {
			String varFeaPrefix = info.genVarFeature();

			List<String> v0LineList = new ArrayList<>();

			v0LineList.add(contextFeature);
			v0LineList.add(varFeaPrefix);

			PositionFeature pf = info.getPositionFeature(0);
			if (pf != null) {
				v0LineList.add("true"); // putin

				List<String> e0LineList = new ArrayList<>();
				e0LineList.add(contextFeature);
				e0LineList.add(varFeaPrefix);

				Predicate pred = leftBottomMost.getCondVector().getPredicate();
				String exprForCsv = predForCSV(pred);
				e0LineList.add(exprForCsv);
				String e0Line = StringUtil.join(e0LineList, del) + "\n";
				try {
					streams.get(1).write(e0Line.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				v0LineList.add("false"); // putin
				if (SKIP) {
					scanedVar++;
					if (scanedVar % SKIP_STEP == 0) {
						continue;
					}
				}
			}
			String v0Line = StringUtil.join(v0LineList, del) + "\n";
			try {
				streams.get(0).write(v0Line.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void wirteR0(TreeVector treeVec, RecurBoolNode leftBottomMost) {
		RecurBoolNode curr = leftBottomMost;
		String contextFeature = treeVec.genContextFeatureStr();

		while (curr != null) {
			List<String> recurNodeTypeList = new ArrayList<>();
			recurNodeTypeList.add(contextFeature);
			String recurNodeFeature = curr.genUpwardFeature();
			recurNodeTypeList.add(recurNodeFeature);

			RecurBoolNode parent = curr.getParent();
			String parentNodeY = "";
			if (parent == null) {
				// empty parent
				parentNodeY += Opcode.NONE.toLabel();
			} else {
				parentNodeY += parent.getOpcode().toLabel();
			}
			recurNodeTypeList.add(parentNodeY);// Y, node type
			String recurNodeLine = StringUtil.join(recurNodeTypeList, del) + "\n";
			try {
				streams.get(2).write(recurNodeLine.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}

			curr = parent;
		}
	}

	private void writeOthers(TreeVector treeVec, RecurBoolNode leftBottomMost) {
		Set<RecurBoolNode> skipped = new HashSet<RecurBoolNode>();
		RecurBoolNode curr = leftBottomMost;
		while (curr != null) {
			skipped.add(curr);
			curr = curr.getParent();
		}
		String contextFeature = treeVec.genContextFeatureStr();
		for (RecurBoolNode node : treeVec.getTree().broadFristSearchTraverse()) {
			if (skipped.contains(node)) {
				continue;
			}

			// write r1
			List<String> recurNodeTypeList = new ArrayList<>();
			recurNodeTypeList.add(contextFeature);
			String recurNodeFeature = node.genDownwardFeature();
			recurNodeTypeList.add(recurNodeFeature);
			String recurNodeY = "" + node.getOpcode().toLabel();
			recurNodeTypeList.add(recurNodeY);// Y, node type
			String recurNodeLine = StringUtil.join(recurNodeTypeList, del) + "\n";

			try {
				streams.get(3).write(recurNodeLine.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (node.getOpcode() != Opcode.NONE) {// skip non-leaf node
				continue;
			}
			List<String> exprLineList = new ArrayList<>();
			exprLineList.add(contextFeature);
			exprLineList.add(recurNodeFeature);

			Predicate pred = node.getCondVector().getPredicate();
			if (pred.getSlopNum() > UPPER_POS_BOUND || pred.getSlopNum() == 0) {
				continue;
			}
			String exprForCsv = predForCSV(pred);
			exprLineList.add(exprForCsv);

			String exprLine = StringUtil.join(exprLineList, del) + "\n";
			try {
				streams.get(4).write(exprLine.getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			String predicateFeature = pred.genPartialProgramFeature();
			for (int slopIndex = 0; slopIndex < pred.getSlopNum(); slopIndex++) {
				for (VariableInfo info : node.getCondVector().getLocals()) {
					String varFeaPrefix = info.genVarFeature();
					List<String> varLineList = new ArrayList<>();
					varLineList.add(contextFeature);
					varLineList.add(recurNodeFeature);
					varLineList.add(varFeaPrefix);
					varLineList.add(predicateFeature);
					String currStr = StringUtil.join(varLineList, del);

					List<PositionFeature> positionFeatureList = info.getPositionFeatures();
					if (positionFeatureList.size() > 0) {
						for (int i = 0; i < positionFeatureList.size(); i++) {// i'th occurrence of the var
							// boolean used = (i > 0) ? true : false;
							String outputStr = StringUtil.connect(currStr, "" + slopIndex, del);// index

							PositionFeature positionFeature = positionFeatureList.get(i);
							if (positionFeature.getPosition() == slopIndex) {
								outputStr = StringUtil.connect(outputStr, "true", del);// putin
							} else {
								outputStr = StringUtil.connect(outputStr, "false", del);// putin
							}
							try {
								streams.get(5).write((outputStr + "\n").getBytes());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						scanedVar++;
						if (scanedVar % 4 == 0) {
							continue;
						}

						String outputStr = StringUtil.connect(currStr, "" + slopIndex, del); // index
						outputStr = StringUtil.connect(outputStr, "false", del);// putin
						try {
							streams.get(5).write((outputStr + "\n").getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	private void closeStreams() {
		for (OutputStream os : streams) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void check() {
		for (String path : paths) {
			CSVChecker.checkAllCSV(path);
		}
	}
}
