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
 
package org.pentaho.di.compatibility;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueNumber.
 *
 * @author Sven Boden
 */
public class ValueBigNumberTest extends TestCase
{
	/**
	 * Constructor test 1.
	 */
	public void testConstructor1()
	{
		ValueBigNumber vs = new ValueBigNumber();

		assertEquals(Value.VALUE_TYPE_BIGNUMBER, vs.getType());
		assertEquals("BigNumber", vs.getTypeDesc());
		assertNull(vs.getBigNumber());
		assertEquals(-1, vs.getLength());
		assertEquals(-1, vs.getPrecision());

		ValueBigNumber vs1 = new ValueBigNumber(new BigDecimal(1.0));
 
		vs1.setLength(2);
		assertEquals(2, vs1.getLength());
		assertEquals(-1, vs1.getPrecision());

		vs1.setLength(4, 2);
		assertEquals(4, vs1.getLength());
		assertEquals(2, vs1.getPrecision());

		vs1.setPrecision(3);
		assertEquals(3, vs1.getPrecision());		
	}

	/**
	 * Test the getters of ValueBigNumber
	 */
	public void testGetters()
	{
		ValueBigNumber vs1 = new ValueBigNumber();
		ValueBigNumber vs2 = new ValueBigNumber(new BigDecimal(0.0D));
		ValueBigNumber vs3 = new ValueBigNumber(new BigDecimal(1.0D));

		assertEquals(false,  vs1.getBoolean());
		assertEquals(false, vs2.getBoolean());
		assertEquals(true,  vs3.getBoolean());

		assertEquals(null,  vs1.getString());
		assertEquals("0", vs2.getString());
		assertEquals("1", vs3.getString());
		
		assertEquals(0.0D, vs1.getNumber(), 0.001D);
		assertEquals(0.0D, vs2.getNumber(), 0.001D);
		assertEquals(1.0D, vs3.getNumber(), 0.001D);

		assertEquals(0L, vs1.getInteger());
		assertEquals(0L,  vs2.getInteger());
		assertEquals(1L,  vs3.getInteger());

		assertNull(vs1.getBigNumber());
		assertEquals(new BigDecimal(0L),  vs2.getBigNumber());
		assertEquals(new BigDecimal(1L),  vs3.getBigNumber());		
		
		assertNull(vs1.getDate());
		assertEquals(0L, vs2.getDate().getTime());
		assertEquals(1L, vs3.getDate().getTime());	
		
		assertNull(vs1.getSerializable());
		assertEquals(new BigDecimal(0.0D), vs2.getSerializable());		
		assertEquals(new BigDecimal(1.0D), vs3.getSerializable());
	}

	/**
	 * Test the setters of ValueBigNumber
	 */
	public void testSetters()
	{
		ValueBigNumber vs = new ValueBigNumber();
		
		vs.setString("unknown");
		assertEquals(new BigDecimal(0.0D), vs.getBigNumber());
		vs.setString("-4.0");
		assertEquals(new BigDecimal(-4), vs.getBigNumber());
		vs.setString("0.0");
		assertEquals(new BigDecimal(0), vs.getBigNumber());
		vs.setString("0");
		assertEquals(new BigDecimal(0), vs.getBigNumber());
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);		
	    Date dt = null;
	    try 
	    {
	       dt = format.parse("2006/06/07 01:02:03.004");
	    }
	    catch ( ParseException ex )
	    {
	       dt = null;	
	    }
		vs.setDate(dt);
		assertEquals(new BigDecimal(1.149634923004E12), vs.getBigNumber());

		vs.setBoolean(true);
		assertEquals(new BigDecimal(1.0D), vs.getBigNumber());
		vs.setBoolean(false);
		assertEquals(new BigDecimal(0.0D), vs.getBigNumber());

		vs.setNumber(5.0D);
		assertEquals(new BigDecimal(5.0D), vs.getBigNumber());
		vs.setNumber(0.0D);
		assertEquals(new BigDecimal(0.0D), vs.getBigNumber());
		
		vs.setInteger(5L);
		assertEquals(new BigDecimal(5.0D), vs.getBigNumber());
		vs.setInteger(0L);
		assertEquals(new BigDecimal(0.0D), vs.getBigNumber());

		vs.setBigNumber(new BigDecimal(5));
		assertEquals(5.0D, vs.getNumber(), 0.1D);
		vs.setBigNumber(new BigDecimal(0));
		assertEquals(0.0D, vs.getNumber(), 0.1D);
				
		// setSerializable is ignored ???
	}	
}
