package org.pentaho.di.core.row;

import java.math.BigDecimal;
import java.util.Date;

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
	
	public void testIntegerToStringToInteger() throws Exception
	{
		ValueMetaInterface intValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_INTEGER);
		intValueMeta.setLength(7);
		
		Long originalValue = new Long(123L);
		
		String string = intValueMeta.getString(originalValue);
		
		assertEquals(" 0000123", string);
		
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		strValueMeta.setStorageMetadata(intValueMeta);
		
		Long x = (Long) strValueMeta.convertDataUsingStorageMetaData(string);
		
		assertEquals(originalValue, x);
	}
	
	
	public void testNumberToStringToNumber() throws Exception
	{
		ValueMetaInterface numValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_NUMBER);
		numValueMeta.setLength(7,3);
		numValueMeta.setDecimalSymbol(",");
		numValueMeta.setGroupingSymbol(".");
		
		Double originalValue = new Double(123.456);
		
		String string = numValueMeta.getString(originalValue);
		
		assertEquals(" 0123,456", string);
		
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		strValueMeta.setStorageMetadata(numValueMeta);
		
		Double x = (Double) strValueMeta.convertDataUsingStorageMetaData(string);
		
		assertEquals(originalValue, x);
	}
	
	public void testBigNumberToStringToBigNumber() throws Exception
	{
		ValueMetaInterface numValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_BIGNUMBER);
		numValueMeta.setLength(42,9);
		numValueMeta.setDecimalSymbol(",");
		numValueMeta.setGroupingSymbol(".");
		
		BigDecimal originalValue = new BigDecimal("34039423484343123.443489056");
		
		String string = numValueMeta.getString(originalValue);
		
		assertEquals("34039423484343123.443489056", string);
		
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		strValueMeta.setStorageMetadata(numValueMeta);
		
		BigDecimal x = (BigDecimal) strValueMeta.convertDataUsingStorageMetaData(string);
		
		assertEquals(originalValue, x);
	}
	
	public void testDateToStringToDate() throws Exception
	{
		ValueMetaInterface datValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_DATE);
		datValueMeta.setConversionMask("yyyy - MM - dd   HH:mm:ss'('SSS')'");
		
		Date originalValue = new Date(7258114799999L);
		
		String string = datValueMeta.getString(originalValue);
		
		assertEquals("2199 - 12 - 31   23:59:59(999)", string);
		
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		strValueMeta.setStorageMetadata(datValueMeta);
		
		Date x = (Date) strValueMeta.convertDataUsingStorageMetaData(string);
		
		assertEquals(originalValue, x);
	}
	
	public void testConvertDataDate() throws Exception
	{
		ValueMetaInterface source = new ValueMeta("src", ValueMetaInterface.TYPE_STRING);
		source.setConversionMask("SSS.ss:mm:HH dd/MM/yyyy");
		ValueMetaInterface target = new ValueMeta("tgt", ValueMetaInterface.TYPE_DATE);
		
		Date date = (Date) target.convertData(source, "999.59:59:23 31/12/2007");
		assertEquals(new Date(1199141999999L), date);
		
		target.setConversionMask("yy/MM/dd HH:mm");
		
		String string = (String) source.convertData(target, date);
		assertEquals("07/12/31 23:59", string);
		
	}

	public void testConvertDataInteger() throws Exception
	{
		ValueMetaInterface source = new ValueMeta("src", ValueMetaInterface.TYPE_STRING);
		source.setConversionMask("###,###,##0.000");
		source.setLength(12,3);
		source.setDecimalSymbol(",");
		source.setGroupingSymbol(".");
		ValueMetaInterface target = new ValueMeta("tgt", ValueMetaInterface.TYPE_NUMBER);
		
		Double d = (Double) target.convertData(source, "123.456.789,012");
		assertEquals(123456789.012, d);
		
		target.setConversionMask("###,###,##0.00");
		source.setLength(12,4);
		source.setDecimalSymbol(".");
		source.setGroupingSymbol("''");
		
		String string = (String) source.convertData(target, d);
		assertEquals("123.456.789,01", string);
		
	}

}