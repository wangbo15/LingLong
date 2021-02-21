package edu.pku.sei.conditon.dedu.grammar;

import org.eclipse.jdt.core.dom.Expression;

public abstract class CondGrammar<T extends Tree> {
	
	public abstract T generateTree(Expression expr);
	
}
