/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import java.util.Date;
import java.util.Objects;

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSFile extends BaseEntity implements File {

  public static final String TYPE = "file";
  public static final String DOMAIN_ROOT = "[\\w]+:///?";
  public static final String PROTOCOL_SEPARATOR = "://";
  public static final String DELIMITER = FileName.SEPARATOR;

  private String connection;
  private String domain;


  @Override public String getProvider() {
    return VFSFileProvider.TYPE;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnection() {
    return connection;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public void setConnection( String connection ) {
    this.connection = connection;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated Use {@link #getPath()}.
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnectionPath() {
    return getPath();
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated Use {@link #getParent()}.
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnectionParentPath() {
    return  getParent();
  }


  /**
   * Builder method to create an instance of  {@link VFSFile}.
   * <p/>
   * NOTE: logic for {@link #setRoot(String)} and {@link #setConnection(String)} is only used in
   * {@link org.pentaho.di.plugins.fileopensave.dragdrop.ElementDragListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)} for the
   * scenario of "Recent Repository File".
   * @param parent
   * @param fileObject
   * @param connection
   * @param domain
   * @return new instance with some populated values.
   */
  public static VFSFile create( String parent, FileObject fileObject, String connection, String domain ) {
    VFSFile vfsFile = new VFSFile();
    String filename = null;
    if ( fileObject != null && fileObject.getName() != null ) {
      filename = fileObject.getName().getBaseName();
    }

    if ( !Utils.isEmpty( filename ) ) {
      if ( filename.endsWith( KTR ) ) {
        vfsFile.setType( TRANSFORMATION );
      } else if ( filename.endsWith( KJB ) ) {
        vfsFile.setType(JOB);
      }
    }
    vfsFile.setName( filename );
    if ( fileObject != null && fileObject.getName() != null ) {
      vfsFile.setPath( fileObject.getName().getURI() );
    }
    vfsFile.setParent( parent );
    if ( connection != null ) {
      vfsFile.setConnection( connection );
      vfsFile.setRoot( VFSFileProvider.NAME );
    }
    vfsFile.setDomain( domain != null ? domain : "" );
    vfsFile.setCanEdit( true );
    try {
      if ( fileObject != null && fileObject.getContent() != null ) {
        vfsFile.setDate( new Date( fileObject.getContent().getLastModifiedTime() ) );
      }
    } catch ( Exception ignored ) {
      vfsFile.setDate( new Date() );
    }
    return vfsFile;
  }

  /**
   * Separate VFS domain variable is no longer needed.
   * @deprecated
   * The domain handled is internally with the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getDomain() {
    return domain;
  }

  /**
   * Separate VFS domain variable is no longer needed.
   * @deprecated
   * The domain handled is internally with the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public void setDomain( String domain ) {
    this.domain = domain;
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

    // This comparison depends on `getProvider()` to always return a hardcoded value
    return compare.getProvider().equals( getProvider() )
      && StringUtils.equals( compare.getConnection(), getConnection() )
      && StringUtils.equals( compare.getPath(), getPath() );
  }

  public EntityType getEntityType(){
    return EntityType.VFS_FILE;
  }

  @Override
  public String getNameDecoded() {
    try {
      return UriParser.decode( getName() );
    } catch ( FileSystemException e ) {
      // Fallback to original name
      return getName();
    }
  }
}
