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

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Test class for the basic functionality of RowSet.
 *
 * @author Sven Boden
 */
public class RowSetTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ROWNR", ValueMeta.TYPE_INTEGER),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	
	/**
	 * The basic stuff.
	 */
	public void testBasicCreation()
	{
	    RowSet set = new BlockingRowSet(10);
	    
	    assertTrue(!set.isDone());
	    // TODO assertTrue(set.isEmpty());
	    // TODO assertTrue(!set.isFull());
	    assertEquals(0, set.size());
	}

	/**
	 * Functionality test.
	 */
	public void testFuntionality1()
	{
		RowSet set = new BlockingRowSet(3);

		RowMetaInterface rm = createRowMetaInterface();
		
	    Object[] r1 = new Object[] { new Long(1L) };
	    Object[] r2 = new Object[] { new Long(2L) };
	    Object[] r3 = new Object[] { new Long(3L) };
	    Object[] r4 = new Object[] { new Long(4L) };
	    Object[] r5 = new Object[] { new Long(5L) };
	    
	 // TODO assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        
        // Add first row. State 1
        set.putRow(rm, r1);
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(1, set.size());
        
        // Add another row. State: 1 2
        set.putRow(rm, r2);
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(2, set.size());
        
        // Pop off row. State: 2
        Object[] r = set.getRow();
        int i = rm.indexOfValue("ROWNR");
        assertEquals(1L, ((Long)r[i]).longValue()); 
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(1, set.size());

        // Add another row. State: 2 3
        set.putRow(rm, r3);
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(2, set.size());        

        // Add another row. State: 2 3 4
        set.putRow(rm, r4);
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(set.isFull());
        assertEquals(3, set.size());        

/*********************************************************************
 *  This was made in more restrict in v2.5.0 with a new RowSet 
 *  implementation. After v2.5.0 you may not try to put more rows
 *  in a rowset then it can hold (this functionality was also never
 *  used in PDI anyway).
 * 
        // Add another row. State: 2 3 4 5
        // Note that we can still add rows after the set is full.
        set.putRow(r5);
        assertTrue(!set.isEmpty());
        assertTrue(set.isFull());
        assertEquals(4, set.size());        
 *********************************************************************/

        // Pop off row. State: 3 4  
        r = set.getRow();
        i = rm.indexOfValue("ROWNR");
        assertEquals(2L, ((Long)r[i]).longValue()); 
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(2, set.size());

        // Add another row. State: 3 4 5
        set.putRow(rm, r5);
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(set.isFull());
        assertEquals(3, set.size());        
        
        // Pop off row. State: 4 5 
        r = set.getRow();
        i = rm.indexOfValue("ROWNR");
        assertEquals(3L, ((Long)r[i]).longValue());  
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(2, set.size());

        // Pop off row. State: 5 
        r = set.getRow();
        i = rm.indexOfValue("ROWNR");
        assertEquals(4L, ((Long)r[i]).longValue());  
        // TODO assertTrue(!set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(1, set.size());

        // Pop off row. State:  
        r = set.getRow();
        i = rm.indexOfValue("ROWNR");
        assertEquals(5L, ((Long)r[i]).longValue());  
        // TODO assertTrue(set.isEmpty());
        // TODO assertTrue(!set.isFull());
        assertEquals(0, set.size());

        /*********************************************************************
         *  This was changed v2.5.0 with a new RowSet         
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
        **********************************************************************/        
	}

	/**
	 * Names test. Just for completeness.
	 */
	public void testNames()
	{
		RowSet set = new BlockingRowSet(3);

        set.setThreadNameFromToCopy("from", 2, "to", 3);
        
        assertEquals("from", set.getOriginStepName());
        assertEquals(2, set.getOriginStepCopy());
        assertEquals("to", set.getDestinationStepName());
        assertEquals(3, set.getDestinationStepCopy());
        assertEquals(set.toString(), set.getName());
        assertEquals("from.2 - to.3", set.getName());
	}
}