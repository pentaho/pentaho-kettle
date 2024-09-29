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
