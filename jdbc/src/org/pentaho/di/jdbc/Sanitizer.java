package org.pentaho.di.jdbc;

import java.nio.CharBuffer;

public class Sanitizer {
	static char[] validChars = new char[] { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z' };

	public static String doFilter(String input) {
		if (input == null) {
			return "";
		}
		if (input.equals(" ")) {
			return "";
		}

		// String r = replace(input);
		String r = (input);
		return r;
	}

	/**
	 * replace the invalid codes.
	 * 
	 * @param input
	 * @return
	 */
	static String replace(String input) {
		char[] data = input.toCharArray();

		CharBuffer cb = CharBuffer.allocate(data.length);
		for (int i = 0; i < data.length; i++) {
			char d = data[i];
			int result = java.util.Arrays.binarySearch(validChars, d);
			// System.out.println(result);
			if (result > 0) {
				cb.append(d);
			}
		}
		String r = new String(cb.array());
		r = r.trim();
		return r;
	}

	/**
	 * @deprecated
	 * @param input
	 * @return
	 */
	static String replace2(String input) {
		char[] data = input.toCharArray();

		CharBuffer cb = CharBuffer.allocate(data.length);
		for (int i = 0; i < data.length; i++) {
			char d = data[i];
			for (int j = 0; j < validChars.length; j++) {
				if (d == validChars[j]) {
					cb.append(d);
					break;
				}
			}
		}
		String r = new String(cb.array());
		r = r.trim();
		return r;
	}

	/**
	 * @deprecated
	 * @param input
	 * @return
	 */
	public static String lowercase(String input) {

		StringBuilder buff = new StringBuilder(input);
		int index = buff.indexOf("SELECT");
		if (index != -1) {
			buff.replace(index, index + 6, "select");
		}

		index = buff.indexOf("FROM");
		if (index != -1) {
			buff.replace(index, index + 4, "from");
		}
		index = buff.indexOf("AS ");
		if (index != -1) {
			buff.replace(index, index + 3, "as ");
		}

		index = buff.indexOf("WHERE ");
		if (index != -1) {
			buff.replace(index, index + 6, "where ");
		}
		return buff.toString();
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static String lowercase2(String input) {
		char[] data = input.toCharArray();

		int max = data.length;
		for (int i = 0; i < max; i++) {
			char c = data[i];
			if (i != max - 1) {
				char c2 = data[i + 1];
				// lowercase SELECT
				if (i == 0) {
					if (c == 'S') {
						data[i] = 's';
					}
					if (c2 == 'E') {
						data[i + 1] = 'e';
					}
					if (data[i + 2] == 'L') {
						data[i + 2] = 'l';
					}
					if (data[i + 3] == 'E') {
						data[i + 3] = 'e';
					}
					if (data[i + 4] == 'C') {
						data[i + 4] = 'c';
					}
					if (data[i + 5] == 'T') {
						data[i + 5] = 't';
					}
					// skip to 6
					i = 6;
					continue;

				}
				// lowercase AS
				if (i > 7 && i < max - 3) {

					if (c == ' ' && c2 == 'A') {
						if (data[i + 2] == 'S' && data[i + 3] == ' ') {
							data[i + 1] = 'a';
							data[i + 2] = 's';
							i = i + 2;
							continue;
						}

					}
				}
				// lowercase FROM
				if (i > 7 && i < max - 5) {
					if (c == ' ' && c2 == 'F') {
						if (data[i + 2] == 'R' && data[i + 3] == 'O'
								&& data[i + 4] == 'M') {
							data[i + 1] = 'f';
							data[i + 2] = 'r';
							data[i + 3] = 'o';
							data[i + 4] = 'm';
							i = i + 4;
							continue;
						}

					}
				}

				// lowercase WHERE
				if (i > 7 && i < max - 5) {
					if (c == ' ' && c2 == 'W') {
						if (data[i + 2] == 'H' && data[i + 3] == 'E'
								&& data[i + 4] == 'R' && data[i + 5] == 'E') {
							data[i + 1] = 'w';
							data[i + 2] = 'h';
							data[i + 3] = 'e';
							data[i + 4] = 'r';
							data[i + 5] = 'e';
							
							
						}

					}
				}
				
				//lowercase AND
				if (i > 7 && i < max - 5) {
					if (c == ' ' && c2 == 'A') {
						if (data[i + 2] == 'N' && data[i + 3] == 'D'
								) {
							data[i + 1] = 'a';
							data[i + 2] = 'n';
							data[i + 3] = 'd';
							
							
							
						}

					}
				}
			}

		}

		String tmpStr = new String(data);

		return tmpStr;
	}

	static void doLoop(String input, int count) {
		for (int i = 0; i < count; i++) {
			Sanitizer.replace(input);
		}
	}

	static void doLoop2(String input, int count) {
		for (int i = 0; i < count; i++) {
			Sanitizer.replace2(input);
		}
	}

	static void doLoop3(String input, int count) {
		for (int i = 0; i < count; i++) {
			Sanitizer.lowercase(input);
		}
	}

	static void doLoop4(String input, int count) {
		for (int i = 0; i < count; i++) {
			Sanitizer.lowercase2(input);
		}
	}

	public static void main(String[] args) {
		String t = "SELECT schema1.a AS asbc,schema1.b AS efg,schema1.c AS hijk FROM schema1.for_pentaho WHERE schema1.a=1";
		System.out.println(Sanitizer.lowercase2(t));

		t = "%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123%d%^##>*+- 123";
		System.out.println(t);
		long start = System.currentTimeMillis();
		int m = 100;
		Sanitizer.doLoop(t, m);
		System.out
				.println("time costs:" + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		Sanitizer.doLoop2(t, m);
		System.out
				.println("time costs:" + (System.currentTimeMillis() - start));
		char[] c = new char[] { 'A', 'S', ' ' };
		System.out.println(c);

		t = "SELECT schema1.a AS asbc,schema1.b AS efg,schema1.c AS hijk,,schema1.d AS lmn,schema1.e AS opq FROM schema1.for_pentaho WHERE schema1.a=1";
		start = System.currentTimeMillis();

		int n = 10000;
		Sanitizer.doLoop3(t, n);
		long end = System.currentTimeMillis();
		long t1 = end - start;
		// System.out
		// .println("time costs:" + (end-start));

		start = System.currentTimeMillis();
		Sanitizer.doLoop4(t, n);
		end = System.currentTimeMillis();
		long t2 = end - start;
		// System.out
		// .println("time costs:" + (end - start));
		System.out.println("t2=" + t2 + ",t1=" + t1);
		System.out.println("time gap(t2-t1)=" + (t2 - t1));
	}
}
