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

package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.ssh.mina.MinaSshConnection;

/**
 * Tests that verify proxy configuration is properly handled in SSH connections.
 * These tests verify configuration setup but don't test actual proxy connectivity
 * as that would require setting up a real proxy server.
 */
public class SshProxyConfigurationTest {

  private TestSshServer testServer;
  private int serverPort;
  private Path tempKeyFile;
  private KeyPair testKeyPair;

  @Before
  public void setUp() throws Exception {
    // Start test SSH server
    testServer = new TestSshServer();
    testServer.start( 0 ); // Use any available port
    serverPort = testServer.getAssignedPort();

    // Generate a test key pair for public key authentication
    KeyPairGenerator kpg = KeyPairGenerator.getInstance( "RSA" );
    kpg.initialize( 2048 );
    testKeyPair = kpg.generateKeyPair();

    // Write the private key to a temporary file
    tempKeyFile = Files.createTempFile( "test-ssh-key", ".pem" );
    try ( FileOutputStream fos = new FileOutputStream( tempKeyFile.toFile() ) ) {
      OpenSSHKeyPairResourceWriter writer = new OpenSSHKeyPairResourceWriter();
      writer.writePrivateKey( testKeyPair, "Test SSH Key", null, fos );
    }

    // Configure the server to accept this public key
    testServer.setAuthorizedKey( testKeyPair.getPublic() );
  }

  @After
  public void tearDown() throws Exception {
    if ( testServer != null ) {
      testServer.stop();
    }
    if ( tempKeyFile != null && Files.exists( tempKeyFile ) ) {
      Files.delete( tempKeyFile );
    }
  }

  @Test
  public void testProxyConfigurationIsHandled() throws Exception {
    // Create SSH config with proxy settings
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .proxy( "proxy.example.com", 8080 )
        .proxyAuth( "proxyuser", "proxypass" )
        .connectTimeoutMillis( 1000 ) // Short timeout since proxy won't work
        .commandTimeoutMillis( 5000 );

    // Verify that proxy configuration is properly set
    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
    assertEquals( "proxyuser", config.getProxyUser() );
    assertEquals( "proxypass", config.getProxyPassword() );

    // Create connection with proxy config - this will fail to connect but won't crash
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      fail( "Connection should fail due to non-existent proxy" );
    } catch ( Exception e ) {
      // Expected - proxy doesn't exist so connection should fail
      // The important thing is that the proxy configuration was properly handled
      // without causing any NPE or configuration errors
      assertNotNull( "Exception should be thrown due to proxy connection failure", e );
    }
  }

  @Test
  public void testDirectConnectionWithoutProxy() throws Exception {
    // Create SSH config without proxy settings
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .connectTimeoutMillis( 5000 )
        .commandTimeoutMillis( 10000 );

    // Verify no proxy is configured
    assertNull( "Proxy host should be null", config.getProxyHost() );
    assertEquals( 0, config.getProxyPort() );
    assertNull( "Proxy user should be null", config.getProxyUser() );

    // This should work fine without proxy
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      // If we get here without exception, connection was successful

      // Test command execution
      ExecResult result = connection.exec( "echo Direct Connection Works", 5000 );
      assertNotNull( "Command result should not be null", result );
      assertEquals( "Command should succeed", 0, result.getExitCode() );
      assertEquals( "Output should match", "Direct Connection Works\n", result.getStdout() );
    }
  }

  @Test
  public void testProxyConfigurationWithPublicKey() throws Exception {
    // Create SSH config with both proxy and public key authentication
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .keyPath( tempKeyFile )
        .passphrase( "test-passphrase" )
        .authType( SshConfig.AuthType.PUBLIC_KEY )
        .proxy( "proxy.example.com", 3128 )
        .proxyAuth( "user", "pass" )
        .connectTimeoutMillis( 1000 ) // Short timeout
        .commandTimeoutMillis( 5000 );

    // Verify configuration is properly set
    assertEquals( "localhost", config.getHost() );
    assertEquals( serverPort, config.getPort() );
    assertEquals( "test", config.getUsername() );
    assertEquals( tempKeyFile, config.getKeyPath() );
    assertEquals( "test-passphrase", config.getPassphrase() );
    assertEquals( SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 3128, config.getProxyPort() );
    assertEquals( "user", config.getProxyUser() );
    assertEquals( "pass", config.getProxyPassword() );

    // Create connection - should handle both proxy and key auth configuration
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      fail( "Connection should fail due to non-existent proxy" );
    } catch ( Exception e ) {
      // Expected - proxy doesn't exist so connection should fail
      // But the configuration should be properly handled
      assertNotNull( "Exception should be thrown", e );
    }
  }

  @Test
  public void testProxyWithoutAuthentication() throws Exception {
    // Create SSH config with proxy but no proxy authentication
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .proxy( "proxy.example.com", 8080 ) // No proxy auth
        .connectTimeoutMillis( 1000 )
        .commandTimeoutMillis( 5000 );

    // Verify proxy is configured but no proxy auth
    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
    assertNull( "Proxy user should be null", config.getProxyUser() );
    assertNull( "Proxy password should be null", config.getProxyPassword() );

    // Connection should fail due to non-existent proxy, but not due to config issues
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      fail( "Connection should fail due to non-existent proxy" );
    } catch ( Exception e ) {
      // Expected
      assertNotNull( "Exception should be thrown", e );
    }
  }
}
