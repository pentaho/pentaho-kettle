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

/**
 * This is a static accessor for the dynamic property loader and should be used by all classes requiring access to
 * property files. The static accessor provides a notification from the LAFFactory when the concrete handler is changed
 * at runtime should the LAF be changed.
 *
 * @author dhushon
 *
 */
public class BasePropertyHandler implements LAFChangeListener<PropertyHandler> {

  static BasePropertyHandler instance = null;
  protected PropertyHandler handler = null;
  Class<PropertyHandler> clazz = PropertyHandler.class;

  static {
    getInstance();
  }

  private BasePropertyHandler() {
    init();
  }

  private void init() {
    // counting on LAFFactory to return a class conforming to @see MessageHandler
    handler = LAFFactory.getHandler( clazz );
  }

  public static BasePropertyHandler getInstance() {
    if ( instance == null ) {
      instance = new BasePropertyHandler();
    }
    return instance;
  }

  protected PropertyHandler getHandler() {
    return handler;
  }

  protected static PropertyHandler getInstanceHandler() {
    return getInstance().getHandler();
  }

  /**
   * return the value of a given key from the properties list
   *
   * @param key
   * @return null if the key is not found
   */
  public static String getProperty( String key ) {
    return getInstanceHandler().getProperty( key );
  }

  /**
   * return the value of a given key from the properties list, returning the defValue string should the key not be found
   *
   * @param key
   * @param defValue
   * @return a string representing either the value associated with the passed key or defValue should that key not be
   *         found
   */
  public static String getProperty( String key, String defValue ) {
    return getInstanceHandler().getProperty( key, defValue );
  }

  @Override
  public void notify( PropertyHandler changedObject ) {
    handler = changedObject;
  }

}
