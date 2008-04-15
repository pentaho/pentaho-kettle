package org.pentaho.di.core.row;


import junit.framework.TestCase;

import org.pentaho.di.core.exception.KettleValueException;

/**
 * Not yet completely finished.
 * 
 * @author sboden
 *
 */
public class ValueDataUtilTest extends TestCase
{
    /**
     * @deprecated Use {@link Const#ltrim(String)} instead
     * @throws KettleValueException
     */
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

    /**
     * @deprecated Use {@link Const#rtrim(String)} instead
     * @throws KettleValueException
     */
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

    /**
     * @deprecated Use {@link Const#isSpace(char)} instead
     * @throws KettleValueException
     */
    public void testIsSpace() throws KettleValueException
    {
    	assertTrue(ValueDataUtil.isSpace(' '));
    	assertTrue(ValueDataUtil.isSpace('\t'));
    	assertTrue(ValueDataUtil.isSpace('\r'));
    	assertTrue(ValueDataUtil.isSpace('\n'));
    	
    	assertFalse(ValueDataUtil.isSpace('S'));
    	assertFalse(ValueDataUtil.isSpace('b'));
    }
    
    /**
     * @deprecated Use {@link Const#trim(String)} instead
     * @throws KettleValueException
     */
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