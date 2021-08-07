package cn.edu.pku.sei.plde.hanabi.build.proj;

public final class ProjectConfigFactory {

	public enum BugType {
		/** defects4j type*/
		D4J_TYPE,
		
		/** bugs.jar root type*/
		BDJ_TYPE,
	}
	
	
	/**
	 * @param configType
	 * @param projectName: such as 'math_3' and 'accumulo_151'
	 * @param root
	 * @return
	 */
	public static ProjectConfig createPorjectConfig(BugType configType, String projectName, String root) {
		initEnv();
		
		switch (configType) {
		case D4J_TYPE: {
			return new D4jProjectConfig(projectName, root);
		}
		case BDJ_TYPE:{
			return new BugsDotJarProjectConfig(projectName, root);
		}
		
		default:
			throw new IllegalArgumentException("UNSUPPORTED TYPE: " + configType);
		}

	}
	
	public static ProjectConfig createPorjectConfig(BugType configType, String projectName, String root, boolean purify) {
		switch (configType) {
		case D4J_TYPE: {
			return new D4jProjectConfig(projectName, root, purify);
		}
		default:
			throw new IllegalArgumentException("UNSUPPORTED TYPE: " + configType);
		}

	}
	
	private static void initEnv() {
		// set the property for log4j: ${log.base}
		System.setProperty("log.base", System.getProperty("user.dir"));
	}
}
