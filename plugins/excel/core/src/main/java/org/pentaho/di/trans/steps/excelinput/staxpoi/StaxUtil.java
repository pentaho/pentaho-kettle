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

package org.pentaho.di.trans.steps.excelinput.staxpoi;

import org.apache.poi.ss.SpreadsheetVersion;

public class StaxUtil {
  private static final SpreadsheetVersion DEFAULT_SPREADSHEET_VERSION = SpreadsheetVersion.EXCEL2007;
  public static final int MAX_ROWS = DEFAULT_SPREADSHEET_VERSION.getMaxRows();
  public static final int MAX_COLUMNS = DEFAULT_SPREADSHEET_VERSION.getMaxColumns();

  public static int extractRowNumber( String position ) {
    int startIndex = 0;
    while ( startIndex < position.length() && !Character.isDigit( position.charAt( startIndex ) )  ) {
      startIndex++;
    }
    String rowPart = position.substring( startIndex );
    return Integer.parseInt( rowPart );
  }

  public static int extractColumnNumber( String position ) {
    int startIndex = 0;
    while ( startIndex < position.length() && !Character.isDigit( position.charAt( startIndex ) ) ) {
      startIndex++;
    }
    String colPart = position.substring( 0, startIndex );
    return parseColumnNumber( colPart );
  }

  /**
   * Convert the column indicator in Excel like A, B, C, AE, CX and so on to a 1-based column number.
   * @param columnIndicator The indicator to convert
   * @return The 1-based column number
   */
  public static final int parseColumnNumber( String columnIndicator ) {
    int col = 0;
    for ( int i = columnIndicator.length() - 1; i >= 0; i-- ) {
      char c = columnIndicator.charAt( i );
      int offset = 1 + Character.getNumericValue( c ) - Character.getNumericValue( 'A' );
      col += Math.pow( 26, columnIndicator.length() - i - 1 ) * offset;
    }

    return col;
  }

}
