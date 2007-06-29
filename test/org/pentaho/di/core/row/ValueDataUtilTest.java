package org.pentaho.di.core.row;


import junit.framework.TestCase;

import org.pentaho.di.core.exception.KettleValueException;

/**
 * Not yet completely finished.
 * 
 * @author sboden
 */
public class ValueDataUtilTest extends TestCase
{
    public void testLeftTrim() throws KettleValueException
    {
    	assertEquals("", ValueDataUtil.leftTrim(""));
    	assertEquals("string", ValueDataUtil.leftTrim("string"));
    	assertEquals("string", ValueDataUtil.leftTrim(" string"));
    	assertEquals("string", ValueDataUtil.leftTrim("  string"));
    	assertEquals("string", ValueDataUtil.leftTrim("   string"));
    	assertEquals("string", ValueDataUtil.leftTrim("     string"));
    	
    	assertEquals("string ", ValueDataUtil.leftTrim(" string "));
    	assertEquals("string  ", ValueDataUtil.leftTrim("  string  "));
    	assertEquals("string   ", ValueDataUtil.leftTrim("   string   "));
    	assertEquals("string    ", ValueDataUtil.leftTrim("    string    "));
    	
    	assertEquals("", ValueDataUtil.leftTrim(" "));
    	assertEquals("", ValueDataUtil.leftTrim("  "));
    	assertEquals("", ValueDataUtil.leftTrim("   "));    	
    }

    public void testRightTrim() throws KettleValueException
    {
    	assertEquals("", ValueDataUtil.rightTrim(""));
    	assertEquals("string", ValueDataUtil.rightTrim("string"));
    	assertEquals("string", ValueDataUtil.rightTrim("string "));
    	assertEquals("string", ValueDataUtil.rightTrim("string  "));
    	assertEquals("string", ValueDataUtil.rightTrim("string   "));
    	assertEquals("string", ValueDataUtil.rightTrim("string    "));
    	
    	assertEquals(" string", ValueDataUtil.rightTrim(" string "));
    	assertEquals("  string", ValueDataUtil.rightTrim("  string  "));
    	assertEquals("   string", ValueDataUtil.rightTrim("   string   "));
    	assertEquals("    string", ValueDataUtil.rightTrim("    string    "));

    	assertEquals("", ValueDataUtil.rightTrim(" "));
    	assertEquals("", ValueDataUtil.rightTrim("  "));
    	assertEquals("", ValueDataUtil.rightTrim("   "));    	    	
    }

    public void testIsSpace() throws KettleValueException
    {
    	assertTrue(ValueDataUtil.isSpace(' '));
    	assertTrue(ValueDataUtil.isSpace('\t'));
    	assertTrue(ValueDataUtil.isSpace('\r'));
    	assertTrue(ValueDataUtil.isSpace('\n'));
    	
    	assertFalse(ValueDataUtil.isSpace('S'));
    	assertFalse(ValueDataUtil.isSpace('b'));
    }
    
    public void testTrim() throws KettleValueException
    {
    	assertEquals("", ValueDataUtil.trim(""));
    	assertEquals("string", ValueDataUtil.trim("string"));
    	assertEquals("string", ValueDataUtil.trim("string "));
    	assertEquals("string", ValueDataUtil.trim("string  "));
    	assertEquals("string", ValueDataUtil.trim("string   "));
    	assertEquals("string", ValueDataUtil.trim("string    "));
    	
    	assertEquals("string", ValueDataUtil.trim(" string "));
    	assertEquals("string", ValueDataUtil.trim("  string  "));
    	assertEquals("string", ValueDataUtil.trim("   string   "));
    	assertEquals("string", ValueDataUtil.trim("    string    "));

    	assertEquals("string", ValueDataUtil.trim(" string"));
    	assertEquals("string", ValueDataUtil.trim("  string"));
    	assertEquals("string", ValueDataUtil.trim("   string"));
    	assertEquals("string", ValueDataUtil.trim("    string"));    	
    	
    	assertEquals("", ValueDataUtil.rightTrim(" "));
    	assertEquals("", ValueDataUtil.rightTrim("  "));
    	assertEquals("", ValueDataUtil.rightTrim("   "));    	    	
    }    
}