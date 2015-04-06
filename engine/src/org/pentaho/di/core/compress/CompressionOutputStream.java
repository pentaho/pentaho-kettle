package org.pentaho.di.core.compress;

import java.io.IOException;
import java.io.OutputStream;

public abstract class CompressionOutputStream extends OutputStream {

  private CompressionProvider compressionProvider;
  protected OutputStream delegate;

  public CompressionOutputStream( OutputStream out, CompressionProvider provider ) {
    this();
    delegate = out;
    compressionProvider = provider;
  }

  private CompressionOutputStream() {
    super();
  }

  public CompressionProvider getCompressionProvider() {
    return compressionProvider;
  }

  public void addEntry( String filename, String extension ) throws IOException {
    // Default no-op behavior
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public void write( int b ) throws IOException {
    delegate.write( b );
  }

  @Override
  public void write( byte[] b ) throws IOException {
    delegate.write( b );
  }

  @Override
  public void write( byte[] b, int off, int len ) throws IOException {
    delegate.write( b, off, len );
  }
}
