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

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;

public interface HasDatabasesInterface {

  /**
   * Get an ArrayList of defined DatabaseInfo objects.
   *
   * @return an ArrayList of defined DatabaseInfo objects.
   */
  public List<DatabaseMeta> getDatabases();

  /**
   * @param databases
   *          The databases to set.
   */
  public void setDatabases( List<DatabaseMeta> databases );

  /**
   * Add a database connection to the transformation.
   *
   * @param databaseMeta
   *          The database connection information.
   */
  public void addDatabase( DatabaseMeta databaseMeta );

  /**
   * Add a database connection to the transformation if that connection didn't exists yet. Otherwise, replace the
   * connection in the transformation
   *
   * @param databaseMeta
   *          The database connection information.
   */
  public void addOrReplaceDatabase( DatabaseMeta databaseMeta );

  /**
   * Add a database connection to the transformation on a certain location.
   *
   * @param p
   *          The location
   * @param ci
   *          The database connection information.
   */
  public void addDatabase( int p, DatabaseMeta ci );

  /**
   * Retrieves a database connection information a a certain location.
   *
   * @param i
   *          The database number.
   * @return The database connection information.
   */
  public DatabaseMeta getDatabase( int i );

  /**
   * Removes a database from the transformation on a certain location.
   *
   * @param i
   *          The location
   */
  public void removeDatabase( int i );

  /**
   * Count the nr of databases in the transformation.
   *
   * @return The nr of databases
   */
  public int nrDatabases();

  /**
   * Searches the list of databases for a database with a certain name
   *
   * @param name
   *          The name of the database connection
   * @return The database connection information or null if nothing was found.
   */
  public DatabaseMeta findDatabase( String name );

  /**
   * Find the location of database
   *
   * @param ci
   *          The database queried
   * @return The location of the database, -1 if nothing was found.
   */
  public int indexOfDatabase( DatabaseMeta ci );

  /**
   * Checks whether or not the connections have changed.
   *
   * @return True if the connections have been changed.
   */
  public boolean haveConnectionsChanged();
}
