/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The SimpleTokenizer class is used to break a string into tokens.
 *
 * <p>The delimiter	can be used in one of two ways, depending on how
 * the singleDelimiter flag is set:
 * <ul>
 * <li>when true, the entire string is treated as a single delimiter
 * <li>when false, each character in the string is treated as a delimiter
 * </ul>
 *
 * <p>The total number of tokens in the text is equal to the number of
 * delimeters found plus one. An empty token is returned when:
 * <ul>
 * <li>two consecutive delimiters are found
 * <li>the text starts with a delimiter
 * <li>the text ends with a delimiter
 * </ul>
 *
 * <p>You can use the tokenizer like the StringTokenizer:
 * <pre>
 *     SimpleTokenizer st = new SimpleTokenizer("this is a test", " ");
 *     while (st.hasMoreTokens())
 *         println(st.nextToken());
 * </pre>
 *
 * <p>Or, you can use the tokenizer like the String.split(...) method:
 * <pre>
 *     SimpleTokenizer st = new SimpleTokenizer("this is a test", " ");
 *     List list = st.getAllTokens();
 *     for (java.util.Iterator it = list.iterator(); it.hasNext();)
 *         println(it.next());
 * </pre>
 *
 * @see java.util.StringTokenizer
 * @see java.lang.String#split
 */
public class SimpleTokenizer
{
	private String delimiter;
	private boolean singleDelimiter;
	private char maxDelimiter;
	private List<String> tokens;

	protected String text;
	protected int position;
	protected int maxPosition;

	/**
	 * Constructs a tokenizer for the specified string.
	 *
	 * Each character in the delimiter string is treated as a delimiter.
	 *
	 * @param  text       a string to be tokenized.
	 * @param  delimiter  the delimiter.
	 */
	public SimpleTokenizer(String text, String delimiter)
	{
		this(text, delimiter, false);
	}

	/**
	 * Constructs a tokenizer for the specified string.
	 *
	 * <p>If the singleDelmiter flag is true, then the delimiter string is
	 * used as a single delimiter. If the flag is false, the each character
	 * in the delimiter is treated as a delimiter.
	 *
	 * @param  text       a string to be tokenized.
	 * @param  delimiter  the delimiter(s).
	 * @param  multipleDelimiters  treat each character as a delimiter.
	 */
	public SimpleTokenizer(String text, String delimiter, boolean singleDelimiter)
	{
		setText( text );
		setDelimiter( delimiter );
		this.singleDelimiter = singleDelimiter;
	}

	/**
	 *	Set the text to be tokenized.
	 *
	 *  @param  text  a string to be tokenized.
	 */
	public void setText(String text)
	{
		this.text = text;
		this.position = 0;
		this.maxPosition = text.length();
	}

	/**
	 *	Set the delimiter(s) used to parse the text.
	 *  The delimiter can be reset before retrieving the next token.
	 *
	 *  @param  delimiter  the delimiter.
	 */
	public void setDelimiter(String delimiter)
	{
		this.delimiter = delimiter;

		maxDelimiter = 0;

		for (int i = 0; i < delimiter.length(); i++)
		{
		    char c = delimiter.charAt(i);

		    if (maxDelimiter < c)
				maxDelimiter = c;
		}
	}

	/**
	 *  Tests if there are more tokens available from this tokenizer.
	 *  A subsequent call to <tt>nextToken</tt> will return a token.
	 *
	 *  @return  <code>true</code> when there is at least one token remaining;
	 *           <code>false</code> otherwise.
	 */
	public boolean hasMoreTokens()
	{
		return position <= maxPosition;
	}

	/**
	 *  Returns the next token from this tokenizer.
	 *
	 *  @return     the next token from this tokenizer.
	 *  @exception  NoSuchElementException  if there are no more tokens
	 */
	public String nextToken()
	{
		if (position > maxPosition)
			throw new NoSuchElementException();

		//  Return an empty token when we have finished parsing all
		//  the other tokens and the text ends with a delimiter

		if (position == maxPosition)
		{
			position++;
			return "";
		}

		return parseToken();
	}
	/**
	 *	Invoked by nextToken() once it is determined that more tokens exist.
	 *
	 *  @return     the next token from this tokenizer.
	 */
	protected String parseToken()
	{
		//  Parsing is different depending on the number of delimiters

		if (singleDelimiter || delimiter.length() == 1)
		{
			return parseUsingSingleDelimiter();
		}
		else
		{
			return parseUsingMultipleDelimiters();
		}
	}

	/*
	 *  Search the entire string until the delimiter is found
	 */
	private String parseUsingSingleDelimiter()
	{
		String token = null;
		int delimiterPosition = 0;

		//  A character search is faster than a String search

		if (delimiter.length() == 1)
			delimiterPosition = text.indexOf(delimiter.charAt(0), position);
		else
			delimiterPosition = text.indexOf(delimiter, position);

		//  Extract token based on search result

		if (delimiterPosition == -1)
		{
			token = text.substring(position, maxPosition);
			position = maxPosition + 1;
		}
		else
		{
			token = text.substring(position, delimiterPosition);
			position = delimiterPosition + delimiter.length();
		}

		return token;
	}

	/*
	 *  Check each character to see if it is one of the delimiters
	 */
	private String parseUsingMultipleDelimiters()
	{
		String token = null;
		int start = position;
		boolean searchingForDelimiter = true;

		while (position < maxPosition)
		{
			char c = text.charAt(position);

            if (c <= maxDelimiter && delimiter.indexOf(c) >= 0)
            {
            	searchingForDelimiter = false;
				break;
			}

		    position++;
		}

		//  Extract token based on search result

		if (searchingForDelimiter)
		{
			token = text.substring(start, maxPosition);
			position = maxPosition + 1;
		}
		else
		{
			token = text.substring(start, position);
			position++;
		}

		return token;
	}

	/**
	 * Returns the nth token from the current position of this tokenizer.
	 * This is equivalent to advancing n-1 tokens and returning the nth token.
	 *
	 * @param  tokenCount  the relative position of the token requested
	 * @return     the token found at the requested relative position.
	 * @exception  NoSuchElementException  if there are no more tokens
	 */
	public String nextToken(int tokenCount)
	{
		while (--tokenCount > 0)
		{
			nextToken();
		}

		return nextToken();
	}

	/**
	 *  Get the text that has not yet been tokenized.
	 *
	 *  @return  the remainder of the text to be tokenized
	 */
	public String getRemainder()
	{
		return hasMoreTokens() ? text.substring(position) : "";
	}

	/**
	 *  Tokenize the remaining text and return all the tokens
	 *
	 *  @return  a List containing all of the individual tokens
	 */
	public List<String> getAllTokens()
	{
		tokens = new ArrayList<String>();

		while (hasMoreTokens())
		{
			tokens.add( nextToken() );
		}

		return tokens;
	}
}
