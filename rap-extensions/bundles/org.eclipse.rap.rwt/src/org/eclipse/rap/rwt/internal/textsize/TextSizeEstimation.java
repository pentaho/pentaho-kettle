/*******************************************************************************
 * Copyright (c) 2006, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import static org.eclipse.rap.rwt.internal.util.EncodingUtil.splitNewLines;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.graphics.FontUtil;


public final class TextSizeEstimation {

  /**
   * Estimates the size of a given text. Line breaks are not respected.
   * @param font the font to perform the estimation for
   * @param string the text whose size to estimate
   * @return the estimated size
   */
  static Point stringExtent( Font font, String string ) {
    int width = getLineWidth( string, font );
    int height = getCharHeight( font ) + 2;
    return new Point( width, height );
  }

  /**
   * Estimates the size of a given text, respecting line breaks and wrapping at
   * a given width.
   * @param font the font to perform the estimation for
   * @param string the text whose size to estimate
   * @param wrapWidth the width to wrap at in pixels, 0 or negative stands for no wrapping
   *
   * @return the estimated size
   */
  static Point textExtent( Font font, String string, int wrapWidth ) {
    int lineCount = 0;
    int maxWidth = 0;
    for( String line : splitNewLines( string ) ) {
      lineCount++;
      int width = getLineWidth( line, font );
      if( wrapWidth > 0 ) {
        boolean done = false;
        while( !done ) {
          int index = getLongestMatch( line, wrapWidth, font );
          if( index == 0 || index == line.length() ) {
            // line fits or cannot be wrapped
            done = true;
          } else {
            // wrap line
            String substr = line.substring( 0, index );
            width = getLineWidth( substr, font );
            maxWidth = Math.max( maxWidth, width );
            line = line.substring( index, line.length() );
            lineCount++;
          }
        }
      }
      maxWidth = Math.max( maxWidth, width );
    }
    int height = Math.round( getCharHeight( font ) * 1.25f * lineCount );
    return new Point( maxWidth, height );
  }

  /**
   * Estimates the size of a given markup text, respecting wrapping at a given width.
   * Line break tag is replaced with a new line character. All other tags are removed.
   * @param font the font to perform the estimation for
   * @param markup the markup text whose size to estimate
   * @param wrapWidth the width to wrap at in pixels, 0 or negative stands for no wrapping
   *
   * @return the estimated size
   */
  static Point markupExtent( Font font, String markup, int wrapWidth ) {
    return textExtent( font, removeAllTags( markup ), wrapWidth );
  }

  /**
   * Returns the character height in pixels. The returned value is only a rough
   * estimation.
   *
   * @param font the font to perform the estimation for
   * @return the estimated character height in pixels
   */
  static int getCharHeight( Font font ) {
    // at 72 dpi, 1 pt == 1 px
    return FontUtil.getData( font ).getHeight();
  }

  /**
   * Returns the average character weight in pixels. The returned value is only
   * a rough estimation that does not take the font family into account.
   *
   * @param font the font to perform the estimation for
   * @return the estimated average character width in pixels
   */
  static float getAvgCharWidth( Font font ) {
    float result;
    FontData fontData = FontUtil.getData( font );
    ProbeResultStore probeStore = ProbeResultStore.getInstance();
    if( probeStore.containsProbeResult( fontData ) ) {
      // we can improve char width estimations in case that we already have the
      // specified font probed.
      result = probeStore.getProbeResult( fontData ).getAvgCharWidth();
    } else {
      result = fontData.getHeight() * 0.48f;
      if( ( fontData.getStyle() & SWT.BOLD ) != 0 ) {
        result *= 1.45;
      }
    }
    return result;
  }

  static String removeAllTags( String markup ) {
    StringBuilder result = new StringBuilder();
    StringBuilder tag = new StringBuilder();
    boolean inTag = false;
    int length = markup.length();
    for( int i = 0; i < length; i++ ) {
      char ch = markup.charAt( i );
      if( ch == '<' ) {
        inTag = true;
      } else if( ch == '>' ) {
        if( tag.toString().equalsIgnoreCase( "br" ) ) {
          result.append( "\n" );
        }
        tag.setLength( 0 );
        inTag = false;
      } else if( inTag ) {
        if( Character.isLetterOrDigit( ch ) ) {
          tag.append( ch );
        }
      } else {
        result.append( ch );
      }
    }
    return result.toString();
  }

  /**
   * Returns the length of the longest substring, whose width is smaller or
   * equal to wrapWidth. If there is no such substring, zero is returned. The
   * result is never negative.
   */
  private static int getLongestMatch( String string, int wrapWidth, Font font ) {
    int result = 0;
    if( getLineWidth( string, font ) < wrapWidth ) {
      result = string.length();
    } else {
      String subStr = nextSubLine( string, 0 );
      boolean done = false;
      while( !done && getLineWidth( subStr, font ) <= wrapWidth ) {
        result = subStr.length();
        // loop prevention (see bug 182754)
        if( subStr.length() == string.length() ) {
          done = true;
        } else {
          subStr = nextSubLine( string, subStr.length() + 1 );
        }
      }
    }
    return result;
  }

  /**
   * Returns the next substring that can be wrapped.
   */
  private static String nextSubLine( String line, int startIndex ) {
    String result = line;
    int index = line.indexOf( ' ', startIndex );
    if( index != -1 ) {
      result = line.substring( 0, index );
    }
    return result;
  }

  /**
   * Returns the width of a given string in pixels. Line breaks are ignored.
   */
  private static int getLineWidth( String line, Font font ) {
    return Math.round( getAvgCharWidth( font ) * line.length() );
  }

}
