package org.pentaho.di.core.compress.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionInputStream extends CompressionInputStream {

  private ZipInputStream delegate;

  public ZIPCompressionInputStream( InputStream in, CompressionProvider provider ) throws IOException {
    super( getDelegate( in ), provider );
  }

  protected static ZipInputStream getDelegate( InputStream in ) throws IOException {
    ZipInputStream delegate = null;
    if ( in instanceof ZipInputStream ) {
      delegate = (ZipInputStream) in;
    } else {
      delegate = new ZipInputStream( in );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    ZipInputStream zis = (ZipInputStream)delegate;
    zis.close();
  }

  @Override
  public int read() throws IOException {
    ZipInputStream zis = (ZipInputStream)delegate;
    return zis.read();
  }

  @Override
  public Object nextEntry() throws IOException {
    ZipInputStream zis = (ZipInputStream)delegate;
    return zis.getNextEntry();
  }

}
