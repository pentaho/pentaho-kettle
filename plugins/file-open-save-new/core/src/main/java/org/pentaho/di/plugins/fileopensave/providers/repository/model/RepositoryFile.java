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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.providers.recents.RecentFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.repository.ObjectId;

import java.util.Objects;

/**
 * Created by bmorrise on 5/16/17.
 */
public class RepositoryFile extends RepositoryObject implements File {


  public static final String DELIMITER = "/";
  private String username;

  public RepositoryFile() {
    // Necessary for JSON marshalling
  }

  public static RepositoryFile create( LastUsedFile lastUsedFile, final ObjectId objectId ) {
    RepositoryFile repositoryFile = new RepositoryFile();
    repositoryFile.setType( lastUsedFile.isTransformation() ? TRANSFORMATION : JOB );
    repositoryFile.setDate( lastUsedFile.getLastOpened() );
    repositoryFile.setRoot( RecentFileProvider.NAME );
    repositoryFile.setName( lastUsedFile.getFilename() );
    repositoryFile.setParent( lastUsedFile.getDirectory() );
    repositoryFile.setPath( lastUsedFile.getDirectory() + DELIMITER + lastUsedFile.getFilename() );
    repositoryFile.setRepository( lastUsedFile.getRepositoryName() );
    repositoryFile.setUsername( lastUsedFile.getUsername() );
    repositoryFile.setObjectId( objectId.getId() );

    return repositoryFile;
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
    // This comparison depends on `getProvider()` to always return a hardcoded value
    return compare.getProvider().equals( getProvider() )
      && StringUtils.equals( compare.getPath(), getPath() );
  }

  // TODO: fix repository files so that the extension is populated consistently in the two build methods
  // so that we don't need to use this method
  public boolean passesTypeFilter( String filter ) {
    return Utils.matches( getName() + ( TRANSFORMATION.equalsIgnoreCase( getType() ) ? KTR : KJB ), filter );
  }

  @Override
  public EntityType getEntityType(){
    return EntityType.REPOSITORY_FILE;
  }

}
