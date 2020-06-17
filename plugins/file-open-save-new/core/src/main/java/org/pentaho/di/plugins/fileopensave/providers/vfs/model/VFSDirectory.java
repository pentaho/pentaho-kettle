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
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSDirectory extends VFSFile implements Directory {

  public static final String DIRECTORY = "folder";

  private boolean hasChildren;
  private boolean canAddChildren;
  private List<VFSFile> children = new ArrayList<>();

  @Override public String getType() {
    return DIRECTORY;
  }

  public boolean hasChildren() {
    return hasChildren;
  }

  public void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  public List<VFSFile> getChildren() {
    return children;
  }

  public void setChildren( List<VFSFile> children ) {
    this.children = children;
  }

  public void addChild( VFSFile file ) {
    this.children.add( file );
  }

  public boolean isHasChildren() {
    return hasChildren;
  }

  @Override public boolean isCanAddChildren() {
    return canAddChildren;
  }

  public void setCanAddChildren( boolean canAddChildren ) {
    this.canAddChildren = canAddChildren;
  }

  public static VFSDirectory create( String parent, FileObject fileObject, String connection, String domain ) {
    VFSDirectory vfsDirectory = new VFSDirectory();
    vfsDirectory.setName( fileObject.getName().getBaseName() );
    vfsDirectory.setPath( fileObject.getName().getURI() );
    vfsDirectory.setParent( parent );
    if ( connection != null ) {
      vfsDirectory.setConnection( connection );
      vfsDirectory.setRoot( VFSFileProvider.NAME );
    }
    vfsDirectory.setDomain( domain != null ? domain : "" );
    vfsDirectory.setCanEdit( true );
    vfsDirectory.setHasChildren( true );
    vfsDirectory.setCanAddChildren( true );
    try {
      vfsDirectory.setDate( new Date( fileObject.getContent().getLastModifiedTime() ) );
    } catch ( FileSystemException e ) {
      vfsDirectory.setDate( new Date() );
    }
    return vfsDirectory;
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

    VFSDirectory compare = (VFSDirectory) obj;
    return compare.getProvider().equals( getProvider() )
      && ( ( compare.getConnection() == null && getConnection() == null ) || compare.getConnection()
      .equals( getConnection() ) )
      && ( ( compare.getPath() == null && getPath() == null ) || compare.getPath().equals( getPath() ) );
  }
}
