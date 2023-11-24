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

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StaxWorkBookIT {

  private static final String SAMPLE_FILE = "src/it/resources/sample-file.xlsx";
  private static final String SAMPLE_FILE_PROTECTED = "src/it/resources/sample-file-protected.xlsx";
  private static final String SAMPLE_FILE_PROTECTED_PASSWORD = "password";

  @Test
  public void testReadData() throws KettleException {
    readData( SAMPLE_FILE, null );
  }

  // TODO Get a legit test file
  // Acording to the API this should work; however, I think there is a conversion issue when I save this
  // worksheeting with a password in Excel.  We need to get a legit file that is password protected from PM.
  @Ignore
  @Test
  public void testReadDataProtected() throws KettleException {
    readData( SAMPLE_FILE_PROTECTED, SAMPLE_FILE_PROTECTED_PASSWORD );
  }


  @Test
  public void testFileDoesNotChange() throws KettleException, IOException {
    File fileBeforeRead = new File( SAMPLE_FILE );
    readData( SAMPLE_FILE );
    File fileAfterRead = new File( SAMPLE_FILE );
    assertTrue( FileUtils.contentEquals(fileBeforeRead, fileAfterRead ) );
  }

  @Test
  public void testRead() throws Exception {
    FileLock lock = null;
    RandomAccessFile randomAccessFile = null;
    try {
      readData( SAMPLE_FILE );
      File fileAfterRead = new File( SAMPLE_FILE );
      randomAccessFile = new RandomAccessFile( fileAfterRead, "rw" );
      FileChannel fileChannel = randomAccessFile.getChannel();
      lock = fileChannel.tryLock();
      // check that we could lock file
      assertTrue( lock.isValid() );
    } finally {
      if ( lock != null ) {
        lock.release();
      }
      if ( randomAccessFile != null ) {
        randomAccessFile.close();
      }
    }
  }

  @Test
  public void testEmptySheet() throws Exception {
    KWorkbook workbook = getWorkbook( SAMPLE_FILE, null );
    int numberOfSheets = workbook.getNumberOfSheets();
    assertEquals( 3, numberOfSheets );
    // last two sheets are empty, check if no exception opening
    for ( int i = 1; i< numberOfSheets; i++ ) {
      KSheet sheet = workbook.getSheet( i );
      for ( int j = 0; j < sheet.getRows(); j++ ) {
        sheet.getRow( j );
      }
    }
  }

  @Test
  public void testReadSameRow() throws Exception {
    KWorkbook workbook = getWorkbook( SAMPLE_FILE, null );
    KSheet sheet1 = workbook.getSheet( 0 );
    KCell[] row = sheet1.getRow( 3 );
    assertEquals( "Two", row[1].getValue() );
    row = sheet1.getRow( 3 );
    assertEquals( "Two", row[1].getValue() );
  }

  @Test
  public void testReadRowRA() throws Exception {
    KWorkbook workbook = getWorkbook( SAMPLE_FILE, null );
    KSheet sheet1 = workbook.getSheet( 0 );
    KCell[] row = sheet1.getRow( 4 );
    assertEquals( "Three", row[1].getValue() );
    row = sheet1.getRow( 2 );
    assertEquals( "One", row[1].getValue() );
  }


  @Test
  public void testReadEmptyRow() throws Exception {
    KWorkbook workbook = getWorkbook( SAMPLE_FILE, null );
    KSheet sheet1 = workbook.getSheet( 0 );
    KCell[] row = sheet1.getRow( 0 );
    assertEquals( 0, row.length );
  }

  @Test
  public void testReadCells() throws Exception {
    KWorkbook workbook = getWorkbook( SAMPLE_FILE, null );
    KSheet sheet = workbook.getSheet( 0 );

    KCell cell = sheet.getCell( 1, 2 );
    assertEquals( "One", cell.getValue() );
    assertEquals( KCellType.LABEL, cell.getType() );

    cell = sheet.getCell( 2, 2 );
    assertEquals( KCellType.DATE, cell.getType() );
    assertEquals( new Date( 1283817600000L ), cell.getValue() );

    cell = sheet.getCell( 1, 3 );
    assertEquals( "Two", cell.getValue() );
    assertEquals( KCellType.LABEL, cell.getType() );
  }

  protected KWorkbook getWorkbook( String file, String encoding ) throws KettleException {
    return getWorkbook( file, encoding, null );
  }

  protected KWorkbook getWorkbook( String file, String encoding, String password ) throws KettleException {
    return WorkbookFactory.getWorkbook( SpreadSheetType.SAX_POI, file, encoding, password );
  }

  private void readData( String file ) throws KettleException {
    readData( file, null );
  }

  private void readData( String file, String password ) throws KettleException {
    KWorkbook workbook = getWorkbook( file, null, password );
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
    assertEquals( KCellType.BOOLEAN, row[4].getType() );
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
    assertEquals( KCellType.BOOLEAN, row[4].getType() );
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
    assertEquals( KCellType.BOOLEAN, row[4].getType() );
    assertEquals( Boolean.valueOf( true ), row[4].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[5].getType() );
    assertEquals( Double.valueOf( "210" ), row[5].getValue() );

    try {
      row = sheet1.getRow( 5 );
      fail( "No out of bounds exception thrown when expected" );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      // OK!
    }
  }
}
