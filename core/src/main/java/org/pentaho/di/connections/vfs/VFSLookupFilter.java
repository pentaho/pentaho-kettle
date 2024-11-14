/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.connections.vfs;

import org.pentaho.di.connections.LookupFilter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bmorrise on 2/12/19.
 */
public class VFSLookupFilter implements LookupFilter {

  private ConcurrentHashMap<String, String> keyLookup = new ConcurrentHashMap<>();

  @Override public String filter( String input ) {
    Pattern pattern = Pattern.compile( "^([\\w]+)://" );
    Matcher matcher = pattern.matcher( input );
    if ( matcher.find() ) {
      input = matcher.group( 1 );
    }
    String lookup = keyLookup.get( input );
    if ( lookup != null ) {
      return lookup;
    }
    return input;
  }

  public void addKeyLookup( String from, String to ) {
    keyLookup.put( from, to );
  }
}
