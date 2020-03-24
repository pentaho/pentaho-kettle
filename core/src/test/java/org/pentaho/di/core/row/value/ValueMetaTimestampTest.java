/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.row.value;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * User: Dzmitry Stsiapanau Date: 3/20/2014 Time: 11:51 AM
 */
public class ValueMetaTimestampTest {
  private static final Timestamp TIMESTAMP_WITH_NANOSECONDS = Timestamp.valueOf( "2019-09-01 04:34:56.123456789" );
  private static final Timestamp TIMESTAMP_WITH_MILLISECONDS = Timestamp.valueOf( "2019-09-01 04:34:56.12300000" );
  private static final long TIMESTAMP_AS_NANOSECONDS = 1567308896123456789L;
  private static final long TIMESTAMP_AS_MILLISECONDS = 1567308896123L;

  /**
   * <p>For the Number to Timestamp conversion (nanoseconds) we use the above values but slightly truncated to avoid
   * different results depending on the used JVM.</p>
   * <p>This is because Number uses {@code double} which only guarantees 15 significant digits</p>
   *
   * @see #testConvertNumberToTimestamp_DefaultMode()
   * @see #testConvertNumberToTimestamp_Nanoseconds()
   */
  private static final Timestamp TIMESTAMP_WITH_NANOSECONDS_DOUBLE = Timestamp.valueOf( "2019-09-01 04:34:56.123456" );
  private static final double TIMESTAMP_AS_NANOSECONDS_DOUBLE = 1567308896123456000.0;

  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void testSetPreparedStatementValue() throws Exception {
    ValueMetaTimestamp vm = new ValueMetaTimestamp();
    PreparedStatement ps = mock( PreparedStatement.class );
    doAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        Object ts = invocationOnMock.getArguments()[ 1 ];
        return ts.toString();
      }
    } ).when( ps ).setTimestamp( anyInt(), any( Timestamp.class ) );

    try {
      vm.setPreparedStatementValue( mock( DatabaseMeta.class ), ps, 0, null );
    } catch ( KettleDatabaseException ex ) {
      fail( "Check PDI-11547" );
    }
  }

  @Test
  public void testCompare() throws Exception {
    ValueMetaTimestamp vm = new ValueMetaTimestamp();
    Timestamp earlier = Timestamp.valueOf( "2012-12-12 12:12:12.121212" );
    Timestamp later = Timestamp.valueOf( "2013-12-12 12:12:12.121212" );
    assertTrue( vm.isSortedAscending() );
    assertFalse( vm.isSortedDescending() );
    assertEquals( vm.compare( null, null ), 0 );
    assertEquals( vm.compare( null, earlier ), -1 );
    assertEquals( vm.compare( earlier, null ), 1 );
    assertEquals( vm.compare( earlier, earlier ), 0 );
    assertEquals( vm.compare( earlier, later ), -1 );
    assertEquals( vm.compare( later, earlier ), 1 );

    // Check Descending comparison
    vm.setSortedDescending( true );
    assertFalse( vm.isSortedAscending() );
    assertTrue( vm.isSortedDescending() );
    assertEquals( vm.compare( null, null ), 0 );
    assertEquals( vm.compare( null, earlier ), 1 );
    assertEquals( vm.compare( earlier, null ), -1 );
    assertEquals( vm.compare( earlier, earlier ), 0 );
    assertEquals( vm.compare( earlier, later ), 1 );
    assertEquals( vm.compare( later, earlier ), -1 );
  }

  @Test
  public void testConvertStringToTimestamp() throws Exception {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertEquals( Timestamp.valueOf( "2012-04-05 04:03:02.123456" ), valueMetaTimestamp
      .convertStringToTimestamp( "2012/4/5 04:03:02.123456" ) );
    assertEquals( Timestamp.valueOf( "2012-04-05 04:03:02.123" ), valueMetaTimestamp
      .convertStringToTimestamp( "2012/4/5 04:03:02.123" ) );
    assertEquals( Timestamp.valueOf( "2012-04-05 04:03:02.123456789" ), valueMetaTimestamp
      .convertStringToTimestamp( "2012/4/5 04:03:02.123456789" ) );
  }

  @Test
  public void testConvertTimestampToString() throws Exception {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertEquals( "2012/04/05 04:03:02.123456000", valueMetaTimestamp.convertTimestampToString( Timestamp
      .valueOf( "2012-04-05 04:03:02.123456" ) ) );
    assertEquals( "2012/04/05 04:03:02.123000000", valueMetaTimestamp.convertTimestampToString( Timestamp
      .valueOf( "2012-04-05 04:03:02.123" ) ) );
    assertEquals( "2012/04/05 04:03:02.123456789", valueMetaTimestamp.convertTimestampToString( Timestamp
      .valueOf( "2012-04-05 04:03:02.123456789" ) ) );
  }

  @Test
  public void testConvertDateToTimestamp_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.convertDateToTimestamp( null ) );
  }

  @Test
  public void testConvertDateToTimestamp() throws Exception {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    // Converting date to timestamp
    Date date = new Date();
    assertEquals( valueMetaTimestamp.convertDateToTimestamp( date ).getTime(), date.getTime() );

    // Converting timestamp to timestamp
    Timestamp timestamp = Timestamp.valueOf( "2014-04-05 04:03:02.123456789" );
    Timestamp convertedTimestamp = valueMetaTimestamp.convertDateToTimestamp( timestamp );
    assertEquals( convertedTimestamp.getTime(), timestamp.getTime() );
    assertEquals( convertedTimestamp.getNanos(), timestamp.getNanos() );
  }

  @Test
  public void testConvertTimestampToDate_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.getDate( null ) );
  }

  @Test( expected = KettleValueException.class )
  public void testConvertTimestampToBoolean_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    valueMetaTimestamp.getBoolean( TIMESTAMP_WITH_MILLISECONDS );
  }

  @Test
  public void testConvertIntegerToTimestamp_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.convertIntegerToTimestamp( null ) );
  }

  @Test
  public void testConvertIntegerToTimestamp_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertIntegerToTimestamp( TIMESTAMP_AS_NANOSECONDS );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.convertIntegerToTimestamp( TIMESTAMP_AS_NANOSECONDS );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
  }

  @Test
  public void testConvertIntegerToTimestamp_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertIntegerToTimestamp( TIMESTAMP_AS_MILLISECONDS );
    assertEquals( TIMESTAMP_WITH_MILLISECONDS, result );
  }

  @Test
  public void testConvertIntegerToTimestamp_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertIntegerToTimestamp( TIMESTAMP_AS_NANOSECONDS );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
  }

  @Test
  public void testConvertTimestampToInteger_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.getInteger( null ) );
  }

  @Test
  public void testConvertTimestampToInteger_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    long result = valueMetaTimestamp.getInteger( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123L, result );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.getInteger( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123L, result );
  }

  @Test
  public void testConvertTimestampToInteger_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    long result = valueMetaTimestamp.getInteger( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123L, result );
  }

  @Test
  public void testConvertTimestampToInteger_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    long result = valueMetaTimestamp.getInteger( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( TIMESTAMP_AS_NANOSECONDS, result );
  }

  @Test
  public void testConvertNumberToTimestamp_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.convertNumberToTimestamp( null ) );
  }

  @Test
  public void testConvertNumberToTimestamp_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertNumberToTimestamp( TIMESTAMP_AS_NANOSECONDS_DOUBLE );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS_DOUBLE, result );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.convertNumberToTimestamp( TIMESTAMP_AS_NANOSECONDS_DOUBLE );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS_DOUBLE, result );
  }

  @Test
  public void testConvertNumberToTimestamp_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertNumberToTimestamp( (double) TIMESTAMP_AS_MILLISECONDS );
    assertEquals( TIMESTAMP_WITH_MILLISECONDS, result );
  }

  @Test
  public void testConvertNumberToTimestamp_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    // Limiting the significant digits because we're using double
    // Using the values used on other tests could originate different results depending on the JVM
    Timestamp result = valueMetaTimestamp.convertNumberToTimestamp( TIMESTAMP_AS_NANOSECONDS_DOUBLE );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS_DOUBLE, result );
  }

  @Test
  public void testConvertTimestampToNumber_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.getNumber( null ) );
  }

  @Test
  public void testConvertTimestampToNumber_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    double result = valueMetaTimestamp.getNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123.0, result, 0 );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.getNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123.0, result, 0 );
  }

  @Test
  public void testConvertTimestampToNumber_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    double result = valueMetaTimestamp.getNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( 1567308896123.0, result, 0 );
  }

  @Test
  public void testConvertTimestampToNumber_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    double result = valueMetaTimestamp.getNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( (double) TIMESTAMP_AS_NANOSECONDS, result, 0 );
  }

  @Test
  public void testConvertBigNumberToTimestamp_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.convertBigNumberToTimestamp( null ) );
  }

  @Test
  public void testConvertBigNumberToTimestamp_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result =
      valueMetaTimestamp.convertBigNumberToTimestamp( BigDecimal.valueOf( TIMESTAMP_AS_NANOSECONDS ) );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.convertBigNumberToTimestamp( BigDecimal.valueOf( TIMESTAMP_AS_NANOSECONDS ) );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
  }

  @Test
  public void testConvertBigNumberToTimestamp_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result =
      valueMetaTimestamp.convertBigNumberToTimestamp( BigDecimal.valueOf( TIMESTAMP_AS_MILLISECONDS ) );
    assertEquals( TIMESTAMP_WITH_MILLISECONDS, result );
  }

  @Test
  public void testConvertBigNumberToTimestamp_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Timestamp result = valueMetaTimestamp.convertBigNumberToTimestamp( BigDecimal.valueOf( TIMESTAMP_AS_NANOSECONDS ) );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, result );
  }

  @Test
  public void testConvertTimestampToBigNumber_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.getBigNumber( null ) );
  }

  @Test
  public void testConvertTimestampToBigNumber_DefaultMode() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    BigDecimal result = valueMetaTimestamp.getBigNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( BigDecimal.valueOf( 1567308896123L ), result );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE, "Something invalid!" );
    valueMetaTimestamp = new ValueMetaTimestamp();
    result = valueMetaTimestamp.getBigNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( BigDecimal.valueOf( 1567308896123L ), result );
  }

  @Test
  public void testConvertTimestampToBigNumber_Milliseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    BigDecimal result = valueMetaTimestamp.getBigNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( BigDecimal.valueOf( 1567308896123L ), result );
  }

  @Test
  public void testConvertTimestampToBigNumber_Nanoseconds() throws KettleValueException {
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );
    System.setProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE,
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS );
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    BigDecimal result = valueMetaTimestamp.getBigNumber( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( BigDecimal.valueOf( TIMESTAMP_AS_NANOSECONDS ), result );
  }

  @Test
  public void testCloneValueData_Null() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    assertNull( valueMetaTimestamp.cloneValueData( null ) );
  }

  @Test
  public void testCloneValueData() throws KettleValueException {
    ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp();
    Object clonedTimestamp = valueMetaTimestamp.cloneValueData( TIMESTAMP_WITH_NANOSECONDS );
    assertEquals( TIMESTAMP_WITH_NANOSECONDS, clonedTimestamp );
  }
}
