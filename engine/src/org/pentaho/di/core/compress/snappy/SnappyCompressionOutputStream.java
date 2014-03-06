package org.pentaho.di.core.compress.snappy;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.xerial.snappy.SnappyOutputStream;

public class SnappyCompressionOutputStream extends CompressionOutputStream {

  public SnappyCompressionOutputStream( OutputStream out, CompressionProvider provider ) throws IOException {
    super( getDelegate( out ), provider );
  }

  private static SnappyOutputStream getDelegate( OutputStream out ) throws IOException {
    SnappyOutputStream delegate;
    if ( out instanceof SnappyOutputStream ) {
      delegate = (SnappyOutputStream) out;
    } else {
      delegate = new SnappyOutputStream( out );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    SnappyOutputStream zos = (SnappyOutputStream) delegate;
    zos.flush();
    zos.close();
  }

  @Override
  public void write( int b ) throws IOException {
    delegate.write( b );
  }
}
