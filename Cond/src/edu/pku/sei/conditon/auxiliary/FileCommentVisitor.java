package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;

import edu.pku.sei.conditon.dedu.AbstractDeduVisitor;
import edu.pku.sei.conditon.dedu.feature.VariableFeature;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.StringUtil;

public class FileCommentVisitor extends ASTVisitor{
	
	private int startPosition = -1;

	private List<String> constructorSummarys = new ArrayList<>();
	private String currSummarys;
	
	private List<String> constructorAllThrowsMsgs = new ArrayList<>();
	private List<String> currAllThrowsMsgs = new ArrayList<>();
	
	private List<String> codeComments;
	
	private static boolean matchComment(String val, String comment) {
		String reg = "\\b" + val + "\\b";
		Pattern pattern = Pattern.compile(reg); 
		Matcher matcher = pattern.matcher(comment);
		return matcher.find();
	}
	
	private static boolean hitCodeComments(String var, List<String> comments) {
		for(String line: comments) {
			for(String word : StringUtil.devideWords(line)) {
				if(var.equalsIgnoreCase(word)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * the only entry of this class 
	 * @param cu
	 * @param startPos
	 * @param allVariables
	 */
	public static void getFileCommentMsg(CompilationUnit cu,  int startPos, List<VariableInfo> allVariables) {
		FileCommentVisitor visitor = new FileCommentVisitor(startPos);
		cu.accept(visitor);
		
		Map<VariableInfo, String> camelDivied = getAllVarCamelDivied(allVariables);
		
		for(VariableInfo info : allVariables) {
			VariableFeature varFea = info.getVariableFeature();
			String lowerName = info.getNameLiteral().toLowerCase();
			
			varFea.setInDocCode(hitCodeComments(lowerName, visitor.codeComments));
			
			if(varFea.isField()) {
				for(String msg : visitor.constructorAllThrowsMsgs) {
					if(msg.contains(lowerName) || msg.contains(camelDivied.get(info))) {
						analysisThrowTag(varFea, msg);
					}
				}
			} else {
				
				for(String msg : visitor.currAllThrowsMsgs) {
					if(msg.contains(lowerName) || msg.contains(camelDivied.get(info))) {
						analysisThrowTag(varFea, msg);
					}
				}
			}
		}
	}
		
	private static void analysisThrowTag(VariableFeature varFea, String throwsMsg) {
		//throwsMsg = throwsMsg.replaceAll("\n", ""); 
		String exceName = throwsMsg.trim().split(" ")[2];
		//assert exceName.endsWith("exception"): exceName;
		varFea.setDocExcpiton(exceName);
		
		throwsMsg = throwsMsg.replaceAll("<code>", "");
		throwsMsg = throwsMsg.replaceAll("</code>", "");
		throwsMsg = throwsMsg.replaceAll("<p>", "");
		throwsMsg = throwsMsg.replaceAll("</p>", "");
		throwsMsg = throwsMsg.replaceAll("<i>", "");
		throwsMsg = throwsMsg.replaceAll("</i>", "");
		throwsMsg = throwsMsg.replaceAll("<pre>", "");
		throwsMsg = throwsMsg.replaceAll("</pre>", "");
		
		boolean zero = matchComment("0", throwsMsg) || matchComment("zero", throwsMsg) || matchComment("positive", throwsMsg) || matchComment("negtive", throwsMsg);
		varFea.setDocZero(zero);
		
		boolean one = matchComment("1", throwsMsg) || matchComment("one", throwsMsg);
		varFea.setDocOne(one);

		varFea.setDocNullness(matchComment("null", throwsMsg));
		varFea.setDocRange(matchComment("range", throwsMsg));
		
		String op = AbstractDeduVisitor.NONE;
		if(throwsMsg.contains(">=")) {
			op = ">=";
		}else if(throwsMsg.contains("<=")) {
			op = "<=";
		}else if(throwsMsg.contains(">")) {
			op = ">";
		}else if(throwsMsg.contains("<")) {
			op = "<";
		}
		varFea.setDocOpeartor(op);
	}
	
	private static Map<VariableInfo, String> getAllVarCamelDivied(List<VariableInfo> allVariables){
		Map<VariableInfo, String> camelDivied = new HashMap<>();
		for(VariableInfo info: allVariables) {
			String camelStr = StringUtil.getCamelDividedStr(info.getNameLiteral());
			camelDivied.put(info, camelStr);
		}
		return camelDivied;
	}
	
	private FileCommentVisitor(int start) {
		this.startPosition = start;
	}	
		
	@Override
	public boolean visit(MethodDeclaration node) {
		boolean isCons = node.isConstructor();
		if(!isCons && node.getStartPosition() != this.startPosition) {
			return false;
		}
		
		if(node.getStartPosition() == this.startPosition) {
			CommentCollectorVisitor codeCollector = new CommentCollectorVisitor();
			node.accept(codeCollector);
			this.codeComments = codeCollector.getCodeComments();
		}
		
		DocVisitor docVisitor = new DocVisitor();
		node.accept(docVisitor);
		
		if(node.getStartPosition() == this.startPosition) {
			currSummarys = docVisitor.summary;
			currAllThrowsMsgs.addAll(docVisitor.throwsMsg);
			
		}else if (isCons){
			constructorSummarys.add(docVisitor.summary);
			constructorAllThrowsMsgs.addAll(docVisitor.throwsMsg);
		}
		return super.visit(node);
	}
	
	private class DocVisitor extends ASTVisitor{
		/*lower case*/
		List<String> throwsMsg = new ArrayList<>();
		/*lower case*/
		String summary;
		@Override
		public boolean visit(Javadoc node) {
			List<TagElement> tagList = node.tags();
			StringBuffer sb = new StringBuffer();
			for(TagElement tag : tagList){
				String tagName = tag.getTagName();
				if(tagName == null) {//summary
					sb.append(tag.toString());
				} else if (tagName.equals("@throws") || tagName.equals("@exception")) {
					throwsMsg.add(tag.toString().toLowerCase());
				}
			}
			summary = sb.toString().toLowerCase();
			return super.visit(node);
		}
	}
}
