package cn.edu.pku.sei.plde.hanabi.fl.constructor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Pair;
import edu.pku.sei.conditon.util.JavaFile;

public class InvocationCollector extends ASTVisitor{
	
	public static final String UNKNOWN = "UNKNOWN";
	
	private String failedTestCls;
	private String failedTestMtd;
	
	private String failedCls;
	
	private String tobeTestedCls;
	
	List<ContructorItem> invokedConstrutors = new ArrayList<>();
	
	public InvocationCollector(String failedTestCls, String failedTestMtd) {
		super();
		this.failedTestCls = failedTestCls;
		this.failedTestMtd = failedTestMtd;
		
		failedCls = removeTestPostFix(failedTestCls);
		int last = failedCls.lastIndexOf(".");
		if(last > 0) {
			tobeTestedCls = failedCls.substring(last + 1);
		}else{
			tobeTestedCls = failedCls;
		}
	}

	public String getFailedTestCls() {
		return failedTestCls;
	}

	public String getFailedTestMtd() {
		return failedTestMtd;
	}

	public String getTobeTestedCls() {
		return tobeTestedCls;
	}
	
	public static String getTestedClsShortNameFromTest(String testfullName) {
		String removeTest = removeTestPostFix(testfullName);
		String testedSrc;
		int last = removeTest.lastIndexOf(".");
		if(last > 0) {
			testedSrc = removeTest.substring(last + 1);
		}else{
			testedSrc = removeTest;
		}
		return testedSrc;
	}
	
	private static String removeTestPostFix(String testClassName) {
		if(testClassName.endsWith("Test")) {
			int tail = testClassName.length() - 4;
			return testClassName.substring(0, tail);
		}
		return testClassName;
	}
	
	private static String getPkgName(String testClassName) {
		int idx = testClassName.lastIndexOf('.');
		if(idx > 0) {
			return testClassName.substring(0, idx);
		}
		return testClassName;
	}
	
	private static File getSourceFile(String root, String classFullName) {
		String srcPath = FileUtil.getFileAddressOfJava(root, classFullName);
		File file = new File(srcPath);
		if(file.exists() == false) {
			return null;
		}
		return file;
	}

	public static Pair<String, List<Integer>> getInvokedConstructorStmts(ProjectConfig projConfig, String testClsName, String functionName) {
		
		File testClsFile = getSourceFile(projConfig.getTestSrcRoot().getAbsolutePath(), testClsName);
		
		if(testClsFile == null || !testClsFile.exists()) {
			return null;
		}
		
		//load cu and visit
		final CompilationUnit testCu = JavaFile.genCompilationUnit(testClsFile, projConfig.getTestJdkLevel(), projConfig.getSrcRoot().getAbsolutePath());
		
        InvocationCollector testVisitor = new InvocationCollector(testClsName, functionName);
        testCu.accept(testVisitor);
        
        Set<String> tobeAnalysisSrc = new HashSet<>();
        tobeAnalysisSrc.add(removeTestPostFix(testClsName));
        for(ContructorItem item: testVisitor.invokedConstrutors) {
        	String childCls = getPkgName(testClsName) + "." + item.getTypeName();
        	tobeAnalysisSrc.add(childCls);
        }
        
        for(String srcFullName : tobeAnalysisSrc) {
        	File testedSrcFile = getSourceFile(projConfig.getSrcRoot().getAbsolutePath(), srcFullName);
        	if(testedSrcFile == null) {
        		continue;
        	}
        	
            final CompilationUnit srcCu = JavaFile.genCompilationUnit(testedSrcFile, projConfig.getTestJdkLevel(), projConfig.getSrcRoot().getAbsolutePath());
    		SrcConstructorVisitor srcVisitor = new SrcConstructorVisitor(srcCu, testVisitor.invokedConstrutors);
    		srcCu.accept(srcVisitor);
    		if(srcVisitor.isHitConstructor()) {
        		Pair<String, List<Integer>> res = Pair.from(srcFullName, srcVisitor.suspicousLines);
        		return res;
    		}
        }
        return null;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(!node.getName().getIdentifier().equals(this.failedTestMtd)) {
			return false;
		}
		assert this.invokedConstrutors.isEmpty();
		
		InstanceCreationVisitor visitor = new InstanceCreationVisitor();
		node.accept(visitor);
		
		if(this.invokedConstrutors.isEmpty()) {
			return super.visit(node);
		}
		
		return super.visit(node);
	}
	
	private static String parseArgType(ASTNode arg) {
		String currType;
		if(arg instanceof PrefixExpression) {
			arg = ((PrefixExpression) arg).getOperand();
		}else if(arg instanceof PostfixExpression) {
			arg = ((PostfixExpression) arg).getOperand();
		}
		if(arg instanceof SimpleName) {
			SimpleName sm = (SimpleName) arg;
			IVariableBinding bd = (IVariableBinding) sm.resolveBinding();
			if(bd != null) {
				if(bd.getType() != null) {
					currType = bd.getType().toString();
				}else {
					currType = UNKNOWN;
				}
			}else {
				currType = UNKNOWN;
			}
		}else if(arg instanceof NumberLiteral){
			String num = arg.toString();
			if(num.endsWith("l") || num.endsWith("L")) {
				currType = "long";
			}else if(num.endsWith("d") || num.endsWith("D")) {
				currType = "double";
			}else if(num.endsWith("f") || num.endsWith("F")) {
				currType = "float";
			}else if(num.contains(".")) {
				currType = "double";
			}else {
				currType = "int";
			}
			
		}else if(arg instanceof NullLiteral) {
			currType = "Object";
		}else if(arg instanceof StringLiteral) {
			currType = "String";
		}else if(arg instanceof BooleanLiteral) {
			currType = "boolean";
		}else {
			currType = UNKNOWN;
		}
		return currType;
	}
	
	private class InstanceCreationVisitor extends ASTVisitor{
		@Override
		public boolean visit(ClassInstanceCreation node) {
			String invoked = node.getType().toString();
			if(!invoked.startsWith(tobeTestedCls)) {
				return super.visit(node);
			}
			
			int size = node.arguments().size();
			String[] types = new String[size];
			for(int i = 0; i < size; i++) {
				ASTNode arg = (ASTNode) node.arguments().get(i);
				String currType = parseArgType(arg);
				types[i] = currType;
			}
			ContructorItem item = new ContructorItem(invoked, size, types);
			invokedConstrutors.add(item);
			return super.visit(node);
		}

		@Override
		public boolean visit(MethodInvocation node) {
			
			
			
			return super.visit(node);
		}
		
		
		
	}

}


class SrcConstructorVisitor extends ASTVisitor{
	public List<Integer> suspicousLines = new ArrayList<>();
	
	private CompilationUnit cu;
	private List<ContructorItem> invokedConstrutors;
	private boolean hitConstructor = false;
	
	public SrcConstructorVisitor(CompilationUnit cu, List<ContructorItem> invokedConstrutors){
		this.cu = cu;
		this.invokedConstrutors = invokedConstrutors;
	}
	
	public boolean isHitConstructor() {
		return this.hitConstructor;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if(node.isConstructor() == false) {
			return false;
		}
		boolean hit = false;
		for(ContructorItem item: invokedConstrutors) {
			if(item.getArgNum() == node.parameters().size()) {
				//TODO: compare type
				hit = true;
			}
		}
		List<Object> statements = node.getBody().statements();
		if(hit && ! statements.isEmpty()) {
			Statement lastStmt =  (Statement) statements.get(statements.size() - 1);
			int line = -1;
//			if(lastStmt instanceof ConstructorInvocation || lastStmt instanceof SuperConstructorInvocation) {
//				line = cu.getLineNumber(lastStmt.getStartPosition()) + 1;
//			}else {
//				line = cu.getLineNumber(lastStmt.getStartPosition()) + 1;
//			}
			line = cu.getLineNumber(lastStmt.getStartPosition()) + 1;
			suspicousLines.add(line);
			
			this.hitConstructor = true;
		}
		return super.visit(node);
	}
}


class ContructorItem{
	private String typeName;
	private int argNum;
	private String[] types;
	
	public ContructorItem(String typeName, int argNum, String[] types) {
		this.typeName = typeName;
		this.argNum = argNum;
		this.types = types;
	}
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getArgNum() {
		return argNum;
	}

	public void setArgNum(int argNum) {
		this.argNum = argNum;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}
	
	@Override
	public String toString() {
		return "ContructorItem [typeName=" + typeName + ", argNum=" + argNum + ", types=" + Arrays.toString(types)
				+ "]";
	}
	
}
