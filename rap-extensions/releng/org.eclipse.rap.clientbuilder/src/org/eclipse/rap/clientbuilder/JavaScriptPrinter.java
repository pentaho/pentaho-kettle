/*******************************************************************************
 * Copyright (c) 2010, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptToken;


/**
 * Can be used to turn a list of JavaScriptTokens into formatted JavaScript
 * code. The generated code might not be perfect, it is used mostly for
 * debugging.
 */
public class JavaScriptPrinter {

  private static final String INDENT = "  ";
  private static final String NEWLINE = "\n";

  private final StringBuilder code;

  private String indent;
  private String nextPrefix;

  public JavaScriptPrinter() {
    code = new StringBuilder();
    indent = "";
    nextPrefix = "";
  }

  public void appendToken( JavaScriptToken token ) {
    int type = token.getType();
    if( type == Token.RC ) {
      code.append( NEWLINE );
      if( indent.length() >= INDENT.length() ) {
        indent = indent.substring( INDENT.length() );
        code.append( indent );
      }
      code.append( token.getValue() );
      nextPrefix = NEWLINE + indent;
    } else if( type == Token.LC ) {
      code.append( nextPrefix );
      code.append( token.getValue() );
      indent += INDENT;
      nextPrefix = NEWLINE + indent;
    } else if( type == Token.COMMA ) {
      code.append( token.getValue() );
    } else if( type == Token.SEMI ) {
      code.append( token.getValue() );
      nextPrefix = NEWLINE + indent;
    } else if( type == Token.DOT ) {
      code.append( token.getValue() );
      nextPrefix = "";
    } else if( type == Token.STRING ) {
      code.append( nextPrefix + "\"" + escapeString( token.getValue() ) + "\"" );
      nextPrefix = " ";
    } else {
      code.append( nextPrefix + token.getValue() );
      nextPrefix = " ";
    }
  }

  @Override
  public String toString() {
    return code.toString();
  }

  /**
   * Turns a list of JavaScriptTokens into JavaScript code.
   */
  public static String printTokens( TokenList tokens ) {
    return printTokens( tokens, 0, tokens.size() - 1 );
  }

  /**
   * Turns a range of a list of JavaScriptTokens into JavaScript code.
   */
  public static String printTokens( TokenList tokens, int first, int last ) {
    JavaScriptPrinter printer = new JavaScriptPrinter();
    for( int i = first; i <= last; i++ ) {
      printer.appendToken( tokens.getToken( i ) );
    }
    return printer.toString();
  }

  public static String escapeString( String value ) {
    StringBuilder result = new StringBuilder();
    int length = value.length();
    for( int i = 0; i < length; i++ ) {
      char ch = value.charAt( i );
      if( ch == '"' ) {
        result.append( "\\\"" );
      } else if( ch == '\n' ) {
        result.append( "\\n" );
      } else if( ch == '\r' ) {
        result.append( "\\r" );
      } else if( ch == '\t' ) {
        result.append( "\\t" );
      } else if( ch == '\\' ) {
        result.append( "\\\\" );
      } else {
        result.append( ch );
      }
    }
    return result.toString();
  }

}
