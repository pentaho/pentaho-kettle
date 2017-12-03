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
  public void setFilename( String filename );

  /**
   * Gets the name.
   *
   * @return name
   */
  public String getName();

  /**
   * Builds a name for this. If no name is yet set, create the name from the filename.
   */
  public void nameFromFilename();

  /**
   * Clears the changed flag of this.
   */
  public void clearChanged();

  /**
   * Gets the XML representation.
   *
   * @return the XML representation of this transformation
   * @throws KettleException
   *           if any errors occur during generation of the XML
   * @see org.pentaho.di.core.xml.XMLInterface#getXML()
   */

  public String getXML() throws KettleException;

  /**
   * Gets the file type.
   *
   * @return the file type
   */
  public String getFileType();

  /**
   * Gets the filter names.
   */
  public String[] getFilterNames();

  /**
   * Gets the filter extensions.
   *
   * @return the filter extensions
   */
  public String[] getFilterExtensions();

  /**
   * Gets the default extension.
   *
   * @return default extension
   */
  public String getDefaultExtension();

  /**
   * Set the database ID for this in the repository.
   *
   * @param id
   *          the database ID for this in the repository
   */
  public void setObjectId( ObjectId id );

  /**
   * Gets the date the transformation was created.
   *
   * @return the date the transformation was created
   */
  public Date getCreatedDate();

  /**
   * Sets the date the transformation was created.
   *
   * @param createdDate
   *          The creation date to set
   */
  public void setCreatedDate( Date date );

  /**
   * Returns whether or not the this can be saved.
   *
   * @return
   */
  public boolean canSave();

  /**
   * Gets the user by whom this was created.
   *
   * @return the user by whom this was created
   */
  public String getCreatedUser();

  /**
   * Sets the user by whom this was created.
   *
   * @param createdUser
   *          The user to set
   */
  public void setCreatedUser( String createduser );

  /**
   * Gets the date this was modified.
   *
   * @return the date this was modified
   */
  public Date getModifiedDate();

  /**
   * Sets the date this was modified.
   *
   * @param modifiedDate
   *          The modified date to set
   */
  public void setModifiedDate( Date date );

  /**
   * Sets the user who last modified this.
   *
   * @param modifiedUser
   *          The user name to set
   */
  public void setModifiedUser( String user );

  /**
   * Gets the user who last modified this.
   *
   * @return the user who last modified this
   */
  public String getModifiedUser();

  /**
   * Gets the repository element type.
   *
   * @return the repository element type
   */
  public RepositoryDirectoryInterface getRepositoryDirectory();

  /**
   * Get the filename (if any).
   *
   * @return the filename
   */
  public String getFilename();

  /**
   * Saves shared objects, including databases, steps, partition schemas, slave servers, and cluster schemas, to a file.
   *
   * @throws KettleException
   */
  public void saveSharedObjects() throws KettleException;

  /**
   * Sets the internal kettle variables.
   */
  public void setInternalKettleVariables();

  /**
   * Set versioning enabled
   *
   * @param versioningEnabled
   *          is versioning enabled
   */
  public default void setVersioningEnabled( Boolean versioningEnabled ) {
    // Default implementation does nothing
  }

  /**
   * Is versioning enabled.
   *
   * @return is versioning enabled
   */
  public default Boolean getVersioningEnabled() {
    return null;
  }

}
