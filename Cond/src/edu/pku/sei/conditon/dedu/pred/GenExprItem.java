package edu.pku.sei.conditon.dedu.pred;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import edu.pku.sei.conditon.dedu.pred.metric.Cosine;
import edu.pku.sei.conditon.dedu.pred.metric.Jaccard;
import edu.pku.sei.conditon.dedu.pred.metric.LevenshteinDistance;

public class GenExprItem {
	private ExprPredItem expr;
	private List<VarPredItem> vars;
	private BigDecimal score;
	private String generatedExpr;
	
	public GenExprItem(ExprPredItem expr, List<VarPredItem> vars) {
		this.expr = expr;
		this.vars = vars;
				
		String res = new String(expr.getPred());
		for(VarPredItem var: vars){
			res = res.replaceFirst("\\$", var.getLiteral());
		}
		this.generatedExpr = res;
		
		this.score = computeScore();
	}
	
	public ExprPredItem getExpr() {
		return expr;
	}

	public List<VarPredItem> getVars() {
		return vars;
	}

	public BigDecimal getScore() {
		return score;
	}

	private BigDecimal computeScore() {
		BigDecimal mlScore = computeMLScore();
		/*
		if(oriNode instanceof Expression == false){
			return mlScore;
		}
		BigDecimal synScore = computeSynaticDistance();
		
		BigDecimal res = mlScore.multiply(synScore);
		return res;
		*/
		return mlScore;
	}
	
	private BigDecimal computeMLScore(){
		BigDecimal se = BigDecimal.valueOf(expr.getScore());
		for(VarPredItem var : vars){
			se = se.multiply(BigDecimal.valueOf(var.getScore()));
		}
		return se;
	}
	
	private BigDecimal computeSynaticDistance(){
		String src = expr.getPred().replaceAll("\\s", "");
		String tar = generatedExpr.replaceAll("\\s", "");
		
		Cosine cosSim = new Cosine(src, tar);
		BigDecimal cos = new BigDecimal(cosSim.sim());
		System.out.println(generatedExpr + "\t" + cos + "\t" + LevenshteinDistance.similarCalc(src, tar) + "\t" + Jaccard.jaccard(src, tar));
		BigDecimal res =  cos.multiply(new BigDecimal(LevenshteinDistance.similarCalc(src, tar)));
		return res;
	}
		
	@Deprecated
	private BigDecimal computeMLScoreComplex(){
		BigDecimal se = BigDecimal.valueOf(expr.getScore());
		BigDecimal sv = new BigDecimal("0");
		for(VarPredItem var : vars){
			sv = sv.add(BigDecimal.valueOf(var.getScore()));
		}
		BigDecimal slopNum = new BigDecimal(vars.size());
		
		se = se.divide(slopNum, BigDecimal.ROUND_HALF_UP);
		sv = sv.divide(slopNum, BigDecimal.ROUND_HALF_UP);
		
		sv = bigSqrt(sv);

		BigDecimal result = se.multiply(sv);
		return result;
	}
	
	public String getGeneratedExpr(){
		return this.generatedExpr;
	}

	@Override
	public String toString() {
		return this.expr.getPred() + "\t" + this.generatedExpr + "\t" + score;
	}
	
	
	
	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn, BigDecimal precision) {
		BigDecimal fx = xn.pow(2).add(c.negate());
		BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 = fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
		xn1 = xn.add(xn1.negate());
		BigDecimal currentSquare = xn1.pow(2);
		BigDecimal currentPrecision = currentSquare.subtract(c);
		currentPrecision = currentPrecision.abs();
		if (currentPrecision.compareTo(precision) <= -1) {
			return xn1;
		}
		return sqrtNewtonRaphson(c, xn1, precision);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	public static BigDecimal bigSqrt(BigDecimal c) {
		return sqrtNewtonRaphson(c, new BigDecimal(1), new BigDecimal(1).divide(SQRT_PRE));
	}

}
