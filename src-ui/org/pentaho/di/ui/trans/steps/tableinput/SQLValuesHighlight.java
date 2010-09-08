 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.trans.steps.tableinput;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SQLValuesHighlight implements LineStyleListener {
	JavaScanner scanner = new JavaScanner();
	int[] tokenColors;
	Color[] colors;
	Vector<int[]> blockComments = new Vector<int[]>();

	public static final int EOF= -1;
	public static final int EOL= 10;

	public static final int WORD=		0;
	public static final int WHITE=		1;
	public static final int KEY=		2;
	public static final int COMMENT=	3;	// single line comment:	//
	public static final int STRING=		5;
	public static final int OTHER=		6;
	public static final int NUMBER=		7;
	public static final int FUNCTIONS=	8;

	public static final int MAXIMUM_TOKEN= 9;

	public SQLValuesHighlight() {
		initializeColors();
		scanner = new JavaScanner();
	}
	
	public SQLValuesHighlight(String[] strArrSQLFunctions) {
		initializeColors();
		scanner = new JavaScanner();
		scanner.setSQLKeywords(strArrSQLFunctions);
		scanner.initializeSQLFunctions();
	}

	Color getColor(int type) {
		if (type < 0 || type >= tokenColors.length) {
			return null;
		}
		return colors[tokenColors[type]];
	}

	boolean inBlockComment(int start, int end) {
		for (int i=0; i<blockComments.size(); i++) {
			int[] offsets = (int[])blockComments.elementAt(i);
			// start of comment in the line
			if ((offsets[0] >= start) && (offsets[0] <= end)) return true;
			// end of comment in the line
			if ((offsets[1] >= start) && (offsets[1] <= end)) return true;
		if ((offsets[0] <= start) && (offsets[1] >= end)) return true;
	}
	return false;
}

	void initializeColors() {
		Display display = Display.getDefault();
		colors= new Color[] {
				new Color(display, new RGB(0, 0, 0)),		// black
				new Color(display, new RGB(255,0,0)), 		// red 
				new Color(display, new RGB(63, 127, 95)),	// green
				new Color(display, new RGB(0, 0, 255)),	    // blue
				new Color(display, new RGB(255, 0, 255))	// SQL Functions / Rose
				
		};
		tokenColors= new int[MAXIMUM_TOKEN];
		tokenColors[WORD]=		0;
		tokenColors[WHITE]=		0;
		tokenColors[KEY]=		3;
		tokenColors[COMMENT]=	2; 
		tokenColors[STRING]= 	1;
		tokenColors[OTHER]=		0;
		tokenColors[NUMBER]=	0;
		tokenColors[FUNCTIONS]=	4;
	}


	void disposeColors() {
		for (int i=0;i<colors.length;i++) {
			colors[i].dispose();
		}
	}

	/**
	 * Event.detail			line start offset (input)	
	 * Event.text 			line text (input)
	 * LineStyleEvent.styles 	Enumeration of StyleRanges, need to be in order. (output)
	 * LineStyleEvent.background 	line background color (output)
	 */
	public void lineGetStyle(LineStyleEvent event) {
		Vector<StyleRange> styles = new Vector<StyleRange>();
		int token;
		StyleRange lastStyle;
		
		if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
			styles.addElement(new StyleRange(event.lineOffset, event.lineText.length()+4, colors[1], null));
			event.styles = new StyleRange[styles.size()];
			styles.copyInto(event.styles);
			return;
		}
		scanner.setRange(event.lineText);
		String xs = ((StyledText)event.widget).getText();
		if(xs!=null) parseBlockComments(xs);
		token = scanner.nextToken();
		while (token != EOF) {
			if (token == OTHER) {
				// do nothing
			} else if ((token == WHITE) && (!styles.isEmpty())) {
				int start = scanner.getStartOffset() + event.lineOffset;
				lastStyle = (StyleRange)styles.lastElement();
				if (lastStyle.fontStyle != SWT.NORMAL) {
					if (lastStyle.start + lastStyle.length == start) {
						// have the white space take on the style before it to minimize font style
						// changes
						lastStyle.length += scanner.getLength();
					}
				}
			} else {		
				Color color = getColor(token);
				if (color != colors[0]) {		// hardcoded default foreground color, black
					StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
					if (token == KEY) {
						//style.fontStyle = SWT.BOLD;
					}
					if (styles.isEmpty()) {
						styles.addElement(style);
					} else {
						lastStyle = (StyleRange)styles.lastElement();
						if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start)) {
							lastStyle.length += style.length;
						} else {
							styles.addElement(style); 
						}
					} 
				} 
			}
			token= scanner.nextToken();
		}
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);
	}

	public void parseBlockComments(String text) {
		blockComments = new Vector<int[]>();
		StringReader buffer = new StringReader(text);
		int ch;
		boolean blkComment = false;
		int cnt = 0;
		int[] offsets = new int[2];
		boolean done = false;
	
		try {
			while (!done) {
				switch (ch = buffer.read()) {
				case -1 : {
					if (blkComment) {
						offsets[1] = cnt;
						blockComments.addElement(offsets);
					}
					done = true;
					break;
				}
				case '/' : {
					ch = buffer.read();
					if ((ch == '*') && (!blkComment)) {
						offsets = new int[2];
						offsets[0] = cnt;
						blkComment = true;
						cnt++;	
					} else {
						cnt++;
					}						
					cnt++;
					break;
				}
				case '*' : {
					if (blkComment) {
						ch = buffer.read();
						cnt++;
						if (ch == '/') {
							blkComment = false;	
							offsets[1] = cnt;
							blockComments.addElement(offsets);
						}
					}
					cnt++;	
					break;
				}
				default : {
					cnt++;				
					break;
				}
				}
			}		
		} catch(IOException e) {
			// ignore errors
		}
	}

	/**
	 * A simple fuzzy scanner for Java
	 */
	public class JavaScanner {
		protected Map<String, Integer> fgKeys= null;
		protected Map<?, ?> fgFunctions= null;
		protected Map<String, Integer> kfKeys= null;
		protected Map<?, ?> kfFunctions= null;
		
		protected StringBuffer fBuffer= new StringBuffer();
		protected String fDoc;
		protected int fPos;
		protected int fEnd;
		protected int fStartToken;
		protected boolean fEofSeen= false;

		private String[] kfKeywords ={
				"getdate","case","convert","left","right","isnumeric","isdate","isnumber",
				"number", "finally", "cast","var","fetch_status","isnull","charindex",
				"difference","len","nchar","quotename","replicate","reverse","str","stuff",
				"unicode","ascii","char",
				
				"to_char","to_date","to_number","nvl","sysdate","corr","count","grouping","max","min","stdev","sum",
				"concat","length","locate","ltrim","posstr","repeat","replace","rtrim",
				"soundex","space","substr","substring","trunc","nextval","currval","getclobval",
				
				"char_length","compare","patindex","sortkey","uscalar",
				
				"current_date","current_time","current_timestamp","current_user","session_user","system_user",
				"curdate","curtime","database","now","sysdate","today","user","version","coalesce",
				"nullif","octet_length","datalength","decode","greatest","ifnull","least","||",
				"char_length","character_length","collate","concatenate","like","lower","position",
				"translate","upper","char_octet_length","character_maximum_length","character_octet_length",
				"ilike","initcap","instr","lcase","lpad","patindex","rpad","ucase","bit_length","&","|","^",
				"%","+","-","*","/","(",")","abs","asin","atan","ceiling","cos","cot","exp","floor","ln","log",
				"log10","mod","pi","power","rand","round","sign","sin","sqrt","tan","trunc","extract",
				"interval","overlaps","adddate","age","date_add","dateformat","date_part","date_sub",
				"datediff","dateadd","datename","datepart","day","dayname","dayofmonth","dayofweek",
				"dayofyear","hour","last_day","minute","month","month_between","monthname","next_day","second",
				"sub_date","week","year"
		};
			
			private String[] fgKeywords= { 
					"create","procedure","as","set","nocount","on","declare","varchar","print","table","int","tintytext",
					"select","from","where","and","or","insert","into","cursor","read_only","for","open","fetch",
					"next","end","deallocate","table","drop","exec","begin","close","update","delete","truncate",
					"inner","outer","join","union","all","float","when",
					"nolock","with","false","datetime","dare","time","hour",
					"array","minute","second","millisecond","view","function",
					"catch", "const","continue","compute","browse","option",
					"date", "default", "delete", "do","raw","auto","explicit","xmldata",
					"elements","binary","base64","read","outfile","asc","desc",
					"else","eval","escape","having","limit","offset","of","intersect","except","using",
					"variance","specific","language","body","returns","specific","deterministic",
					"not","external","action","reads","static","inherit","called","order","group","by",
					"natural","full","exists","between","some","any","unique","match","value","limite",
					"minus","references","grant","on","top","index",
					"bigint","text","char","use","move",
					"exec","init","name","noskip","skip","noformat","format","stats","disk","from","to","rownum",
					"alter","add","delete","remove",
					"if","else","in","is",
					"new", "Number", "null",
					"string", "switch",
					"this","then","throw", "true","false","try",
					"return",
					"with", "while",
					"start","connect","optimize","first","only","rows","sequence","blob","clob","image","binary",
					"column","decimal","distinct","primary","key","varbinary"
			};

		public JavaScanner() {
			initialize();
			initializeSQLFunctions();
		}

		/**
		 * Returns the ending location of the current token in the document.
		 */
		public final int getLength() {
			return fPos - fStartToken;
		}

		/**
		 * Initialize the lookup table.
		 */
		void initialize() {
			fgKeys= new Hashtable<String, Integer>();
			Integer k= new Integer(KEY);
			for (int i= 0; i < fgKeywords.length; i++)
				fgKeys.put(fgKeywords[i], k);
		}
		
		public void setSQLKeywords(String[]kfKeywords ){
			this.kfKeywords = kfKeywords;
		}
		
		void initializeSQLFunctions(){
			kfKeys = new Hashtable<String, Integer>();
			Integer k = new Integer(FUNCTIONS);
			for (int i= 0; i < kfKeywords.length; i++)
				kfKeys.put(kfKeywords[i], k);
		}

		/**
		 * Returns the starting location of the current token in the document.
		 */
		public final int getStartOffset() {
			return fStartToken;
		}

		/**
		 * Returns the next lexical token in the document.
		 */
		public int nextToken() {
			int c;
			fStartToken= fPos;
			while (true) {
				switch (c= read()) {			
				case EOF:
					return EOF;				
				case '/':	// comment
					c= read();
					if(c == '/') {
						while (true) {
							c= read();
							if ((c == EOF) || (c == EOL)) {
								unread(c);
								return COMMENT;
							}
						}
					} else {
						unread(c);
					}
					return OTHER;
				case '-':	// comment
					c= read();
					if(c == '-') {
						while (true) {
							c= read();
							if ((c == EOF) || (c == EOL)) {
								unread(c);
								return COMMENT;
							}
						}
					} else {
						unread(c);
					}
					return OTHER;
				case '\'':	// char const
					for(;;) {
						c= read();
						switch (c) {
						case '\'':
							return STRING;
						case EOF:
							unread(c);
							return STRING;
						case '\\':
							c= read();
							break;
					}
				}

				case '"':	// string
					for (;;) {
						c= read();
						switch (c) {
						case '"':
							return STRING;
						case EOF:
							unread(c);
							return STRING;
						case '\\':
							c= read();
							break;
						}
					}	

				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					do {
						c= read();
					} while(Character.isDigit((char)c));
					unread(c);
					return NUMBER;
				default:
					if (Character.isWhitespace((char)c)) {
						do {
							c= read();
						} while(Character.isWhitespace((char)c));
						unread(c);
						return WHITE;
					}
					if (Character.isJavaIdentifierStart((char)c)) {
						fBuffer.setLength(0);
						do {
							fBuffer.append((char)c);
							c= read();
						} while(Character.isJavaIdentifierPart((char)c));
						unread(c);
						Integer i= (Integer) fgKeys.get(fBuffer.toString());
						if (i != null) return i.intValue();
						i= (Integer) kfKeys.get(fBuffer.toString());
						if (i != null) return i.intValue();
						return WORD;
					}	
					return OTHER;
				}
			}
		}

		/**
		 * Returns next character.
		 */
		protected int read() {
			if (fPos <= fEnd) {
				return fDoc.charAt(fPos++);
			}
			return EOF;
		}

		public void setRange(String text) {
			fDoc= text.toLowerCase();
			fPos= 0;
			fEnd= fDoc.length() -1;
		}

		protected void unread(int c) {
			if (c != EOF)
				fPos--;
		}
	}
}