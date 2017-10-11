/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.trans.steps.excelinput;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.ods.OdfSheet;

public class OdfSheetTest {

  private KWorkbook ods341;
  private KWorkbook ods24;

  @Before
  public void init() throws KettleException {
    ods341 =
        WorkbookFactory.getWorkbook( SpreadSheetType.ODS, this.getClass().getResource( "files/sample-3.4.1.ods" )
            .getPath(), null );
    ods24 =
        WorkbookFactory.getWorkbook( SpreadSheetType.ODS, this.getClass().getResource( "files/sample-2.4.ods" )
            .getPath(), null );
  }

  @Test
  public void testRowColumnsCount() {

    String sameRowWidthSheet = "SameRowWidth";
    String diffRowWidthSheet = "DifferentRowWidth";

    checkRowCount( (OdfSheet) ods341.getSheet( sameRowWidthSheet ), 3, "Row count mismatch for ODF v3.4.1" );
    checkRowCount( (OdfSheet) ods24.getSheet( sameRowWidthSheet ), 2, "Row count mismatch for ODF v2.4" );
    checkRowCount( (OdfSheet) ods341.getSheet( diffRowWidthSheet ), 3, "Row count mismatch for ODF v3.4.1" );
    checkRowCount( (OdfSheet) ods24.getSheet( diffRowWidthSheet ), 2, "Row count mismatch for ODF v2.4" );

    checkCellCount( (OdfSheet) ods341.getSheet( sameRowWidthSheet ), 15, "Cell count mismatch for ODF v3.4.1" );
    checkCellCount( (OdfSheet) ods24.getSheet( sameRowWidthSheet ), 1, "Cell count mismatch for ODF v2.4" );
    checkCellCount( (OdfSheet) ods341.getSheet( diffRowWidthSheet ), new int[] { 15, 15, 12 },
        "Cell count mismatch for ODF v3.4.1" );
    checkCellCount( (OdfSheet) ods24.getSheet( diffRowWidthSheet ), new int[] { 3, 2 },
        "Cell count mismatch for ODF v2.4" );

  }

  private void checkRowCount( OdfSheet sheet, int expected, String failMsg ) {
    int actual = sheet.getRows();
    assertEquals( failMsg, expected, actual );
  }

  private void checkCellCount( OdfSheet sheet, int expected, String failMsg ) {
    int rowNo = sheet.getRows();
    for ( int i = 0; i < rowNo; i++ ) {
      KCell[] row = sheet.getRow( i );
      assertEquals( failMsg + "; Row content: " + rowToString( row ), expected, row.length );
    }
  }

  private void checkCellCount( OdfSheet sheet, int[] expected, String failMsg ) {
    int rowNo = sheet.getRows();
    assertEquals( "Row count mismatch", expected.length, rowNo );
    for ( int i = 0; i < rowNo; i++ ) {
      KCell[] row = sheet.getRow( i );
      assertEquals( failMsg + "; Row content: " + rowToString( row ), expected[i], row.length );
    }
  }

  private String rowToString( KCell[] row ) {
    if ( row == null || row.length == 0 ) {
      return "";
    }
    String result = cellToStr( row[0] );
    for ( int j = 1; j < row.length; j++ ) {
      result += "," + cellToStr( row[j] );
    }
    return result;
  }

  private String cellToStr( KCell cell ) {
    String result = "null";
    if ( cell != null ) {
      result = cell.getContents();
    }
    return result;
  }
}
