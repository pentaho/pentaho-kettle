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


package org.pentaho.di.laf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class OverlayProperties extends Properties implements PropertyHandler {

  private static final long serialVersionUID = 1L;
  private String name = null;

  public OverlayProperties( String file ) throws IOException {
    load( file );
  }

  @Override
  public boolean exists( String filename ) {
    try {
      return ( getURL( filename ) != null );
    } catch ( MalformedURLException e ) {
      return false;
    }
  }

  @Override
  public boolean loadProps( String filename ) {
    try {
      return load( filename );
    } catch ( IOException e ) {
      return false;
    }
  }

  private URL getURL( String filename ) throws MalformedURLException {
    URL url;
    File file = new File( filename );
    if ( file.exists() ) {
      url = file.toURI().toURL();
    } else {
      ClassLoader classLoader = getClass().getClassLoader();
      url = classLoader.getResource( filename );
    }
    return url;
  }

  /**
   * cleanse and reload the property file
   *
   * @param filename
   * @return
   * @throws IOException
   */
  public boolean load( String filename ) throws IOException {
    URL url = getURL( filename );
    if ( url == null ) {
      return false;
    }
    clear();
    load( url.openStream() );
    return true;
  }

  public String getName() {
    return name;
  }
}
