/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

/**
 * Additional methods to be added to Repository in next major revision.
 *
 */
@Deprecated
public interface RepositoryExtended extends Repository {
  /**
   * Loads the RepositoryDirectoryTree either Eagerly or Lazilly based on the value passed. This value will
   * override the default and any specified setting value for KETTLE_LAZY_REPOSITORY.
   *
   * @param eager
   * @return
   * @throws KettleException
   */
  RepositoryDirectoryInterface loadRepositoryDirectoryTree( boolean eager ) throws KettleException;

  /**
   * Move / rename a repository directory
   *
   * @param dirId
   *          The ObjectId of the repository directory to move
   * @param newParent
   *          The RepositoryDirectoryInterface that will be the new parent of the repository directory (May be null if a
   *          move is not desired)
   * @param newName
   *          The new name of the repository directory (May be null if a rename is not desired)
   * @param renameHomeDirectories
   *          true if this is an allowed action
   * @return The ObjectId of the repository directory that was moved
   * @throws KettleException
   */
  ObjectId renameRepositoryDirectory( final ObjectId dirId, final RepositoryDirectoryInterface newParent,
                                             final String newName, final boolean renameHomeDirectories ) throws KettleException;


  /**
   * Delete a repository directory
   *
   * @param dir
   *          The ObjectId of the repository directory to move
   * @param deleteHomeDirectories
   *          true if this is an allowed action
   * @throws KettleException
   */
  void deleteRepositoryDirectory( final RepositoryDirectoryInterface dir, final boolean deleteHomeDirectories )
          throws KettleException;
}
