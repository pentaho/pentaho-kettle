package org.pentaho.di.core.spreadsheet;


/**
 * The interface that is needed by ExcelInput to handle a single numeric data sheet in a workbook.
 * 
 * @author matt
 */
public interface KSheet {
  /**
   * Get one row of cells in the sheets
   * @param rownr The row number to get
   * @return the cells of the row
   * @throws ArrayIndexOutOfBoundsException in case you try to read beyond the last row. (never returns null as such)
   */
  public KCell[] getRow(int rownr);
  
  /**
   * @return The name of the cell
   */
  public String getName();
  
  /**
   * @return The number of rows in the sheet
   */
  public int getRows();

  /**
   * Get a cell in the sheet by specification of its coordinates.
   * @param colnr The column number
   * @param rownr The row number
   * @return The cell at the specified coordinates.
   */
  public KCell getCell(int colnr, int rownr);
}
