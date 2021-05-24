/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptToken;


public final class StringReplacer {

  private final HashMap<String, Integer> stringMap = new HashMap<>();
  private List<String> strings;

  public void discoverStrings( TokenList tokens ) {
    if( strings != null ) {
      throw new IllegalStateException( "Can not add strings after computing indexes" );
    }
    int length = tokens.size();
    for( int pos = 0; pos < length; pos++ ) {
      if( isReplacableString( tokens, pos ) ) {
        String value = tokens.getToken( pos ).getValue();
        Integer count = stringMap.get( value );
        if( count == null ) {
          stringMap.put( value, new Integer( 1 ) );
        } else {
          stringMap.put( value, new Integer( count.intValue() + 1 ) );
        }
      }
    }
  }

  public void replaceStrings( TokenList tokens ) {
    ensureStringListCreated();
    int length = tokens.size();
    for( int pos = length - 1; pos >= 0; pos-- ) {
      if( isReplacableString( tokens, pos ) ) {
        String string = tokens.getToken( pos ).getValue();
        int index = getIndex( string );
        if( index != -1 ) {
          JavaScriptToken[] replacement = createTokensForArrayAccess( "_", index );
          tokens.replaceToken( pos, replacement );
        }
      }
    }
  }

  public void optimize() {
    ensureStringListCreated();
    for( int i = strings.size() - 1; i >= 0; i-- ) {
      String string = strings.get( i );
      if( !isWorthReplacing( string ) ) {
        strings.remove( i );
      }
    }
  }

  public String[] getStrings() {
    ensureStringListCreated();
    String[] result = new String[ strings.size() ];
    strings.toArray( result );
    return result;
  }

  private boolean isWorthReplacing( String string ) {
    int freq = getFrequency( string );
    return freq > 1 && ( string.length() + 2 ) * ( freq - 1 ) > freq * 6;
  }

  private int getFrequency( String string ) {
    Integer frequency = stringMap.get( string );
    return frequency == null ? 0 : frequency.intValue();
  }

  private int getIndex( String string ) {
    return strings.indexOf( string );
  }

  private void ensureStringListCreated() {
    if( strings == null ) {
      strings = new ArrayList<>( stringMap.keySet() );
      Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare( String string1, String string2 ) {
          int freq1 = getFrequency( string1 );
          int freq2 = getFrequency( string2 );
          return freq1 < freq2 ? 1 : ( freq1 == freq2 ? 0 : -1 );
        }
      };
      Collections.sort( strings, comparator );
    }
  }

  private static JavaScriptToken[] createTokensForArrayAccess( String arrayName,
                                                               int index )
  {
    JavaScriptToken[] replacement = new JavaScriptToken[] {
      new JavaScriptToken( Token.NAME, arrayName ),
      new JavaScriptToken( Token.LB, "[" ),
      new JavaScriptToken( Token.NUMBER, String.valueOf( index ) ),
      new JavaScriptToken( Token.RB, "]" )
    };
    return replacement;
  }

  private static boolean isReplacableString( TokenList tokens, int pos ) {
    boolean result = false;
    JavaScriptToken token = tokens.getToken( pos );
    if( isString( token ) ) {
      JavaScriptToken nextToken = tokens.getToken( pos + 1 );
      if( !isColonInObjectLiteral( nextToken ) ) {
        result = true;
      }
    }
    return result;
  }

  static boolean isString( JavaScriptToken token ) {
    return token != null && token.getType() == Token.STRING;
  }

  static boolean isColonInObjectLiteral( JavaScriptToken token ) {
    return token != null && token.getType() == Token.OBJECTLIT;
  }

}
