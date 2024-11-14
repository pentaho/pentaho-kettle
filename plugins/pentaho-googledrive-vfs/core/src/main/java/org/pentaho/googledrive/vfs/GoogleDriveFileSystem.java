/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.googledrive.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class GoogleDriveFileSystem extends AbstractFileSystem implements FileSystem {

  public GoogleDriveFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new GoogleDriveFileObject( abstractFileName, this );
  }

  public void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( GoogleDriveFileProvider.capabilities );
  }

  protected void clearFileFromCache( FileName name ) {
    super.removeFileFromCache( name );
  }

  public FileObject resolveFile( FileName name ) throws FileSystemException {
    return this.processFile( name, true );
  }

  private synchronized FileObject processFile( FileName name, boolean useCache ) throws FileSystemException {
    if ( !super.getRootName().getRootURI().equals( name.getRootURI() ) ) {
      throw new FileSystemException( "vfs.provider/mismatched-fs-for-name.error",
          new Object[] { name, super.getRootName(), name.getRootURI() } );
    } else {
      FileObject file;
      if ( useCache ) {
        file = super.getFileFromCache( name );
      } else {
        file = null;
      }

      if ( file == null ) {
        try {
          file = this.createFile( (AbstractFileName) name );
        } catch ( Exception var5 ) {
          return null;
        }

        file = super.decorateFileObject( file );
        if ( useCache ) {
          super.putFileToCache( file );
        }
      }

      if ( super.getFileSystemManager().getCacheStrategy().equals( CacheStrategy.ON_RESOLVE ) ) {
        file.refresh();
      }

      return file;
    }
  }

}
