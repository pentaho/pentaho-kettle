package org.pentaho.di.core.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.List;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

public class TestSshServer {
  private SshServer server;
  private int assignedPort;
  private KeyPair serverKeyPair;
  private PublicKey authorizedKey; // The public key that will be accepted for authentication

  public void start( int port ) throws Exception {
    server = SshServer.setUpDefaultServer();
    server.setPort( port );
    server.setKeyPairProvider( new KeyPairProvider() {
      private KeyPair pair;

      private KeyPair get() throws Exception {
        if ( pair == null ) {
          KeyPairGenerator kpg = KeyPairGenerator.getInstance( "RSA" );
          kpg.initialize( 2048 );
          pair = kpg.generateKeyPair();
          serverKeyPair = pair; // Store for testing
        }
        return pair;
      }

      @Override
      public Iterable<KeyPair> loadKeys( SessionContext session ) {
        try {
          return List.of( get() );
        } catch ( Exception e ) {
          throw new RuntimeException( e );
        }
      }
    } );

    // Set up password authentication
    server.setPasswordAuthenticator( ( username, password, session ) -> "test".equals( username ) && "test".equals(
      password ) );

    // Set up public key authentication - accept any key for "test" user by default
    // This can be overridden by calling setAuthorizedKey()
    server.setPublickeyAuthenticator( ( username, key, session ) -> {
      if ( !"test".equals( username ) ) {
        return false;
      }
      if ( authorizedKey == null ) {
        return true; // Accept any key for testing by default
      }
      return KeyUtils.compareKeys( key, authorizedKey );
    } );
    server.setSubsystemFactories( List.of( new SftpSubsystemFactory() ) );
    server.setCommandFactory( new EchoCommandFactory() );
    server.start();
    assignedPort = server.getPort();
  }

  public int getAssignedPort() {
    return assignedPort;
  }

  public KeyPair getServerKeyPair() {
    return serverKeyPair;
  }

  public void setAuthorizedKey( PublicKey key ) {
    this.authorizedKey = key;
  }

  public void stop() throws IOException {
    if ( server != null ) {
      server.stop( true );
    }
  }

  static class EchoCommandFactory implements CommandFactory {
    @Override
    public Command createCommand( ChannelSession channel, String command ) {
      return new EchoCommand( command );
    }
  }

  static class EchoCommand implements Command, Runnable {
    private final String command;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;

    EchoCommand( String command ) {
      this.command = command;
    }

    @Override
    public void setInputStream( InputStream in ) {
    }

    @Override
    public void setOutputStream( OutputStream out ) {
      this.out = out;
    }

    @Override
    public void setErrorStream( OutputStream err ) {
      this.err = err;
    }

    @Override
    public void setExitCallback( ExitCallback callback ) {
      this.callback = callback;
    }

    @Override
    public void start( ChannelSession channel, Environment env ) throws IOException {
      new Thread( this, "echo-cmd" ).start();
    }

    @Override
    public void destroy( ChannelSession channel ) {
    }

    @Override
    public void run() {
      try {
        String result = command.startsWith( "echo " ) ? command.substring( 5 ) + "\n" : "unsupported\n";
        if ( out != null ) {
          out.write( result.getBytes( StandardCharsets.UTF_8 ) );
          out.flush();
        }
        if ( callback != null ) {
          callback.onExit( 0 );
        }
      } catch ( IOException e ) {
        try {
          if ( err != null ) {
            err.write( e.getMessage().getBytes( StandardCharsets.UTF_8 ) );
            err.flush();
          }
        } catch ( IOException ignored ) {
        }
        if ( callback != null ) {
          callback.onExit( 1 );
        }
      }
    }
  }
}
