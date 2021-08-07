package cn.edu.pku.sei.plde.hanabi.fl.asm;

import java.util.HashMap;
import java.util.Map;

public class SourceLocation {
	private String containingClassName;
	private String filePath;
	private String mtdKey;
	private int lineNumber;
	
    private SourceLocation(final String containingClassName, final int lineNumber) {
        this.containingClassName = containingClassName;
        this.lineNumber = lineNumber;
    }
    
    private static final Map<String, SourceLocation> cache = new HashMap<>(); 
    
    public static SourceLocation newSourceLocation(final String containingClassName, final int lineNumber) {
    	String key = containingClassName + "#" + lineNumber;
    	if(cache.containsKey(key)) {
    		return cache.get(key);
    	}else {
    		SourceLocation sl = new SourceLocation(containingClassName, lineNumber);
    		cache.put(key, sl);
    		return sl;
    	}
    	
    }
    
	public String getContainingClassName() {
		return containingClassName;
	}

	public void setContainingClassName(String containingClassName) {
		this.containingClassName = containingClassName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMtdKey() {
		return mtdKey;
	}

	public void setMtdKey(String mtdKey) {
		this.mtdKey = mtdKey;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
    @Override
    public String toString() {
        return String.format("%s#%d", containingClassName, lineNumber);
    }

    @Override
    public int hashCode() {
        int result = containingClassName != null ? containingClassName.hashCode() : 0;
        result = 31 * result + lineNumber;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceLocation that = (SourceLocation) o;

        if (lineNumber != that.lineNumber) return false;
        return !(containingClassName != null ? !containingClassName.equals(that.containingClassName) : that.containingClassName != null);
    }
}
