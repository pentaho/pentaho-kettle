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

package org.pentaho.di.trans.steps.tablecompare;

public class Kjube {

  public static final String DEFAULT_CUSTOMER_PARAMETER = "KJUBE_CUSTOMER";
  public static final String DEFAULT_APPLICATION_PARAMETER = "KJUBE_APPLICATION";
  public static final String DEFAULT_LIFECYCLE_PARAMETER = "KJUBE_LIFECYCLE";

  public static final String DEFAULT_CONFIG_FILE_PATH =
    "/kjube/projects/${KJUBE_CUSTOMER}/${KJUBE_APPLICATION}/config/configuration_${KJUBE_LIFECYCLE}.properties";

  public static final String DEFAULT_BATCH_ID_CONNECTION = "${KJUBE_BATCH_ID_CONNECTION}";
  public static final String DEFAULT_BATCH_ID_SCHEMA = "${KJUBE_BATCH_ID_SCHEMA}";
  public static final String DEFAULT_BATCH_ID_TABLE = "${KJUBE_BATCH_ID_TABLE}";

  public static final String DEFAULT_BATCH_LOGGING_CONNECTION = "${KJUBE_BATCH_LOGGING_CONNECTION}";
  public static final String DEFAULT_BATCH_LOGGING_SCHEMA = "${KJUBE_BATCH_LOGGING_SCHEMA}";
  public static final String DEFAULT_BATCH_LOGGING_TABLE = "${KJUBE_BATCH_LOGGING_TABLE}";

  public static final String DEFAULT_REJECTS_SCHEMA = "${KJUBE_REJECTS_SCHEMA}";
  public static final String DEFAULT_REJECTS_TABLE = "${KJUBE_REJECTS_TABLE}";

  public static final String DEFAULT_BATCH_ID_VARIABLE_NAME = "${KJUBE_BATCH_ID}";

  public static final String DEFAULT_ERROR_COUNT_VARIABLE_NAME = "${KJUBE_ERROR_COUNT_FIELD}";
  public static final String DEFAULT_ERROR_DESCRIPTIONS_VARIABLE_NAME = "${KJUBE_ERROR_DESCRIPTIONS_FIELD}";
  public static final String DEFAULT_ERROR_FIELDS_VARIABLE_NAME = "${KJUBE_ERROR_FIELDS_FIELD}";
  public static final String DEFAULT_ERROR_CODES_VARIABLE_NAME = "${KJUBE_ERROR_CODES_FIELD}";

  /**
   * Determines whether or not a character is considered a space. A character is considered a space in Kettle if it is a
   * space, a tab, a newline or a cariage return.
   *
   * @param c
   *          The character to verify if it is a space.
   * @return true if the character is a space. false otherwise.
   */
  public static final boolean isSpace( char c ) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n' || Character.isWhitespace( c );
  }

  /**
   * Left trim: remove spaces to the left of a String.
   *
   * @param str
   *          The String to left trim
   * @return The left trimmed String
   */
  public static String ltrim( String source ) {
    if ( source == null ) {
      return null;
    }
    int from = 0;
    while ( from < source.length() && isSpace( source.charAt( from ) ) ) {
      from++;
    }

    return source.substring( from );
  }

  /**
   * Right trim: remove spaces to the right of a string
   *
   * @param str
   *          The string to right trim
   * @return The trimmed string.
   */
  public static String rtrim( String source ) {
    if ( source == null ) {
      return null;
    }

    int max = source.length();
    while ( max > 0 && isSpace( source.charAt( max - 1 ) ) ) {
      max--;
    }

    return source.substring( 0, max );
  }

  /**
   * Trims a string: removes the leading and trailing spaces of a String.
   *
   * @param str
   *          The string to trim
   * @return The trimmed string.
   */
  public static final String trim( String str ) {
    if ( str == null ) {
      return null;
    }

    int max = str.length() - 1;
    int min = 0;

    while ( min <= max && isSpace( str.charAt( min ) ) ) {
      min++;
    }
    while ( max >= 0 && isSpace( str.charAt( max ) ) ) {
      max--;
    }

    if ( max < min ) {
      return "";
    }

    return str.substring( min, max + 1 );
  }

}
