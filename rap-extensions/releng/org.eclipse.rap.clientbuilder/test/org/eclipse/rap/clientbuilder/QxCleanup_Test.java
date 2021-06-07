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

import static org.junit.Assert.*;

import org.junit.Test;



public class QxCleanup_Test {

  @Test
  public void testRemoveEmptyDebugVariantConditional() throws Exception {
    String input = "if( rwt.util.Variant.isSet( \"qx.debug\", \"on\" ) ) {\n}\n";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( 0, tokens.size() );
  }

  @Test
  public void testRemoveCompatVariantConditional() throws Exception {
    String input = "if( rwt.util.Variant.isSet( \"qx.compatibility\", \"on\" ) ) {\n}\n";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( 0, tokens.size() );
  }

  @Test
  public void testRemoveAspectVariantConditional() throws Exception {
    String input = "if( rwt.util.Variant.isSet( \"qx.aspects\", \"on\" ) ) {\n}\n";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( 0, tokens.size() );
  }

  @Test
  public void testRemoveMultipleVariantConditionals() throws Exception {
    String input = "if( rwt.util.Variant.isSet( \"qx.debug\", \"on\" ) ) {\n}\n";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( 0, tokens.size() );
  }

  @Test
  public void testRemoveVariantConditionalBetweenStatements() throws Exception {
    String input = "a = 1;\n"
                 + "if( rwt.util.Variant.isSet( \"qx.debug\", \"on\" ) ) {\n"
                 + "  if( false ) { throw \"ERROR\" }\n"
                 + "}\n"
                 + "b = 2;";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( "a = 1;\nb = 2;", JavaScriptPrinter.printTokens( tokens ) );
  }

  @Test
  public void testRemoveVariantConditionalWithElseBlock() throws Exception {
    String input = "if( rwt.util.Variant.isSet( \"qx.debug\", \"on\" ) ) {\n"
                 + "  a = 1;\n"
                 + "}\n else {\n"
                 + "  b = 2;\n"
                 + "}";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( "b = 2;", JavaScriptPrinter.printTokens( tokens ) );
  }

  @Test
  public void testRemoveNestedVariantConditional() throws Exception {
    String input = "if( vObject && vObject.__disposed === false ) {\n"
                 + "  try {\n"
                 + "    vObject.dispose();\n"
                 + "  }\n"
                 + "  catch( ex ) {\n"
                 + "    if( rwt.util.Variant.isSet( \"qx.debug\", \"on\" ) ) {\n"
                 + "      qx.core.Log.warn( \"Could not dispose: \" + vObject + \":\", ex );\n"
                 + "    }\n"
                 + "  }\n"
                 + "}\n";
    String expected = "if ( vObject && vObject.__disposed === false ) {\n"
                    + "  try {\n"
                    + "    vObject.dispose ( );\n"
                    + "  }\n"
                    + "  catch ( ex ) {\n"
                    + "  }\n"
                    + "}";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( expected, JavaScriptPrinter.printTokens( tokens ) );
  }

  @Test
  public void testReplaceVariantSelection() throws Exception {
    String input =   "result = rwt.util.Variant.select( \"qx.debug\", {\n"
                   + "  \"on\": {\n"
                   + "    \"foo\" : 23,\n"
                   + "    \"bar\" : 42\n"
                   + "  },\n"
                   + "  \"default\" : null\n"
                   + "} )";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    assertEquals( "result = null;", JavaScriptPrinter.printTokens( tokens ) );
  }

  @Test
  public void testReplaceBaseCall() throws Exception {
    String input = "result = this.base( arguments );";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    String expected = "result = arguments.callee.base.call ( this );";
    assertEquals( expected, JavaScriptPrinter.printTokens( tokens ) );
  }

  @Test
  public void testReplaceBaseCallWithParameters() throws Exception {
    String input = "result = this.base( arguments, 23, 'foo' );";
    TokenList tokens = TestUtil.parse( input );

    QxCodeCleaner cleaner = new QxCodeCleaner( tokens );
    cleaner.cleanupQxCode();

    String expected = "result = arguments.callee.base.call ( this, 23, \"foo\" );";
    assertEquals( expected, JavaScriptPrinter.printTokens( tokens ) );
  }

}
