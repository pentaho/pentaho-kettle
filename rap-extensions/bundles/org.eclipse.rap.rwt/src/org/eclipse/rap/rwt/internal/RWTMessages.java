/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.service.ServletLog;

public final class RWTMessages {

  private static final String BUNDLE_NAME = "org.eclipse.rap.rwt.internal.RWTMessages";

  public static final String SERVER_ERROR = "ServerError";
  public static final String SERVER_ERROR_DESCRIPTION = "ServerErrorDescription";
  public static final String CONNECTION_ERROR = "ConnectionError";
  public static final String CONNECTION_ERROR_DESCRIPTION = "ConnectionErrorDescription";
  public static final String SESSION_TIMEOUT = "SessionTimeout";
  public static final String SESSION_TIMEOUT_DESCRIPTION = "SessionTimeoutDescription";
  public static final String CLIENT_ERROR = "ClientError";
  public static final String CLIENT_ERROR_DESCRIPTION = "ClientErrorDescription";
  public static final String RETRY = "Retry";
  public static final String RESTART = "Restart";
  public static final String DETAILS = "Details";
  public static final String NO_SCRIPT_WARNING = "NoScriptWarning";

  private RWTMessages() {
    // prevent instantiation
  }

  public static String getMessage( String key ) {
    return getMessage( key, BUNDLE_NAME, RWT.getLocale() );
  }

  public static String getMessage( String key, Locale locale ) {
    return getMessage( key, BUNDLE_NAME, locale );
  }

  public static String getMessage( String key, String bundleName ) {
    return getMessage( key, bundleName, RWT.getLocale() );
  }

  private static String getMessage( String key, String bundleName, Locale locale ) {
    String result = key;
    ResourceBundle bundle = null;
    try {
      bundle = getBundle( bundleName, locale );
    } catch( @SuppressWarnings( "unused" ) MissingResourceException ex ) {
      result = key + " (no resource bundle)";
    }
    if( bundle != null ) {
      try {
        result = bundle.getString( key );
      } catch( @SuppressWarnings( "unused" ) MissingResourceException mre ) {
      }
    }
    return result;
  }

  private static ResourceBundle getBundle( String baseName, Locale locale ) {
    ResourceBundle result = null;
    try {
      ClassLoader loader = RWTMessages.class.getClassLoader();
      result = ResourceBundle.getBundle( baseName, locale, loader );
    } catch( RuntimeException re ) {
      ServletLog.log( "Warning: could not retrieve resource bundle, loading system default", re );
      result = ResourceBundle.getBundle( baseName );
    }
    return result;
  }

}
