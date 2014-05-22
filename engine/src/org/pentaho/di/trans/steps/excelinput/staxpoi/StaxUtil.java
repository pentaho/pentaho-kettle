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
