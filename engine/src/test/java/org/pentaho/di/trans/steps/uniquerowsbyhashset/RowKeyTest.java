/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/



package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RowKeyTest {

  @Test
  public void testHashCodeCalculationsandEquals() throws Exception {
    Object[] arr1 = new Object[9];
    arr1[0] = true;

    SimpleDateFormat sdf = new SimpleDateFormat( "dd/M/yyyy" );
    String dateInString = "1/1/2018";
    Date dateObj = sdf.parse( dateInString );
    arr1[1] = dateObj;
    arr1[2] = Double.valueOf( 5.1 );
    arr1[3] = "test";
    arr1[4] = 123L;
    arr1[5] = new BigDecimal( 123.1 );

    byte[] bBinary = new byte[2];
    bBinary[0] = 1;
    bBinary[1] = 0;
    arr1[6] = bBinary;

    Timestamp timestampObj = Timestamp.valueOf( "2018-01-01 10:10:10.000000000" );
    arr1[7] = timestampObj;

    byte[] ipAddr = new byte[]{127, 0, 0, 1};
    InetAddress addrObj = InetAddress.getByAddress( ipAddr );
    arr1[8] = addrObj;

    UniqueRowsByHashSetData uniqueRowsObj = new UniqueRowsByHashSetData();
    uniqueRowsObj.fieldnrs = new int[0];
    uniqueRowsObj.storeValues = false;
    RowKey rowKey1 = new RowKey( arr1, uniqueRowsObj );
    assertEquals( rowKey1.hashCode(),  -227281350 );
    assertTrue( rowKey1.equals( new Object() ) );

    uniqueRowsObj.storeValues = true;
    RowKey rowKey2 = new RowKey( arr1, uniqueRowsObj );
    assertFalse( rowKey2.equals( rowKey1 ) );

    RowKey rowKey3 = new RowKey( arr1, uniqueRowsObj );
    assertTrue( rowKey2.equals( rowKey3 ) );
  }
}
