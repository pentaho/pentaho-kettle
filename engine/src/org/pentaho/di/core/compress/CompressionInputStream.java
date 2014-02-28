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
}
