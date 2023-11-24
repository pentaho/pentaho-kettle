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
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PoiWorkBookIT {

  private String sampleFile = "src/it/resources/sample-file.xlsx";
  private String sampleFileProtected = "src/it/resources/sample-file-protected.xlsx";
  private String password = "password";

  @BeforeClass
  public static void beforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testReadData() throws KettleException {
    readData( sampleFile, null );
  }

  @Test
  public void testReadDataEmptyPassword() throws KettleException {
    readData( sampleFile, "" );
  }

  @Test
  public void testReadDataProtected() throws KettleException {
    readData( sampleFileProtected, password );
  }

  @Test
  public void testReadDataProtectedEmptyPassword() throws KettleException {
    KettleException actualException = null;
    try {
      readData( sampleFileProtected, "asdf" );
    } catch ( KettleException e ) {
      actualException = e;
    }
    assertNotNull( actualException );
    assertNotNull( actualException.getMessage() );
    assertEquals( "Password incorrect", actualException.getMessage().trim() );
  }

  @Test
  public void testFileDoesNotChange() throws KettleException, IOException {
    File fileBeforeRead = new File( sampleFile );
    readData( sampleFile, null );
    File fileAfterRead = new File( sampleFile );
    assertTrue( FileUtils.contentEquals( fileBeforeRead, fileAfterRead ) );
  }

  @Test
  public void testResourceFree() throws Exception {
    FileLock lock = null;
    RandomAccessFile randomAccessFile = null;
    try {
      readData( sampleFile, null );
      File fileAfterRead = new File( sampleFile );
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

  private void readData( String file, String password ) throws KettleException {
    KWorkbook workbook = WorkbookFactory.getWorkbook( SpreadSheetType.POI, file, null, password );
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
      sheet1.getRow( 5 );
      fail( "No out of bounds exception thrown when expected" );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      // OK!
    }
    workbook.close();
  }
}
