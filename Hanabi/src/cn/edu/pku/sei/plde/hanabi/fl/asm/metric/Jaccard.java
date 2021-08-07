package cn.edu.pku.sei.plde.hanabi.fl.asm.metric;

public class Jaccard implements Metric {
	
	private static Jaccard instance;
	
	private Jaccard() {}
	
	public static Jaccard getOchiaiInstance() {
		if(instance == null) {
			synchronized(Ochiai.class) {
				instance = new Jaccard();
			}
		}
		return instance;
	}
	
    @Override
    public double value(int ef, int ep, int nf, int np) {
        // ef / float(ef + ep + nf)
        return ef / ((double) (ef + ep + nf));
    }
}