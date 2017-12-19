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
import java.util.Iterator;
import java.util.LinkedList;

import org.pentaho.di.core.Const;

public class OverlayPropertyHandler implements PropertyHandler {
  protected static final String propFile = "ui/laf.properties";

  private static PropertyHandler instance = null;

  private LinkedList<OverlayProperties> propList = new LinkedList<OverlayProperties>();

  public OverlayPropertyHandler() {
    initProps();
  }

  public static PropertyHandler getInstance() {
    if ( instance == null ) {
      instance = new OverlayPropertyHandler();
    }
    return instance;
  }

  protected boolean loadAltProps() {
    // check the -D switch... something like -Dorg.pentaho.di.laf.alt="somefile.properties"
    String altFile = Const.getEnvironmentVariable( "org.pentaho.di.laf.alt", null );
    if ( altFile != null ) {
      return loadProps( altFile );
    }
    return false;
  }

  private boolean initProps() {
    boolean flag = loadProps( propFile );
    return ( loadAltProps() || flag );
  }

  @Override
  public String getProperty( String key ) {
    // System.out.println(properties.getProperty(key));
    // go through linked list from front to back to find the property
    String s = null;
    Iterator<OverlayProperties> i = propList.iterator();
    while ( i.hasNext() ) {
      s = i.next().getProperty( key );
      if ( s != null ) {
        return s;
      }
    }
    return s;
  }

  public static String getLAFProp( String key ) {
    return getInstance().getProperty( key );
  }

  @Override
  public boolean loadProps( String filename ) {
    try {
      OverlayProperties ph = new OverlayProperties( filename );
      propList.addFirst( ph );
      return true;
    } catch ( IOException e ) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public String getProperty( String key, String defValue ) {
    String s = getProperty( key );
    if ( s != null ) {
      return s;
    }
    return defValue;
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

  @Override
  public boolean exists( String filename ) {
    try {
      return ( getURL( filename ) != null );
    } catch ( MalformedURLException e ) {
      return false;
    }
  }

}
