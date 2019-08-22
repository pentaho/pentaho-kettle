/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;

import java.util.Objects;

/**
 * Created by bmorrise on 5/16/17.
 */
public class RepositoryFile extends RepositoryObject implements File {

  public static final String TRANSFORMATION = "transformation";
  public static final String JOB = "job";
  public static final String KTR = ".ktr";
  public static final String KJB = ".kjb";
  public static final String DELIMITER = "/";
  private String username;

  public RepositoryFile() {
    // Necessary for JSON marshalling
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public static RepositoryFile build( org.pentaho.di.repository.RepositoryObject repositoryObject ) {
    RepositoryFile repositoryFile = new RepositoryFile();
    repositoryFile.setObjectId( repositoryObject.getObjectId().getId() );
    repositoryFile.setName( repositoryObject.getName() );
    repositoryFile.setType( repositoryObject.getObjectType().getTypeDescription() );
    repositoryFile.setExtension( repositoryObject.getObjectType().getExtension() );
    repositoryFile.setDate( repositoryObject.getModifiedDate() );
    repositoryFile.setParent( repositoryObject.getRepositoryDirectory().getPath() );
    String rootPath = repositoryObject.getRepositoryDirectory().getPath();
    rootPath = rootPath.equals( DELIMITER ) ? rootPath : rootPath + DELIMITER;
    String filename = repositoryObject.getName();
    if ( !filename.endsWith( repositoryFile.getExtension() ) ) {
      filename += repositoryFile.getExtension();
    }
    repositoryFile.setPath( rootPath + filename );
    repositoryFile.setRoot( RepositoryFileProvider.NAME );
    repositoryFile.setCanEdit( true );

    return repositoryFile;
  }

  public static RepositoryFile build( String parentPath,
                                      org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile,
                                      Boolean isAdmin ) {
    RepositoryFile repositoryFile1 = new RepositoryFile();
    repositoryFile1.setObjectId( (String) repositoryFile.getId() );
    repositoryFile1.setName( stripExtension( repositoryFile.getName() ) );
    repositoryFile1.setType( getType( repositoryFile.getName() ) );
    repositoryFile1.setExtension( "" );
    repositoryFile1.setDate( repositoryFile.getLastModifiedDate() );
    repositoryFile1.setParent( parentPath );
    repositoryFile1.setPath( repositoryFile.getPath() );
    repositoryFile1.setHidden( repositoryFile.isHidden() && !isAdmin );
    repositoryFile1.setRoot( RepositoryFileProvider.NAME );
    repositoryFile1.setCanEdit( true );

    return repositoryFile1;
  }

  public static String stripExtension( String filename ) {
    if ( filename.indexOf( '.' ) != -1 ) {
      return filename.substring( 0, filename.lastIndexOf( '.' ) );
    }
    return filename;
  }

  public static String getType( String filename ) {
    if ( filename.endsWith( KTR ) ) {
      return TRANSFORMATION;
    }
    if ( filename.endsWith( KJB ) ) {
      return JOB;
    }
    return "";
  }

  @Override
  public int hashCode() {
    return Objects.hash( getProvider(), getPath() );
  }

  @Override public boolean equals( Object obj ) {
    // If the object is compared with itself then return true
    if ( obj == this ) {
      return true;
    }

    if ( !( obj instanceof RepositoryFile ) ) {
      return false;
    }

    RepositoryFile compare = (RepositoryFile) obj;
    return compare.getProvider().equals( getProvider() )
      && ( ( compare.getPath() == null && getPath() == null ) || compare.getPath().equals( getPath() ) );
  }
}
