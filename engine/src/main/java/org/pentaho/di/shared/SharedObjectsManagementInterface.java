/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import java.util.List;

/**
 * This is the management interface used by the UI to perform CRUD operation for all shared objects. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public interface SharedObjectsManagementInterface<T extends SharedObjectInterface> {

  /**
   * Add the SharedObject to global or project specific file store(shared.xml) depending on the bowl
   * @param sharedObjectInterface
   * @throws KettleException
   */
  void add( T sharedObjectInterface ) throws KettleException;

  /**
   * Get the list of SharedObjects based  on the current bowl
   * @return List<DatabaseMeta> Returns the list of DatabaseMeta
   * @throws KettleException
   */
  List<T> getAll() throws KettleException;

  /**
   * Get a single SharedObject by name.
   *
   * @param name name of the SharedObject
   * @return SharedObjectInterface SharedObject instance
   * @throws KettleException
   */
  T get( String name ) throws KettleException;


  /**
   * Remove the SharedObject
   * @param sharedObjectInterface SharedObject to remove
   * @throws KettleException
   */
  void remove( T sharedObjectInterface ) throws KettleException;

  /**
   * Remove the provided database
   * @param sharedObjectName name of the SharedObject to remove
   * @throws KettleException
   */
  void remove( String sharedObjectName ) throws KettleException;

  /**
   * Removes all sharedObjects for a type
   * @throws KettleException
   */
  void clear() throws KettleException;

}
