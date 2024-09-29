/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.spreadsheet;

public interface KWorkbook extends AutoCloseable {
  /**
   * Get a sheet with a certain name in a workbook.
   *
   * @param sheetName
   *          The name of the sheet.
   * @return The sheet or null if the sheet was not found.
   */
  public KSheet getSheet( String sheetName );

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
   *
   * @param sheetNr
   *          The sheet number to get
   * @return The selected sheet
   */
  public KSheet getSheet( int sheetNr );

  /**
   * Get a sheet name in the workbook by index
   *
   * @param sheetNr
   *          The sheet number to get
   * @return The selected sheet's name
   */
  public String getSheetName( int sheetNr );
}
