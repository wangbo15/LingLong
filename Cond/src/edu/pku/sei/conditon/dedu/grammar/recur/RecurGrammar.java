package edu.pku.sei.conditon.dedu.grammar.recur;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import edu.pku.sei.conditon.dedu.grammar.CondGrammar;
import edu.pku.sei.conditon.dedu.grammar.Tree;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Location;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode.Opcode;
import edu.pku.sei.conditon.util.OperatorUtil;

/**
 * A 2-level plain grammar
 * COND -> PRED(V1, V2...Vn)
 */
public class RecurGrammar extends CondGrammar<RecurTree>{
	
	public RecurTree generateTree(Expression expr) {
		
		CondVisitor visitor = new CondVisitor(expr);
		expr.accept(visitor);
		
		RecurBoolNode root = visitor.getTreeRoot();
		
		return new RecurTree(root);
	}
	
	
	class CondVisitor extends ASTVisitor{
		
		private Expression root;
		private Map<Expression, RecurBoolNode> exprToGrammarNode = new HashMap<>();
		private Map<RecurBoolNode, Expression> grammarNodeToExpr = new HashMap<>();

		private RecurBoolNode treeRoot;
		
		public CondVisitor(Expression root) {
			this.root = root;
		}
		
		public RecurBoolNode getTreeRoot() {
			return treeRoot;
		}

		private RecurBoolNode getDirectParentRecurNode(ASTNode node) {
			ASTNode parent = node.getParent();
			while(parent != null) {
				if(exprToGrammarNode.containsKey(parent)) {
					return exprToGrammarNode.get(parent);
				}
				parent = parent.getParent();
			}
			
			return null;
		}
		
		private void biLinkParent(ASTNode node, RecurBoolNode direcParent, RecurBoolNode current) {
			if(direcParent.getOpcode() == Opcode.NOT || direcParent.getOpcode() == Opcode.NONE) {
				direcParent.setChild0(current);
				current.setLocation(Location.DIRECT);
				return;
			}
			
			assert grammarNodeToExpr.containsKey(direcParent);
			
			InfixExpression infix = (InfixExpression) grammarNodeToExpr.get(direcParent);
			
			ASTNode parent = node;
			while(parent != null) {
				if(parent == infix.getLeftOperand()) {
					direcParent.setChild0(current);
					current.setLocation(Location.LEFT);
					break;
				}else if(parent == infix.getRightOperand()) {
					direcParent.setChild1(current);
					current.setLocation(Location.RIGHT);
					break;
				}
				parent = parent.getParent();
			}
			
		}
		
		private RecurBoolNode processInnerNode(Expression node, String op) {
			RecurBoolNode recurNode; 
			if(node == root) {
				recurNode = new RecurBoolNode(null, op, node);
				recurNode.setLocation(Location.DIRECT);
				this.treeRoot = recurNode;
			}else {
				RecurBoolNode parent = getDirectParentRecurNode(node);
				
				assert parent != null;
				recurNode = new RecurBoolNode(parent, op, node);
				
				biLinkParent(node, parent, recurNode);
			}
			
			exprToGrammarNode.put(node, recurNode);
			grammarNodeToExpr.put(recurNode, node);
			return recurNode;
		}
		
		private void processSpecialRoot(Expression node) {
			if(node == root) {
				assert isChild(node): "MUST BE CHILD";
				treeRoot = new RecurBoolNode(null, Opcode.NONE.toString(), node);
				treeRoot.setLocation(Location.DIRECT);
				
				exprToGrammarNode.put(node, treeRoot);
				grammarNodeToExpr.put(treeRoot, node);
				return;
			}
			
			boolean isChildNode = false;
			ASTNode parent = node.getParent();
			
			if(parent instanceof InfixExpression) {
				InfixExpression infix = (InfixExpression) parent;
				if(OperatorUtil.isBoolOp(infix.getOperator().toString())) {
					if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY || 
							node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY) {
						isChildNode = true;
					}
				}
				
			}else if(node.getParent() instanceof PrefixExpression) {
				PrefixExpression prefix = (PrefixExpression) parent;
				if(OperatorUtil.isBoolOp(prefix.getOperator().toString())) {
					isChildNode = true;
				}
			}
			
			if(isChildNode && isChild(node)) {
				RecurBoolNode parentRecurBoolNode = getDirectParentRecurNode(node);
				
				assert parentRecurBoolNode != null;
				RecurBoolNode recurNode = new RecurBoolNode(parentRecurBoolNode, Opcode.NONE.toString(), node);
				
				biLinkParent(node, parentRecurBoolNode, recurNode);
				
				exprToGrammarNode.put(node, recurNode);
				grammarNodeToExpr.put(recurNode, node);
			}
			
			
			
		}
		
		@Override
		public boolean visit(ArrayAccess node) {
			processSpecialRoot(node);
			return false;
		}
		
		@Override
		public boolean visit(CastExpression node) {
			processSpecialRoot(node);
			return false;
		}
		
		@Override
		public boolean visit(MethodInvocation node) {
			processSpecialRoot(node);
			return false;
		}

		@Override
		public boolean visit(InfixExpression node) {
			String op = node.getOperator().toString();
			boolean child = isChild(node);
			if(!OperatorUtil.isBoolOp(op) && !child){				
				return super.visit(node);
			}
			if(child) {
				op = Opcode.NONE.toString();
			}
			
			processInnerNode(node, op);
			
			if(!child) {
				node.getLeftOperand().accept(this);
				node.getRightOperand().accept(this);
			}
			
			return false;
		}

		@Override
		public boolean visit(PrefixExpression node) {
			String op = node.getOperator().toString();
			boolean child = isChild(node);
			if(!OperatorUtil.isBoolOp(op) && !child){				
				return super.visit(node);
			}
			if(child) {
				op = Opcode.NONE.toString();
			}
			
			RecurBoolNode recurNode = processInnerNode(node, op);
			
			Expression operand = node.getOperand();
			if(isChild(operand)) {
				RecurBoolNode operNode = new RecurBoolNode(recurNode, Opcode.NONE.toString(), operand);
				biLinkParent(node, recurNode, operNode);
				exprToGrammarNode.put(operand, operNode);
				grammarNodeToExpr.put(operNode, operand);
			} else {
				operand.accept(this);
			}
			
			return false;
		}
		
		
		@Override
		public boolean visit(SimpleName node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(ParenthesizedExpression node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(QualifiedName node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(FieldAccess node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(SuperFieldAccess node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(SuperMethodInvocation node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(InstanceofExpression node) {
			processSpecialRoot(node);
			if(node == root) {
				return false;
			}
			return super.visit(node);
		}

		/**
		 * @param node
		 * @return whether the node is a leaf node
		 */
		private boolean isChild(ASTNode node) {
			class OpVisitor extends ASTVisitor{
				boolean child = true;
				
				@Override
				public boolean visit(InfixExpression node) {
					if(!child) {
						return false;
					}
					if(OperatorUtil.isBoolOp(node.getOperator().toString())) {
						child = false;
					}
					return super.visit(node);
				}
				
				@Override
				public boolean visit(PrefixExpression node) {
					if(!child) {
						return false;
					}
					if(OperatorUtil.isBoolOp(node.getOperator().toString())) {
						child = false;
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(ArrayAccess node) {
					if(!child) {
						return false;
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(FieldAccess node) {
					if(!child) {
						return false;
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(MethodInvocation node) {
					if(!child) {
						return false;
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(SuperFieldAccess node) {
					if(!child) {
						return false;
					}
					return super.visit(node);
				}
				
				
			};
			
			OpVisitor visitor = new OpVisitor();
			node.accept(visitor);
			return visitor.child;
		}
		
	}
	
}
