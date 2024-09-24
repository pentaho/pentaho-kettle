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

package org.pentaho.di.core.compress;

import java.io.IOException;
import java.io.InputStream;

public abstract class CompressionInputStream extends InputStream {

  private CompressionProvider compressionProvider;
  protected InputStream delegate;

  public CompressionInputStream( InputStream in, CompressionProvider provider ) {
    this();
    delegate = in;
    compressionProvider = provider;
  }

  private CompressionInputStream() {
    super();
  }

  public CompressionProvider getCompressionProvider() {
    return compressionProvider;
  }

  public Object nextEntry() throws IOException {
    return null;
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read( byte[] b ) throws IOException {
    return delegate.read( b );
  }

  @Override
  public int read( byte[] b, int off, int len ) throws IOException {
    return delegate.read( b, off, len );
  }
}
