package edu.pku.sei.conditon.dedu.pred;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.auxiliary.PredicateFeatureVisitor;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.StringUtil;

public class ExprPredItem extends AbsPredItem{
	private String pred;
	private String temVarCompletPred;
	private int posnum = 0;
	private PredicateFeature predicateFeature;
	private String exprFeature;
	
	private Expression astNode;
	
	private Map<Integer, List<VarPredItem>> predVars;
	
		
	/**
	 * @param expr, is a dollaralized string
	 * @return '$ + $ > 0' -> tmp0 + tmp1 > 0
	 * @throws Exception
	 */
	public static Expression generateASTNodeForDollarExpr(String expr) throws ClassCastException{
		String completedExpr = new String(expr);
		int posnum = StringUtil.charOccurTimesInStr(expr, '$');
		
		for(int i = 0; i < posnum; i++){
			completedExpr = completedExpr.replaceFirst("\\$", "tmp" + i);
		}
		
		Expression astNode = (Expression) JavaFile.genASTFromSourceAsJava7(completedExpr, ASTParser.K_EXPRESSION);
		return astNode;
	}
	
	
	public ExprPredItem(String file, String pred, Expression astNode, double score) {
		super(score);
		this.pred = pred;
		this.astNode = astNode;
		this.posnum = this.computePositionNum();
		this.predicateFeature = this.computFeature(file);
		this.exprFeature = this.genPredicateFeature();
	}

	private String genPredicateFeature() {
		List<String> predLineList = new ArrayList<>();
		predLineList.add(pred);
		predLineList.add("" + posnum);
		predLineList.add(predicateFeature.getRootType());
		predLineList.add(predicateFeature.getAriop());
		predLineList.add("" + predicateFeature.getHight());
		predLineList.add(predicateFeature.getFirstMtd());
		predLineList.add("" + predicateFeature.isHasInstanceof());
		predLineList.add(predicateFeature.getFirstNum());
		predLineList.add(predicateFeature.getSecondNum());
		predLineList.add("" + predicateFeature.isHasNull());
		return StringUtil.join(predLineList, AbstractDeduVisitor.del);
	}

	public String getExprFeature(){
		return exprFeature;
	}
	
	public PredicateFeature getPredicateFeature() {
		return predicateFeature;
	}
	
	public String getPred() {
		return pred;
	}
	
	public int getPositionNum(){
		return posnum;
	}
	
	public String getTemVarCompletPred() {
		return temVarCompletPred;
	}
	
	public Map<Integer, List<VarPredItem>> getPredVars() {
		return predVars;
	}

	public void setPredVars(Map<Integer, List<VarPredItem>> predVars) {
		this.predVars = predVars;
	}

	public Expression getAstNode() {
		return astNode;
	}
	
	private PredicateFeature computFeature(String file){
		String tmpPred = this.pred;
		for(int i = 0; i < this.posnum; i++){
			tmpPred = tmpPred.replaceFirst("\\$", "tmp" + i);
		}
		
		this.temVarCompletPred = tmpPred;
		
		PredicateFeatureVisitor visitor = new PredicateFeatureVisitor(file, this.astNode);
		this.astNode.accept(visitor);
		return visitor.getCollectedFeature();
	}
	
	private int computePositionNum(){
		int res = 0;
		for(int i = 0; i < this.pred.length(); i++){
			if(this.pred.charAt(i) == '$'){
				res++;
			}
		}
		return res;
	}
	
	@Override
	public String toString() {
		return pred + "\t" + score;
	}

}
