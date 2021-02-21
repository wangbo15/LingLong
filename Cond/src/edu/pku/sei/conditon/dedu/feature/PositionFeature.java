package edu.pku.sei.conditon.dedu.feature;

public class PositionFeature extends Feature {
	private PredicateFeature predicate;
	
	private int position;
	private String locOfFather;
	private String leftNode;
	
	public PositionFeature(PredicateFeature predicate, int position, String locOfFather, String leftNode) {
		super();
		this.predicate = predicate;
		this.position = position;
		this.locOfFather = locOfFather;
		this.leftNode = leftNode;
	}

	public PredicateFeature getPredicate(){
		return predicate;
	}
	
	public int getPosition() {
		return position;
	}
	
	public String getLocOfFather() {
		return locOfFather;
	}

	public String getLeftNode() {
		return leftNode;
	}

}
