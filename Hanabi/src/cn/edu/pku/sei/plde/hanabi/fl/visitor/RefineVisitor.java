package cn.edu.pku.sei.plde.hanabi.fl.visitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class RefineVisitor extends ASTVisitor{
	private String className;
	private List<Suspect> suspects;
	private CompilationUnit cu;
	
	public RefineVisitor(String className, CompilationUnit cu, List<Suspect> suspects){
		this.className = className;
		this.cu = cu;
		this.suspects = suspects;
	}
	
	List<Suspect> getSuspects() {
		return suspects;
	}
	
	/**
	 * remove suspects out of method block
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		List<Suspect> toBeRemoved = new ArrayList<>();
		for(Suspect sus : suspects){
			if(lineHitTheNode(className, cu, node, sus)) {
				toBeRemoved.add(sus);
			}
		}
		suspects.removeAll(toBeRemoved);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		
		class ResveredStmtVisitor extends ASTVisitor{
			List<Integer> reservedLines = new ArrayList<>();
			@Override
			public boolean visit(IfStatement ifStmt) {
				int exprLine = cu.getLineNumber(ifStmt.getExpression().getStartPosition());
				reservedLines.add(exprLine);
				//int endLine = cu.getLineNumber(ifStmt.getStartPosition() + ifStmt.getLength()) + 1;
				//ifCondLines.add(endLine);
				return super.visit(ifStmt);
			}
			
			@Override
			public boolean visit(ReturnStatement retStmt) {
				int resvered = cu.getLineNumber(retStmt.getStartPosition());
				reservedLines.add(resvered);
				return super.visit(retStmt);
			}

			@Override
			public boolean visit(VariableDeclarationStatement varDeclStmt) {
				int resvered = cu.getLineNumber(varDeclStmt.getStartPosition());
				reservedLines.add(resvered);
				return super.visit(varDeclStmt);
			}
							
		};
		
		ResveredStmtVisitor resveredVisitor = new ResveredStmtVisitor();
		node.accept(resveredVisitor);
		
		TreeMap<Double, TreeSet<Suspect>> sorted = new TreeMap<>();
		for(Suspect sus : suspects){
			if(lineHitTheNode(className, cu, node, sus)){
				Double score = Double.valueOf(sus.getScore());
				TreeSet<Suspect> sameScore;
				if(sorted.containsKey(score)){
					sameScore = sorted.get(score);
				}else{
					sameScore = new TreeSet<>(new Comparator<Suspect>(){
						@Override
						public int compare(Suspect o1, Suspect o2) {
							return o2.getLine() - o1.getLine(); //TODO::
						}
					});
					sorted.put(score, sameScore);
				}
				sameScore.add(sus);
			}
		}
		
		if(sorted.size() == 0){
			return false;
		}
		List<Suspect> toBeRemoved = new ArrayList<>();
		for(Entry<Double, TreeSet<Suspect>> entry: sorted.entrySet()){
			TreeSet<Suspect> sameScore = entry.getValue();
			Suspect last = sameScore.last();
			Suspect first = sameScore.first();
			for(Suspect sus: sameScore){
				int line = sus.getLine();
				boolean inIfCond = resveredVisitor.reservedLines.contains(line);
				if(sus != last && sus != first && !inIfCond){
					toBeRemoved.add(sus);
				}
			}
		}
		suspects.removeAll(toBeRemoved);
		return false;
	}
	
	private static boolean lineHitTheNode(String className, CompilationUnit cu, ASTNode node, Suspect sus){
		int start = cu.getLineNumber(node.getStartPosition());
		int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
		int line = sus.getLine();
		
		return className.equals(sus.getClassName()) && line >= start && line <= end;
	}
	
}