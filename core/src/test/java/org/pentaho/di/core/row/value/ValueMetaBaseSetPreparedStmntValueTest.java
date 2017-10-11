/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.row.value;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBaseSetPreparedStmntValueTest {

  private DatabaseMeta dbMeta;
  private PreparedStatement ps;
  private Date date;
  private Timestamp ts;

  @Before
  public void setUp() {
    dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.supportsTimeStampToDateConversion() ).thenReturn( true );
    ps = mock( PreparedStatement.class );
    date = new Date( System.currentTimeMillis() );
    ts = new Timestamp( System.currentTimeMillis() );
  }

  @Test
  public void testXMLParsingWithNoDataFormatLocale() throws IOException {
    ValueMetaInterface r1 = new ValueMetaString( "value" );
    r1.setDateFormatLocale( null );
    RowMetaInterface row = new RowMeta();
    row.setValueMetaList( new ArrayList<ValueMetaInterface>( Arrays.asList( r1 ) ) );

    row.getMetaXML();
  }

  @Test
  public void testDateRegular() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "N" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPrecision( 1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, date );

    verify( ps ).setDate( eq( 1 ), any( java.sql.Date.class ), any( Calendar.class ) );
  }

  @Test
  public void testDateIgnoreTZ() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "Y" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPrecision( 1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, date );

    verify( ps ).setDate( eq( 1 ), any( java.sql.Date.class ) );
  }

  @Test
  public void testTimestampRegular() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "N" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, ts );

    verify( ps ).setTimestamp( eq( 1 ), any( Timestamp.class ), any( Calendar.class ) );
  }

  @Test
  public void testTimestampIgnoreTZ() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "Y" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, ts );

    verify( ps ).setTimestamp( eq( 1 ), any( Timestamp.class ) );
  }

  @Test
  public void testConvertedTimestampRegular() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "N" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, date );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    verify( ps ).setTimestamp( eq( 1 ), any( Timestamp.class ), any( Calendar.class ) );
  }

  @Test
  public void testConvertedTimestampIgnoreTZ() throws Exception {

    System.setProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "Y" );

    ValueMetaBase valueMeta = new ValueMetaDate( "", -1, -1 );
    valueMeta.setPreparedStatementValue( dbMeta, ps, 1, date );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    verify( ps ).setTimestamp( eq( 1 ), any( Timestamp.class ) );
  }

}
