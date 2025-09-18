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

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.ssh.SshImplementation;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.BaseStepData;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SSHData extends BaseStepData {
  
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
   * 
   * @param preferredImplementation SSH implementation to prefer (null for auto-detect)
   * @param log Optional log channel for warnings and debug info
   */
  public static SshConnection OpenSshConnection(
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
        log.logDebug( "SSH Config - Auth type: " + config.getAuthType()
                    + ", Connect timeout: " + config.getConnectTimeoutMillis() + "ms"
                    + ", Command timeout: " + config.getCommandTimeoutMillis() + "ms"
                    + ", Implementation: " + config.getImplementation() );
      }

      SshConnection connection = SshConnectionFactory.defaultFactory().open( config, log );
      if ( log != null && log.isDebug() ) {
        log.logDebug( "Successfully created SSH connection object, type: " + connection.getClass().getSimpleName() );
      }
      return connection;

    } catch ( Exception e ) {
      // Log the exception details for debugging
      if ( log != null ) {
        log.logError( "Failed to create SSH connection to " + server + ":" + port
                    + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage() );
        log.logError( "Full stack trace for SSH connection failure:", e );
      }

      // Re-throw as KettleException to maintain API contract
      throw new KettleException( "SSH connection failed: " + e.getMessage(), e );
    }
  }

  /**
   * Convenience method that uses auto-detection for SSH implementation.
   */
  public static SshConnection OpenSshConnection(
      Bowl bowl, String server, int port, String username, String password,
      boolean useKey, String keyFilename, String passPhrase, int timeOut,
      VariableSpace space, String proxyhost, int proxyport,
      String proxyusername, String proxypassword, LogChannelInterface log ) throws KettleException {

    return OpenSshConnection( bowl, server, port, username, password, useKey, keyFilename,
                              passPhrase, timeOut, space, proxyhost, proxyport, proxyusername,
                              proxypassword, null, log );
  }
}
