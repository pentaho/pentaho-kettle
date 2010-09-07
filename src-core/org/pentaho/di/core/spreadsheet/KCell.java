package org.pentaho.di.core.spreadsheet;

public interface KCell {
  public KCellType getType();
  
  /**
   * @return 
   * java.util.Date for KCellType.DATE<br>
   * Boolean for KCellType.BOOLEAN<br>
   * Double for KCellType.NUMBER<br>
   * String for KCellType.LABEL<br>
   * null for KCellType.EMPTY<br>
   */
  public Object getValue();
  
  /**
   * @return The content description of the cell
   */
  public String getContents();
  
  /**
   * @return The row number in the sheet.
   */
  public int getRow();
}
