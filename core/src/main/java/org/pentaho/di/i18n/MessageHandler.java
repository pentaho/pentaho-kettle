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
