package edu.pku.sei.conditon.dedu.feature;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.dedu.grammar.recur.RecurTree;
import edu.pku.sei.conditon.ds.VariableInfo;

public class TreeVector extends AbstractVector{

	private RecurTree tree;
	
	private Expression expr;
	
	public TreeVector(int id, int line, int col, ContextFeature contextFeature, Expression expr,
			RecurTree tree, List<VariableInfo> allLocals) {
		super(id, contextFeature.getFileName(), line, col, contextFeature, allLocals);
		this.expr = expr;
		this.tree = tree;
	}
	
	public Expression getExpr() {
		return expr;
	}
	
	public RecurTree getTree() {
		return tree;
	}

	@Override
	public String toString() {
		return fileName + "#" + line + " : " + tree;
	}
	
}
