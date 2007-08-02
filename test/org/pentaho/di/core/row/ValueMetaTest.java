package org.pentaho.di.core.row;

import junit.framework.TestCase;

/**
 * Test functionality in ValueMeta
 * 
 * @author sboden
 */
public class ValueMetaTest extends TestCase
{
	/**
	 * Compare to byte arrays for equality.
	 * 
	 * @param b1 1st byte array
	 * @param b2 2nd byte array
	 * 
	 * @return true if equal
	 */
	private boolean byteCompare(byte[] b1, byte[] b2)
	{
		if ( b1.length != b2.length )
		    return false;
		
		int idx = 0;
		while ( idx < b1.length )
		{
			if ( b1[idx] != b2[idx] )
				return false;
		    idx++;
		}
		return true;
	}
	
	public void testCvtStringToBinaryString() throws Exception
	{
		ValueMeta val1 = new ValueMeta("STR1", ValueMetaInterface.TYPE_STRING);
		val1.setLength(6);
		val1.setStringEncoding("UTF8");
		
		// No truncating or padding!!!
		byte b1[] = val1.getBinary("PDI123");
		assertTrue(byteCompare(b1, new byte[] { 'P', 'D', 'I', '1', '2', '3' }));
		
		byte b2[] = val1.getBinary("PDI");
		assertTrue(byteCompare(b2, new byte[] { 'P', 'D', 'I' }));
		
		byte b3[] = val1.getBinary("PDI123456");
		assertTrue(byteCompare(b3, new byte[] { 'P', 'D', 'I', '1', '2', '3', '4', '5', '6' }));
		
		ValueMeta val2 = new ValueMeta("STR2", ValueMetaInterface.TYPE_STRING);
		val2.setLength(1);
		
		byte b4[] = val2.getBinary("PDI123");
		assertTrue(byteCompare(b4, new byte[] { 'P', 'D', 'I', '1', '2', '3' }));
		
		byte b5[] = val2.getBinary("PDI");
		assertTrue(byteCompare(b5, new byte[] { 'P', 'D', 'I' }));
		
		byte b6[] = val2.getBinary("PDI123456");
		assertTrue(byteCompare(b6, new byte[] { 'P', 'D', 'I', '1', '2', '3', '4', '5', '6' }));	
	}
	
	public void testCvtStringBinaryString() throws Exception
	{		
		ValueMeta val1 = new ValueMeta("STR1", ValueMetaInterface.TYPE_STRING);
		val1.setLength(6);
		val1.setStringEncoding("UTF8");

		ValueMeta val2 = new ValueMeta("BINSTR1", ValueMetaInterface.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
		val2.setLength(6);
		val2.setStringEncoding("UTF8");		
		
		String str1 = val2.getString(val1.getBinary("PDI123"));
		assertTrue("PDI123".equals(str1));
		
		String str2 = val2.getString(val1.getBinary("PDI"));
		assertTrue("PDI".equals(str2));

		String str3 = val2.getString(val1.getBinary("PDI123456"));
		assertTrue("PDI123456".equals(str3));
	}	
}