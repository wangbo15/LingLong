package cn.edu.pku.sei.plde.hanabi.build.testrunner;

public class StackTraceItem {
	private String fullMethod;
	private String file;
	private int line = -1;
	
	public String getFullMethod() {
		return fullMethod;
	}
	
	public String getFile() {
		return file;
	}
	
	public int getLine() {
		return line;
	}

	public StackTraceItem(String fullMethod, String file, int line) {
		super();
		this.fullMethod = fullMethod;
		this.file = file;
		this.line = line;
	}
	
	public StackTraceItem(String fullMethod, String file) {
		super();
		this.fullMethod = fullMethod;
		this.file = file;
	}

	@Override
	public String toString() {
		if(getLine() == -1){
			return this.getFullMethod() + "(" + this.getFile() + ")";
		}else{
			return this.getFullMethod() + "(" + this.getFile() + ":" + this.getLine() + ")";
		}
	}
	
	
}
