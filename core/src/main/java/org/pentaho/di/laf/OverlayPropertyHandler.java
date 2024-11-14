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
