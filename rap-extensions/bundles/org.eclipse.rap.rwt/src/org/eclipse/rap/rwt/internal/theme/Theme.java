/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.theme.css.CssElementHolder;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceManager;


public class Theme {

  private static final String JS_THEME_PREFIX = "rwt.theme.";

  private final String id;
  private final String jsId;
  private final String name;
  private StyleSheetBuilder styleSheetBuilder;
  private CssValuesMap valuesMap;

  private String registeredLocation;

  private CssElement[] elements;

  public Theme( String id, String name, StyleSheet styleSheet ) {
    if( id == null ) {
      throw new NullPointerException( "id" );
    }
    this.id = id;
    this.name = name != null ? name : "Unnamed Theme";
    jsId = createUniqueJsId( id );
    valuesMap = null;
    styleSheetBuilder = new StyleSheetBuilder();
    if( styleSheet != null ) {
      styleSheetBuilder.addStyleSheet( styleSheet );
    }
  }

  public String getId() {
    return id;
  }

  public String getJsId() {
    return jsId;
  }

  public String getName() {
    return name;
  }

  public void addStyleSheet( StyleSheet styleSheet ) {
    if( valuesMap != null ) {
      throw new IllegalStateException( "Theme is already initialized" );
    }
    styleSheetBuilder.addStyleSheet( styleSheet );
  }

  public void initialize( ThemeableWidget[] themeableWidgets ) {
    elements = extractElements( themeableWidgets );
    if( valuesMap != null ) {
      throw new IllegalStateException( "Theme is already initialized" );
    }
    StyleSheet styleSheet = styleSheetBuilder.getStyleSheet();
    valuesMap = new CssValuesMap( styleSheet, themeableWidgets );
    styleSheetBuilder = null;
  }

  private static CssElement[] extractElements( ThemeableWidget[] themeableWidgets ) {
    CssElementHolder elements = new CssElementHolder();
    for( ThemeableWidget themeableWidget : themeableWidgets ) {
      if( themeableWidget.elements != null ) {
        for( CssElement element : themeableWidget.elements ) {
          elements.addElement( element );
        }
      }
    }
    return elements.getAllElements();
  }

  public StyleSheet getStyleSheet() {
    return styleSheetBuilder.getStyleSheet();
  }

  public CssValuesMap getValuesMap() {
    if( valuesMap == null ) {
      throw new IllegalStateException( "Theme is not initialized" );
    }
    return valuesMap;
  }

  public String getRegisteredLocation() {
    return registeredLocation;
  }

  public void registerResources( ApplicationContext applicationContext ) {
    try {
      registerThemeResources( applicationContext );
      registerThemeStoreFile( applicationContext );
    } catch( IOException ioe ) {
      throw new ThemeManagerException( "Failed to register theme resources for theme " + id, ioe );
    }
  }

  private void registerThemeResources( ApplicationContext applicationContext ) throws IOException {
    CssValue[] values = valuesMap.getAllValues();
    for( CssValue value : values ) {
      if( value instanceof ThemeResource ) {
        registerResource( applicationContext, ( ThemeResource )value );
      }
    }
  }

  private void registerThemeStoreFile( ApplicationContext applicationContext ) {
    ThemeStoreWriter storeWriter = new ThemeStoreWriter( applicationContext, this, elements );
    String name = "rap-" + jsId + ".json";
    String code = storeWriter.createJson();
    registeredLocation = registerResource( applicationContext, name, code );
  }

  private static void registerResource( ApplicationContext applicationContext, ThemeResource value )
    throws IOException
  {
    String registerPath = value.getResourcePath( applicationContext );
    if( registerPath != null ) {
      InputStream inputStream = value.getResourceAsStream();
      if( inputStream == null ) {
        throw new IllegalArgumentException( "Resource not found for theme property: " + value );
      }
      try {
        applicationContext.getResourceManager().register( registerPath, inputStream );
      } finally {
        inputStream.close();
      }
    }
  }

  private static String registerResource( ApplicationContext applicationContext,
                                          String name,
                                          String content )
  {
    byte[] buffer;
    try {
      buffer = content.getBytes( "UTF-8" );
    } catch( UnsupportedEncodingException shouldNotHappen ) {
      throw new RuntimeException( shouldNotHappen );
    }
    InputStream inputStream = new ByteArrayInputStream( buffer );
    ResourceManager resourceManager = applicationContext.getResourceManager();
    resourceManager.register( name, inputStream );
    return resourceManager.getLocation( name );
  }

  private static String createUniqueJsId( String id ) {
    if( RWT.DEFAULT_THEME_ID.equals( id ) ) {
      return JS_THEME_PREFIX + "Default";
    }
    if( ThemeManager.FALLBACK_THEME_ID.equals( id ) ) {
      return JS_THEME_PREFIX + "Fallback";
    }
    String hash = Integer.toHexString( id.hashCode() );
    return JS_THEME_PREFIX + "Custom_" + hash;
  }

}
