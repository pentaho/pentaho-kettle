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

package org.pentaho.di.core;

import junit.framework.TestCase;

/**
 * Test class for counters functionality.
 *
 * @author Sven Boden
 */
public class CountersTest extends TestCase
{
	/**
	 * Test about all. Class is not too big.
	 */
	public void testGeneralFunctionality()
	{
		Counters cntrs = Counters.getInstance();
		assertNull(cntrs.getCounter("counter1"));
		cntrs.setCounter("counter1", new Counter());
		assertNotNull(cntrs.getCounter("counter1"));
		
		// Clear 1 counter
		cntrs.clearCounter("counter1");
		assertNull(cntrs.getCounter("counter1"));
		
		// Clear all
		cntrs.setCounter("counter1", new Counter());
		cntrs.setCounter("counter2", new Counter());
		assertNotNull(cntrs.getCounter("counter1"));
		assertNotNull(cntrs.getCounter("counter2"));
		cntrs.clear();
		assertNull(cntrs.getCounter("counter1"));
		assertNull(cntrs.getCounter("counter2"));

		// Same object is returned
		Counters cntrsCopy = Counters.getInstance();
		assertTrue(cntrsCopy == cntrs);
	}

}
