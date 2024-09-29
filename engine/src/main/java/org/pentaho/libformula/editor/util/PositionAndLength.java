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


package org.pentaho.libformula.editor.util;

import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;

public class PositionAndLength {
  private int position;
  private int length;

  /**
   * @param position
   * @param length
   */
  public PositionAndLength( int position, int length ) {
    this.position = position;
    this.length = length;
  }

  /**
   * Determine the absolute position in the expression
   *
   * @param expression
   * @param position
   * @return Point : x=start, y=end
   */
  public static final PositionAndLength calculatePositionAndLength( String expression, ParsePosition position ) {
    int p = 0;
    PositionAndLength result = new PositionAndLength( 0, 0 );

    if ( position == null ) {
      return result;
    }

    // Position at the start of the correct line...
    int line = 1;
    while ( p < expression.length() && line < position.getStartLine() ) {
      if ( expression.substring( p ).startsWith( "\n\r" ) ) {
        line++;
        p += 2;
      } else if ( expression.substring( p ).startsWith( "\n" ) ) {
        line++;
        p++;
      } else if ( expression.substring( p ).startsWith( "\r" ) ) {
        line++;
        p++;
      } else {
        p++;
      }
    }

    // Position on the right column
    int col = 1;
    while ( p < expression.length() && col < position.getStartColumn() ) {
      p++;
      col++;
    }
    result.setPosition( p );

    // Position at the start of the correct line...
    while ( p < expression.length() && line < position.getEndLine() ) {
      if ( expression.substring( p ).startsWith( "\n\r" ) ) {
        line++;
        col = 1;
        p += 2;
      } else if ( expression.substring( p ).startsWith( "\n" ) ) {
        line++;
        p++;
        col = 1;
      } else if ( expression.substring( p ).startsWith( "\r" ) ) {
        line++;
        p++;
        col = 1;
      } else {
        p++;
      }
    }

    // Position on the right column
    while ( p < expression.length() && col < position.getEndColumn() ) {
      p++;
      col++;
    }
    int length = p - result.getPosition() + 1;
    result.setLength( length );

    // System.out.println(position.toString()+" --> position="+result.getPosition()+", length="+result.getLength());
    return result;
  }

  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

  /**
   * @param position
   *          the position to set
   */
  public void setPosition( int position ) {
    this.position = position;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length
   *          the length to set
   */
  public void setLength( int length ) {
    this.length = length;
  }

}
