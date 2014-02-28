package org.pentaho.di.core.compress.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionOutputStream extends CompressionOutputStream {

  public ZIPCompressionOutputStream( OutputStream out, CompressionProvider provider ) {
    super( getDelegate( out ), provider );
  }

  protected static ZipOutputStream getDelegate( OutputStream out ) {
    ZipOutputStream delegate = null;
    if ( out instanceof ZipOutputStream ) {
      delegate = (ZipOutputStream) out;
    } else {

      delegate = new ZipOutputStream( out );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    ZipOutputStream zos = (ZipOutputStream) delegate;
    zos.flush();
    zos.closeEntry();
    zos.finish();
    zos.close();
  }

  @Override
  public void addEntry( Object entry ) throws IOException {
    ZipEntry zipentry = new ZipEntry( entry.toString() );
    zipentry.setComment( "Compressed by Kettle" );
    ( (ZipOutputStream) delegate ).putNextEntry( zipentry );
  }
}
