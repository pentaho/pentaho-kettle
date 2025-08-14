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

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;

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
  public void connect() throws Exception {
    if ( client != null ) {
      return;
    }

    logInfo( "MinaSshConnection: Starting connection to " + config.getHost() + ":" + config.getPort() );
    logInfo( "MinaSshConnection: Auth type: " + config.getAuthType() + ", Username: " + config.getUsername() );
    logInfo( "MinaSshConnection: Connect timeout: " + config.getConnectTimeoutMillis() + "ms" );

    client = SshClient.setUpDefaultClient();

    // Disable strict host key checking to avoid key exchange issues
    client.setServerKeyVerifier( ( clientSession, remoteAddress, serverKey ) -> {
      logDebug( "MinaSshConnection: Host key verifier called for " + remoteAddress + ", accepting key" );
      return true;
    } );

    // Set additional properties to improve compatibility
    client.getProperties().put( "heartbeat-interval", "0" );
    client.getProperties().put( "heartbeat-request", "FALSE" );

    // Try to set more compatible settings for older SSH servers
    client.getProperties().put( "nio2-min-write-timeout", "30000" );
    client.getProperties().put( "channel-session-max-packet-size", "32768" );
    client.getProperties().put( "window-size", "2097152" );

    logDebug( "MinaSshConnection: Starting SSH client..." );
    client.start();
    logDebug( "MinaSshConnection: SSH client started successfully" );

    logInfo( "MinaSshConnection: Attempting connection..." );

    // Create connection with more detailed error handling and retry logic
    ConnectFuture cf;
    try {
      cf = client.connect( config.getUsername(), config.getHost(), config.getPort() );
      logDebug( "MinaSshConnection: Connection future created, waiting for completion..." );
    } catch ( Exception e ) {
      logError( "MinaSshConnection: Failed to create connection: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e );
      throw new IOException( "Failed to create SSH connection", e );
    }

    // Try with a longer timeout for key exchange
    long extendedTimeout = Math.max( config.getConnectTimeoutMillis(), 60000L ); // At least 60 seconds
    boolean connected = cf.await( extendedTimeout );
    logDebug( "MinaSshConnection: Connection await completed, connected: " + connected );

    if ( !connected ) {
      logError( "MinaSshConnection: Connection timed out after " + extendedTimeout + "ms", null );
      throw new IOException( "SSH connection timed out after " + extendedTimeout + "ms" );
    }

    if ( !cf.isConnected() ) {
      Throwable cause = cf.getException();
      logError( "MinaSshConnection: Connection failed, exception: " + ( cause != null ? cause.getClass().getSimpleName() + ": " + cause.getMessage() : "null" ), cause );
      throw new IOException( "SSH connection failed", cause );
    }

    logInfo( "MinaSshConnection: Connection established successfully" );
    session = cf.getSession();
    if ( session == null ) {
      logError( "MinaSshConnection: Session is null after connection", null );
      throw new IOException( "SSH connection failed - session is null" );
    }

    logDebug( "MinaSshConnection: Session obtained: " + session.getClass().getSimpleName() );

    logInfo( "MinaSshConnection: Attempting authentication..." );
    boolean authed = false;
    if ( config.getAuthType() == SshConfig.AuthType.PUBLIC_KEY && config.getKeyPath() != null ) {
      Path key = config.getKeyPath();
      if ( Files.exists( key ) ) {
        FileKeyPairProvider prov = new FileKeyPairProvider( List.of( key ) );
        for ( KeyPair kp : prov.loadKeys( null ) ) {
          session.addPublicKeyIdentity( kp );
        }
        authed = session.auth().verify( config.getConnectTimeoutMillis() ).isSuccess();
      }
    }
    if ( !authed && config.getPassword() != null ) {
      logDebug( "MinaSshConnection: Trying password authentication..." );
      session.addPasswordIdentity( config.getPassword() );
      authed = session.auth().verify( config.getConnectTimeoutMillis() ).isSuccess();
      logDebug( "MinaSshConnection: Password authentication result: " + authed );
    }
    if ( !authed ) {
      logError( "MinaSshConnection: Authentication failed - no valid auth method succeeded", null );
      throw new IOException( "SSH authentication failed" );
    }

    logInfo( "MinaSshConnection: Successfully authenticated and connected!" );
    if ( config.getCommandTimeoutMillis() > 0 ) {
      int intervalSeconds = (int) Math.max( 1, config.getCommandTimeoutMillis() / 1000 );
      // session implements SessionHeartbeatController
      ( (SessionHeartbeatController) session ).setSessionHeartbeat( SessionHeartbeatController.HeartbeatType.IGNORE,
        TimeUnit.SECONDS, intervalSeconds );
    }
  }

  @Override
  public ExecResult exec( String command, long timeoutMs ) throws Exception {
    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    int exit;
    try ( var ch = session.createExecChannel( command ) ) {
      ch.setOut( stdout );
      ch.setErr( stderr );
      ch.open().verify( timeoutMs );

      // Wait for the command to complete and exit status to be available
      Set<org.apache.sshd.client.channel.ClientChannelEvent> events = ch.waitFor(
        EnumSet.of( org.apache.sshd.client.channel.ClientChannelEvent.CLOSED ), timeoutMs );

      // Give a bit more time for exit status to be set if the channel closed successfully
      if ( events.contains( org.apache.sshd.client.channel.ClientChannelEvent.CLOSED ) ) {
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
        exit = -1;
      }
    }
    String outStr = stdout.toString( StandardCharsets.UTF_8 );
    String errStr = stderr.toString( StandardCharsets.UTF_8 );
    return new ExecResult( outStr, errStr, outStr + errStr, exit, exit != 0 );
  }

  @Override
  public SftpSession openSftp() throws IOException {
    var factory = SftpClientFactory.instance();
    var sftp = factory.createSftpClient( session );
    return new MinaSftpSession( sftp );
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
