package edu.pku.sei.conditon.dedu.feature;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.util.StringUtil;

public class Predicate {
	private String literal;
	private String oriLiteral;
	private String rootType;
	
	private List<String> slopTypes;
	private int slopNum = -1;
	
	private List<String> oriSlopVars;//a var list, follow the occurrence order of the ori expr
	
	private PredicateFeature predicateFeature;
	
	public static final String getPredType(Expression expr){
		if(expr instanceof MethodInvocation){
			return ((MethodInvocation) expr).getName().getIdentifier();
		}else if(expr instanceof InfixExpression){
			return ((InfixExpression) expr).getOperator().toString();
		}else{
			return expr.getClass().getName();
		}
	}
	
	public Predicate(Expression expr, List<String> slopTypes, List<String> oriSlopVars, 
			PredicateFeature predicateFeature) {
		this.literal = DollarilizeVisitor.dollarilize(expr);
		this.oriLiteral = expr.toString().replaceAll("\n", " ");
		this.rootType = getPredType(expr);
		
		assert slopTypes.size() == oriSlopVars.size();
		this.slopNum = slopTypes.size();
		this.predicateFeature = predicateFeature;
	}
	
	
	public Predicate(String literal, String oriLiteral, String rootType, 
			int slopNum, List<String> slopTypes, List<String> oriSlopVars, 
			PredicateFeature predicateFeature) {
		
		this.literal = literal;
		this.oriLiteral = oriLiteral;
		this.rootType = rootType;
		this.slopNum = slopNum;
		this.slopTypes = slopTypes;
		this.oriSlopVars = oriSlopVars;
		this.predicateFeature = predicateFeature;
	}
	
	public PredicateFeature getPredicateFeature(){
		return predicateFeature;
	}
	
	public String getLiteral() {
		return literal;
	}

	public String getOriLiteral() {
		return oriLiteral;
	}

	public int getSlopNum() {
		return slopNum;
	}
	
	public String getRootType() {
		return rootType;
	}
	
	public List<String> getSlopTypes() {
		return slopTypes;
	}
	
	public List<String> getOriSlopVars() {
		return oriSlopVars;
	}

	@Override
	public String toString() {
		return oriLiteral + " => "+ literal + " ---- " + slopNum;
	}
	
	private static final String del = AbstractDeduVisitor.del;
	
	private String partialProgramFeatureStr;
	
	public String genPartialProgramFeature(){
		if(partialProgramFeatureStr != null) {
			return partialProgramFeatureStr;
		}
		PredicateFeature  predFea = this.getPredicateFeature();
		List<String> predLineList = new ArrayList<>();
		predLineList.add(this.getLiteral());
		predLineList.add("" + this.getSlopNum());
		predLineList.add(predFea.getRootType());
		predLineList.add(predFea.getAriop());
		predLineList.add("" + predFea.getHight());
		predLineList.add(predFea.getFirstMtd());
		predLineList.add("" + predFea.isHasInstanceof());
		predLineList.add(predFea.getFirstNum());
		predLineList.add(predFea.getSecondNum());
		predLineList.add("" + predFea.isHasNull());
		partialProgramFeatureStr = StringUtil.join(predLineList, del);
		return partialProgramFeatureStr;
	}
	
}
