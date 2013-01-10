package org.pentaho.di.jdbc;

import java.nio.CharBuffer;

public class StringTools {
	
	/**
	 * @deprecated
	 * 
	 */
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
