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
	
}