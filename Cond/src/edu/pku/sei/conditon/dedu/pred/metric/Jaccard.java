package edu.pku.sei.conditon.dedu.pred.metric;

public class Jaccard {
	
	public static double jaccard(String src, String tar) {
		char[] s = src.toCharArray();
		char[] t = tar.toCharArray();
		int intersection = 0;
		int union = s.length + t.length;
		boolean[] sdup = new boolean[s.length];
		union -= findDuplicates(s, sdup); // <co id="co_fuzzy_jaccard_dups1"/>
		boolean[] tdup = new boolean[t.length];
		union -= findDuplicates(t, tdup);
		for (int si = 0; si < s.length; si++) {
			if (!sdup[si]) { // <co id="co_fuzzy_jaccard_skip1"/>
				for (int ti = 0; ti < t.length; ti++) {
					if (!tdup[ti]) {
						if (s[si] == t[ti]) { // <co
												// id="co_fuzzy_jaccard_intersection"
												// />
							intersection++;
							break;
						}
					}
				}
			}
		}
		union -= intersection;
		return (double) intersection / union; // <co
												// id="co_fuzzy_jaccard_return"/>
	}

	private static int findDuplicates(char[] s, boolean[] sdup) {
		int ndup = 0;
		for (int si = 0; si < s.length; si++) {
			if (sdup[si]) {
				ndup++;
			} else {
				for (int si2 = si + 1; si2 < s.length; si2++) {
					if (!sdup[si2]) {
						sdup[si2] = s[si] == s[si2];
					}
				}
			}
		}
		return ndup;
	}
}
