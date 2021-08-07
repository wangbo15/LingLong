package cn.edu.pku.sei.plde.hanabi.test;

public class Ret {
	public static boolean equals(double x, double y) {
		return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
	}
}