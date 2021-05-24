/*******************************************************************************
 * Copyright (c) 2010, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptToken;
import com.yahoo.platform.yui.compressor.TestAdapter;
import com.yahoo.platform.yui.compressor.TestErrorReporter;


public class TestUtil {

  public static TokenList parse( String input ) throws IOException {
    List<JavaScriptToken> tokens = TestAdapter.parseString( input );
    return new TokenList( tokens );
  }

  public static String compress( String input ) throws IOException {
    Reader inputReader = new StringReader( input );
    TestErrorReporter errorReporter = new TestErrorReporter();
    JavaScriptCompressor compressor = new JavaScriptCompressor( inputReader, errorReporter );
    StringWriter outputWriter = new StringWriter();
    compressor.compress( outputWriter, -1, true, false, false, false );
    return outputWriter.toString();
  }

  public static void printTokens( TokenList tokens ) {
    int size = tokens.size();
    for( int i = 0; i < size; i++ ) {
      printToken( i, tokens.getToken( i ) );
    }
  }

  private static void printToken( int n, JavaScriptToken token ) {
    int type = token.getType();
    switch( type ) {
      case Token.NAME:
        System.out.println( n + ". name: " + token.getValue() );
      break;
      case Token.REGEXP:
        System.out.println( n + ". regexp: " + token.getValue() );
      break;
      case Token.STRING:
        System.out.println( n + ". string: " + token.getValue() );
      break;
      case Token.NUMBER:
        System.out.println( n + ". number: " + token.getValue() );
      break;
      default:
        String litStr = TestAdapter.getLiteralString( type );
        System.out.println( n + ". literal: " + litStr );
      break;
    }
  }
}
