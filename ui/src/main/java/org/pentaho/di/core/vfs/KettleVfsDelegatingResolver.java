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

package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.vfs.ui.VfsResolver;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleVfsDelegatingResolver implements VfsResolver {

  @Override
  public FileObject resolveFile( String vfsUrl ) {
    try {
      return KettleVFS.getFileObject( vfsUrl );
    } catch ( KettleFileException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public FileObject resolveFile( String vfsUrl, FileSystemOptions fsOptions ) {
    try {
      return KettleVFS.getFileObject( vfsUrl, fsOptions );
    } catch ( KettleFileException e ) {
      throw new RuntimeException( e );
    }
  }
}
