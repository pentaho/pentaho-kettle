/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BIGNUMBER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BINARY;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BOOLEAN;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_DATE;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_INET;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_INTEGER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_NUMBER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.NetezzaDatabaseMeta;
import org.pentaho.di.core.database.Vertica5DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class ValueMetaBaseTest {

  private static final String TEST_NAME = "TEST_NAME";
  // Get PKG from class under test
  private static Class<?> PKG = ( new ValueMetaBase() {
    public Class<?> getPackage() {
      return PKG;
    }
  } ).getPackage();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Test
  public void testDefaultCtor() {
    ValueMetaBase base = new ValueMetaBase();
    assertNotNull( base );
    assertNull( base.getName() );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_NONE );
  }

  @Test
  public void testCtorName() {
    ValueMetaBase base = new ValueMetaBase( "myValueMeta" );
    assertEquals( base.getName(), "myValueMeta" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_NONE );
    assertNotNull( base.getTypeDesc() );
  }

  @Test
  public void testCtorNameAndType() {
    ValueMetaBase base = new ValueMetaBase( "myStringType", TYPE_STRING );
    assertEquals( base.getName(), "myStringType" );
    assertEquals( base.getType(), TYPE_STRING );
    assertEquals( base.getTypeDesc(), "String" );
  }

  @Test
  public void test4ArgCtor() {
    ValueMetaBase base =
      new ValueMetaBase( "Hello, is it me you're looking for?", TYPE_BOOLEAN, 4, 9 );
    assertEquals( base.getName(), "Hello, is it me you're looking for?" );
    assertEquals( base.getType(), TYPE_BOOLEAN );
    assertEquals( base.getLength(), 4 );
    assertEquals( base.getPrecision(), -1 );
    assertEquals( base.getStorageType(), ValueMetaInterface.STORAGE_TYPE_NORMAL );
  }

  /**
   * PDI-10877 Table input step returns no data when pulling a timestamp column from IBM Netezza
   *
   * @throws Exception
   */
  @Test
  public void testGetValueFromSqlTypeNetezza() throws Exception {
    ValueMetaBase obj = new ValueMetaBase();
    DatabaseInterface databaseInterface = new NetezzaDatabaseMeta();
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );

    Mockito.when( metaData.getColumnType( 1 ) ).thenReturn( Types.DATE );
    Mockito.when( metaData.getColumnType( 2 ) ).thenReturn( Types.TIME );

    obj.type = TYPE_DATE;
    // call to testing method
    obj.getValueFromResultSet( databaseInterface, resultSet, 0 );
    // for jdbc Date type getDate method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getDate( Mockito.anyInt() );

    obj.getValueFromResultSet( databaseInterface, resultSet, 1 );
    // for jdbc Time type getTime method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTime( Mockito.anyInt() );
  }

  @Test
  public void testGetDataXML() throws IOException {
    Encoder encoder = ESAPI.encoder();

    BigDecimal bigDecimal = BigDecimal.ONE;
    ValueMetaBase valueDoubleMetaBase =
      new ValueMetaBase( String.valueOf( bigDecimal ), TYPE_BIGNUMBER, ValueMetaInterface.STORAGE_TYPE_NORMAL );
    assertEquals(
      "<value-data>" + encoder.encodeForXML( String.valueOf( bigDecimal ) ) + "</value-data>" + LINE_SEPARATOR,
      valueDoubleMetaBase.getDataXML( bigDecimal ) );

    boolean valueBoolean = Boolean.TRUE;
    ValueMetaBase valueBooleanMetaBase =
      new ValueMetaBase( String.valueOf( valueBoolean ), TYPE_BOOLEAN, ValueMetaInterface.STORAGE_TYPE_NORMAL );
    assertEquals(
      "<value-data>" + encoder.encodeForXML( String.valueOf( valueBoolean ) ) + "</value-data>" + LINE_SEPARATOR,
      valueBooleanMetaBase.getDataXML( valueBoolean ) );

    Date date = new Date( 0 );
    ValueMetaBase dateMetaBase =
      new ValueMetaBase( date.toString(), TYPE_DATE, ValueMetaInterface.STORAGE_TYPE_NORMAL );
    SimpleDateFormat formaterData = new SimpleDateFormat( ValueMetaBase.DEFAULT_DATE_FORMAT_MASK );
    assertEquals(
      "<value-data>" + encoder.encodeForXML( formaterData.format( date ) ) + "</value-data>" + LINE_SEPARATOR,
      dateMetaBase.getDataXML( date ) );

    InetAddress inetAddress = InetAddress.getByName( "127.0.0.1" );
    ValueMetaBase inetAddressMetaBase =
      new ValueMetaBase( inetAddress.toString(), TYPE_INET, ValueMetaInterface.STORAGE_TYPE_NORMAL );
    assertEquals( "<value-data>" + encoder.encodeForXML( inetAddress.toString() ) + "</value-data>" + LINE_SEPARATOR,
      inetAddressMetaBase.getDataXML( inetAddress ) );

    long value = Long.MAX_VALUE;
    ValueMetaBase integerMetaBase = new ValueMetaBase( String.valueOf( value ), TYPE_INTEGER,
      ValueMetaInterface.STORAGE_TYPE_NORMAL );
    assertEquals( "<value-data>" + encoder.encodeForXML( String.valueOf( value ) ) + "</value-data>" + LINE_SEPARATOR,
      integerMetaBase.getDataXML( value ) );

    String stringValue = "TEST_STRING";
    ValueMetaBase valueMetaBase =
      new ValueMetaBase( stringValue, TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_NORMAL );
    assertEquals( "<value-data>" + encoder.encodeForXML( stringValue ) + "</value-data>" + LINE_SEPARATOR,
      valueMetaBase.getDataXML( stringValue ) );

    Timestamp timestamp = new Timestamp( 0 );
    ValueMetaBase valueMetaBaseTimeStamp = new ValueMetaBase( timestamp.toString(), ValueMetaInterface.TYPE_TIMESTAMP,
      ValueMetaInterface.STORAGE_TYPE_NORMAL );
    SimpleDateFormat formater = new SimpleDateFormat( ValueMetaBase.DEFAULT_TIMESTAMP_FORMAT_MASK );
    assertEquals(
      "<value-data>" + encoder.encodeForXML( formater.format( timestamp ) ) + "</value-data>" + LINE_SEPARATOR,
      valueMetaBaseTimeStamp.getDataXML( timestamp ) );
  }

  @Test
  public void testGetBinaryWithLength_WhenBinarySqlTypesOfVertica() throws Exception {
    final int binaryColumnIndex = 1;
    final int varbinaryColumnIndex = 2;
    final int expectedBinarylength = 1;
    final int expectedVarBinarylength = 80;

    ValueMetaBase obj = new ValueMetaBase();
    DatabaseMeta dbMeta = spy( new DatabaseMeta() );
    DatabaseInterface databaseInterface = new Vertica5DatabaseMeta();
    dbMeta.setDatabaseInterface( databaseInterface );

    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );

    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );
    Mockito.when( metaData.getColumnType( binaryColumnIndex ) ).thenReturn( Types.BINARY );
    Mockito.when( metaData.getPrecision( binaryColumnIndex ) ).thenReturn( expectedBinarylength );
    Mockito.when( metaData.getColumnDisplaySize( binaryColumnIndex ) ).thenReturn( expectedBinarylength * 2 );

    Mockito.when( metaData.getColumnType( varbinaryColumnIndex ) ).thenReturn( Types.BINARY );
    Mockito.when( metaData.getPrecision( varbinaryColumnIndex ) ).thenReturn( expectedVarBinarylength );
    Mockito.when( metaData.getColumnDisplaySize( varbinaryColumnIndex ) ).thenReturn( expectedVarBinarylength * 2 );

    // get value meta for binary type
    ValueMetaInterface binaryValueMeta =
      obj.getValueFromSQLType( dbMeta, TEST_NAME, metaData, binaryColumnIndex, false, false );
    assertNotNull( binaryValueMeta );
    assertTrue( TEST_NAME.equals( binaryValueMeta.getName() ) );
    assertTrue( TYPE_BINARY == binaryValueMeta.getType() );
    assertTrue( expectedBinarylength == binaryValueMeta.getLength() );
    assertFalse( binaryValueMeta.isLargeTextField() );

    // get value meta for varbinary type
    ValueMetaInterface varbinaryValueMeta =
      obj.getValueFromSQLType( dbMeta, TEST_NAME, metaData, varbinaryColumnIndex, false, false );
    assertNotNull( varbinaryValueMeta );
    assertTrue( TEST_NAME.equals( varbinaryValueMeta.getName() ) );
    assertTrue( TYPE_BINARY == varbinaryValueMeta.getType() );
    assertTrue( expectedVarBinarylength == varbinaryValueMeta.getLength() );
    assertFalse( varbinaryValueMeta.isLargeTextField() );

  }

  @Test
  public void testGetValueFromSQLTypeTypeOverride() throws Exception {
    final int varbinaryColumnIndex = 2;

    ValueMetaBase valueMetaBase = new ValueMetaBase(),
      valueMetaBaseSpy = spy( valueMetaBase );
    DatabaseMeta dbMeta = Mockito.mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = Mockito.mock( DatabaseInterface.class );
    doReturn( databaseInterface ).when( dbMeta ).getDatabaseInterface();

    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    valueMetaBaseSpy.getValueFromSQLType( dbMeta, TEST_NAME, metaData, varbinaryColumnIndex, false, false );

    verify( databaseInterface, Mockito.times( 1 ) ).customizeValueFromSQLType( any( ValueMetaInterface.class ),
      any( ResultSetMetaData.class ), anyInt() );
  }

  @Test
  public void testVerticaTimeType() throws Exception {
    // PDI-12244
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    ValueMetaInterface valueMetaInterface = Mockito.mock( ValueMetaInternetAddress.class );

    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );
    Mockito.when( metaData.getColumnType( 1 ) ).thenReturn( Types.TIME );
    Mockito.when( resultSet.getTime( 1 ) ).thenReturn( new Time( 0 ) );
    Mockito.when( valueMetaInterface.getOriginalColumnType() ).thenReturn( Types.TIME );
    Mockito.when( valueMetaInterface.getType() ).thenReturn( TYPE_DATE );

    DatabaseInterface databaseInterface = new Vertica5DatabaseMeta();
    Object ret = databaseInterface.getValueFromResultSet( resultSet, valueMetaInterface, 0 );
    assertEquals( new Time( 0 ), ret );
  }

  @Test
  public void testConvertStringToBoolean() {
    assertNull( ValueMetaBase.convertStringToBoolean( null ) );
    assertNull( ValueMetaBase.convertStringToBoolean( "" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Y" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "y" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Yes" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "YES" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "yES" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "TRUE" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "True" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "true" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "tRuE" ) );
    assertTrue( ValueMetaBase.convertStringToBoolean( "Y" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "N" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "No" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "no" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "Yeah" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "False" ) );
    assertFalse( ValueMetaBase.convertStringToBoolean( "NOT false" ) );
  }

  @Test
  public void testConvertDataFromStringToString() throws KettleValueException {
    ValueMetaBase inValueMetaString = new ValueMetaString();
    ValueMetaBase outValueMetaString = new ValueMetaString();
    String inputValueEmptyString = StringUtils.EMPTY;
    String inputValueNullString = null;
    String nullIf = null;
    String ifNull = null;
    int trim_type = 0;
    Object result;

    result =
      outValueMetaString.convertDataFromString( inputValueEmptyString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "Conversion from empty string to string must return empty string", result, StringUtils.EMPTY );

    result =
      outValueMetaString.convertDataFromString( inputValueNullString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "Conversion from null string must return null", result, null );
  }

  @Test
  public void testConvertDataFromStringToDate() throws KettleValueException {
    ValueMetaBase inValueMetaString = new ValueMetaString();
    ValueMetaBase outValueMetaDate = new ValueMetaDate();
    String inputValueEmptyString = StringUtils.EMPTY;
    String nullIf = null;
    String ifNull = null;
    int trim_type = 0;
    Object result;

    result =
      outValueMetaDate.convertDataFromString( inputValueEmptyString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "Conversion from empty string to date must return null", result, null );
  }

  @Test( expected = KettleValueException.class )
  public void testConvertDataFromStringForNullMeta() throws KettleValueException {
    ValueMetaBase valueMetaBase = new ValueMetaBase();
    String inputValueEmptyString = StringUtils.EMPTY;
    ValueMetaInterface valueMetaInterface = null;
    String nullIf = null;
    String ifNull = null;
    int trim_type = 0;

    valueMetaBase.convertDataFromString( inputValueEmptyString, valueMetaInterface, nullIf, ifNull, trim_type );
  }

  @Test
  public void testIsNumeric() {
    int[] numTypes = { TYPE_INTEGER, TYPE_NUMBER, TYPE_BIGNUMBER };
    for ( int type : numTypes ) {
      assertTrue( Integer.toString( type ), ValueMetaBase.isNumeric( type ) );
    }

    int[] notNumTypes = { TYPE_INET, TYPE_BOOLEAN, TYPE_BINARY, TYPE_DATE, TYPE_STRING };
    for ( int type : notNumTypes ) {
      assertFalse( Integer.toString( type ), ValueMetaBase.isNumeric( type ) );
    }
  }

  @Test
  public void testGetAllTypes() {
    assertArrayEquals( ValueMetaBase.getAllTypes(), ValueMetaFactory.getAllValueMetaNames() );
  }

  @Test
  public void testGetTrimTypeByCode() {
    assertEquals( ValueMetaBase.getTrimTypeByCode( "none" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "left" ), ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "right" ), ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "both" ), ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( ValueMetaBase.getTrimTypeByCode( null ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByCode( "fake" ), ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Test
  public void testGetTrimTypeCode() {
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_NONE ), "none" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_LEFT ), "left" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_RIGHT ), "right" );
    assertEquals( ValueMetaBase.getTrimTypeCode( ValueMetaInterface.TRIM_TYPE_BOTH ), "both" );
  }

  @Test
  public void testGetTrimTypeByDesc() {
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) ),
      ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Left" ) ),
      ValueMetaInterface.TRIM_TYPE_LEFT );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Right" ) ),
      ValueMetaInterface.TRIM_TYPE_RIGHT );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( BaseMessages.getString( PKG, "ValueMeta.TrimType.Both" ) ),
      ValueMetaInterface.TRIM_TYPE_BOTH );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( null ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( "" ), ValueMetaInterface.TRIM_TYPE_NONE );
    assertEquals( ValueMetaBase.getTrimTypeByDesc( "fake" ), ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Test
  public void testGetTrimTypeDesc() {
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_NONE ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.None" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_LEFT ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Left" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_RIGHT ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Right" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_BOTH ), BaseMessages.getString( PKG,
      "ValueMeta.TrimType.Both" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( -1 ), BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) );
    assertEquals( ValueMetaBase.getTrimTypeDesc( 10000 ), BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ) );
  }

  @Test
  public void testOrigin() {
    ValueMetaBase base = new ValueMetaBase();
    base.setOrigin( "myOrigin" );
    assertEquals( base.getOrigin(), "myOrigin" );
    base.setOrigin( null );
    assertNull( base.getOrigin() );
    base.setOrigin( "" );
    assertEquals( base.getOrigin(), "" );
  }

  @Test
  public void testName() {
    ValueMetaBase base = new ValueMetaBase();
    base.setName( "myName" );
    assertEquals( base.getName(), "myName" );
    base.setName( null );
    assertNull( base.getName() );
    base.setName( "" );
    assertEquals( base.getName(), "" );

  }

  @Test
  public void testLength() {
    ValueMetaBase base = new ValueMetaBase();
    base.setLength( 6 );
    assertEquals( base.getLength(), 6 );
    base.setLength( -1 );
    assertEquals( base.getLength(), -1 );
  }

  @Test
  public void testPrecision() {
    ValueMetaBase base = new ValueMetaBase();
    base.setPrecision( 6 );
    assertEquals( base.getPrecision(), 6 );
    base.setPrecision( -1 );
    assertEquals( base.getPrecision(), -1 );
  }

  @Test
  public void testCompareIntegers() throws KettleValueException {
    ValueMetaBase intMeta = new ValueMetaBase( "int", TYPE_INTEGER );
    Long int1 = new Long( 6223372036854775804L );
    Long int2 = new Long( -6223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );
    assertEquals( 0, intMeta.compare( int2, int2 ) );

    int1 = new Long( 9223372036854775804L );
    int2 = new Long( -9223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );
    assertEquals( 0, intMeta.compare( int2, int2 ) );

    int1 = new Long( 6223372036854775804L );
    int2 = new Long( -9223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );

    int1 = new Long( 9223372036854775804L );
    int2 = new Long( -6223372036854775804L );
    assertEquals( 1, intMeta.compare( int1, int2 ) );
    assertEquals( -1, intMeta.compare( int2, int1 ) );
    assertEquals( 0, intMeta.compare( int1, int1 ) );

  }

  @Test
  public void testCompareDate() throws KettleValueException {
    ValueMetaBase dateMeta = new ValueMetaBase( "int", TYPE_DATE );
    Date date1 = new Date( 6223372036854775804L );
    Date date2 = new Date( -6223372036854775804L );
    assertEquals( 1, dateMeta.compare( date1, date2 ) );
    assertEquals( -1, dateMeta.compare( date2, date1 ) );
    assertEquals( 0, dateMeta.compare( date1, date1 ) );
  }

  @Test
  public void testDateParsing8601() throws Exception {
    ValueMetaBase dateMeta = new ValueMetaBase( "date", TYPE_DATE );
    dateMeta.setDateFormatLenient( false );

    // try to convert date by 'start-of-date' make - old behavior
    try {
      dateMeta.setConversionMask( "yyyy-MM-dd" );
      dateMeta.convertStringToDate( "1918-03-25T07:40:03.012+03:00" );
      fail( "Shouldn't be converted" );
    } catch ( KettleValueException ex ) {
    }

    // convert ISO-8601 date - supported since Java 7
    dateMeta.setConversionMask( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" );
    assertEquals( utc( 1918, 3, 25, 5, 10, 3, 12 ), dateMeta.convertStringToDate( "1918-03-25T07:40:03.012+02:30" ) );
    assertEquals( utc( 1918, 3, 25, 7, 40, 3, 12 ), dateMeta.convertStringToDate( "1918-03-25T07:40:03.012Z" ) );

    // convert date
    dateMeta.setConversionMask( "yyyy-MM-dd" );
    assertEquals( local( 1918, 3, 25, 0, 0, 0, 0 ), dateMeta.convertStringToDate( "1918-03-25" ) );
    // convert date with spaces at the end
    assertEquals( local( 1918, 3, 25, 0, 0, 0, 0 ), dateMeta.convertStringToDate( "1918-03-25  \n" ) );
  }

  Date local( int year, int month, int dat, int hrs, int min, int sec, int ms ) {
    GregorianCalendar cal = new GregorianCalendar( year, month - 1, dat, hrs, min, sec );
    cal.set( Calendar.MILLISECOND, ms );
    return cal.getTime();
  }

  Date utc( int year, int month, int dat, int hrs, int min, int sec, int ms ) {
    GregorianCalendar cal = new GregorianCalendar( year, month - 1, dat, hrs, min, sec );
    cal.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    cal.set( Calendar.MILLISECOND, ms );
    return cal.getTime();
  }
}
