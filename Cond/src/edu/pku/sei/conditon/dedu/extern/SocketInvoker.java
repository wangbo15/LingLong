package edu.pku.sei.conditon.dedu.extern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Expression;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.pred.ExprPredItem;
import edu.pku.sei.conditon.dedu.pred.OriPredItem;
import edu.pku.sei.conditon.dedu.pred.RecurNodePredItem;
import edu.pku.sei.conditon.dedu.pred.VarPredItem;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.CollectionUtil;
import edu.pku.sei.conditon.util.StringUtil;

public class SocketInvoker extends AbsInvoker{
	public static final String IP = "localhost";
	public static final int DEFAULT_PORT = 6666;
	
	private static final String CLOSE_MSG = "#@!CLOSE\n";
	
	private static final String BU_V0_MSG = "#@!>>BU_V0";
	private static final String BU_EXPR_MSG = "#@!>>BU_EXPR";
	private static final String BU_VAR_MSG = "#@!>>BU_VAR";
	
	private static final String TD_EXPR_MSG = "#@!>>TD_EXPR";
	private static final String TD_VAR_MSG = "#@!>>TD_VAR";
	
	private static final String RECUR_NODE_MSG = "#@!>>RC_NODE";
	private static final String RECUR_EXPR_MSG = "#@!>>RC_EXPR";
	private static final String RECUR_VAR_MSG = "#@!>>RC_VAR";

	private static final String MSG_END = "#@!<<<<";
	
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private int port = DEFAULT_PORT;
	
	public SocketInvoker(int port, Map<String, OriPredItem> allOriPredicates, Map<String, Integer> pos0TimeMap) {
		super(allOriPredicates, pos0TimeMap);
		try {
			this.port = port;
			this.socket = new Socket(IP, port);
			System.out.println("Client start @ " + IP + ":" + port + " !!");
			this.out = new PrintWriter(socket.getOutputStream()); // output，to sever socket
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // input， from sever socket
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.hook();
	}
	
	public SocketInvoker(Map<String, OriPredItem> allOriPredicates, Map<String, Integer> pos0TimeMap) {
		super(allOriPredicates, pos0TimeMap);
		try {
			this.socket = new Socket(IP, port);
			System.out.println("Client start @ " + IP + ":" + port + " !!");
			this.out = new PrintWriter(socket.getOutputStream()); // output，to sever socket
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // input， from sever socket
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.hook();
	}
	
	@Override
	public List<ExprPredItem> predictExpr(String direction, String featureLine){
		String content = "";
		String header = "";
		if("BU".equals(direction)) {
			header = AbstractDeduVisitor.getButtomUpStepOneHeader().trim();
			content = BU_EXPR_MSG + "\n" + header + "\n" + featureLine + "\n" + MSG_END;
		}else if("TD".equals(direction)) {
			header = AbstractDeduVisitor.getTopDownStepZeroHeader().trim();
			
			int num = header.split("\t").length - 1;
			String[] arr = featureLine.split("\t");
			String resLine = arr[0];
			for(int i = 1; i < num; i++) {
				resLine = resLine + "\t" + arr[i];
			}
			resLine = resLine + "\t?";
			content = TD_EXPR_MSG + "\n" + header + "\n" + resLine + "\n" + MSG_END;
			
			System.err.println(resLine);
		}
		
		writeToServer(content);
		List<ExprPredItem> exprs = getSortedExprList("");
		return exprs;
	}
	
	@Override
	public List<VarPredItem> predictVar(String direction, int n, List<String> featureLines, Map<String, VariableInfo> allVarInfoMap){
		String content = "";
		String header = "";
		if("BU".equals(direction)) {
			//if(n == 0) {
			header = AbstractDeduVisitor.getButtomUpStepZeroHeader().trim();
			content = BU_V0_MSG + "\n" + header + "\n";
			for(String line: featureLines) {
				String newLine = "";
				int num = header.split("\t").length;
				String[] arr = line.split("\t");
				for(int i = 0; i < num - 2; i++) {
					newLine += arr[i] + "\t"; 
				}
				newLine +=  "0\t?";
				assert newLine.split("\t").length == num;
				content += newLine + "\n";
			}
			//} else {
			//	header = AbstractDeduVisitor.getButtomUpStepTwoHeader().trim();
			//	content = BU_VAR_MSG + "\n" + header + "\n";
			//} 
		} else if("TD".equals(direction)) {
			header = AbstractDeduVisitor.getTopDownStepOneHeader().trim();
			content = TD_VAR_MSG + "\n" + header + "\n";
		}
		
		content += MSG_END;
		//System.err.println(content);
		writeToServer(content);
		List<VarPredItem> varsAtN = getSortedVarList(allVarInfoMap, n);
		return varsAtN;
	}
	
	@Override
	public void prepare() {
		if(this.socket == null || this.socket.isClosed()) {
			throw new Error("NO SEVER");
		}
	}

	@Override
	public void finish() {
		if(this.socket != null && !this.socket.isClosed()) {
			try {
				this.writeToServer(CLOSE_MSG);
				this.socket.close();
				
				System.out.println("Client disconnect!!");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<ExprPredItem> predictTDExprs(String ctxFea) {
		String exprData = genTopDownExprDataForServer(ctxFea);
		//System.err.println(exprData);
		writeToServer(exprData);
		//TODO: change "fileName" to empty string
		List<ExprPredItem> exprs = getSortedExprList("");
//		if(!exprs.isEmpty()) {
//			ExprPredItem first = exprs.get(0);
//			if(first.getScore() == 1.0) {
//				System.err.println(first.toString());
//			}
//		}
		CollectionUtil.remainListFirstK(exprs, CONFIG.getExprLimit());
		return exprs;
	}

	@Override
	public List<VarPredItem> predictTDVars(int n, List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap){
		String varData = genTopDownVarDataForServer(varFeatersAtN);
		//System.err.println(varData);
		writeToServer(varData);
		List<VarPredItem> varsAtN = getSortedVarList(allVarInfoMap, n);
		CollectionUtil.remainListFirstK(varsAtN, CONFIG.getVarLimit());
		return varsAtN;
	}
	
	@Override
	public List<VarPredItem> predictBUV0(Map<String, VariableInfo> allVarInfoMap, Map<String, String> varToFeaPrefixMap){
		String v0Data = genBottomUpV0DataForServer(varToFeaPrefixMap);
		if(v0Data == null) {
			return Collections.emptyList();
		}
		//System.err.println(v0Data);
		writeToServer(v0Data);
		List<VarPredItem> vars = getSortedVarList(allVarInfoMap, 0);
		CollectionUtil.remainListFirstK(vars, CONFIG.getVarLimit());
		return vars;
	}

	@Override
	public List<ExprPredItem> predictBUExprs(String v0Iter, String curVarFea){
		String exprData = genBottomUpExprDataForServer(v0Iter, curVarFea);
		//System.err.println(exprData);
		writeToServer(exprData);
		List<ExprPredItem> exprs = getSortedExprList("");
//		if(!exprs.isEmpty()) {
//			ExprPredItem first = exprs.get(0);
//			if(first.getScore() == 1.0) {
//				System.err.println(first.toString());
//			}
//		}
		CollectionUtil.remainListFirstK(exprs, CONFIG.getExprLimit());
		return exprs;
	}
	
	@Override
	public List<VarPredItem> predictBUVars(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n){
		String varData = genBottomUpVarDataForServer(varFeatersAtN);
		writeToServer(varData);
		List<VarPredItem> varsAtN = getSortedVarList(allVarInfoMap, n);
		CollectionUtil.remainListFirstK(varsAtN, CONFIG.getVarLimit());
		return varsAtN;
	}
	
	@Override
	public List<RecurNodePredItem> predictForNodeTypes(String featureLine) {
		String data = genRecurNodeDataForServer(featureLine);
		writeToServer(data);
		List<RecurNodePredItem> predNodes = getSortedRecurNodes();
		return predNodes;
	}
	
	@Override
	public List<ExprPredItem> predictRecurExprs(String featureLine) {
		String exprData = genRecurExprDataForServer(featureLine);
		writeToServer(exprData);
		//TODO: change "fileName" to empty string
		List<ExprPredItem> exprs = getSortedExprList("");
		CollectionUtil.remainListFirstK(exprs, CONFIG.getExprLimit());
		return exprs;
	}
	
	@Override
	public List<VarPredItem> predictRecurVar(List<String> varFeatersAtN, Map<String, VariableInfo> allVarInfoMap, int n){
		String data =  genRecurVarDataForServer(varFeatersAtN);
		writeToServer(data);
		List<VarPredItem> varsAtN = getSortedVarList(allVarInfoMap, n);
		CollectionUtil.remainListFirstK(varsAtN, CONFIG.getVarLimit());
		return varsAtN;
	}
	
	private List<RecurNodePredItem> getSortedRecurNodes(){
		List<RecurNodePredItem> predNodes = new ArrayList<>(4);
		List<String> lines = readFromServer();
		for(String line : lines) {
			String[] columns = line.split("\t");
			assert columns.length == 2;
			int label = Integer.valueOf(columns[0]);
			double score = Double.valueOf(columns[1]);
			RecurNodePredItem item = new RecurNodePredItem(label, score);
			predNodes.add(item);
		}
		return predNodes;
	}
	
	private String genRecurNodeDataForServer(String featureLine) {
		List<String> features = new ArrayList<>(5);
		features.add(RECUR_NODE_MSG);
		String header = AbstractDeduVisitor.getRecurNodeTypeHeader().trim();
		features.add(header);
		
		//String line = contextFeature + recurNodeFea + "?";
		features.add(featureLine);
		
		predictTime++;
		recurNodePredictTime++;
		
		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}

	private String genRecurExprDataForServer(String line) {
		List<String> features = new ArrayList<>(5);
		features.add(RECUR_EXPR_MSG);
		String header = AbstractDeduVisitor.getRecurNodeExprHeader().trim();
		features.add(header);
		//String line = ctxFea + recurNodeFea + "\t?";
		features.add(line);
		
		predictTime++;
		exprPredictTime++;
		
		features.add(MSG_END);
		String data = StringUtil.join(features, "\n");
		return data;
	}
	
	private String genRecurVarDataForServer(List<String> features) {
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(RECUR_VAR_MSG);
		String header = AbstractDeduVisitor.getRecurNodeVarHeader().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);
		
		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
	
	private List<VarPredItem> getSortedVarList(Map<String, VariableInfo> allVarInfoMap, int position) {
		List<VarPredItem> predVars = new ArrayList<>();

		List<String> predRes = readFromServer();
		for(String line : predRes) {
			//System.out.println(line);
			String[] columns = line.split("\t");
			assert columns.length == 2;
			String var = columns[0];
			Double score = new Double(columns[1]);
			VariableInfo info = allVarInfoMap.get(var);
			if(info == null) {
				if (var.endsWith("#F")) {
					String nonFld = var.substring(0, var.length() - 2);
					info = allVarInfoMap.get(nonFld);
					if(info == null) {
						continue;
					}
				}
			}
			VarPredItem item = new VarPredItem(var, position, info, score);
			predVars.add(item);
		}
		VarPredItem.adjustVarsProbability(predVars);
		
		Collections.sort(predVars, new Comparator<VarPredItem>(){
			@Override
			public int compare(VarPredItem o1, VarPredItem o2) {
				return o2.compareTo(o1);
			}
			
		});
		return predVars;
	}
	
	private List<ExprPredItem> getSortedExprList(String javaSrcFileName) {
		ArrayList<ExprPredItem> predExprs = new ArrayList<>(CONFIG.getExprLimit());
		List<String> predRes = readFromServer();
		
		/*
		System.out.println("------------------------------------------------------------------");
		int oneSum = 0;
		int twoSum = 0;
		int otherSum = 0;
		for(int i = 0; i < predRes.size(); i++) {
			String line = predRes.get(i);
			int occur = StringUtil.charOccurTimesInStr(line, '$');
			if(occur == 1)
				oneSum++;
			if(occur == 2)
				twoSum++;
			else
				otherSum++;
			System.out.println(i + "\t" + line + "\t" + oneSum + "\t" + twoSum);
		}*/
		
		for(String line : predRes) {
			//System.out.println(line);
			String[] columns = line.split("\t");
			assert columns.length == 2;
			String expr = columns[0];
			
			if(expr.contains("'$'")) {
				continue;
			}
			try {
				Expression astnode = ExprPredItem.generateASTNodeForDollarExpr(expr);
				Double score = new Double(columns[1]);
				ExprPredItem item = new ExprPredItem(javaSrcFileName, expr, astnode, score);
				predExprs.add(item);
			}catch (ClassCastException e) {
				System.err.println(expr);
				continue;
			}
		}
		predExprs.trimToSize();
		return predExprs;
	}
	
	private List<String> readFromServer() {
		List<String> lines = new ArrayList<>(10);
		try {
			String line;
			while ((line = in.readLine()) != null) {
				if(line.trim().equals(MSG_END)) {
					break;
				}
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private void writeToServer(String content) {
		this.out.print(content);
		this.out.flush();
	}
	
	private String genBottomUpV0DataForServer(Map<String, String> varToFeaPrefixMap) {
		List<String> varFeatures = new ArrayList<>(DEFAULT_VAR_RES_NUM);
		varFeatures.add(BU_V0_MSG);
		String header = AbstractDeduVisitor.getButtomUpStepZeroHeader().trim();
		varFeatures.add(header);
		generateBottomUpV0Lines(varFeatures, varToFeaPrefixMap, pos0TimeMap);
		
		if(varFeatures.size() == 2) {
			return null;
		}
		assert checkLines(varFeatures.subList(1, varFeatures.size()));
		
		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
	
	private String genBottomUpExprDataForServer(String v0Iter, String v0Fea) {
		String posZeroTime = "" + (pos0TimeMap.containsKey(v0Iter) ? pos0TimeMap.get(v0Iter) : 0);
		String exprLine = StringUtil.connectMulty(del, v0Fea, posZeroTime, "?"); 

		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(BU_EXPR_MSG);
		String header = AbstractDeduVisitor.getButtomUpStepOneHeader().trim();
		exprFeatures.add(header);
		exprFeatures.add(exprLine);
		
		assert checkLines(exprFeatures.subList(1, exprFeatures.size()));
		
		//FileUtil.writeStringToFile("/home/nightwish/tmp/bu.pred.csv", exprLine + "\n", true);
		
		predictTime++;
		exprPredictTime++;
		
		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}
	
	private String genBottomUpVarDataForServer(List<String> features){
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(BU_VAR_MSG);
		String header = AbstractDeduVisitor.getButtomUpStepTwoHeader().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);
		
		assert checkLines(varFeatures.subList(1, varFeatures.size()));
		
		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
		
	private static String genTopDownExprDataForServer(String contextFeature) {
		List<String> exprFeatures = new ArrayList<>(5);
		exprFeatures.add(TD_EXPR_MSG);
		String header = AbstractDeduVisitor.getTopDownStepZeroHeader().trim();
		exprFeatures.add(header);
		
		String line = contextFeature + AbstractDeduVisitor.del + "?";
		exprFeatures.add(line);
		
		assert checkLines(exprFeatures.subList(1, exprFeatures.size()));
		
		predictTime++;
		exprPredictTime++;
		
		exprFeatures.add(MSG_END);
		String data = StringUtil.join(exprFeatures, "\n");
		return data;
	}
	
	private static String genTopDownVarDataForServer(List<String> features) {
		List<String> varFeatures = new ArrayList<>(features.size() + 5);
		varFeatures.add(TD_VAR_MSG);
		String header = AbstractDeduVisitor.getTopDownStepOneHeader().trim();
		varFeatures.add(header);
		varFeatures.addAll(features);
		
		assert checkLines(varFeatures.subList(1, varFeatures.size()));

		predictTime += varFeatures.size() - 2;
		varPredictTime += varFeatures.size() - 2;
		
		varFeatures.add(MSG_END);
		String data = StringUtil.join(varFeatures, "\n");
		return data;
	}
	
	private void hook() {  
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {  
            @Override  
            public void run()  
            {  
            	finish();  
            }  
        }));  
    }

}
