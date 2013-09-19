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

import java.nio.CharBuffer;

public class StringTools {
	
	/**
	 * @deprecated
	 * 
	 */
  @Deprecated
	public static void removeToken(StringBuilder sb,String token) {
		int index =sb.indexOf(token);
		while(index!=-1)
		{
			sb = sb.deleteCharAt(index);
			index =sb.indexOf(token);
		}
	}
	
	/**
	 * 
	 * @param s
	 * @param token
	 * @return
	 */
	public static String removeToken(String s,char token) {
		char[] data = s.toCharArray();
		CharBuffer cb = CharBuffer.allocate(s.length());
		for (int i = 0; i < data.length; i++) {
			char d = data[i];
			if(d!=token)
			{
				cb.append(d);
			}
		}
		String tmpStr = new String(cb.array());
		tmpStr = tmpStr.trim();
		return tmpStr;
	}
	
	static void loop(String input,int counter)
	{
		for (int i = 0; i < counter; i++) {
			removeToken(new StringBuilder(input),"\"");
		}
	}
	
	static void loop2(String input,int counter)
	{
		for (int i = 0; i < counter; i++) {
			removeToken(input,'"');
		}
	}
	
	public static void main(String[] args) {
		String str = "select \"testor\".col1 as \"unit cell\",\"testor\".col3 as \"price cell\",c\"testor\".ol2 as \"price cell\" from \"testor\".\"orderdetail\"";
		System.out.println(removeToken(str,'"'));
		int counter=500;
		long start = System.currentTimeMillis();
		StringTools.loop(str, counter);
		System.out.println(System.currentTimeMillis()-start);
		
		start = System.currentTimeMillis();
		StringTools.loop2(str, counter);
		System.out.println(System.currentTimeMillis()-start);
	}
	
}
