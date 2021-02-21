package edu.pku.sei.conditon.dedu.pf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import edu.pku.sei.conditon.dedu.pred.TreePredItem;

public class Path implements Comparable<Path>{
	
	public static final DecimalFormat decimalFormat =new DecimalFormat("0.00000000");  
	
	private final List<ProgramPoint> points = new ArrayList<>(); 
	
	private boolean complete = false;
	
	private double score = 0.0D;
	
	public void append(ProgramPoint point) {
		this.points.add(point);
		this.score = point.getScore();
	}

	public double getScore() {
		return this.score;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------ PATH \n");
		for(ProgramPoint p : points) {
			sb.append(p.hashCode());
			sb.append('[');
			String str = decimalFormat.format(p.getScore());
			sb.append(str);
			sb.append(']');
			sb.append("->");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append('\n');
		//sb.append("TOTAL SCORE: " + score.toEngineeringString() + "\n");
		ProgramPoint p = points.get(points.size() - 1);
		sb.append(">>>> FINAL POINT: \n");
		sb.append(p.toString());
		sb.append('\n');
		return sb.toString();
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public ProgramPoint getLast() {
		if(points.isEmpty())
			return null;
		int idx = points.size() - 1;
		return points.get(idx);
	}
	
	public List<ProgramPoint> getPoints() {
		return points;
	}

	@Override
	public int compareTo(Path obj) {
		Double d0 = Double.valueOf(this.score);
		Double d1 = Double.valueOf(obj.score);
		return d0.compareTo(d1);
	}

	public Path deepClone() {
		Path res = new Path();
		for(ProgramPoint p : this.points) {
			res.append(p);
		}
		res.complete = this.complete;
		return res;
	}
	
	public static List<String> getResultLines(TreeSet<Path> paths){
		List<String> result = new ArrayList<>();
		for(Path p : paths) {
			TreePredItem root = p.getLast().getAstRoot();
			String literal = root.fullyComplementedLiteral();
			// TODO: omit illeagal literals
			// assert literal.contains("#");
			
			String scoreStr = decimalFormat.format(p.getScore());
			String line = literal + "\t" + scoreStr;
			result.add(line);
			//System.out.println(line);
		}
		return result;
	}
	
	
}
