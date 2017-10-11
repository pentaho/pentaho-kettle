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

public interface PropertyHandler extends Handler {
  /**
   * load properties for the given properties file
   *
   * @param filename
   * @return true if load was successful
   */
  public boolean loadProps( String filename );

  /**
   * check to see whether a property file exists within the classpath or filesystem
   *
   * @param filename
   * @return true if resource exists
   */
  public boolean exists( String filename );

  /**
   * return the value of a given key from the properties list
   *
   * @param key
   * @return null if the key is not found
   */
  public String getProperty( String key );

  /**
   * return the value of a given key from the properties list, returning the defValue string should the key not be found
   *
   * @param key
   * @param defValue
   * @return a string representing either the value associated with the passed key or defValue should that key not be
   *         found
   */
  public String getProperty( String key, String defValue );
}
