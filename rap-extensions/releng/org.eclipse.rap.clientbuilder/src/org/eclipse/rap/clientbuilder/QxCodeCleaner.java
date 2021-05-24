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
import java.util.Iterator;
import java.util.List;

import org.eclipse.rap.clientbuilder.TokenList.TokenMatcher;
import org.mozilla.javascript.Token;

import com.yahoo.platform.yui.compressor.JavaScriptToken;


public class QxCodeCleaner {

  private final TokenList tokens;

  private final List<Replacement> replacements;

  public QxCodeCleaner( TokenList tokens ) {
    this.tokens = tokens;
    replacements = new ArrayList<>();
  }

  public void cleanupQxCode() {
    int pos = 0;
    while( pos < tokens.size() ) {
      int nextPos = removeVariantConditional( pos );
      if( nextPos == pos ) {
        nextPos = replaceVariantSelection( pos );
      }
      if( nextPos == pos ) {
        nextPos = replaceBaseCall( pos );
      }
      if( nextPos == pos ) {
        nextPos++;
      }
      pos = nextPos;
    }
    doReplacements();
  }

  private int removeVariantConditional( int offset ) {
    int nextPos = offset;
    VariantConditional conditional = readVariantConditional( offset );
    if( conditional != null ) {
      if( canRemoveVariant( conditional.variant ) ) {
        int endExpr = tokens.readExpression( conditional.end + 1 );
        if( endExpr != -1 ) {
          markTokensForRemoval( conditional.begin, endExpr );
          nextPos = endExpr + 1;
          if( TokenMatcher.ELSE.matches( tokens.getToken( nextPos ) ) ) {
            markTokensForRemoval( nextPos, nextPos );
            nextPos++;
            if( TokenMatcher.LEFT_BRACE.matches( tokens.getToken( nextPos ) ) ) {
              int closingBrace = tokens.findClosing( nextPos );
              if( closingBrace != -1 ) {
                markTokensForRemoval( nextPos, nextPos );
                markTokensForRemoval( closingBrace, closingBrace );
                nextPos++;
              }
            }
          }
        }
      }
    }
    return nextPos;
  }

  private int replaceVariantSelection( int offset ) {
    int nextPos = offset;
    VariantSelection selection = readVariantSelection( offset );
    if( selection != null ) {
      int closingBrace = tokens.findClosing( selection.end );
      if( closingBrace != -1 ) {
        if( TokenMatcher.RIGHT_PAREN.matches( tokens.getToken( closingBrace + 1 ) ) ) {
          int closingParen = closingBrace + 1;
          nextPos = selection.end + 1;
          if( canRemoveVariant( selection.variant ) ) {
            int selectedExpression
              = tokens.findInObjectLiteral( "off", selection.end );
            if( selectedExpression != -1 ) {
              int endExpression = tokens.readExpression( selectedExpression );
              if( endExpression != -1 ) {
                markTokensForRemoval( offset, selectedExpression - 1 );
                markTokensForRemoval( endExpression + 1, closingParen );
                nextPos = closingParen + 1;
              }
            }
          }
        }
      }
    }
    return nextPos;
  }

  private int replaceBaseCall( int offset ) {
    int nextPos = offset;
    Range baseCall = readBaseCall( offset );
    if( baseCall != null ) {
      JavaScriptToken[] replacement = new JavaScriptToken[] {
        new JavaScriptToken( Token.NAME, "arguments" ),
        new JavaScriptToken( Token.DOT, "." ),
        new JavaScriptToken( Token.NAME, "callee" ),
        new JavaScriptToken( Token.DOT, "." ),
        new JavaScriptToken( Token.NAME, "base" ),
        new JavaScriptToken( Token.DOT, "." ),
        new JavaScriptToken( Token.NAME, "call" ),
        new JavaScriptToken( Token.LP, "(" ),
        new JavaScriptToken( Token.NAME, "this" )
      };
      markRangeForReplacement( baseCall, replacement );
      nextPos = baseCall.end + 1;
    }
    return nextPos;
  }

  private int markTokensForRemoval( int first, int last ) {
    replacements.add( new Replacement( first, last, null ) );
    return last - first + 1;
  }

  private void markRangeForReplacement( Range range,
                                        JavaScriptToken[] replacementTokens )
  {
    replacements.add( new Replacement( range.begin,
                                       range.end,
                                       replacementTokens ) );
  }

  private void doReplacements() {
    Collections.sort( replacements, new Comparator<Replacement>() {
      @Override
      public int compare( Replacement repl1, Replacement repl2 ) {
        return repl1.end < repl2.end ? 1 : repl1.end == repl2.end ? 0 : -1;
      }
    } );
    for( Iterator<Replacement> iterator = replacements.iterator(); iterator.hasNext(); ) {
      Replacement replacement = iterator.next();
      tokens.replaceTokens( replacement.begin, replacement.end, replacement.replacement );
    }
  }

  private static boolean canRemoveVariant( String variantName ) {
    return "qx.debug".equals( variantName )
           || "qx.compatibility".equals( variantName )
           || "qx.aspects".equals( variantName );
  }

  VariantConditional readVariantConditional( int offset ) {
    VariantConditional result = null;
    int pos = offset;
    boolean matched = true;
    TokenMatcher nameMatcher = TokenMatcher.string();
    matched &= TokenMatcher.IF.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.LEFT_PAREN.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "rwt" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "util" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "Variant" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "isSet" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.LEFT_PAREN.matches( tokens.getToken( pos++ ) );
    matched &= nameMatcher.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.COMMA.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.string( "on" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.RIGHT_PAREN.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.RIGHT_PAREN.matches( tokens.getToken( pos++ ) );
    if( matched ) {
      result = new VariantConditional( offset, pos - 1, nameMatcher.matchedValue );
    }
    return result;
  }

  VariantSelection readVariantSelection( int offset ) {
    VariantSelection result = null;
    int pos = offset;
    boolean matched = true;
    TokenMatcher nameMatcher = TokenMatcher.string();
    matched &= TokenMatcher.name( "rwt" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "util" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "Variant" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "select" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.LEFT_PAREN.matches( tokens.getToken( pos++ ) );
    matched &= nameMatcher.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.COMMA.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.LEFT_BRACE.matches( tokens.getToken( pos++ ) );
    if( matched ) {
      result = new VariantSelection( offset, pos - 1, nameMatcher.matchedValue );
    }
    return result;
  }

  Range readBaseCall( int offset ) {
    Range result = null;
    int pos = offset;
    boolean matched = true;
    TokenMatcher nameMatcher = TokenMatcher.name( "arguments" );
    matched &= TokenMatcher.literal( Token.THIS ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.DOT.matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.name( "base" ).matches( tokens.getToken( pos++ ) );
    matched &= TokenMatcher.LEFT_PAREN.matches( tokens.getToken( pos++ ) );
    matched &= nameMatcher.matches( tokens.getToken( pos++ ) );
    if( matched ) {
      result = new Range( offset, pos - 1 );
    }
    return result;
  }

  public static class Range {
    public final int begin;
    public final int end;

    public Range( int begin, int end ) {
      this.begin = begin;
      this.end = end;
    }
  }

  public static class Replacement extends Range {

    public final JavaScriptToken[] replacement;

    public Replacement( int begin, int end, JavaScriptToken[] replacement ) {
      super( begin, end );
      this.replacement = replacement;
    }
  }

  static class VariantConditional extends Range {

    public final String variant;

    public VariantConditional( int begin, int end, String variant ) {
      super( begin, end );
      this.variant = variant;
    }
  }

  static class VariantSelection extends Range {

    public final String variant;

    public VariantSelection( int begin, int end, String variant ) {
      super( begin, end );
      this.variant = variant;
    }
  }

}
