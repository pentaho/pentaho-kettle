package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pentaho.di.core.ssh.trilead.TrileadSshConnection;

/**
 * Specific tests for Trilead SSH implementation.
 * These tests focus on validating the implementation without requiring
 * a running SSH server, since Trilead SSH2 build213 is incompatible
 * with modern MINA SSHD servers.
 */
public class TrileadSshConnectionTest {

  @Test
  public void testTrileadClassesAvailable() {
    // Verify that Trilead classes can be loaded
    TrileadSshServerStub.validateTrileadImplementation();
  }

  @Test
  public void testTrileadSshConnectionCreation() {
    // Test basic instantiation of TrileadSshConnection with config
    SshConfig config = SshConfig.create()
      .host( "localhost" )
      .port( 22 )
      .username( "testuser" )
      .password( "testpass" );

    TrileadSshConnection connection = new TrileadSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testTrileadSshConnectionConfiguration() {
    // Test that SshConfig works properly with Trilead implementation
    SshConfig config = SshConfig.create()
      .host( "localhost" )
      .port( 22 )
      .username( "testuser" )
      .password( "testpass" )
      .connectTimeoutMillis( 5000 )
      .commandTimeoutMillis( 30000 );

    // Test configuration methods don't throw exceptions
    try {
      TrileadSshConnection connection = new TrileadSshConnection( config );
      assertNotNull( connection );
    } catch ( Exception e ) {
      fail( "Configuration should not throw exceptions: " + e.getMessage() );
    }

    // Verify configuration was set
    assertEquals( "localhost", config.getHost() );
    assertEquals( 22, config.getPort() );
    assertEquals( "testuser", config.getUsername() );
    assertEquals( 5000, config.getConnectTimeoutMillis() );
    assertEquals( 30000, config.getCommandTimeoutMillis() );
  }
}
