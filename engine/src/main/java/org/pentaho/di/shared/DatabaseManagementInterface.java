/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.shared;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

import java.util.List;

/**
 * This is the management interface used by the UI to perform CRUD operation. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public interface DatabaseManagementInterface {

  /**
   * Add the database connection to global or project specific file store(shared.xml) depending on the bowl
   * @param databaseMeta
   * @throws KettleException
   */
  void addDatabase( DatabaseMeta databaseMeta ) throws KettleException;

  /**
   * Get the list of databases connection based  on the current bowl
   * @return List<DatabaseMeta> Returns the list of DatabaseMeta
   * @throws KettleException
   */
  List<DatabaseMeta> getDatabases() throws KettleException;

  /**
   * Get a single database by name.
   *
   *
   * @param name name of the database
   *
   * @return DatabaseMeta database instance
   * @throws KettleException
   */
  DatabaseMeta getDatabase( String name ) throws KettleException;


  /**
   * Remove the provided database
   *
   *
   * @param databaseMeta database to remove
   */
  void removeDatabase( DatabaseMeta databaseMeta ) throws KettleException;

  /**
   * Remove the provided database
   *
   *
   * @param databasename name of the database to remove
   */
  void removeDatabase( String databaseName ) throws KettleException;

  /**
   * Removes all connections
   *
   */
  void clear() throws KettleException;


}
