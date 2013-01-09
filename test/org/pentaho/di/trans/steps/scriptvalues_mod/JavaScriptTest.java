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

package org.pentaho.di.trans.steps.scriptvalues_mod;

/**
 * Test help class for the interface Modified Javascript to java.
 *
 * @author Sven Boden
 */
public class JavaScriptTest
{	
	
	public JavaScriptTest()
	{
		
	}
	
	/**
	 * Add 1 to l
	 * 
	 * @param l Original long value
	 * @return l + 1
	 */
	public Long add1ToLong(Long l)
	{
		return add1ToLongStatic(l);
	}
	
	/**
	 * Add 1 to l
	 * 
	 * @param l Original long value
	 * @return l + 1
	 */
	public static Long add1ToLongStatic(Long l)
	{
		long res = l.longValue() + 1;
		return new Long(res);
	}

	/**
	 * Add 1.0 to d
	 * 
	 * @param d Original double value
	 * @return d + 1.0
	 */
	public Double add1ToNumber(Double d)
	{
		return add1ToNumberStatic(d);
	}

	/**
	 * Add 1.0 to d
	 * 
	 * @param d Original double value
	 * @return d + 1.0
	 */
	public static Double add1ToNumberStatic(Double d)
	{
		double res = d.doubleValue() + 1;
		return new Double(res);
	}	
	
	/**
	 * Add 1 to int value in s
	 * 
	 * @param s Original string value
	 * @return s + 1
	 */
	public String add1ToString(String s)
	{
		return add1ToStringStatic(s);
	}
	
	/**
	 * Add 1 to int value in s
	 * 
	 * @param s Original string value
	 * @return s + 1
	 */
	public static String add1ToStringStatic(String s)
	{
		long res = 0L;
		
		try 
		{
		    res = Long.parseLong(s);
		}
		catch ( Exception ex )
		{
			res = 0L;
		}
		return "" + (res + 1);
	}		
}