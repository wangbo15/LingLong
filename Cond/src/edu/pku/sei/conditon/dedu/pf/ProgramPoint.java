package edu.pku.sei.conditon.dedu.pf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.pred.TreePredItem;

public class ProgramPoint implements Comparable<ProgramPoint>{
	
	private final ProgramPoint previousStep;
	
	private final int depth;
	
	private final List<ProgramPoint> nextSteps = new ArrayList<>();
	
	private final int remainingPosNum;
	
	private TreePredItem astRoot;
	
	private double score;
	
	private static long instanceNum = 0;
	
	public ProgramPoint(ProgramPoint previousStep, TreePredItem astRoot, double score, int remainingPosNum) {
		assert remainingPosNum >= 0;
		assert score <= 0;
		assert astRoot != null;
		
		this.previousStep = previousStep;
		this.astRoot = astRoot;
		this.score = score;
		this.remainingPosNum = remainingPosNum;
		if(this.previousStep == null) {
			this.depth = 0;
		} else {
			this.depth = previousStep.depth + 1;
		}
		
		instanceNum++;
	}
	
	/**************************************************************/
	public static long getInstanceNum() {
		return instanceNum;
	}
	
	public List<ProgramPoint> getNextSteps() {
		return nextSteps;
	}

	public TreePredItem getAstRoot() {
		return astRoot;
	}

	public void setAstRoot(TreePredItem astRoot) {
		this.astRoot = astRoot;
	}

	public double getScore() {
		return score;
	}
	
	public int getRemainingPosNum() {
		return remainingPosNum;
	}

	public boolean isComplete() {
		return remainingPosNum == 0;
	}
	
	public ProgramPoint getPreviousStep() {
		return previousStep;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public int compareTo(ProgramPoint o) {
		Double d0 = Double.valueOf(this.score);
		Double d1 = Double.valueOf(o.score);
		return d0.compareTo(d1);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ProgramPoint@" + this.hashCode() + "\n");
		if(astRoot != null) {
			sb.append("TREE ITEM:\n");
			sb.append(astRoot.toString());
		}
		sb.append('\n');
		String scoreStr = Path.decimalFormat.format(score);
		sb.append("SCORE: " + scoreStr + "\tPOS-NUM: " + this.remainingPosNum);
		sb.append('\n');
		return sb.toString();
	}
	
	public static ProgramPoint getBytreeLiteral(TreeSet<ProgramPoint> set, String treeLiter) {
		Iterator<ProgramPoint> it = set.iterator();
		while(it.hasNext()) {
			ProgramPoint p = it.next();
			TreePredItem root = p.getAstRoot();
			if(root != null) {
				if(root.toString().equals(treeLiter)) {
					return p;
				}
			}
		}
		return null;
	}
	
}
