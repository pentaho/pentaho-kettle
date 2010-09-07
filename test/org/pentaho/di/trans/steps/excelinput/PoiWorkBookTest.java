package org.pentaho.di.trans.steps.excelinput;

import java.util.Date;

import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.excelinput.WorkbookFactory;

import junit.framework.TestCase;

public class PoiWorkBookTest extends TestCase {
  public void testRead() throws Exception {
    KWorkbook workbook = WorkbookFactory.getWorkbook(SpreadSheetType.POI, "testfiles/sample-file.xlsx", null);
    int numberOfSheets = workbook.getNumberOfSheets();
    assertEquals(3, numberOfSheets);
    KSheet sheet1 = workbook.getSheet(0);
    assertEquals("Sheet1", sheet1.getName());
    sheet1 = workbook.getSheet("Sheet1");
    assertEquals("Sheet1", sheet1.getName());

    assertEquals(5, sheet1.getRows());
    
    KCell[] row = sheet1.getRow(2);
    assertEquals(KCellType.LABEL, row[1].getType());
    assertEquals("One", row[1].getValue());
    assertEquals(KCellType.DATE, row[2].getType());
    assertEquals(new Date(1283817600000L), row[2].getValue());
    assertEquals(KCellType.NUMBER, row[3].getType());
    assertEquals(Double.valueOf("75"), row[3].getValue());
    assertEquals(KCellType.BOOLEAN, row[4].getType());
    assertEquals(Boolean.valueOf(true), row[4].getValue());
    assertEquals(KCellType.NUMBER_FORMULA, row[5].getType());
    assertEquals(Double.valueOf("75"), row[5].getValue());
    
    row = sheet1.getRow(3);
    assertEquals(KCellType.LABEL, row[1].getType());
    assertEquals("Two", row[1].getValue());
    assertEquals(KCellType.DATE, row[2].getType());
    assertEquals(new Date(1283904000000L), row[2].getValue());
    assertEquals(KCellType.NUMBER, row[3].getType());
    assertEquals(Double.valueOf("42"), row[3].getValue());
    assertEquals(KCellType.BOOLEAN, row[4].getType());
    assertEquals(Boolean.valueOf(false), row[4].getValue());
    assertEquals(KCellType.NUMBER_FORMULA, row[5].getType());
    assertEquals(Double.valueOf("117"), row[5].getValue());

    row = sheet1.getRow(4);
    assertEquals(KCellType.LABEL, row[1].getType());
    assertEquals("Three", row[1].getValue());
    assertEquals(KCellType.DATE, row[2].getType());
    assertEquals(new Date(1283990400000L), row[2].getValue());
    assertEquals(KCellType.NUMBER, row[3].getType());
    assertEquals(Double.valueOf("93"), row[3].getValue());
    assertEquals(KCellType.BOOLEAN, row[4].getType());
    assertEquals(Boolean.valueOf(true), row[4].getValue());
    assertEquals(KCellType.NUMBER_FORMULA, row[5].getType());
    assertEquals(Double.valueOf("210"), row[5].getValue());

    try {
      sheet1.getRow(5);
      throw new Exception("No out of bounds exception thrown when expected");
    } catch(ArrayIndexOutOfBoundsException e) {
      // OK!
    }
    
    workbook.close();
  }
}
