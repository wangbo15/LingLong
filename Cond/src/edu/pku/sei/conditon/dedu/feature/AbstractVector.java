package edu.pku.sei.conditon.dedu.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.StringUtil;
import edu.pku.sei.conditon.util.TypeUtil;

public abstract class AbstractVector {
	protected int id = -1;
	protected String fileName;
	protected int line = -1;
	protected int col = -1;
	protected ContextFeature contextFeature;
	
	protected List<VariableInfo> locals;

	protected int allVarNum;
	protected int localNums;
	protected int paramNums;
	protected int fldNums;
	
	
	public AbstractVector(int id, String fileName, int line, int col, ContextFeature contextFeature, List<VariableInfo> locals) {
		this.id = id;
		this.fileName = fileName;
		this.line = line;
		this.col = col;
		this.contextFeature = contextFeature;
		this.locals = locals;
		setVarNums();
	}
	
	private void setVarNums(){
		for(VariableInfo var: locals){
			if(var.getVariableFeature().isField()){
				this.fldNums++;
			}else if(var.getVariableFeature().isParam()){
				this.paramNums++;
			}else{
				this.localNums++;
			}
		}
		this.allVarNum = locals.size();
	}
	
	public final int getId() {
		return id;
	}

	public final String getFileName() {
		return fileName;
	}
	
	public final int getLine() {
		return line;
	}
	
	public final int getCol() {
		return col;
	}
	
	public final ContextFeature getContextFeature() {
		return contextFeature;
	}
	
	public final String getAllLocalVarEncodingStr(){
		assert locals != null;
		if(locals.size() == 0){
			return "[]";
		}
		List<String> nameList = new ArrayList<>();
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField()){
				continue;
			}
			nameList.add(info.getNameLiteral());
		}
		Collections.sort(nameList);
		
		StringBuffer sb = new StringBuffer("[");
		int num = 0;
		for(String nm: nameList){
			num++;
			sb.append(nm);
			sb.append(" ");
		}
		if(num > 1){
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public final String getAllLocalVarTypeEncodingStr(){
		assert locals != null;
		if(locals.size() == 0){
			return "[]";
		}
		Map<String, VariableInfo> nameMap = new TreeMap<>();
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField()){
				continue;
			}
			nameMap.put(info.getNameLiteral(), info);
		}
		StringBuffer sb = new StringBuffer("[");
		int num = 0;
		for(Entry<String, VariableInfo> entry : nameMap.entrySet()){
			num++;
			sb.append(entry.getValue().getType());
			sb.append(" ");
		}
		if(num > 1){
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public final int getIntegerLocalVarNum(){
		int res = 0;
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField()){
				continue;
			}
			if(TypeUtil.isPrimitiveIntType(info.getType())){
				res++;
			}
		}
		return res;
	}
	
	public final int getFloatLocalVarNum(){
		int res = 0;
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField()){
				continue;
			}
			if(TypeUtil.isPrimitiveFloatType(info.getType())){
				res++;
			}
		}
		return res;
	}
	
	public final int getArrayLocalVarNum(){
		int res = 0;
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField()){
				continue;
			}
			if(info.getType().endsWith("[]")){
				res++;
			}
		}
		return res;
	}
	
	public final String getAllFieldEncodingStr(){
		assert locals != null;
		if(locals.size() == 0){
			return "[]";
		}
		
		List<String> nameList = new ArrayList<>();
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField() == false){
				continue;
			}
			if(ASTLocator.maybeConstant(info.getNameLiteral())){
				continue;
			}
			nameList.add(info.getNameLiteral());
		}
		Collections.sort(nameList);
		
		StringBuffer sb = new StringBuffer("[");
		int num = 0;
		for(String nm: nameList){
			num++;
			sb.append(nm);
			sb.append(" ");
		}
		if(num > 1){
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public final String getAllFieldTypeEncodingStr(){
		assert locals != null;
		if(locals.size() == 0){
			return "[]";
		}
		Map<String, VariableInfo> nameMap = new TreeMap<>();
		for(VariableInfo info: locals){
			if(info.getVariableFeature().isField() == false){
				continue;
			}
			if(ASTLocator.maybeConstant(info.getNameLiteral())){
				continue;
			}
			nameMap.put(info.getNameLiteral(), info);
		}
		StringBuffer sb = new StringBuffer("[");
		int num = 0;
		for(Entry<String, VariableInfo> entry : nameMap.entrySet()){
			num++;
			sb.append(entry.getValue().getType());
			sb.append(" ");
		}
		if(num > 1){
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		return sb.toString();
	}

	public final List<VariableInfo> getLocals() {
		return locals;
	}

	
	public final int getAllVarNum() {
		return allVarNum;
	}

	public final int getLocalNums() {
		return localNums;
	}

	public final int getParamNums() {
		return paramNums;
	}

	public final int getFldNums() {
		return fldNums;
	}
	
	protected final static List<VariableInfo> getNearestNLocalVars(List<VariableInfo> locals){
		List<VariableInfo> nearestLocs = new ArrayList<>();
		for(int i = 0; i < ContextFeature.NEAREST_LOCAL_NUM; i++){
			VariableInfo curMax = null;
			for(VariableInfo info: locals){
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
	

	private static final String del = AbstractDeduVisitor.del;
	private static final String NONE = AbstractDeduVisitor.NONE;
	
	private String contextFeatureStr = null;
	
	public final String genContextFeatureStr(){
		if(contextFeatureStr != null) {
			return contextFeatureStr;
		}
		
		ContextFeature context = this.getContextFeature();
		
		String befSyn = context.getAllBefStr();
		String bodySyn = context.getAllBdStr();
		String afSyn = context.getAllAfStr();
		
		List<String> contextFeatureList = new ArrayList<>();
		
		contextFeatureList.add("" + id);
		contextFeatureList.add("" + line);
		contextFeatureList.add("" + col);
		contextFeatureList.add(fileName);
		contextFeatureList.add(context.getTdName());
		contextFeatureList.add(context.getMtdName());
		contextFeatureList.add("" + context.getMtdModifier());
		contextFeatureList.add("" + context.getMtdLineNum());
		contextFeatureList.add("" + this.getLocalNums());
		contextFeatureList.add("" + this.getFldNums());
		contextFeatureList.add("" + this.getParamNums());
		contextFeatureList.add(this.getAllLocalVarEncodingStr());
		contextFeatureList.add(this.getAllLocalVarTypeEncodingStr());
		contextFeatureList.add("" + this.getIntegerLocalVarNum());
		contextFeatureList.add("" + this.getFloatLocalVarNum());
		contextFeatureList.add("" + this.getArrayLocalVarNum());
		contextFeatureList.add(this.getAllFieldEncodingStr());
		contextFeatureList.add(this.getAllFieldTypeEncodingStr());
		contextFeatureList.add("" + context.isInLoop());
		contextFeatureList.add("" + context.getBodyCtl());
		contextFeatureList.add(befSyn);
		contextFeatureList.add(bodySyn);
		contextFeatureList.add(afSyn);
		
		contextFeatureList.addAll(context.getBefSynList());
		contextFeatureList.addAll(context.getBdSynList());
		contextFeatureList.addAll(context.getAfSynList());
		
		List<VariableInfo> nearestLocs = getNearestNLocalVars(locals);
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
		contextFeatureList.add(context.getPreviousPred());
		
		contextFeatureStr = StringUtil.join(contextFeatureList, del);
		return contextFeatureStr;
	}	
}
