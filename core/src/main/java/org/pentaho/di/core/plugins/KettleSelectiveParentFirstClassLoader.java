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


package org.pentaho.di.core.plugins;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KettleSelectiveParentFirstClassLoader extends KettleURLClassLoader {
  private List<Pattern> patterns = new ArrayList<>();

  public KettleSelectiveParentFirstClassLoader( URL[] url, ClassLoader classLoader, String[] patterns ) {
    super( url, classLoader );
    addPatterns( patterns );
  }

  public KettleSelectiveParentFirstClassLoader( URL[] url, ClassLoader classLoader, String name, String[] patterns ) {
    super( url, classLoader, name );
    addPatterns( patterns );
  }

  public void addPatterns( String[] patterns ) {
    if ( patterns != null ) {
      this.patterns.addAll( Arrays.stream( patterns )
        .map( Pattern::compile )
        .collect( Collectors.toList() )
      );
    }
  }

  private Class<?> loadClassParentFirst( String arg0, boolean arg1 ) throws ClassNotFoundException {
    try {
      return loadClassFromParent( arg0, arg1 );
    } catch ( ClassNotFoundException | NoClassDefFoundError e ) {
      // ignore
    }

    return loadClassFromThisLoader( arg0, arg1 );
  }

  @Override
  protected synchronized Class<?> loadClass( String arg0, boolean arg1 ) throws ClassNotFoundException {
    for ( Pattern pattern : patterns ) {
      if ( pattern.matcher( arg0 ).matches() ) {
        return loadClassParentFirst( arg0, arg1 );
      }
    }
    return super.loadClass( arg0, arg1 );
  }
}
