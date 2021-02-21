package edu.pku.sei.conditon.dedu.grammar.plain;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.dedu.grammar.CondGrammar;
import edu.pku.sei.conditon.dedu.grammar.Tree;

/**
 * A 2-level plain grammar
 * COND -> PRED(V1, V2...Vn)
 */
public class PlainGrammar extends CondGrammar{

	@Override
	public Tree generateTree(Expression expr) {
		VarVisitor visitor = new VarVisitor();
		expr.accept(visitor);
		
		
		
		return null;
	}
	
	class VarVisitor extends ASTVisitor {
		List<SimpleName> vars = new ArrayList<>(); 
		
		public boolean visit(SimpleName node) {
			if(Character.isUpperCase(node.getIdentifier().charAt(0))){
				return false;
			}
			if(!ASTLocator.notVarLocation(node) && !ASTLocator.maybeConstant(node.getIdentifier())){
				vars.add(node);
			}
			return false;
		}
	}

}
