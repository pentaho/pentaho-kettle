package org.pentaho.di.core.spreadsheet;

public interface KWorkbook {
  /**
   * Get a sheet with a certain name in a workbook.
   * 
   * @param sheetName The name of the sheet.
   * @return The sheet or null if the sheet was not found.
   */
  public KSheet getSheet(String sheetName);
  
  /**
   * @return The array of sheet names in the workbook
   */
  public String[] getSheetNames();
  
  /**
   * Close the workbook file
   */
  public void close();
  
  /**
   * @return The number of sheets in the workbook
   */
  public int getNumberOfSheets();
  
  /**
   * Get a sheet in the workbook by index
   * @param sheetNr The sheet number to get
   * @return The selected sheet
   */
  public KSheet getSheet(int sheetNr);
}
