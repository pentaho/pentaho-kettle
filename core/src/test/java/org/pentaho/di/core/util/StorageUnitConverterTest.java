/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.util;

import org.junit.*;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class StorageUnitConverterTest {

  long B = 1;
  long KB = B * 1024;
  long MB = KB * 1024;
  long GB = MB  * 1024;
  double DELTA = 10;

  @Test
  public void byteCountToDisplaySize() {

    StorageUnitConverter storageUnitConverter = new StorageUnitConverter();

    // TEST 1
    assertEquals( "0B", storageUnitConverter.byteCountToDisplaySize( 0 ) );

    // TEST 2
    assertEquals( "5B", storageUnitConverter.byteCountToDisplaySize( 5 ) );

    // TEST 3
    assertEquals( "13KB", storageUnitConverter.byteCountToDisplaySize( 13 * KB ) );

    // TEST 4
    assertEquals( "13KB", storageUnitConverter.byteCountToDisplaySize( (long) ( 13.1 * KB ) ) );

    // TEST 4
    assertEquals( "128MB", storageUnitConverter.byteCountToDisplaySize( (long) ( 128.9 * MB ) ) );

    // TEST 5
    assertEquals( "6GB", storageUnitConverter.byteCountToDisplaySize( (long) ( 6 * GB ) ) );
  }

  @Test
  public void displaySizeToByteCount_wholeNumbers() {

    StorageUnitConverter storageUnitConverter = new StorageUnitConverter();

    // TEST 1: whole number Byte
    assertEquals( 34 * B, storageUnitConverter.displaySizeToByteCount( "34B" ) );

    // TEST 2: whole number kilobyte
    assertEquals( 121 * KB, storageUnitConverter.displaySizeToByteCount( "121KB" ) );

    // TEST 3: whole number megabyte
    assertEquals( 5 * MB, storageUnitConverter.displaySizeToByteCount( "5MB" ) );

    // TEST 4: whole number gigabyte
    assertEquals( 2 * GB, storageUnitConverter.displaySizeToByteCount( "2GB" ) );

    // TEST 5: missing number
    assertEquals( -1, storageUnitConverter.displaySizeToByteCount( "GB" ) );

    // TEST 6: missing number
    assertEquals( -1, storageUnitConverter.displaySizeToByteCount( "B" ) );

    // TEST 7: letter for number
    assertEquals( -1, storageUnitConverter.displaySizeToByteCount( "XGB" ) );

    // TEST 8: 0
    assertEquals( 0, storageUnitConverter.displaySizeToByteCount( "0KB" ) );

    // TEST 9: empty string
    assertEquals( -1L, storageUnitConverter.displaySizeToByteCount( "" ) );

    // TEST 10: null
    assertEquals( -1L, storageUnitConverter.displaySizeToByteCount( null ) );
  }

  @Test
  public void displaySizeToByteCount_decimalNumbers() {

    StorageUnitConverter storageUnitConverter = new StorageUnitConverter();

    // TEST 1: whole number Byte
    assertEquals( 34.02 * B, storageUnitConverter.displaySizeToByteCount( "34.02B" ), DELTA );

    // TEST 2: whole number kilobyte
    assertEquals( 121.5 * KB, storageUnitConverter.displaySizeToByteCount( "121.5KB" ), DELTA );

    // TEST 3: whole number megabyte
    assertEquals( 5.7 * MB, storageUnitConverter.displaySizeToByteCount( "5.7MB" ), DELTA );

    // TEST 4: whole number gigabyte
    assertEquals( 2.25 * GB, storageUnitConverter.displaySizeToByteCount( "2.25GB" ), DELTA );

    // TEST 5: whole number gigabyte
    assertEquals( 2.25 * GB, storageUnitConverter.displaySizeToByteCount( "2,25GB" ), DELTA );
  }

}
