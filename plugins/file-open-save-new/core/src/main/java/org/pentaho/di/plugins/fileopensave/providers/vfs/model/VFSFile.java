/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.vfs.model;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import java.util.Date;
import java.util.Objects;

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSFile extends BaseEntity implements File {
  public static String TYPE = "file";

  private String connection;

  public VFSFile() {
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public String getProvider() {
    return VFSFileProvider.TYPE;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection( String connection ) {
    this.connection = connection;
  }

  public String getConnectionPath() {
    if ( getPath() == null || connection == null ) {
      return null;
    }
    return ConnectionFileProvider.SCHEME + "://" + connection + "/" + getPath().replaceAll( "[\\w]+://", "" );
  }

  public static VFSFile create( String parent, FileObject fileObject, String connection ) {
    VFSFile vfsFile = new VFSFile();
    vfsFile.setName( fileObject.getName().getBaseName() );
    vfsFile.setPath( fileObject.getName().getURI() );
    vfsFile.setParent( parent );
    if ( connection != null ) {
      vfsFile.setConnection( connection );
      vfsFile.setRoot( VFSFileProvider.NAME );
    }
    vfsFile.setCanEdit( true );
    try {
      vfsFile.setDate( new Date( fileObject.getContent().getLastModifiedTime() ) );
    } catch ( FileSystemException ignored ) {
      vfsFile.setDate( new Date() );
    }
    return vfsFile;
  }

  @Override
  public int hashCode() {
    return Objects.hash( getProvider(), getConnection(), getPath() );
  }

  @Override public boolean equals( Object obj ) {
    // If the object is compared with itself then return true
    if ( obj == this ) {
      return true;
    }

    if ( !( obj instanceof VFSFile ) ) {
      return false;
    }

    VFSFile compare = (VFSFile) obj;
    return compare.getProvider().equals( getProvider() )
      && ( ( compare.getConnection() == null && getConnection() == null ) || compare.getConnection()
      .equals( getConnection() ) )
      && ( ( compare.getPath() == null && getPath() == null ) || compare.getPath().equals( getPath() ) );
  }
}
