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
package com.yahoo.platform.yui.compressor;

import static org.junit.Assert.*;
import org.eclipse.rap.clientbuilder.TestUtil;
import org.eclipse.rap.clientbuilder.TokenList;
import org.junit.Test;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Token;


public class Parser_Test {

  static final ErrorReporter REPORTER = new TestErrorReporter();

  @Test
  public void testParseNumber() throws Exception {
    TokenList result = TestUtil.parse( "23.0" );

    assertEquals( 2, result.size() );
    assertEquals( Token.NUMBER, result.getToken( 0 ).getType() );
    assertEquals( Token.SEMI, result.getToken( 1 ).getType() );
  }

  @Test
  public void testParseVar() throws Exception {
    TokenList tokens = TestUtil.parse( "var x = 12;" );

    assertEquals( 5, tokens.size() );
    assertEquals( Token.VAR, tokens.getToken( 0 ).getType() );
    assertEquals( Token.NAME, tokens.getToken( 1 ).getType() );
    assertEquals( Token.ASSIGN, tokens.getToken( 2 ).getType() );
    assertEquals( Token.NUMBER, tokens.getToken( 3 ).getType() );
    assertEquals( Token.SEMI, tokens.getToken( 4 ).getType() );
  }

  @Test
  public void testParseAssignment() throws Exception {
    TokenList tokens = TestUtil.parse( "a = 1;" );

    assertEquals( 4, tokens.size() );
    assertEquals( Token.NAME, tokens.getToken( 0 ).getType() );
    assertEquals( Token.ASSIGN, tokens.getToken( 1 ).getType() );
    assertEquals( Token.NUMBER, tokens.getToken( 2 ).getType() );
    assertEquals( Token.SEMI, tokens.getToken( 3 ).getType() );
  }

  @Test
  public void testParseArrayAccess() throws Exception {
    TokenList tokens = TestUtil.parse( "a = $[23];" );

    assertEquals( 7, tokens.size() );
    assertEquals( Token.NAME, tokens.getToken( 0 ).getType() );
    assertEquals( Token.ASSIGN, tokens.getToken( 1 ).getType() );
    assertEquals( Token.NAME, tokens.getToken( 2 ).getType() );
    assertEquals( Token.LB, tokens.getToken( 3 ).getType() );
    assertEquals( Token.NUMBER, tokens.getToken( 4 ).getType() );
    assertEquals( Token.RB, tokens.getToken( 5 ).getType() );
    assertEquals( Token.SEMI, tokens.getToken( 6 ).getType() );
  }

}
