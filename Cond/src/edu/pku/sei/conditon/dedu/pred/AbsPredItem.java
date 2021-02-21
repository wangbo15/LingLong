package edu.pku.sei.conditon.dedu.pred;

import java.math.BigDecimal;

public abstract class AbsPredItem implements Comparable<AbsPredItem> {
	protected double score;
	
	public AbsPredItem(double score) {
		this.score = score;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public int compareTo(AbsPredItem obj) {
		Double d0 = Double.valueOf(this.score);
		Double d1 = Double.valueOf(obj.score);
		return d0.compareTo(d1);
	}
}
