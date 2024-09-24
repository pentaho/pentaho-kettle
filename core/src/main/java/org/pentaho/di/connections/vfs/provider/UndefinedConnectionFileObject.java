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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;

/**
 * This class represents a connection file object whose connection does not exist.
 */
class UndefinedConnectionFileObject extends ConnectionFileObject {
  public UndefinedConnectionFileObject( @NonNull ConnectionFileName name, @NonNull ConnectionFileSystem fs ) {
    super( name, fs );
  }

  @Override
  @Nullable
  public FileObject getResolvedFileObject() {
    return null;
  }

  @Override
  @NonNull
  protected AbstractFileObject<?> requireResolvedFileObject() throws FileSystemException {
    throw new KettleVFSFileSystemException( "ConnectionFileObject.UndefinedConnection", getName().toString() );
  }

  @Override
  public FileType getType() throws FileSystemException {
    return FileType.IMAGINARY;
  }

  @Override
  public boolean isAttached() {
    return true;
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    return false;
  }

  @Override
  public boolean canRenameTo( FileObject newfile ) {
    return false;
  }
}
