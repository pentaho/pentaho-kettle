/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;

public interface EngineMetaInterface extends RepositoryElementInterface {

  /**
   * Sets the filename.
   *
   * @param filename
   */
  void setFilename( String filename );

  /**
   * Gets the name.
   *
   * @return name
   */
  @Override String getName();

  /**
   * Builds a name for this. If no name is yet set, create the name from the filename.
   */
  void nameFromFilename();

  /**
   * Clears the changed flag of this.
   */
  void clearChanged();

  /**
   * Gets the XML representation.
   *
   * @return the XML representation of this transformation
   * @throws KettleException
   *           if any errors occur during generation of the XML
   * @see org.pentaho.di.core.xml.XMLInterface#getXML()
   */

  String getXML() throws KettleException;

  /**
   * Gets the file type.
   *
   * @return the file type
   */
  String getFileType();

  /**
   * Gets the filter names.
   */
  String[] getFilterNames();

  /**
   * Gets the filter extensions.
   *
   * @return the filter extensions
   */
  String[] getFilterExtensions();

  /**
   * Gets the default extension.
   *
   * @return default extension
   */
  String getDefaultExtension();

  /**
   * Set the database ID for this in the repository.
   *
   * @param id
   *          the database ID for this in the repository
   */
  @Override void setObjectId( ObjectId id );

  /**
   * Gets the date the transformation was created.
   *
   * @return the date the transformation was created
   */
  Date getCreatedDate();

  /**
   * Sets the date the transformation was created.
   *
   * @param date
   *          The creation date to set
   */
  void setCreatedDate( Date date );

  /**
   * Returns whether or not the this can be saved.
   *
   * @return
   */
  boolean canSave();

  /**
   * Gets the user by whom this was created.
   *
   * @return the user by whom this was created
   */
  String getCreatedUser();

  /**
   * Sets the user by whom this was created.
   *
   * @param createduser
   *          The user to set
   */
  void setCreatedUser( String createduser );

  /**
   * Gets the date this was modified.
   *
   * @return the date this was modified
   */
  Date getModifiedDate();

  /**
   * Sets the date this was modified.
   *
   * @param date
   *          The modified date to set
   */
  void setModifiedDate( Date date );

  /**
   * Sets the user who last modified this.
   *
   * @param user
   *          The user name to set
   */
  void setModifiedUser( String user );

  /**
   * Gets the user who last modified this.
   *
   * @return the user who last modified this
   */
  String getModifiedUser();

  /**
   * Gets the repository element type.
   *
   * @return the repository element type
   */
  @Override RepositoryDirectoryInterface getRepositoryDirectory();

  /**
   * Get the filename (if any).
   *
   * @return the filename
   */
  String getFilename();

  /**
   * Saves shared objects, including databases, steps, partition schemas, slave servers, and cluster schemas, to a file.
   *
   */
  void saveSharedObjects() throws KettleException;

  /**
   * Sets the internal kettle variables.
   */
  void setInternalKettleVariables();

  /**
   * Set versioning enabled
   *
   * @param versioningEnabled
   *          is versioning enabled
   */
  default void setVersioningEnabled( Boolean versioningEnabled ) {
    // Default implementation does nothing
  }

  /**
   * Is versioning enabled.
   *
   * @return is versioning enabled
   */
  default Boolean getVersioningEnabled() {
    return null;
  }

}
