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

package org.pentaho.di.trans.steps.excelinput;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import static org.junit.Assert.assertEquals;

public class ExcelInputContentParsingTest extends BaseExcelParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  public void before() {
    super.before();

    System.clearProperty( Const.KETTLE_ZIP_MAX_ENTRY_SIZE );
    System.clearProperty( Const.KETTLE_ZIP_MAX_TEXT_SIZE );
    System.clearProperty( Const.KETTLE_ZIP_MIN_INFLATE_RATIO );
  }

  @Test
  public void testXLS() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.JXL );
    init( "sample.xls" );

    setFields( new ExcelInputField( "f1", -1, -1 ), new ExcelInputField( "f2", -1, -1 ) );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testXLSX() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.POI );
    init( "sample.xlsx" );

    setFields( new ExcelInputField( "f1", -1, -1 ), new ExcelInputField( "f2", -1, -1 ) );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testXLSXStream() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "sample.xlsx" );

    setFields( new ExcelInputField( "f1", -1, -1 ), new ExcelInputField( "f2", -1, -1 ) );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testODS24() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.ODS );
    init( "sample-2.4.ods" );

    setFields( new ExcelInputField( "f1", -1, -1 ), new ExcelInputField( "f2", -1, -1 ) );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testODS341() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.ODS );
    init( "sample-3.4.1.ods" );

    setFields( new ExcelInputField( "f1", -1, -1 ), new ExcelInputField( "f2", -1, -1 ) );

    process();

    check( new Object[][] { { "AAABBC", "Nissan" }, { "AAABBC", "Nissan" }, { "AAABBC", "Nissan" }, { "AAABBC",
        "Nissan" } } );
  }

  @Test
  public void testZipBombConfiguration_Default() throws Exception {

    // First set some random values
    Long bogusMaxEntrySize = 1000L;
    ZipSecureFile.setMaxEntrySize( bogusMaxEntrySize );
    Long bogusMaxTextSize = 1000L;
    ZipSecureFile.setMaxTextSize( bogusMaxTextSize );
    Double bogusMinInflateRatio = 0.5d;
    ZipSecureFile.setMinInflateRatio( bogusMinInflateRatio );

    // Verify that the bogus values were set
    assertEquals( bogusMaxEntrySize, (Long) ZipSecureFile.getMaxEntrySize() );
    assertEquals( bogusMaxTextSize, (Long) ZipSecureFile.getMaxTextSize() );
    assertEquals( bogusMinInflateRatio, (Double) ZipSecureFile.getMinInflateRatio() );

    // Initializing the ExcelInput step should make the new values to be set
    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "Balance_Type_Codes.xlsx" );

    // Verify that the default values were used
    assertEquals( Const.KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT, (Long) ZipSecureFile.getMaxEntrySize() );
    assertEquals( Const.KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT, (Long) ZipSecureFile.getMaxTextSize() );
    assertEquals( Const.KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT, (Double) ZipSecureFile.getMinInflateRatio() );
  }

  @Test
  public void testZipBombConfiguration() throws Exception {
    Long maxEntrySizeVal = 3L * 1024 * 1024 * 1024;
    Long maxTextSizeVal = 2L * 1024 * 1024 * 1024;
    Double minInflateRatioVal = 0.123d;

    // First set the property values
    System.setProperty( Const.KETTLE_ZIP_MAX_ENTRY_SIZE, maxEntrySizeVal.toString() );
    System.setProperty( Const.KETTLE_ZIP_MAX_TEXT_SIZE, maxTextSizeVal.toString() );
    System.setProperty( Const.KETTLE_ZIP_MIN_INFLATE_RATIO, minInflateRatioVal.toString() );
    //ExcelInput excelInput = new ExcelInput( null, null, 0, null, null );

    // Initializing the ExcelInput step should make the new values to be set

    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "Balance_Type_Codes.xlsx" );

    // Verify that the setted values were used
    assertEquals( maxEntrySizeVal, (Long) ZipSecureFile.getMaxEntrySize() );
    assertEquals( maxTextSizeVal, (Long) ZipSecureFile.getMaxTextSize() );
    assertEquals( minInflateRatioVal, (Double) ZipSecureFile.getMinInflateRatio() );
  }

  @Test
  public void testXLSXCompressionRatioIsBig() throws Exception {

    // For this zip to be correctly handed, we need to allow a lower inflate ratio
    Double minInflateRatio = 0.007d;
    System.setProperty( Const.KETTLE_ZIP_MIN_INFLATE_RATIO, minInflateRatio.toString() );

    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "Balance_Type_Codes.xlsx" );

    // Verify that the minimum allowed inflate ratio is the expected
    assertEquals( minInflateRatio, (Double) ZipSecureFile.getMinInflateRatio() );

    setFields( new ExcelInputField( "FIST ID", -1, -1 ), new ExcelInputField( "SOURCE SYSTEM", -1, -1 ) );

    process();

    checkErrors();
    checkContent( new Object[][] { { "FIST0200", "ACM" } } );
  }
}
