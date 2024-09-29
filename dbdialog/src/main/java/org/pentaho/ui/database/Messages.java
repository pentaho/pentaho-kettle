/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
