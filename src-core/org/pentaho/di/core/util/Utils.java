package org.pentaho.di.core.util;

/* Levenshtein in Java, originally from Josh Drew's code at
 * http://joshdrew.com/
 * Code from http://blog.lolyco.com
 *
 */
public class Utils {
	private final static int[] ZERO_LENGTH_INT_ARRAY = new int[0];
	
	private static int damerauLevenshteinDistance(String s, String t, int[] workspace) {
		int lenS = s.length();
		int lenT = t.length();
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenT1 == 1)
			return lenS1 - 1;
		if (lenS1 == 1)
			return lenT1 - 1;
		int[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + 1;
				// insertion
				tmp = dl[dlIndex - 1] + 1;
				if (tmp < min)
					min = tmp;
				cost = 1;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				if (sIndex > 0 && tIndex > 0)
				{
					if (s.charAt(sIndex) == t.charAt(tPrevIndex) && s.charAt(sPrevIndex) == t.charAt(tIndex))
					{
						tmp = dl[rowBefore - tri] + cost;
						// transposition
						if (tmp < min)
							min = tmp;
					}
				}
				// substitution
				tmp = dl[rowBefore - 1] + cost;
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;
				tPrevIndex = tIndex;
			}
			sPrevIndex = sIndex;
		}
		return dl[dlIndex];
	}
	
	private static int[] getWorkspace(int sl, int tl) {
		return new int[(sl + 1) * (tl + 1)];
	}
	
	public static int getDamerauLevenshteinDistance(String s, String t) {
		if (s != null && t != null)
			return damerauLevenshteinDistance(s, t, getWorkspace(s.length(), t.length()));
		else
			return damerauLevenshteinDistance(s, t, ZERO_LENGTH_INT_ARRAY);
	}

}
