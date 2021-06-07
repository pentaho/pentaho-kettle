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

import java.util.List;

import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptToken;


public class TokenList {

  private final List<JavaScriptToken> tokens;

  public TokenList( List<JavaScriptToken> tokens ) {
    this.tokens = tokens;
  }

  public int size() {
    return tokens.size();
  }

  public JavaScriptToken getToken( int index ) {
    JavaScriptToken token = null;
    if( index >= 0 && index < tokens.size() ) {
      token = tokens.get( index );
    }
    return token;
  }

  public void removeToken( int index ) {
    replaceToken( index, null );
  }

  public void replaceToken( int index, JavaScriptToken[] replacement ) {
    replaceTokens( index, index, replacement );
  }

  public void replaceTokens( int begin, int end, JavaScriptToken[] replacement ) {
    if( begin >= 0 && begin <= end && end < tokens.size() ) {
      for( int i = begin; i <= end; i++ ) {
        tokens.remove( begin );
      }
      if( replacement != null ) {
        for( int i = 0; i < replacement.length; i++ ) {
          tokens.add( begin + i, replacement[ i ] );
        }
      }
    }
  }

  public int readExpression( int offset ) {
    int result = -1;
    JavaScriptToken token = getToken( offset );
    if( TokenMatcher.LEFT_BRACE.matches( token ) || TokenMatcher.LEFT_BRACKET.matches( token ) ) {
      result = findClosing( offset );
    }
    int pos = offset;
    while( pos >= 0 && pos < size() && result == -1 ) {
      token = getToken( pos );
      if(    TokenMatcher.LEFT_BRACE.matches( token )
          || TokenMatcher.LEFT_BRACKET.matches( token )
          || TokenMatcher.LEFT_PAREN.matches( token ) )
      {
        pos = findClosing( pos ) + 1;
      } else if(    TokenMatcher.RIGHT_BRACE.matches( token )
                 || TokenMatcher.RIGHT_BRACKET.matches( token )
                 || TokenMatcher.RIGHT_PAREN.matches( token )
                 || TokenMatcher.COMMA.matches( token )
                 || TokenMatcher.SEMI.matches( token ) )
      {
        result = pos - 1;
      } else {
        pos++;
      }
    }
    if( result == -1 ) {
      result = pos - 1;
    }
    return result;
  }

  public int findClosing( int offset ) {
    int result = -1;
    TokenMatcher leftMatcher;
    TokenMatcher rightMatcher;
    JavaScriptToken token = getToken( offset );
    if( TokenMatcher.LEFT_BRACE.matches( token ) ) {
      leftMatcher = TokenMatcher.LEFT_BRACE;
      rightMatcher = TokenMatcher.RIGHT_BRACE;
    } else if( TokenMatcher.LEFT_BRACKET.matches( token ) ) {
      leftMatcher = TokenMatcher.LEFT_BRACKET;
      rightMatcher = TokenMatcher.RIGHT_BRACKET;
    } else if( TokenMatcher.LEFT_PAREN.matches( token ) ) {
      leftMatcher = TokenMatcher.LEFT_PAREN;
      rightMatcher = TokenMatcher.RIGHT_PAREN;
    } else {
      String message = "Not an opening brace, bracket or parenthesis at pos " + offset;
      throw new IllegalArgumentException( message );
    }
    int level = 0;
    int pos = offset;
    while( pos < tokens.size() && result == -1 ) {
      token = getToken( pos );
      if( leftMatcher.matches( token ) ) {
        level++;
      } else if( rightMatcher.matches( token ) ) {
        level--;
        if( level == 0 ) {
          result = pos;
        }
      }
      pos++;
    }
    return result;
  }

  public int findInObjectLiteral( String key, int offset ) {
    if( !TokenMatcher.LEFT_BRACE.matches( getToken( offset ) ) ) {
      throw new IllegalArgumentException( "Not an object literal at pos " + offset );
    }
    int result = -1;
    int closingBrace = findClosing( offset );
    if( closingBrace == -1 ) {
      throw new IllegalArgumentException( "No closing brace found for pos " + offset );
    }
    int pos = offset + 1;
    TokenMatcher keyStringMatcher = TokenMatcher.string();
    TokenMatcher keyNameMatcher = TokenMatcher.name();
    while( pos < closingBrace - 2 ) {
      JavaScriptToken token = getToken( pos );
      if( keyStringMatcher.matches( token ) || keyNameMatcher.matches( token ) ) {
        if(    key.equals( keyStringMatcher.matchedValue )
            || key.equals( keyNameMatcher.matchedValue )
            || "default".equals( keyStringMatcher.matchedValue )
            || "default".equals( keyNameMatcher.matchedValue ) )
        {
          if( TokenMatcher.OBJECTLIT.matches( getToken( pos + 1 ) ) ) {
            result = pos + 2;
          }
        }
      }
      pos++;
    }
    return result;
  }
  
  public static class TokenPattern {
    
    private final TokenMatcher[] matchers;

    public TokenPattern( TokenMatcher[] matchers ) {
      this.matchers = matchers;
    }

    public int read( TokenList reader, int offset ) {
      boolean result = true;
      for( int i = 0; i < matchers.length && result; i++ ) {
        matchers[ i ].clear();
      }
      int pos = offset;
      for( int i = 0; i < matchers.length && result; i++ ) {
        result &= matchers[ i ].matches( reader.getToken( pos ) );
        pos++;
      }
      if( result ) {
        return pos - 1;
      }
      return -1;
    }
  }

  public static class TokenMatcher {

    public static final TokenMatcher DOT = TokenMatcher.literal( Token.DOT );

    public static final TokenMatcher IF = TokenMatcher.literal( Token.IF );

    public static final TokenMatcher ELSE = TokenMatcher.literal( Token.ELSE );

    public static final TokenMatcher COMMA = TokenMatcher.literal( Token.COMMA );

    public static final TokenMatcher SEMI = TokenMatcher.literal( Token.SEMI );

    public static final TokenMatcher OBJECTLIT = TokenMatcher.literal( Token.OBJECTLIT );

    public static final TokenMatcher LEFT_PAREN = TokenMatcher.literal( Token.LP );

    public static final TokenMatcher RIGHT_PAREN = TokenMatcher.literal( Token.RP );

    public static final TokenMatcher LEFT_BRACE = TokenMatcher.literal( Token.LC );

    public static final TokenMatcher RIGHT_BRACE = TokenMatcher.literal( Token.RC );

    public static final TokenMatcher LEFT_BRACKET = TokenMatcher.literal( Token.LB );
    
    public static final TokenMatcher RIGHT_BRACKET = TokenMatcher.literal( Token.RB );

    private final int expectedType;
    private final String expectedValue;
    public String matchedValue;

    public static TokenMatcher string() {
      return new TokenMatcher( Token.STRING, null );
    }

    public static TokenMatcher name() {
      return new TokenMatcher( Token.NAME, null );
    }

    public static TokenMatcher string( String string ) {
      return new TokenMatcher( Token.STRING, string );
    }

    public static TokenMatcher name( String string ) {
      return new TokenMatcher( Token.NAME, string );
    }

    public static TokenMatcher literal( int type ) {
      return new TokenMatcher( type, null );
    }

    private TokenMatcher( int type, String value ) {
      this.expectedType = type;
      this.expectedValue = value;
    }

    public boolean matches( JavaScriptToken token ) {
      boolean result = false;
      matchedValue = null;
      if( token != null ) {
        result = token.getType() == expectedType;
        if( expectedValue != null ) {
          result &= expectedValue.equals( token.getValue() );
        } else {
          matchedValue = token.getValue();
        }
      }
      return result;
    }

    public String getMatchedValue() {
      return matchedValue;
    }

    public void clear() {
      matchedValue = null;
    }
  }
}
