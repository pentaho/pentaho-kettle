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

package org.pentaho.di.core.ssh.mina;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.pentaho.di.core.ssh.exceptions.SftpException;
import org.pentaho.di.core.ssh.SftpFile;
import org.pentaho.di.core.ssh.SftpSession;

public class MinaSftpSession implements SftpSession {
  private final SftpClient client;

  public MinaSftpSession( SftpClient client ) {
    this.client = client;
  }

  @Override
  public List<SftpFile> list( String path ) throws SftpException {
    try {
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
    } catch ( IOException e ) {
      throw new SftpException( "Failed to list directory: " + path, e );
    }
  }

  @Override
  public boolean exists( String path ) throws SftpException {
    try {
      client.stat( path );
      return true;
    } catch ( IOException e ) {
      return false;
    }
  }

  @Override
  public boolean isDirectory( String path ) throws SftpException {
    try {
      return client.stat( path ).isDirectory();
    } catch ( IOException e ) {
      throw new SftpException( "Failed to check if path is directory: " + path, e );
    }
  }

  @Override
  public long size( String path ) throws SftpException {
    try {
      return client.stat( path ).getSize();
    } catch ( IOException e ) {
      throw new SftpException( "Failed to get file size: " + path, e );
    }
  }

  @Override
  public void download( String remote, OutputStream target ) throws SftpException {
    try {
      try ( InputStream in = client.read( remote ) ) {
        in.transferTo( target );
      }
    } catch ( IOException e ) {
      throw new SftpException( "Failed to download file: " + remote, e );
    }
  }

  @Override
  public void upload( InputStream source, String remote, boolean overwrite ) throws SftpException {
    try {
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
    } catch ( IOException e ) {
      throw new SftpException( "Failed to upload file: " + remote, e );
    }
  }

  @Override
  public void mkdir( String path ) throws SftpException {
    try {
      client.mkdir( path );
    } catch ( IOException e ) {
      throw new SftpException( "Failed to create directory: " + path, e );
    }
  }

  @Override
  public void delete( String path ) throws SftpException {
    try {
      if ( isDirectory( path ) ) {
        client.rmdir( path );
      } else {
        client.remove( path );
      }
    } catch ( IOException e ) {
      throw new SftpException( "Failed to delete: " + path, e );
    }
  }

  @Override
  public void rename( String oldPath, String newPath ) throws SftpException {
    try {
      client.rename( oldPath, newPath );
    } catch ( IOException e ) {
      throw new SftpException( "Failed to rename from " + oldPath + " to " + newPath, e );
    }
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
