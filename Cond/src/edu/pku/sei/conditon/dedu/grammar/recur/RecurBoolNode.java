package edu.pku.sei.conditon.dedu.grammar.recur;

import static edu.pku.sei.conditon.dedu.AbstractDeduVisitor.del;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.Feature;
import edu.pku.sei.conditon.dedu.grammar.BoolNode;
import edu.pku.sei.conditon.dedu.grammar.GrammarNode;
import edu.pku.sei.conditon.util.StringUtil;

public class RecurBoolNode extends BoolNode {
	
	private static final long serialVersionUID = 7520254481954048382L;

	public static enum Location{
		LEFT,
		RIGHT,
		DIRECT	/* root is set as DIRECT */
	}
	
	public static class Opcode{
		private final String token;
		private final int positionNum;
		
		private Opcode(String token, int positionNum) {
			this.token = token;
			this.positionNum = positionNum;
		}
		
		public String toString() {
			return this.token;
		}
		
		public int toLabel(){
			return CODES_TO_LABEL.get(this);
		}
		
		public int getPositionNum() {
			return this.positionNum;
		}
		
		public static final Opcode NONE = new Opcode("NONE", 1);
		public static final Opcode NOT = new Opcode("!", 1);
		public static final Opcode AND = new Opcode("&&", 2);
		public static final Opcode OR = new Opcode("||", 2);
				
		private static final Map<String, Opcode> CODES = new HashMap<String, Opcode>();
		private static final Map<Opcode, Integer> CODES_TO_LABEL = new HashMap<Opcode, Integer>(); 
		private static final Map<Integer, Opcode> LABEL_TO_CODE = new HashMap<Integer, Opcode>(); 
		static {
			CODES.put(AND.toString(), AND);
			CODES.put(OR.toString(), OR);
			CODES.put(NOT.toString(), NOT);
			CODES.put(NONE.toString(), NONE);
			
			CODES_TO_LABEL.put(NONE, 0);
			LABEL_TO_CODE.put(0, NONE);
			CODES_TO_LABEL.put(AND, 1);
			LABEL_TO_CODE.put(1, AND);
			CODES_TO_LABEL.put(OR, 2);
			LABEL_TO_CODE.put(2, OR);
			CODES_TO_LABEL.put(NOT, 3);
			LABEL_TO_CODE.put(3, NOT);
		}
		
		public static Opcode toOpcode(String token) {
			return (Opcode) CODES.get(token);
		}
		
		public static Opcode labelToOpcode(int label) {
			assert LABEL_TO_CODE.containsKey(label);
			return LABEL_TO_CODE.get(label);
		}
	}
	
	private RecurBoolNode child0 = null;
	private RecurBoolNode child1 = null;
	private Opcode opcode = null;
	private Expression expr;
	private Location locationInParent;
	
	/** distance to the root */
	private int depthLevel;
	
	/** distance to the leaf */
	private int hight;
	
	private CondVector condVector;
	
	/**
	 * only used for the predictor
	 * @return
	 */
	public static RecurBoolNode getEmptyRecurBoolNode(GrammarNode parent, Location loc) {
		RecurBoolNode res = new RecurBoolNode(parent, Opcode.NONE.toString(), null);
		res.setLocation(loc);
		return res;
	}
	
	RecurBoolNode(GrammarNode parent, String op, Expression expr) {
		super(parent);
		this.opcode = Opcode.toOpcode(op);
		
		if(this.opcode == null) {
			throw new IllegalArgumentException(op);
		}
		this.expr = expr;
	}
	
	@Override
	public RecurBoolNode getParent() {
		return (RecurBoolNode) parent;
	}

	public Opcode getOpcode() {
		return this.opcode;
	}
	
	public RecurBoolNode getChild0() {
		return child0;
	}

	public void setChild0(RecurBoolNode setChild0) {
		this.child0 = setChild0;
	}

	public RecurBoolNode getChild1() {
		return child1;
	}

	public void setChild1(RecurBoolNode child1) {
		this.child1 = child1;
	}

	/**
	 * Only used in training steps
	 * @return
	 */
	public Expression getExpr() {
		assert this.expr != null;
		return this.expr;
	}
	
	public void setLocation(Location location) {
		assert location != null;
		this.locationInParent = location;
	}
	
	public Location getLocation() {
		return this.locationInParent;
	}
	
	public int getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(int depthLevel) {
		this.depthLevel = depthLevel;
	}
	
	public int getHight() {
		return hight;
	}

	public void setHight(int hight) {
		this.hight = hight;
	}

	public void setCondVector(CondVector condVec) {
		assert this.opcode == Opcode.NONE;
		assert condVec != null;
		this.condVector = condVec;
	}
	/**
	 * @return null if this node's Opcode is not NONE
	 */
	public CondVector getCondVector() {
		return this.condVector;
	}
	
	private RecurBoolNode(RecurBoolNode copy, RecurBoolNode parent) {
		super(parent); //TODO: new 
		this.opcode = copy.opcode;
		this.locationInParent = copy.locationInParent;
		this.depthLevel = copy.depthLevel;
	}
	
	private static void cloneChild(RecurBoolNode from, RecurBoolNode to) {
		if(from.opcode.equals(NONE)) {
			return;
		}
		if(from.child0 != null) {
			to.child0 = new RecurBoolNode(from.child0, to);
			cloneChild(from.child0, to.child0);
		}
		
		if(from.child1 != null) {
			to.child1 = new RecurBoolNode(from.child1, to);
			cloneChild(from.child1, to.child1);
		}
	}
	
	public static RecurBoolNode deepCloneFromRootForSynthesis(RecurBoolNode root) {
		assert isRoot(root);
		RecurBoolNode newRoot = new RecurBoolNode(root, null);
		cloneChild(root, newRoot);
		return newRoot;
	}
	
	public static boolean isRoot(RecurBoolNode root) {
		return root.getParent() == null && root.getDepthLevel() == 0;
	}
	
	
	@Override
	public String toString() {
		if(this.opcode == Opcode.NONE) {
			return "#";
		} else if(this.opcode == Opcode.NOT) {
			return "(" + Opcode.NOT + " " + (child0 == null ? "#" : child0.toString()) + ")";
		} else {
			return "(" + (child0 == null ? "#" : child0.toString()) + " " + this.opcode + " " + (child1 == null ? "#" : child1.toString()) + ")";
		}
	}
	

	private String downwardFeature = null;

	public String genDownwardFeature() {
		if(downwardFeature != null) {
			return downwardFeature;
		}
		List<String> lineList = new ArrayList<>();
		boolean isRoot = this.parent == null;
		lineList.add("" + isRoot);
		if(isRoot) {
			lineList.add(Opcode.NONE.toString());	//parent type
			lineList.add(AbstractDeduVisitor.NONE); //sibling type
		}else {
			RecurBoolNode parent  = (RecurBoolNode) this.getParent();
			lineList.add(parent.getOpcode().toString());//parent type
			
			switch(locationInParent) {//sibling type
			case LEFT:
				if(parent.getChild1() == null) {
					lineList.add(AbstractDeduVisitor.NONE);
				}else {
					lineList.add(parent.getChild1().getOpcode().toString());
				}
				break;
			case RIGHT:
				if(parent.getChild0() == null) {
					lineList.add(AbstractDeduVisitor.NONE);
				}else {
					lineList.add(parent.getChild0().getOpcode().toString());
				}
				break;
			case DIRECT:
				lineList.add(AbstractDeduVisitor.NONE);
				break;
			}
			

		}
		lineList.add(locationInParent.toString());
		lineList.add("" + depthLevel);
		
		downwardFeature = StringUtil.join(lineList, del);
		return downwardFeature;
	}
	
	/** NIL represents empty */
	private final static String NIL = "NIL"; 
	
	private String upwardFeature = null;
	/**
	 * @return Feature list: "nodetp	cld0tp	cld1tp	hight"
	 */
	public String genUpwardFeature() {
		if (upwardFeature != null) {
			return upwardFeature;
		}
		
		List<String> lineList = new ArrayList<>();
		lineList.add(this.getOpcode().toString());
		
		if(child0 == null) {
			lineList.add(NIL);
		} else {
			lineList.add(child0.getOpcode().toString());
		}
		
		if (child1 == null) {
			lineList.add(NIL);
		} else {
			lineList.add(child0.getOpcode().toString());
		}
		
		lineList.add("" + hight);
		upwardFeature = StringUtil.join(lineList, del);
		return upwardFeature;
	}

	
	public static String getFeatureHeader() {
		return Feature.genFeatureHeaderFromList("recur");
	}
}

