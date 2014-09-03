package org.pentaho.di.trans.steps.excelinput.poi;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KSheet;

public class PoiWorkbookTest {

  private final int SHEET_COUNT = 1;

  private final int SHEET_NUMBER = 0;

  private final String SHEET_NAME = "Sheet1";

  private final int ROW_COUNT = 5;

  private final int CELL_ROW_NUMBER = 2;

  private final int CELL_COL_NUMBER = 2;

  private final String CELL_CONTENT = "2010-09-07";

  private String filename;

  private long lastModifiedBeforeReading;

  @Before
  public void init() {
    filename = this.getClass().getResource( "sample-file.xlsx" ).getPath();
    File file = new File( filename );
    lastModifiedBeforeReading = file.lastModified();
  }

  @Test
  public void testCloseWithoutSaveAfterRead() throws KettleException {
    PoiWorkbook workbook = new PoiWorkbook( filename, null );
    assertNotNull( workbook );
    assertEquals( SHEET_COUNT, workbook.getNumberOfSheets() );

    KSheet sheet = workbook.getSheet( SHEET_NUMBER );
    assertNotNull( sheet );
    assertEquals( SHEET_NAME, sheet.getName() );
    assertEquals( ROW_COUNT, sheet.getRows() );

    String content = sheet.getCell( CELL_COL_NUMBER, CELL_ROW_NUMBER ).getContents();
    assertEquals( CELL_CONTENT, content );

    workbook.close();
    File file = new File( filename );
    assertEquals( lastModifiedBeforeReading, file.lastModified() );
  }
}
