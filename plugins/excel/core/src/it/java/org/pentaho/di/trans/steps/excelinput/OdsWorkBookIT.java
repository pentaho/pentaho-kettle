/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.excelinput;

import junit.framework.TestCase;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;

import java.util.Date;

public class OdsWorkBookIT extends TestCase {
  public void testRead() throws Exception {
    KWorkbook workbook = WorkbookFactory.getWorkbook( SpreadSheetType.ODS, "src/it/resources/sample-file.ods", null );
    int numberOfSheets = workbook.getNumberOfSheets();
    assertEquals( 3, numberOfSheets );
    KSheet sheet1 = workbook.getSheet( 0 );
    assertEquals( "Sheet1", sheet1.getName() );
    sheet1 = workbook.getSheet( "Sheet1" );
    assertEquals( "Sheet1", sheet1.getName() );

    assertEquals( 5, sheet1.getRows() );

    KCell[] row = sheet1.getRow( 2 );
    assertEquals( KCellType.LABEL, row[1].getType() );
    assertEquals( "One", row[1].getValue() );
    assertEquals( KCellType.DATE, row[2].getType() );
    assertEquals( new Date( 1283817600000L ), row[2].getValue() );
    assertEquals( KCellType.NUMBER, row[3].getType() );
    assertEquals( Double.valueOf( "75" ), row[3].getValue() );
    assertEquals( KCellType.BOOLEAN_FORMULA, row[4].getType() ); // Always like that in ODS
    assertEquals( Boolean.valueOf( true ), row[4].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[5].getType() );
    assertEquals( Double.valueOf( "75" ), row[5].getValue() );

    row = sheet1.getRow( 3 );
    assertEquals( KCellType.LABEL, row[1].getType() );
    assertEquals( "Two", row[1].getValue() );
    assertEquals( KCellType.DATE, row[2].getType() );
    assertEquals( new Date( 1283904000000L ), row[2].getValue() );
    assertEquals( KCellType.NUMBER, row[3].getType() );
    assertEquals( Double.valueOf( "42" ), row[3].getValue() );
    assertEquals( KCellType.BOOLEAN_FORMULA, row[4].getType() ); // Always like that in ODS
    assertEquals( Boolean.valueOf( false ), row[4].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[5].getType() );
    assertEquals( Double.valueOf( "117" ), row[5].getValue() );

    row = sheet1.getRow( 4 );
    assertEquals( KCellType.LABEL, row[1].getType() );
    assertEquals( "Three", row[1].getValue() );
    assertEquals( KCellType.DATE, row[2].getType() );
    assertEquals( new Date( 1283990400000L ), row[2].getValue() );
    assertEquals( KCellType.NUMBER, row[3].getType() );
    assertEquals( Double.valueOf( "93" ), row[3].getValue() );
    assertEquals( KCellType.BOOLEAN_FORMULA, row[4].getType() ); // Always like that in ODS
    assertEquals( Boolean.valueOf( true ), row[4].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[5].getType() );
    assertEquals( Double.valueOf( "210" ), row[5].getValue() );

    workbook.close();
  }
}
