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


package org.pentaho.di.trans.steps.ssh;

import java.io.CharArrayWriter;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.ssh.SshImplementation;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;

import com.google.common.annotations.VisibleForTesting;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SSHData extends BaseStepData {
  public int indexOfCommand;
  public Connection conn;  // Legacy Trilead connection
  public SshStepConnectionAdapter sshConn;  // New abstraction layer connection
  public boolean wroteOneRow;
  public String commands;
  public int nrInputFields;
  public int nrOutputFields;

  // Output fields
  public String stdOutField;
  public String stdTypeField;

  public RowMetaInterface outputRowMeta;

  public SSHData() {
    super();
    this.indexOfCommand = -1;
    this.conn = null;
    this.sshConn = null;
    this.wroteOneRow = false;
    this.commands = null;
    this.stdOutField = null;
    this.stdTypeField = null;
  }

  public static Connection OpenConnection( Bowl bowl, String serveur, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut, VariableSpace space, String proxyhost,
      int proxyport, String proxyusername, String proxypassword ) throws KettleException {
    Connection conn = null;
    char[] content = null;
    boolean isAuthenticated = false;
    try {
      // perform some checks
      if ( useKey ) {
        if ( Utils.isEmpty( keyFilename ) ) {
          throw new KettleException( BaseMessages.getString( SSHMeta.PKG, "SSH.Error.PrivateKeyFileMissing" ) );
        }
        FileObject keyFileObject = KettleVFS.getInstance( bowl ).getFileObject( keyFilename );

        if ( !keyFileObject.exists() ) {
          throw new KettleException( BaseMessages.getString( SSHMeta.PKG, "SSH.Error.PrivateKeyNotExist", keyFilename ) );
        }

        FileContent keyFileContent = keyFileObject.getContent();

        CharArrayWriter charArrayWriter = new CharArrayWriter( (int) keyFileContent.getSize() );

        try ( InputStream in = keyFileContent.getInputStream() ) {
          IOUtils.copy( in, charArrayWriter );
        }

        content = charArrayWriter.toCharArray();
      }
      // Create a new connection
      conn = createConnection( serveur, port );

      /* We want to connect through a HTTP proxy */
      if ( !Utils.isEmpty( proxyhost ) ) {
        /* Now connect */
        // if the proxy requires basic authentication:
        if ( !Utils.isEmpty( proxyusername ) ) {
          conn.setProxyData( new HTTPProxyData( proxyhost, proxyport, proxyusername, proxypassword ) );
        } else {
          conn.setProxyData( new HTTPProxyData( proxyhost, proxyport ) );
        }
      }

      // and connect
      if ( timeOut == 0 ) {
        conn.connect();
      } else {
        conn.connect( null, 0, timeOut * 1000 );
      }
      // authenticate
      if ( useKey ) {
        isAuthenticated =
          conn.authenticateWithPublicKey( username, content, space.environmentSubstitute( passPhrase ) );
      } else {
        isAuthenticated = conn.authenticateWithPassword( username, password );
      }
      if ( !isAuthenticated ) {
        throw new KettleException( BaseMessages.getString( SSHMeta.PKG, "SSH.Error.AuthenticationFailed", username ) );
      }
    } catch ( Exception e ) {
      // Something wrong happened
      // do not forget to disconnect if connected
      if ( conn != null ) {
        conn.close();
      }
      throw new KettleException( BaseMessages.getString( SSHMeta.PKG, "SSH.Error.ErrorConnecting", serveur, username ), e );
    }
    return conn;
  }

  @VisibleForTesting
   static Connection createConnection( String serveur, int port ) {
    return new Connection( serveur, port );
  }

  /**
   * New connection method using our SSH abstraction layer.
   * Provides modern SSH implementation with Ed25519 support and algorithm flexibility.
   * Falls back to legacy Trilead if the modern implementation fails.
   * 
   * @param preferredImplementation SSH implementation to prefer (null for auto-detect)
   * @param log Optional log channel for warnings and debug info
   */
  public static SshStepConnectionAdapter OpenSshConnection(
      Bowl bowl, String server, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut, 
      VariableSpace space, String proxyhost, int proxyport, 
      String proxyusername, String proxypassword, 
      SshImplementation preferredImplementation, LogChannelInterface log ) throws KettleException {
    
    try {
      // Build SSH configuration
      SshConfig config = SshConfig.create()
          .host( server )
          .port( port )
          .username( username )
          .connectTimeoutMillis( timeOut > 0 ? timeOut * 1000 : 30000 ); // Default 30 seconds

      if ( useKey && !Utils.isEmpty( keyFilename ) ) {
        config.authType( SshConfig.AuthType.PUBLIC_KEY )
              .keyPath( java.nio.file.Paths.get( space.environmentSubstitute( keyFilename ) ) );
        if ( !Utils.isEmpty( passPhrase ) ) {
          config.passphrase( space.environmentSubstitute( passPhrase ) );
        }
      } else {
        config.authType( SshConfig.AuthType.PASSWORD )
              .password( password );
      }
      
      // Set preferred implementation or let factory decide
      if ( preferredImplementation != null ) {
        config.implementation( preferredImplementation );
      }

      if ( log != null && log.isDebug() ) {
        log.logDebug( "Creating SSH connection using modern abstraction layer to " + server + ":" + port );
      }

      SshConnection connection = SshConnectionFactory.defaultFactory().open( config );
      return new SshStepConnectionAdapter( connection );

    } catch ( Exception e ) {
      // Fall back to legacy Trilead connection for backward compatibility
      if ( log != null ) {
        log.logMinimal( "Failed to create modern SSH connection, falling back to legacy Trilead: " + e.getMessage() );
      }

      Connection triLeadConn = OpenConnection( bowl, server, port, username, password,
                                            useKey, keyFilename, passPhrase, timeOut, space,
                                            proxyhost, proxyport, proxyusername, proxypassword );
      return new SshStepConnectionAdapter( new TrileadConnectionWrapper( triLeadConn ) );
    }
  }

  /**
   * Convenience method that uses auto-detection for SSH implementation.
   */
  public static SshStepConnectionAdapter OpenSshConnection(
      Bowl bowl, String server, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut, 
      VariableSpace space, String proxyhost, int proxyport, 
      String proxyusername, String proxypassword, LogChannelInterface log ) throws KettleException {

    return OpenSshConnection( bowl, server, port, username, password, useKey, keyFilename,
                           passPhrase, timeOut, space, proxyhost, proxyport, proxyusername,
                           proxypassword, null, log );
  }

  /**
   * Wrapper to make legacy Trilead Connection work with our SshConnection interface.
   * This enables backward compatibility when the modern implementation fails.
   */
  private static class TrileadConnectionWrapper implements SshConnection {
    private final Connection triLeadConn;

    public TrileadConnectionWrapper( Connection conn ) {
      this.triLeadConn = conn;
    }
    
    @Override
    public void connect() throws Exception {
      // Connection is already established when passed to constructor
      // No additional connection setup needed for Trilead
    }
    
    @Override
    public ExecResult exec( String command, long timeoutMs ) throws Exception {
      com.trilead.ssh2.Session session = triLeadConn.openSession();
      try {
        session.execCommand( command );

        // Wait for command completion (similar to SessionResult logic)
        session.waitForCondition( com.trilead.ssh2.ChannelCondition.EXIT_STATUS, timeoutMs );

        // Read results using existing SessionResult logic
        SessionResult result = new SessionResult( session );
        Integer exitStatus = session.getExitStatus();
        
        return new ExecResult(
          result.getStdOut(),
          result.getStdErr(),
          exitStatus != null ? exitStatus : -1
        );
      } finally {
        session.close();
      }
    }
    
    @Override
    public SftpSession openSftp() throws Exception {
      throw new UnsupportedOperationException( "SFTP not supported in legacy Trilead wrapper" );
    }
    
    @Override
    public void close() {
      try {
        triLeadConn.close();
      } catch ( Exception e ) {
        // Log but don't throw - close() shouldn't throw exceptions
      }
    }
  }
}
