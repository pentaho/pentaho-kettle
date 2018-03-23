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

import org.junit.Test;

public class ExcelInputContentParsingTest extends BaseExcelParsingTest {
  @Test
  public void testXLS() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.JXL );
    init( "sample.xls" );

    setFields( new ExcelInputField(), new ExcelInputField() );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testXLSX() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.POI );
    init( "sample.xlsx" );

    setFields( new ExcelInputField(), new ExcelInputField() );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testXLSXStream() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "sample.xlsx" );

    setFields( new ExcelInputField(), new ExcelInputField() );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testODS24() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.ODS );
    init( "sample-2.4.ods" );

    setFields( new ExcelInputField(), new ExcelInputField() );

    process();

    check( new Object[][] { { "test", null }, { "test", "test" } } );
  }

  @Test
  public void testODS341() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.ODS );
    init( "sample-3.4.1.ods" );

    setFields( new ExcelInputField(), new ExcelInputField() );

    process();

    check( new Object[][] { { "AAABBC", "Nissan" }, { "AAABBC", "Nissan" }, { "AAABBC", "Nissan" }, { "AAABBC",
        "Nissan" } } );
  }

  @Test
  public void testXLSXCompressionRatioIsBig() throws Exception {
    meta.setSpreadSheetType( SpreadSheetType.SAX_POI );
    init( "Balance_Type_Codes.xlsx" );

    setFields( new ExcelInputField( "FIST ID", -1, -1 ), new ExcelInputField( "SOURCE SYSTEM", -1, -1 ) );

    process();

    checkErrors();
    checkContent( new Object[][] { { "FIST0200", "ACM" } } );
  }
}
