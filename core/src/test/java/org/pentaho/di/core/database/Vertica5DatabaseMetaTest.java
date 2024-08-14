/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class Vertica5DatabaseMetaTest extends VerticaDatabaseMetaTest {

  @Test
  public void testOverridesToVerticaDatabaseMeta() throws Exception {
    Vertica5DatabaseMeta nativeMeta = new Vertica5DatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );

    assertEquals( "com.vertica.jdbc.Driver", nativeMeta.getDriverClass() );
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );

    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );

    Mockito.when( resultSet.getTimestamp( 1 ) ).thenReturn( new java.sql.Timestamp( 65535 ) );
    Mockito.when( resultSet.getTime( 2 ) ).thenReturn( new java.sql.Time( 1000 ) );
    Mockito.when( resultSet.getDate( 3 ) ).thenReturn( new java.sql.Date( ( 65535 * 2 ) ) );
    ValueMetaTimestamp ts = new ValueMetaTimestamp( "FOO" );
    ts.setOriginalColumnType( java.sql.Types.TIMESTAMP );
    ValueMetaDate tm = new ValueMetaDate( "BAR" );
    tm.setOriginalColumnType( java.sql.Types.TIME );
    ValueMetaDate dt = new ValueMetaDate( "WIBBLE" );
    dt.setOriginalColumnType( java.sql.Types.DATE );


    Object rtn = null;
    rtn = nativeMeta.getValueFromResultSet( resultSet, ts, 0 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Timestamp", rtn.getClass().getName() );

    rtn = nativeMeta.getValueFromResultSet( resultSet, tm, 1 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Time", rtn.getClass().getName() );

    rtn = nativeMeta.getValueFromResultSet( resultSet, dt, 2 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Date", rtn.getClass().getName() );

    Mockito.when( resultSet.wasNull() ).thenReturn( true );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaString( "WOBBLE" ), 3 );
    assertNull( rtn );

    // Verify that getDate, getTime, and getTimestamp were respectively called once
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getDate( Mockito.anyInt() );
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTime( Mockito.anyInt() );
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTimestamp( Mockito.anyInt() );

  }

}
