/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
