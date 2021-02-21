package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

public class CommentCollectorVisitor extends ASTVisitor{

	private List<String> codes = new ArrayList<>();
	
	public List<String> getCodeComments(){
		return this.codes;
	}
	
	@Override
	public boolean visit(Javadoc node) {
		for(Object o1 : node.tags()){
			TagElement  tag = (TagElement) o1;
			if(tag.getTagName() == null){
				for(Object o2: tag.fragments()){
					putInCodes(codes, o2.toString());
				}
			}
		}
		
		return super.visit(node);
	}
	
	public void putInCodes(List<String> codes, String line) {
		String start = "<code>";
		String end = "</code>";
		int curr = 0;
		do{
			int s = line.indexOf(start, curr);
			if(s < 0){
				break;
			}
			int e = line.indexOf(end, curr);
			if(e < 0){
				break;
			}
			if(e < s){
				e = line.indexOf(end, curr + s);
			}
			if(e < 0){
				break;
			}
			codes.add(line.substring(s + start.length(), e).toLowerCase());
			curr += e + end.length();
		}while(true);
	}
	
//	public static void main(String[] args){
//		CommentCollectorVisitor commentCollectorVisitor = new CommentCollectorVisitor();
//		List<String> list = new ArrayList<>();
//		String com = "* function at <code>a</code> and <code>b</code> and keeps moving";
//		commentCollectorVisitor.putInCodes(list, com);
//		
//		com = "  * <li> <code> f(a) * f(b) <= 0 </code> </li>";
//		commentCollectorVisitor.putInCodes(list, com);
//
//		System.out.println(list);
//	}
}
