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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpException;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshAuthenticationException;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionException;
import org.pentaho.di.core.ssh.SshTimeoutException;

public class MinaSshConnection implements SshConnection {
  private SshClient client;
  private ClientSession session;
  private final SshConfig config;
  private final LogChannelInterface log;

  public MinaSshConnection( SshConfig config ) {
    this.config = config;
    this.log = new LogChannel( "MinaSshConnection" );
  }

  public MinaSshConnection( SshConfig config, LogChannelInterface log ) {
    this.config = config;
    this.log = log != null ? log : new LogChannel( "MinaSshConnection" );
  }

  private void logInfo( String message ) {
    log.logBasic( message );
  }

  private void logDebug( String message ) {
    log.logDebug( message );
  }

  private void logError( String message, Throwable t ) {
    if ( t != null ) {
      log.logError( message, t );
    } else {
      log.logError( message );
    }
  }

  @Override
  public void connect() throws SshConnectionException {
    if ( client != null ) {
      return;
    }

    logConnectionStart();
    setupSshClient();
    ConnectFuture connectFuture = createConnection();
    waitForConnection( connectFuture );
    session = establishSession( connectFuture );
    authenticateSession();
    configureSessionHeartbeat();
  }

  private void logConnectionStart() {
    logInfo( "MinaSshConnection: Starting connection to " + config.getHost() + ":" + config.getPort() );
    logInfo( "MinaSshConnection: Auth type: " + config.getAuthType() + ", Username: " + config.getUsername() );
    logInfo( "MinaSshConnection: Connect timeout: " + config.getConnectTimeoutMillis() + "ms" );
  }

  private void setupSshClient() {
    client = SshClient.setUpDefaultClient();

    // Disable strict host key checking to avoid key exchange issues
    client.setServerKeyVerifier( ( clientSession, remoteAddress, serverKey ) -> {
      logDebug( "MinaSshConnection: Host key verifier called for " + remoteAddress + ", accepting key" );
      return true;
    } );

    logDebug( "MinaSshConnection: Starting SSH client..." );
    client.start();
    logDebug( "MinaSshConnection: SSH client started successfully" );
  }

  private ConnectFuture createConnection() throws SshConnectionException {
    logInfo( "MinaSshConnection: Attempting connection..." );
    
    try {
      ConnectFuture cf = client.connect( config.getUsername(), config.getHost(), config.getPort() );
      logDebug( "MinaSshConnection: Connection future created, waiting for completion..." );
      return cf;
    } catch ( Exception e ) {
      logError( "MinaSshConnection: Failed to create connection: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e );
      throw new SshConnectionException( "Failed to create SSH connection", e );
    }
  }

  private void waitForConnection( ConnectFuture cf ) throws SshConnectionException {
    long extendedTimeout = Math.max( config.getConnectTimeoutMillis(), 60000L ); // At least 60 seconds
    boolean connected;
    
    try {
      connected = cf.await( extendedTimeout );
    } catch ( IOException e ) {
      throw new SshConnectionException( "SSH connection failed during await", e );
    }
    
    logDebug( "MinaSshConnection: Connection await completed, connected: " + connected );

    if ( !connected ) {
      logError( "MinaSshConnection: Connection timed out after " + extendedTimeout + "ms", null );
      throw new SshTimeoutException( "SSH connection timed out after " + extendedTimeout + "ms" );
    }

    if ( !cf.isConnected() ) {
      Throwable cause = cf.getException();
      logError( "MinaSshConnection: Connection failed, exception: " + ( cause != null ? cause.getClass().getSimpleName() + ": " + cause.getMessage() : "null" ), cause );
      throw new SshConnectionException( "SSH connection failed", cause );
    }
  }

  private ClientSession establishSession( ConnectFuture cf ) throws SshConnectionException {
    logInfo( "MinaSshConnection: Connection established successfully" );
    ClientSession session = cf.getSession();
    
    if ( session == null ) {
      logError( "MinaSshConnection: Session is null after connection", null );
      throw new SshConnectionException( "SSH connection failed - session is null" );
    }

    logDebug( "MinaSshConnection: Session obtained: " + session.getClass().getSimpleName() );
    return session;
  }

  private void authenticateSession() throws SshConnectionException {
    logInfo( "MinaSshConnection: Attempting authentication..." );
    boolean authed = tryPublicKeyAuthentication();
    
    if ( !authed ) {
      authed = tryPasswordAuthentication();
    }
    
    if ( !authed ) {
      logError( "MinaSshConnection: Authentication failed - no valid auth method succeeded", null );
      throw new SshAuthenticationException( "SSH authentication failed" );
    }

    logInfo( "MinaSshConnection: Successfully authenticated and connected!" );
  }

  private boolean tryPublicKeyAuthentication() throws SshAuthenticationException {
    if ( config.getAuthType() != SshConfig.AuthType.PUBLIC_KEY || config.getKeyPath() == null ) {
      return false;
    }

    Path key = config.getKeyPath();
    if ( !Files.exists( key ) ) {
      return false;
    }

    try {
      FileKeyPairProvider prov = new FileKeyPairProvider( List.of( key ) );
      for ( KeyPair kp : prov.loadKeys( null ) ) {
        session.addPublicKeyIdentity( kp );
      }
      return session.auth().verify( config.getConnectTimeoutMillis() ).isSuccess();
    } catch ( IOException e ) {
      throw new SshAuthenticationException( "Failed to load SSH key: " + key, e );
    }
  }

  private boolean tryPasswordAuthentication() throws SshAuthenticationException {
    if ( config.getPassword() == null ) {
      return false;
    }

    logDebug( "MinaSshConnection: Trying password authentication..." );
    try {
      session.addPasswordIdentity( config.getPassword() );
      boolean authed = session.auth().verify( config.getConnectTimeoutMillis() ).isSuccess();
      logDebug( "MinaSshConnection: Password authentication result: " + authed );
      return authed;
    } catch ( IOException e ) {
      throw new SshAuthenticationException( "Password authentication failed", e );
    }
  }

  private void configureSessionHeartbeat() {
    if ( config.getCommandTimeoutMillis() > 0 ) {
      int intervalSeconds = (int) Math.max( 1, config.getCommandTimeoutMillis() / 1000 );
      // session implements SessionHeartbeatController
      ( (SessionHeartbeatController) session ).setSessionHeartbeat( SessionHeartbeatController.HeartbeatType.IGNORE,
        TimeUnit.SECONDS, intervalSeconds );
    }
  }

  @Override
  public ExecResult exec( String command, long timeoutMs ) throws SshConnectionException {
    try {
      ByteArrayOutputStream stdout = new ByteArrayOutputStream();
      ByteArrayOutputStream stderr = new ByteArrayOutputStream();
      int exit;
      try ( var ch = session.createExecChannel( command ) ) {
        ch.setOut( stdout );
        ch.setErr( stderr );
        ch.open().verify( timeoutMs );

        // Wait for the command to complete and exit status to be available
        Set<ClientChannelEvent> events = ch.waitFor(
          EnumSet.of( ClientChannelEvent.CLOSED ), timeoutMs );

        // Give a bit more time for exit status to be set if the channel closed successfully
        if ( events.contains( ClientChannelEvent.CLOSED ) ) {
          // Try to get exit status with a short additional wait
          Integer ex = ch.getExitStatus();
          if ( ex == null ) {
            // Wait a bit more for exit status to be set
            Thread.sleep( 100 );
            ex = ch.getExitStatus();
          }
          exit = ex == null ? -1 : ex;
        } else {
          // Timeout occurred
          throw new SshTimeoutException( "Command execution timed out after " + timeoutMs + "ms" );
        }
      }
      String outStr = stdout.toString( StandardCharsets.UTF_8 );
      String errStr = stderr.toString( StandardCharsets.UTF_8 );
      return new ExecResult( outStr, errStr, outStr + errStr, exit, exit != 0 );
    } catch ( SshTimeoutException e ) {
      throw e; // Re-throw timeout exceptions as-is
    } catch ( Exception e ) {
      throw new SshConnectionException( "Failed to execute command: " + command, e );
    }
  }

  @Override
  public SftpSession openSftp() throws SshConnectionException {
    try {
      var factory = SftpClientFactory.instance();
      var sftp = factory.createSftpClient( session );
      return new MinaSftpSession( sftp );
    } catch ( Exception e ) {
      throw new SftpException( "Failed to open SFTP session", e );
    }
  }

  @Override
  public void close() {
    if ( session != null ) {
      session.close( false );
    }
    if ( client != null ) {
      client.stop();
    }
  }
}
