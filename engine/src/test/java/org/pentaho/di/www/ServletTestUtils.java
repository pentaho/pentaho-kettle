/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
