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
		val2.setStorageMetadata(val1);
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
		strValueMeta.setConversionMetadata(intValueMeta);
		
		Long x = (Long) strValueMeta.convertDataUsingConversionMetaData(string);
		
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
		strValueMeta.setConversionMetadata(numValueMeta);
		
		Double x = (Double) strValueMeta.convertDataUsingConversionMetaData(string);
		
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
		strValueMeta.setConversionMetadata(numValueMeta);
		
		BigDecimal x = (BigDecimal) strValueMeta.convertDataUsingConversionMetaData(string);
		
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
		strValueMeta.setConversionMetadata(datValueMeta);
		
		Date x = (Date) strValueMeta.convertDataUsingConversionMetaData(string);
		
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
	
	/**
	 * Lazy conversion is used to read data from disk in a binary format.
	 * The data itself is not converted from the byte[] to Integer, rather left untouched until it's needed.
	 * 
	 * However at that time we do need it we should get the correct value back.
	 * @throws Exception
	 */
	public void testLazyConversionInteger() throws Exception
	{
		byte[] data = ("1234").getBytes();
		ValueMetaInterface intValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_INTEGER);
		intValueMeta.setLength(7);
		intValueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		intValueMeta.setStorageMetadata(strValueMeta);
		
		Long integerValue = intValueMeta.getInteger(data);
		assertEquals(new Long(1234L), integerValue);
		Double numberValue = intValueMeta.getNumber(data);
		assertEquals(new Double(1234), numberValue);
		BigDecimal bigNumberValue = intValueMeta.getBigNumber(data);
		assertEquals(new BigDecimal(1234), bigNumberValue);
		Date dateValue = intValueMeta.getDate(data);
		assertEquals(new Date(1234L), dateValue);
		String string = intValueMeta.getString(data);
		assertEquals(" 0001234", string);
	}

	/**
	 * Lazy conversion is used to read data from disk in a binary format.
	 * The data itself is not converted from the byte[] to Integer, rather left untouched until it's needed.
	 * 
	 * However at that time we do need it we should get the correct value back.
	 * @throws Exception
	 */
	public void testLazyConversionNumber() throws Exception
	{
		byte[] data = ("1,234.56").getBytes();
		ValueMetaInterface numValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_NUMBER);
		// The representation formatting options.
		//
		numValueMeta.setLength(12,4);
		numValueMeta.setDecimalSymbol(",");
		numValueMeta.setGroupingSymbol(".");
		numValueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
		
		// let's explain to the parser how the input data looks like. (the storage metadata)
		//
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		strValueMeta.setConversionMask("#,##0.00");
		strValueMeta.setDecimalSymbol(".");
		strValueMeta.setGroupingSymbol(",");
		numValueMeta.setStorageMetadata(strValueMeta);
		
		Long integerValue = numValueMeta.getInteger(data);
		assertEquals(new Long(1234L), integerValue);
		Double numberValue = numValueMeta.getNumber(data);
		assertEquals(new Double(1234.56), numberValue);
		BigDecimal bigNumberValue = numValueMeta.getBigNumber(data);
		assertEquals(new BigDecimal(1234.56), bigNumberValue);
		Date dateValue = numValueMeta.getDate(data);
		assertEquals(new Date(1234L), dateValue);
		String string = numValueMeta.getString(data);
		assertEquals(" 00001234,5600", string);
		
		// Get the binary data back : has to return exactly the same as we asked ONLY if the formatting options are the same
		// In this unit test they are not!
		//
		byte[] binaryValue = numValueMeta.getBinaryString(data);
		assertTrue(byteCompare((" 00001234,5600").getBytes(), binaryValue));
	}
	
	/**
	 * Lazy conversion is used to read data from disk in a binary format.
	 * The data itself is not converted from the byte[] to Integer, rather left untouched until it's needed.
	 * 
	 * However at that time we do need it we should get the correct value back.
	 * @throws Exception
	 */
	public void testLazyConversionBigNumber() throws Exception
	{
		String originalValue = "34983433433212304121900934.5634314343"; 
		byte[] data = originalValue.getBytes();
		ValueMetaInterface numValueMeta = new ValueMeta("i", ValueMetaInterface.TYPE_BIGNUMBER);
		// The representation formatting options.
		//
		numValueMeta.setLength(36,10);
		numValueMeta.setDecimalSymbol(",");
		numValueMeta.setGroupingSymbol(".");
		numValueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
		
		// let's explain to the parser how the input data looks like. (the storage metadata)
		//
		ValueMetaInterface strValueMeta = new ValueMeta("str", ValueMetaInterface.TYPE_STRING);
		numValueMeta.setStorageMetadata(strValueMeta);
		
		// NOTE This is obviously a number that is too large to fit into an Integer or a Number, but this is what we expect to come back.
		// Later it might be better to throw exceptions for big-number to integer conversion.
		// At the time of writing this unit test is not the case. 
		// -- Matt
		
		Long integerValue = numValueMeta.getInteger(data);
		assertEquals(new Long(-5045838617297571962L), integerValue); 
		Double numberValue = numValueMeta.getNumber(data);
		assertEquals(new Double("3.4983433433212304E25"), numberValue);
		BigDecimal bigNumberValue = numValueMeta.getBigNumber(data);
		assertEquals(new BigDecimal(originalValue), bigNumberValue);
		Date dateValue = numValueMeta.getDate(data);
		assertEquals(new Date(-5045838617297571962L), dateValue);
		String string = numValueMeta.getString(data);
		assertEquals(originalValue, string);
	}
}