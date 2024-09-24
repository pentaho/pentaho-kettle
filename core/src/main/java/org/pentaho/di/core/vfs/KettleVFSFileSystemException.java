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

import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.i18n.BaseMessages;

/**
 * An exception class that extends and is otherwise identical to Apache's {@link FileSystemException},
 * but which resolves message codes on the local {@code org.pentaho.di.core.vfs.messages.messages} bundle.
 * <p>
 * This exception class is useful in contexts where a {@code FileSystemException} is expected.
 */
public class KettleVFSFileSystemException extends FileSystemException {
  private static final Class<?> PKG = KettleVFSFileSystemException.class; // for i18n purposes, needed by Translator2!!

  public KettleVFSFileSystemException( String code ) {
    super( code );
  }

  public KettleVFSFileSystemException( String code, Object info0 ) {
    super( code, info0 );
  }

  public KettleVFSFileSystemException( String code, Object info0, Throwable throwable ) {
    super( code, info0, throwable );
  }

  public KettleVFSFileSystemException( String code, Object... info ) {
    super( code, info );
  }

  public KettleVFSFileSystemException( String code, Throwable throwable ) {
    super( code, throwable );
  }

  public KettleVFSFileSystemException( String code, Throwable throwable, Object... info ) {
    super( code, throwable, info );
  }

  public KettleVFSFileSystemException( Throwable throwable ) {
    super( throwable );
  }

  @Override
  public String getMessage() {
    return BaseMessages.getString( PKG, super.getCode(), (Object[]) getInfo() );
  }
}
