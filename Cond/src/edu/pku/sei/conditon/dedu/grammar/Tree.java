package edu.pku.sei.conditon.dedu.grammar;

import java.util.List;

import edu.pku.sei.conditon.dedu.feature.ContextFeature;

/**
 * Tree is an inmutable data structure. 
 * @author nightwish
 *
 * @param <T>
 */
public abstract class Tree <T extends GrammarNode> {
	
	protected T root;
	protected Order order;
	
	protected int nodeNum;
	protected int hight;
	protected int leafNum;
	
	public Tree(T root) {
		assert root != null;
		this.root = root;
	}
	
	public abstract List<T> broadFristSearchTraverse();
	public abstract void dump();
	
	@Override
	public String toString() {
		return this.root.toString();
	}
	
	public T getRoot() {
		return root;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public int getNodeNum() {
		return nodeNum;
	}
	
	public int getHight() {
		return hight;
	}
	
	public int getLeafNum() {
		return leafNum;
	}
	
}
