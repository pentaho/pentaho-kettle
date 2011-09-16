/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 * Copyright (C) 2004 The jTDS Project
 */
package org.pentaho.di.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SQLParser {
	/** Original SQL string */
	private String sql;
	/** Input buffer with SQL statement. */
	private char[] in;
	/** Current position in input buffer. */
	private int s;
	/** Length of input buffer. */
	private int len;
	/** Output buffer to contain parsed SQL. */
	private char[] out;
	/** Current position in output buffer. */
	private int d;
	/**
	 * Parameter list to be populated or <code>null</code> if no parameters are
	 * expected.
	 */
	private ArrayList<String> params;
	/** Current expected terminator character. */
	private char terminator;
	/** Procedure name in call escape. */
	private String procName;
	/** First SQL keyword or identifier in statement. */
	private String keyWord;
	/** First table name in from clause */
	private String tableName;
	/** Connection object for server specific parsing. */
	private ConnectionJDBC3 connection;
	private static transient final Log log = LogFactory
	.getLog(SQLParser.class);

	/** Syntax mask for time escape. */
	private static final byte[] timeMask = { '#', '#', ':', '#', '#', ':', '#',
			'#' };

	/** Syntax mask for date escape. */
	private static final byte[] dateMask = { '#', '#', '#', '#', '-', '#', '#',
			'-', '#', '#' };

	/** Syntax mask for timestamp escape. */
	static final byte[] timestampMask = { '#', '#', '#', '#', '-', '#', '#',
			'-', '#', '#', ' ', '#', '#', ':', '#', '#', ':', '#', '#' };

	/** Lookup table to test if character is part of an identifier. */
	private static boolean identifierChar[] = { false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, true, true, false, false, false, false,
			false, false, false, false, false, false, false, true, true, true,
			true, true, true, true, true, true, true, false, false, false,
			false, false, false, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, false, false,
			false, false, true, false, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, false, false,
			false, false, false };
	
	public static final String select_token = "select";
	public static final String from_token = "from";
	public static final String where_token = "where";

	public SQLParser(String sqlIn, ArrayList<String> paramList,
			ConnectionJDBC3 connection) {
		sql = sqlIn;
		in = sql.toCharArray();
		len = in.length;
		out = new char[len + 256]; // Allow extra for curdate/curtime
		params = paramList;
		procName = "";

		this.connection = connection;
	}

	public static String[] parse(String sql, ArrayList<String> paramList,
			ConnectionJDBC3 connection, boolean extractTable)
			throws SQLException {

	  // Don't cache extract table parse requests, just process it

		return SQLParser.parse2(sql);

	}
	
	public static String[] parse2(final String sql) throws SQLException {
		String result[] = new String[4];
		log.debug("sql="+sql);
		String tmpStr = StringTools.removeToken(sql,'"');
		//lower case the sql
		tmpStr=Sanitizer.lowercase2(tmpStr);
		log.debug("tmpStr="+tmpStr);
		String columnStr = tmpStr.substring(tmpStr.indexOf(select_token)+select_token.length()+1);
		
		String table = "";
		String where ="";
		String str = tmpStr.substring(tmpStr.lastIndexOf(from_token)+from_token.length() );
		if(str.indexOf(where_token)==-1)
			table=	str;
		else
		{
//			System.out.println("str="+str);
			table = str.substring(0,str.indexOf(where_token));
			where = str.substring(str.indexOf(where_token)+where_token.length());
		}
		table = table.trim();

		
		columnStr = columnStr.trim();
		
		columnStr = columnStr.substring(0,columnStr.lastIndexOf(from_token) );
		columnStr = columnStr.trim();
//		System.out.println("table="+table+",columns="+columnStr+" ,where="+where);
		result[3]= table;
		result[2]=columnStr;
		result[1]=where;
		return result;
	}

	

	String[] parse(boolean extractTable) throws SQLException {

		boolean isSelect = false;
		boolean isModified = false;
		boolean isSlowScan = true;
		try {
			while (s < len) {
				final char c = in[s];

				switch (c) {
				case '{':
					escape();
					isModified = true;
					break;
				case '[':
				case '"':
				case '\'':
					copyString();
					break;
				case '?':
					copyParam(null, d);
					break;
				case '/':
					if (s + 1 < len && in[s + 1] == '*') {
						skipMultiComments();
					} else {
						out[d++] = c;
						s++;
					}
					break;
				case '-':
					if (s + 1 < len && in[s + 1] == '-') {
						skipSingleComments();
					} else {
						out[d++] = c;
						s++;
					}
					break;
				default:
					if (isSlowScan && Character.isLetter(c)) {
						if (keyWord == null) {
							keyWord = copyKeyWord();
							if ("select".equals(keyWord)) {
								isSelect = true;
							}
							isSlowScan = extractTable && isSelect;
							break;
						}
						if (extractTable && isSelect) {
							String sqlWord = copyKeyWord();
							if ("from".equals(sqlWord)) {
								// Ensure only first 'from' is processed
								isSlowScan = false;
								tableName = getTableName();
							}
							break;
						}
					}

					out[d++] = c;
					s++;
					break;
				}
			}

			String result[] = new String[4];

			// return sql and procname
			result[0] = (isModified) ? new String(out, 0, d) : sql;
			result[1] = procName;
			result[2] = (keyWord == null) ? "" : keyWord;
			result[3] = tableName;
			return result;
		} catch (IndexOutOfBoundsException e) {
			// Should only come here if string is invalid in some way.
			throw new SQLException(Messages.get("error.parsesql.missing",
					String.valueOf(terminator)), "22025");
		}

	}

	/**
	 * Extracts the first table name following the keyword FROM.
	 * 
	 * @return the table name as a <code>String</code>
	 */
	private String getTableName() throws SQLException {
		StringBuffer name = new StringBuffer(128);
		copyWhiteSpace();
		char c = (s < len) ? in[s] : ' ';
		if (c == '{') {
			// Start of {oj ... } we can assume that there is
			// more than one table in select and therefore
			// it would not be updateable.
			return "";
		}
		//
		// Skip any leading comments before first table name
		//
		while (c == '/' || c == '-' && s + 1 < len) {
			if (c == '/') {
				if (in[s + 1] == '*') {
					skipMultiComments();
				} else {
					break;
				}
			} else {
				if (in[s + 1] == '-') {
					skipSingleComments();
				} else {
					break;
				}
			}
			copyWhiteSpace();
			c = (s < len) ? in[s] : ' ';
		}

		if (c == '{') {
			// See comment above
			return "";
		}
		//
		// Now process table name
		//
		while (s < len) {
			if (c == '[' || c == '"') {
				int start = d;
				copyString();
				name.append(String.valueOf(out, start, d - start));
				copyWhiteSpace();
				c = (s < len) ? in[s] : ' ';
			} else {
				int start = d;
				c = (s < len) ? in[s++] : ' ';
				while ((isIdentifier(c)) && c != '.' && c != ',') {
					out[d++] = c;
					c = (s < len) ? in[s++] : ' ';
				}
				name.append(String.valueOf(out, start, d - start));
				s--;
				copyWhiteSpace();
				c = (s < len) ? in[s] : ' ';
			}
			if (c != '.') {
				break;
			}
			name.append(c);
			out[d++] = c;
			s++;
			copyWhiteSpace();
			c = (s < len) ? in[s] : ' ';
		}
		return name.toString();
	}

	/**
	 * Copies over white space.
	 */
	private void copyWhiteSpace() {
		while (s < in.length && Character.isWhitespace(in[s])) {
			out[d++] = in[s++];
		}
	}

	/**
	 * Builds a new parameter item.
	 * 
	 * @param name
	 *            Optional parameter name or null.
	 * @param pos
	 *            The parameter marker position in the output buffer.
	 */
	private void copyParam(String name, int pos) throws SQLException {
//		if (params == null) {
//			throw new SQLException(Messages.get(
//					"error.parsesql.unexpectedparam", String.valueOf(s)),
//					"2A000");
//		}
//
//		ParamInfo pi = new ParamInfo(pos, connection.getUseUnicode());
//		pi.name = name;
//
//		if (pos >= 0) {
//			out[d++] = in[s++];
//		} else {
//			pi.isRetVal = true;
//			s++;
//		}
//
//		params.add(pi);
	}

	/**
	 * Skips embedded white space.
	 */
	private void skipWhiteSpace() {
		while (Character.isWhitespace(in[s])) {
			s++;
		}
	}

	/**
	 * Skips single-line comments.
	 */
	private void skipSingleComments() {
		while (s < len && in[s] != '\n' && in[s] != '\r') {
			// comments should be passed on to the server
			out[d++] = in[s++];
		}
	}

	/**
	 * Skips multi-line comments
	 */
	private void skipMultiComments() throws SQLException {
		int block = 0;

		do {
			if (s < len - 1) {
				if (in[s] == '/' && in[s + 1] == '*') {
					block++;
				} else if (in[s] == '*' && in[s + 1] == '/') {
					block--;
				}
				// comments should be passed on to the server
				out[d++] = in[s++];
			} else {
				throw new SQLException(Messages.get("error.parsesql.missing",
						"*/"), "22025");
			}
		} while (block > 0);
		out[d++] = in[s++];
	}

	/**
	 * Processes the JDBC escape sequences.
	 * 
	 * @throws SQLException
	 */
	private void escape() throws SQLException {
		char tc = terminator;
		terminator = '}';
		StringBuffer escBuf = new StringBuffer();
		s++;
		skipWhiteSpace();

		if (in[s] == '?') {
			copyParam("@return_status", -1);
			skipWhiteSpace();
			mustbe('=', false);
			skipWhiteSpace();

			while (Character.isLetter(in[s])) {
				escBuf.append(Character.toLowerCase(in[s++]));
			}

			skipWhiteSpace();
			String esc = escBuf.toString();

			if ("call".equals(esc)) {
				callEscape();
			} else {
				throw new SQLException(Messages.get("error.parsesql.syntax",
						"call", String.valueOf(s)), "22019");
			}
		} else {
			while (Character.isLetter(in[s])) {
				escBuf.append(Character.toLowerCase(in[s++]));
			}

			skipWhiteSpace();
			String esc = escBuf.toString();

			if ("call".equals(esc)) {
				callEscape();
			} else if ("t".equals(esc)) {
				if (!getDateTimeField(timeMask)) {
					throw new SQLException(
							Messages.get("error.parsesql.syntax", "time",
									String.valueOf(s)), "22019");
				}
			} else if ("d".equals(esc)) {
				if (!getDateTimeField(dateMask)) {
					throw new SQLException(
							Messages.get("error.parsesql.syntax", "date",
									String.valueOf(s)), "22019");
				}
			} else if ("ts".equals(esc)) {
				if (!getDateTimeField(timestampMask)) {
					throw new SQLException(Messages.get(
							"error.parsesql.syntax", "timestamp", String
									.valueOf(s)), "22019");
				}
			} else {
				throw new SQLException(Messages.get("error.parsesql.badesc",
						esc, String.valueOf(s)), "22019");
			}
		}

		mustbe('}', false);
		terminator = tc;
	}

	/**
	 * Utility routine to validate date and time escapes.
	 * 
	 * @param mask
	 *            The validation mask
	 * @return True if the escape was valid and processed OK.
	 */
	private boolean getDateTimeField(byte[] mask) throws SQLException {
		skipWhiteSpace();
		if (in[s] == '?') {
			// Allow {ts ?} type construct
			copyParam(null, d);
			skipWhiteSpace();
			return in[s] == terminator;
		}
		out[d++] = '\'';
		terminator = (in[s] == '\'' || in[s] == '"') ? in[s++] : '}';
		skipWhiteSpace();
		int ptr = 0;

		while (ptr < mask.length) {
			char c = in[s++];
			if (c == ' ' && out[d - 1] == ' ') {
				continue; // Eliminate multiple spaces
			}

			if (mask[ptr] == '#') {
				if (!Character.isDigit(c)) {
					return false;
				}
			} else if (mask[ptr] != c) {
				return false;
			}

			if (c != '-') {
				out[d++] = c;
			}

			ptr++;
		}

		if (mask.length == 19) { // Timestamp
			int digits = 0;

			if (in[s] == '.') {
				out[d++] = in[s++];

				while (Character.isDigit(in[s])) {
					if (digits < 3) {
						out[d++] = in[s++];
						digits++;
					} else {
						s++;
					}
				}
			} else {
				out[d++] = '.';
			}

			for (; digits < 3; digits++) {
				out[d++] = '0';
			}
		}

		skipWhiteSpace();

		if (in[s] != terminator) {
			return false;
		}

		if (terminator != '}') {
			s++; // Skip terminator
		}

		skipWhiteSpace();
		out[d++] = '\'';

		return true;
	}

	/**
	 * Checks that the next character is as expected.
	 * 
	 * @param c
	 *            The expected character.
	 * @param copy
	 *            True if found character should be copied.
	 * @throws SQLException
	 *             if expected characeter not found.
	 */
	private void mustbe(char c, boolean copy) throws SQLException {
		if (in[s] != c) {
			throw new SQLException(Messages.get("error.parsesql.mustbe", String
					.valueOf(s), String.valueOf(c)), "22019");
		}

		if (copy) {
			out[d++] = in[s++];
		} else {
			s++;
		}
	}

	/**
	 * Processes the JDBC {call procedure [(&#63;,&#63;,&#63;)]} type escape.
	 * 
	 * @throws SQLException
	 *             if an error occurs
	 */
	private void callEscape() throws SQLException {
		// Insert EXECUTE into SQL so that proc can be called as normal SQL
		copyLiteral("EXECUTE ");
		keyWord = "execute";
		// Process procedure name
		procName = copyProcName();
		skipWhiteSpace();

		if (in[s] == '(') { // Optional ( )
			s++;
			terminator = ')';
			skipWhiteSpace();
		} else {
			terminator = '}';
		}

		out[d++] = ' ';

		// Process any parameters
		while (in[s] != terminator) {
			String name = null;

			if (in[s] == '@') {
				// Named parameter
				name = copyParamName();
				skipWhiteSpace();
				mustbe('=', true);
				skipWhiteSpace();

				if (in[s] == '?') {
					copyParam(name, d);
				} else {
					// Named param has literal value can't call as RPC
					procName = "";
				}
			} else if (in[s] == '?') {
				copyParam(name, d);
			} else {
				// Literal parameter can't call as RPC
				procName = "";
			}

			// Now find terminator or comma
			while (in[s] != terminator && in[s] != ',') {
				if (in[s] == '{') {
					escape();
				} else if (in[s] == '\'' || in[s] == '[' || in[s] == '"') {
					copyString();
				} else {
					out[d++] = in[s++];
				}
			}

			if (in[s] == ',') {
				out[d++] = in[s++];
			}

			skipWhiteSpace();
		}

		if (terminator == ')') {
			s++; // Elide
		}

		terminator = '}';
		skipWhiteSpace();
	}

	/**
	 * Copies an embedded stored procedure identifier over to the output buffer.
	 * 
	 * @return The identifier as a <code>String</code>.
	 */
	private String copyProcName() throws SQLException {
		int start = d;

		do {
			if (in[s] == '"' || in[s] == '[') {
				copyString();
			} else {
				char c = in[s++];

				while (isIdentifier(c) || c == ';') {
					out[d++] = c;
					c = in[s++];
				}

				s--;
			}

			if (in[s] == '.') {
				while (in[s] == '.') {
					out[d++] = in[s++];
				}
			} else {
				break;
			}
		} while (true);

		if (d == start) {
			// Procedure name expected but found something else
			throw new SQLException(Messages.get("error.parsesql.syntax",
					"call", String.valueOf(s)), "22025");
		}

		return new String(out, start, d - start);
	}

	/**
	 * Copies an embedded parameter name to the output buffer.
	 * 
	 * @return The identifier as a <code>String</code>.
	 */
	private String copyParamName() {
		int start = d;
		char c = in[s++];

		while (isIdentifier(c)) {
			out[d++] = c;
			c = in[s++];
		}

		s--;

		return new String(out, start, d - start);
	}

	/**
	 * Inserts a String literal in the output buffer.
	 * 
	 * @param txt
	 *            The text to insert.
	 */
	private void copyLiteral(String txt) throws SQLException {
//		final int len = txt.length();
//
//		for (int i = 0; i < len; i++) {
//			final char c = txt.charAt(i);
//
//			if (c == '?') {
//				if (params == null) {
//					throw new SQLException(Messages
//							.get("error.parsesql.unexpectedparam", String
//									.valueOf(s)), "2A000");
//				}
//				// param marker embedded in escape
//				ParamInfo pi = new ParamInfo(d, connection.getUseUnicode());
//				params.add(pi);
//			}
//
//			out[d++] = c;
//		}
	}

	/**
	 * Copies over an embedded string literal unchanged.
	 */
	private void copyString() {
		char saveTc = terminator;
		char tc = in[s];

		if (tc == '[') {
			tc = ']';
		}

		terminator = tc;

		out[d++] = in[s++];

		while (in[s] != tc) {
			out[d++] = in[s++];
		}

		out[d++] = in[s++];

		terminator = saveTc;
	}

	/**
	 * Copies over possible SQL keyword eg 'SELECT'
	 */
	private String copyKeyWord() {
		int start = d;

		while (s < len && isIdentifier(in[s])) {
			out[d++] = in[s++];
		}

		return String.valueOf(out, start, d - start).toLowerCase();
	}

	/**
	 * Determines if character could be part of an SQL identifier.
	 * <p/>
	 * Characters > 127 are assumed to be unicode letters in other languages
	 * than english which is reasonable in this application.
	 * 
	 * @param ch
	 *            the character to test.
	 * @return <code>boolean</code> true if ch in A-Z a-z 0-9 @ $ # _.
	 */
	private static boolean isIdentifier(int ch) {
		return ch > 127 || identifierChar[ch];
	}

  /**
   * @return the params
   */
  public ArrayList<String> getParams() {
    return params;
  }

  /**
   * @param params the params to set
   */
  public void setParams(ArrayList<String> params) {
    this.params = params;
  }

  /**
   * @return the connection
   */
  public ConnectionJDBC3 getConnection() {
    return connection;
  }

  /**
   * @param connection the connection to set
   */
  public void setConnection(ConnectionJDBC3 connection) {
    this.connection = connection;
  }

}