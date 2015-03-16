package org.pentaho.di.core.compress.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionOutputStream extends CompressionOutputStream {

  public ZIPCompressionOutputStream( OutputStream out, CompressionProvider provider ) {
    super( getDelegate( out ), provider );
  }

  protected static ZipOutputStream getDelegate( OutputStream out ) {
    ZipOutputStream delegate;
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
  public void addEntry( String filename, String extension ) throws IOException {
    // remove folder hierarchy
    int index = filename.lastIndexOf( Const.FILE_SEPARATOR );
    String entryPath;
    if ( index != -1 ) {
      entryPath = filename.substring( index + 1 );
    } else {
      entryPath = filename;
    }

    // remove ZIP extension
    index = entryPath.toLowerCase().lastIndexOf( ".zip" );
    if ( index != -1 ) {
      entryPath = entryPath.substring( 0, index ) + entryPath.substring( index + ".zip".length() );
    }

    // add real extension if needed
    if ( !Const.isEmpty( extension ) ) {
      entryPath += "." + extension;
    }

    ZipEntry zipentry = new ZipEntry( entryPath );
    zipentry.setComment( "Compressed by Kettle" );
    ( (ZipOutputStream) delegate ).putNextEntry( zipentry );
  }
}
