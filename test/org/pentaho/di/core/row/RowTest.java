/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.row;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class RowTest extends TestCase
{
    public void testNormalStringConversion() throws Exception
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Object[] rowData1 = new Object[] { "sampleString", fmt.parse("2007/05/07 13:04:13.203"), new Double(9123.00), new Long(12345), new BigDecimal("123456789012345678.9349"), Boolean.TRUE, };
        RowMetaInterface rowMeta1 = createTestRowMetaNormalStringConversion1();
        
        assertEquals("sampleString", rowMeta1.getString(rowData1, 0));        
        assertEquals("2007/05/07 13:04:13.203", rowMeta1.getString(rowData1, 1));        
        assertEquals("9,123.00", rowMeta1.getString(rowData1, 2));        
        assertEquals("0012345", rowMeta1.getString(rowData1, 3));        
        assertEquals("123456789012345678.9349", rowMeta1.getString(rowData1, 4));
        assertEquals("Y", rowMeta1.getString(rowData1, 5));
        
        fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        Object[] rowData2 = new Object[] { null, fmt.parse("20070507130413"), new Double(9123.9), new Long(12345), new BigDecimal("123456789012345678.9349"), Boolean.FALSE, };
        RowMetaInterface rowMeta2 = createTestRowMetaNormalStringConversion2();
        
        assertTrue( rowMeta2.getString(rowData2, 0)==null);        
        assertEquals("20070507130413", rowMeta2.getString(rowData2, 1));        
        assertEquals("9.123,9", rowMeta2.getString(rowData2, 2));        
        assertEquals("0012345", rowMeta2.getString(rowData2, 3));        
        assertEquals("123456789012345678.9349", rowMeta2.getString(rowData2, 4));
        assertEquals("false", rowMeta2.getString(rowData2, 5));
    }
    
    public void testIndexedStringConversion() throws Exception
    {
        String colors[] = new String[] { "Green", "Red", "Blue", "Yellow", null, };
        
        // create some timezone friendly dates
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date   dates[]  = new Date[]   { fmt.parse("2007/05/07 13:04:13.203"), 
        		null, 
        		fmt.parse("2007/05/05 05:15:49.349"), 
        		fmt.parse("2007/05/05 19:08:44.736"), };

        RowMetaInterface rowMeta = createTestRowMetaIndexedStringConversion1(colors, dates);

        Object[] rowData1 = new Object[] { Integer.valueOf(0), Integer.valueOf(0), }; 
        Object[] rowData2 = new Object[] { Integer.valueOf(1), Integer.valueOf(1), }; 
        Object[] rowData3 = new Object[] { Integer.valueOf(2), Integer.valueOf(2), }; 
        Object[] rowData4 = new Object[] { Integer.valueOf(3), Integer.valueOf(3), }; 
        Object[] rowData5 = new Object[] { Integer.valueOf(4), Integer.valueOf(0), }; 
        
        assertEquals("Green", rowMeta.getString(rowData1, 0));
        assertEquals("2007/05/07 13:04:13.203", rowMeta.getString(rowData1, 1));

        assertEquals("Red", rowMeta.getString(rowData2, 0));
        assertTrue(null == rowMeta.getString(rowData2, 1));

        assertEquals("Blue", rowMeta.getString(rowData3, 0));
        assertEquals("2007/05/05 05:15:49.349", rowMeta.getString(rowData3, 1));

        assertEquals("Yellow", rowMeta.getString(rowData4, 0));
        assertEquals("2007/05/05 19:08:44.736", rowMeta.getString(rowData4, 1));

        assertTrue(null == rowMeta.getString(rowData5, 0));
        assertEquals("2007/05/07 13:04:13.203", rowMeta.getString(rowData5, 1));
    }
    
    
    private RowMetaInterface createTestRowMetaNormalStringConversion1()
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30, 0);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        rowMeta.addValueMeta(meta2);

        ValueMetaInterface meta3 = new ValueMeta("numberValue", ValueMetaInterface.TYPE_NUMBER, 5, 2);
        meta3.setConversionMask("#,##0.00");
        meta3.setDecimalSymbol(".");
        meta3.setGroupingSymbol(",");
        rowMeta.addValueMeta(meta3);

        ValueMetaInterface meta4 = new ValueMeta("integerValue", ValueMetaInterface.TYPE_INTEGER, 7, 0);
        meta4.setConversionMask("0000000");
        meta4.setDecimalSymbol(".");
        meta4.setGroupingSymbol(",");
        rowMeta.addValueMeta(meta4);

        ValueMetaInterface meta5 = new ValueMeta("bigNumberValue", ValueMetaInterface.TYPE_BIGNUMBER, 30, 7);
        meta5.setDecimalSymbol(".");
        rowMeta.addValueMeta(meta5);

        ValueMetaInterface meta6 = new ValueMeta("booleanValue", ValueMetaInterface.TYPE_BOOLEAN);
        rowMeta.addValueMeta(meta6);

        return rowMeta;
    }
    
    private RowMetaInterface createTestRowMetaNormalStringConversion2()
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30, 0);
        meta1.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        meta2.setConversionMask("yyyyMMddHHmmss");
        rowMeta.addValueMeta(meta2);

        ValueMetaInterface meta3 = new ValueMeta("numberValue", ValueMetaInterface.TYPE_NUMBER, 5, 2);
        meta3.setConversionMask("###,##0.##");
        meta3.setDecimalSymbol(",");
        meta3.setGroupingSymbol(".");
        rowMeta.addValueMeta(meta3);

        ValueMetaInterface meta4 = new ValueMeta("integerValue", ValueMetaInterface.TYPE_INTEGER, 7, 0);
        meta4.setConversionMask("0000000");
        meta4.setDecimalSymbol(",");
        meta4.setGroupingSymbol(".");
        rowMeta.addValueMeta(meta4);

        ValueMetaInterface meta5 = new ValueMeta("bigNumberValue", ValueMetaInterface.TYPE_BIGNUMBER, 30, 7);
        meta5.setDecimalSymbol(".");
        rowMeta.addValueMeta(meta5);

        ValueMetaInterface meta6 = new ValueMeta("booleanValue", ValueMetaInterface.TYPE_BOOLEAN, 3, 0);
        rowMeta.addValueMeta(meta6);

        return rowMeta;
    }
    
    private RowMetaInterface createTestRowMetaIndexedStringConversion1(String[] colors, Date[] dates)
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object, indexed.
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30, 0);
        meta1.setIndex(colors);
        meta1.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        meta2.setIndex(dates);
        meta2.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        rowMeta.addValueMeta(meta2);

        return rowMeta;
    }
}
