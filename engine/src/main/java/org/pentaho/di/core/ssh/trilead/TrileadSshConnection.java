package org.pentaho.di.core.ssh.trilead;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpFile;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.Session;

public class TrileadSshConnection implements SshConnection {
  private final SshConfig config;
  private final LogChannelInterface log;
  private Connection conn;

  public TrileadSshConnection( SshConfig config ) {
    this.config = config;
    this.log = new LogChannel( "TrileadSshConnection" );
  }

  public TrileadSshConnection( SshConfig config, LogChannelInterface log ) {
    this.config = config;
    this.log = log != null ? log : new LogChannel( "TrileadSshConnection" );
  }

  @Override
  public void connect() throws Exception {
    if ( conn != null ) {
      return;
    }

    log.logBasic( "TrileadSshConnection: Connecting to " + config.getHost() + ":" + config.getPort() );
    conn = new Connection( config.getHost(), config.getPort() );
    // TODO: proxy, timeouts, known hosts handling (reuse existing logic later)
    conn.connect();

    log.logBasic( "TrileadSshConnection: Attempting authentication..." );
    boolean authed = false;
    if ( config.getPassword() != null ) {
      authed = conn.authenticateWithPassword( config.getUsername(), config.getPassword() );
    }
    // TODO: key auth
    if ( !authed ) {
      log.logError( "TrileadSshConnection: Authentication failed" );
      throw new IOException( "SSH authentication failed (Trilead)" );
    }

    log.logBasic( "TrileadSshConnection: Successfully connected and authenticated" );
  }


  @Override
  public ExecResult exec( String command, long timeoutMs ) throws Exception {
    Session session = conn.openSession();
    try {
      session.execCommand( command ); // Trilead API only takes the command string
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      pump( session.getStdout(), out );
      pump( session.getStderr(), err );
      Integer exit = session.getExitStatus();
      String sOut = out.toString( StandardCharsets.UTF_8 );
      String sErr = err.toString( StandardCharsets.UTF_8 );
      return new ExecResult( sOut, sErr, sOut + sErr, exit == null ? -1 : exit, exit != null && exit != 0 );
    } finally {
      session.close();
    }
  }

  private void pump( InputStream in, OutputStream out ) throws IOException {
    if ( in == null ) {
      return;
    }
    byte[] buf = new byte[ 8192 ];
    int r;
    while ( ( r = in.read( buf ) ) >= 0 ) {
      if ( r > 0 ) {
        out.write( buf, 0, r );
      }
    }
  }

  @Override
  public SftpSession openSftp() throws Exception {
    return new TrileadSftpSession( new SFTPv3Client( conn ) );
  }

  @Override
  public void close() {
    if ( conn != null ) {
      conn.close();
      conn = null;
    }
  }

  static class TrileadSftpSession implements SftpSession {
    private final SFTPv3Client client;

    TrileadSftpSession( SFTPv3Client client ) {
      this.client = client;
    }

    @Override
    public List<SftpFile> list( String path ) throws IOException {
      List<SftpFile> list = new ArrayList<>();
      for ( Object o : client.ls( path ) ) { // Trilead returns a raw collection
        SFTPv3DirectoryEntry e = (SFTPv3DirectoryEntry) o;
        boolean isDir = ( e.attributes != null ) && ( ( e.attributes.permissions & 040000 ) != 0 );
        long size = e.attributes == null ? 0 : e.attributes.size;
        long mtime = e.attributes == null ? 0 : e.attributes.mtime * 1000L;
        list.add( new SftpFile( e.filename, isDir, size, Instant.ofEpochMilli( mtime ) ) );
      }
      return list;
    }

    @Override
    public boolean exists( String path ) {
      try {
        client.stat( path );
        return true;
      } catch ( IOException ex ) {
        return false;
      }
    }

    @Override
    public boolean isDirectory( String path ) throws IOException {
      var s = client.stat( path );
      return ( s.permissions & 040000 ) != 0;
    }

    @Override
    public long size( String path ) throws IOException {
      return client.stat( path ).size;
    }

    @Override
    public void download( String remote, OutputStream target ) throws IOException {
      SFTPv3FileHandle h = client.openFileRO( remote );
      try {
        byte[] b = new byte[ 8192 ];
        long off = 0;
        int r;
        while ( ( r = client.read( h, off, b, 0, b.length ) ) > 0 ) {
          target.write( b, 0, r );
          off += r;
        }
      } finally {
        client.closeFile( h );
      }
    }

    @Override
    public void upload( InputStream source, String remote, boolean overwrite ) throws IOException {
      SFTPv3FileHandle h = client.createFileTruncate( remote );
      try {
        byte[] b = new byte[ 8192 ];
        long off = 0;
        int r;
        while ( ( r = source.read( b ) ) >= 0 ) {
          if ( r == 0 ) {
            continue;
          }
          client.write( h, off, b, 0, r );
          off += r;
        }
      } finally {
        client.closeFile( h );
      }
    }

    @Override
    public void mkdir( String path ) throws IOException {
      client.mkdir( path, 0755 );
    }

    @Override
    public void delete( String path ) throws IOException {
      if ( isDirectory( path ) ) {
        client.rmdir( path );
      } else {
        client.rm( path );
      }
    }

    @Override
    public void rename( String oldPath, String newPath ) throws IOException {
      client.mv( oldPath, newPath );
    }

    @Override
    public void close() {
      client.close();
    }
  }
}
