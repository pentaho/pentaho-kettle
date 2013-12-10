/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
