package edu.pku.sei.conditon.dedu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.auxiliary.ArgumentNormolizeVisitor;
import edu.pku.sei.conditon.auxiliary.CondDivisionVisitor;
import edu.pku.sei.conditon.auxiliary.DefUseCompletVisitor;
import edu.pku.sei.conditon.auxiliary.DollarilizeVisitor;
import edu.pku.sei.conditon.auxiliary.PredicateFeatureVisitor;
import edu.pku.sei.conditon.auxiliary.StatisticVisitor;
import edu.pku.sei.conditon.dedu.extern.pf.ConstTrue;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.ContextFeature;
import edu.pku.sei.conditon.dedu.feature.PositionFeature;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.PredicateFeature;
import edu.pku.sei.conditon.dedu.feature.TreeVector;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurBoolNode;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurGrammar;
import edu.pku.sei.conditon.dedu.grammar.recur.RecurTree;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.PROCESSING_TYPE;
import edu.pku.sei.conditon.dedu.writer.AllPredWriter;
import edu.pku.sei.conditon.dedu.writer.BUWriter;
import edu.pku.sei.conditon.dedu.writer.RecurBUWriter;
import edu.pku.sei.conditon.dedu.writer.RecurWriter;
import edu.pku.sei.conditon.dedu.writer.TDWriter;
import edu.pku.sei.conditon.dedu.writer.Writer;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.OperatorUtil;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.PackageRepre;
import edu.pku.sei.proj.ProInfo;

public final class DeduConditionVisitor extends AbstractDeduVisitor{
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	private static Logger logger = Logger.getLogger(DeduConditionVisitor.class);  
	
	public static int CONDITION_ID = 0;
	public static int TREE_ID = 0;

	private static final int CAPACITY = 8192;
	
	public static List<CondVector> plainCondVec = new ArrayList<>(CAPACITY);
	
	public static List<TreeVector> recurTreeList = new ArrayList<>(CAPACITY);
	
	private static Set<CondVector> duplicatedCondVec = new HashSet<>();
		
	public static final String UNKNOWN_TYPE = "UNKOWN";
			
	private static final RecurGrammar recurGrammar = new RecurGrammar();
	
	private static final PROCESSING_TYPE missionType = ConditionConfig.getInstance().getProcessType();
	
	public DeduConditionVisitor(CompilationUnit cu, File f, char[] rawSource, ProInfo proInfo){
		this.cu = cu;
		this.file = f;
		this.rawSource = rawSource;
		this.proInfo = proInfo;
		
		this.sourceLines = JavaFile.readFileToStringList(f);
		
		FileCondVisitor visitor = new FileCondVisitor();
		cu.accept(visitor);
	}
	
	public static void reset(){
		plainCondVec.clear();
		recurTreeList.clear();
		duplicatedCondVec.clear();
		
		CONDITION_ID = 0;
		TREE_ID = 0;
	}
	
	/**
	 * Write the data to files
	 * @param outFilePrefix
	 */
	protected static void writeVectors(String outFilePrefix){
		Writer allPredWriter = new AllPredWriter(outFilePrefix, plainCondVec);
		allPredWriter.write();
		List<Writer> list = new ArrayList<>();
		list.add(new BUWriter(outFilePrefix, plainCondVec, duplicatedCondVec));
		
		if(ConditionConfig.getInstance().isPredAll()) {
			list.add(new TDWriter(outFilePrefix, plainCondVec));
			list.add(new RecurWriter(outFilePrefix, recurTreeList));
			list.add(new RecurBUWriter(outFilePrefix, recurTreeList));
		}
		
		for(Writer writer: list) {
			writer.write();
		}
	}
	

	public static boolean usedAsParam(String condition, String varName) {
		
		Expression expr = (Expression) JavaFile.genASTFromSource(condition, ASTParser.K_EXPRESSION, DeduMain.JAVA_VERSION);
		
		class MtdVisitor extends ASTVisitor{
			String mtdName = null;

			@Override
			public boolean visit(MethodInvocation node) {
				//TODO: if there are two mtdinvc, it can not handle
				mtdName = node.getName().getIdentifier();
				return super.visit(node);
			}
		};
		MtdVisitor visitor = new MtdVisitor();
		expr.accept(visitor);
		
		if(StatisticVisitor.mtdNameToUsedParamVars.containsKey(visitor.mtdName)) {//the key can be null
			Set<String> args = StatisticVisitor.mtdNameToUsedParamVars.get(visitor.mtdName);
			if(args.contains(varName)){
				return true;
			}
		}
		
		return false;
	}
	
	public static int getVarOccurredTimeAtTheExprPosion(String varname, String pred, int pos, Map<String, OriPredItem> allOriPredicates) {
		pred = pred.replaceAll("\\s", "");
		if(!allOriPredicates.containsKey(pred)) {
			return 0;
		}
		OriPredItem item = allOriPredicates.get(pred);
		
		Map<String, Integer> timeMap = item.getPosToOccurredVarTimesMap().get(pos);
		
//		assert timeMap != null;
		if(timeMap == null) {
			return 0;
		}
		if(varname.equals(ConstTrue.CONSTANT_TRUE)) {
			return 10;
		}
		
		return timeMap.containsKey(varname) ? timeMap.get(varname) : 0;
	}

	
	private CondVector getCondVector(int id, Statement node, Expression expr, int ln, int col, ContextFeature context,
			ClassRepre clsRepre) {
		CondVector condVector = foreachCondExpr(id, node, expr, ln, col, context, clsRepre);

		String clsName = this.getClsName(node);
		PredicateFeatureVisitor predicateFeatureVisitor = new PredicateFeatureVisitor(clsName, expr);
		expr.accept(predicateFeatureVisitor);

		PredicateFeature predicateFeature = predicateFeatureVisitor.getCollectedFeature();

		PositionFeatureVisitor positionFeatureVisitor = new PositionFeatureVisitor(predicateFeature,
				condVector.getLocals());
		expr.accept(positionFeatureVisitor);

		// assert predVisitor.positionId.size() == predVisitor.positionList.size();

		String predStr = normolizeExpr(expr, positionFeatureVisitor, condVector.getLocals());

		sortPositions(positionFeatureVisitor);

		List<String> positionTypeList = new ArrayList<>();
		List<String> positionVarList = new ArrayList<>();
		setVarNameAndTypeForEachPosition(positionFeatureVisitor, positionTypeList, positionVarList);

		Predicate pred = new Predicate(predStr, expr.toString().replaceAll("\n", " "), Predicate.getPredType(expr),
				positionFeatureVisitor.positionList.size(), positionTypeList, positionVarList, predicateFeature);

		condVector.setPredicate(pred);

		System.out.println(pred);

		return condVector;
	}
	
	private Expression getCondExpr(ASTNode node){
		Expression conditionExpr = null;

		if (node instanceof IfStatement) {
			
			conditionExpr = ((IfStatement) node).getExpression();

			if(conditionExpr instanceof BooleanLiteral){
				return null;
			}
			
		} else if (node instanceof ForStatement) {

			conditionExpr = ((ForStatement) node).getExpression();
			if (conditionExpr == null) {// stm like "for( ; ; )"
				return null;
			}
			if(conditionExpr instanceof BooleanLiteral){
				return null;
			}

		} else if (node instanceof WhileStatement) {

			conditionExpr = ((WhileStatement) node).getExpression();
			if(conditionExpr instanceof BooleanLiteral){
				return null;
			}

		} else if (node instanceof DoStatement) {

			conditionExpr = ((DoStatement) node).getExpression();
			if(conditionExpr instanceof BooleanLiteral){
				return null;
			}

		} else if(node instanceof ConditionalExpression){
			
			conditionExpr = ((ConditionalExpression) node).getExpression();
			
			if(conditionExpr instanceof ParenthesizedExpression){
				conditionExpr = ((ParenthesizedExpression) conditionExpr).getExpression();
			}
			
			if(conditionExpr instanceof BooleanLiteral){
				return null;
			}
			
		} else if(node instanceof Expression){
			
			conditionExpr = (Expression) node;
			
		} else if(node instanceof AssertStatement) {
			
			conditionExpr = ((AssertStatement) node).getExpression();
			
		} else {
			throw new Error("ERR TYPE @collectCondition");
		}
		return conditionExpr;
	}

	private CondVector foreachCondExpr(int id, Statement stmt, Expression expr, 
			int ln, int col, ContextFeature context, ClassRepre classRepre){

		Pair<List<VariableInfo>, VariableInfo> pair = getAllVarInfo(stmt, classRepre, expr);
		List<VariableInfo> localVarInfoList = pair.getFirst();
		VariableInfo thisPointer = pair.getSecond();
		
		CondVector condVecotor = new CondVector(id, file.getName(), ln, col, localVarInfoList, context);

		//2. set variable features
		setMethodIfCondNum(localVarInfoList, thisPointer);
		setFileIfCondNum(localVarInfoList);
		setAssignment(stmt, localVarInfoList, thisPointer);
		setSimpleUsage(stmt, localVarInfoList, thisPointer);
		setPreviousCond(localVarInfoList, ln);

		setDocInformation(currentMtdDecl, localVarInfoList);
		
		return condVecotor;
	}
	
	private String reDollarilize(String condition) {
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(condition, ASTParser.K_EXPRESSION);	
		String result = DollarilizeVisitor.naiveDollarilize(expr);
		result = result.replaceAll("\n", " ");
		return result;
	}	
	
	private void collectConstForRepair(Statement node) {
		
		if(hasAnonyClsDecl(node)) {
			return;
		}
		
		int ln = cu.getLineNumber(node.getStartPosition());
		int col = cu.getColumnNumber(node.getStartPosition());
		
		logger.debug(file.getName() + "\tLINE: " + ln + "\tCOL: " + col + "\nCONSTANT\n" + node.toString());
		
		String clsName = this.getClsName(node);
		//ClassRepre currentClassRepre = this.getCurrentClassRepre(clsName, node);
		//get context
		String fullName = cu.getPackage() == null ? clsName : cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		ContextFeature context = this.generateContextFeature(fullName, currentMtdDecl, node, ln, ln);
		
		VariableInfo trueConstInfo = ConstTrue.getTheTrueConstVariableInfo();
		
		List<VariableInfo> localVarInfoList = new ArrayList<>(1);
		localVarInfoList.add(trueConstInfo);
		CondVector condVector = new CondVector(CONDITION_ID++, file.getName(), ln, col, localVarInfoList, context);
				
		Predicate pred = ConstTrue.getTheTrueConstPredicate(clsName);
		
		PositionFeature positionFeature = new PositionFeature(pred.getPredicateFeature(), 0, "OTHER", "NIL");
		trueConstInfo.getPositionFeatures().add(positionFeature);
		
		condVector.setPredicate(pred);

		plainCondVec.add(condVector);
	}
	
	private void collectConditionForRepair(ASTNode node) {
		
		if(hasAnonyClsDecl(node)) {
			return;
		}
		
		Expression conditionExpr = getCondExpr(node);
		if(conditionExpr == null){
			return;
		}
		String condStr = conditionExpr.toString();
		if(condStr.contains(" | ") || condStr.contains(" & ") || condStr.contains(">>") || condStr.contains("<<")) {
			return;
		}

		int ln = cu.getLineNumber(node.getStartPosition());
		int col = cu.getColumnNumber(node.getStartPosition());
		
		logger.debug(file.getName() + "\tLINE: " + ln + "\tCOL: " + col + "\n" + conditionExpr);

		String clsName = this.getClsName(node);
		ClassRepre currentClassRepre = this.getCurrentClassRepre(clsName, node);
		
		//assert file.getName().startsWith(clsName);
		
		//get context
		String fullName = cu.getPackage() == null ? clsName : cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		ContextFeature context = this.generateContextFeature(fullName, currentMtdDecl, node, ln, ln);
		
		//get variables
		Statement stmt = null;
		if(node instanceof Statement) {
			//if, for, while, do-while
			stmt = (Statement) node;
		}else {
			//conditional expr or assertTrue/assertFalse
			stmt = (Statement) ASTLocator.getSpecifiedTypeFather(node, Statement.class);
		}
		
		/** divide condition*/
		CondDivisionVisitor divideVisitor = null;
		
		if(conditionExpr.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY){
			/**to deal with assertTure*/
			divideVisitor = new CondDivisionVisitor(conditionExpr, conditionExpr);
		}else{
			divideVisitor = new CondDivisionVisitor();
		}
		
		conditionExpr.accept(divideVisitor);
				
		if(divideVisitor.getDividedExprList().isEmpty()) {
			return;
		}
		
		Set<Expression> dividedExprs = new HashSet<>();

				
		ArgumentNormolizeVisitor arguNormVisitor = null;
		boolean needNormalize = ArgumentNormolizeVisitor.isComplexExprArgu(conditionExpr);
		if(needNormalize) {
			dividedExprs.add(conditionExpr);
		}else if(missionType == PROCESSING_TYPE.D4J) {
			dividedExprs.add(conditionExpr);
		}else if(ArgumentNormolizeVisitor.isSimpleDisjunctionOrConjuntion(conditionExpr)) {
			dividedExprs.add(conditionExpr);
		}
		
		
		dividedExprs.addAll(divideVisitor.getDividedExprList());
		
		if(node instanceof IfStatement || node instanceof WhileStatement || node instanceof DoStatement) {
			DefUseCompletVisitor duVisitor = new DefUseCompletVisitor((Statement) node);
			List<Expression> udExpantedList = duVisitor.extandByDefUse(conditionExpr);
			if(!udExpantedList.isEmpty()) {
				dividedExprs.clear();
				dividedExprs.addAll(udExpantedList);
			}
		}
		
		/** get predict*/ 
		for(Expression expr : dividedExprs){
			
			expr = ASTLocator.removeBraces(expr);
			
			if(missionType != PROCESSING_TYPE.D4J
					&& expr == conditionExpr
					&& needNormalize) {
				arguNormVisitor = new ArgumentNormolizeVisitor();
				expr.accept(arguNormVisitor);
			}
			
			
			//assert expr.toString().contains("&&") == false && expr.toString().contains("||") == false;
			
			CondVector condVector = getCondVector(CONDITION_ID++, stmt, expr, ln, col, context, currentClassRepre); 
			
			if(condVector.getPredicate().getLiteral().trim().length() != 0){//TODO: handle which cases?
				plainCondVec.add(condVector);
			}
						
		}
	}
	
	private void setVarNameAndTypeForEachPosition(PositionFeatureVisitor positionFeatureVisitor, 
			List<String> positionTypeList, List<String> positionVarList) {
		for(Position pos : positionFeatureVisitor.positionList){
			//if(pos.getRepalcedStr() == null){
				assert pos.getVarType() != null;
				String posTp = pos.getVarType();
				positionTypeList.add(posTp);
				positionVarList.add(pos.node.toString());
			//}
		}
		assert positionTypeList.size() == positionFeatureVisitor.positionList.size();
	}
	
	private void sortPositions(PositionFeatureVisitor positionFeatureVisitor) {
		Collections.sort(positionFeatureVisitor.positionList, new Comparator<Position>(){
			@Override
			public int compare(Position o1, Position o2) {
				return o1.startPosition - o2.startPosition;
			}
			
		});
	}
	
	private String normolizeExpr(Expression expr, PositionFeatureVisitor predVisitor, List<VariableInfo> allLocals) {
		// all var are replaced by dollar
		String predictStr = DollarilizeVisitor.dollarilize(expr);
		if(expr instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression) expr;
			Expression leftOper = infix.getLeftOperand();
			Expression rightOper = infix.getRightOperand();
			String oper = infix.getOperator().toString();

			if(predVisitor.positionList.size() == 1){
				if (leftOper instanceof SimpleName) {
					/* replace contants to its value */
					if (rightOper instanceof SimpleName) {
						String right = rightOper.toString();
						if (ASTLocator.maybeConstant(right)) {
							predictStr = replaceConstants(file.getAbsolutePath(), predictStr, right);
						}
					}

					/* replace float to a unified format */
					String newStr = StringUtil.formatNumeralVal(oper, predictStr);
					if (!newStr.equals(predictStr)) {
						// System.err.println(predictStr + " => " + newStr);
						predictStr = newStr;
					}

				} else if (leftOper instanceof QualifiedName) {
					String getMtd = fieldAccessToGetMethod((QualifiedName) leftOper, allLocals);
					if (getMtd != null && getMtd.length() > 0) {
						predictStr = getMtd + " " + infix.getOperator() + " " + rightOper;
						//System.out.println(">>>>>>>>>\t" + predictStr);
					}
				}
				
			}else if(OperatorUtil.isComparing(oper)){
				if(rightOper instanceof NumberLiteral) {
					String right = rightOper.toString().trim();
					String newRight = TypeUtil.modifyUselessFormat(right);
					if(newRight.equals(right) == false){
						predictStr = leftOper.toString().trim() + " " + oper + " " + newRight;
						predictStr = reDollarilize(predictStr);
					}
				}
			}
		}
		return predictStr;
	}
	
	
	
	private static String replaceConstants(String fileName, String predictStr, String right) {
		if(StatisticVisitor.constandsMap != null && StatisticVisitor.constandsMap.containsKey(fileName)){
			Map<String, String> contantsMap = StatisticVisitor.constandsMap.get(fileName);
			
			for(Entry<String, String> entry : contantsMap.entrySet()){
				String contantName = entry.getKey();
				if(contantName.equals(right)){
					String val = entry.getValue();
					return predictStr.replace(right, val);
				}
				
			}
			
		}
		
		return predictStr;
	}

	private String fieldAccessToGetMethod(QualifiedName leftOper, List<VariableInfo> locals){
		Name qualifier = leftOper.getQualifier();
		String gettingFld = leftOper.getName().getIdentifier();
		if(qualifier instanceof SimpleName == false || ASTLocator.maybeConstant(gettingFld) || Character.isUpperCase(gettingFld.charAt(0))){
			return null;
		}
		VariableInfo hitedInfo = null;
		for(VariableInfo info: locals){
			if(info.getNameLiteral().equals(qualifier.toString())){
				hitedInfo = info;
				break;
			}
			
		}
		if(hitedInfo == null){
			return null;
		}
//		String tp = TypeUtil.removeGenericType(hitedInfo.getType());
		IVariableBinding binding = (IVariableBinding) hitedInfo.getDef().resolveBinding();
		if(binding == null){
			return null;
		}
		ITypeBinding typeBinding = binding.getType();
		if(typeBinding == null){
			return null;
		}
		String fullType = TypeUtil.removeGenericType(typeBinding.getQualifiedName());
		int lastIdx = fullType.lastIndexOf(".");
		if(lastIdx < 0){
			return null;
		}
		String pkgName = fullType.substring(0, lastIdx);
		PackageRepre pkg = this.proInfo.getProjectRepre().getPackage(pkgName);
		if(pkg == null){
			return null;
		}
		String clsName = fullType.substring(lastIdx + 1);
		ClassRepre clazz = pkg.getClassRepre(clsName);
		if(clazz == null){
			return null;
		}
		String getMtd = "get" + gettingFld.substring(0, 1).toUpperCase() + gettingFld.substring(1);
		if(clazz.getMethodRepreByName(getMtd).size() != 1){
			return null;
		}
		return "$." + getMtd + "()";
	}
	
	
	private void collectCondtionForPredAll(IfStatement node) {
		
		Expression conditionExpr = node.getExpression();
		
		if(conditionExpr instanceof ConditionalExpression) {
			return;
		}
		
		String condStr = conditionExpr.toString();
		if(condStr.contains(" | ") || condStr.contains(" & ") || condStr.contains(">>") || condStr.contains("<<")) {
			return;
		}
				
		class IllegalInvoker extends ASTVisitor{
			boolean illegal = false;
			@Override
			public boolean visit(MethodInvocation node) {
				if(node.getExpression() instanceof StringLiteral) {
					illegal = true;
				}
				return super.visit(node);
			}
		}
		IllegalInvoker visitor = new IllegalInvoker();
		conditionExpr.accept(visitor);
		if(visitor.illegal) {
			return;
		}

		int ln = cu.getLineNumber(node.getStartPosition());
		int col = cu.getColumnNumber(node.getStartPosition());
		
		logger.debug(file.getName() + "\tLINE: " + ln + "\tCOL: " + col + "\n" + conditionExpr);

		String clsName = this.getClsName(node);
		ClassRepre currentClassRepre = this.getCurrentClassRepre(clsName, node);
		
		//get context
		String fullName = cu.getPackage() == null ? clsName : cu.getPackage().getName().getFullyQualifiedName() + "." + clsName;
		ContextFeature context = this.generateContextFeature(fullName, currentMtdDecl, node, ln, ln);
		
		addToPlainGrammar(node, conditionExpr, ln, col, context, currentClassRepre);
		
		addToRecurGrammar(node, conditionExpr, ln , col, context, currentClassRepre);
		
		TREE_ID++;
	}
	
	private void addToPlainGrammar(IfStatement node, Expression conditionExpr, int ln, 
			int col, ContextFeature context, ClassRepre currentClassRepre) {
		
		if(conditionExpr.toString().contains("this ") || conditionExpr.toString().contains("this.")) {
			return;
		}
		
		/** divide condition*/
		CondDivisionVisitor divideVisitor = new CondDivisionVisitor();
				
		conditionExpr.accept(divideVisitor);
		
		if(divideVisitor.getDividedExprList().isEmpty()) {
			return;
		}
		
		Set<Expression> dividedExprs = new HashSet<>();	
		
		dividedExprs.add(conditionExpr);
		
		dividedExprs.addAll(divideVisitor.getDividedExprList());
		if(dividedExprs.isEmpty()) {
			return;
		}
		
		/** get predict*/ 
		for(Expression expr : dividedExprs){
			
			expr = ASTLocator.removeBraces(expr);
			
			//assert expr.toString().contains("&&") == false && expr.toString().contains("||") == false;
			
			CondVector condVector = getCondVector(TREE_ID, node, expr, ln, col, context, currentClassRepre); 
			
			if(condVector.getPredicate().getLiteral().trim().length() != 0){//TODO: handle which cases?
				plainCondVec.add(condVector);
				
				if(expr != conditionExpr) {
					duplicatedCondVec.add(condVector);
				}
			}
			
		}
	}
	
	private void addToRecurGrammar(Statement node, Expression conditionExpr, int ln, int col, ContextFeature context, ClassRepre clsRepre) {
		
		if(conditionExpr.toString().contains("this ") || conditionExpr.toString().contains("this.")) {
			return;
		}
		
		RecurTree tree = recurGrammar.generateTree(conditionExpr);
		
		// Note that the allLocals has no position features
		List<VariableInfo> allLocals = getAllVarInfo(node, clsRepre, conditionExpr).getFirst();
		
		TreeVector treeVec = new TreeVector(TREE_ID, ln, col, context, conditionExpr, tree, allLocals);
		
		List<RecurBoolNode> bfsList = tree.broadFristSearchTraverse();
		
		for(RecurBoolNode recurBoolNode: bfsList) {
			if(recurBoolNode.getOpcode() != RecurBoolNode.Opcode.NONE) {
				continue;
			}
			Expression leafNodeExpr = recurBoolNode.getExpr();
			leafNodeExpr = ASTLocator.removeBraces(leafNodeExpr);
			
			CondVector condVector = getCondVector(treeVec.getId(), node, leafNodeExpr, ln, col, context, clsRepre); 

			recurBoolNode.setCondVector(condVector);
		}
		recurTreeList.add(treeVec);
	}
	
	/******************************************************************/
	
	@Override
	public boolean visit(AssertStatement node) {
		
		if(missionType == PROCESSING_TYPE.D4J && !CONFIG.isPredAll()) {
			collectConditionForRepair(node);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(IfStatement node) {
		if(CONFIG.isPredAll()) {
			collectCondtionForPredAll(node);
		}else {
			collectConditionForRepair(node);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		
		if(missionType == PROCESSING_TYPE.D4J && !CONFIG.isPredAll()) {
			collectConditionForRepair(node);
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		
		if(missionType == PROCESSING_TYPE.D4J && !CONFIG.isPredAll()) {
			collectConditionForRepair(node);
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		
		if(missionType == PROCESSING_TYPE.D4J && !CONFIG.isPredAll()) {
			collectConditionForRepair(node);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ConditionalExpression node) {
		if(!CONFIG.isPredAll()) {
			MethodDeclaration mtd = (MethodDeclaration) ASTLocator.getSpecifiedTypeFather(node, MethodDeclaration.class);
			if(mtd == null){
				return false;
			}
			collectConditionForRepair(node);
		}
		
		return super.visit(node);
	}
	
	
	@Override
	public boolean visit(ReturnStatement node) {
		if(!CONFIG.isPredAll()) {
			if(ConstTrue.isTargetRetThrow(node)) {
				collectConstForRepair(node);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		if(!CONFIG.isPredAll()) {
			if (ConstTrue.isTargetRetThrow(node)) {
				collectConstForRepair(node);
			}
		}
		return super.visit(node);
	}


	/*******************************************************************************/
	public class Position implements Comparable<Position>{
		public String action; //"ReplaceAll", "InsertThis", "ReplaceSuper"
		public Expression node;
		public int startPosition = -1;
		public int len = -1;
		
		//record the variable type of the position
		private String varType = UNKNOWN_TYPE;
		
		//only for initiall value
		private String repalcedStr; 
		
		public Position(String action, Expression node, int startPosition, int len) {
			super();
			
			assert action.equals("ReplaceAll") || action.equals("InsertThis") || action.equals("ReplaceSuper");
			
			assert node instanceof SimpleName || node instanceof ThisExpression || node instanceof SuperFieldAccess;
			
			this.action = action;
			this.node = node;
			this.startPosition = startPosition;
			this.len = len;
		}

		public String getRepalcedStr() {
			return repalcedStr;
		}

		public void setRepalcedStr(String repalcedStr) {
			this.repalcedStr = repalcedStr;
		}
		

		public String getVarType() {
			return varType;
		}

		public void setVarType(String varType) {
			this.varType = varType;
		}

		public Expression getNode() {
			return node;
		}
		
		@Override
		public int compareTo(Position that) {
			Integer thisPos = new Integer(startPosition);
			Integer thatPos = new Integer(that.startPosition);
			return thisPos.compareTo(thatPos);
		}

		@Override
		public String toString() {
			return  node.toString() + "-" + varType + " " + action + " STAR: " + startPosition + " LEN: " + len + ((this.repalcedStr == null) ? "" : this.repalcedStr);
		}
		
	}
	
	
	/*******************************************************************************/
	
	private class PositionFeatureVisitor extends ASTVisitor{
		
		private PredicateFeature predicateFeature;
		
		private List<VariableInfo> locals;
		
		public List<Position> positionList = new ArrayList<>();
		
		public List<Integer> positionId = new ArrayList<>();
		
		//used for partial program feature
		private List<String> positionStr = new ArrayList<>();
		
		//in case of a local appears more than one time, like a + b > a
		private Map<String, Integer> nodeLiteralMap = new HashMap<>();
		
		public PositionFeatureVisitor(PredicateFeature predicateFeature, List<VariableInfo> locals) {
			this.predicateFeature = predicateFeature;
			this.locals = locals;
		}

		private void setPositionID(String literal){
			if(nodeLiteralMap.containsKey(literal)){
				int id = nodeLiteralMap.get(literal);
				positionId.add(id);
			}else{
				nodeLiteralMap.put(literal, positionId.size());
				positionId.add(positionId.size());
			}
		}
		
		private VariableInfo getBindedVarInfo(SimpleName node){
			
			for(VariableInfo info: locals){
				if(info.getNameLiteral().equals(node.getIdentifier())){
//					IVariableBinding binding1 = (IVariableBinding) node.resolveBinding();
//					if(binding0 != null && binding1 != null){
//						assert binding0.isEqualTo(binding1);
//					}else if(binding0 == null || (binding0 != null && binding0.isField()) ){
//						if(info.isSuperField() && node.getIdentifier().equals(info.getNameLiteral())){
//							return info;
//						}
//					}
					return info;
				}
				
			}
			return null;
		}
		
		@Override
		public boolean visit(SimpleName node) {
			if(ASTLocator.notVarLocation(node)){
				return super.visit(node);
			}
			IBinding binding = node.resolveBinding();
			if(binding != null && binding instanceof IPackageBinding) {
				return super.visit(node);
			}
			
			//maybe access of class
			if(Character.isUpperCase(node.getIdentifier().charAt(0))){
				return false;
			}
			Position position = new Position("ReplaceAll", node, node.getStartPosition(), node.getLength());
			positionList.add(position);
			
			
			VariableInfo info = getBindedVarInfo(node);
			if(info == null){
				IVariableBinding binding0 = (IVariableBinding) binding;
				if(binding0 != null) {
					String tp = binding0.getType().toString();
					if(tp.startsWith("public final class ") || tp.startsWith("public class ")) {
						if(tp.contains("\n")) {
							tp = tp.split("\n")[0];
						}
						int spaceIdx = tp.lastIndexOf(" ");
						int dotIdx = tp.lastIndexOf(".");
						if(dotIdx > spaceIdx) {
							tp = tp.substring(dotIdx + 1).trim();
						}else{
							tp = tp.substring(spaceIdx + 1).trim();
						}
						if(tp.contains("$")) {
							tp = tp.replaceAll("\\$", ".");
						}
					}
					position.setVarType(tp);
				}
				
				//TODO:: is not coincide with new branched work, 2017-11-11
				//if info is null, it must be a constant 
				if(currentMtdInfo.getConstantFieldMap().containsKey(node.getIdentifier())){
					String initVal = currentMtdInfo.getConstantFieldMap().get(node.getIdentifier()).getInitStr();
					if(initVal != null) {
						position.setRepalcedStr(initVal);
					}
				}else{
					position.setVarType(UNKNOWN_TYPE);
				}
			}else{
				
				position.setVarType(info.getType());
				
				setPositionID(node.toString());
				//it is local variable
				
				positionStr.add(node.getIdentifier());
				
				
				String locationInFather = getLocationInFather(node);
				
				int last = positionId.get(positionId.size() - 1);
				String leftNode = (last == 0) ? "NIL": positionStr.get(positionStr.size() - 1);
				
//				String fatherTp = (node.getParent() instanceof Expression) ? getPredType((Expression) node.getParent()) : "NIL";
//				PartialProgramFeature ppf = new PartialProgramFeature("", rootTp, fatherTp, locationInFather, leftNode, last);

				PositionFeature positionFeature = new PositionFeature(this.predicateFeature, last, locationInFather, leftNode);
				info.getPositionFeatures().add(positionFeature);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(ThisExpression node) {
			Position position = new Position("ReplaceAll", node, node.getStartPosition(), node.getLength());
			
			position.setVarType(this.predicateFeature.getClassName());
			
			positionList.add(position);
			
			setPositionID("THIS");
			
			return super.visit(node);
		}
		
		
		private String getLocationInFather(SimpleName node){
			if(node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY){
				return "PARAM";
			}else if(node.getLocationInParent() == MethodInvocation.NAME_PROPERTY){
				return "CALLER";
			}else if(node.getLocationInParent() == FieldAccess.EXPRESSION_PROPERTY){
				return "FLDAC";
			}else if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY){
				return "INF-L";
			}else if(node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY){
				return "INF-R";
			}else{
				return "OTHER";
			}
		}
	}
	
	
	private boolean hasAnonyClsDecl(ASTNode node) {
		InnerClsVisitor innerClsVisitor = new InnerClsVisitor();
		node.accept(innerClsVisitor);
		return innerClsVisitor.hasAnonymousClsDecl;
	}
	
	private class InnerClsVisitor extends ASTVisitor{
		boolean hasAnonymousClsDecl = false;
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			hasAnonymousClsDecl = true;
			return super.visit(node);
		}
		
	}
}



