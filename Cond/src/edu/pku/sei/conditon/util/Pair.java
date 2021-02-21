/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package edu.pku.sei.conditon.util;

/**
 * User defined struct, which saves the information appear together
 * 
 * @author Jiajun
 *
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> {

	protected A _first;
	protected B _second;

	public Pair() {
	}

	public Pair(A first, B second) {
		_first = first;
		_second = second;
	}

	public void setFirst(A first) {
		_first = first;
	}

	public void setSecond(B second) {
		_second = second;
	}

	public A getFirst() {
		return _first;
	}

	public B getSecond() {
		return _second;
	}
	@Override
	public String toString() {
		return "<" + _first + ", " + _second + ">";
	}
	
}