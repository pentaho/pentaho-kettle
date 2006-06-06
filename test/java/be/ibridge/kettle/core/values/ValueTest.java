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
 
package be.ibridge.kettle.core.values;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import be.ibridge.kettle.core.value.Value;

/**
 * Test class for the basic functionality of Value.
 *
 * @author Sven Boden
 */
public class ValueTest extends TestCase
{
	/**
	 * Constructor test 1.
	 */
	public void testConstructor1()
	{
	    Value vs = new Value();
	    
	    // Set by clearValue()
	    assertFalse(vs.isNull());             // historical probably
	    assertTrue(vs.isEmpty());             // historical probably
	    assertEquals(null, vs.getName()); 
	    assertEquals(null, vs.getOrigin());
	    assertEquals(Value.VALUE_TYPE_NONE, vs.getType());
	    assertFalse(vs.isString());
	    assertFalse(vs.isDate());
	    assertFalse(vs.isNumeric());
	    assertFalse(vs.isInteger());
	    assertFalse(vs.isBigNumber());
	    assertFalse(vs.isNumber());
	    assertFalse(vs.isBoolean());
	    
	    Value vs1 = new Value("Name");
	    
	    // Set by clearValue()
	    assertFalse(vs1.isNull());            // historical probably
	    assertTrue(vs1.isEmpty());            // historical probably
	    assertEquals("Name", vs1.getName()); 
	    assertEquals(null, vs1.getOrigin());
	    assertEquals(Value.VALUE_TYPE_NONE, vs1.getType());	    
	}

	/**
	 * Constructor test 2.
	 */
	public void testConstructor2()
	{
	    Value vs = new Value("Name",  Value.VALUE_TYPE_NUMBER);	    
	    assertFalse(vs.isNull());
	    assertFalse(vs.isEmpty());
	    assertEquals("Name", vs.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_NUMBER, vs.getType());
	    assertTrue(vs.isNumber());
	    assertTrue(vs.isNumeric());

	    Value vs1= new Value("Name",  Value.VALUE_TYPE_STRING);	    
	    assertFalse(vs1.isNull());
	    assertFalse(vs1.isEmpty());
	    assertEquals("Name", vs1.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_STRING, vs1.getType());
	    assertTrue(vs1.isString());

	    Value vs2= new Value("Name",  Value.VALUE_TYPE_DATE);	    
	    assertFalse(vs2.isNull());
	    assertFalse(vs2.isEmpty());
	    assertEquals("Name", vs2.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_DATE, vs2.getType());
	    assertTrue(vs2.isDate());

	    Value vs3= new Value("Name",  Value.VALUE_TYPE_BOOLEAN);	    
	    assertFalse(vs3.isNull());
	    assertFalse(vs3.isEmpty());
	    assertEquals("Name", vs3.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_BOOLEAN, vs3.getType());
	    assertTrue(vs3.isBoolean());	

	    Value vs4= new Value("Name",  Value.VALUE_TYPE_INTEGER);	    
	    assertFalse(vs4.isNull());
	    assertFalse(vs4.isEmpty());
	    assertEquals("Name", vs4.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_INTEGER, vs4.getType());
	    assertTrue(vs4.isInteger());	    
	    assertTrue(vs4.isNumeric());

	    Value vs5= new Value("Name",  Value.VALUE_TYPE_BIGNUMBER);	    
	    assertFalse(vs5.isNull());
	    assertFalse(vs5.isEmpty());
	    assertEquals("Name", vs5.getName()); 	 
	    assertEquals(Value.VALUE_TYPE_BIGNUMBER, vs5.getType());
	    assertTrue(vs5.isBigNumber());	    
	    assertTrue(vs5.isNumeric());

	    Value vs6= new Value("Name", 1000000);
	    assertEquals(Value.VALUE_TYPE_NONE, vs6.getType());
	}

	/**
	 * Constructors using Values
	 */
	public void testConstructor3()
	{
	    Value vs = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs.setValue(10.0D);
	    vs.setOrigin("origin");
	    vs.setLength(4, 2);
	    Value copy = new Value(vs);
	    assertEquals(vs.getType(), copy.getType());
	    assertEquals(vs.getNumber(), copy.getNumber(), 0.1D);
	    assertEquals(vs.getLength(), copy.getLength());
	    assertEquals(vs.getPrecision(), copy.getPrecision());
	    assertEquals(vs.isNull(), copy.isNull());
	    assertEquals(vs.getOrigin(), copy.getOrigin());
	    assertEquals(vs.getName(), copy.getName());
	    
	    // Show it's a deep copy
	    copy.setName("newName");
	    assertEquals("Name", vs.getName());
	    assertEquals("newName", copy.getName());

	    copy.setOrigin("newOrigin");
	    assertEquals("origin", vs.getOrigin());
	    assertEquals("newOrigin", copy.getOrigin());
	    
	    copy.setValue(11.0D);
	    assertEquals(10.0D, vs.getNumber(), 0.1D);
	    assertEquals(11.0D, copy.getNumber(), 0.1D);

	
	    Value vs1 = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs1.setName(null);
	    // name and origin are null
	    Value copy1 = new Value(vs1);
	    assertEquals(vs1.getType(), copy1.getType());
	    assertEquals(vs1.getNumber(), copy1.getNumber(), 0.1D);
	    assertEquals(vs1.getLength(), copy1.getLength());
	    assertEquals(vs1.getPrecision(), copy1.getPrecision());
	    assertEquals(vs1.isNull(), copy1.isNull());
	    assertEquals(vs1.getOrigin(), copy1.getOrigin());
	    assertEquals(vs1.getName(), copy1.getName());
	    
	    Value vs2 = new Value((Value)null);
	    assertTrue(vs2.isNull());
	    assertNull(vs2.getName());
	    assertNull(vs2.getOrigin());
	}

	/**
	 * Constructors using Values
	 */
	public void testConstructor4()
	{
	    Value vs = new Value("Name", new StringBuffer("buffer"));
	    assertEquals(Value.VALUE_TYPE_STRING, vs.getType());
	    assertEquals("buffer", vs.getString());
	}

	/**
	 * Constructors using Values
	 */
	public void testConstructor5()
	{
	    Value vs = new Value("Name", 10.0D);
	    assertEquals(Value.VALUE_TYPE_NUMBER, vs.getType());
	    assertEquals("Name", vs.getName());
	    
	    Value copy = new Value("newName", vs);
	    assertEquals("newName", copy.getName());
	    assertFalse(!vs.equals(copy));
	    copy.setName("Name");
	    assertTrue(vs.equals(copy));
	}
	
	/**
	 * Test of string representaion of Value.
	 */
	public void testToStringString()
	{
		String result = null;
		
	    Value vs = new Value("Name", Value.VALUE_TYPE_STRING);	    
	    vs.setValue("test string");
	    result = vs.toString(true);
	    assertEquals("test string", result);
	    
	    vs.setLength(20);
	    result = vs.toString(true);  // padding
	    assertEquals("test string         ", result);
	    
	    vs.setLength(4);              
	    result = vs.toString(true);  // truncate
	    assertEquals("test", result);

	    vs.setLength(0);              
	    result = vs.toString(true);  // on 0 => full string
	    assertEquals("test string", result);
	    
	    // no padding
	    result = vs.toString(false);
	    assertEquals("test string", result);
	    
	    vs.setLength(20);
	    result = vs.toString(false); 
	    assertEquals("test string", result);
	    
	    vs.setLength(4);              
	    result = vs.toString(false); 
	    assertEquals("test string", result);

	    vs.setLength(0);              
	    result = vs.toString(false);
	    assertEquals("test string", result);
	    
	    vs.setLength(4);              
	    vs.setNull(); 
	    result = vs.toString(false);
	    assertEquals("", result);
	    
	    Value vs1 = new Value("Name", Value.VALUE_TYPE_STRING);
	    assertEquals("", vs1.toString());
	}

	/**
	 * Test of boolean representation of Value.
	 */
	public void testToStringBoolean()
	{
		String result = null;
		
	    Value vs = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs.setValue(true);
	    result = vs.toString(true);
	    assertEquals("true", result);
	    
	    Value vs1 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs1.setValue(false);
	    result = vs1.toString(true);
	    assertEquals("false", result);
	    
	    // set to "null"
	    Value vs2 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs2.setValue(true);
	    vs2.setNull();
	    result = vs2.toString(true);
	    assertEquals("", result);

	    // set to "null"
	    Value vs3 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs3.setValue(false);
	    vs3.setNull();
	    result = vs3.toString(true);
	    assertEquals("", result);		    
	    
	    // set to length = 1 => get Y/N
	    Value vs4 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs4.setValue(true); 
	    vs4.setLength(1);
	    result = vs4.toString(true);
	    assertEquals("true", result);

	    // set to length = 1 => get Y/N
	    Value vs5 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs5.setValue(false);
	    vs5.setLength(1);
	    result = vs5.toString(true);
	    assertEquals("false", result);

	    // set to length > 1 => get true/false
	    Value vs6 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs6.setValue(true); 
	    vs6.setLength(3);
	    result = vs6.toString(true);
	    assertEquals("true", result);

	    // set to length > 1 => get true/false (+ truncation)
	    Value vs7 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);	    
	    vs7.setValue(false);
	    vs7.setLength(3);
	    result = vs7.toString(true);
	    assertEquals("false", result);	    	    
	}		

	/**
	 * Test of boolean representation of Value.
	 */
	public void testToStringDate()
	{
		String result = null;
		
	    Value vs1 = new Value("Name", Value.VALUE_TYPE_DATE);	    
	    result = vs1.toString(true);
	    assertEquals("", result);

	    Value vs2 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs2.setNull(true);
	    result = vs2.toString(true);
	    assertEquals("", result);

	    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	    Date dt = df.parse("2006/03/01 17:01:02.005", new ParsePosition(0));
	    
	    Value vs3 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs3.setValue(dt);
	    result = vs3.toString(true);
	    assertEquals("2006/03/01 17:01:02.005", result);	
	    
	    Value vs4 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs4.setNull(true);
	    vs4.setLength(2);
	    result = vs4.toString(true);
	    assertEquals("", result);	

	    Value vs5 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs3.setValue(dt);
	    vs5.setLength(10);
	    result = vs5.toString(true);
	    assertEquals("", result);	   
	}	
	
	public void testToStringMeta()
	{
		String result = null;
		
		// Strings
	    Value vs = new Value("Name", Value.VALUE_TYPE_STRING);
	    vs.setValue("test");	   
	    result = vs.toStringMeta();
	    assertEquals("String", result);

	    Value vs1= new Value("Name", Value.VALUE_TYPE_STRING);
	    vs1.setValue("test");
	    vs1.setLength(0);
	    result = vs1.toStringMeta();
	    assertEquals("String", result);

	    Value vs2= new Value("Name", Value.VALUE_TYPE_STRING);
	    vs2.setValue("test");
	    vs2.setLength(4);
	    result = vs2.toStringMeta();
	    assertEquals("String(4)", result);	    
	   
	    
		// Booleans: not affected by length on output
	    Value vs3 = new Value("Name", Value.VALUE_TYPE_BOOLEAN);
	    vs3.setValue(false);	   
	    result = vs3.toStringMeta();
	    assertEquals("Boolean", result);

	    Value vs4= new Value("Name", Value.VALUE_TYPE_BOOLEAN);
	    vs4.setValue(false);
	    vs4.setLength(0);
	    result = vs4.toStringMeta();
	    assertEquals("Boolean", result);

	    Value vs5= new Value("Name", Value.VALUE_TYPE_BOOLEAN);
	    vs5.setValue(false);
	    vs5.setLength(4);
	    result = vs5.toStringMeta();
	    assertEquals("Boolean", result);	  
	    
	    
		// Integers
	    Value vs6 = new Value("Name", Value.VALUE_TYPE_INTEGER);
	    vs6.setValue(10);	   
	    result = vs6.toStringMeta();
	    assertEquals("Integer", result);

	    Value vs7= new Value("Name", Value.VALUE_TYPE_INTEGER);
	    vs7.setValue(10);
	    vs7.setLength(0);
	    result = vs7.toStringMeta();
	    assertEquals("Integer", result);

	    Value vs8= new Value("Name", Value.VALUE_TYPE_INTEGER);
	    vs8.setValue(10);
	    vs8.setLength(4);
	    result = vs8.toStringMeta();
	    assertEquals("Integer(4)", result);	   
	    
	    
		// Numbers
	    Value vs9 = new Value("Name", Value.VALUE_TYPE_NUMBER);
	    vs9.setValue(10.0D);	   
	    result = vs9.toStringMeta();
	    assertEquals("Number", result);

	    Value vs10 = new Value("Name", Value.VALUE_TYPE_NUMBER);
	    vs10.setValue(10.0D);
	    vs10.setLength(0);
	    result = vs10.toStringMeta();
	    assertEquals("Number", result);

	    Value vs11 = new Value("Name", Value.VALUE_TYPE_NUMBER);
	    vs11.setValue(10.0D);
	    vs11.setLength(4);
	    result = vs11.toStringMeta();
	    assertEquals("Number(4)", result);
	    
	    Value vs12 = new Value("Name", Value.VALUE_TYPE_NUMBER);
	    vs12.setValue(10.0D);
	    vs12.setLength(4);
	    vs12.setPrecision(2);
	    result = vs12.toStringMeta();
	    assertEquals("Number(4, 2)", result);
	    
	    
	    // BigNumber
	    Value vs13 = new Value("Name", Value.VALUE_TYPE_BIGNUMBER);
	    vs13.setValue(new BigDecimal(10));	   
	    result = vs13.toStringMeta();
	    assertEquals("BigNumber", result);

	    Value vs14 = new Value("Name", Value.VALUE_TYPE_BIGNUMBER);
	    vs14.setValue(new BigDecimal(10));
	    vs14.setLength(0);
	    result = vs14.toStringMeta();
	    assertEquals("BigNumber", result);

	    Value vs15 = new Value("Name", Value.VALUE_TYPE_BIGNUMBER);
	    vs15.setValue(new BigDecimal(10));
	    vs15.setLength(4);
	    result = vs15.toStringMeta();
	    assertEquals("BigNumber(4)", result);
	    
	    Value vs16 = new Value("Name", Value.VALUE_TYPE_BIGNUMBER);
	    vs16.setValue(new BigDecimal(10));
	    vs16.setLength(4);
	    vs16.setPrecision(2);
	    result = vs16.toStringMeta();
	    assertEquals("BigNumber(4, 2)", result);

	    
	    // Date
	    Value vs17 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs17.setValue(new Date());	   
	    result = vs17.toStringMeta();
	    assertEquals("Date", result);

	    Value vs18 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs18.setValue(new Date());
	    vs18.setLength(0);
	    result = vs18.toStringMeta();
	    assertEquals("Date", result);

	    Value vs19 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs19.setValue(new Date());
	    vs19.setLength(4);
	    result = vs19.toStringMeta();
	    assertEquals("Date", result);
	    
	    Value vs20 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs20.setValue(new Date());
	    vs20.setLength(4);
	    vs20.setPrecision(2);
	    result = vs20.toStringMeta();
	    assertEquals("Date", result);	   	    
	}

	/**
	 * Constructors using Values.
	 */
	public void testClone1()
	{
	    Value vs = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs.setValue(10.0D);
	    vs.setOrigin("origin");
	    vs.setLength(4, 2);
	    Value copy = (Value)vs.Clone();
	    assertEquals(vs.getType(), copy.getType());
	    assertEquals(vs.getNumber(), copy.getNumber(), 0.1D);
	    assertEquals(vs.getLength(), copy.getLength());
	    assertEquals(vs.getPrecision(), copy.getPrecision());
	    assertEquals(vs.isNull(), copy.isNull());
	    assertEquals(vs.getOrigin(), copy.getOrigin());
	    assertEquals(vs.getName(), copy.getName());
	    
	    // Show it's a deep copy
	    copy.setName("newName");
	    assertEquals("Name", vs.getName());
	    assertEquals("newName", copy.getName());

	    copy.setOrigin("newOrigin");
	    assertEquals("origin", vs.getOrigin());
	    assertEquals("newOrigin", copy.getOrigin());
	    
	    copy.setValue(11.0D);
	    assertEquals(10.0D, vs.getNumber(), 0.1D);
	    assertEquals(11.0D, copy.getNumber(), 0.1D);

	
	    Value vs1 = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs1.setName(null);
	    // name and origin are null
	    Value copy1 = new Value(vs1);
	    assertEquals(vs1.getType(), copy1.getType());
	    assertEquals(vs1.getNumber(), copy1.getNumber(), 0.1D);
	    assertEquals(vs1.getLength(), copy1.getLength());
	    assertEquals(vs1.getPrecision(), copy1.getPrecision());
	    assertEquals(vs1.isNull(), copy1.isNull());
	    assertEquals(vs1.getOrigin(), copy1.getOrigin());
	    assertEquals(vs1.getName(), copy1.getName());
	    
	    Value vs2 = new Value((Value)null);
	    assertTrue(vs2.isNull());
	    assertNull(vs2.getName());
	    assertNull(vs2.getOrigin());
	}
	
	/**
	 * Test of getStringLength().
	 *
	 */
	public void testGetStringLength()
	{
		int result = 0;
		
	    Value vs1 = new Value("Name", Value.VALUE_TYPE_STRING);
	    result = vs1.getStringLength();
	    assertEquals(0, result);
	    
	    Value vs2 = new Value("Name", Value.VALUE_TYPE_STRING);
	    vs2.setNull();
	    result = vs2.getStringLength();
	    assertEquals(0, result);		

	    Value vs3 = new Value("Name", Value.VALUE_TYPE_STRING);
	    vs3.setValue("stringlength");
	    result = vs3.getStringLength();
	    assertEquals(12, result);	    
	}
	
	public void testGetXML()
	{
		String result = null;

	    Value vs1= new Value("Name", Value.VALUE_TYPE_STRING);
	    vs1.setValue("test");
	    vs1.setLength(4);
	    vs1.setPrecision(2);
	    result = vs1.getXML();
	    assertEquals("<name>Name</name><type>String</type><text>test</text><length>4</length><precision>-1</precision><isnull>N</isnull>", result);	    

	    Value vs2= new Value("Name", Value.VALUE_TYPE_BOOLEAN);
	    vs2.setValue(false);
	    vs2.setLength(4);
	    vs2.setPrecision(2);
	    result = vs2.getXML();
	    assertEquals("<name>Name</name><type>Boolean</type><text>false</text><length>-1</length><precision>-1</precision><isnull>N</isnull>", result);	  	   	 

	    Value vs3= new Value("Name", Value.VALUE_TYPE_INTEGER);
	    vs3.setValue(10);
	    vs3.setLength(4);
	    vs3.setPrecision(2);
	    result = vs3.getXML();
	    assertEquals("<name>Name</name><type>Integer</type><text> 0010</text><length>4</length><precision>0</precision><isnull>N</isnull>", result);	       
	    
	    Value vs4 = new Value("Name", Value.VALUE_TYPE_NUMBER);
	    vs4.setValue(10.0D);
	    vs4.setLength(4);
	    vs4.setPrecision(2);
	    result = vs4.getXML();
	    assertEquals("<name>Name</name><type>Number</type><text>10.0</text><length>4</length><precision>2</precision><isnull>N</isnull>", result);
	    	    
	    Value vs5 = new Value("Name", Value.VALUE_TYPE_BIGNUMBER);
	    vs5.setValue(new BigDecimal(10));
	    vs5.setLength(4);
	    vs5.setPrecision(2);
	    result = vs5.getXML();
	    assertEquals("<name>Name</name><type>BigNumber</type><text>10</text><length>4</length><precision>2</precision><isnull>N</isnull>", result);

	    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	    Date dt = df.parse("2006/03/01 17:01:02.005", new ParsePosition(0));
	    Value vs6 = new Value("Name", Value.VALUE_TYPE_DATE);
	    vs6.setValue(dt);
	    vs6.setLength(4);
	    vs6.setPrecision(2);
	    result = vs6.getXML();
	    assertEquals("<name>Name</name><type>Date</type><text>2006/03/01 17:01:02.005</text><length>-1</length><precision>-1</precision><isnull>N</isnull>", result);	   	    		
	}

	/**
	 * Test of setValue()
	 */
	public void testSetValue()
	{
	    Value vs = new Value("Name",  Value.VALUE_TYPE_INTEGER);
	    vs.setValue(100L);
	    vs.setOrigin("origin");

	    Value vs1 = new Value((Value)null);
	    assertTrue(vs1.isNull());          
	    assertTrue(vs1.isEmpty());             
	    assertEquals(null, vs1.getName()); 
	    assertNull(vs1.getOrigin());
	    assertEquals(Value.VALUE_TYPE_NONE, vs1.getType());
	    
	    Value vs2 = new Value("newName", Value.VALUE_TYPE_INTEGER);
	    vs2.setOrigin("origin1");
	    vs2.setValue(vs);	    
	    assertEquals("origin", vs2.getOrigin());
	    assertEquals(vs.getInteger(), vs2.getInteger());	    
	}
		
	/**
	 * Stuff which we didn't get in other checks.
	 */
	public void testLooseEnds()
	{
		assertEquals(Value.VALUE_TYPE_NONE, Value.getType("INVALID_TYPE"));
		assertEquals("String", Value.getTypeDesc(Value.VALUE_TYPE_STRING));
	}

	
	/**
	 * Constructors using Values DEBUG CLONE
	 *
	public void testClone2()
	{
	    Value vs = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs.setValue(10.0D);
	    vs.setOrigin("origin");
	    vs.setLength(4, 2);
	    Value copy = (Value)vs.clone();
	    assertEquals(vs.getType(), copy.getType());
	    assertEquals(vs.getNumber(), copy.getNumber(), 0.1D);
	    assertEquals(vs.getLength(), copy.getLength());
	    assertEquals(vs.getPrecision(), copy.getPrecision());
	    assertEquals(vs.isNull(), copy.isNull());
	    assertEquals(vs.getOrigin(), copy.getOrigin());
	    assertEquals(vs.getName(), copy.getName());
	    
	    // Show it's a deep copy
	    copy.setName("newName");
	    assertEquals("Name", vs.getName());
	    assertEquals("newName", copy.getName());

	    copy.setOrigin("newOrigin");
	    assertEquals("origin", vs.getOrigin());
	    assertEquals("newOrigin", copy.getOrigin());
	    
	    copy.setValue(11.0D);
	    assertEquals(10.0D, vs.getNumber(), 0.1D);
	    assertEquals(11.0D, copy.getNumber(), 0.1D);

	
	    Value vs1 = new Value("Name",  Value.VALUE_TYPE_NUMBER);
	    vs1.setName(null);
	    // name and origin are null
	    Value copy1 = new Value(vs1);
	    assertEquals(vs1.getType(), copy1.getType());
	    assertEquals(vs1.getNumber(), copy1.getNumber(), 0.1D);
	    assertEquals(vs1.getLength(), copy1.getLength());
	    assertEquals(vs1.getPrecision(), copy1.getPrecision());
	    assertEquals(vs1.isNull(), copy1.isNull());
	    assertEquals(vs1.getOrigin(), copy1.getOrigin());
	    assertEquals(vs1.getName(), copy1.getName());
	    
	    Value vs2 = new Value((Value)null);
	    assertTrue(vs2.isNull());
	    assertNull(vs2.getName());
	    assertNull(vs2.getOrigin());
	}
*/	
		
	// Value.clone returns shallow copies of Value, is this intended 
	// behaviour.
}
