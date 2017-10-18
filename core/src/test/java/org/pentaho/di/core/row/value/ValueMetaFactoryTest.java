/********************************************************************************
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

package org.pentaho.di.core.row.value;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaFactoryTest {

  @BeforeClass
  public static void beforeClassSetUp() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testClone() throws KettleException {
    ValueMetaInterface original = new ValueMetaString();
    original.setCollatorLocale( Locale.CANADA );
    original.setCollatorStrength( 3 );
    ValueMetaInterface cloned = ValueMetaFactory.cloneValueMeta( original );
    assertNotNull( cloned );
    assertNotSame( original, cloned );
    valueMetaDeepEquals( original, cloned );
  }

  private static void valueMetaDeepEquals( ValueMetaInterface expected, ValueMetaInterface actual ) {
    if ( expected == null && actual == null ) {
      return;
    }
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getType(), actual.getType() );
    assertEquals( expected.getLength(), actual.getLength() );
    assertEquals( expected.getPrecision(), actual.getPrecision() );
    assertEquals( expected.getConversionMask(), actual.getConversionMask() );
    assertEquals( expected.getDecimalSymbol(), actual.getDecimalSymbol() );
    assertEquals( expected.getGroupingSymbol(), actual.getGroupingSymbol() );
    assertEquals( expected.getStorageType(), actual.getStorageType() );
    assertEquals( expected.getStringEncoding(), actual.getStringEncoding() );
    assertEquals( expected.getTrimType(), actual.getTrimType() );
    assertEquals( expected.isDateFormatLenient(), actual.isDateFormatLenient() );
    assertEquals( expected.getDateFormatLocale(), actual.getDateFormatLocale() );
    assertEquals( expected.getDateFormatTimeZone(), actual.getDateFormatTimeZone() );
    assertEquals( expected.isLenientStringToNumber(), actual.isLenientStringToNumber() );
    assertEquals( expected.isLargeTextField(), actual.isLargeTextField() );
    assertEquals( expected.getComments(), actual.getComments() );
    assertEquals( expected.isCaseInsensitive(), actual.isCaseInsensitive() );
    assertEquals( expected.isCollatorDisabled(), actual.isCollatorDisabled() );
    assertEquals( expected.getCollatorStrength(), actual.getCollatorStrength() );
    assertArrayEquals( expected.getIndex(), actual.getIndex() );
    assertEquals( expected.getOrigin(), actual.getOrigin() );
    assertEquals( expected.isOriginalAutoIncrement(), actual.isOriginalAutoIncrement() );
    assertEquals( expected.getOriginalColumnType(), actual.getOriginalColumnType() );
    assertEquals( expected.getOriginalColumnTypeName(), actual.getOriginalColumnTypeName() );
    assertEquals( expected.isOriginalNullable(), actual.isOriginalNullable() );
    assertEquals( expected.getOriginalPrecision(), actual.getOriginalPrecision() );
    assertEquals( expected.getOriginalScale(), actual.getOriginalScale() );
    assertEquals( expected.isOriginalSigned(), actual.isOriginalSigned() );
    valueMetaDeepEquals( expected.getStorageMetadata(), actual.getStorageMetadata() );
  }

  @Test
  public void testCreateValueMeta() throws KettlePluginException {
    ValueMetaInterface testObject;

    try {
      testObject = ValueMetaFactory.createValueMeta( Integer.MIN_VALUE );
      fail();
    } catch ( KettlePluginException expected ) {
      // Do nothing, Integer.MIN_VALUE is not a valid option
    }

    try {
      testObject = ValueMetaFactory.createValueMeta( null, Integer.MIN_VALUE );
      fail();
    } catch ( KettlePluginException expected ) {
      // Do nothing, Integer.MIN_VALUE is not a valid option
    }

    try {
      testObject = ValueMetaFactory.createValueMeta( null, Integer.MIN_VALUE, 10, 10 );
      fail();
    } catch ( KettlePluginException expected ) {
      // Do nothing, Integer.MIN_VALUE is not a valid option
    }

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_NONE );
    assertTrue( testObject instanceof ValueMetaNone );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testNone", ValueMetaInterface.TYPE_NONE );
    assertTrue( testObject instanceof ValueMetaNone );
    assertEquals( "testNone", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testNone", ValueMetaInterface.TYPE_NONE, 10, 20 );
    assertTrue( testObject instanceof ValueMetaNone );
    assertEquals( "testNone", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_NUMBER );
    assertTrue( testObject instanceof ValueMetaNumber );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testNumber", ValueMetaInterface.TYPE_NUMBER );
    assertTrue( testObject instanceof ValueMetaNumber );
    assertEquals( "testNumber", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testNumber", ValueMetaInterface.TYPE_NUMBER, 10, 20 );
    assertTrue( testObject instanceof ValueMetaNumber );
    assertEquals( "testNumber", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_STRING );
    assertTrue( testObject instanceof ValueMetaString );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testString", ValueMetaInterface.TYPE_STRING );
    assertTrue( testObject instanceof ValueMetaString );
    assertEquals( "testString", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testString", ValueMetaInterface.TYPE_STRING, 1000, 50 );
    assertTrue( testObject instanceof ValueMetaString );
    assertEquals( "testString", testObject.getName() );
    assertEquals( 1000, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() ); // Special case for String

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_DATE );
    assertTrue( testObject instanceof ValueMetaDate );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testDate", ValueMetaInterface.TYPE_DATE );
    assertTrue( testObject instanceof ValueMetaDate );
    assertEquals( "testDate", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testDate", ValueMetaInterface.TYPE_DATE, 10, 20 );
    assertTrue( testObject instanceof ValueMetaDate );
    assertEquals( "testDate", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_BOOLEAN );
    assertTrue( testObject instanceof ValueMetaBoolean );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testBoolean", ValueMetaInterface.TYPE_BOOLEAN );
    assertTrue( testObject instanceof ValueMetaBoolean );
    assertEquals( "testBoolean", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testBoolean", ValueMetaInterface.TYPE_BOOLEAN, 10, 20 );
    assertTrue( testObject instanceof ValueMetaBoolean );
    assertEquals( "testBoolean", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_INTEGER );
    assertTrue( testObject instanceof ValueMetaInteger );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Integer

    testObject = ValueMetaFactory.createValueMeta( "testInteger", ValueMetaInterface.TYPE_INTEGER );
    assertTrue( testObject instanceof ValueMetaInteger );
    assertEquals( "testInteger", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Integer

    testObject = ValueMetaFactory.createValueMeta( "testInteger", ValueMetaInterface.TYPE_INTEGER, 10, 20 );
    assertTrue( testObject instanceof ValueMetaInteger );
    assertEquals( "testInteger", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Integer

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_BIGNUMBER );
    assertTrue( testObject instanceof ValueMetaBigNumber );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testBigNumber", ValueMetaInterface.TYPE_BIGNUMBER );
    assertTrue( testObject instanceof ValueMetaBigNumber );
    assertEquals( "testBigNumber", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testBigNumber", ValueMetaInterface.TYPE_BIGNUMBER, 10, 20 );
    assertTrue( testObject instanceof ValueMetaBigNumber );
    assertEquals( "testBigNumber", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_SERIALIZABLE );
    assertTrue( testObject instanceof ValueMetaSerializable );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testSerializable", ValueMetaInterface.TYPE_SERIALIZABLE );
    assertTrue( testObject instanceof ValueMetaSerializable );
    assertEquals( "testSerializable", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testSerializable", ValueMetaInterface.TYPE_SERIALIZABLE, 10, 20 );
    assertTrue( testObject instanceof ValueMetaSerializable );
    assertEquals( "testSerializable", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_BINARY );
    assertTrue( testObject instanceof ValueMetaBinary );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Binary

    testObject = ValueMetaFactory.createValueMeta( "testBinary", ValueMetaInterface.TYPE_BINARY );
    assertTrue( testObject instanceof ValueMetaBinary );
    assertEquals( "testBinary", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Binary

    testObject = ValueMetaFactory.createValueMeta( "testBinary", ValueMetaInterface.TYPE_BINARY, 10, 20 );
    assertTrue( testObject instanceof ValueMetaBinary );
    assertEquals( "testBinary", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 0, testObject.getPrecision() ); // Special case for Binary

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_TIMESTAMP );
    assertTrue( testObject instanceof ValueMetaTimestamp );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testTimestamp", ValueMetaInterface.TYPE_TIMESTAMP );
    assertTrue( testObject instanceof ValueMetaTimestamp );
    assertEquals( "testTimestamp", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testTimestamp", ValueMetaInterface.TYPE_TIMESTAMP, 10, 20 );
    assertTrue( testObject instanceof ValueMetaTimestamp );
    assertEquals( "testTimestamp", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( ValueMetaInterface.TYPE_INET );
    assertTrue( testObject instanceof ValueMetaInternetAddress );
    assertEquals( null, testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testInternetAddress", ValueMetaInterface.TYPE_INET );
    assertTrue( testObject instanceof ValueMetaInternetAddress );
    assertEquals( "testInternetAddress", testObject.getName() );
    assertEquals( -1, testObject.getLength() );
    assertEquals( -1, testObject.getPrecision() );

    testObject = ValueMetaFactory.createValueMeta( "testInternetAddress", ValueMetaInterface.TYPE_INET, 10, 20 );
    assertTrue( testObject instanceof ValueMetaInternetAddress );
    assertEquals( "testInternetAddress", testObject.getName() );
    assertEquals( 10, testObject.getLength() );
    assertEquals( 20, testObject.getPrecision() );
  }

  @Test
  public void testGetValueMetaNames() {
    List<String> dataTypes = Arrays.<String>asList( ValueMetaFactory.getValueMetaNames() );

    assertTrue( dataTypes.contains( "Number" ) );
    assertTrue( dataTypes.contains( "String" ) );
    assertTrue( dataTypes.contains( "Date" ) );
    assertTrue( dataTypes.contains( "Boolean" ) );
    assertTrue( dataTypes.contains( "Integer" ) );
    assertTrue( dataTypes.contains( "BigNumber" ) );
    assertFalse( dataTypes.contains( "Serializable" ) );
    assertTrue( dataTypes.contains( "Binary" ) );
    assertTrue( dataTypes.contains( "Timestamp" ) );
    assertTrue( dataTypes.contains( "Internet Address" ) );
  }

  @Test
  public void testGetAllValueMetaNames() {
    List<String> dataTypes = Arrays.<String>asList( ValueMetaFactory.getAllValueMetaNames() );

    assertTrue( dataTypes.contains( "Number" ) );
    assertTrue( dataTypes.contains( "String" ) );
    assertTrue( dataTypes.contains( "Date" ) );
    assertTrue( dataTypes.contains( "Boolean" ) );
    assertTrue( dataTypes.contains( "Integer" ) );
    assertTrue( dataTypes.contains( "BigNumber" ) );
    assertTrue( dataTypes.contains( "Serializable" ) );
    assertTrue( dataTypes.contains( "Binary" ) );
    assertTrue( dataTypes.contains( "Timestamp" ) );
    assertTrue( dataTypes.contains( "Internet Address" ) );
  }

  @Test
  public void testGetValueMetaName() {
    assertEquals( "-", ValueMetaFactory.getValueMetaName( Integer.MIN_VALUE ) );
    assertEquals( "None", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_NONE ) );
    assertEquals( "Number", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_NUMBER ) );
    assertEquals( "String", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) );
    assertEquals( "Date", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_DATE ) );
    assertEquals( "Boolean", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_BOOLEAN ) );
    assertEquals( "Integer", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ) );
    assertEquals( "BigNumber", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_BIGNUMBER ) );
    assertEquals( "Serializable", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_SERIALIZABLE ) );
    assertEquals( "Binary", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_BINARY ) );
    assertEquals( "Timestamp", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_TIMESTAMP ) );
    assertEquals( "Internet Address", ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INET ) );
  }

  @Test
  public void testGetIdForValueMeta() {
    assertEquals( ValueMetaInterface.TYPE_NONE, ValueMetaFactory.getIdForValueMeta( null ) );
    assertEquals( ValueMetaInterface.TYPE_NONE, ValueMetaFactory.getIdForValueMeta( "" ) );
    assertEquals( ValueMetaInterface.TYPE_NONE, ValueMetaFactory.getIdForValueMeta( "None" ) );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, ValueMetaFactory.getIdForValueMeta( "Number" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, ValueMetaFactory.getIdForValueMeta( "String" ) );
    assertEquals( ValueMetaInterface.TYPE_DATE, ValueMetaFactory.getIdForValueMeta( "Date" ) );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, ValueMetaFactory.getIdForValueMeta( "Boolean" ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, ValueMetaFactory.getIdForValueMeta( "Integer" ) );
    assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, ValueMetaFactory.getIdForValueMeta( "BigNumber" ) );
    assertEquals( ValueMetaInterface.TYPE_SERIALIZABLE, ValueMetaFactory.getIdForValueMeta( "Serializable" ) );
    assertEquals( ValueMetaInterface.TYPE_BINARY, ValueMetaFactory.getIdForValueMeta( "Binary" ) );
    assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, ValueMetaFactory.getIdForValueMeta( "Timestamp" ) );
    assertEquals( ValueMetaInterface.TYPE_INET, ValueMetaFactory.getIdForValueMeta( "Internet Address" ) );
  }

  @Test
  public void testGetValueMetaPluginClasses() throws KettlePluginException {
    List<ValueMetaInterface> dataTypes = ValueMetaFactory.getValueMetaPluginClasses();

    boolean numberExists = false;
    boolean stringExists = false;
    boolean dateExists = false;
    boolean booleanExists = false;
    boolean integerExists = false;
    boolean bignumberExists = false;
    boolean serializableExists = false;
    boolean binaryExists = false;
    boolean timestampExists = false;
    boolean inetExists = false;

    for ( ValueMetaInterface obj : dataTypes ) {
      if ( obj instanceof ValueMetaNumber ) {
        numberExists = true;
      }
      if ( obj.getClass().equals( ValueMetaString.class ) ) {
        stringExists = true;
      }
      if ( obj.getClass().equals( ValueMetaDate.class ) ) {
        dateExists = true;
      }
      if ( obj.getClass().equals( ValueMetaBoolean.class ) ) {
        booleanExists = true;
      }
      if ( obj.getClass().equals( ValueMetaInteger.class ) ) {
        integerExists = true;
      }
      if ( obj.getClass().equals( ValueMetaBigNumber.class ) ) {
        bignumberExists = true;
      }
      if ( obj.getClass().equals( ValueMetaSerializable.class ) ) {
        serializableExists = true;
      }
      if ( obj.getClass().equals( ValueMetaBinary.class ) ) {
        binaryExists = true;
      }
      if ( obj.getClass().equals( ValueMetaTimestamp.class ) ) {
        timestampExists = true;
      }
      if ( obj.getClass().equals( ValueMetaInternetAddress.class ) ) {
        inetExists = true;
      }
    }

    assertTrue( numberExists );
    assertTrue( stringExists );
    assertTrue( dateExists );
    assertTrue( booleanExists );
    assertTrue( integerExists );
    assertTrue( bignumberExists );
    assertTrue( serializableExists );
    assertTrue( binaryExists );
    assertTrue( timestampExists );
    assertTrue( inetExists );
  }

  @Test
  public void testGuessValueMetaInterface() {
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new BigDecimal( 1.0 ) ) instanceof ValueMetaBigNumber );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new Double( 1.0 ) ) instanceof ValueMetaNumber );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new Long( 1 ) ) instanceof ValueMetaInteger );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new String() ) instanceof ValueMetaString );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new Date() ) instanceof ValueMetaDate );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new Boolean( false ) ) instanceof ValueMetaBoolean );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new Boolean( true ) ) instanceof ValueMetaBoolean );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( false ) instanceof ValueMetaBoolean );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( true ) instanceof ValueMetaBoolean );
    assertTrue( ValueMetaFactory.guessValueMetaInterface( new byte[10] ) instanceof ValueMetaBinary );

    // Test Unsupported Data Types
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( null ) );
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( new Short( (short) 1 ) ) );
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( new Byte( (byte) 1 ) ) );
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( new Float( 1.0 ) ) );
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( new StringBuilder() ) );
    assertEquals( null, ValueMetaFactory.guessValueMetaInterface( (byte) 1 ) );
  }

  @Test
  public void testGetNativeDataTypeClass() throws KettlePluginException {
    for ( String valueMetaName : ValueMetaFactory.getValueMetaNames() ) {
      int valueMetaID = ValueMetaFactory.getIdForValueMeta( valueMetaName );
      ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( valueMetaID );
      try {
        Class<?> clazz = valueMeta.getNativeDataTypeClass();
        assertNotNull( clazz );
      } catch ( KettleValueException kve ) {
        fail( valueMetaName + " should implement getNativeDataTypeClass()" );
      }
    }
  }
}
