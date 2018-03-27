/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.repo.model;

/**
 * Created by bmorrise on 5/16/17.
 */
public class RepositoryFile extends RepositoryObject {

  private String username;

  @Override public String getType() {
    return this.type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public static RepositoryFile build( org.pentaho.di.repository.RepositoryObject repositoryObject ) {
    RepositoryFile repositoryFile = new RepositoryFile();
    repositoryFile.setObjectId( repositoryObject.getObjectId() );
    repositoryFile.setName( repositoryObject.getName() );
    repositoryFile.setType( repositoryObject.getObjectType().getTypeDescription() );
    repositoryFile.setExtension( repositoryObject.getObjectType().getExtension() );
    repositoryFile.setDate( repositoryObject.getModifiedDate() );
    repositoryFile.setObjectId( repositoryObject.getObjectId() );
    repositoryFile.setPath( repositoryObject.getRepositoryDirectory().getPath() );

    return repositoryFile;
  }
}
