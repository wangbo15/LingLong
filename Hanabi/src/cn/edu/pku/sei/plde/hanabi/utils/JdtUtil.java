package cn.edu.pku.sei.plde.hanabi.utils;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class JdtUtil {
	
	private static ASTParser genASTParser(String src, String jdkVersion, int astType){
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(jdkVersion, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(src.toCharArray());
		astParser.setKind(astType);
		
		// useless
		astParser.setResolveBindings(false);
		return astParser;
	}
	
	public static ASTNode genASTFromSource(String src, String jdkVersion, int astType) {
		ASTParser astParser = genASTParser(src, jdkVersion, astType);
		return astParser.createAST(null);
	}
	
	public static MethodDeclaration getMtdByName(final String filePath, final String jdkVersion, final String name){
		final CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(filePath), jdkVersion, ASTParser.K_COMPILATION_UNIT);
		class MtdVisitor extends ASTVisitor{
			private MethodDeclaration hitMtd;
			
			public MethodDeclaration getHitMtd(){
				return hitMtd;
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				if(node.getName().getIdentifier().equals(name)){
					hitMtd = node;
				}
				return super.visit(node);
			}
			
		};
		MtdVisitor visitor = new MtdVisitor();
		cu.accept(visitor);
		return visitor.getHitMtd();
	}
	
	public static MethodDeclaration getMtdByLine(final String filePath, final String jdkVersion, final int line){
		final CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(filePath), jdkVersion, ASTParser.K_COMPILATION_UNIT);
		class MtdVisitor extends ASTVisitor{
			private MethodDeclaration hitMtd;
			
			public MethodDeclaration getHitMtd(){
				return hitMtd;
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				if(cu.getLineNumber(node.getStartPosition()) <= line && cu.getLineNumber(node.getStartPosition() + node.getLength()) > line){
					hitMtd = node;
				}
				return super.visit(node);
			}
			
		};
		MtdVisitor visitor = new MtdVisitor();
		cu.accept(visitor);
		return visitor.getHitMtd();
	}
	
	public static Statement genASTFromStmt(String src, String jdkVersion) throws IllegalArgumentException{
		ASTParser astParser = genASTParser(src, jdkVersion, ASTParser.K_STATEMENTS);
		Statement result = (Statement) astParser.createAST(null);
		if(result instanceof Block){
			return (Statement) ((Block) result).statements().get(0);
		}
		return result;
	}
	
	public static Expression genASTFromExpression(String src, String jdkVersion) throws IllegalArgumentException{
		ASTParser astParser = genASTParser(src, jdkVersion, ASTParser.K_EXPRESSION);
		Expression result = (Expression) astParser.createAST(null);
		return result;
	}
}
