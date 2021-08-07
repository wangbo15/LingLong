package cn.edu.pku.sei.plde.hanabi.fl.asm.metric;
/**
 * Singleton object for FL
 */
public class Ochiai implements Metric {
	
	private static Ochiai instance;
	
	private Ochiai() {}
	
	public static Ochiai getOchiaiInstance() {
		if(instance == null) {
			synchronized(Ochiai.class) {
				instance = new Ochiai();
			}
		}
		return instance;
	}
	
	/**
	 * ef: covered failed test number
	 * ep: covered passed test number
	 * nf: uncovered failed test number
	 * (ef + nf) = the total number of failed test
	 */
	@Override
	public double value(int ef, int ep, int nf, int np) {
		return ef / Math.sqrt((ef + nf) * (ef + ep));
	}
}