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