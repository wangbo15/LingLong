
public class SwitchCase{
	
	public void foo(int method, double f0, double f1) {
        switch (method) {
        case 0:
            f0 *= 0.5;
            break;
        case 1:
            f0 *= f1 / (f1 + f1);
            break;
        case 2:
            break;
        case 3:
        	if(f0 == f1) {
        		return;
        	}
        default:
            throw new Exception();
        }
        
	}
	
	public void bar (int method, double f0, double f1, double fx, double x, double x0, double x1) {
        if (f1 * fx < 0) {
            // The value of x1 has switched to the other bound, thus inverting
            // the interval.
            x0 = x1;
            f0 = f1;
        } else {
            switch (method) {
            case 0:
                f0 *= 0.5;
                break;
            case 1:
                f0 *= f1 / (f1 + fx);
                break;
            case 2:
                // Nothing.
                if (x == x1) {
                    x0 = 0.5 * (x0 + x1 - FastMath.max(rtol * FastMath.abs(x1), atol));
                    f0 = computeObjectiveValue(x0);
                }
                break;
            default:
                // Should never happen.
                throw new MathInternalError();
            }
        }
	}
	
}