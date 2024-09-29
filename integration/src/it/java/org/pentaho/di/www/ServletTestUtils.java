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

public class ServletTestUtils {
  public static final String BAD_STRING = "!@#$%^&*()<>/";

  public static String getInsideOfTag( String tag, String string ) {
    String open = "<" + tag + ">";
    String close = "</" + tag + ">";
    return string.substring( string.indexOf( open ) + open.length(), string.indexOf( close ) );
  }

  public static boolean hasBadText( String value ) {
    return value.indexOf( '!' ) != -1
      || value.indexOf( '@' ) != -1 || value.indexOf( '$' ) != -1 || value.indexOf( '%' ) != -1
      || value.indexOf( '^' ) != -1 || value.indexOf( '*' ) != -1 || value.indexOf( '(' ) != -1
      || value.indexOf( ')' ) != -1 || value.indexOf( '<' ) != -1 || value.indexOf( '>' ) != -1
      || value.indexOf( '/' ) != -1;
  }
}
