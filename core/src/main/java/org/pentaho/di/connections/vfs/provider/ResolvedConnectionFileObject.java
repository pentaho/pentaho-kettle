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
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.util.Objects;

/**
 * This class represents a file object with an associated provider, resolved file object.
 */
class ResolvedConnectionFileObject extends ConnectionFileObject {
  @NonNull
  private final AbstractFileObject<?> resolvedFileObject;

  public ResolvedConnectionFileObject( @NonNull ConnectionFileName name,
                                       @NonNull ConnectionFileSystem fs,
                                       @NonNull AbstractFileObject<?> resolvedFileObject ) {
    super( name, fs );

    this.resolvedFileObject = Objects.requireNonNull( resolvedFileObject );
  }

  @NonNull
  @Override
  public FileObject getResolvedFileObject() {
    return resolvedFileObject;
  }

  @NonNull
  @Override
  protected AbstractFileObject<?> requireResolvedFileObject() {
    return resolvedFileObject;
  }

  @Override
  public FileType getType() throws FileSystemException {
    return resolvedFileObject.getType();
  }

  @Override
  public void holdObject( Object strongRef ) {
    resolvedFileObject.holdObject( strongRef );
  }

  @Override
  public boolean isAttached() {
    return resolvedFileObject.isAttached();
  }

  @Override
  public boolean isContentOpen() {
    return resolvedFileObject.isContentOpen();
  }

  @Override
  public boolean isExecutable() throws FileSystemException {
    return resolvedFileObject.isExecutable();
  }

  @Override
  public boolean isHidden() throws FileSystemException {
    return resolvedFileObject.isHidden();
  }

  @Override
  public boolean isReadable() throws FileSystemException {
    return resolvedFileObject.isReadable();
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    return resolvedFileObject.isWriteable();
  }

  @Override
  public boolean canRenameTo( FileObject newfile ) {
    return resolvedFileObject.canRenameTo( newfile );
  }

  @Override
  public String getAELSafeURIString() {
    return resolvedFileObject.getPublicURIString().replaceFirst( "s3://", "s3a://" );
  }
}
