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

package org.pentaho.di.repository.keyvalue;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

public interface RepositoryKeyValueInterface {

  public static final String NAMESPACE_VARIABLES = "Variables";
  public static final String NAMESPACE_DIMENSIONS = "Dimensions";

  /**
   * Store a value in the repository using the key/value interface
   *
   * @param namespace
   *          the name-space to reference
   * @param key
   *          The key to use
   * @param value
   *          The value to store
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public void putValue( String namespace, String key, String value ) throws KettleException;

  /**
   * Remove a value from the repository key/value store
   *
   * @param namespace
   *          the name-space to reference
   * @param key
   *          The key of the value to remove
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public void removeValue( String namespace, String key ) throws KettleException;

  /**
   * Load a value from the repository
   *
   * @param namespace
   *          The name-space to use
   * @param key
   *          The key to look up
   * @param revision
   *          The revision to use or null if you want the last revision (optionally supported)
   * @return The value including name, description, ...
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public String getValue( String namespace, String key, String revision ) throws KettleException;

  /**
   * @return The list of name-spaces in the repository
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public List<String> listNamespaces() throws KettleException;

  /**
   * List the keys for a given name-space in the repository
   *
   * @param namespace
   *          The name-space to query
   * @return The list of keys in the name-space
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public List<String> listKeys( String namespace ) throws KettleException;

  /**
   * This method lists the key/value entries for a given name-space. Even though this method returns a
   * {@link RepositoryValueInterface} it does NOT (need to) load the actual object mentioned in it.
   *
   * @param namespace
   *          The name-space to query
   *
   * @return A list of value entries, unsorted.
   * @throws KettleException
   *           in case there is an unexpected repository error
   */
  public List<RepositoryValueInterface> listValues( String namespace ) throws KettleException;
}
