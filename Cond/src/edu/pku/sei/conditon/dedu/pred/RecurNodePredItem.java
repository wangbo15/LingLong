package edu.pku.sei.conditon.dedu.pred;

import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;;

public class RecurNodePredItem extends AbsPredItem{
	private Opcode opcode;
	
	public RecurNodePredItem(int label, double score) {
		super(score);
		this.opcode = Opcode.labelToOpcode(label);
	}

	public Opcode getOpcode(){
		return this.opcode;
	}
	
	@Override
	public String toString() {
		return  opcode.toString() + ": " + score;
	}
	
}
