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

  private static final Class<?> PKG = SSHMeta.class; // for i18n purposes

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
  public static SshConnection openSshConnection(
      Bowl bowl, String server, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut,
      VariableSpace space, String proxyhost, int proxyport,
      String proxyusername, String proxypassword ) throws KettleException {

    SshConnection connection = null;
    Path tempKeyFile = null;

    try {
      // Build SSH configuration
      SshConfig config = SshConfig.create()
          .host( server )
          .port( port )
          .username( username )
          .connectTimeoutMillis( timeOut > 0 ? timeOut * 1000 : 30000 ); // Default 30 seconds

      // Configure authentication
      if ( useKey ) {
        if ( Utils.isEmpty( keyFilename ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.PrivateKeyFileMissing" ) );
        }

        FileObject keyFileObject = KettleVFS.getInstance( bowl ).getFileObject( keyFilename );
        if ( !keyFileObject.exists() ) {
          throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.PrivateKeyNotExist", keyFilename ) );
        }

        // Read key file content and write to temporary file for SSH library
        FileContent keyFileContent = keyFileObject.getContent();
        byte[] keyBytes = new byte[(int) keyFileContent.getSize()];
        try ( InputStream in = keyFileContent.getInputStream() ) {
          in.read( keyBytes );
        }

        // Create temporary key file
        tempKeyFile = Files.createTempFile( "ssh_key_", ".pem" );
        Files.write( tempKeyFile, keyBytes );

        config.authType( SshConfig.AuthType.PUBLIC_KEY )
              .keyPath( tempKeyFile );
        if ( !Utils.isEmpty( passPhrase ) ) {
          config.passphrase( space.environmentSubstitute( passPhrase ) );
        }
      } else {
        config.authType( SshConfig.AuthType.PASSWORD )
              .password( password );
      }

      // Configure proxy if specified
      if ( !Utils.isEmpty( proxyhost ) ) {
        config.proxy( proxyhost, proxyport );
        if ( !Utils.isEmpty( proxyusername ) ) {
          config.proxyAuth( proxyusername, proxypassword );
        }
      }

      // Create and connect
      connection = SshConnectionFactory.defaultFactory().open( config );
      connection.connect();

      return connection;

    } catch ( Exception e ) {
      // Something wrong happened - clean up and re-throw
      if ( connection != null ) {
        connection.close();
      }
      throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.ErrorConnecting", server, username ), e );
    } finally {
      // Clean up temporary key file
      if ( tempKeyFile != null ) {
        try {
          Files.deleteIfExists( tempKeyFile );
        } catch ( IOException e ) {
          // Log but don't fail - cleanup is best effort
        }
      }
    }
  }
}
