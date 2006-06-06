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

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;
import be.ibridge.kettle.core.value.Value;

/**
 * Test class for the basic functionality of Row.
 *
 * Rows on the same stream in KETTLE are always assumed to be
 * of the same width.
 *
 * @author Sven Boden
 */
public class RowTest extends TestCase
{
	/**
	 * Constructor test 1. No params.
	 */
	public void testConstructor1()
	{
	    Row r = new Row();
	    assertTrue(!r.isIgnored());
	    assertNull(r.getLogdate());
	    assertEquals(0L, r.getLogtime());
	}

	/**
	 * Constructor test 2. Several cases.
	 */
	public void testConstructor2()
	{
		Row rEmpty = new Row();

	    Row r1 = new Row();
        r1.addValue(new Value("field1", "KETTLE"));
        r1.addValue(new Value("field2", 123L));
        r1.addValue(new Value("field3", true));
        r1.setIgnore();

        Row r2 = new Row(r1);
        Row r3 = new Row(rEmpty);

        assertEquals(0, rEmpty.size());
        assertEquals(r1.size(), r2.size());
        assertTrue(r1.equals(r2));
        assertTrue(r1.isIgnored());
        assertEquals(0, r3.size());
	}

	/**
	 * Test RemoveValue().
	 */
	public void testRemoveValue()
	{
		Value values[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45)),  // BigDecimal
			new Value("field6", new BigDecimal(123.60))   // BigDecimal
		};

	    Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}
		assertEquals(values.length, r1.size());


		r1.removeValue(0);
		assertEquals(values.length - 1, r1.size());
		assertEquals(values[1], r1.getValue(0));

		boolean removed;
		int idx;

		idx = r1.searchValueIndex("field5");
		assertTrue(idx > 0);
		removed = r1.removeValue("field5");
		idx = r1.searchValueIndex("field5");
		assertTrue(removed);
		assertTrue(idx < 0);
		assertEquals(values.length - 2, r1.size());

		// RemoveValue and duplicate values
		idx = r1.searchValueIndex("field6");
		assertTrue(idx > 0);
		removed = r1.removeValue("field6");
		idx = r1.searchValueIndex("field6");
		assertTrue(removed);
		assertTrue(idx > 0);
		assertEquals(values.length - 3, r1.size());

		idx = r1.searchValueIndex("field6");
		assertTrue(idx > 0);
		removed = r1.removeValue("field6");
		idx = r1.searchValueIndex("field6");
		assertTrue(removed);
		assertTrue(idx < 0);
		assertEquals(values.length - 4, r1.size());

		removed = r1.removeValue("field6");
		assertTrue(!removed);
	}

	/**
	 * Test clear().
	 */
	public void testClear()
	{
		Value values[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

	    Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}
		assertEquals(values.length, r1.size());

		r1.clear();
		assertEquals(0, r1.size());

		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}
		assertEquals(values.length, r1.size());
	}

	/**
	 * Test addRow().
	 */
	public void testAddRow()
	{
		Value values[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

		Value values1[] = {
			new Value("field1", "dupl"),                  // String
			new Value("field2", "string"),                // String
			new Value("field7", true),                    // Boolean
     	};

	    Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}
		assertEquals(values.length, r1.size());

	    Row r2 = new Row();
		for (int i=0; i < values1.length; i++ )
		{
			r2.addValue(values1[i]);
		}
		assertEquals(values1.length, r2.size());

		r1.addRow(r2);
		assertEquals(values.length + values1.length, r1.size());

		Value v1 = r1.getValue(values.length);
		assertEquals(values1[0], v1);

		Value v2 = r1.getValue(values.length+1);
		assertEquals(values1[1], v2);

		Value v3 = r1.getValue(values.length+2);
		assertEquals(values1[2], v3);
	}

	/**
	 * Test mergeRow().
	 */
	public void testMergeRow()
	{
		Value values[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

		Value values1[] = {
			new Value("field1", "dupl"),                  // String
			new Value("field2", "string"),                // String
			new Value("field7", true),                    // Boolean
     	};

	    Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}
		assertEquals(values.length, r1.size());

	    Row r2 = new Row();
		for (int i=0; i < values1.length; i++ )
		{
			r2.addValue(values1[i]);
		}
		assertEquals(values1.length, r2.size());

		r1.mergeRow(r2);
		assertEquals(values.length + 1, r1.size());

		Value v1 = r1.getValue(values.length);
		assertEquals(values1[2], v1);
	}

	/**
	 * Test getValue.
	 */
	public void testGetValue()
	{
		Value values[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

	    Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		for (int i=0; i < values.length; i++ )
		{
			Value v = r1.getValue(i);
			assertTrue(v.equals(values[i]));
		}

		//////////////////////////////////////////////////////////
		// If you're here because the build fails and you want
		// to change the following part, think again (Sven Boden).
		//
		// A Row returns an IndexOutOfBoundsException when the
		// requested value is not in the Row. If this would be
		// catched and a null returned, this check would slow
		// down processing a lot.
		//
		// It's also unusual to get it in KETTLE, as one of the
		// assumptions is that all Rows on a certain stream are
		// of the same width (same number of columns).
		//////////////////////////////////////////////////////////
		Value v = null;
		try  {
		    v = r1.getValue(values.length + 1);
		    fail("expected out of bounds exception");
		}
		catch ( IndexOutOfBoundsException e ) {	
			v = null;  // not important
		}

	}

	/**
	 * Test searchValueIndex.
	 */
	public void testSearchValueIndex()
	{
		Date date = new Date();

		Value values[] = {
			    new Value("field1", "KETTLE"),                // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", date),                    // Date
				new Value("field5", true),                    // Boolean
				new Value("field6", new BigDecimal(123.45)),  // BigDecimal
		};

		Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		int idx;
		idx = r1.searchValueIndex("field0");
		assertEquals(-1L, idx);

		idx = r1.searchValueIndex("field1");
		assertEquals(0, idx);

		idx = r1.searchValueIndex("field6");
		assertEquals(5, idx);

		Row r2 = new Row(r1);

		Value values1[] = {
  		    new Value("field8", "KETTLE "),               // String
		    new Value("field8", "KETTLE1"),               // String
		    new Value("field9 ", "KETTLE1")               // String
		};

		for (int i=0; i < values1.length; i++ )
		{
			r2.addValue(values1[i]);
		}

		idx = r2.searchValueIndex("field8");
		assertEquals(6, idx);

		idx = r2.searchValueIndex("field8");
		assertEquals(6, idx);

		idx = r2.searchValueIndex("field9 ");
		assertEquals(8, idx);
	}

	/**
	 * Test searchValue.
	 */
	public void testSearchValue()
	{
		Date date = new Date();

		Value values[] = {
			    new Value("field1", "KETTLE"),                // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", date),                    // Date
				new Value("field5", true),                    // Boolean
				new Value("field6", new BigDecimal(123.45)),  // BigDecimal
		};

		Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		Value v0 = r1.searchValue("field0");
		assertNull(v0);

		Value v1 = r1.searchValue("field1");
		assertEquals("field1", v1.getName());
		assertEquals("KETTLE", v1.getString());

		Value v2 = r1.searchValue("field2");
		assertEquals("field2", v2.getName());
		assertEquals(new Long(123L), new Long(v2.getInteger()));

		Value v3 = r1.searchValue("field3");
		assertEquals("field3", v3.getName());
		assertEquals(new Double(10.5D), new Double(v3.getNumber()));

		Value v4 = r1.searchValue("field4");
		assertEquals("field4", v4.getName());
		assertEquals(date, v4.getDate());

		Value v5 = r1.searchValue("field5");
		assertEquals("field5", v5.getName());
		assertEquals(true, v5.getBoolean());

		Value v6 = r1.searchValue("field6");
		assertEquals("field6", v6.getName());
		assertEquals(new BigDecimal(123.45), v6.getBigNumber());

		Row r2 = new Row(r1);

		Value values1[] = {
  		    new Value("field8", "KETTLE "),               // String
		    new Value("field8", "KETTLE1"),               // String
		    new Value("field9 ", "KETTLE1")               // String
		};

		for (int i=0; i < values1.length; i++ )
		{
			r2.addValue(values1[i]);
		}

		Value v21 = r2.searchValue("field8");
		assertEquals("field8", v21.getName());
		assertEquals("KETTLE ", v21.getString());

		/* Second value is never found */
		Value v22 = r2.searchValue("field8");
		assertEquals("field8", v22.getName());
		assertEquals("KETTLE ", v22.getString());

		Value v23 = r2.searchValue("field9 ");
		assertEquals("field9 ", v23.getName());
		assertEquals("KETTLE1", v23.getString());
	}

	/**
	 * Test isEmpty.
	 */
	public void testIsEmpty()
	{
		Row r0 = new Row();

	    Row r1 = new Row();
        r1.addValue(new Value("field1", "KETTLE"));
        r1.addValue(new Value("field2", 123L));
        r1.addValue(new Value("field3", true));

        Row r2 = new Row();
        r2.addValue(new Value("field1", (String)null));
        r2.addValue(new Value("field2", (String)null));

        Row r3 = new Row();
        r3.addValue(new Value("field1", "KETTLE"));
        r3.addValue(new Value("field2", (String)null));

        assertTrue(r0.isEmpty());            /* row with no value is empty */
        assertFalse(r1.isEmpty());			 /* normal row */
        assertTrue(r2.isEmpty());			 /* all values are NULL => empty */
        assertFalse(r3.isEmpty());           /* some values are non null => not empty */
	}

	/**
	 * Test getFieldNames.
	 */
	public void testGetFieldNames()
	{
		Row r0 = new Row();

		Value values[] = {
			    new Value("field1", "KETTLE"),                // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", new Date()),              // Date
				new Value("field5", true),                    // Boolean
				new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

		Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		String fieldNames[] = r1.getFieldNames();
		for (int i=0; i < fieldNames.length; i++ )
		{
			assertEquals(fieldNames[i], values[i].getName());
		}
		assertEquals(fieldNames.length, values.length);

		String fieldNames1[] = r0.getFieldNames();
		assertEquals(0L, fieldNames1.length);
	}

	/**
	 * Test toString().
	 */
	public void testToString()
	{
		Row r0 = new Row();

		Value values[] = {
			    new Value("field1", "KETTLE"),                // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", true),                    // Boolean
				new Value("field5", new BigDecimal(123.0)),   // BigDecimal
				new Value("field6", (String)null),            // NULL value
				null                                          // null
		};

		Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		assertEquals("[]", r0.toString());
		assertEquals("[field1=KETTLE, field2= 123, field3=10.5, field4=true, field5=123, field6=, NULL]", r1.toString());
	}

	/**
	 * Test toStringMeta().
	 */
	public void testToStringMeta()
	{
		Row r0 = new Row();

		Value values[] = {
			    new Value("field1", "KETTLE"),                // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", true),                    // Boolean
				new Value("field5", new BigDecimal(123.0)),   // BigDecimal
				new Value("field6", (String)null),            // NULL value
				null                                          // null
		};

		Row r1 = new Row();
		for (int i=0; i < values.length; i++ )
		{
			r1.addValue(values[i]);
		}

		assertEquals("[]", r0.toStringMeta());
		assertEquals("[field1(String), field2(Integer), field3(Number), field4(Boolean), field5(BigNumber), field6(String), NULL]", r1.toStringMeta());
	}
}
