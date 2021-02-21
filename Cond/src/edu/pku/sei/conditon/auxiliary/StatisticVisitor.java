package edu.pku.sei.conditon.auxiliary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.pred.evl.DynEvaluator;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.FileUtil;
import edu.pku.sei.conditon.util.SubjectsUtil;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ClassRepre;
import edu.pku.sei.proj.ProInfo;

/**
 * Statistic times in conditions
 * Collect Constants for the src file
 * @author nightwish
 *
 */
public class StatisticVisitor  extends AbstractDeduVisitor{

	private String fileAbsPath;
	
	public static int IF_STMT_NUM;
//	public static int IF_RET_NUM;
//	public static int IF_THROW_NUM;

	public static int COND_EXPR_NUM;

	public static int WHILE_STMT_NUM;
	public static int WHILE_STMT_BRK_NUM;
	public static int WHILE_STMT_CON_NUM;
	
	public static int DO_STMT_NUM;
	public static int DO_STMT_BRK_NUM;
	public static int DO_STMT_CON_NUM;

	
	public static int FOR_STMT_NUM;
	public static int FOR_STMT_BRK_NUM;
	public static int FOR_STMT_CON_NUM;
	
	public static Map<String, Integer> totalIfCondTimeMap = new HashMap<>(1024);
	
	// varname -> Map<predicate, times>
	public static Map<String, Map<String, Integer>> varUsedByPredateMap = new HashMap<>(1024);
	// type -> Map<predicate, times>
	public static Map<String, Map<String, Integer>> typeUsedByPredateMap = new HashMap<>(1024);

	public static Map<String, Set<String>> mtdNameToUsedParamVars = new HashMap<>();
	
	public static Map<String, Map<String, String>> constandsMap = new HashMap<>();
	
	private List<String> constantsCodes = new ArrayList<>();
	private Set<String> bannedConstants = new HashSet<>();
	
	private static void incTotalIfCondTime(String name){
		if(totalIfCondTimeMap.containsKey(name)){
			int oldVal = totalIfCondTimeMap.get(name);
			totalIfCondTimeMap.put(name, oldVal + 1);
		}else{
			totalIfCondTimeMap.put(name, 1);
		}
	}
	
	private static void putinConstandesMap(String fileName, String fld, String contantVal){
		if(!constandsMap.containsKey(fileName)){
			Map<String, String> fldToValue = new HashMap<>();
			constandsMap.put(fileName, fldToValue);
		}
		Map<String, String> fldToValue = constandsMap.get(fileName);		
		fldToValue.put(fld, contantVal);
	}
	
	private static void putinPredMap(Map<String, Map<String, Integer>> map, String var, String pred) {
		Map<String, Integer> predTimeMap = null;
		if(map.containsKey(var)) {
			predTimeMap = map.get(var);
		}else {
			predTimeMap = new HashMap<>();
			map.put(var, predTimeMap);
		}
		int time = 0;
		
		pred = pred.replaceAll("\t", "");
		pred = pred.replaceAll("\n", "");
		
		if(predTimeMap.containsKey(pred)) {
			time = predTimeMap.get(pred);
		}
		time++;
		predTimeMap.put(pred, time);
	}
	
	private static void sortPredicateMap(Map<String, Map<String, Integer>> fullMap) {
		
		assert fullMap == varUsedByPredateMap || fullMap == typeUsedByPredateMap;
		
		//collate the predate map by times
		for(String key : fullMap.keySet()) {
			Map<String, Integer> map = fullMap.get(key);
			Map<String, Integer> tmpMap = CollectionUtil.sortByValue(map, true);
			fullMap.put(key, tmpMap);
		}
	}
	
	public static void dump(String path){
		
		System.out.println(">>>> STASTIC DUMP >>>>");
		System.out.println("IF COUNT: " + IF_STMT_NUM);
		System.out.println("COND EXPR NUM: " + COND_EXPR_NUM);
		
		System.out.println("TYPE\tBRK\tCTN\tALL");
		System.out.println("WHILE\t"+ WHILE_STMT_BRK_NUM + "\t" + WHILE_STMT_CON_NUM + "\t" + WHILE_STMT_NUM);
		System.out.println("DO\t"+ DO_STMT_BRK_NUM + "\t" + DO_STMT_CON_NUM + "\t" + DO_STMT_NUM);
		System.out.println("FOR\t"+ FOR_STMT_BRK_NUM + "\t" + FOR_STMT_CON_NUM + "\t" + FOR_STMT_NUM);

		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>\n");

		assert totalIfCondTimeMap != null && totalIfCondTimeMap.size() > 0: "TOTAL_IF_COND_TIME_MAP IS EMPTY!";
		FileUtil.writeObjectToFile(path + ".tot.map", totalIfCondTimeMap);

		sortPredicateMap(varUsedByPredateMap);
		assert varUsedByPredateMap != null && varUsedByPredateMap.size() > 0: "MAP IS EMPTY!";
		FileUtil.writeObjectToFile(path + ".varpred.map", varUsedByPredateMap);
		
		sortPredicateMap(typeUsedByPredateMap);
		assert typeUsedByPredateMap != null && typeUsedByPredateMap.size() > 0: "MAP IS EMPTY!";
		FileUtil.writeObjectToFile(path + ".typepred.map", typeUsedByPredateMap);
		
		assert mtdNameToUsedParamVars != null && mtdNameToUsedParamVars.size() > 0;
		FileUtil.writeObjectToFile(path + ".mtdpara.map", mtdNameToUsedParamVars);
	}
	
	public static void generateTotalIfCodTimeMap(String bugName, String subjectSrcPath, String subjectTestPath){
		
		ArrayList<File> srcFileList = new ArrayList<File>(128);
		SubjectsUtil.getFileList(subjectSrcPath, srcFileList);
		
		if(subjectTestPath != null && subjectTestPath.length() > 0){
			ArrayList<File> testFileList = new ArrayList<File>(128);
			SubjectsUtil.getFileList(subjectTestPath, testFileList);
			srcFileList.addAll(testFileList);
		}
		DeduMain.resetVisitors();
		DeduMain.forEachJavaFile(srcFileList, StatisticVisitor.class, DeduMain.OUTPUT_ROOT + bugName);

	}
	
	public static void loadTotalIfCondTimeMap(String baseProjAndBug, String subjectSrcPath, String subjectTestPath){
				
		String path = DeduMain.OUTPUT_ROOT + baseProjAndBug.toLowerCase();
		
//		if(!file1.exists()){
//			//Only Test need this branch
//			generateTotalIfCodTimeMap(baseProjAndBug, subjectSrcPath, subjectTestPath);
//		}
		
		totalIfCondTimeMap = (Map<String, Integer>) FileUtil.loadObjeceFromFile(path  + ".tot.map");
		
		assert totalIfCondTimeMap != null : "TOTAL_IF_COND_TIME_MAP IS NULL!";
		assert totalIfCondTimeMap.size() > 0: "TOTAL_IF_COND_TIME_MAP IS EMPTY!";
		
		
		varUsedByPredateMap = (Map<String, Map<String, Integer>>) FileUtil.loadObjeceFromFile(path  + ".varpred.map");
		assert varUsedByPredateMap != null : "MAP IS NULL!";
		assert varUsedByPredateMap.size() > 0: "MAP IS EMPTY!";
		
		typeUsedByPredateMap =  (Map<String, Map<String, Integer>>) FileUtil.loadObjeceFromFile(path  + ".typepred.map");
		assert typeUsedByPredateMap != null : "MAP IS NULL!";
		assert typeUsedByPredateMap.size() > 0: "MAP IS EMPTY!";
		
		
		mtdNameToUsedParamVars = (Map<String, Set<String>>) FileUtil.loadObjeceFromFile(path  + ".mtdpara.map");
		assert mtdNameToUsedParamVars != null : "ARGS_MAP IS NULL!";
		assert mtdNameToUsedParamVars.size() > 0 : "ARGS_MAP IS EMPTY!";

	}
	
	//reset for each project
	public static void reset(){
		IF_STMT_NUM = 0;
//		IF_RET_NUM = 0;
//		IF_THROW_NUM = 0;
		
		COND_EXPR_NUM = 0;
		
		WHILE_STMT_NUM = 0;
		WHILE_STMT_BRK_NUM = 0;
		WHILE_STMT_CON_NUM = 0;
		
		DO_STMT_NUM = 0;
		DO_STMT_BRK_NUM = 0;
		DO_STMT_CON_NUM = 0;
		
		FOR_STMT_NUM = 0;
		FOR_STMT_BRK_NUM = 0;
		FOR_STMT_CON_NUM = 0;
		
		totalIfCondTimeMap.clear();
		
		CollectionUtil.cleanMapsInTwoDepth(constandsMap, varUsedByPredateMap, typeUsedByPredateMap);
		
		for(Set set : mtdNameToUsedParamVars.values()) {
			set.clear();
		}
		mtdNameToUsedParamVars.clear();
	}
	
	public StatisticVisitor(CompilationUnit cu, File file, ProInfo proInfo) {
		this.cu = cu;
		this.file = file;
		this.fileAbsPath = file.getAbsolutePath();
		this.proInfo = proInfo;
	}
	
	private Map<String, String> getVarToTypeMap(IfStatement node){
		//get all varinfo
		String clsName = this.getClsName(node);
		ClassRepre classRepre = this.getCurrentClassRepre(clsName, node);
		List<VariableInfo> localVarInfoList = getAllVarInfo(node, classRepre, node.getExpression()).getFirst();
		
		Map<String, String> result = new HashMap<>();
		for(VariableInfo info : localVarInfoList) {
			result.put(info.getNameLiteral(), info.getType());
		}
		return result;
	}
	
	private boolean existingConstant(String curr) {
		for(String exsiting: constantsCodes) {
			String exsitingHedaer = exsiting.trim().split("=")[0].trim();
			String currHeader =curr.trim().split("=")[0].trim();
			if(exsitingHedaer.equals(currHeader)) {
				return true;
			}
		}
		return false;
	}
	
	/********************************************************************/
	
	@Override
	public boolean visit(FieldDeclaration node) {
		
		int flag = node.getModifiers();
		boolean constant = Modifier.isFinal(flag) && Modifier.isStatic(flag);
		if(!constant){
			return super.visit(node);
		}
		
		String type = node.getType().toString();
		if(! (TypeUtil.isPrimitiveType(type))){
			return super.visit(node);
		}
		
		if(node.fragments().size() != 1){
			return super.visit(node);
		}
		
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
		Expression initializer = fragment.getInitializer();

		if(fragment.getName().getIdentifier().equals("serialVersionUID") || initializer == null){
			return super.visit(node);
		}
		
		class CallerVisitor extends ASTVisitor{
			public boolean illegal;
			
			@Override
			public boolean visit(ArrayInitializer node) {
				illegal = true;
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodInvocation node) {
				illegal = true;
				return super.visit(node);
			}

			@Override
			public boolean visit(FieldAccess node) {
				illegal = true;
				return super.visit(node);
			}

			@Override
			public boolean visit(QualifiedName node) {
				illegal = true;
				return super.visit(node);
			}

			@Override
			public boolean visit(SimpleName node) {
				if(bannedConstants.contains(node.getIdentifier())){
					illegal = true;
				}
				return super.visit(node);
			}
			
		};
		
		CallerVisitor callerVisitor = new CallerVisitor();
		initializer.accept(callerVisitor);
		
		if(callerVisitor.illegal == false){
			if(!existingConstant(node.toString())){
				constantsCodes.add(node.toString());
			}
		}else{
			bannedConstants.add(fragment.getName().getIdentifier());
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		IF_STMT_NUM++;
		
		Map<String, String> nameToTypeMap= getVarToTypeMap(node);
		
		Expression cond = node.getExpression();
		
		// omit the occurrence 'new Abc(){}'
		ClassInstanceCreationVisitor cicVistor = new ClassInstanceCreationVisitor();
		cond.accept(cicVistor);
		if (cicVistor.hit) {
			return super.visit(node);
		}
		            
		IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
		cond.accept(condTimeVisitor);
		
		//for expr occurrence time
		CondDivisionVisitor divisionVisitor = new CondDivisionVisitor();
		cond.accept(divisionVisitor);
		
		divisionVisitor.getDividedExprList().add(cond);
		
		for(Expression expr : divisionVisitor.getDividedExprList()) {
			String pred = DollarilizeVisitor.naiveDollarilize(expr);
			VarCollectionVisitor varVisitor = new VarCollectionVisitor();
			expr.accept(varVisitor);
			
			for(String varName : varVisitor.getVarList()) {
				putinPredMap(varUsedByPredateMap, varName, pred);
				
				String type = nameToTypeMap.get(varName);
				if(type != null) {
					putinPredMap(typeUsedByPredateMap, type, pred);
				}
			}
			
			PredicateComponentVisitor predicateVisitor = new PredicateComponentVisitor(mtdNameToUsedParamVars);
			expr.accept(predicateVisitor);
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		DO_STMT_NUM++;
		ControlCounterVisitor counter = new ControlCounterVisitor();
		node.accept(counter);
		
		DO_STMT_BRK_NUM += counter.brkNum;
		DO_STMT_CON_NUM += counter.ctnNum;
		
		if(node.getExpression() instanceof BooleanLiteral == false){
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		FOR_STMT_NUM++;
		
		ControlCounterVisitor counter = new ControlCounterVisitor();
		node.accept(counter);
		
		FOR_STMT_BRK_NUM += counter.brkNum;
		FOR_STMT_CON_NUM += counter.ctnNum;	
		
		if(node.getExpression() != null){
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ForStatement node) {
		FOR_STMT_NUM++;
		
		ControlCounterVisitor counter = new ControlCounterVisitor();
		node.accept(counter);
		
		FOR_STMT_BRK_NUM += counter.brkNum;
		FOR_STMT_CON_NUM += counter.ctnNum;	
		
		if(node.getExpression() != null){
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		WHILE_STMT_NUM++;
		ControlCounterVisitor counter = new ControlCounterVisitor();
		node.accept(counter);
		
		WHILE_STMT_BRK_NUM += counter.brkNum;
		WHILE_STMT_CON_NUM += counter.ctnNum;
		
		if(node.getExpression() instanceof BooleanLiteral == false){
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ConditionalExpression node) {
		COND_EXPR_NUM++;
		if(node.getExpression() instanceof BooleanLiteral == false){
			IfCondTimeVisitor condTimeVisitor = new IfCondTimeVisitor();
			node.getExpression().accept(condTimeVisitor);
		}
		return super.visit(node);
	}

	
	@Override
	public void endVisit(CompilationUnit node) {
		
		if(this.constantsCodes.isEmpty()){
			return;
		}
		
		/*reset contants values*/
		StringBuffer codes = new StringBuffer("public class Test{\n");
		for(String line: this.constantsCodes){
			codes.append("\t" + line + "\n");
		}
		codes.append("}");
		
		try {
			Map<String, String> values = DynEvaluator.eval("Test", codes.toString());
			for (Entry<String, String> ety : values.entrySet()) {
				putinConstandesMap(fileAbsPath, ety.getKey(), ety.getValue());
			}
		} catch (Exception e) {

		}
		
		super.endVisit(node);
	}

	/********************************************************************/
	private class IfCondTimeVisitor extends ASTVisitor{
		@Override
		public boolean visit(SimpleName node) {
			
			String varName = node.getIdentifier();
			incTotalIfCondTime(varName);

			return super.visit(node);
		}

		@Override
		public boolean visit(SuperMethodInvocation node) {
			if(node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
				String varName = "THIS";
				incTotalIfCondTime(varName);
			}
			return super.visit(node);
		}


		@Override
		public boolean visit(ThisExpression node) {
			if(node.getLocationInParent() == InfixExpression.LEFT_OPERAND_PROPERTY 
					|| node.getLocationInParent() == InfixExpression.RIGHT_OPERAND_PROPERTY 
					|| node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY){
				String varName = "THIS";
				incTotalIfCondTime(varName);
			}
			
			return super.visit(node);
		}
		
	}
	
	private class ControlCounterVisitor extends ASTVisitor{
		public int brkNum = 0;
		public int ctnNum = 0;
		public int retNum = 0;
		public int throwNum = 0;
		
		@Override
		public boolean visit(BreakStatement node) {
			brkNum++;
			return super.visit(node);
		}

		@Override
		public boolean visit(ContinueStatement node) {
			ctnNum++;
			return super.visit(node);
		}

		@Override
		public boolean visit(ReturnStatement node) {
			retNum++;
			return super.visit(node);
		}

		@Override
		public boolean visit(ThrowStatement node) {
			throwNum++;
			return super.visit(node);
		}
	}
	
	
	private class PredicateComponentVisitor extends ASTVisitor{

		private final Map<String, Set<String>> varToParamTimes;
		
		public PredicateComponentVisitor(Map<String, Set<String>> totalMap) {
			this.varToParamTimes = totalMap;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			String name = node.getName().getIdentifier();
			Set<String> currSet = null;
			if(varToParamTimes.containsKey(name)) {
				currSet = varToParamTimes.get(name);
			}else {
				currSet = new HashSet<>();
				varToParamTimes.put(name, currSet);
			}
			for(Object obj : node.arguments()) {
				if(obj instanceof SimpleName) {
					//int index = node.arguments().indexOf(obj);
					String varname = obj.toString();
					currSet.add(varname);
				}
			}
			return super.visit(node);
		}

	}
}
