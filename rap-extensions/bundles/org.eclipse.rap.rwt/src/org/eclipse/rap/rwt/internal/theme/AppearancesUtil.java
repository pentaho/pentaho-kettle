/*******************************************************************************
 * Copyright (c) 2008, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class AppearancesUtil {

  private static final Pattern END_TEMPLATE_PATTERN
    = Pattern.compile( "(\\r|\\n).*?END TEMPLATE" );
  private static final Pattern BEGIN_TEMPLATE_PATTERN
    = Pattern.compile( "BEGIN TEMPLATE.*(\\r|\\n)" );

  private AppearancesUtil() {
    // prevent instantiation
  }

  public static String readAppearanceFile( InputStream inStream ) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStreamReader reader = new InputStreamReader( inStream, "UTF-8" );
    BufferedReader br = new BufferedReader( reader );
    for( int i = 0; i < 100; i++ ) {
      int character = br.read();
      while( character != -1 ) {
        sb.append( ( char )character );
        character = br.read();
      }
    }
    return stripTemplate( sb.toString() );
  }

  private static String stripTemplate( String input ) {
    int beginIndex = 0;
    int endIndex = input.length();
    Matcher matcher;
    matcher = BEGIN_TEMPLATE_PATTERN.matcher( input );
    if( matcher.find() ) {
      beginIndex = matcher.end();
    }
    matcher = END_TEMPLATE_PATTERN.matcher( input );
    if( matcher.find() ) {
      endIndex = matcher.start();
    }
    return input.substring( beginIndex, endIndex );
  }
}
