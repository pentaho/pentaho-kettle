package org.pentaho.di.trans.steps.excelinput.ods;

import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KSheet;

public class OdfSheet implements KSheet {
  private OdfTable table;
  private int nrOfRows;
  
  public OdfSheet(OdfTable table) {
    this.table = table;
    
    int size = table.getOdfElement().getChildNodes().getLength();
    
    int rowNr = 0;
    int maxIndex=0;
    OdfTableRow row = table.getRowByIndex(rowNr);
    row = row.getNextRow();
    rowNr++;
    while (rowNr<size) {
      int cols = findNrColumns(row);
      if (cols>0) maxIndex=rowNr;
      row = row.getNextRow();
      rowNr++;
    }
    nrOfRows = maxIndex+1;
  }
  
  private int findNrColumns(OdfTableRow row) {
    return row.getOdfElement().getChildNodes().getLength()-1;
  }
  
  public String getName() {
    return table.getTableName();
  }
  
  public KCell[] getRow(int rownr) {
    if (rownr>=nrOfRows) {
      throw new ArrayIndexOutOfBoundsException("Read beyond last row: "+rownr);
    }
    OdfTableRow row = table.getRowByIndex(rownr);
    int cols = findNrColumns(row);
    OdfCell[] xlsCells = new OdfCell[cols];
    for (int i=0;i<cols;i++) {
      OdfTableCell cell = row.getCellByIndex(i);
      if (cell!=null) {
        xlsCells[i] = new OdfCell(cell);
      }
    }
    return xlsCells;
  }
  
  public int getRows() {
    return nrOfRows;
  }
  
  public KCell getCell(int colnr, int rownr) {
    OdfTableCell cell = table.getCellByPosition(colnr, rownr);
    if (cell==null) return null;
    return new OdfCell(cell);
  }
}
