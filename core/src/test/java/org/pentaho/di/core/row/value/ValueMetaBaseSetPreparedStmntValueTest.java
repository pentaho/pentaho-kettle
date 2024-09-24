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

package org.pentaho.di.core.row.value;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class ValueMetaBaseSetPreparedStmntValueTest {

  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

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
