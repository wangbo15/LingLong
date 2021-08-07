package instrument;

public class Instrumentee {
	
	public boolean isOddNumber(int a) {
		int tmp = (a - 1) % 2;
		if (tmp != 0) {
            return true;
        }
        return false;
	}
		
}
