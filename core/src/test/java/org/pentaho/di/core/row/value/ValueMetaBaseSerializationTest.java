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
package org.pentaho.di.core.row.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * @author Andrey Khayrutdinov
 */
public class ValueMetaBaseSerializationTest {

  @Test
  public void restoresMetaData_storageTypeNormal() throws Exception {
    ValueMetaBase vmb = createTestObject( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    checkRestoring( vmb );
  }

  @Test
  public void restoresMetaData_storageTypeIndexed() throws Exception {
    ValueMetaBase vmb = createTestObject( ValueMetaInterface.STORAGE_TYPE_INDEXED );
    vmb.setIndex( new Object[] { "qwerty", "asdfg" } );

    checkRestoring( vmb );
  }

  @Test
  public void restoresMetaData_storageTypeBinaryString() throws Exception {
    ValueMetaBase vmb = createTestObject( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    vmb.setStorageMetadata( new ValueMetaBase( "storageMetadataInstance", ValueMetaInterface.TYPE_STRING ) );

    checkRestoring( vmb );
  }

  private static ValueMetaBase createTestObject( int storageType ) {
    ValueMetaBase vmb = new ValueMetaBase( "test", ValueMetaInterface.TYPE_STRING );
    vmb.setStorageType( storageType );
    vmb.setLength( 10, 5 );
    vmb.setOrigin( "origin" );
    vmb.setComments( "comments" );
    vmb.setConversionMask( "conversionMask" );
    vmb.setDecimalSymbol( "decimalSymbol" );
    vmb.setGroupingSymbol( "groupingSymbol" );
    vmb.setCurrencySymbol( "currencySymbol" );
    vmb.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    vmb.setCaseInsensitive( true );
    vmb.setSortedDescending( true );
    vmb.setOutputPaddingEnabled( true );
    vmb.setDateFormatLenient( true );
    vmb.setLenientStringToNumber( true );
    vmb.setDateFormatLocale( Locale.JAPAN );
    vmb.setCollatorDisabled( false );
    vmb.setCollatorLocale( Locale.JAPANESE );
    vmb.setCollatorStrength( 1 );

    String[] zones = TimeZone.getAvailableIDs();
    vmb.setDateFormatTimeZone( TimeZone.getTimeZone( zones[ new Random().nextInt( zones.length ) ] ) );

    return vmb;
  }

  private static void checkRestoring( ValueMetaBase initial ) throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    initial.writeMeta( new DataOutputStream( os ) );

    DataInputStream dataInputStream = new DataInputStream( new ByteArrayInputStream( os.toByteArray() ) );
    // an awkward hack, since readMetaData() expects object's type to have been read
    int restoredType = dataInputStream.readInt();
    assertEquals( "type", initial.getType(), restoredType );

    ValueMetaBase restored = new ValueMetaBase( initial.getName(), restoredType );
    restored.readMetaData( dataInputStream );

    assertMetaDataAreEqual( initial, restored );
  }

  private static void assertMetaDataAreEqual( ValueMetaInterface expected, ValueMetaInterface actual ) {
    assertEquals( "storageType", expected.getStorageType(), actual.getStorageType() );

    if ( expected.getIndex() == null ) {
      assertNull( "index", actual.getIndex() );
    } else {
      assertEquals( "index.length", expected.getIndex().length, actual.getIndex().length );
      for ( int i = 0; i < expected.getIndex().length; i++ ) {
        assertEquals( "index[" + i + "]", expected.getIndex()[ i ], actual.getIndex()[ i ] );
      }
    }

    if ( expected.getStorageMetadata() == null ) {
      assertNull( "storageMetadata", actual.getStorageMetadata() );
    } else {
      assertMetaDataAreEqual( expected.getStorageMetadata(), actual.getStorageMetadata() );
    }

    assertEquals( "name", expected.getName(), actual.getName() );
    assertEquals( "length", expected.getLength(), actual.getLength() );
    assertEquals( "origin", expected.getOrigin(), actual.getOrigin() );
    assertEquals( "comments", expected.getComments(), actual.getComments() );
    assertEquals( "conversionMask", expected.getConversionMask(), actual.getConversionMask() );
    assertEquals( "decimalSymbol", expected.getDecimalSymbol(), actual.getDecimalSymbol() );
    assertEquals( "groupingSymbol", expected.getGroupingSymbol(), actual.getGroupingSymbol() );
    assertEquals( "currencySymbol", expected.getCurrencySymbol(), actual.getCurrencySymbol() );
    assertEquals( "trimType", expected.getTrimType(), actual.getTrimType() );
    assertEquals( "caseInsensitive", expected.isCaseInsensitive(), actual.isCaseInsensitive() );
    assertEquals( "sortedDescending", expected.isSortedDescending(), actual.isSortedDescending() );
    assertEquals( "outputPaddingEnabled", expected.isOutputPaddingEnabled(), actual.isOutputPaddingEnabled() );
    assertEquals( "dateFormatLenient", expected.isDateFormatLenient(), actual.isDateFormatLenient() );
    assertEquals( "dateFormatLocale", expected.getDateFormatLocale(), actual.getDateFormatLocale() );
    assertEquals( "dateFormatTimeZone", expected.getDateFormatTimeZone(), actual.getDateFormatTimeZone() );
    assertEquals( "lenientStringToNumber", expected.isLenientStringToNumber(), actual.isLenientStringToNumber() );
    assertEquals( "collatorDisabled", expected.isCollatorDisabled(), actual.isCollatorDisabled() );
    assertEquals( "collatorLocale", expected.getCollatorLocale(), actual.getCollatorLocale() );
    assertEquals( "collatorStrength", expected.getCollatorStrength(), actual.getCollatorStrength() );
  }
}
