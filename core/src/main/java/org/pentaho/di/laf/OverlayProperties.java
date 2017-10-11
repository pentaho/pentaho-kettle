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
