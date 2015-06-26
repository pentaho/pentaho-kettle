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
package org.pentaho.di.trans.steps.excelinput;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;

public class StaxWorkBookTest {

  @Test
  public void testReadData() throws KettleException {
    readData();
  }

  @Test
  public void testFileDoesNotChange() throws KettleException, IOException {
    File fileBeforeRead = new File( "testfiles/sample-file.xlsx" );
    readData();
    File fileAfterRead = new File( "testfiles/sample-file.xlsx" );
    assertTrue( FileUtils.contentEquals(fileBeforeRead, fileAfterRead ) );
  }

  @Test
  public void testRead() throws Exception {
    FileLock lock = null;
    RandomAccessFile randomAccessFile = null;
    try {
      File fileAfterRead = new File( "testfiles/sample-file.xlsx" );
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

  private void readData() throws KettleException {
    KWorkbook workbook = WorkbookFactory.getWorkbook( SpreadSheetType.SAX_POI, "testfiles/sample-file.xlsx", null );
    int numberOfSheets = workbook.getNumberOfSheets();
    assertEquals( 3, numberOfSheets );
    KSheet sheet1 = workbook.getSheet( 0 );
    assertEquals( "Sheet1", sheet1.getName() );
    sheet1 = workbook.getSheet( "Sheet1" );
    assertEquals( "Sheet1", sheet1.getName() );

    assertEquals( 5, sheet1.getRows() );

    KCell[] row = sheet1.getRow( 2 );
    assertEquals( KCellType.STRING_FORMULA, row[1].getType() );
    assertEquals( "One", row[1].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[2].getType() );
    assertEquals( "40428", row[2].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[3].getType() );
    assertEquals( "75", row[3].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[4].getType() );
    assertEquals( "1", row[4].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[5].getType() );
    assertEquals( "75", row[5].getValue() );

    row = sheet1.getRow( 3 );
    assertEquals( KCellType.STRING_FORMULA, row[1].getType() );
    assertEquals( "Two", row[1].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[2].getType() );
    assertEquals( "40429", row[2].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[3].getType() );
    assertEquals( "42", row[3].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[4].getType() );
    assertEquals( "0", row[4].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[5].getType() );
    assertEquals( "117", row[5].getValue() );

    row = sheet1.getRow( 4 );
    assertEquals( KCellType.STRING_FORMULA, row[1].getType() );
    assertEquals( "Three", row[1].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[2].getType() );
    assertEquals( "40430", row[2].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[3].getType() );
    assertEquals( "93", row[3].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[4].getType() );
    assertEquals( "1", row[4].getValue() );
    assertEquals( KCellType.STRING_FORMULA, row[5].getType() );
    assertEquals( "210", row[5].getValue() );
    workbook.close();
  }
}
