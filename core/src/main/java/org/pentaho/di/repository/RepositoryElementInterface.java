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

/**
 * A repository element is an object that can be saved or loaded from the repository. As such, we need to be able to
 * identify it. It needs a RepositoryDirectory, a name and an ID.
 *
 * We also need to identify the type of the element.
 *
 * Finally, we need to be able to optionally identify the revision of the element.
 *
 * @author matt
 *
 */
public interface RepositoryElementInterface extends RepositoryObjectInterface {

  public RepositoryDirectoryInterface getRepositoryDirectory();

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory );

  /**
   * Gets the name of the repository object.
   */
  @Override
  public String getName();

  /**
   * Sets the name of the repository object.
   *
   * @param name
   */
  public void setName( String name );

  /**
   * Gets the description of the repository object.
   *
   * @return
   */
  public String getDescription();

  /**
   * Sets the description of the repository object.
   *
   * @param description
   */
  public void setDescription( String description );

  /**
   * Gets the database ID in the repository for this object.
   *
   * @return the database ID in the repository for this object
   */
  @Override
  public ObjectId getObjectId();

  /**
   * Sets the database ID in the repository for this object.
   *
   * @return the database ID in the repository for this object
   */
  public void setObjectId( ObjectId id );

  /**
   * Gets the repository element type for this object.
   *
   * @return the repository element type for this object
   */
  public RepositoryObjectType getRepositoryElementType();

  /**
   * Gets the object revision.
   *
   * @return the object revision
   */
  public ObjectRevision getObjectRevision();

  /**
   * Sets the object revision.
   *
   * @param objectRevision
   */
  public void setObjectRevision( ObjectRevision objectRevision );

}
