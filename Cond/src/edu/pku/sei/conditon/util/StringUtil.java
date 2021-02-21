package edu.pku.sei.conditon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	
	public static int charOccurTimesInStr(String str, char ch) {
		assert str != null;
		int time = 0;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == ch) {
				time++;
			}
		}
		return time;
	}
	
	public static String getCamelDividedStr(String var) {
		List<String> list = camelDivision(var);
		StringBuffer sb = new StringBuffer();
		for(String item : list) {
			sb.append(item.toLowerCase());
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	
	public static List<String> camelDivision(String var){
		int i = 0;
		while(var.charAt(i) == '_'){
			i++;
		}
		var = var.substring(i); 
		
		List<String> result = new ArrayList<>();
        var=String.valueOf(var.charAt(0)).toUpperCase().concat(var.substring(1));
		Pattern pattern=Pattern.compile("[A-Z]([a-z\\d]+)?");
        Matcher matcher=pattern.matcher(var);
        while(matcher.find()){
            String word=matcher.group();
            result.add(word);
        }
        return result;

	}
	
	public static List<String> devideWords(String code){
		List<String> res = new ArrayList<>();
		for(String s : code.split("\\W")){
			if(s.trim().length() > 0){
				res.add(s);
			}
		}
		return res;
	}
	
    public static List<String> split(String chainedStrings, Character character) {
        return split(chainedStrings, String.format("[%c]", character));
    }

    public static List<String> split(String chainedStrings, String splittingRegex) {
        return Arrays.asList(chainedStrings.split(splittingRegex));
    }
    
    public static String lastAfterSplit(String string, Character character) {
        return lastAfterSplit(string, String.format("[%c]", character));
    }

    public static String lastAfterSplit(String string, String splittingRegex) {
        List<String> splitted = split(string, splittingRegex);
        if (!splitted.isEmpty()) {
            return splitted.get(splitted.size() - 1);
        }
        return string;
    }
	
	public static String stripEnd(String string, String suffix) {
        if (string.endsWith(suffix)) {
            return string.substring(0, string.length() - suffix.length());
        }
        return string;
    }
	
    public static String join(Collection<String> subStrings, Character connector) {
        return join(subStrings, "" + connector);
    }

    public static String join(Collection<String> subStrings, String connector) {
        StringBuilder joined = new StringBuilder();
        if (!subStrings.isEmpty()) {
            Iterator<String> iterator = subStrings.iterator();
            joined.append(iterator.next());
            while (iterator.hasNext()) {
                joined.append(connector + iterator.next());
            }
        }
        return joined.toString();
    }
    
    public static String connectMulty(String connector, String... args){
    	if(connector == null || args == null) {
    		return "";
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	for(String str: args){
    		sb.append(str);
    		sb.append(connector);
    	}
    	for(int i = connector.length(); i > 0; i--) {
        	sb.deleteCharAt(sb.length() - 1);
    	}
    	return sb.toString();
    }
    
    public static String connect(String s1, String s2, String connector){
		StringBuffer sb = new StringBuffer(s1);
		sb.append(connector);
		sb.append(s2);
		return sb.toString();
	}

	public static String connectAndNewLine(String s1, String s2, String connector){
		StringBuffer sb = new StringBuffer(s1);
		sb.append(connector);
		sb.append(s2);
		sb.append("\n");
		return sb.toString();
	}
    
	public static String formatNumeralVal(String oper, String predict){
		if(predict.contains(">>") || predict.contains("<<") || predict.endsWith("\'>\'") || predict.endsWith("\'<\'") ||
				predict.endsWith("\">\"") || predict.endsWith("\"<\"") || predict.endsWith("\">=\"") || predict.endsWith("\"<=\"")) {
			return predict;
		} 
		if(OperatorUtil.isComparing(oper)){
			String reg = "(\\<=|==|\\>=|\\>|\\<|\\!=)";
			String[] strs = predict.split(reg);
			assert strs.length == 2: predict;
			StringBuffer sb = new StringBuffer();
			sb.append(strs[0].trim());
			sb.append(" ");
			sb.append(oper);
			sb.append(" ");
			strs[1] = strs[1].trim();
			
			String newStr = TypeUtil.modifyUselessFormat(strs[1]);
			if(newStr.equals(strs[1]) == false){
				sb.append(newStr);
			}else{
				sb.append(strs[1]);
			}
			return sb.toString();
		}
		return predict;
	}
	
	public static String[] parseListString(String listLike) {
		if(listLike == null)
			return null;
		
		StringBuffer sb = new StringBuffer(listLike);
		//remove '[' and ']'
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString().split(",");
	}
	
	public static String[] parseTypeListString(String listLike) {
		if(listLike == null)
			return null;
		
		Stack<Character> stack = new Stack<>();
		List<String> types = new ArrayList<>(10);
		String contents = listLike.substring(1, listLike.length() - 1);
		int cursor = 0;
		int length = listLike.length() - 2;
		int start = 0;
		while(cursor < length) {
			char ch = contents.charAt(cursor);
			if(ch == '<') {
				stack.push('<');
			} else if(ch == '>') {
				if(stack.isEmpty() || stack.pop() != '<') {
					return new String[0];
				}
			} else if(ch == '[') {
				stack.push('[');
			} else if(ch == ']') {
				if(stack.isEmpty() || stack.pop() != '[') {
					return new String[0];
				}
			} else if(ch == ',') {
				if(stack.isEmpty()) {
					types.add(contents.substring(start, cursor).trim());
					start = cursor + 1;
				}
			}
			cursor ++;
		}
		types.add(contents.substring(start).trim());
		String[] result = new String[types.size()];
		result = types.toArray(result);
		return result;
	}
	
}
