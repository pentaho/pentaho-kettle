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

package org.pentaho.di.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of StringUtil.
 *
 * @author Sven Boden
 */
public class StringUtilTest extends TestCase
{
	/**
	 *  Test initCap for JIRA PDI-619.
	 */	
	public void testinitCap()
	{
		assertEquals("", StringUtil.initCap(null));
		assertEquals("", StringUtil.initCap(""));
		assertEquals("", StringUtil.initCap("   "));
		
		assertEquals("A word", StringUtil.initCap("a word"));
		assertEquals("A word", StringUtil.initCap("A word"));
		
		assertEquals("Award", StringUtil.initCap("award"));
		assertEquals("Award", StringUtil.initCap("Award"));

		assertEquals("AWard", StringUtil.initCap("aWard"));
		assertEquals("AWard", StringUtil.initCap("AWard"));
	}
	
	
	/**
	 * Create an example map to be used for variable resolution.
	 * 
	 * @return Map of variablenames/values.
	 */
	Map<String, String> createVariables1(String open, String close)
	{
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("EMPTY", "");
		map.put("checkcase", "case1");
		map.put("CheckCase", "case2");
		map.put("CHECKCASE", "case3");
		map.put("VARIABLE_1", "VARIABLE1");
		
		map.put("recursive1", "A" + open + "recursive2" + close);
		map.put("recursive2", "recurse");
		
		map.put("recursive3", open + "recursive4" + close + "B");
		map.put("recursive4", "recurse");

		map.put("recursive5", open + "recursive6" + close + "B");
		map.put("recursive6", "Z" + open + "recursive7" + close);
		map.put("recursive7", "final");		
		
		// endless recursive 
		map.put("recursive_all", open + "recursive_all1" + close + " tail");
		map.put("recursive_all1", open + "recursive_all" + close + " tail1");
		
		return map;
	}
	
	/**
	 * Test the basic substitute call.
	 */
	public void testSubstituteBasic()	
	{
		Map<String, String> map = createVariables1("${", "}");
		assertEquals("||", StringUtil.substitute("|${EMPTY}|", map, "${", "}", 0));
		assertEquals("|case1|", StringUtil.substitute("|${checkcase}|", map, "${", "}", 0));
		assertEquals("|case2|", StringUtil.substitute("|${CheckCase}|", map, "${", "}", 0));
		assertEquals("|case3|", StringUtil.substitute("|${CHECKCASE}|", map, "${", "}", 0));
		assertEquals("|Arecurse|", StringUtil.substitute("|${recursive1}|", map, "${", "}", 0));
		assertEquals("|recurseB|", StringUtil.substitute("|${recursive3}|", map, "${", "}", 0));
		assertEquals("|ZfinalB|", StringUtil.substitute("|${recursive5}|", map, "${", "}", 0));
		
		try {
		    StringUtil.substitute("|${recursive_all}|", map, "${", "}", 0);
		    fail("recursive check is failing");
		}
		catch ( RuntimeException rex )
		{ }
		
		map = createVariables1("%%", "%%");
		assertEquals("||", StringUtil.substitute("|%%EMPTY%%|", map, "%%", "%%", 0));
		assertEquals("|case1|", StringUtil.substitute("|%%checkcase%%|", map, "%%", "%%", 0));
		assertEquals("|case2|", StringUtil.substitute("|%%CheckCase%%|", map, "%%", "%%", 0));
		assertEquals("|case3|", StringUtil.substitute("|%%CHECKCASE%%|", map, "%%", "%%", 0));
		assertEquals("|Arecurse|", StringUtil.substitute("|%%recursive1%%|", map, "%%", "%%", 0));
		assertEquals("|recurseB|", StringUtil.substitute("|%%recursive3%%|", map, "%%", "%%", 0));
		assertEquals("|ZfinalB|", StringUtil.substitute("|%%recursive5%%|", map, "%%", "%%", 0));
		
		try {
		    StringUtil.substitute("|%%recursive_all%%|", map, "%%", "%%", 0);
		    fail("recursive check is failing");
		}
		catch ( RuntimeException rex )
		{ }		
	}
	
	/**
	 * Test isEmpty() call.
	 */
	public void testIsEmpty()	
	{
		assertTrue(StringUtil.isEmpty((String)null));
		assertTrue(StringUtil.isEmpty(""));
		assertFalse(StringUtil.isEmpty("A"));
		assertFalse(StringUtil.isEmpty(" A "));
	}
}