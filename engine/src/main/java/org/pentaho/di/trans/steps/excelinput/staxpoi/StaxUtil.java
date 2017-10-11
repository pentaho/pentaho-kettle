/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput.staxpoi;

public class StaxUtil {

  public static int extractRowNumber( String position ) {
    int startIndex = 0;
    while ( !Character.isDigit( position.charAt( startIndex ) ) && startIndex < position.length() ) {
      startIndex++;
    }
    String rowPart = position.substring( startIndex );
    return Integer.parseInt( rowPart );
  }

  public static int extractColumnNumber( String position ) {
    int startIndex = 0;
    while ( !Character.isDigit( position.charAt( startIndex ) ) && startIndex < position.length() ) {
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
