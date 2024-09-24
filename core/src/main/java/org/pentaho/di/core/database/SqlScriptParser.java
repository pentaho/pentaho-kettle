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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This class represents a splitter of SQL script into separate statements. It respects the notion of a string
 * literal and comments, such that if a separator appears in a string literal or comment, it is treated as
 * part of the string or comment instead of a separator.
 *
 * @author Alexander Buloichik
 */
public class SqlScriptParser {

  enum MODE {
    SQL, LINE_COMMENT, BLOCK_COMMENT, STRING
  };

  private boolean usingBackslashAsEscapeCharForQuotation;
  /**
   * @param usingBackslashAsEscapeCharForQuotation use backslash as escape char for quotation (\')
   */
  public SqlScriptParser( boolean usingBackslashAsEscapeCharForQuotation ) {
    this.usingBackslashAsEscapeCharForQuotation = usingBackslashAsEscapeCharForQuotation;
  }

  /**
   * This method splits script into separate statements.
   *
   * @param script a string representing the SQL script to parse
   * @return the list of statements
   */
  public List<String> split( String script ) {
    if ( script == null ) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<String>();

    MODE mode = MODE.SQL;

    char currentStringChar = 0;
    int statementStart = 0;

    for ( int i = 0; i < script.length(); i++ ) {
      char ch = script.charAt( i );
      char nextCh = i < script.length() - 1 ? script.charAt( i + 1 ) : 0;
      switch ( mode ) {
        case SQL:
          switch ( ch ) {
            case '/':
              if ( nextCh == '*' ) {
                mode = MODE.BLOCK_COMMENT;
                i++;
              }
              break;
            case '-':
              if ( nextCh == '-' ) {
                mode = MODE.LINE_COMMENT;
                i++;
              }
              break;
            case '\'':
            case '"':
              mode = MODE.STRING;
              currentStringChar = ch;
              break;
            case ';':
              String st = script.substring( statementStart, i );
              if ( StringUtils.isNotBlank( st ) ) {
                result.add( st );
              }
              statementStart = i + 1;
              break;
          }
          break;
        case BLOCK_COMMENT:
          if ( ch == '*' ) {
            if ( nextCh == '/' ) {
              mode = MODE.SQL;
              i++;
            }
          }
          break;
        case LINE_COMMENT:
          if ( ch == '\n' || ch == '\r' ) {
            mode = MODE.SQL;
          }
          break;
        case STRING:
          if ( ch == '\\' && nextCh == '\\' ) {
            /*
             * The user is hard-coding a backslash into the string.
             * Pass the hard-coded backslash through, and skip over the real backslash on the next loop
             */
            i++;
          } else if ( ch == '\\' && nextCh == currentStringChar && usingBackslashAsEscapeCharForQuotation ) {
            /*
             * The user is hard-coding a quote character into the string.
             * Pass the hard-coded quote character through, and skip over the quote on next loop
             */

            /*
             * usingBackslashAsEscapeCharForQuotation
             * PDI-16224.
             *
             * ANSI standards specify that using the backslash character (\) to escape single (' ') or double (" ")
             * quotation marks is invalid. For example, the following attempt to find a quotation mark does not conform to ANSI standards:
             * where col1 = '\'';"
             * In any way a construction '\'|| is correct for Oracle but for others DBs (ex. MySQl) isn't correct.
             *
             */
            i++;
          } else if ( ch == currentStringChar ) {
            mode = MODE.SQL;
          }
          break;
      }
    }
    if ( statementStart < script.length() ) {
      String st = script.substring( statementStart );
      if ( StringUtils.isNotBlank( st ) ) {
        result.add( st );
      }
    }
    return result;
  }

  /**
   * This method removes comments from one statement.
   *
   * @param script a string representing the SQL script to parse
   * @return script without comments
   */
  public String removeComments( String script ) {
    if ( script == null ) {
      return null;
    }

    StringBuilder result = new StringBuilder();

    MODE mode = MODE.SQL;

    char currentStringChar = 0;

    for ( int i = 0; i < script.length(); i++ ) {
      char ch = script.charAt( i );
      char nextCh = i < script.length() - 1 ? script.charAt( i + 1 ) : 0;
      char nextPlusOneCh = i < script.length() - 2 ? script.charAt( i + 2 ) : 0;
      switch ( mode ) {
        case SQL:
          switch ( ch ) {
            case '/':
              if ( nextCh == '*' && nextPlusOneCh != '+' ) {
                mode = MODE.BLOCK_COMMENT;
                i++;
                ch = 0;
              }
              break;
            case '-':
              if ( nextCh == '-' ) {
                mode = MODE.LINE_COMMENT;
                i++;
                ch = 0;
              }
              break;
            case '\'':
            case '"':
              mode = MODE.STRING;
              currentStringChar = ch;
              break;
          }
          break;
        case BLOCK_COMMENT:
          if ( ch == '*' ) {
            if ( nextCh == '/' ) {
              mode = MODE.SQL;
              i++;
            }
          }
          ch = 0;
          break;
        case LINE_COMMENT:
          if ( ch == '\n' || ch == '\r' ) {
            mode = MODE.SQL;
          } else {
            ch = 0;
          }
          break;
        case STRING:
          if ( ch == '\\' && nextCh == currentStringChar && usingBackslashAsEscapeCharForQuotation ) {
            /*
             * The user is hard-coding a quote character into the string.
             * Pass the hard-coded quote character through, and skip over the quote on next loop
             */

            /*
             * usingBackslashAsEscapeCharForQuotation
             * PDI-16224.
             *
             * ANSI standards specify that using the backslash character (\) to escape single (' ') or double (" ")
             * quotation marks is invalid. For example, the following attempt to find a quotation mark does not conform to ANSI standards:
             * where col1 = '\'';"
             * In any way a construction '\'|| is correct for Oracle but for others DBs (ex. MySQl) isn't correct.
             *
             */

            result.append( ch );
            result.append( nextCh );
            ch = 0;
            i++;
          } else if ( ch == currentStringChar ) {
            mode = MODE.SQL;
          }
          break;
      }
      if ( ch != 0 ) {
        result.append( ch );
      }
    }

    return result.toString();
  }
}
