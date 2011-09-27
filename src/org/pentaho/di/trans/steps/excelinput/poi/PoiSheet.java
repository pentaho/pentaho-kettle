package org.pentaho.di.trans.steps.excelinput.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KSheet;

public class PoiSheet implements KSheet {
  private Sheet sheet;
  
  public PoiSheet(Sheet sheet) {
    this.sheet = sheet;
  }
  
  public String getName() {
    return sheet.getSheetName();
  }
  
  public KCell[] getRow(int rownr) {
    if (rownr < sheet.getFirstRowNum()) {
      return new KCell[] {};
    } else if (rownr > sheet.getLastRowNum()) {
      throw new ArrayIndexOutOfBoundsException("Read beyond last row: "+rownr);
    }
    Row row = sheet.getRow(rownr);
    if (row == null) { // read an empty row
      return new KCell[] {};
    }
    int cols = row.getLastCellNum();
    if (cols < 0){ // this happens if a row has no cells, POI returns -1 then
        return new KCell[] {};
    }
    PoiCell[] xlsCells = new PoiCell[cols];
    for (int i=0;i<cols;i++) {
      Cell cell = row.getCell(i);
      if (cell!=null) {
        xlsCells[i] = new PoiCell(cell);
      }
    }
    return xlsCells;
  }
  
  public int getRows() {
    return sheet.getLastRowNum()+1;
  }
  
  public KCell getCell(int colnr, int rownr) {
    Row row = sheet.getRow(rownr);
    if (row==null) return null;
    Cell cell = row.getCell(colnr);
    if (cell==null) return null;
    return new PoiCell(cell);
  }
}
