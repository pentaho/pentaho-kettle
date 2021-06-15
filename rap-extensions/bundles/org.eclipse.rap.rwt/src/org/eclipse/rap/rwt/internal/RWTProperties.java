/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Innoopract Informationssysteme GmbH - initial API and implementation
 *   EclipseSource - ongoing implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal;


public final class RWTProperties {

  public static final String SERVICE_HANDLER_BASE_URL = "org.eclipse.rap.rwt.serviceHandlerBaseURL";
  public static final String DEVELOPMEMT_MODE = "org.eclipse.rap.rwt.developmentMode";
  public static final String TEXT_SIZE_STORE_SIZE = "org.eclipse.rap.rwt.textSizeStoreSize";

  /*
   * Used in conjunction with <code>WidgetUtil#CUSTOM_WIDGET_ID</code>,
   * to activate support for custom widget ids.</p>
   */
  public static final String ENABLE_UI_TESTS = "org.eclipse.rap.rwt.enableUITests";

  private RWTProperties() {
    // prevent instantiation
  }

  public static String getServiceHandlerBaseUrl() {
    return System.getProperty( SERVICE_HANDLER_BASE_URL );
  }

  public static boolean isDevelopmentMode() {
    return getBooleanProperty( DEVELOPMEMT_MODE, false );
  }

  public static int getTextSizeStoreSize( int defaultValue ) {
    return getIntProperty( TEXT_SIZE_STORE_SIZE, defaultValue );
  }

  public static boolean getBooleanProperty( String name, boolean defaultValue ) {
    String value = System.getProperty( name );
    return value == null ? defaultValue : value.equalsIgnoreCase( "true" );
  }

  public static int getIntProperty( String name, int defaultValue ) {
    String value = System.getProperty( name );
    try {
      return Integer.parseInt( value );
    } catch ( @SuppressWarnings( "unused" ) NumberFormatException ex ) {
      return defaultValue;
    }
  }

}
