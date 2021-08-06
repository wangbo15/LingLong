package edu.pku.sei.conditon.dedu.predall;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import edu.pku.sei.conditon.util.FileUtil;

public class ConditionConfig implements Serializable {

	private static final long serialVersionUID = 2437087075125779943L;
	
	public static final String CONFIG_FILE = "config.ini";
	
	public static final String PYTHON_CONFIG_FILE = "../python/env.conf";
	
	private static ConditionConfig instance;
	
	private boolean useSocket;
	private boolean useConcrete;
	private boolean debug;
	private boolean opt;
	private boolean predAll;
	private boolean predAllPreparing;
	
	public enum PROCESSING_TYPE {
		D4J, 
		BUS_DOT_JAR, 
		GIT_REPOS
	};
	
	private PROCESSING_TYPE processType;
	
	private boolean recur;
	private boolean bottomUp;
	
	private SearchMethod searchMethod;
	
	public enum SearchMethod{
		BEAM(0, "BEAM"),
		GREEDY_BEAM(1, "GREEDY_BEAM"),
		DFS(2, "DFS"),
		DIJKSTRA(3, "DIJKSTRA");
		
		private int idx;
		private String name;
		
		private SearchMethod(int idx, String name) {
			this.idx = idx;
			this.name = name;
		}

		public int getIdx() {
			return idx;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return this.getName();
		}
	}
	
	private int greedyBeamSearchLimits;
	
	private int beamSearchLimits;
	private int beamSearchResultLimits;
	
	private int predResLimits;
	
	private int dijkstraSearchLimits;
	
	private boolean typeConstraint;
	private boolean compilationFilter;
	
	private double treeProbLimit;
	private double exprProbLimit;
	private double varProbLimit;
	private double rnProbLimit;
	
	private int treeDepth;
	
	private int exprLimit;
	private int exprVarLimit;
	private int varLimit;
	
	private String learningAlgorithm;
	
	public static ConditionConfig getInstance() {
		if(instance == null) {
			instance = new ConditionConfig();
		}
		return instance;
	}
	
	private void loadConditionConfigFile() {
		Properties p = new Properties();
		ClassLoader classLoader = ConditionConfig.class.getClassLoader();
		
		try {
			InputStream ins = classLoader.getResourceAsStream(CONFIG_FILE);
			assert ins != null;
			p.load(ins);
			
			this.useSocket = Boolean.parseBoolean(p.getProperty("useSocket", "false"));
			this.useConcrete = Boolean.parseBoolean(p.getProperty("useConcrete", "false"));
			this.debug = Boolean.parseBoolean(p.getProperty("debug", "true"));
			this.opt = Boolean.parseBoolean(p.getProperty("opt", "false"));
			this.predAll = Boolean.parseBoolean(p.getProperty("predAll", "true"));
			this.predAllPreparing = Boolean.parseBoolean(p.getProperty("predAllPreparing", "false"));
			
			String pt = p.getProperty("processType", "d4j");
			if(pt.equalsIgnoreCase("d4j")) {
				this.processType = PROCESSING_TYPE.D4J;
			} else if (pt.equalsIgnoreCase("bdj")) {
				this.processType = PROCESSING_TYPE.BUS_DOT_JAR;
			} else if (pt.equalsIgnoreCase("github")) {
				this.processType = PROCESSING_TYPE.GIT_REPOS;
			} else {
				throw new Error(pt);
			}
			
			this.recur = Boolean.parseBoolean(p.getProperty("recur", "false"));
			this.bottomUp = Boolean.parseBoolean(p.getProperty("bottomUp", "false"));
			
			String search = p.getProperty("search", "beam");
			
			if(search.equalsIgnoreCase("beam"))
				this.searchMethod = SearchMethod.BEAM;
			else if(search.equalsIgnoreCase("dfs")) {
				this.searchMethod = SearchMethod.DFS;
			} else if(search.equalsIgnoreCase("dijkstra")) {
				this.searchMethod = SearchMethod.DIJKSTRA;
			} else if(search.equalsIgnoreCase("greedybeam")) {
				this.searchMethod = SearchMethod.GREEDY_BEAM;
			} else {
				throw new Error(search);
			}
			
			this.beamSearchLimits = Integer.parseInt(p.getProperty("beamSearchLimits", "100"));
			this.beamSearchResultLimits = Integer.parseInt(p.getProperty("beamSearchResultLimits", "" + beamSearchLimits));
			
			assert beamSearchLimits >= beamSearchResultLimits;
			
			this.dijkstraSearchLimits = Integer.parseInt(p.getProperty("dijkstraSearchLimits", "10"));
			this.greedyBeamSearchLimits = Integer.parseInt(p.getProperty("greedyBeamSearchLimits", "400"));
			
			this.predResLimits = Integer.parseInt(p.getProperty("predResLimits", "20"));
			this.typeConstraint = Boolean.parseBoolean(p.getProperty("typeConstraint", "true"));
			this.compilationFilter = Boolean.parseBoolean(p.getProperty("compilationFilter", "false"));
			
			this.treeProbLimit = Double.parseDouble(p.getProperty("treeProbLimit", "0.05"));
			
			this.exprProbLimit = Double.parseDouble(p.getProperty("exprProbLimit", "0.0"));
			this.varProbLimit = Double.parseDouble(p.getProperty("varProbLimit", "0.0"));
			this.rnProbLimit = Double.parseDouble(p.getProperty("rnProbLimit", "0.0"));
			
			this.treeDepth = Integer.parseInt(p.getProperty("treeDepth", "5"));
			
			this.exprLimit = Integer.parseInt(p.getProperty("exprLimit", "200"));
			this.exprVarLimit = Integer.parseInt(p.getProperty("exprVarLimit", "4"));
			this.varLimit = Integer.parseInt(p.getProperty("varLimit", "5"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadPythonConfigFile() {
		String pythonCfgFilePath = ClassLoader.getSystemResource("").getPath() + ConditionConfig.PYTHON_CONFIG_FILE;
		List<String> list = FileUtil.readFileToStringList(pythonCfgFilePath);
		for(String str: list) {
			str = str.trim();
			if(str.startsWith("algorithm")) {
				learningAlgorithm  = str.split("=")[1].trim().toUpperCase();
			}
			
		}
		
		if(learningAlgorithm == null) {
			System.out.println("EMPTY LEARNING ALG, SET TO XGBOOST");
			learningAlgorithm = "XGB";
		}
	}
	
	private ConditionConfig() {
		loadConditionConfigFile();
		loadPythonConfigFile();
	}

	public static String getConfigFile() {
		return CONFIG_FILE;
	}
	
	public boolean isUseSocket() {
		return useSocket;
	}

	public boolean isUseConcrete() {
		return useConcrete;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public boolean isOpt() {
		return opt;
	}
	
	public boolean isPredAll() {
		return predAll;
	}
	
	public boolean isPredAllPreparing() {
		return predAllPreparing;
	}
	
	public boolean isRecur() {
		return recur;
	}

	public boolean isBottomUp() {
		return bottomUp;
	}

	public SearchMethod getSearchMethod() {
		return searchMethod;
	}

	public int getBeamSearchLimits() {
		return beamSearchLimits;
	}

	public int getBeamSearchResultLimits() {
		return beamSearchResultLimits;
	}
	
	public int getPredResLimits() {
		return predResLimits;
	}

	public boolean isTypeConstraint() {
		return typeConstraint;
	}

	public boolean isCompilationFilter() {
		return compilationFilter;
	}
	
	public int getTreeDepth() {
		return treeDepth;
	}

	public int getExprLimit() {
		return exprLimit;
	}

	public int getExprVarLimit() {
		return exprVarLimit;
	}

	public int getVarLimit() {
		return varLimit;
	}

	public double getTreeProbLimit() {
		return treeProbLimit;
	}

	public double getExprProbLimit() {
		return exprProbLimit;
	}

	public double getVarProbLimit() {
		return varProbLimit;
	}

	public double getRnProbLimit() {
		return rnProbLimit;
	}

	public int getDijkstraSearchLimits() {
		return dijkstraSearchLimits;
	}

	public int getGreedyBeamSearchLimits() {
		return greedyBeamSearchLimits;
	}
	
	public PROCESSING_TYPE getProcessType() {
		return processType;
	}
	
	public String getLearningAlgorithm() {
		return this.learningAlgorithm;
	}

	public String dumpOrder() {
		StringBuffer sb = new StringBuffer();
		if(recur) {
			if(bottomUp) {
				sb.append("RECUR_BU");
			} else {
				sb.append("RECUR");
			}
		} else {
			if(bottomUp) {
				sb.append("BOTTOM_UP");
			} else {
				sb.append("TOP_DOWN");
			}
		}
		return sb.toString();
	}
	
	public String getDumpStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(learningAlgorithm + " ");
		sb.append(dumpOrder());
		sb.append('\n');
		
		if(searchMethod == SearchMethod.BEAM) {
			sb.append("BEAM: RES=");
			sb.append(beamSearchResultLimits);
			sb.append(", K=");
			sb.append(beamSearchLimits);
		} else if(searchMethod == SearchMethod.DIJKSTRA) {
			sb.append("DIJKSTRA: TOP N=");
			sb.append(dijkstraSearchLimits);
		} else if (searchMethod == SearchMethod.GREEDY_BEAM) {
			sb.append("GREEDY_BEAM");
			//sb.append(greedyBeamSearchLimits);
		}
		sb.append('\n');
		
		sb.append("TYPE CONSTRAINTS: ");
		if(typeConstraint) {
			sb.append("YES\n");
		} else {
			sb.append("NO\n");
		}
		sb.append("COMPILER FILTER: ");
		if(compilationFilter) {
			sb.append("YES\n");
		} else {
			sb.append("NO\n");
		}
		
		if(recur) {
			sb.append("TREE HEIGHT LIMIT: " + treeDepth + "\n");
			if(rnProbLimit != 0.0) {
				sb.append("RECUR_NODE PORB LIMIT: " + rnProbLimit + "\n");
			}
		}
		if(exprProbLimit != 0.0) {
			sb.append("EXPR PORB LIMIT: " + exprProbLimit + "\n");
		}
		if(varProbLimit != 0.0) {
			sb.append("VAR PORB LIMIT: " + varProbLimit + "\n");
		}
		
		return sb.toString();
	}
	
}
