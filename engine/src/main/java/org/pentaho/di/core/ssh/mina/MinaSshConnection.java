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
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SftpSession;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;

public class MinaSshConnection implements SshConnection {
  private SshClient client;
  private ClientSession session;
  private final SshConfig config;

  public MinaSshConnection( SshConfig config ) {
    this.config = config;
  }

  @Override
  public void connect() throws Exception {
    if ( client != null ) return;
    client = SshClient.setUpDefaultClient();
    client.start();
    ConnectFuture cf = client.connect( config.getUsername(), config.getHost(), config.getPort() );
    boolean connected = cf.await( config.getConnectTimeoutMillis() );
    if ( !connected ) {
      throw new IOException( "SSH connection timed out after " + config.getConnectTimeoutMillis() + "ms" );
    }
    if ( !cf.isConnected() ) {
      Throwable cause = cf.getException();
      throw new IOException( "SSH connection failed", cause );
    }
    session = cf.getSession();
    if ( session == null ) {
      throw new IOException( "SSH connection failed - session is null" );
    }
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
      session.addPasswordIdentity( config.getPassword() );
      authed = session.auth().verify( config.getConnectTimeoutMillis() ).isSuccess();
    }
    if ( !authed ) throw new IOException( "SSH authentication failed" );
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
