/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.repository.model;

import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;

/**
 * Created by bmorrise on 5/16/17.
 */
// TODO: Add something to keep from moving to here
public class RepositoryDirectory extends RepositoryFile implements Directory {

  public static final String DIRECTORY = "folder";

  private boolean canAddChildren;

  public RepositoryDirectory() {
    this.setHasChildren( true );
  }

  @Override public String getType() {
    return DIRECTORY;
  }

  public static RepositoryDirectory build( String parentPath,
                                           RepositoryDirectoryInterface repositoryDirectoryInterface ) {
    RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
    repositoryDirectory.setParent( parentPath );
    repositoryDirectory.setName( repositoryDirectoryInterface.getName() );
    repositoryDirectory.setPath( parentPath == null ? repositoryDirectoryInterface.getPath() : parentPath + "/" + repositoryDirectoryInterface.getName() );
    repositoryDirectory.setObjectId( repositoryDirectoryInterface.getObjectId().getId() );
    repositoryDirectory.setHidden( !repositoryDirectoryInterface.isVisible() );
    repositoryDirectory.setRoot( RepositoryFileProvider.NAME );
    repositoryDirectory.setCanAddChildren( true );
    repositoryDirectory.setCanEdit( true );

    return repositoryDirectory;
  }

  public static RepositoryDirectory build( String parentPath,
                                           org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile,
                                           Boolean isAdmin ) {
    RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
    repositoryDirectory.setParent( parentPath );
    repositoryDirectory.setName( repositoryFile.getName() );
    repositoryDirectory.setPath( repositoryFile.getPath() );
    repositoryDirectory.setObjectId( (String) repositoryFile.getId() );
    repositoryDirectory.setHidden( repositoryFile.isHidden() && !isAdmin );
    repositoryDirectory.setDate( repositoryFile.getLastModifiedDate() != null ? repositoryFile.getLastModifiedDate()
      : repositoryFile.getCreatedDate() );
    repositoryDirectory.setRoot( RepositoryFileProvider.NAME );
    repositoryDirectory.setCanAddChildren( true );
    repositoryDirectory.setCanEdit( true );

    return repositoryDirectory;
  }

  /**
   * Used to create a new RepositoryDirectory object from exisiting parent without actually creating the directory
   * @param parentPath
   * @param name
   * @param path
   * @param id
   * @param isVisible
   * @return
   */
  public static RepositoryDirectory build( String parentPath, String name, String id, boolean isVisible ) {
    RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
    repositoryDirectory.setParent( parentPath );
    repositoryDirectory.setName( name );
    repositoryDirectory.setPath( parentPath + "/" + name );
    repositoryDirectory.setObjectId( id );
    repositoryDirectory.setHidden( isVisible );
    repositoryDirectory.setRoot( RepositoryFileProvider.NAME );
    repositoryDirectory.setCanAddChildren( true );
    repositoryDirectory.setCanEdit( true );

    return repositoryDirectory;
  }

  @Override public boolean isCanAddChildren() {
    return canAddChildren;
  }

  public void setCanAddChildren( boolean canAddChildren ) {
    this.canAddChildren = canAddChildren;
  }

  @Override
  public EntityType getEntityType(){
    return EntityType.REPOSITORY_DIRECTORY;
  }
}
