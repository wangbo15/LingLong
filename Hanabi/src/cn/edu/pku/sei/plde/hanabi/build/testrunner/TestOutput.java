package cn.edu.pku.sei.plde.hanabi.build.testrunner;

import java.util.ArrayList;
import java.util.List;

public abstract class TestOutput {
	
	//something like "org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath781"
	protected String failTest;
	
	protected String failMessage;
	protected List<StackTraceItem> stackTrace = new ArrayList<>();;
	protected String failTestCls;
	protected String failTestSrcPath;
	
	protected String failTestMethod;
	protected int failAssertLine;
	
	
	public void setFailTest(String failTest) {
		this.failTest = failTest;
	}

	public void setFailMessage(String failMessage) {
		this.failMessage = failMessage;
	}

	public void setStackTrace(List<StackTraceItem> stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	/**
	 * @return something like "org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath781", IMPORTANT: it is not divided by '#'
	 */
	public String getFailTest() {
		return failTest;
	}
	
	public String getFailMessage() {
		return failMessage;
	}
	
	/**
	 * @return the thrown exception name within its fail message
	 */
	public String getExceptionName() {
		String msg = this.getFailMessage();
		if(msg == null) {
			return "";
		}
		int idx = msg.indexOf(':');
		if(idx == -1) {
			if(msg.startsWith("java.") && msg.endsWith("Exception")) {
				return msg;
			}else {
				return "";
			}
		}
		String exception = msg.substring(0, idx).trim();
		return exception;
	}
	
	public List<StackTraceItem> getStackTrace() {
		return stackTrace;
	}

	public void setFailTestCls(String failTestCls) {
		this.failTestCls = failTestCls;
	}

	public void setFailTestMethod(String failTestMethod) {
		this.failTestMethod = failTestMethod;
	}

	public void setFailAssertLine(int failAssertLine) {
		this.failAssertLine = failAssertLine;
	}

	public String getFailTestCls() {
		return failTestCls;
	}

	public String getFailTestMethod() {
		return failTestMethod;
	}

	public int getFailAssertLine() {
		return failAssertLine;
	}

	public String getFailTestSrcPath() {
		return failTestSrcPath;
	}

	public void setFailTestSrcPath(String failTestSrcPath) {
		this.failTestSrcPath = failTestSrcPath;
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("FAIL TEST: " + this.failTest + "\n");
		res.append("FAIL MESSAGE: " + this.failMessage + "\n");
		res.append("FAIL ASSERT LINE: " + this.failAssertLine + "\n");
		res.append("STACK TRACE:\n");
		for(StackTraceItem item: this.stackTrace){
			res.append("\t" + item.toString() + "\n");
		}
		return res.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + failAssertLine;
		result = prime * result + ((failMessage == null) ? 0 : failMessage.hashCode());
		result = prime * result + ((failTest == null) ? 0 : failTest.hashCode());
		result = prime * result + ((failTestCls == null) ? 0 : failTestCls.hashCode());
		result = prime * result + ((failTestMethod == null) ? 0 : failTestMethod.hashCode());
		result = prime * result + ((failTestSrcPath == null) ? 0 : failTestSrcPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestOutput other = (TestOutput) obj;
		if (failAssertLine != other.failAssertLine)
			return false;
		if (failMessage == null) {
			if (other.failMessage != null)
				return false;
		} else if (!failMessage.equals(other.failMessage))
			return false;
		if (failTest == null) {
			if (other.failTest != null)
				return false;
		} else if (!failTest.equals(other.failTest))
			return false;
		if (failTestCls == null) {
			if (other.failTestCls != null)
				return false;
		} else if (!failTestCls.equals(other.failTestCls))
			return false;
		if (failTestMethod == null) {
			if (other.failTestMethod != null)
				return false;
		} else if (!failTestMethod.equals(other.failTestMethod))
			return false;
		if (failTestSrcPath == null) {
			if (other.failTestSrcPath != null)
				return false;
		} else if (!failTestSrcPath.equals(other.failTestSrcPath))
			return false;
		return true;
	}
	
	public static boolean contains(List<TestOutput> list, TestOutput currentOutput) {
		assert list != null && list.isEmpty() == false : "CANNOT BE NULL LIST";
		
		for(TestOutput testout: list) {
			if(testout.getFailTest().equals(currentOutput.getFailTest()) && testout.getFailAssertLine() == currentOutput.getFailAssertLine()) {
				return true;
			}
		}
		return false;
	}
	
}
