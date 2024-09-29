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


package org.pentaho.di.i18n;

import org.pentaho.di.laf.Handler;

/**
 * Standard Message handler that takes a root package, plus key and resolves that into one/more resultant messages. This
 * Handler is used by all message types to enable flexible look and feel as well as i18n to be implemented in variable
 * ways.
 *
 * @author dhushon
 *
 */
public interface MessageHandler extends Handler {

  /**
   * get a key from the default (System global) bundle
   *
   * @param key
   * @return
   */
  public String getString( String key );

  /**
   * get a key from the defined package bundle, by key
   *
   * @param packageName
   * @param key
   * @return
   */
  public String getString( String packageName, String key );

  /**
   * get a key from the defined package bundle, by key
   *
   * @param packageName
   * @param key
   * @param parameters
   * @return
   */
  public String getString( String packageName, String key, String... parameters );

  /**
   * Get a string from the defined package bundle, by key and by a resource class
   *
   * @param packageName
   * @param key
   * @param resourceClass
   * @param parameters
   * @return
   */
  public String getString( String packageName, String key, Class<?> resourceClass, String... parameters );
}
