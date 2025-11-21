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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.exceptions.SftpException;
import org.pentaho.di.core.ssh.exceptions.SshAuthenticationException;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;
import org.pentaho.di.core.ssh.exceptions.SshTimeoutException;
import org.pentaho.di.core.logging.LogChannelInterface;

public class MinaSshConnection implements SshConnection {

  private static final String ERROR = "error";
  private static final String BASIC = "basic";
  private static final String DEBUG = "debug";
  private SshClient client;
  private ClientSession session;

  private final SshConfig config;

  public MinaSshConnection( SshConfig config ) {
    this.config = config;
  }

  private void log( String level, String message ) {
    LogChannelInterface log = config.getLogChannel();
    if ( log != null ) {
      switch ( level.toLowerCase() ) {
        case DEBUG:
          log.logDebug( message );
          break;
        case BASIC:
          log.logBasic( message );
          break;
        case ERROR:
          log.logError( message );
          break;
        default:
          log.logBasic( message );
      }
    }
  }

  private void log( String level, String message, Throwable t ) {
    LogChannelInterface log = config.getLogChannel();
    if ( log != null ) {
      switch ( level.toLowerCase() ) {
        case DEBUG:
          log.logDebug( message, t );
          break;
        case ERROR:
          log.logError( message, t );
          break;
        default:
          log.logError( message, t );
      }
    }
  }

  @Override
  public void connect() throws SshConnectionException {
    if ( client != null ) {
      return;
    }

    setupSshClient();
    ConnectFuture connectFuture = createConnection();
    waitForConnection( connectFuture );
    session = establishSession( connectFuture );
    authenticateSession();
    configureSessionHeartbeat();
  }

  private void setupSshClient() {
    client = SshClient.setUpDefaultClient();

    // Disable strict host key checking to avoid key exchange issues
    client.setServerKeyVerifier( ( clientSession, remoteAddress, serverKey ) -> true );

    client.start();
  }

  private ConnectFuture createConnection() throws SshConnectionException {
    try {
      if ( isProxyConfigured() ) {
        return createProxyConnection();
      } else {
        return createDirectConnection();
      }
    } catch ( SshConnectionException e ) {
      throw e; // Re-throw SshConnectionException as-is
    } catch ( Exception e ) {
      throw new SshConnectionException( buildConnectionErrorMessage( e ), e );
    }
  }

  private boolean isProxyConfigured() {
    return config.getProxyHost() != null && !config.getProxyHost().trim().isEmpty();
  }

  private ConnectFuture createDirectConnection() throws IOException {
    log( DEBUG, "SSH Direct Connection: " + config.getHost() + ":" + config.getPort()
        + formatUserInfo() );
    return client.connect( config.getUsername(), config.getHost(), config.getPort() );
  }

  private ConnectFuture createProxyConnection() throws SshConnectionException, IOException {
    log( BASIC, "SSH over HTTP Proxy: " + config.getProxyHost() + ":" + config.getProxyPort()
        + " -> " + config.getHost() + ":" + config.getPort() );
    log( DEBUG, "HTTP Proxy Details - Target User: " + config.getUsername() + ", Auth: " + config.getAuthType() );

    configureHttpProxyConnector();

    log( DEBUG, "Connecting to HTTP proxy: " + config.getProxyHost() + ":" + config.getProxyPort() );
    log( DEBUG, "Target SSH server (via proxy): " + config.getHost() + ":" + config.getPort() );

    return client.connect( config.getUsername(), config.getProxyHost(), config.getProxyPort() );
  }

  private void configureHttpProxyConnector() throws SshConnectionException {
    try {
      client.setClientProxyConnector( this::sendHttpConnectRequest );
    } catch ( Exception e ) {
      String errorMsg = "SSH over HTTP proxy connection failed: " + e.getMessage()
          + "\nProxy: " + config.getProxyHost() + ":" + config.getProxyPort()
          + "\nTarget: " + config.getHost() + ":" + config.getPort() + formatUserInfo()
          + "\nNote: Ensure HTTP proxy supports CONNECT method and target is reachable.";
      log( ERROR, errorMsg );
      throw new SshConnectionException( errorMsg, e );
    }
  }

  private String formatUserInfo() {
    return " (user: " + config.getUsername() + ")";
  }

  private void sendHttpConnectRequest( ClientSession session ) throws IOException {
    log( DEBUG, "Sending HTTP CONNECT proxy metadata" );

    String connectRequest = buildHttpConnectRequest();
    log( DEBUG, "HTTP CONNECT request: " + connectRequest.replace( "\r\n", "\\r\\n" ) );

    IoSession ioSession = session.getIoSession();
    ByteArrayBuffer buffer = new ByteArrayBuffer( connectRequest.getBytes() );
    ioSession.writeBuffer( buffer );

    log( DEBUG, "HTTP CONNECT request sent via proxy connector" );
  }

  private String buildHttpConnectRequest() {
    StringBuilder request = new StringBuilder();

    request.append( String.format(
        "CONNECT %s:%d HTTP/1.1\r\n"
        + "Host: %s:%d\r\n"
        + "Proxy-Connection: keep-alive\r\n",
        config.getHost(), config.getPort(),
        config.getHost(), config.getPort()
    ) );

    if ( hasProxyAuthentication() ) {
      request.append( buildProxyAuthorizationHeader() );
    }

    request.append( "\r\n" );
    return request.toString();
  }

  private boolean hasProxyAuthentication() {
    return config.getProxyUser() != null && config.getProxyPassword() != null
        && !config.getProxyUser().trim().isEmpty() && !config.getProxyPassword().trim().isEmpty();
  }

  private String buildProxyAuthorizationHeader() {
    String auth = config.getProxyUser() + ":" + config.getProxyPassword();
    String encodedAuth = java.util.Base64.getEncoder().encodeToString(
        auth.getBytes( java.nio.charset.StandardCharsets.UTF_8 ) );
    log( DEBUG, "Added HTTP proxy authentication for user: " + config.getProxyUser() );
    return "Proxy-Authorization: Basic " + encodedAuth + "\r\n";
  }

  private String buildConnectionErrorMessage( Exception e ) {
    StringBuilder errorMsg = new StringBuilder( "SSH connection failed - " );

    if ( isProxyConfigured() ) {
      errorMsg.append( "HTTP Proxy: " ).append( config.getProxyHost() ).append( ":" )
          .append( config.getProxyPort() ).append( " -> Target: " );
    } else {
      errorMsg.append( "Direct: " );
    }

    errorMsg.append( config.getHost() ).append( ":" ).append( config.getPort() )
        .append( formatUserInfo() )
        .append( " - " ).append( e.getClass().getSimpleName() ).append( ": " )
        .append( e.getMessage() );

    return errorMsg.toString();
  }

  private void waitForConnection( ConnectFuture cf ) throws SshConnectionException {
    long extendedTimeout = config.getConnectTimeoutMillis() > 0 ? config.getConnectTimeoutMillis() : 60000; // Default 60 seconds
    boolean connected;

    try {
      connected = cf.await( extendedTimeout );
    } catch ( IOException e ) {
      throw new SshConnectionException( "SSH connection failed during await", e );
    }


    if ( !connected ) {
      throw new SshTimeoutException( "SSH connection timed out after " + extendedTimeout + "ms" );
    }

    if ( !cf.isConnected() ) {
      Throwable cause = cf.getException();
      throw new SshConnectionException( "SSH connection failed", cause );
    }
  }

  private ClientSession establishSession( ConnectFuture cf ) throws SshConnectionException {
    ClientSession s = cf.getSession();

    if ( s == null ) {
      throw new SshConnectionException( "SSH connection failed - session is null" );
    }

    return s;
  }

  private void authenticateSession() throws SshConnectionException {
    log( DEBUG, "Starting SSH authentication - Auth type: " + config.getAuthType() );
    boolean authed = tryPublicKeyAuthentication();

    if ( !authed ) {
      log( DEBUG, "Public key authentication failed or not configured, trying password authentication" );
      authed = tryPasswordAuthentication();
    }

    if ( !authed ) {
      log( ERROR, "All SSH authentication methods failed" );
      throw new SshAuthenticationException( "SSH authentication failed" );
    } else {
      log( BASIC, "SSH authentication successful" );
    }
  }

  private boolean tryPublicKeyAuthentication() throws SshAuthenticationException {
    if ( config.getAuthType() != SshConfig.AuthType.PUBLIC_KEY ) {
      log( DEBUG, "Skipping public key authentication - not configured" );
      return false;
    }

    log( DEBUG, "Attempting SSH public key authentication" );

    try {
      KeyPairProvider keyPairProvider = loadKeyPairProvider();
      if ( keyPairProvider == null ) {
        return false;
      }

      configurePassphrase( keyPairProvider );
      loadKeysIntoSession( keyPairProvider );

      return performPublicKeyAuth();
    } catch ( IOException | GeneralSecurityException e ) {
      log( ERROR, "SSH public key authentication error: " + e.getMessage(), e );
      throw new SshAuthenticationException( "Failed to load SSH key", e );
    }
  }

  private KeyPairProvider loadKeyPairProvider() {
    if ( config.getKeyContent() != null ) {
      log( DEBUG, "Using in-memory SSH key content" );
      return createInMemoryKeyProvider( config.getKeyContent() );
    }

    if ( config.getKeyPath() != null ) {
      log( DEBUG, "Loading SSH key from file: " + config.getKeyPath() );
      Path key = config.getKeyPath();
      if ( !Files.exists( key ) ) {
        log( ERROR, "SSH key file does not exist: " + key );
        return null;
      }
      return new FileKeyPairProvider( List.of( key ) );
    }

    log( ERROR, "No SSH key content or file path provided" );
    return null;
  }

  private void configurePassphrase( KeyPairProvider keyPairProvider ) {
    if ( config.getPassphrase() != null && !config.getPassphrase().trim().isEmpty() ) {
      log( DEBUG, "SSH key passphrase provided" );
      String passphrase = config.getPassphrase();
      if ( keyPairProvider instanceof FileKeyPairProvider ) {
        ((FileKeyPairProvider) keyPairProvider).setPasswordFinder( ( sessionResource, resourceKey, retryIndex ) -> passphrase );
      }
    } else {
      log( DEBUG, "No SSH key passphrase provided" );
    }
  }

  private void loadKeysIntoSession( KeyPairProvider keyPairProvider ) throws IOException, GeneralSecurityException {
    Iterable<KeyPair> keys = keyPairProvider.loadKeys( null );
    int keyCount = 0;
    for ( KeyPair kp : keys ) {
      session.addPublicKeyIdentity( kp );
      keyCount++;
    }
    log( DEBUG, "Loaded " + keyCount + " SSH key(s)" );
  }

  private boolean performPublicKeyAuth() throws IOException {
    AuthFuture authFuture = session.auth();
    boolean success = authFuture.verify( config.getConnectTimeoutMillis() ).isSuccess();

    if ( success ) {
      log( DEBUG, "SSH public key authentication successful" );
    } else {
      log( DEBUG, "SSH public key authentication failed" );
      if ( authFuture.getException() != null ) {
        log( DEBUG, "Authentication failure reason: " + authFuture.getException().getMessage() );
      }
    }

    return success;
  }

  private boolean tryPasswordAuthentication() throws SshAuthenticationException {
    if ( config.getPassword() == null ) {
      log( DEBUG, "No password provided for SSH authentication" );
      return false;
    }

    log( DEBUG, "Attempting SSH password authentication" );

    try {
      // Always add password identity - SSH authentication happens with the target server
      // even when using HTTP CONNECT proxy (proxy just tunnels the TCP connection)
      session.addPasswordIdentity( config.getPassword() );
      log( DEBUG, "Added password identity for SSH authentication" );

      AuthFuture authFuture = session.auth();
      boolean success = authFuture.verify( config.getConnectTimeoutMillis() ).isSuccess();

      if ( success ) {
        log( DEBUG, "SSH password authentication successful" );
      } else {
        log( DEBUG, "SSH password authentication failed" );
        if ( authFuture.getException() != null ) {
          log( DEBUG, "Authentication failure reason: " + authFuture.getException().getMessage() );
        }
      }

      return success;
    } catch ( IOException e ) {
      log( ERROR, "SSH password authentication error: " + e.getMessage(), e );
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
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt(); // Restore interrupted status
      throw new SshConnectionException( "Command execution was interrupted", e );
    } catch ( Exception e ) {
      throw new SshConnectionException( "Failed to execute command: " + command, e );
    }
  }

  @Override
  public ExecResult exec( String command ) throws SshConnectionException {
    return exec( command, config.getCommandTimeoutMillis() );
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

  /**
   * Creates an in-memory key provider from key content bytes.
   * This avoids writing sensitive key data to the filesystem.
   */
  private KeyPairProvider createInMemoryKeyProvider( byte[] keyContent ) {
    return new AbstractKeyPairProvider() {
      @Override
      public Iterable<KeyPair> loadKeys( SessionContext session ) throws IOException {
        try {
          // Use SecurityUtils to parse the key content directly from input stream
          ByteArrayInputStream keyStream = new ByteArrayInputStream( keyContent );
          return SecurityUtils.loadKeyPairIdentities( session, null,
              keyStream, ( s, r, i ) -> config.getPassphrase() );
        } catch ( Exception e ) {
          throw new IOException( "Failed to parse SSH key content", e );
        }
      }
    };
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
