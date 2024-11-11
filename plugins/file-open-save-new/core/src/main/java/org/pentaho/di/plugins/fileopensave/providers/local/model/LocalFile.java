/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.plugins.fileopensave.providers.local.model;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

/**
 * Created by bmorrise on 2/16/19.
 */
public class LocalFile extends BaseEntity implements File {
  public LocalFile() {
  }

  @Override public String getProvider() {
    return LocalFileProvider.TYPE;
  }

  public static LocalFile create( String parent, Path path ) {
    LocalFile localFile = new LocalFile();
    String filename = null;
    if ( path != null && path.getFileName() != null && !Utils.isEmpty( path.getFileName().toString() ) ) {
      filename = path.getFileName().toString();
    }

    if ( !Utils.isEmpty( filename ) ) {
      if ( filename.endsWith( KTR ) ) {
        localFile.setType( TRANSFORMATION );
      } else if ( filename.endsWith( KJB ) ) {
        localFile.setType( JOB );
      } else {
        localFile.setType( TYPE );
      }
    }
    localFile.setName( filename );
    if ( path != null ) {
      localFile.setPath( path.toString() );
    }

    localFile.setParent( parent );
    try {
      localFile.setDate( new Date( Files.getLastModifiedTime( path ).toMillis() ) );
    } catch ( IOException e ) {
      localFile.setDate( new Date() );
    }
    localFile.setRoot( LocalFileProvider.NAME );
    localFile.setCanEdit( true );
    return localFile;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash( getProvider(), getPath() );
  }
  
  @Override
  public boolean equals( Object obj ) {
    if ( obj == this ) {
      return true;
    }

    if ( !( obj instanceof LocalFile ) ) {
      return false;
    }

    LocalFile compare = (LocalFile) obj;
    // This comparison depends on `getProvider()` to always return a hardcoded value
    return compare.getProvider().equals( getProvider() ) && Objects.equals( getPath(), compare.getPath() );
  }

  public EntityType getEntityType(){
    return EntityType.LOCAL_FILE;
  }
}
