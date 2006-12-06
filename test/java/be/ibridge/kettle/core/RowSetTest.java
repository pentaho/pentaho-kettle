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
 
package be.ibridge.kettle.core;

import junit.framework.TestCase;
import be.ibridge.kettle.core.value.Value;

/**
 * Test class for the basic functionality of RowSet.
 *
 * @author Sven Boden
 */
public class RowSetTest extends TestCase
{
	/**
	 * The basic stuff.
	 */
	public void testBasicCreation()
	{
	    RowSet set = new RowSet(10);
	    
	    assertTrue(!set.isDone());
	    assertTrue(set.isEmpty());
	    assertTrue(!set.isFull());
	    assertEquals(0, set.size());
	}

	/**
	 * Functionality test.
	 */
	public void testFuntionality1()
	{
		RowSet set = new RowSet(3);

	    Row r1 = new Row();
        r1.addValue(new Value("ROWNR", 1L));
	    Row r2 = new Row();
        r2.addValue(new Value("ROWNR", 2L));
	    Row r3 = new Row();
        r3.addValue(new Value("ROWNR", 3L));
	    Row r4 = new Row();
        r4.addValue(new Value("ROWNR", 4L));
	    Row r5 = new Row();
        r5.addValue(new Value("ROWNR", 5L));

        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        
        // Add first row. State 1
        set.putRow(r1);
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(1, set.size());
        
        // Add another row. State: 1 2
        set.putRow(r2);
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(2, set.size());
        
        // Pop off row. State: 2
        Row r = set.getRow();
        Value v = r.searchValue("ROWNR");
        assertEquals(1L, v.getInteger()); 
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(1, set.size());

        // Add another row. State: 2 3
        set.putRow(r3);
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(2, set.size());        

        // Add another row. State: 2 3 4
        set.putRow(r4);
        assertTrue(!set.isEmpty());
        assertTrue(set.isFull());
        assertEquals(3, set.size());        

        // Add another row. State: 2 3 4 5
        // Note that we can still add rows after the set is full.
        set.putRow(r5);
        assertTrue(!set.isEmpty());
        assertTrue(set.isFull());
        assertEquals(4, set.size());        

        // Pop off row. State: 3 4 5 
        r = set.getRow();
        v = r.searchValue("ROWNR");
        assertEquals(2L, v.getInteger()); 
        assertTrue(!set.isEmpty());
        assertTrue(set.isFull());
        assertEquals(3, set.size());

        // Pop off row. State: 4 5 
        r = set.getRow();
        v = r.searchValue("ROWNR");
        assertEquals(3L, v.getInteger()); 
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(2, set.size());

        // Pop off row. State: 5 
        r = set.getRow();
        v = r.searchValue("ROWNR");
        assertEquals(4L, v.getInteger()); 
        assertTrue(!set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(1, set.size());

        // Pop off row. State:  
        r = set.getRow();
        v = r.searchValue("ROWNR");
        assertEquals(5L, v.getInteger()); 
        assertTrue(set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(0, set.size());

        // Pop off row. State:
        try  {
            r = set.getRow();
            fail("expected NoSuchElementException");
        }
        catch ( IndexOutOfBoundsException ex ) 
        { }
        assertTrue(set.isEmpty());
        assertTrue(!set.isFull());
        assertEquals(0, set.size());        
	}

	/**
	 * Names test. Just for completeness.
	 */
	public void testNames()
	{
		RowSet set = new RowSet(3);

	    Row r1 = new Row();
        r1.addValue(new Value("ROWNR", 1L));
        set.setThreadNameFromToCopy("from", 2, "to", 3);
        
        assertEquals("from", set.getOriginStepName());
        assertEquals(2, set.getOriginStepCopy());
        assertEquals("to", set.getDestinationStepName());
        assertEquals(3, set.getDestinationStepCopy());
        assertEquals(set.toString(), set.getName());
        assertEquals("from.2 - to.3", set.getName());
        
	}

}
