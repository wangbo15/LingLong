package edu.pku.sei.conditon.auxiliary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.ds.VariableInfo;

public class AssignmentCollector {
	
	private static Logger logger = Logger.getLogger(AssignmentCollector.class);  
	private Set<String> logWarnMsg = new HashSet<>();

	
	private MethodDeclaration currentMtdDecl;
	private CompilationUnit cu;
	private File file;
//	private IVariableBinding[] fieldsBindings;
		
	public AssignmentCollector(MethodDeclaration currentMtdDecl, CompilationUnit cu, File file) {
		super();
		this.currentMtdDecl = currentMtdDecl;
		this.cu = cu;
		this.file = file;
		
//		if(currentMtdDecl.getParent() instanceof TypeDeclaration){
//			TypeDeclaration td = (TypeDeclaration) currentMtdDecl.getParent();
//			
//			fieldsBindings = td.resolveBinding().getDeclaredFields();
//		}
		
	}

	private List<ASTNode> getThisPtrAssignmentList(){
		List<ASTNode> res = new ArrayList<>();
		AssignmentCollectorVisitor assignCollector = new AssignmentCollectorVisitor(res);
		currentMtdDecl.accept(assignCollector);
		return res;
	}
		
	private List<ASTNode> getLocalAssignmentList(SimpleName node) {

		List<ASTNode> res = new ArrayList<>();

		//for locals
		if (node.resolveBinding() instanceof IVariableBinding) {
			
			IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();

			AssignmentCollectorVisitor assignCollector = new AssignmentCollectorVisitor(res, variableBinding);
			currentMtdDecl.accept(assignCollector);

			// TODO: to be learning
			/*
			 * OccurrencesFinder finder= new OccurrencesFinder();
			 * OccurrenceLocation[] locations = finder.getOccurrences();
			 */
		}

		return res;
	}
	
	private boolean isBranchStatementBody(ASTNode node){
		if(node == null){
			return false;
		}
		return node.getLocationInParent() == IfStatement.THEN_STATEMENT_PROPERTY || node.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY
				|| node.getLocationInParent() == ForStatement.BODY_PROPERTY 
				|| node.getLocationInParent() == WhileStatement.BODY_PROPERTY 
				|| node.getLocationInParent() == DoStatement.BODY_PROPERTY
				|| node.getLocationInParent() == EnhancedForStatement.BODY_PROPERTY;
	}
	
	private boolean inBranchStatementBlock(ASTNode node){
		ASTNode parent = ASTLocator.getSpecifiedTypeFather(node, Block.class);
		return isBranchStatementBody(parent);
	}
	
	private ASTNode getMostForwardLine(VariableInfo varinfo, List<ASTNode> assignList, int line) {
		ASTNode last = null;
		int lastPosition = 0;
		int preAssNum = 0;
		
		for (ASTNode assign : assignList) {

			int assignLine = cu.getLineNumber(assign.getStartPosition());

			if (assignLine <= line && assign.getStartPosition() > lastPosition) {
				last = assign;
				lastPosition = assign.getStartPosition();
				preAssNum++;
			}
		}
		varinfo.getVariableFeature().setPreAssNum(preAssNum);
		return last;
	}
	
	public ASTNode getThisPtrLastAssign(VariableInfo thisInfo, ASTNode startPoint) {
		List<ASTNode> assignList = getThisPtrAssignmentList();
		int nodeLine = cu.getLineNumber(startPoint.getStartPosition());
		return getMostForwardLine(thisInfo, assignList, nodeLine);
	}
	
	public ASTNode getLocalLastAssign(VariableInfo varinfo, SimpleName node, ASTNode startPoint) {
		List<ASTNode> assignList = getLocalAssignmentList(node);
		int nodeLine = cu.getLineNumber(startPoint.getStartPosition());
		return getMostForwardLine(varinfo, assignList, nodeLine);
	}
	
	public AssignInfo getThisPtrLastAssignType(ASTNode lastAssignExpr){
		if(lastAssignExpr == null){
			return AssignInfo.getUnknowAssign();
		}else{
			return new AssignInfo(lastAssignExpr);
		}
	}
	
	public AssignInfo getLocalLastAssignType(SimpleName node, ASTNode lastAssignExpr){
		
		if(node.resolveBinding() == null){
			int line = cu.getLineNumber(node.getStartPosition());
			String location = file.getName() + " @ " + currentMtdDecl.getName().getIdentifier() + "(), VAR: " + node.getIdentifier() + " : " + line;
			
			if(!logWarnMsg.contains(location)){
				logWarnMsg.add(location);
				logger.warn("Null Binding @ getLocalLastAssignType() : " + location);	
			}
			
			return AssignInfo.getUnknowAssign();
		}
		
		IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();
				
		if(lastAssignExpr == null){
			if(variableBinding.isField()){
				return AssignInfo.getFieldAssign();
			}else if(variableBinding.isParameter()){
				return AssignInfo.getParameterAssign();
			}else{
				return AssignInfo.getUnknowAssign();
			}
		}else{
			return new AssignInfo(lastAssignExpr);
		}
	}
		
}
