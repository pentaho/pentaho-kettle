/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.database.NetezzaDatabaseMeta;
import org.pentaho.di.core.database.Vertica5DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

public class ValueMetaBaseTest {

  private static final String TEST_NAME = "TEST_NAME";
  private static final String LOG_FIELD = "LOG_FIELD";
  public static final int MAX_TEXT_FIELD_LEN = 5;

  // Get PKG from class under test
  private static Class<?> PKG = ( new ValueMetaBase() {
    public Class<?> getPackage() {
      return PKG;
    }
  } ).getPackage();
  private StoreLoggingEventListener listener;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.addPluginType( DatabasePluginType.getInstance() );
    PluginRegistry.init( true );
    KettleLogStore.init();
  }

  @Before
  public void setUp() {
    listener = new StoreLoggingEventListener();
    KettleLogStore.getAppender().addLoggingEventListener( listener );
  }

  @After
  public void tearDown() {
    KettleLogStore.getAppender().removeLoggingEventListener( listener );
    listener = new StoreLoggingEventListener();
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
    ValueMetaBase base = new ValueMetaBase( "myStringType", ValueMetaInterface.TYPE_STRING );
    assertEquals( base.getName(), "myStringType" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_STRING );
    assertEquals( base.getTypeDesc(), "String" );
  }

  @Test
  public void test4ArgCtor() {
    ValueMetaBase base =
      new ValueMetaBoolean( "Hello, is it me you're looking for?", 4, 9 );
    assertEquals( base.getName(), "Hello, is it me you're looking for?" );
    assertEquals( base.getType(), ValueMetaInterface.TYPE_BOOLEAN );
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

    obj.type = ValueMetaInterface.TYPE_DATE;
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
    BigDecimal bigDecimal = BigDecimal.ONE;
    ValueMetaBase valueDoubleMetaBase =
      new ValueMetaBase( String.valueOf( bigDecimal ), ValueMetaInterface.TYPE_BIGNUMBER );
    assertEquals(
      "<value-data>" + Encode.forXml( String.valueOf( bigDecimal ) ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      valueDoubleMetaBase.getDataXML( bigDecimal ) );

    boolean valueBoolean = Boolean.TRUE;
    ValueMetaBase valueBooleanMetaBase =
      new ValueMetaBase( String.valueOf( valueBoolean ), ValueMetaInterface.TYPE_BOOLEAN );
    assertEquals(
      "<value-data>" + Encode.forXml( String.valueOf( valueBoolean ) ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      valueBooleanMetaBase.getDataXML( valueBoolean ) );

    Date date = new Date( 0 );
    ValueMetaBase dateMetaBase =
      new ValueMetaBase( date.toString(), ValueMetaInterface.TYPE_DATE );
    SimpleDateFormat formaterData = new SimpleDateFormat( ValueMetaBase.DEFAULT_DATE_FORMAT_MASK );
    assertEquals(
      "<value-data>" + Encode.forXml( formaterData.format( date ) ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      dateMetaBase.getDataXML( date ) );

    InetAddress inetAddress = InetAddress.getByName( "127.0.0.1" );
    ValueMetaBase inetAddressMetaBase =
      new ValueMetaBase( inetAddress.toString(), ValueMetaInterface.TYPE_INET );
    assertEquals( "<value-data>" + Encode.forXml( inetAddress.toString() ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      inetAddressMetaBase.getDataXML( inetAddress ) );

    long value = Long.MAX_VALUE;
    ValueMetaBase integerMetaBase = new ValueMetaBase( String.valueOf( value ), ValueMetaInterface.TYPE_INTEGER );
    assertEquals( "<value-data>" + Encode.forXml( String.valueOf( value ) ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      integerMetaBase.getDataXML( value ) );

    String stringValue = "TEST_STRING";
    ValueMetaBase valueMetaBase = new ValueMetaString( stringValue );
    assertEquals( "<value-data>" + Encode.forXml( stringValue ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      valueMetaBase.getDataXML( stringValue ) );

    Timestamp timestamp = new Timestamp( 0 );
    ValueMetaBase valueMetaBaseTimeStamp = new ValueMetaBase( timestamp.toString(), ValueMetaInterface.TYPE_TIMESTAMP );
    SimpleDateFormat formater = new SimpleDateFormat( ValueMetaBase.DEFAULT_TIMESTAMP_FORMAT_MASK );
    assertEquals(
      "<value-data>" + Encode.forXml( formater.format( timestamp ) ) + "</value-data>" + SystemUtils.LINE_SEPARATOR,
      valueMetaBaseTimeStamp.getDataXML( timestamp ) );

    byte[] byteTestValues = { 0, 1, 2, 3 };
    ValueMetaBase valueMetaBaseByteArray = new ValueMetaBase( byteTestValues.toString(), ValueMetaInterface.TYPE_STRING );
    valueMetaBaseByteArray.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    assertEquals(
      "<value-data><binary-string>" + Encode.forXml( XMLHandler.encodeBinaryData( byteTestValues ) )
        + "</binary-string>" + Const.CR + "</value-data>",
      valueMetaBaseByteArray.getDataXML( byteTestValues ) );
  }

  @Test
  public void testGetBinaryWithLength_WhenBinarySqlTypesOfVertica() throws Exception {
    final int binaryColumnIndex = 1;
    final int varbinaryColumnIndex = 2;
    final int expectedBinarylength = 1;
    final int expectedVarBinarylength = 80;

    ValueMetaBase obj = new ValueMetaBase();
    DatabaseMeta dbMeta = Mockito.spy( new DatabaseMeta() );
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
    assertTrue( ValueMetaInterface.TYPE_BINARY == binaryValueMeta.getType() );
    assertTrue( expectedBinarylength == binaryValueMeta.getLength() );
    Assert.assertFalse( binaryValueMeta.isLargeTextField() );

    // get value meta for varbinary type
    ValueMetaInterface varbinaryValueMeta =
      obj.getValueFromSQLType( dbMeta, TEST_NAME, metaData, varbinaryColumnIndex, false, false );
    assertNotNull( varbinaryValueMeta );
    assertTrue( TEST_NAME.equals( varbinaryValueMeta.getName() ) );
    assertTrue( ValueMetaInterface.TYPE_BINARY == varbinaryValueMeta.getType() );
    assertTrue( expectedVarBinarylength == varbinaryValueMeta.getLength() );
    Assert.assertFalse( varbinaryValueMeta.isLargeTextField() );

  }

  @Test
  public void testGetValueFromSQLTypeTypeOverride() throws Exception {
    final int varbinaryColumnIndex = 2;

    ValueMetaBase valueMetaBase = new ValueMetaBase(),
      valueMetaBaseSpy = Mockito.spy( valueMetaBase );
    DatabaseMeta dbMeta = Mockito.mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = Mockito.mock( DatabaseInterface.class );
    Mockito.doReturn( databaseInterface ).when( dbMeta ).getDatabaseInterface();

    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    valueMetaBaseSpy.getValueFromSQLType( dbMeta, TEST_NAME, metaData, varbinaryColumnIndex, false, false );

    Mockito.verify( databaseInterface, Mockito.times( 1 ) ).customizeValueFromSQLType( Mockito.any( ValueMetaInterface.class ),
      Mockito.any( ResultSetMetaData.class ), Mockito.anyInt() );
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
    Mockito.when( valueMetaInterface.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );

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
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "N" ) );
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "No" ) );
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "no" ) );
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "Yeah" ) );
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "False" ) );
    Assert.assertFalse( ValueMetaBase.convertStringToBoolean( "NOT false" ) );
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

    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    result =
      outValueMetaString.convertDataFromString( inputValueEmptyString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = N: "
        + "Conversion from empty string to string must return empty string", StringUtils.EMPTY, result );

    result =
      outValueMetaString.convertDataFromString( inputValueNullString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = N: "
        + "Conversion from null string must return null", null, result );

    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    result =
        outValueMetaString.convertDataFromString( inputValueEmptyString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = Y: "
        + "Conversion from empty string to string must return empty string", StringUtils.EMPTY, result );

    result =
        outValueMetaString.convertDataFromString( inputValueNullString, inValueMetaString, nullIf, ifNull, trim_type );
    assertEquals( "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = Y: "
        + "Conversion from null string must return empty string", StringUtils.EMPTY, result );
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

  @Test( expected = KettleValueException.class )
  public void testGetBigDecimalThrowsKettleValueException() throws KettleValueException {
    ValueMetaBase valueMeta = new ValueMetaBigNumber();
    valueMeta.getBigNumber( "1234567890" );
  }

  @Test( expected = KettleValueException.class )
  public void testGetIntegerThrowsKettleValueException() throws KettleValueException {
    ValueMetaBase valueMeta = new ValueMetaInteger();
    valueMeta.getInteger( "1234567890" );
  }

  @Test( expected = KettleValueException.class )
  public void testGetNumberThrowsKettleValueException() throws KettleValueException {
    ValueMetaBase valueMeta = new ValueMetaNumber();
    valueMeta.getNumber( "1234567890" );
  }

  @Test
  public void testIsNumeric() {
    int[] numTypes = { ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_NUMBER, ValueMetaInterface.TYPE_BIGNUMBER };
    for ( int type : numTypes ) {
      assertTrue( Integer.toString( type ), ValueMetaBase.isNumeric( type ) );
    }

    int[] notNumTypes = { ValueMetaInterface.TYPE_INET, ValueMetaInterface.TYPE_BOOLEAN, ValueMetaInterface.TYPE_BINARY, ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_STRING };
    for ( int type : notNumTypes ) {
      Assert.assertFalse( Integer.toString( type ), ValueMetaBase.isNumeric( type ) );
    }
  }

  @Test
  public void testGetAllTypes() {
    Assert.assertArrayEquals( ValueMetaBase.getAllTypes(), ValueMetaFactory.getAllValueMetaNames() );
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
    ValueMetaBase intMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_INTEGER );
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

    int1 = null;
    int2 = new Long( 6223372036854775804L );
    assertEquals( -1, intMeta.compare( int1, int2 ) );
    intMeta.setSortedDescending( true );
    assertEquals( 1, intMeta.compare( int1, int2 ) );

  }

  @Test
  public void testCompareIntegerToDouble() throws KettleValueException {
    ValueMetaBase intMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_INTEGER );
    Long int1 = new Long( 2L );
    ValueMetaBase numberMeta = new ValueMetaBase( "number", ValueMetaInterface.TYPE_NUMBER );
    Double double2 = new Double( 1.5 );
    assertEquals( 1, intMeta.compare( int1, numberMeta, double2 ) );
  }

  @Test
  public void testCompareDate() throws KettleValueException {
    ValueMetaBase dateMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_DATE );
    Date date1 = new Date( 6223372036854775804L );
    Date date2 = new Date( -6223372036854775804L );
    assertEquals( 1, dateMeta.compare( date1, date2 ) );
    assertEquals( -1, dateMeta.compare( date2, date1 ) );
    assertEquals( 0, dateMeta.compare( date1, date1 ) );
  }

  @Test
  public void testCompareDateWithStorageMask() throws KettleValueException {
    ValueMetaBase storageMeta = new ValueMetaBase( "string", ValueMetaInterface.TYPE_STRING );
    storageMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    storageMeta.setConversionMask( "MM/dd/yyyy HH:mm" );

    ValueMetaBase dateMeta = new ValueMetaBase( "date", ValueMetaInterface.TYPE_DATE );
    dateMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    dateMeta.setStorageMetadata( storageMeta );
    dateMeta.setConversionMask( "yyyy-MM-dd" );

    ValueMetaBase targetDateMeta = new ValueMetaBase( "date", ValueMetaInterface.TYPE_DATE );
    targetDateMeta.setConversionMask( "yyyy-MM-dd" );
    targetDateMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    String date = "2/24/2017 0:00";

    Date equalDate = new GregorianCalendar( 2017, Calendar.FEBRUARY, 24 ).getTime();
    assertEquals( 0, dateMeta.compare( date.getBytes(), targetDateMeta, equalDate ) );

    Date pastDate = new GregorianCalendar( 2017, Calendar.JANUARY, 24 ).getTime();
    assertEquals( 1, dateMeta.compare( date.getBytes(), targetDateMeta, pastDate ) );

    Date futureDate = new GregorianCalendar( 2017, Calendar.MARCH, 24 ).getTime();
    assertEquals( -1, dateMeta.compare( date.getBytes(), targetDateMeta, futureDate ) );
  }

  @Test
  public void testCompareDateNoStorageMask() throws KettleValueException {
    ValueMetaBase storageMeta = new ValueMetaBase( "string", ValueMetaInterface.TYPE_STRING );
    storageMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    storageMeta.setConversionMask( null ); // explicit set to null, to make sure test condition are met

    ValueMetaBase dateMeta = new ValueMetaBase( "date", ValueMetaInterface.TYPE_DATE );
    dateMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    dateMeta.setStorageMetadata( storageMeta );
    dateMeta.setConversionMask( "yyyy-MM-dd" );

    ValueMetaBase targetDateMeta = new ValueMetaBase( "date", ValueMetaInterface.TYPE_DATE );
    //targetDateMeta.setConversionMask( "yyyy-MM-dd" ); by not setting a maks, the default one is used
    //and since this is a date of normal storage it should work
    targetDateMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    String date = "2017/02/24 00:00:00.000";

    Date equalDate = new GregorianCalendar( 2017, Calendar.FEBRUARY, 24 ).getTime();
    assertEquals( 0, dateMeta.compare( date.getBytes(), targetDateMeta, equalDate ) );

    Date pastDate = new GregorianCalendar( 2017, Calendar.JANUARY, 24 ).getTime();
    assertEquals( 1, dateMeta.compare( date.getBytes(), targetDateMeta, pastDate ) );

    Date futureDate = new GregorianCalendar( 2017, Calendar.MARCH, 24 ).getTime();
    assertEquals( -1, dateMeta.compare( date.getBytes(), targetDateMeta, futureDate ) );
  }

  @Test
  public void testCompareBinary() throws KettleValueException {
    ValueMetaBase dateMeta = new ValueMetaBase( "int", ValueMetaInterface.TYPE_BINARY );
    byte[] value1 = new byte[] { 0, 1, 0, 0, 0, 1 };
    byte[] value2 = new byte[] { 0, 1, 0, 0, 0, 0 };
    assertEquals( 1, dateMeta.compare( value1, value2 ) );
    assertEquals( -1, dateMeta.compare( value2, value1 ) );
    assertEquals( 0, dateMeta.compare( value1, value1 ) );
  }

  @Test
  public void testDateParsing8601() throws Exception {
    ValueMetaBase dateMeta = new ValueMetaBase( "date", ValueMetaInterface.TYPE_DATE );
    dateMeta.setDateFormatLenient( false );

    // try to convert date by 'start-of-date' make - old behavior
    dateMeta.setConversionMask( "yyyy-MM-dd" );
    assertEquals( local( 1918, 3, 25, 0, 0, 0, 0 ), dateMeta.convertStringToDate( "1918-03-25T07:40:03.012+03:00" ) );

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

  @Test
  public void testDateToStringParse() throws Exception {
    ValueMetaBase dateMeta = new ValueMetaString( "date" );
    dateMeta.setDateFormatLenient( false );

    // try to convert date by 'start-of-date' make - old behavior
    dateMeta.setConversionMask( "yyyy-MM-dd" );
    assertEquals( local( 1918, 3, 25, 0, 0, 0, 0 ), dateMeta.convertStringToDate( "1918-03-25T07:40:03.012+03:00" ) );
  }

  @Test
  public void testSetPreparedStatementStringValueDontLogTruncated() throws KettleDatabaseException {
    ValueMetaBase valueMetaString = new ValueMetaBase( "LOG_FIELD", ValueMetaInterface.TYPE_STRING,  LOG_FIELD.length(), 0 );

    DatabaseMeta databaseMeta = Mockito.mock( DatabaseMeta.class );
    PreparedStatement preparedStatement = Mockito.mock( PreparedStatement.class );
    Mockito.when( databaseMeta.getMaxTextFieldLength() ).thenReturn( LOG_FIELD.length() );
    List<KettleLoggingEvent> events = listener.getEvents();
    assertEquals( 0, events.size() );

    valueMetaString.setPreparedStatementValue( databaseMeta, preparedStatement, 0, LOG_FIELD );

    //no logging occurred as max string length equals to logging text length
    assertEquals( 0, events.size() );
  }

  @Test
  public void testSetPreparedStatementStringValueLogTruncated() throws KettleDatabaseException {
    ValueMetaBase valueMetaString = new ValueMetaBase( "LOG_FIELD", ValueMetaInterface.TYPE_STRING,  LOG_FIELD.length(), 0 );

    DatabaseMeta databaseMeta = Mockito.mock( DatabaseMeta.class );
    PreparedStatement preparedStatement = Mockito.mock( PreparedStatement.class );
    Mockito.when( databaseMeta.getMaxTextFieldLength() ).thenReturn( MAX_TEXT_FIELD_LEN );
    List<KettleLoggingEvent> events = listener.getEvents();
    assertEquals( 0, events.size() );

    valueMetaString.setPreparedStatementValue( databaseMeta, preparedStatement, 0, LOG_FIELD );

    //check that truncated string was logged
    assertEquals( 1, events.size() );
  }

  @Test
  public void testValueMetaBaseOnlyHasOneLogger() throws NoSuchFieldException, IllegalAccessException {
    Field log = ValueMetaBase.class.getDeclaredField( "log" );
    assertTrue( Modifier.isStatic( log.getModifiers() ) );
    assertTrue( Modifier.isFinal( log.getModifiers() ) );
    log.setAccessible( true );
    try {
      assertEquals( LoggingRegistry.getInstance().findExistingLoggingSource( new LoggingObject( "ValueMetaBase" ) )
          .getLogChannelId(),
        ( (LogChannelInterface) log.get( null ) ).getLogChannelId() );
    } finally {
      log.setAccessible( false );
    }
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

  @Test
  public void testGetNativeDataTypeClass() {
    ValueMetaInterface base = new ValueMetaBase();
    Class<?> clazz = null;
    try {
      clazz = base.getNativeDataTypeClass();
      Assert.fail();
    } catch ( KettleValueException expected ) {
      // ValueMetaBase should throw an exception, as all sub-classes should override
      Assert.assertNull( clazz );
    }
  }

  @Test
  public void testConvertDataUsingConversionMetaDataForCustomMeta() {
    ValueMetaBase baseMeta = new ValueMetaBase( "CUSTOM_VALUEMETA_STRING", ValueMetaInterface.TYPE_STRING );
    baseMeta.setConversionMetadata( new ValueMetaBase( "CUSTOM", 999 ) );
    Object customData = new Object();
    try {
      baseMeta.convertDataUsingConversionMetaData( customData );
      Assert.fail( "Should have thrown a Kettle Value Exception with a proper message. Not a NPE stack trace" );
    } catch ( KettleValueException e ) {
      String expectedMessage = "CUSTOM_VALUEMETA_STRING String : I can't convert the specified value to data type : 999";
      assertEquals( expectedMessage, e.getMessage().trim() );
    }
  }

  @Test
  public void testConvertDataUsingConversionMetaData() throws KettleValueException, ParseException {
    ValueMetaString base = new ValueMetaString();
    double DELTA = 1e-15;

    base.setConversionMetadata( new ValueMetaString( "STRING" ) );
    Object defaultStringData = "STRING DATA";
    String convertedStringData = (String) base.convertDataUsingConversionMetaData( defaultStringData );
    assertEquals( "STRING DATA", convertedStringData );

    base.setConversionMetadata( new ValueMetaInteger( "INTEGER" ) );
    Object defaultIntegerData = "1";
    long convertedIntegerData = (long) base.convertDataUsingConversionMetaData( defaultIntegerData );
    assertEquals( 1, convertedIntegerData );


    base.setConversionMetadata( new ValueMetaNumber( "NUMBER" ) );
    Object defaultNumberData = "1.999";
    double convertedNumberData = (double) base.convertDataUsingConversionMetaData( defaultNumberData );
    assertEquals( 1.999, convertedNumberData, DELTA );

    ValueMetaInterface dateConversionMeta = new ValueMetaDate( "DATE" );
    dateConversionMeta.setDateFormatTimeZone( TimeZone.getTimeZone( "CST" ) );
    base.setConversionMetadata( dateConversionMeta );
    Object defaultDateData = "1990/02/18 00:00:00.000";
    Date date1 = new Date( 635320800000L );
    Date convertedDateData = (Date) base.convertDataUsingConversionMetaData( defaultDateData );
    assertEquals( date1, convertedDateData );

    base.setConversionMetadata( new ValueMetaBigNumber( "BIG_NUMBER" ) );
    Object defaultBigNumber = String.valueOf( BigDecimal.ONE );
    BigDecimal convertedBigNumber = (BigDecimal) base.convertDataUsingConversionMetaData( defaultBigNumber );
    assertEquals( BigDecimal.ONE, convertedBigNumber );

    base.setConversionMetadata( new ValueMetaBoolean( "BOOLEAN" ) );
    Object defaultBoolean = "true";
    boolean convertedBoolean = (boolean) base.convertDataUsingConversionMetaData( defaultBoolean );
    assertEquals( true, convertedBoolean );
  }

  @Test
  public void testGetCompatibleString() throws KettleValueException {
    ValueMetaInteger valueMetaInteger = new ValueMetaInteger( "INTEGER" );
    valueMetaInteger.setType( 5 ); // Integer
    valueMetaInteger.setStorageType( 1 ); // STORAGE_TYPE_BINARY_STRING

    assertEquals( "2", valueMetaInteger.getCompatibleString( new Long( 2 ) ) ); //BACKLOG-15750
  }

  @Test
  public void testReadDataInet() throws Exception {
    InetAddress localhost = InetAddress.getByName( "127.0.0.1" );
    byte[] address = localhost.getAddress();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream( byteArrayOutputStream );
    dataOutputStream.writeBoolean( false );
    dataOutputStream.writeInt( address.length );
    dataOutputStream.write( address );

    DataInputStream dis = new DataInputStream( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );
    ValueMetaBase vm = new ValueMetaInternetAddress();
    assertEquals( localhost, vm.readData( dis ) );
  }

  @Test
  public void testWriteDataInet() throws Exception {
    InetAddress localhost = InetAddress.getByName( "127.0.0.1" );
    byte[] address = localhost.getAddress();

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    DataOutputStream dos1 = new DataOutputStream( out1 );
    dos1.writeBoolean( false );
    dos1.writeInt( address.length );
    dos1.write( address );
    byte[] expected = out1.toByteArray();

    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
    DataOutputStream dos2 = new DataOutputStream( out2 );
    ValueMetaBase vm = new ValueMetaInternetAddress();
    vm.writeData( dos2, localhost );
    byte[] actual = out2.toByteArray();

    assertArrayEquals( expected, actual );
  }

  private class StoreLoggingEventListener implements KettleLoggingEventListener {

    private List<KettleLoggingEvent> events = new ArrayList<>();

    @Override
    public void eventAdded( KettleLoggingEvent event ) {
      events.add( event );
    }

    public List<KettleLoggingEvent> getEvents() {
      return events;
    }
  }

  @Test
  public void testConvertBigNumberToBoolean() {
    ValueMetaBase vmb = new ValueMetaBase();
    System.out.println( vmb.convertBigNumberToBoolean( new BigDecimal( "-234" ) ) );
    System.out.println( vmb.convertBigNumberToBoolean( new BigDecimal( "234" ) ) );
    System.out.println( vmb.convertBigNumberToBoolean( new BigDecimal( "0" ) ) );
    System.out.println( vmb.convertBigNumberToBoolean( new BigDecimal( "1.7976E308" ) ) );

    Assert.assertTrue( vmb.convertBigNumberToBoolean( new BigDecimal( "-234" ) ) );
    Assert.assertTrue( vmb.convertBigNumberToBoolean( new BigDecimal( "234" ) ) );
    Assert.assertFalse( vmb.convertBigNumberToBoolean( new BigDecimal( "0" ) ) );
    Assert.assertTrue( vmb.convertBigNumberToBoolean( new BigDecimal( "1.7976E308" ) ) );
  }


  //PDI-14721 ESR-5021
  @Test
  public void testGetValueFromSQLTypeBinaryMysql() throws Exception {

    final int binaryColumnIndex = 1;
    ValueMetaBase valueMetaBase = new ValueMetaBase();
    DatabaseMeta dbMeta = Mockito.spy( new DatabaseMeta() );
    DatabaseInterface databaseInterface = new MySQLDatabaseMeta();
    dbMeta.setDatabaseInterface( databaseInterface );

    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );

    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );
    Mockito.when( metaData.getColumnType( binaryColumnIndex ) ).thenReturn( Types.LONGVARBINARY );

    ValueMetaInterface binaryValueMeta =
      valueMetaBase.getValueFromSQLType( dbMeta, TEST_NAME, metaData, binaryColumnIndex, false, false );
    Assert.assertEquals( ValueMetaInterface.TYPE_BINARY, binaryValueMeta.getType() );
    Assert.assertTrue( binaryValueMeta.isBinary() );
  }

  @Test
  public void testGetValueFromNode() throws Exception {

    ValueMetaBase valueMetaBase = null;
    Node xmlNode = null;

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_STRING );
    xmlNode = XMLHandler.loadXMLString( "<value-data>String val</value-data>" ).getFirstChild();
    Assert.assertEquals( "String val", valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_NUMBER );
    xmlNode = XMLHandler.loadXMLString( "<value-data>689.2</value-data>" ).getFirstChild();
    Assert.assertEquals( 689.2, valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_NUMBER );
    xmlNode = XMLHandler.loadXMLString( "<value-data>689.2</value-data>" ).getFirstChild();
    Assert.assertEquals( 689.2, valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_INTEGER );
    xmlNode = XMLHandler.loadXMLString( "<value-data>68933</value-data>" ).getFirstChild();
    Assert.assertEquals( 68933l, valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_DATE );
    xmlNode = XMLHandler.loadXMLString( "<value-data>2017/11/27 08:47:10.000</value-data>" ).getFirstChild();
    Assert.assertEquals( XMLHandler.stringToDate( "2017/11/27 08:47:10.000" ), valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_TIMESTAMP );
    xmlNode = XMLHandler.loadXMLString( "<value-data>2017/11/27 08:47:10.123456789</value-data>" ).getFirstChild();
    Assert.assertEquals( XMLHandler.stringToTimestamp( "2017/11/27 08:47:10.123456789" ), valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_BOOLEAN );
    xmlNode = XMLHandler.loadXMLString( "<value-data>Y</value-data>" ).getFirstChild();
    Assert.assertEquals( true, valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_BINARY );
    byte[] bytes = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    String s = XMLHandler.encodeBinaryData( bytes );
    xmlNode = XMLHandler.loadXMLString( "<value-data>test<binary-value>" + s + "</binary-value></value-data>" ).getFirstChild();
    Assert.assertArrayEquals( bytes, (byte[]) valueMetaBase.getValue( xmlNode ) );

    valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_STRING );
    xmlNode = XMLHandler.loadXMLString( "<value-data></value-data>" ).getFirstChild();
    Assert.assertNull( valueMetaBase.getValue( xmlNode ) );
  }

  @Test( expected = KettleException.class )
  public void testGetValueUnknownType() throws Exception {
    ValueMetaBase valueMetaBase = new ValueMetaBase( "test", ValueMetaInterface.TYPE_NONE );
    valueMetaBase.getValue( XMLHandler.loadXMLString( "<value-data>not empty</value-data>" ).getFirstChild() );
  }

}
