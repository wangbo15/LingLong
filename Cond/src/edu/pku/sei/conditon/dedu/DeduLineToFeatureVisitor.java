package edu.pku.sei.conditon.dedu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.auxiliary.StatisticVisitor;
import edu.pku.sei.conditon.auxiliary.UsageVisitor;
import edu.pku.sei.conditon.auxiliary.ds.AssignInfo;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.ProInfo;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 26, 2017
 */
public final class DeduLineToFeatureVisitor extends AbstractDeduVisitor {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	private static Logger logger = Logger.getLogger(DeduLineToFeatureVisitor.class);  

	/**
	 * suspicious id
	 */
	private String sid;
	
	private int line;
	
	private String contextRes;
	
	private final static int DEFAULT_VAR_NUM = 1 << 5; 
	
	private Map<String, String> varToCtxAndVarFeaPrefix = new HashMap<>(DEFAULT_VAR_NUM); // varname -> ctx feature, var feature
	private Map<String, String> varToVarFea = new HashMap<>(DEFAULT_VAR_NUM); // varname ->var feature only
	
	private ASTNode hitNode;
	
	private List<VariableInfo> varInfos = new ArrayList<>(DEFAULT_VAR_NUM);;
	
	public ASTNode getHitNode(){
		return hitNode;
	}

	public Map<String, String> getVarToCtxAndVarFeaPrefix(){
		return varToCtxAndVarFeaPrefix;
	}
	
	public Map<String, String> getVarToVarFea(){
		return varToVarFea;
	}
	
	public String getContextResult() {
		return contextRes;
	}

	public List<VariableInfo> getVarInfos(){
		assert this.varInfos != null;
		return this.varInfos;
	}
	
	public File getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public DeduLineToFeatureVisitor(CompilationUnit cu, File file, ProInfo proInfo, int line, String sid) {

		super();
		this.cu = cu;
		this.file = file;
		this.proInfo = proInfo;
		this.line = fixLine(line);
		this.sourceLines = JavaFile.readFileToStringList(file);
		
		this.sid = sid;
		
		FileCondVisitor visitor = new FileCondVisitor();
		cu.accept(visitor);
	}
	
	private boolean skipNoneMeaningLine(String line) {
		return line.length() == 0 
				|| line.equals("}") 
				|| line.equals("{") 
				|| line.startsWith("//")
				|| line.startsWith("/*") 
				|| line.trim().equals("if (")
				|| line.trim().equals("if(") 
				|| line.startsWith("throw ")
				|| (line.contains("}") && line.contains("{") && !line.contains("else") && !line.contains("if") && !line.contains("[]"));
	}
	
	private int fixLine(int line){
		String code = getCodeFromFile();
		String[] lines = code.split("\n");
		int currentLine = -1;
		for(currentLine = lines.length; currentLine >= 1; currentLine--){
			if(currentLine > line){
				continue;
			}else{
				String curLineStr = lines[currentLine - 1].trim();
//				System.err.println(currentLine + " : " + curLineStr);
				if(skipNoneMeaningLine(curLineStr)){
					continue;
				}else{
					break;
				}
			}
			
		}
		System.out.println("FIX\t" + line + "\tTO\t" + currentLine + ": " + lines[line - 1].trim() + "  >>>>  " + lines[currentLine - 1].trim());

		return currentLine;
	}
	
	public String getLineFromCode(String code, int line){
		int lineNum = 0;
        for (String lineString: code.split("\n")){
            lineNum++;
            if (lineNum == line){
                return lineString.trim();
            }
        }
        return "";
	}
	
    public String getCodeFromFile(){
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			byte[] b= new byte[stream.available()];
			int len = stream.read(b);
			if (len <= 0){
				throw new IOException("Source code file "+ file.getAbsolutePath() + " read fail!");
			}
			stream.close();
			return new String(b);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "";
		} finally {
			if (stream!=null){
				try {
					stream.close();
				} catch (IOException e){
				}
			}
		}
    }
	

/*****************************************************************/

	private boolean inBound(ASTNode node){
		int nodeBegin = cu.getLineNumber(node.getStartPosition());
		int nodeEnd = cu.getLineNumber(node.getStartPosition() + node.getLength());
		if(line >= nodeBegin && line <= nodeEnd){
			return true;
		}
		return false;
	}
	
	private void generateRes(CondVector vec){
		String contextFeature = genContextFeatureStr(vec);
		
		this.contextRes = contextFeature;
		
		Set<String> locNameSet = new HashSet<>();
		for(VariableInfo info: vec.getLocals()){
			if(! info.getVariableFeature().isField()){
				locNameSet.add(info.getNameLiteral());
			}
		}
		
		for(VariableInfo info: vec.getLocals()){
			if(info.getVariableFeature().isField()){
				if(!StatisticVisitor.totalIfCondTimeMap.containsKey(info.getNameLiteral())){
					if(!CONFIG.isPredAll()) {
						continue;
					}
				}
				
				if(Character.isUpperCase(info.getNameLiteral().charAt(0))){
					continue;
				}
//				if(locNameSet.contains(info.getNameLiteral())){
//					continue;
//				}
			}
			String infoTp = info.getType();
			if(infoTp.contains("<") && infoTp.contains(">")){
				infoTp = infoTp.split("<")[0];
				info.setType(infoTp);
			}
			
			this.varInfos.add(info);
			
			String varFeaPrefix = genVarFeature(info);
			String ctxAndVarLine = StringUtil.connectMulty(del, contextFeature, varFeaPrefix);

			String varName = info.getNameLiteral();
			varToCtxAndVarFeaPrefix.put(varName, ctxAndVarLine);
			varToVarFea.put(varName, varFeaPrefix);
			
		}//for(VariableInfo info: vec.getLocals())
		
	}
	
	private String genVarFeature(VariableInfo info){
		String literal = info.getNameLiteral();
		int fileCondNum = info.getVariableFeature().getFileIfCondNumber();
		int totCondNum = getTotalCondNum(literal);
		String previousCond = getPreviousCondForVar(info);
		VariableFeature varfea = info.getVariableFeature();
		List<String> variableFeatureList = new ArrayList<>();
		
		String type = info.getType();
		if(type.contains("<") && type.contains(">")){
			type = type.split("<")[0];
		}
		
		variableFeatureList.add(info.getNameLiteral());
		variableFeatureList.add(type);
		
		//varname related features
		List<String> words = StringUtil.camelDivision(info.getNameLiteral());
		assert words.isEmpty() == false: "AT LEAST CONTAINS ONE WORD";
		
		variableFeatureList.add("" + literal.length());	//the len of vname
		variableFeatureList.add("" + (literal.length() <= 3));	//is short name
		variableFeatureList.add("" + words.size()); //words of the vname
		variableFeatureList.add("" + literal.charAt(0));	//1st letter
		variableFeatureList.add("" + ((literal.length() >=2) ? literal.charAt(1) : NONE));	//2nd letter
		variableFeatureList.add("" + ((literal.length() >=3) ? literal.charAt(2) : NONE));	//3rd letter

		variableFeatureList.add(((words.size() > 1) ? words.get(0).toLowerCase() : NONE)); //fst word, only aaaBbb recorded
		variableFeatureList.add("" + ((words.size() >= 2) ? words.get(1).toLowerCase() : NONE)); //2nd word
		variableFeatureList.add("" + ((words.size() >= 3) ? words.get(2).toLowerCase() : NONE)); //3rd word
		
		//type related features
		variableFeatureList.add("" + varfea.isInt());
		variableFeatureList.add("" + varfea.isFloat());
		variableFeatureList.add("" + varfea.isArray());
		variableFeatureList.add("" + varfea.isCollection());
		variableFeatureList.add("" + (varfea.isArray() && TypeUtil.isPurePrimitiveType(type.replaceAll("\\[\\]", ""))));
		boolean primitiveAndSimple = (literal.length() <= 3) && TypeUtil.isPurePrimitiveType(type.replaceAll("\\[\\]", ""));
		variableFeatureList.add("" + primitiveAndSimple);
		
		//type to words
		List<String> typeWords = StringUtil.camelDivision(type);
		assert typeWords.isEmpty() == false: "AT LEAST CONTAINS ONE WORD";
		int lastIndex = typeWords.size() - 1;
		variableFeatureList.add("" + ((typeWords.size() > 1) ? typeWords.get(lastIndex).toLowerCase() : NONE)); //last 1st word
		
		//lastassign
		variableFeatureList.add(varfea.getLastAssign().getAssignType().toString());
		//ass_op
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.INFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.PREFIX)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.POSTFIX)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_mtd
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.METHOD_INVOCATION)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_name
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.QUALIFIED_NAME)
				|| varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.SIMPLE_NAME)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		//ass_num
		if(varfea.getLastAssign().getAssignType().equals(AssignInfo.AssignType.NUMBER_LITERAL)) {
			variableFeatureList.add(varfea.getLastAssign().getMsg());
		}else {
			variableFeatureList.add(NONE);
		}
		
		variableFeatureList.add("" + varfea.getLastAssignDis());
		variableFeatureList.add("" + (varfea.getLastAssignDis() <= 10));//dis0 [0,10]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 10 && varfea.getLastAssignDis() <= 20));//dis0 [11,20]
		variableFeatureList.add("" + (varfea.getLastAssignDis() > 20));//dis0 (20, +infinite)
		variableFeatureList.add("" + varfea.getPreAssNum());
		variableFeatureList.add("" + varfea.isParam());
		variableFeatureList.add("" + varfea.isField());
		variableFeatureList.add("" + varfea.isFinal());
		variableFeatureList.add("" + varfea.isForStmtIndexer());

		
		String bduse = varfea.getFirstUseInBody();
//		boolean casted = false;
//		String castedTp = NONE;
//		if(bduse.startsWith(VariableInfo.CAST_USE)) {
//			casted = true;
//			if(bduse.contains("#")) {
//				castedTp = TypeUtil.removeGenericType(bduse.split("#")[1]);
//				//change all primitive type to its package type
//				castedTp = TypeUtil.getPackageType(castedTp);
//			}
//			bduse = VariableInfo.CAST_USE;
//		}
		variableFeatureList.add(bduse);
//		variableFeatureList.add("" + casted);
//		variableFeatureList.add(castedTp);
		
		variableFeatureList.add(varfea.getFirstUseOutOfStmt());
		
		variableFeatureList.add("" + varfea.getIfCondNumber());
		variableFeatureList.add("" + fileCondNum);
		variableFeatureList.add("" + totCondNum);
		
		variableFeatureList.add(previousCond);

		/*
		//var pred times
		List<String> predSortedByVarTime = getPredListSortedByVarTime(literal);
		variableFeatureList.add(((predSortedByVarTime.size() >= 1) ? predSortedByVarTime.get(0) : NONE));
		variableFeatureList.add(((predSortedByVarTime.size() >= 2) ? predSortedByVarTime.get(1) : NONE));
//		variableFeatureList.add(((predSortedByVarTime.size() >= 3) ? predSortedByVarTime.get(2) : NONE));
		//type pred times
		if(TypeUtil.isPurePrimitiveType(type)) {
			variableFeatureList.add(NONE);
			variableFeatureList.add(NONE);
			variableFeatureList.add(NONE);
		}else {
			List<String> predSortedByTpTime = getPredListSortedByTypeTime(type);
			variableFeatureList.add(((predSortedByTpTime.size() >= 1) ? predSortedByTpTime.get(0) : NONE));
			variableFeatureList.add(((predSortedByTpTime.size() >= 2) ? predSortedByTpTime.get(1) : NONE));
			variableFeatureList.add(((predSortedByTpTime.size() >= 3) ? predSortedByTpTime.get(2) : NONE));
		}*/
		
		
		//features from javadoc
		variableFeatureList.add(varfea.getDocExcpiton());
		variableFeatureList.add(varfea.getDocOpeartor());
		variableFeatureList.add("" + varfea.isDocZero());
		variableFeatureList.add("" + varfea.isDocOne());
		variableFeatureList.add("" + varfea.isDocNullness());
		variableFeatureList.add("" + varfea.isDocRange());
		variableFeatureList.add("" + varfea.isInDocCode());

		//return StringUtil.join(variableFeatureList, del);
		return StringUtil.join(variableFeatureList, del);
	}
	
	private String genContextFeatureStr(CondVector vec){
		ContextFeature context = vec.getContextFeature();
		
		String befSyn = context.getAllBefStr();
		String bodySyn = context.getAllBdStr();
		String afSyn = context.getAllAfStr();
		
		List<String> contextFeatureList = new ArrayList<>();
		
		contextFeatureList.add("" + vec.getId());
		contextFeatureList.add("" + vec.getLine());
		contextFeatureList.add("" + vec.getCol());
		contextFeatureList.add(vec.getFileName());
		contextFeatureList.add(context.getTdName());
		contextFeatureList.add(context.getMtdName());
		contextFeatureList.add("" + context.getMtdModifier());
		contextFeatureList.add("" + context.getMtdLineNum());
		contextFeatureList.add("" + vec.getLocalNums());
		contextFeatureList.add("" + vec.getFldNums());
		contextFeatureList.add("" + vec.getParamNums());
		contextFeatureList.add(vec.getAllLocalVarEncodingStr());
		contextFeatureList.add(vec.getAllLocalVarTypeEncodingStr());
		contextFeatureList.add("" + vec.getIntegerLocalVarNum());
		contextFeatureList.add("" + vec.getFloatLocalVarNum());
		contextFeatureList.add("" + vec.getArrayLocalVarNum());
		contextFeatureList.add(vec.getAllFieldEncodingStr());
		contextFeatureList.add(vec.getAllFieldTypeEncodingStr());
		contextFeatureList.add("" + context.isInLoop());
		contextFeatureList.add("" + context.getBodyCtl());
		contextFeatureList.add(befSyn);
		contextFeatureList.add(bodySyn);
		contextFeatureList.add(afSyn);
		
		contextFeatureList.addAll(context.getBefSynList());
		contextFeatureList.addAll(context.getBdSynList());
		contextFeatureList.addAll(context.getAfSynList());
		
		List<VariableInfo> nearestLocs = getNearestNLocalVars(vec);
		for(int i = 0; i < ContextFeature.NEAREST_LOCAL_NUM; i++){
			if(i < nearestLocs.size()){
				contextFeatureList.add(nearestLocs.get(i).getNameLiteral());
			}else{
				contextFeatureList.add(NONE);
			}
		}
		
		/**previouse stmt and next stmt**/
		contextFeatureList.add(context.getPreviousStmt0());
		contextFeatureList.add(context.getPreviousStmt1());

		contextFeatureList.add(context.getNextStmt0());
		contextFeatureList.add(context.getNextStmt1());

		/** condition is like a > b, pred is like $ > $*/
		contextFeatureList.add(context.getPreviousCond());
		String previousPred = getPreviousPred();
		contextFeatureList.add(previousPred);
		
		return StringUtil.join(contextFeatureList, del);
	}
	
	
	private String getPreviousPred() {
		
		class IfVisitor extends ASTVisitor{
			public Expression preExpr;
			
			@Override
			public boolean visit(MethodDeclaration node) {
				if(inBound(node) == false){
					return false;
				}
				
				return super.visit(node);
			}
			@Override
			public boolean visit(IfStatement ifNode) {
				if(before(ifNode)){
					preExpr = ifNode.getExpression();
				}
				return super.visit(ifNode);
			}
			private boolean before(IfStatement ifNode){
				int exprLine = DeduLineToFeatureVisitor.this.cu.getLineNumber(ifNode.getExpression().getStartPosition());
				if(exprLine < DeduLineToFeatureVisitor.this.line){
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
	
	
	private String getPreviousCondForVar(final VariableInfo varinfo) {
		
		class IfVisitor extends ASTVisitor{
			public Expression preExpr;
			
			@Override
			public boolean visit(MethodDeclaration node) {
				if(inBound(node) == false){
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
						if(name.equals(varinfo.getNameLiteral())){
							this.preExpr = expr;
							break;
						}
					}
				}
				return super.visit(ifNode);
			}
			private boolean before(IfStatement ifNode){
				int exprLine = DeduLineToFeatureVisitor.this.cu.getLineNumber(ifNode.getExpression().getStartPosition());
				if(exprLine < DeduLineToFeatureVisitor.this.line){
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

	private void setComplexUsage(Statement rootStmt, Expression expr, List<VariableInfo> localVarInfoList, VariableInfo thisPointer){
		//body use
		UsageVisitor usageVisitor = new UsageVisitor(false, cu, currentMtdDecl, file, thisPointer, localVarInfoList);
		expr.accept(usageVisitor);
		
		//outer use
		Block fatherBlk = (Block) ASTLocator.getSpecifiedTypeFather(rootStmt, Block.class);
		int nodeEnd = cu.getLineNumber(rootStmt.getStartPosition() + rootStmt.getLength());
		int blkEnd = cu.getLineNumber(fatherBlk.getStartPosition() + fatherBlk.getLength());
		UsageVisitor outUseVisitor = new UsageVisitor(false, cu, currentMtdDecl, file, 
				thisPointer, localVarInfoList, nodeEnd, blkEnd);
		fatherBlk.accept(outUseVisitor);
		outUseVisitor.setNoUse();
	}
	
	private CondVector getCondVector(Statement stmt, Expression expr){
		int ln = cu.getLineNumber(stmt.getStartPosition());
		int col = cu.getColumnNumber(stmt.getStartPosition());
		
		logger.debug(file.getName() + "\tLINE: " + ln + "\tCOL: " + col + "\n" + stmt);

		String clsName = this.getClsName(stmt);
		ClassRepre currentClassRepre = this.getCurrentClassRepre(clsName, stmt);
		
		//get context
		String fullName = cu.getPackage() == null ? clsName : cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		
		int nextN;
		if(expr != null && stmt instanceof IfStatement){
			nextN = ln;
		}else{
			nextN = ln = 1;
		}
		
		ContextFeature context = this.generateContextFeature(fullName, currentMtdDecl, stmt, ln , nextN);
		
		//set nearby statements
		String[] pre2stmt = this.getPreviousNMeaningfulLine(ln, 2);
		context.setPreviousStmt0(pre2stmt[0]);
		context.setPreviousStmt1(pre2stmt[1]);
		
		String[] next2stmt = null;
		if(expr != null && stmt instanceof IfStatement){
			next2stmt = this.getNextNMeaningFulLine(ln, 2); // from the next line of if
		}else{
			next2stmt = this.getNextNMeaningFulLine(ln - 1, 2); //from the current line
		}
		context.setNextStmt0(next2stmt[0]);
		context.setNextStmt1(next2stmt[1]);
		
		Pair<List<VariableInfo>, VariableInfo> pair = getAllVarInfo(stmt, currentClassRepre, null);
		
		List<VariableInfo> localVarInfoList = pair.getFirst();
		VariableInfo thisPointer = pair.getSecond();
		
		setMethodIfCondNum(localVarInfoList, thisPointer);
		setFileIfCondNum(localVarInfoList);
		setAssignment(stmt, localVarInfoList, thisPointer);
		
		if(expr == null){
			setSimpleUsage(stmt, localVarInfoList, thisPointer);
		}else{
			setComplexUsage(stmt, expr, localVarInfoList, thisPointer);
		}
		
		setDocInformation(currentMtdDecl, localVarInfoList);
		
		int id = Integer.valueOf(this.sid);
		CondVector condVecotor = new CondVector(id, file.getName(), ln, col, localVarInfoList, context);
		return condVecotor;
	}
	
	private void buildSimpleStmt(Statement stmt){

		if(inBound(stmt) == false){
			return;
		}
		this.hitNode = stmt;
		
		CondVector condVecotor = getCondVector(stmt, null);
		
		generateRes(condVecotor);
		
	}
	
	private void buildComplexStmt(Statement rootStmt, Expression expr){
		if(inBound(rootStmt) == false || inBound(expr) == false){
			return;
		}
		this.hitNode = expr;
		
		CondVector condVecotor = getCondVector(rootStmt, expr);
		
		generateRes(condVecotor);
		
	}

	
	/********************************************************************/
	@Override
	public boolean visit(MethodDeclaration node) {
		if(inBound(node)){
			currentMtdDecl = node;
//			System.out.println("HTI METHOD: " + node.getName().getIdentifier() + "() @ " + line); 
			return super.visit(node);
		}
		return false;
	}
	

	/******************************************************************************/
	@Override
	public boolean visit(AssertStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}

//	@Override
//	public boolean visit(Block node) {
//		return super.visit(node);
//	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		buildComplexStmt(node, node.getExpression());
		return super.visit(node);
	}


	@Override
	public boolean visit(ExpressionStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}



	@Override
	public boolean visit(ForStatement node) {
		if(node.getExpression() != null){
			buildComplexStmt(node, node.getExpression());
		}
		return super.visit(node);
	}



	@Override
	public boolean visit(EnhancedForStatement node) {
		buildComplexStmt(node, node.getExpression());
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		buildComplexStmt(node, node.getExpression());
		return super.visit(node);
	}



	@Override
	public boolean visit(ReturnStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}


	@Override
	public boolean visit(SuperConstructorInvocation node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}



	@Override
	public boolean visit(SwitchStatement node) {
		buildComplexStmt(node, node.getExpression());

		return super.visit(node);
	}



	@Override
	public boolean visit(SynchronizedStatement node) {
		buildComplexStmt(node, node.getExpression());
		return super.visit(node);
	}

	
	@Override
	public boolean visit(BreakStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}

	
	@Override
	public boolean visit(ContinueStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}
	

	@Override
	public boolean visit(EmptyStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}
	
	
	@Override
	public boolean visit(ThrowStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}



	@Override
	public boolean visit(TryStatement node) {
//		buildSimpleStmt(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		buildSimpleStmt(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		buildComplexStmt(node, node.getExpression());
		return super.visit(node);
	}
	
}
