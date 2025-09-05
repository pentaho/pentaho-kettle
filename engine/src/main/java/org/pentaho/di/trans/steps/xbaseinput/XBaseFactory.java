package org.pentaho.di.trans.steps.xbaseinput;

import com.linuxense.javadbf.DBFException;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

public class XBaseFactory {
  public static XBase createXBase( LogChannelInterface log, FileObject fileObject, String fileCompression ) throws KettleException {
    try {
      InputStream is = KettleVFS.getInputStream( fileObject );
      return new XBase( log, fileObject.getName().getPath(), decompress( is, fileCompression ) );
    } catch ( DBFException e ) {
      throw new KettleException( "Error opening DBF metadata", e );
    } catch ( IOException e ) {
      throw new KettleException( "Error reading DBF file", e );
    }
  }

  public static XBase createXBase( LogChannelInterface log, String filePath, String fileCompression ) throws KettleException {
    try {
      InputStream is = new BufferedInputStream( new FileInputStream( filePath ) );
      return new XBase( log, filePath, decompress( is, fileCompression ) );
    } catch ( DBFException e ) {
      throw new KettleException( "Error opening DBF metadata", e );
    } catch ( IOException e ) {
      throw new KettleException( "Error reading DBF file", e );
    }
  }

  private static InputStream decompress( InputStream inputStream, String fileCompression ) throws IOException {
    if ( fileCompression == null ) {
      return inputStream;
    }

    CompressionProvider provider =
      CompressionProviderFactory.getInstance().getCompressionProviderByName( fileCompression );

    CompressionInputStream cis = provider.createInputStream( inputStream );
    cis.nextEntry();

    return new BlockingInputStream( new BufferedInputStream( cis ) );
  }

  // This class is needed because the javadbf library has a bug in which it tries to read an array of bytes, but
  // doesnt' check to ensure the desired amount of bytes is actually read
  // This class forces the read(byte[] buf, int pos, int len) to go through this class, which not only read available
  // bytes but tries to read as much bytes as needed
  static class BlockingInputStream extends InputStream {

    private final InputStream in;

    protected BlockingInputStream( InputStream in ) {
      this.in = in;
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }

    @Override
    public int read( @NotNull byte[] buf, int pos, int len ) throws IOException {
      int count = 0;
      while ( len > 0 ) {
        int n = in.read( buf, pos, len );
        pos += n;
        count += n;
        len -= n;
      }

      return count;
    }
  }

}
