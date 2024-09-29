/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.di.trans.steps.gpload;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.DateDetector;

public class GPLoadDataOutputTest {

  //we pass only one field, so the index always 0
  private final int INDEX_OF_VALUE = 0;

  private final String DELIMITER = ",";

  @Mock private GPLoad gpLoad;

  @Mock private GPLoadMeta gpLoadMeta;

  @Mock private RowMetaInterface mi;

  @Mock private ValueMetaInterface value;

  private Object[] row = { new Object() };

  private StringWriter results;

  private PrintWriter printWriter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks( this );
    Mockito.when( gpLoadMeta.getDelimiter() ).thenReturn( DELIMITER );
    Mockito.when( gpLoad.environmentSubstitute( Mockito.anyString() ) ).thenReturn( DELIMITER );
    Mockito.when( mi.indexOfValue( Mockito.anyString() ) ).thenReturn( INDEX_OF_VALUE );

    results = new StringWriter();
    printWriter =  new PrintWriter( results );
  }

  @After
  public void tearDown() {
    if ( printWriter != null ) {
      printWriter.close();
    }
  }

  @Test
  public void testWritiLine_String() throws KettleValueException {
    String sample = "sample";
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    Mockito.when( mi.getString( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, String.valueOf( sample ) + Const.CR );
  }

  @Test
  public void testWritiLine_Integer() throws KettleValueException {
    long sample = 1;
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    Mockito.when( mi.getInteger( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, String.valueOf( sample ) + Const.CR );
  }

  @Test
  public void testWritiLine_Number() throws KettleValueException {
    Double sample = 1.0;
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    Mockito.when( mi.getNumber( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, String.valueOf( sample ) + Const.CR );
  }

  @Test
  public void testWritiLine_BigNumber() throws KettleValueException {
    BigDecimal sample = new BigDecimal( 1 );
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_BIGNUMBER );
    Mockito.when( mi.getBigNumber( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, String.valueOf( sample ) + Const.CR );
  }

  @Test
  public void testWritiLine_Date() throws KettleValueException, ParseException {
    String sampleDate = "2000-10-09";
    Date sample = DateDetector.getDateFromString( sampleDate );
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );
    Mockito.when( mi.getDate( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, sampleDate + Const.CR );
  }

  @Test
  public void testWritiLine_boolean() throws KettleValueException {
    boolean sample = true;
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_BOOLEAN );
    Mockito.when( mi.getBoolean( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, "Y" + Const.CR );
  }

  @Test
  public void testWritiLine_TimeStamp() throws KettleValueException, ParseException {
    String sampleDate = "2000-10-09 11:22:33.444";
    Date sample = DateDetector.getDateFromString( sampleDate );
    Mockito.when( value.getType() ).thenReturn( ValueMetaInterface.TYPE_TIMESTAMP );
    Mockito.when( mi.getDate( Mockito.any( Object[].class ), Mockito.anyInt() ) ).thenReturn( sample );
    testWritiLine( new String[] { String.valueOf( sample ) }, value, sampleDate + Const.CR );
  }

  private void testWritiLine( String[] string, ValueMetaInterface value, String expected ) {
    try {
      Mockito.when( gpLoadMeta.getFieldStream() ).thenReturn( string );
      Mockito.when( mi.getValueMeta( Mockito.anyInt() ) ).thenReturn( value );
      GPLoadDataOutput gpLoadDataOutput = new GPLoadDataOutput( gpLoad, gpLoadMeta );
      gpLoadDataOutput.setOutput(  printWriter );
      gpLoadDataOutput.writeLine( mi, row );
      Assert.assertEquals( expected, results.toString() );
    } catch ( KettleException e ) {
      Assert.fail( "Something wrong" );
    }
  }

}
