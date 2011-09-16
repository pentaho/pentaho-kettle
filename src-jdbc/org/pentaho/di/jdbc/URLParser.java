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
 */
package org.pentaho.di.jdbc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class URLParser {
	public static final String TOKEN1 = "|";
	public static final String TOKEN2 = "&";
	public static final String TOKEN3 = "=";
	
	private String kettleUrl;
	private String[] options;

	public String getKettleUrl() {
		return kettleUrl;
	}

	public void setKettleUrl(String kettleUrl) {
		this.kettleUrl = kettleUrl;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public URLParser() {

	}

	public void parse(String url) {

		// ArrayList<String> list = new ArrayList<String>();
		
		int index = url.indexOf(TOKEN1);
		if (index != -1) {
			this.kettleUrl = url.substring(0, index);
			if(url.length()>(index+1))
			{
				String tmp = url.substring(index+1);
				StringTokenizer st = new StringTokenizer(tmp,TOKEN2);
				options = new String[st.countTokens()];
				int i =0;
				while(st.hasMoreTokens())
				{
					String tmpStr = st.nextToken();
					try {
					  tmpStr= java.net.URLDecoder.decode(tmpStr, "UTF-8");
					} catch(UnsupportedEncodingException e) {
					  throw new RuntimeException(e);
					}
					options[i]=tmpStr;
					i++;
				}
			}
			
		}
		else
		{
			this.kettleUrl = url;
		}

	}
	
	public static void main(String[] args) {
		String url ="a=1&b=2&c=3&d=4&e=5&f=6&g=7";
		long start = System.currentTimeMillis();
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(url,"&");
		System.out.println(st.countTokens());
		while(st.hasMoreTokens())
		{
			list.add(st.nextToken());
		}
		
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		
		start = System.currentTimeMillis();
		String tmp = url;
		list = new ArrayList<String>();
		int index = tmp.indexOf("&");
		
		while(index!=-1)
		{
			list.add(tmp.substring(0,index));
			tmp = tmp.substring(index+1);
			
			index = tmp.indexOf("&");
		}
		end = System.currentTimeMillis();
		System.out.println(end-start);
	}
}
