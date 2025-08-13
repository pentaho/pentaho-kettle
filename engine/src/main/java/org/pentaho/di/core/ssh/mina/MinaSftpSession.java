package org.pentaho.di.core.ssh.mina;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.pentaho.di.core.ssh.SftpFile;
import org.pentaho.di.core.ssh.SftpSession;

public class MinaSftpSession implements SftpSession {
  private final SftpClient client;

  public MinaSftpSession( SftpClient client ) {
    this.client = client;
  }

  @Override
  public List<SftpFile> list( String path ) throws IOException {
    List<SftpFile> out = new ArrayList<>();
    for ( SftpClient.DirEntry e : client.readDir( path ) ) {
      Attributes a = e.getAttributes();
      Instant mtime = Instant.EPOCH;
      if ( a.getModifyTime() != null ) {
        // getModifyTime returns a FileTime; convert via toMillis()
        mtime = Instant.ofEpochMilli( a.getModifyTime().toMillis() );
      }
      out.add( new SftpFile( e.getFilename(), a.isDirectory(), a.getSize(), mtime ) );
    }
    return out;
  }

  @Override
  public boolean exists( String path ) {
    try {
      client.stat( path );
      return true;
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  public boolean isDirectory( String path ) throws IOException {
    return client.stat( path ).isDirectory();
  }

  @Override
  public long size( String path ) throws IOException {
    return client.stat( path ).getSize();
  }

  @Override
  public void download( String remote, OutputStream target ) throws IOException {
    try ( InputStream in = client.read( remote ) ) {
      in.transferTo( target );
    }
  }

  @Override
  public void upload( InputStream source, String remote, boolean overwrite ) throws IOException {
    try ( SftpClient.CloseableHandle h = client.open( remote, SftpClient.OpenMode.Write, SftpClient.OpenMode.Create,
      SftpClient.OpenMode.Truncate ) ) {
      byte[] buf = new byte[ 8192 ];
      int r;
      long off = 0;
      while ( ( r = source.read( buf ) ) >= 0 ) {
        if ( r == 0 ) {
          continue;
        }
        client.write( h, off, buf, 0, r );
        off += r;
      }
    }
  }

  @Override
  public void mkdir( String path ) throws IOException {
    client.mkdir( path );
  }

  @Override
  public void delete( String path ) throws IOException {
    if ( isDirectory( path ) ) {
      client.rmdir( path );
    } else {
      client.remove( path );
    }
  }

  @Override
  public void rename( String oldPath, String newPath ) throws IOException {
    client.rename( oldPath, newPath );
  }

  @Override
  public void close() {
    try {
      client.close();
    } catch ( IOException ignored ) {
      // Ignore
    }
  }
}
