package edu.pku.sei.conditon.dedu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.AssignmentCollector;
import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.auxiliary.FieldCollectorVisitor;
import edu.pku.sei.conditon.auxiliary.FileCommentVisitor;
import edu.pku.sei.conditon.auxiliary.IfBodyControlVisitor;
import edu.pku.sei.conditon.auxiliary.IfConditionVisitor;
import edu.pku.sei.conditon.auxiliary.MtdIfCondNumberVisitor;
import edu.pku.sei.conditon.auxiliary.PlainSyntacticVisitor;
import edu.pku.sei.conditon.auxiliary.StatisticVisitor;
import edu.pku.sei.conditon.auxiliary.UsageVisitor;
import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode;
import edu.pku.sei.conditon.dedu.writer.Writer;
import edu.pku.sei.conditon.ds.MethodInfo;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.ClsCollectorVisitor;
import edu.pku.sei.proj.ProInfo;

public abstract class AbstractDeduVisitor extends ASTVisitor{
	
	public static final String del = Writer.del;
	public static final String NONE = Writer.NONE;
	
	protected CompilationUnit cu;
	protected File file;
	protected char[] rawSource;
	protected List<String> sourceLines;
	protected ProInfo proInfo;
	
	protected MethodDeclaration currentMtdDecl;
	protected MethodInfo currentMtdInfo;

	protected TreeMap<Integer, String> positionToSyntacticMap = new TreeMap<>();

	protected Map<TypeDeclaration, Map<String,SimpleName>> typeDeclToFields = new HashMap<>();

	protected Map<String, Integer> fileCondMap = new HashMap<>();

	
	protected static List<VariableInfo> getNearestNLocalVars(CondVector vec){
		List<VariableInfo> nearestLocs = new ArrayList<>();
		for(int i = 0; i < ContextFeature.NEAREST_LOCAL_NUM; i++){
			VariableInfo curMax = null;
			for(VariableInfo info: vec.getLocals()){
				if(info.getVariableFeature().isField() || info.getDef() == null || nearestLocs.contains(info)){
					continue;
				}
				if(curMax == null || info.getDef().getStartPosition() > curMax.getDef().getStartPosition()){
					curMax = info;
				}
			}
			if(curMax != null){
				nearestLocs.add(curMax);
			}
		}
		return nearestLocs;
	}
	
	
	public static int getTotalCondNum(String varname){
		int totCondNum = 0;
		if(StatisticVisitor.totalIfCondTimeMap.containsKey(varname)){
			totCondNum = StatisticVisitor.totalIfCondTimeMap.get(varname);
		}
		return totCondNum;
	}
	
	protected static List<String> getPredListSortedByVarTime(String varname){
		List<String> result = new ArrayList<>();
		if(StatisticVisitor.varUsedByPredateMap.containsKey(varname)){
			Map<String, Integer> timesMap = StatisticVisitor.varUsedByPredateMap.get(varname);
			for(String key: timesMap.keySet()) {
				result.add(key);
			}
		}
		return result;
	}
	
	protected static List<String> getPredListSortedByTypeTime(String type){
		List<String> result = new ArrayList<>();
		if(StatisticVisitor.typeUsedByPredateMap.containsKey(type)){
			Map<String, Integer> timesMap = StatisticVisitor.typeUsedByPredateMap.get(type);
			for(String key: timesMap.keySet()) {
				result.add(key);
			}
		}
		return result;
	}
	
	protected final String[] getPreviousNMeaningfulLine(int line, int n){
		line -= 2;
		String[] result = new String[n];
		int curIdx = 0;
		for(int i = line; i >= 0; i--){
			if(curIdx == n){
				break;
			}
			String curr = sourceLines.get(i).trim().replaceAll(" ", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\'", "");
			if(curr.length() == 0 || curr.startsWith("//") || curr.startsWith("/*") || curr.startsWith("*")){
				continue;
			} else if(curr.equals("{") || curr.equals("}") || curr.equals("}else{") || curr.startsWith("@")){
				break;
			}
			result[curIdx] = curr.replaceAll("(´|\t)", "");
			curIdx++;
		}
		for(; curIdx < n; curIdx++){
			result[curIdx] = NONE;
		}
		
		return result;
	}
	
	protected final String[] getNextNMeaningFulLine(int line, int n){
		String[] result = new String[n];
		int curIdx = 0;
		for(int i = line; i < sourceLines.size(); i++){
			if(curIdx == n){
				break;
			}
			String curr = sourceLines.get(i).trim().replaceAll(" ", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\'", "");
			if(curr.length() == 0 || curr.startsWith("//") || curr.startsWith("/*") || curr.startsWith("*")){
				continue;
			} else if(curr.equals("{") || curr.equals("}") || curr.equals("}else{") || curr.startsWith("@")){
				break;
			}
			result[curIdx] = curr.replaceAll("(´|\t)", "");
			curIdx++;
		}
		for(; curIdx < n; curIdx++){
			result[curIdx] = NONE;
		}
		return result;		
	}
	
	/**String
	 * 
	 * @param stmt
	 * @param classRepre
	 * @param conditionExpr
	 * @return a pair, the first is locals, the second is this pointer  
	 */
	protected final Pair<List<VariableInfo>, VariableInfo> getAllVarInfo(Statement stmt, ClassRepre classRepre, Expression conditionExpr){
		
		TypeDeclaration td = (TypeDeclaration) ASTLocator.getSpecifiedTypeFather(stmt, TypeDeclaration.class);
		
		String clsName = ClsCollectorVisitor.typeDeclToClassName(cu, td);
		String key = cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		ClassRepre currentClassRepre = proInfo.getProjectRepre().fullNameToClazzesMap.get(key);
		
		assert currentClassRepre != null;
		
		Map<String, SimpleName> currentFieldsMap = typeDeclToFields.get(td);
		
		MethodInfo currentMethodInfo = new MethodInfo(currentMtdDecl, currentClassRepre, currentFieldsMap);
		
		this.currentMtdInfo = currentMethodInfo; 
				
		List<VariableInfo> localVarInfoList = new ArrayList<>();
		ASTLocator.getLocalVariables(stmt, localVarInfoList);
		
		List<VariableInfo> paramVarInfoList = currentMethodInfo.getParamsList();
		
		VariableInfo thisPointer = currentMethodInfo.getThisPointer();
		List<VariableInfo> fieldVarInfoList = currentMethodInfo.getFieldList();
		
		//set whether in cond-expr, only used in training
		if(conditionExpr  != null){
			IfConditionVisitor condVisitor = new IfConditionVisitor(cu, file, currentMtdDecl,
					paramVarInfoList, localVarInfoList, fieldVarInfoList, thisPointer, classRepre);
			conditionExpr.accept(condVisitor);
		}
		
		localVarInfoList.addAll(paramVarInfoList); //add all params to locallist now.
		localVarInfoList.addAll(fieldVarInfoList);
		
		Pair<List<VariableInfo>, VariableInfo> pair = new Pair<>(localVarInfoList, thisPointer);

		return pair;
	}
	
	protected final void setSyntacticContext(ContextFeature context, MethodDeclaration currentMtdDecl, ASTNode node){
		
		assert positionToSyntacticMap.size() > 0;
		
		int mtdStartPos = currentMtdDecl.getStartPosition();
		int mtdEndPos = mtdStartPos + currentMtdDecl.getLength();
		
		int nodeStartPos = node.getStartPosition();
		int nodeEndPos = nodeStartPos + node.getLength();
		
		Queue<String> beforeQueue = new LinkedList<String>();
		Queue<String> bodyQueue = new LinkedList<String>();
		Queue<String> afterQueue = new LinkedList<String>();
		for(Entry<Integer, String> entry: positionToSyntacticMap.entrySet()){
			int currPosition = entry.getKey();
			if(currPosition < mtdStartPos){
				continue;
			}
			if(currPosition >= mtdEndPos){
				break;
			}
			//before stmt 
			if(currPosition < nodeStartPos){
				if(beforeQueue.size() >= ContextFeature.BEFORE_SYNTACTIC_NUM){
					beforeQueue.poll();
				}
				beforeQueue.offer(entry.getValue());
			}
			//stmt body
			if(currPosition >= nodeStartPos && currPosition < nodeEndPos){
				if(bodyQueue.size() >= ContextFeature.BODY_SYNTACTIC_NUM){
					continue;
				}else{
					bodyQueue.offer(entry.getValue());
				}
			}
			//after stmt
			if(afterQueue.size() >= ContextFeature.AFTER_SYNTACTIC_NUM){
				break;
			}
			if(currPosition >= nodeEndPos){
				afterQueue.offer(entry.getValue());
			}
		}
		
		context.setBeforeSyntacticQueue(beforeQueue);
		context.setBodySyntacticQueue(bodyQueue);
		context.setAfterSyntacticQueue(afterQueue);
	}
	
	protected final ContextFeature generateContextFeature(String clsName, MethodDeclaration mtdDecl, ASTNode node, int prevNLn, int nextNLn){
		
		int mtdLineNum = cu.getLineNumber(mtdDecl.getStartPosition() + mtdDecl.getLength()) - cu.getLineNumber(mtdDecl.getName().getStartPosition());
		boolean inLoop = ASTLocator.inLoopStmt(node);
		
		ContextFeature res = new ContextFeature(file, clsName, mtdDecl.getName().getIdentifier(), 
				mtdDecl.getModifiers(), mtdLineNum, inLoop);
		
		res.setBodyCtl(getBodyCtlStr(node));
		setSyntacticContext(res, mtdDecl, node);
		
		String preCond = getPreviousIfCondPred(mtdDecl, node);
		res.setPreviousCond(preCond);
		
		String prePred = DollarilizeVisitor.dollarilize(preCond);
		res.setPreviousPred(prePred);
		
		String[] pre2stmt = this.getPreviousNMeaningfulLine(prevNLn, 2);
		res.setPreviousStmt0(pre2stmt[0]);
		res.setPreviousStmt1(pre2stmt[1]);
		
		String[] next2stmt = this.getNextNMeaningFulLine(nextNLn, 2);
		res.setNextStmt0(next2stmt[0]);
		res.setNextStmt1(next2stmt[1]);
		return res;
	}
	
	protected final String getBodyCtlStr(ASTNode node){
		if (node instanceof IfStatement) {
			Statement thenStmt = ((IfStatement) node).getThenStatement();
			IfBodyControlVisitor ifBodyCtrVisitor = new IfBodyControlVisitor();
			thenStmt.accept(ifBodyCtrVisitor);
			return ifBodyCtrVisitor.getEncodeStr();
		} else if(node instanceof ReturnStatement || node instanceof ThrowStatement) {
			IfBodyControlVisitor ifBodyCtrVisitor = new IfBodyControlVisitor();
			node.accept(ifBodyCtrVisitor);
			return ifBodyCtrVisitor.getEncodeStr();
		} else if (node instanceof ForStatement) {
			return "NIL-FOR";
		} else if (node instanceof WhileStatement) {
			return "NIL-WHILE";
		} else if (node instanceof DoStatement) {
			return "NIL-DO";
		} else if(node instanceof ConditionalExpression){
			return "NIL-CD";
		} else if(node instanceof Expression){
			return "NIL-AS";
		} else {
			return "NIL-UN";
		}
	}
	
	public final void setMethodIfCondNum(List<VariableInfo> localVarInfoList, VariableInfo thisPointer){
		MtdIfCondNumberVisitor condNumberVisitor = new MtdIfCondNumberVisitor(cu, currentMtdDecl, file, thisPointer, localVarInfoList);
		currentMtdDecl.accept(condNumberVisitor);
	}
	
	public final void setFileIfCondNum(List<VariableInfo> localVarInfoList) {
		for(VariableInfo info: localVarInfoList){
			String literal = info.getNameLiteral();
			int fileCondNum = 0;
			if(fileCondMap.containsKey(literal)){
				fileCondNum = fileCondMap.get(literal);
			}
			info.getVariableFeature().setFileIfCondNumber(fileCondNum);
		}
	}
	
	public final void setAssignment(Statement stmt, List<VariableInfo> localVarInfoList, VariableInfo thisPointer){
		AssignmentCollector assCollector = new AssignmentCollector(currentMtdDecl, cu, file);
		
		for(VariableInfo info : localVarInfoList){
			if(info.getVariableFeature().isSuperField()){
				continue;
			}
			
			ASTNode lastAssignExpr = assCollector.getLocalLastAssign(info, info.getDef(), stmt);
			if(lastAssignExpr != null){
				//namely dis0
				int lastAssDis = cu.getLineNumber(stmt.getStartPosition()) - cu.getLineNumber(lastAssignExpr.getStartPosition());
				info.getVariableFeature().setLastAssignDis(lastAssDis);
				
			}
			AssignInfo lastAssStr = assCollector.getLocalLastAssignType(info.getDef(), lastAssignExpr);
			info.getVariableFeature().setLastAssign(lastAssStr);
			
			if(lastAssStr.equals(VariableInfo.PARAMETER_ASSIGN)){
				int lastAssDis = cu.getLineNumber(stmt.getStartPosition()) - cu.getLineNumber(currentMtdDecl.getName().getStartPosition());
				info.getVariableFeature().setLastAssignDis(lastAssDis);
			}
		}//end for(VariableInfo info : localVarInfoList)
		
		ASTNode thisPtrLastAssExpr = assCollector.getThisPtrLastAssign(thisPointer, stmt);
		if(thisPtrLastAssExpr != null){
			int dis = cu.getLineNumber(stmt.getStartPosition()) - cu.getLineNumber(thisPtrLastAssExpr.getStartPosition());
			thisPointer.getVariableFeature().setLastAssignDis(dis);
		}
		
		AssignInfo thisLastAssStr = assCollector.getThisPtrLastAssignType(thisPtrLastAssExpr); 
		thisPointer.getVariableFeature().setLastAssign(thisLastAssStr);
	}
	
	public final void setSimpleUsage(Statement stmt, List<VariableInfo> localVarInfoList, VariableInfo thisPointer){
		//usage within the stmt
		UsageVisitor usageVisitor = new UsageVisitor(false, cu, currentMtdDecl, file, thisPointer, localVarInfoList);
		ASTNode collectReange = null;
		
		if (stmt instanceof IfStatement) {
			
			collectReange = ((IfStatement) stmt).getThenStatement();

		} else if (stmt instanceof ForStatement) {

			collectReange = ((ForStatement) stmt).getBody();
			
		} else if (stmt instanceof WhileStatement) {
			
			collectReange = ((WhileStatement) stmt).getBody();
			
		} else if (stmt instanceof DoStatement) {
			
			collectReange = ((DoStatement) stmt).getBody();
			
		}else {
			collectReange = stmt;
		}
		
		collectReange.accept(usageVisitor);
				
		if(usageVisitor.hitAll() == false){
			usageVisitor.setNoUse();
		}
		
		//outer use
		Block fatherBlk = (Block) ASTLocator.getSpecifiedTypeFather(stmt, Block.class);
		int nodeEnd = cu.getLineNumber(stmt.getStartPosition() + stmt.getLength());
		int blkEnd = cu.getLineNumber(fatherBlk.getStartPosition() + fatherBlk.getLength());
		UsageVisitor outUseVisitor = new UsageVisitor(false, cu, currentMtdDecl, file, 
				thisPointer, localVarInfoList, nodeEnd, blkEnd);
		fatherBlk.accept(outUseVisitor);
		outUseVisitor.setNoUse();
	}
	
	public final void setDocInformation(MethodDeclaration currentMtdDecl, List<VariableInfo> localVarInfoList) {
		FileCommentVisitor.getFileCommentMsg(cu, currentMtdDecl.getStartPosition(), localVarInfoList);
	}
	
	public final void setPreviousCond(final List<VariableInfo> locals, final int line) {
		for(VariableInfo info: locals) {
			String preCond = getPreviousCondForVar(info, line);
			info.getVariableFeature().setPreviousCond(preCond);
		}
	}
	
	private String getPreviousCondForVar(final VariableInfo info, final int lineNum) {
		
		class IfVisitor extends ASTVisitor{
			
			public Expression preExpr;
			
			private boolean inBound(ASTNode node, int line){
				int nodeBegin = cu.getLineNumber(node.getStartPosition());
				int nodeEnd = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if(line >= nodeBegin && line <= nodeEnd){
					return true;
				}
				return false;
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				if(inBound(node, lineNum) == false){
					return false;
				}
				
				return super.visit(node);
			}
			
			@Override
			public boolean visit(IfStatement ifNode) {
				if(before(ifNode)){
					Expression expr = ifNode.getExpression();
					String str = expr.toString();
					String[] names = str.split("\\W");
					for(String name: names){
						if(name.equals(info.getNameLiteral())){
							this.preExpr = expr;
							break;
						}
					}
				}
				return super.visit(ifNode);
			}
			private boolean before(IfStatement ifNode){
				int exprLine = AbstractDeduVisitor.this.cu.getLineNumber(ifNode.getExpression().getStartPosition());
				if(exprLine < lineNum){
					return true;
				}
				return false;
			}
		}
		
		IfVisitor visitor = new IfVisitor();
		cu.accept(visitor);
				
		if(visitor.preExpr != null){
			return DollarilizeVisitor.naiveDollarilize(visitor.preExpr);
		}
		return "NIL";
	}
	
	public final ClassRepre getCurrentClassRepre(String clsName, ASTNode node){
		String key = cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		ClassRepre currentClassRepre = proInfo.getProjectRepre().fullNameToClazzesMap.get(key);
		assert currentClassRepre != null;
		return currentClassRepre;
	} 
	
	public final ClassRepre getCurrentClassRepre(ASTNode node){
		String clsName = this.getClsName(node);
		return this.getCurrentClassRepre(clsName, node);
	} 
	
	public final String getClsName(ASTNode node){
		TypeDeclaration td = (TypeDeclaration) ASTLocator.getSpecifiedTypeFather(node, TypeDeclaration.class);
		String clsName = ClsCollectorVisitor.typeDeclToClassName(cu, td);
		return clsName;
	}
	
	public static final String getPredType(Expression expr){
		if(expr instanceof MethodInvocation){
			return ((MethodInvocation) expr).getName().getIdentifier();
		}else if(expr instanceof InfixExpression){
			return ((InfixExpression) expr).getOperator().toString();
		}else{
			return expr.getClass().getName();
		}
	}
	
	private String getPreviousIfCondPred(MethodDeclaration mtdDecl, ASTNode node){
		/** get previous if condition*/
		IfStatement previosIf = ASTLocator.getPreviousIf(currentMtdDecl, node);
		if(previosIf == null){
			return "NIL";			
		}
		//TODO:: dolar 
		Expression previosCond = previosIf.getExpression();
//		Pair<List<VariableInfo>, VariableInfo> pair = getAllVarInfo(previosIf, this.getCurrentClassRepre(node), previosCond);
		String result = previosCond.toString();
		return Writer.predForCSV(result);
	}
	
	/******************************************************************/
	@Override
	public final boolean visit(CompilationUnit node) {
		PlainSyntacticVisitor syntacticVisitor = new PlainSyntacticVisitor(this.positionToSyntacticMap);
		node.accept(syntacticVisitor);
		return super.visit(node);
	}
	
	@Override
	public final boolean visit(EnumDeclaration node) {
		return false;
	}
	
	@Override
	public final boolean visit(Initializer node) {
		return false;
	}
	
	@Override
	public final boolean visit(TypeDeclarationStatement node) {
		return false;
	}

	@Override
	public final boolean visit(AnonymousClassDeclaration node) {
		return false;
	}
	
	@Override
	public final boolean visit(LambdaExpression node){
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		currentMtdDecl = node;
		return super.visit(node);
	}
	
	@Override
	public final void endVisit(MethodDeclaration node) {
		currentMtdDecl = null;
		currentMtdInfo = null;
		super.endVisit(node);
	}
	
	@Override
	public final boolean visit(TypeDeclaration node) {
		
		Map<String, SimpleName> currentFieldsMap = new HashMap<String, SimpleName>();
		
		typeDeclToFields.put(node, currentFieldsMap);
		
		FieldCollectorVisitor fldVisitor = new FieldCollectorVisitor(currentFieldsMap);
		
		ASTNode curNode = node;
		while(true){
			ASTNode parent = curNode.getParent();
			if(parent instanceof TypeDeclaration){
				curNode = parent;
			}else{
				break;
			}
		}
		
		curNode.accept(fldVisitor);
		
		return super.visit(node);
	}
	
	
	protected class FileCondVisitor extends ASTVisitor{

		@Override
		public boolean visit(ConditionalExpression node) {
			if(node.getExpression() instanceof BooleanLiteral == false){
				IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
				node.getExpression().accept(condTimeVisitor);
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(DoStatement node) {
			if(node.getExpression() instanceof BooleanLiteral == false){
				IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
				node.getExpression().accept(condTimeVisitor);
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(ForStatement node) {
			if(node.getExpression() != null){
				IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
				node.getExpression().accept(condTimeVisitor);
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(IfStatement node) {
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
			return super.visit(node);
		}

		@Override
		public boolean visit(WhileStatement node) {
			if(node.getExpression() instanceof BooleanLiteral == false){
				IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
				node.getExpression().accept(condTimeVisitor);
			}
			
			return super.visit(node);
		}
		
	}
	
	protected class IfCondTimeVisitor extends ASTVisitor{
		
		@Override
		public boolean visit(SimpleName node) {
			
			String varName = node.getIdentifier();
			if(fileCondMap.containsKey(varName)){
				int oldVal = fileCondMap.get(varName);
				fileCondMap.put(varName, oldVal + 1);
			}else{
				fileCondMap.put(varName, 1);
			}

			return super.visit(node);
		}

	}
}
