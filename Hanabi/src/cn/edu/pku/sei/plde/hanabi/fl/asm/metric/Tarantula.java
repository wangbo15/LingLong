package cn.edu.pku.sei.plde.hanabi.fl.asm.metric;

public class Tarantula implements Metric {
	
	private static Tarantula instance;
	
	private Tarantula() {}
	
	public static Tarantula getOchiaiInstance() {
		if(instance == null) {
			synchronized(Ochiai.class) {
				instance = new Tarantula();
			}
		}
		return instance;
	}
	
	@Override
    public double value(int ef, int ep, int nf, int np) {
        // (ef/float(ef+nf + smooth))/float((ef/float(ef+nf + smooth))+(ep/float(ep+np + smooth)) + smooth)
        if(ef+nf == 0) {
            return 0;
        }
        return (ef / ((double) (ef + nf))) / ((ef / ((double)(ef - nf))) + (ep/ ((double) ep + np))) ;
    }
}
