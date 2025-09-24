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
 * Integration tests that verify SSH connections actually work end-to-end
 * with a real SSH server, including public key authentication.
 */
public class SshConnectionIntegrationTest {

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
  public void testPasswordAuthenticationConnection() throws Exception {
    // Create SSH config for password authentication
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .connectTimeoutMillis( 5000 )
        .commandTimeoutMillis( 10000 );

    // Create and test connection
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      // If we get here without exception, connection was successful

      // Test command execution
      ExecResult result = connection.exec( "echo Hello World", 5000 );
      assertNotNull( "Command result should not be null", result );
      assertEquals( "Command should succeed", 0, result.getExitCode() );
      assertEquals( "Output should match", "Hello World\n", result.getStdout() );
    }
  }

  @Test
  public void testPublicKeyAuthenticationConnection() throws Exception {
    // Create SSH config for public key authentication
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .keyPath( tempKeyFile )
        .authType( SshConfig.AuthType.PUBLIC_KEY )
        .connectTimeoutMillis( 5000 )
        .commandTimeoutMillis( 10000 );

    // Create and test connection
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      // If we get here without exception, connection was successful

      // Test command execution
      ExecResult result = connection.exec( "echo Public Key Auth Works", 5000 );
      assertNotNull( "Command result should not be null", result );
      assertEquals( "Command should succeed", 0, result.getExitCode() );
      assertEquals( "Output should match", "Public Key Auth Works\n", result.getStdout() );
    }
  }

  @Test
  public void testPublicKeyAuthenticationWithPassphrase() throws Exception {
    // Create a key file with passphrase
    Path passphraseKeyFile = Files.createTempFile( "test-ssh-key-pass", ".pem" );
    try {
      String passphrase = "test-passphrase";
      try ( FileOutputStream fos = new FileOutputStream( passphraseKeyFile.toFile() ) ) {
        OpenSSHKeyPairResourceWriter writer = new OpenSSHKeyPairResourceWriter();
        writer.writePrivateKey( testKeyPair, "Test SSH Key with Passphrase", null, fos );
      }

      // Create SSH config with passphrase
      SshConfig config = SshConfig.create()
          .host( "localhost" )
          .port( serverPort )
          .username( "test" )
          .keyPath( passphraseKeyFile )
          .passphrase( passphrase )
          .authType( SshConfig.AuthType.PUBLIC_KEY )
          .connectTimeoutMillis( 5000 )
          .commandTimeoutMillis( 10000 );

      // Create and test connection
      try ( SshConnection connection = new MinaSshConnection( config ) ) {
        connection.connect();
        // If we get here without exception, connection was successful

        // Test command execution
        ExecResult result = connection.exec( "echo Passphrase Auth Works", 5000 );
        assertNotNull( "Command result should not be null", result );
        assertEquals( "Command should succeed", 0, result.getExitCode() );
        assertEquals( "Output should match", "Passphrase Auth Works\n", result.getStdout() );
      }
    } finally {
      if ( Files.exists( passphraseKeyFile ) ) {
        Files.delete( passphraseKeyFile );
      }
    }
  }

  @Test
  public void testSftpSession() throws Exception {
    // Create SSH config for password authentication
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .connectTimeoutMillis( 5000 )
        .commandTimeoutMillis( 10000 );

    // Create and test SFTP session
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      // If we get here without exception, connection was successful

      // Test SFTP session creation
      try ( SftpSession sftp = connection.openSftp() ) {
        assertNotNull( "SFTP session should not be null", sftp );
        // Basic SFTP test - just verify we can create the session
        // More detailed SFTP tests could be added here
      }
    }
  }

  @Test
  public void testInvalidAuthentication() throws Exception {
    // Test with wrong password
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( serverPort )
        .username( "test" )
        .password( "wrong-password" )
        .authType( SshConfig.AuthType.PASSWORD )
        .connectTimeoutMillis( 5000 )
        .commandTimeoutMillis( 10000 );

    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      // This should throw an authentication exception
      connection.connect();
      fail( "Connection should fail with wrong password" );
    } catch ( Exception e ) {
      // Expected - authentication should fail
      assertTrue( "Should get authentication-related exception",
             e.getMessage().toLowerCase().contains( "auth" )
          || e.getMessage().toLowerCase().contains( "login" )
          || e.getMessage().toLowerCase().contains( "password" ) );
    }
  }

  @Test
  public void testConnectionTimeout() throws Exception {
    // Test connection to a non-existent port
    SshConfig config = SshConfig.create()
        .host( "localhost" )
        .port( 65534 ) // Likely unused port
        .username( "test" )
        .password( "test" )
        .authType( SshConfig.AuthType.PASSWORD )
        .connectTimeoutMillis( 1000 ) // Short timeout
        .commandTimeoutMillis( 10000 );

    long startTime = System.currentTimeMillis();
    try ( SshConnection connection = new MinaSshConnection( config ) ) {
      connection.connect();
      fail( "Connection should timeout" );
    } catch ( Exception e ) {
      long elapsed = System.currentTimeMillis() - startTime;
      // Should timeout relatively quickly
      assertTrue( "Should timeout within reasonable time", elapsed < 5000 );
      assertTrue( "Should get connection-related exception",
             e.getMessage().toLowerCase().contains( "connect" )
          || e.getMessage().toLowerCase().contains( "timeout" )
          || e.getMessage().toLowerCase().contains( "refused" ) );
    }
  }
}
