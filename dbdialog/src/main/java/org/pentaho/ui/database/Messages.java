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

package org.pentaho.ui.database;

import org.pentaho.di.i18n.GlobalMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
  private static final Logger log = LoggerFactory.getLogger( Messages.class );

  private static final String BUNDLE_NAME = "org.pentaho.ui.database.databasedialog";

  private static ResourceBundle RESOURCE_BUNDLE = GlobalMessageUtil.getBundle( BUNDLE_NAME, Messages.class );

  private Messages() {
  }

  public static ResourceBundle getBundle() {
    if ( RESOURCE_BUNDLE == null ) {
      RESOURCE_BUNDLE = GlobalMessageUtil.getBundle( BUNDLE_NAME, Messages.class );
    }

    return RESOURCE_BUNDLE;
  }

  public static String getString( String key ) {
    try {
      return getBundle().getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1 ) {
    try {
      Object[] args = { param1 };
      return MessageFormat.format( getString( key ), args );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1, String param2 ) {
    try {
      Object[] args = { param1, param2 };
      return MessageFormat.format( getString( key ), args );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1, String param2, String param3 ) {
    try {
      Object[] args = { param1, param2, param3 };
      return MessageFormat.format( getString( key ), args );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1, String param2, String param3, String param4 ) {
    try {
      Object[] args = { param1, param2, param3, param4 };
      return MessageFormat.format( getString( key ), args );
    } catch ( Exception e ) {
      return '!' + key + '!';
    }
  }

}
