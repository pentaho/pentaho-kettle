/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.internal.util.HTTP;


public final class StartupPageTemplate {

  public final static String TOKEN_BACKGROUND_IMAGE = "backgroundImage";
  public final static String TOKEN_LIBRARIES = "libraries";
  public final static String TOKEN_APP_SCRIPT = "appScript";
  public final static String TOKEN_BODY = "body";
  public final static String TOKEN_TITLE = "title";
  public final static String TOKEN_HEADERS = "headers";
  public final static String TOKEN_NO_SCRIPT_MESSAGE = "noScriptMessage";

  public interface VariableWriter {
    void writeVariable( PrintWriter printWriter, String variableName );
  }

  private final Token[] tokens;

  public StartupPageTemplate() {
    this( loadStatupPageTemplate() );
  }

  public StartupPageTemplate( String template ) {
    tokens = new TemplateParser( template ).parse();
  }

  public void writePage( PrintWriter printWriter, VariableWriter variableValueProvider ) {
    for( Token token : tokens ) {
      if( token.isVariable() ) {
        variableValueProvider.writeVariable( printWriter, token.toString() );
      } else {
        printWriter.print( token.toString() );
      }
    }
    printWriter.flush();
  }

  private static String loadStatupPageTemplate() {
    StringBuilder buffer = new StringBuilder();
    try {
      InputStream stream = StartupPageTemplate.class.getResourceAsStream( "rwt-index.html" );
      InputStreamReader streamReader = new InputStreamReader( stream, HTTP.CHARSET_UTF_8 );
      BufferedReader reader = new BufferedReader( streamReader );
      try {
        String line = reader.readLine();
        while( line != null ) {
          buffer.append( line );
          buffer.append( '\n' );
          line = reader.readLine();
        }
      } finally {
        reader.close();
      }
    } catch( IOException ioe ) {
      throw new RuntimeException( "Failed to read startup page template", ioe );
    }
    return buffer.toString();
  }

  static class Token {
    private final String string;
    private final boolean variable;

    Token( String string, boolean variable ) {
      this.string = string;
      this.variable = variable;
    }

    boolean isVariable() {
      return variable;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  static class TemplateParser {

    private final String template;
    private final List<Token> tokens;
    private int index;
    private boolean withinBrackets;
    private StringBuilder currentToken;

    TemplateParser( String template ) {
      this.template = template;
      this.tokens = new LinkedList<>();
      this.currentToken = new StringBuilder();
    }

    Token[] parse() {
      while( index < template.length() ) {
        if( !withinBrackets && currentCharEquals( '$' ) && nextCharEquals( '{' ) ) {
          pushCurrentToken();
          withinBrackets = true;
          index++;
        } else if( withinBrackets && currentCharEquals( '}' ) ){
          pushCurrentToken();
          withinBrackets = false;
        } else {
          currentToken.append( currentChar() );
        }
        index++;
      }
      pushCurrentToken();
      return tokens.toArray( new Token[ tokens.size() ] );
    }

    private char currentChar() {
      return template.charAt( index );
    }

    private boolean currentCharEquals( char character ) {
      return currentChar() == character;
    }

    private boolean nextCharEquals( char character ) {
      return index + 1 < template.length() && template.charAt( index + 1 ) == character;
    }

    private void pushCurrentToken() {
      if( currentToken.length() > 0 ) {
        tokens.add( new Token( currentToken.toString(), withinBrackets ) );
        currentToken = new StringBuilder();
      }
    }

  }

}
