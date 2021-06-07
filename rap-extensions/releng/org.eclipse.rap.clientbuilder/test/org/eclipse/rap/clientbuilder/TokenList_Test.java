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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptToken;


public class TokenList_Test {

  @Test
  public void testCreate() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "Test" ) );
    tokens.add( new JavaScriptToken( Token.LB, "[" ) );

    TokenList tokenList = new TokenList( tokens );

    assertEquals( 2, tokenList.size() );
  }

  @Test
  public void testGetToken() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "Test" ) );
    tokens.add( new JavaScriptToken( Token.LB, "[" ) );

    TokenList tokenList = new TokenList( tokens );

    assertNull( tokenList.getToken( -1 ) );
    assertEquals( tokens.get( 0 ), tokenList.getToken( 0 ) );
    assertEquals( tokens.get( 1 ), tokenList.getToken( 1 ) );
    assertNull( tokenList.getToken( 2 ) );
  }

  @Test
  public void testRemoveToken() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "First" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "Second" ) );

    TokenList tokenList = new TokenList( tokens );
    tokenList.removeToken( 0 );

    assertEquals( 1, tokenList.size() );
    assertEquals( "Second", tokenList.getToken( 0 ).getValue() );
  }

  @Test
  public void testRemoveToken_ModifiesOriginalList() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "First" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "Second" ) );

    TokenList tokenList = new TokenList( tokens );
    tokenList.removeToken( 0 );

    assertEquals( 1, tokens.size() );
    assertEquals( "Second", tokens.get( 0 ).getValue() );
  }

  @Test
  public void testReplaceToken() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "orig 1" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 2" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 3" ) );

    TokenList tokenList = new TokenList( tokens );
    JavaScriptToken[] replacement = new JavaScriptToken[] {
      new JavaScriptToken( Token.STRING, "new 1" ),
      new JavaScriptToken( Token.STRING, "new 2" )
    };
    tokenList.replaceToken( 1, replacement );

    assertEquals( 4, tokenList.size() );
    assertEquals( "orig 1", tokenList.getToken( 0 ).getValue() );
    assertEquals( "new 1", tokenList.getToken( 1 ).getValue() );
    assertEquals( "new 2", tokenList.getToken( 2 ).getValue() );
    assertEquals( "orig 3", tokenList.getToken( 3 ).getValue() );
  }

  @Test
  public void testReplaceToken_WithNull() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "orig 1" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 2" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 3" ) );

    TokenList tokenList = new TokenList( tokens );
    tokenList.replaceToken( 1, null );

    assertEquals( 2, tokenList.size() );
    assertEquals( "orig 1", tokenList.getToken( 0 ).getValue() );
    assertEquals( "orig 3", tokenList.getToken( 1 ).getValue() );
  }

  @Test
  public void testReplaceToken_ModifiesOriginalList() {
    List<JavaScriptToken> tokens = new ArrayList<>();
    tokens.add( new JavaScriptToken( Token.STRING, "orig 1" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 2" ) );
    tokens.add( new JavaScriptToken( Token.STRING, "orig 3" ) );

    TokenList tokenList = new TokenList( tokens );
    JavaScriptToken[] replacement = new JavaScriptToken[] {
      new JavaScriptToken( Token.STRING, "new 1" ),
      new JavaScriptToken( Token.STRING, "new 2" )
    };
    tokenList.replaceToken( 1, replacement );

    assertEquals( 4, tokenList.size() );
    assertEquals( "orig 1", tokens.get( 0 ).getValue() );
    assertEquals( "new 1", tokens.get( 1 ).getValue() );
    assertEquals( "new 2", tokens.get( 2 ).getValue() );
    assertEquals( "orig 3", tokens.get( 3 ).getValue() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testFindClosingFailsIfNotOnOpeningBrace() throws Exception {
    String input = "a, b, c";
    TokenList tokens = TestUtil.parse( input );

    tokens.findClosing( 0 );
  }

  @Test
  public void testFindClosingBrace() throws Exception {
    String input = "a = { foo : 23, bar : { x : 7, y : [ 1, 2, 3 ] } }";

    TokenList tokens = TestUtil.parse( input );

    int closingBrace = tokens.findClosing( 2 );
    assertEquals( 24, closingBrace );
  }

  @Test
  public void testFindClosingBracket() throws Exception {
    String input = "a = [ \"foo\", 23, { x : 7, y : [ 1, 2, 3 ] } ]";

    TokenList tokens = TestUtil.parse( input );

    int closingBrace = tokens.findClosing( 2 );
    assertEquals( 22, closingBrace );
  }

  @Test
  public void testFindInObjectLiteral() throws Exception {
    String input = "a = { \"foo\" : {}, \"bar\" : {} }";

    TokenList tokens = TestUtil.parse( input );

    int startSelectedExpr = tokens.findInObjectLiteral( "foo", 2 );
    assertEquals( 5, startSelectedExpr );
  }

  @Test
  public void testFindInObjectLiteralUnquoted() throws Exception {
    String input = "a = { foo : {}, bar : {} }";

    TokenList tokens = TestUtil.parse( input );

    int startSelectedExpr = tokens.findInObjectLiteral( "bar", 2 );
    assertEquals( 10, startSelectedExpr );
  }

  @Test
  public void testFindInObjectLiteralDefault() throws Exception {
    String input = "a = { \"foo\" : {}, \"default\" : {} }";

    TokenList tokens = TestUtil.parse( input );

    int startSelectedExpr = tokens.findInObjectLiteral( "bar", 2 );
    assertEquals( 10, startSelectedExpr );
  }

}
