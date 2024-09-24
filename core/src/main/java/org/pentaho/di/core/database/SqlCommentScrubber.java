/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.database;

import java.io.IOException;
import java.io.StringReader;

/**
 * This class represents a parser that will remove SQL comments (both multi-line and single-line) from a string
 * representing a SQL query. It respects the notion of a string literal, such that if a comment appears in a string
 * literal, it is treated as part of the string instead of a comment. Both single-quoted and double-quoted string
 * literals are supported, including nested quotes (whether the SQL dialect supports them or not).
 *
 * @author Matt Burgess
 * 
 * @deprecated Use SqlScriptParser instead.
 */
@Deprecated
public class SqlCommentScrubber {

  /** End-of-File (EOF) indicator **/
  public static final int EOF = -1;

  /** End-of-Line (EOL) indicator **/
  public static final int EOL = 10;

  /** List of characters that can signify a string literal **/
  private static final int[] QUOTE_CHARS = { '\'', '"' };

  /**
   * Private constructor to enforce static access
   */
  private SqlCommentScrubber() {
  }

  /**
   * Checks to see whether the character is a quote character
   *
   * @param ch
   *          the input character to check
   *
   * @return true if the input character is a quote character, false if not
   */
  private static boolean isQuoteChar( int ch ) {
    for ( int c : QUOTE_CHARS ) {
      if ( ch == c ) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method will remove SQL comments (both multi-line and single-line) from a string representing a SQL query. It
   * respects the notion of a string literal, such that if a comment appears in a string literal, it is treated as part
   * of the string instead of a comment. A simple state machine is implemented, keeping track of whether the current
   * character is starting, ending, or inside a comment construct. The state machine also checks to see if the current
   * character is starting, ending, or inside a single-quoted string literal, as this takes precedence over comment
   * constructs. In other words, comments inside strings are not actually comments, they are part of the string literal.
   *
   * @param text
   *          a string representing the SQL query to parse and from which to remove comments
   *
   * @return the input string with SQL comments removed, or null if the input string is null
   */
  public static String removeComments( String text ) {

    if ( text == null ) {
      return null;
    }

    StringBuilder queryWithoutComments = new StringBuilder();
    boolean blkComment = false;
    boolean lineComment = false;
    boolean inString = false;
    StringReader buffer = new StringReader( text );
    int ch;
    char currentStringChar = (char) QUOTE_CHARS[0];
    boolean done = false;

    try {
      while ( !done ) {
        switch ( ch = buffer.read() ) {
          case EOF: { // End Of File
            done = true;
            break;
          }
          case '\'': // NOTE: Add cases for any other quote characters in QUOTE_CHARS
          case '"': { // String literals

            // If we're not in a comment, we're either entering or leaving a string
            if ( !lineComment && !blkComment ) {
              char cch = (char) ch;
              if ( inString ) {
                if ( currentStringChar == cch ) {
                  inString = false;
                }
              } else {
                inString = true;
                currentStringChar = cch;
              }
              queryWithoutComments.append( cch );
            }
            break;
          }
          case '/': { // multi-line comments

            // If we're not in a line comment, we might be entering a line or multi-line comment
            if ( !lineComment ) {
              ch = buffer.read();

              // If we see a multi-line comment starter (/*) and we're not in a string or
              // multi-line comment, then we have started a multi-line comment.
              if ( ( ch == '*' ) && ( !blkComment ) && ( !inString ) ) {
               // Make sure that the next character isn't a + which identifies a hint in Oracle (PDI-13054)
                ch = buffer.read();
                if ( ch == '+' ) {
                  queryWithoutComments.append( '/' );
                  queryWithoutComments.append( '*' );
                  queryWithoutComments.append( '+' );
                } else {
                  blkComment = true;
                }
              } else {
                // Otherwise if we aren't already in a block comment, pass the chars through
                if ( !blkComment ) {
                  queryWithoutComments.append( '/' );
                  queryWithoutComments.append( (char) ch );
                  if ( inString && ( currentStringChar == (char) ch ) ) {
                    inString = false;
                  }
                }
              }
            }
            break;
          }
          case '*': { // multi-line comments

            // If we're in a multi-line comment, look ahead to see if we're about to exit
            if ( blkComment ) {
              ch = buffer.read();
              if ( ch == '/' ) {
                blkComment = false;
              }
            } else {
              // if we're not in a multi-line or line comment, pass the char through
              if ( !lineComment ) {
                queryWithoutComments.append( '*' );
              }
            }
            break;
          }
          case '-': { // single-line comment

            // if we're not in a multi-line or line comment, we might be entering a line comment
            if ( !blkComment && !lineComment ) {
              ch = buffer.read();
              // If we look ahead to see another dash and we're not in a string, we're entering a line comment
              if ( ch == '-' && !inString ) {
                lineComment = true;
              } else {
                queryWithoutComments.append( '-' );
                queryWithoutComments.append( (char) ch );
                // If it's a quote character, we're entering or leaving a string
                if ( isQuoteChar( ch ) ) {
                  char cch = (char) ch;
                  if ( inString ) {
                    if ( currentStringChar == cch ) {
                      inString = false;
                    }
                  } else {
                    inString = true;
                    currentStringChar = cch;
                  }
                }
              }
            }
            break;
          }
          case EOL: { // End Of Line
            // If we're not in a comment, pass the EOL through
            if ( !blkComment && !lineComment ) {
              queryWithoutComments.append( (char) ch );
            }
            lineComment = false;
            break;
          }
          default: {
            // if we're not in a comment, pass the character through
            if ( !blkComment && !lineComment ) {
              queryWithoutComments.append( (char) ch );
            }
            break;
          }
        }
      }
    } catch ( IOException e ) {
      // break on error, exit gracefully with altered query thus far
    }

    return queryWithoutComments.toString();
  }
}
