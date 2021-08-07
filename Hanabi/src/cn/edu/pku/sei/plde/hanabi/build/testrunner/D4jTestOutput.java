package cn.edu.pku.sei.plde.hanabi.build.testrunner;

public class D4jTestOutput extends TestOutput {
	

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer("D4jTestOutput :\n");
		res.append(super.toString());
		return res.toString();
	}
	
	public String getException(){
		assert this.failMessage != null;
		return this.failMessage.split(":")[1];
	}
	
	
}
