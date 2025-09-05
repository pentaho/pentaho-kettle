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

package org.pentaho.di.core.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface SftpSession extends AutoCloseable {
  List<SftpFile> list( String path ) throws IOException;

  boolean exists( String path ) throws IOException;

  boolean isDirectory( String path ) throws IOException;

  long size( String path ) throws IOException;

  void download( String remote, OutputStream target ) throws IOException;

  void upload( InputStream source, String remote, boolean overwrite ) throws IOException;

  void mkdir( String path ) throws IOException;

  void delete( String path ) throws IOException;

  void rename( String oldPath, String newPath ) throws IOException;

  @Override
  void close();
}
