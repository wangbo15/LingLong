package edu.pku.sei.conditon.dedu.pred;
import java.math.BigDecimal;
import java.util.List;

import edu.pku.sei.conditon.ds.VariableInfo;

public class VarPredItem extends AbsPredItem{
	private String literal;
	private boolean isField = false;
	private int position;
	private VariableInfo info;
	
	public VarPredItem(String literal, int position, VariableInfo varInfo, Double score){
		super(score);
		
		if(literal.endsWith("#F")){
			literal = literal.substring(0, literal.length() - 2);
			isField = true;
		}
		
		this.literal = literal;
		
		this.position = position;
		this.info = varInfo;
	}
	public String getLiteral() {
		return literal;
	}

	public boolean isField() {
		return isField;
	}
	public int getPosition() {
		return position;
	}

	public VariableInfo getInfo(){
		return info;
	}
	
	@Override
	public String toString(){
		return literal + "\t" + isField + "\t" + position + "\t" + score;
	}
	
	
	/**
	 * adjust the sum of vars to 1
	 * @param vars
	 */
	public static void adjustVarsProbability(List<VarPredItem> vars){
		BigDecimal sum = BigDecimal.ZERO;
		for(VarPredItem var : vars){
			BigDecimal bigDec = BigDecimal.valueOf(var.getScore());
			sum = sum.add(bigDec);
		}
//		BigDecimal e = new BigDecimal("1E-8");
//		if(BigDecimal.ONE.subtract(sum).compareTo(e) < 0){
//			return;
//		}
		for(VarPredItem var : vars){
			BigDecimal bigDec = BigDecimal.valueOf(var.getScore());
			BigDecimal div = bigDec.divide(sum, 17, BigDecimal.ROUND_HALF_UP);
//			System.out.println(var.getScore() + " => " + div);
			var.setScore(div.doubleValue());
		}
	}
}
