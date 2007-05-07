package org.pentaho.pdi.core.row.test;

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.pdi.core.row.RowMeta;
import org.pentaho.pdi.core.row.RowMetaInterface;
import org.pentaho.pdi.core.row.ValueMeta;
import org.pentaho.pdi.core.row.ValueMetaInterface;

import be.ibridge.kettle.core.exception.KettleValueException;

import junit.framework.TestCase;

public class RowTest extends TestCase
{
    public void testStringConversion() throws KettleValueException
    {
        RowMetaInterface rowMeta = createTestRowMeta();

        Object[] rowData = new Object[] 
            { 
                "sampleString", 
                new Date(1178535853203L), 
                new Double(123.00), 
                new Integer(12345), 
                new BigDecimal("123456789012345678.9349"),
            };
        
        /*
        System.out.println(rowMeta.getString(rowData, 0));
        System.out.println(rowMeta.getString(rowData, 1));
        System.out.println(rowMeta.getString(rowData, 2));
        System.out.println(rowMeta.getString(rowData, 3));
        System.out.println(rowMeta.getString(rowData, 4));
        */
        
        assertEquals("sampleString", rowMeta.getString(rowData, 0));        
        assertEquals("2007/05/07 13:04:13.203", rowMeta.getString(rowData, 1));        
        assertEquals("123.00", rowMeta.getString(rowData, 2));        
        assertEquals("0012345", rowMeta.getString(rowData, 3));        
        assertEquals("00123456789012345680.0000000000", rowMeta.getString(rowData, 4));        
    }
    
    private RowMetaInterface createTestRowMeta()
    {
        RowMetaInterface rowMeta = new RowMeta();

        // A string object
        ValueMetaInterface meta1 = new ValueMeta("stringValue", ValueMetaInterface.TYPE_STRING, 30);
        rowMeta.addMetaValue(meta1);
        
        ValueMetaInterface meta2 = new ValueMeta("dateValue", ValueMetaInterface.TYPE_DATE);
        rowMeta.addMetaValue(meta2);

        ValueMetaInterface meta3 = new ValueMeta("numberValue", ValueMetaInterface.TYPE_NUMBER, 5, 2);
        meta3.setConversionMask("##0.00");
        meta3.setDecimalSymbol(".");
        meta3.setGroupingSymbol(",");
        rowMeta.addMetaValue(meta3);

        ValueMetaInterface meta4 = new ValueMeta("integerValue", ValueMetaInterface.TYPE_INTEGER, 7);
        meta4.setConversionMask("0000000");
        meta4.setDecimalSymbol(".");
        meta4.setGroupingSymbol(",");
        rowMeta.addMetaValue(meta4);

        ValueMetaInterface meta5 = new ValueMeta("bigNumberValue", ValueMetaInterface.TYPE_BIGNUMBER, 30, 7);
        meta5.setConversionMask("00000000000000000000.0000000000");
        meta5.setDecimalSymbol(".");
        meta5.setGroupingSymbol(",");
        rowMeta.addMetaValue(meta5);

        return rowMeta;
    }
}
