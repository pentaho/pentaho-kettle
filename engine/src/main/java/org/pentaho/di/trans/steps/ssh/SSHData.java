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

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SSHData extends BaseStepData {

  private static final Class<?> PKG = SSHData.class; // for i18n purposes

  private SshConnection sshConnection;
  private boolean connected;

  public int indexOfCommand;
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
    this.sshConnection = null;
    this.connected = false;
    this.wroteOneRow = false;
    this.commands = null;
    this.stdOutField = null;
    this.stdTypeField = null;
  }

  /**
   * Gets the SSH connection.
   * 
   * @return the SSH connection, or null if not set
   */
  public SshConnection getSshConnection() {
    return sshConnection;
  }

  /**
   * Sets the SSH connection.
   * 
   * @param sshConnection the SSH connection to set
   */
  public void setSshConnection( SshConnection sshConnection ) {
    this.sshConnection = sshConnection;
  }

  /**
   * Checks if the SSH connection is established.
   * 
   * @return true if connected, false otherwise
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Sets the connection state.
   * 
   * @param connected true if connected, false otherwise
   */
  public void setConnected( boolean connected ) {
    this.connected = connected;
  }

  /**
   * Connection method using our SSH abstraction layer.
   * Provides modern SSH implementation with Ed25519 support and algorithm flexibility.
   * Supports all the parameters from the original implementation including proxy settings.
   */
  public static SshConnection openSshConnection( SshConnectionParameters params ) throws KettleException {

    SshConnection connection = null;
    Path tempKeyFile = null;

    try {
      // Build basic SSH configuration
      SshConfig config = SshConfig.create()
          .host( params.getServer() )
          .port( params.getPort() )
          .username( params.getUsername() )
          .connectTimeoutMillis( params.getTimeOut() > 0 ? params.getTimeOut() * 1000 : 30000 ); // Default 30 seconds

      // Configure authentication
      tempKeyFile = configureAuthentication( config, params.getBowl(), params.isUseKey(),
          params.getKeyFilename(), params.getPassPhrase(), params.getPassword(), params.getSpace() );

      // Configure proxy if specified
      configureProxy( config, params.getProxyhost(), params.getProxyport(),
          params.getProxyusername(), params.getProxypassword() );

      // Create and connect
      connection = SshConnectionFactory.defaultFactory().open( config );
      connection.connect();

      return connection;

    } catch ( Exception e ) {
      // Something wrong happened - clean up and re-throw
      if ( connection != null ) {
        connection.close();
      }
      throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.ErrorConnecting", params.getServer(), params.getUsername() ), e );
    } finally {
      // Clean up temporary key file
      cleanupTempKeyFile( tempKeyFile );
    }
  }

  /**
   * @deprecated Use {@link #openSshConnection(SshConnectionParameters)} instead.
   * This method is maintained for backward compatibility.
   */
  @Deprecated
  public static SshConnection openSshConnection(
      Bowl bowl, String server, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut,
      VariableSpace space, String proxyhost, int proxyport,
      String proxyusername, String proxypassword ) throws KettleException {

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( bowl )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( useKey )
        .keyFilename( keyFilename )
        .passPhrase( passPhrase )
        .timeOut( timeOut )
        .space( space )
        .proxyhost( proxyhost )
        .proxyport( proxyport )
        .proxyusername( proxyusername )
        .proxypassword( proxypassword )
        .build();

    return openSshConnection( params );
  }

  /**
   * Configures authentication for the SSH connection.
   * 
   * @return the temporary key file path if created, null otherwise
   */
  private static Path configureAuthentication( SshConfig config, Bowl bowl, boolean useKey,
      String keyFilename, String passPhrase, String password, VariableSpace space ) throws KettleException {

    if ( useKey ) {
      return configureKeyAuthentication( config, bowl, keyFilename, passPhrase, space );
    } else {
      config.authType( SshConfig.AuthType.PASSWORD ).password( password );
      return null;
    }
  }

  /**
   * Configures key-based authentication and creates temporary key file.
   * 
   * @return the temporary key file path
   */
  private static Path configureKeyAuthentication( SshConfig config, Bowl bowl,
      String keyFilename, String passPhrase, VariableSpace space ) throws KettleException {

    if ( Utils.isEmpty( keyFilename ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.PrivateKeyFileMissing" ) );
    }

    try {
      FileObject keyFileObject = KettleVFS.getInstance( bowl ).getFileObject( keyFilename );
      if ( !keyFileObject.exists() ) {
        throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.PrivateKeyNotExist", keyFilename ) );
      }

      // Read key file content and write to temporary file for SSH library
      FileContent keyFileContent = keyFileObject.getContent();
      byte[] keyBytes;
      try ( InputStream in = keyFileContent.getInputStream() ) {
        // Read all available bytes from the stream
        keyBytes = in.readAllBytes();
        if ( keyBytes.length == 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.ProcessingKeyFile", keyFilename ) );
        }
      }

      // Create temporary key file
      Path tempKeyFile = Files.createTempFile( "ssh_key_", ".pem" );
      Files.write( tempKeyFile, keyBytes );

      config.authType( SshConfig.AuthType.PUBLIC_KEY ).keyPath( tempKeyFile );

      if ( !Utils.isEmpty( passPhrase ) ) {
        config.passphrase( space.environmentSubstitute( passPhrase ) );
      }

      return tempKeyFile;

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.ProcessingKeyFile", keyFilename ), e );
    }
  }

  /**
   * Configures proxy settings for the SSH connection.
   */
  private static void configureProxy( SshConfig config, String proxyhost, int proxyport,
      String proxyusername, String proxypassword ) {

    if ( !Utils.isEmpty( proxyhost ) ) {
      config.proxy( proxyhost, proxyport );
      if ( !Utils.isEmpty( proxyusername ) ) {
        config.proxyAuth( proxyusername, proxypassword );
      }
    }
  }

  /**
   * Cleans up the temporary key file.
   */
  private static void cleanupTempKeyFile( Path tempKeyFile ) {
    if ( tempKeyFile != null ) {
      try {
        Files.deleteIfExists( tempKeyFile );
      } catch ( IOException e ) {
        // Log but don't fail - cleanup is best effort
      }
    }
  }
}
