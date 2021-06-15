/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;


public class MnemonicUtil {

  private MnemonicUtil() {
    // prevent instantiation
  }

  public static int findMnemonicCharacterIndex( String text ) {
    int result = -1;
    int counter = -1;
    boolean insertAmp = false;
    int textLength = text.length();
    for( int i = 0; i < textLength; i++ ) {
      char ch = text.charAt( i );
      if( ch == '&' ) {
        if( insertAmp ) {
          insertAmp = false;
          counter++;
        } else if( i + 1 < textLength ) {
          if( text.charAt( i + 1 ) == '&' ) {
            insertAmp = true;
          } else {
            result = counter + 1;
          }
        }
      } else {
        counter++;
      }
    }
    return result;
  }

  public static String removeAmpersandControlCharacters( String text ) {
    boolean insertAmp = false;
    StringBuffer buffer = new StringBuffer();
    int textLength = text.length();
    for( int i = 0; i < textLength; i++ ) {
      char ch = text.charAt( i );
      if( ch == '&' ) {
        if( insertAmp ) {
          insertAmp = false;
          buffer.append( '&' );
        } else if( i + 1 < textLength && text.charAt( i + 1 ) == '&' ) {
          insertAmp = true;
        }
      } else {
        buffer.append( ch );
      }
    }
    return buffer.toString();
  }

}
