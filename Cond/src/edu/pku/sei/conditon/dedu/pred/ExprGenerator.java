package edu.pku.sei.conditon.dedu.pred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.dedu.DeduFeatureGenerator;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.Pair;
import edu.pku.sei.conditon.util.TypeUtil;
import edu.pku.sei.proj.ProInfo;

public class ExprGenerator {
	
	private static final ConditionConfig CONFIG = ConditionConfig.getInstance();
	
	private List<ExprPredItem> predExprs;
//	private List<VarPredItem> predVars;
	private ProInfo proInfo;
	
	private final int topNExpr = 500;
		
	private Map<String, OriPredItem> allOriPredicates;
	
	private final static Set<String> symmetryExprs;
	private final static Set<String> intFloatConstracts;
	private final static Set<String> presionConstracts;
		
	static {
		symmetryExprs = new HashSet<>();
		symmetryExprs.add("$ != $");
		symmetryExprs.add("$ == $");
		symmetryExprs.add("$.length == $.length");
		symmetryExprs.add("$.length != $.length");
		symmetryExprs.add("$[0] == $[0]");
		symmetryExprs.add("$[1] == $[1]");
		symmetryExprs.add("$[2] == $[2]");
		symmetryExprs.add("$[0] != $[0]");
		symmetryExprs.add("$[1] != $[1]");
		symmetryExprs.add("$[2] != $[2]");
		symmetryExprs.add("$.size() == $.size()");
		symmetryExprs.add("$.size() != $.size()");
		symmetryExprs.add("$ * $ > 0");
		symmetryExprs.add("$ * $ < 0");
		symmetryExprs.add("$ * $ == 0");
		symmetryExprs.add("$ * $ >= 0");
		symmetryExprs.add("$ * $ <= 0");
		symmetryExprs.add("$ + $ > 0");
		symmetryExprs.add("$ + $ < 0");
		symmetryExprs.add("$ + $ == 0");
		symmetryExprs.add("$ + $ >= 0");
		symmetryExprs.add("$ + $ <= 0");


		intFloatConstracts = new HashSet<>();
		intFloatConstracts.add("$ >");
		intFloatConstracts.add("$ >=");
		intFloatConstracts.add("$ ==");
		intFloatConstracts.add("$ !=");
		intFloatConstracts.add("$ <=");
		intFloatConstracts.add("$ <");
		
		
		presionConstracts = new HashSet<>();
		presionConstracts.add("$ > $");
		presionConstracts.add("$ >= $");
		presionConstracts.add("$ <= $");
		presionConstracts.add("$ < $");
	}
		
	public ExprGenerator(List<ExprPredItem> predExprs, Map<String, OriPredItem> allOriPredicates){
		this.predExprs = predExprs;
		
		this.allOriPredicates = allOriPredicates;
		
		this.proInfo = DeduFeatureGenerator.proInfoTmp;
		assert proInfo != null;
	}
	
	private static int typedVarNum(String tp){
		int hit = 0;
		if(tp.contains("<") && tp.contains(">")){
			tp = tp.split("<")[0];
		}
		
//		VarPredItem v0 = exprItem.getPredVars().get(0).get(0);
		
		List<VariableInfo> allVariables = DeduFeatureGenerator.getAllVariables();
		for(VariableInfo info : allVariables){
			
			//maybe constant
			if (ASTLocator.maybeConstant(info.getNameLiteral()) || Character.isUpperCase(info.getNameLiteral().charAt(0))){
				continue;
			}
			
			String infoTp = info.getType();
			
			if (tp.equalsIgnoreCase("float") || tp.equalsIgnoreCase("double")) {
				if (infoTp.equalsIgnoreCase("float") || infoTp.equalsIgnoreCase("double") || infoTp.equalsIgnoreCase("long") || infoTp.equalsIgnoreCase("int")) {
					hit++;
				}

			} else if (tp.equals("int") || tp.equals("Integer")) {
				if (infoTp.equals("int") || infoTp.equals("Integer")) {
					hit++;
				}

			} else if(tp.equalsIgnoreCase("long")){
				if(infoTp.equals("int") || infoTp.equals("Integer") || infoTp.equalsIgnoreCase("long")){
					hit++;
				}
			} else if(tp.equals("char") || tp.equals("Character")){
				if(infoTp.equals("char") || infoTp.equals("Character")){
					hit++;
				}
			} else if(infoTp.equals(tp)){
				hit++;
			}
		}
		return hit;
	}
	
	private boolean isValidVar(int position, VarPredItem item, OriPredItem oriPredItem) {
		String litTmp = oriPredItem.getLiteral().replaceAll("\\s", "");
		
		if(litTmp.equals("$==null") || litTmp.equals("$!=null")){
			
			List<VariableInfo> allVariables = DeduFeatureGenerator.getAllVariables();	
			for(VariableInfo info : allVariables){
				//of the same name
				if(info.getNameLiteral().equals(item.getLiteral())){
					if(TypeUtil.isPurePrimitiveType(info.getType()) == false){
							return true;
						}
					}
			}
		}
		
		Set<String> allNeededTypes = new HashSet<>();
		for(String encodedType : oriPredItem.getPosToTypesMap().get(position)){
			encodedType = TypeUtil.removeGenericType(encodedType);
			allNeededTypes.add(encodedType);
		}

		String infoType = item.getInfo().getType();
		infoType = TypeUtil.removeGenericType(infoType);
		
		for(String t : allNeededTypes){
			if(TypeUtil.isPrimitiveNumType(t) && TypeUtil.isPrimitiveNumType(infoType)){
				return true;
			}else if(t.equals(infoType)){
				return true;
			}
		}
		return false;
	}
		
	public static String getProcessedExpr(String expr) {
		return expr.replaceAll("\\s", "");
	}
	
	public static boolean isFit(ExprPredItem exprItem, VarPredItem varItem, int n, Map<String, OriPredItem> allOriPreds) {
		String literal = exprItem.getPred();
		String litTmp = getProcessedExpr(literal);
		String type = varItem.getInfo().getType();
		boolean isPrim = TypeUtil.isPurePrimitiveType(type);
		if(litTmp.equals("$==null") || litTmp.equals("$!=null") || litTmp.contains("instanceof")){
			if(isPrim){
				return false;
			}
			return true;
		}
		// TODO: 
		OriPredItem oriPredItem = allOriPreds.get(litTmp);
		if(oriPredItem == null){
			return true;
		}
		Set<String> types = oriPredItem.getPosToTypesMap().get(n);
		for(String t: types){
			if(TypeUtil.mayMatchingTypeVariable(t, varItem.getInfo())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isLegalExprForV0(VarPredItem varItem, ExprPredItem exprItem, Map<String, OriPredItem> allOriPreds){
		String literal = exprItem.getPred();
		String litTmp = getProcessedExpr(literal);
		String type = varItem.getInfo().getType();
		boolean isPrim = TypeUtil.isPurePrimitiveType(type);
		if(litTmp.equals("$==null") || litTmp.equals("$!=null") || litTmp.contains("instanceof")){
			if(isPrim){
				return false;
			}
			return true;
		}
		
		OriPredItem oriPredItem = allOriPreds.get(litTmp);
		if(oriPredItem == null){
			//System.err.println("PRED EXPR NOT FOUND: " + literal);
			return false;
		}
		
		if(isPrim && exprItem.getPositionNum() == 1 && exprItem.getAstNode() instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression) exprItem.getAstNode();
			if(infix.getRightOperand() instanceof QualifiedName) {
				String qualifier = ((QualifiedName) infix.getRightOperand()).getQualifier().toString();
				if(TypeUtil.isPackageType(qualifier) && !TypeUtil.getPrimitiveType(qualifier).equals(type)) {
					return false;
				}
			}
			
		}
		
		
		Set<String> types = oriPredItem.getPosToTypesMap().get(0);
		boolean legalType = false;
		for(String t: types){
			if(TypeUtil.mayMatchingTypeVariable(t, varItem.getInfo())){
				legalType = true;
				break;
			}
		}
		if(!legalType) {
			return false;
		}
		String varName = varItem.getLiteral();
		if(isPrim && varName.length() > 3) {//TODO
			Expression node = exprItem.getAstNode();
			if(node != null) {
				if(isSimpleInfix(node)) {
					Map<String, Integer> currPosZeroMap = oriPredItem.getPosToOccurredVarTimesMap().get(0);
					if(currPosZeroMap.containsKey(varName) == false) {
						return false;
					}
				}
			}
			
		}
		return true;
	}
	
	public static boolean isLegalExprItem(ExprPredItem exprItem, Map<String, OriPredItem> allOriPredicates){
		if(CONFIG.isBottomUp() && exprItem.getPositionNum() == 1){
			return true; // has been checked at v0 in bottom up
		}
		
		String literal = exprItem.getPred().replaceAll("\\s", "");
		
		OriPredItem  oriPredItem = allOriPredicates.get(literal);
		if(oriPredItem == null){
			//System.err.println("PRED EXPR NOT FOUND: " + literal);
			return false;
		}
		
		int slopNum = oriPredItem.getSlopNum();
		
		assert slopNum > 0;
				
		for(int pos = 1; pos < slopNum; pos++){
			Set<String> types = oriPredItem.getPosToTypesMap().get(pos);
			boolean found = false;
			for(String type : types){
				if(typedVarNum(type) > 0){
					found = true;
				}
			}
			if(!found){
				return false;
			}
		}
		return true;//TODO: len < len is not filted
	}
	
	/**
	 * This method is to generate expression in an enumerate fashion
	 * @return
	 */
	public List<GenExprItem> generateExpr(){
		int curExprNum = 0;
		int illegal = 0;
				
		List<GenExprItem> allGeneratedExpr = new ArrayList<>();
		
		for(ExprPredItem exprItem : predExprs){
			curExprNum++;
			if(!isLegalExprItem(exprItem, allOriPredicates)){
//				System.out.println("ILLEGAL EXPR: " + exprItem.getPred());
				illegal++;
				continue;
			}
			
			if(curExprNum - illegal> this.topNExpr){
				break;
			}
			
			String keyPred = exprItem.getPred().replaceAll("\\s", "");
			OriPredItem oriPredItem = allOriPredicates.get(keyPred);
			int slopNum = oriPredItem.getSlopNum();
			
			List<List<VarPredItem>> validVarList = new ArrayList<>();
			
			boolean unfinished = false;
			for(int pos = 0; pos < slopNum; pos++){
				
				List<VarPredItem> validList = new ArrayList<>();
				List<VarPredItem> sortedVariables = exprItem.getPredVars().get(pos);
				for(VarPredItem item : sortedVariables){
					if(isValidVar(pos, item, oriPredItem)){
						validList.add(item);
					}
				}
				if(validList.isEmpty()){
					unfinished = true;
					break;
				}
				validVarList.add(validList);
			}
			if(unfinished){//uncompleted expr
				continue;
			}
			
			List<List<VarPredItem>> recursiveResult = new ArrayList<>();
			
	        recursive(validVarList, recursiveResult, 0, new ArrayList<VarPredItem>());  
			
//	        System.out.println(exprItem.getPred() + "    SIZE: " + recursiveResult.size());  
	        for (List<VarPredItem> list : recursiveResult) { 
	        	GenExprItem generated = new GenExprItem(exprItem, list);
	        	
//	        	Map<String, Integer> duplicMap = new HashMap<>();
//	        	if(FileInvoker.designatedVars != null && !FileInvoker.designatedVars.isEmpty()) {
//	        		for(String var : FileInvoker.designatedVars) {
//	        			int time = 0;
//	        			if(duplicMap.containsKey(var)) {
//	        				time = duplicMap.get(var);
//	        			}
//	        			duplicMap.put(var, time + 1);
//	        		}
//        		}
//	        	
//	        	if(checkVarOccurTime(list, duplicMap)){
//	        		continue;
//	        	}
	        	
	        	allGeneratedExpr.add(generated);
	        	/*
	        	System.out.print("\t");
	            for (VarPredItem var : list) {  
	                System.out.print(var.getLiteral() + " ");  
	            }
	            System.out.println();  
	            */
	        } 
		}
		System.out.println(predExprs.size() + " TOP " + curExprNum + " HAS " + illegal + " ILLEGAL PRED EXPRS");
		System.out.println("ALL GENREATED EXPR NUM: " + allGeneratedExpr.size());
		
//		Set<String> outTmp = new HashSet<>();
//		for(GenExprItem generated : allGeneratedExpr){
//			String pre = generated.getExpr().getPred();
//			if(outTmp.contains(pre)){
//				continue;
//			}else{
//				outTmp.add(pre);
//				System.out.println("\t" + pre);
//			}
//		}
		
		Collections.sort(allGeneratedExpr, new Comparator<GenExprItem>(){
			@Override
			public int compare(GenExprItem o1, GenExprItem o2) {
				return o2.getScore().compareTo(o1.getScore());
			}
		});
//		for(int i = 0; i < 1000; i++){
//			System.out.println(i + " " + allGeneratedExpr.get(i));
//		}
		
		List<GenExprItem> tobeRemoved = tobeRemoved(allGeneratedExpr);
		allGeneratedExpr.removeAll(tobeRemoved);
		return allGeneratedExpr;
	}

	private boolean varItemTimeDismath(List<VarPredItem> varPredItemList, Map<String, Integer> duplicMap) {
		Map<String, Integer> varLiteralTimesMap = new HashMap<>();
		for(VarPredItem item: varPredItemList){
			String key = item.getLiteral();
			int time = 0;
			if(varLiteralTimesMap.containsKey(key)) {
				time = varLiteralTimesMap.get(key);
			}
			varLiteralTimesMap.put(key, time + 1);
		}
		
		for(Entry<String, Integer> entry: varLiteralTimesMap.entrySet()) {
			String key = entry.getKey();
			if(duplicMap.containsKey(key)) {
				if(entry.getValue() != duplicMap.get(key)) {
					return true;
				}
			}else {
				return true;
			}
		}
		return false;
	}
	
	private boolean usingSameVarItem(List<VarPredItem> varPredItemList) {
		Set<String> varLiteralSet = new HashSet<>();
		for(VarPredItem item: varPredItemList){
			varLiteralSet.add(item.getLiteral());
		}
		if(varLiteralSet.size() < varPredItemList.size()){
			return true;
		}
		return false;
	}
	
	private boolean checkVarOccurTime(List<VarPredItem> varPredItemList, Map<String, Integer> duplicMap) {
		if(duplicMap.isEmpty()) {
			return usingSameVarItem(varPredItemList);
		}else {
			return varItemTimeDismath(varPredItemList, duplicMap);
		}
	}

	private static void recursive (List<List<VarPredItem>> dimValue, List<List<VarPredItem>> result, int layer, List<VarPredItem> curList) {  
        if (layer < dimValue.size() - 1) {  
            if (dimValue.get(layer).size() == 0) {  
                recursive(dimValue, result, layer + 1, curList);  
            } else {  
                for (int i = 0; i < dimValue.get(layer).size(); i++) { 
                    List<VarPredItem> list = new ArrayList<>(curList);  
                    list.add(dimValue.get(layer).get(i));  
                    recursive(dimValue, result, layer + 1, list);  
                }  
            }  
        } else if (layer == dimValue.size() - 1) {  
            if (dimValue.get(layer).size() == 0) {  
                result.add(curList);  
            } else {  
                for (int i = 0; i < dimValue.get(layer).size(); i++) {  
                    List<VarPredItem> list = new ArrayList<>(curList);  
                    list.add(dimValue.get(layer).get(i));  
                    result.add(list);  
                }  
            }  
        }  
    }
	
	private List<GenExprItem> tobeRemoved(List<GenExprItem> allGeneratedExpr) {
		List<GenExprItem> tobeRemoved = new ArrayList<>();
		Map<String, Set<Pair<String, String>>> occuredVars = new HashMap<>();
		for(GenExprItem genExprItem : allGeneratedExpr) {
			String pred = genExprItem.getExpr().getPred();
			
			if(pred.contains("++$") || pred.contains("--$") || pred.contains("$++") || pred.contains("$--") || pred.contains(" & ")) {
				tobeRemoved.add(genExprItem);
			}else if(pred.startsWith("$.length") || pred.startsWith("$[")) {
				VarPredItem var0 = genExprItem.getVars().get(0);
				if(var0.getInfo().getType().endsWith("[]") == false) {//not array
					tobeRemoved.add(genExprItem);
				}
			}else if(symmetryExprs.contains(pred)) {
				VarPredItem left = genExprItem.getVars().get(0);
				VarPredItem right = genExprItem.getVars().get(1);
				boolean leftIsPrrimitive = TypeUtil.isPrimitiveType(left.getInfo().getType());
				boolean rightIsPrrimitive = TypeUtil.isPrimitiveType(right.getInfo().getType());
				if(leftIsPrrimitive != rightIsPrrimitive) {
					tobeRemoved.add(genExprItem);
					continue;
				}
				Set<Pair<String, String>> currSet = null;
				if(occuredVars.containsKey(pred)) {
					currSet = occuredVars.get(pred);
				}else {
					currSet = new HashSet<>();
					occuredVars.put(pred, currSet);
				}
				currSet.add(new Pair<String, String>(left.getLiteral(), right.getLiteral()));
				for(Pair<String, String> pair : currSet) {
					if(pair.getFirst().equals(right.getLiteral()) && pair.getSecond().equals(left.getLiteral())) {
						tobeRemoved.add(genExprItem);
						break;
					}
				}
			}
			if(genExprItem.getExpr().getPositionNum() == 1) {
				String tp = genExprItem.getVars().get(0).getInfo().getType();
				if(TypeUtil.isPrimitiveIntType(tp) == false) {
					continue;
				}
				int idx = pred.lastIndexOf(" ");
				if(idx < 0) {
					continue;
				}
				String prefix = pred.substring(0, idx);
				if(intFloatConstracts.contains(prefix)) {
					String tail = pred.substring(idx + 1);
					if(TypeUtil.isSmpleDouble(tail)) {
						tobeRemoved.add(genExprItem);
					}
				}
			}else if(genExprItem.getExpr().getPositionNum() == 2 && presionConstracts.contains(pred)) {
				VarPredItem left = genExprItem.getVars().get(0);
				VarPredItem right = genExprItem.getVars().get(1);
				String leftTp = left.getInfo().getType();
				String rightTp = right.getInfo().getType();
				if(TypeUtil.is32BitInt(leftTp) && TypeUtil.is64BitInt(rightTp) || TypeUtil.is32BitInt(rightTp) && TypeUtil.is64BitInt(leftTp)) {
					tobeRemoved.add(genExprItem);
				}
			}
		}
		return tobeRemoved;
	}
	
	private static boolean isSimpleInfix(Expression expr) {
		if(expr instanceof InfixExpression == false) {
			return false;
		}
		InfixExpression infix = (InfixExpression) expr;
		if(infix.getLeftOperand() instanceof SimpleName && infix.getRightOperand() instanceof NumberLiteral) {
			return true;
		}
		return false;
	}
}
