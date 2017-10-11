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

package org.pentaho.di.www;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServletTestUtils {
  public static final char[] BAD_CHARACTERS_TO_ESCAPE = {'<', '>', '\'', '\"'};
  public static final String BAD_STRING_TO_TEST = "!@#$%\"\'^&*()<>&/test string&";
 // Pattern to check that ampersand character '&' was successfully escaped.
 // Eg search excluding '&amp;', '&lt;', '&gt;', '&quote;', '&apos;', and numeric reference '&#'
  public static final Pattern PATTERN = Pattern.compile( "(&(?=(?!amp;))(?=(?!#[0-9a-f]{1,5};))(?=(?!lt;))(?=(?!gt;))(?=(?!quote;))(?=(?!apos;)))" );

  public static String getInsideOfTag( String tag, String string ) {
    String open = "<" + tag + ">";
    String close = "</" + tag + ">";
    return string.substring( string.indexOf( open ) + open.length(), string.indexOf( close ) );
  }

  public static boolean hasBadText( String value ) {
    Matcher matcher = PATTERN.matcher( value );
    if ( matcher.find() ) {
      return true;
    }
    return StringUtils.containsAny( value, BAD_CHARACTERS_TO_ESCAPE );
  }

}
