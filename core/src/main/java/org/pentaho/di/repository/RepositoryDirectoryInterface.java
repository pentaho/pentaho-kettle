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

package org.pentaho.di.repository;

import java.util.List;

/**
 * This interface represents Directories within the Repository API
 *
 */
public interface RepositoryDirectoryInterface extends RepositoryObjectInterface {

  /**
   * Returns a list of child directories
   *
   * @return list of child directories
   */
  public List<RepositoryDirectoryInterface> getChildren();

  /**
   * sets the list of child directories
   *
   * @param children
   *          list of child directories
   */
  public void setChildren( List<RepositoryDirectoryInterface> children );

  /**
   * Returns a list of child files
   *
   * @return list of child files
   */
  public List<RepositoryElementMetaInterface> getRepositoryObjects();

  /**
   * sets the list of child files
   *
   * @param children
   *          list of child files
   */
  public void setRepositoryObjects( List<RepositoryElementMetaInterface> children );

  /**
   * If true, this directory should be shown in UIs. Not necessarily persisted. Each repository implementation decides
   * whether to mark each directory as visible.
   */
  public boolean isVisible();

  public String[] getPathArray();

  /**
   * Find a directory using the path to the directory with file.separator between the dir-names.
   *
   * @param path
   *          The path to the directory
   * @return The directory if one was found, null if nothing was found.
   */
  public RepositoryDirectoryInterface findDirectory( String path );

  /**
   * Find the sub-directory with a certain ID
   *
   * @param id_directory
   *          the directory ID to look for.
   * @return The RepositoryDirectory if the ID was found, null if nothing could be found.
   */
  public RepositoryDirectoryInterface findDirectory( ObjectId id_directory );

  /**
   * Find the directory by following the path of strings
   *
   * @param path
   *          The path to the directory we're looking for.
   * @return The directory if one can be found, null if no directory was found.
   */
  public RepositoryDirectoryInterface findDirectory( String[] path );

  public ObjectId[] getDirectoryIDs();

  /**
   * Describe the complete path to ( and including) this directory, separated by the
   * RepositoryDirectory.DIRECTORY_SEPARATOR property (slash).
   *
   * @return The complete path to this directory.
   */
  public String getPath();

  /**
   * Counts the number of subdirectories in this directory.
   *
   * @return The number of subdirectories
   */
  public int getNrSubdirectories();

  /**
   * Get a subdirectory on a certain position.
   *
   * @param i
   *          The subdirectory position
   * @return The subdirectory with on a certain position
   */
  public RepositoryDirectory getSubdirectory( int i );

  /**
   * Check whether or not this is the root of the directory trees. (default)
   *
   * @return true if this is the root directory node. False if it is not.
   */
  public boolean isRoot();

  /**
   * Find the root of the directory tree starting from this directory.
   *
   * @return the root of the directory tree
   */
  public RepositoryDirectoryInterface findRoot();

  public void clear();

  /**
   * Add a subdirectory to this directory.
   *
   * @param subdir
   *          The subdirectory to add.
   */
  public void addSubdirectory( RepositoryDirectoryInterface subdir );

  /**
   * Change the parent of this directory. (move directory)
   *
   * @param parent
   *          The new parent of this directory.
   */
  public void setParent( RepositoryDirectoryInterface parent );

  /**
   * get the parent directory for this directory.
   *
   * @return The parent directory of null if this is the root directory.
   */
  public RepositoryDirectoryInterface getParent();

  /**
   * Set the database ID for this object in the repository.
   *
   * @param id
   *          the database ID for this object in the repository.
   */
  public void setObjectId( ObjectId id );

  /**
   * Set the directory name (rename)
   *
   * @param directoryname
   *          The new directory name
   */
  public void setName( String directoryname );

  public String getPathObjectCombination( String transName );

  public RepositoryDirectoryInterface findChild( String name );

}
