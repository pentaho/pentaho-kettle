/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.core.ssh.mina;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.apache.sshd.client.future.ConnectFuture;
import org.junit.Test;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;

/**
 * Additional unit tests for MinaSshConnection edge cases and configurations.
 * Complements the existing MinaSshConnectionTest and AbstractSshConnectionContractTest.
 */
public class MinaSshConnectionEdgeCasesTest {

  @Test
  public void testConnectionWithMinimalConfig() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithPublicKeyAuth() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyPath( Path.of( "/tmp/fake-key" ) )
      .passphrase( "passphrase" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithKeyContent() {
    byte[] keyContent = "fake-key-content".getBytes();
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyContent( keyContent )
      .passphrase( "passphrase" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithProxy() {
    SshConfig config = SshConfig.create()
      .host( "internal.server" )
      .username( "user" )
      .password( "pass" )
      .proxy( "proxy.example.com", 8080 );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithProxyAuth() {
    SshConfig config = SshConfig.create()
      .host( "internal.server" )
      .username( "user" )
      .password( "pass" )
      .proxy( "proxy.example.com", 8080 )
      .proxyAuth( "proxyuser", "proxypass" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithTimeouts() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .connectTimeoutMillis( 10000 )
      .commandTimeoutMillis( 30000 );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithCustomPort() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .port( 2222 )
      .username( "user" )
      .password( "pass" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithKnownHostsFile() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .knownHostsFile( "/home/user/.ssh/known_hosts" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionWithCacheHostKey() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .cacheHostKey( true );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
  }

  @Test
  public void testConnectionClosedBeforeConnect() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    MinaSshConnection connection = new MinaSshConnection( config );
    assertNotNull( connection );
    // Should not throw exception when closing before connecting
    connection.close();
    // If we get here without exception, the test passed
    assertNotNull( "Connection should still exist after close", connection );
  }

  @Test
  public void testMultipleCloseCallsAreSafe() {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    try ( MinaSshConnection connection = new MinaSshConnection( config ) ) {
      assertNotNull( connection );
      // Multiple close calls should be safe (idempotent operation)
      connection.close();
      connection.close();
      connection.close();
      // If we get here without exception, multiple close calls are safe
      assertNotNull( "Connection should still exist after multiple closes", connection );
    }
  }

  @Test
  public void testConnectionWithAllOptions() {
    SshConfig config = SshConfig.create()
      .host( "ssh.example.com" )
      .port( 2222 )
      .username( "admin" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyPath( Path.of( "/home/user/.ssh/id_rsa" ) )
      .passphrase( "keypass" )
      .proxy( "proxy.corp.com", 3128 )
      .proxyAuth( "proxyuser", "proxypass" )
      .knownHostsFile( "/home/user/.ssh/known_hosts" )
      .cacheHostKey( true )
      .connectTimeoutMillis( 15000 )
      .commandTimeoutMillis( 45000 );

    try ( MinaSshConnection connection = new MinaSshConnection( config ) ) {
      assertNotNull( connection );
    }
  }

  @Test( expected = SshConnectionException.class )
  public void testExecBeforeConnect() throws SshConnectionException {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    try ( MinaSshConnection connection = new MinaSshConnection( config ) ) {
      // Should throw exception when trying to exec without connecting first
      connection.exec( "echo test", 5000 );
    }
  }

  @Test( expected = SshConnectionException.class )
  public void testOpenSftpBeforeConnect() throws SshConnectionException {
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    try ( MinaSshConnection connection = new MinaSshConnection( config ) ) {
      // Should throw exception when trying to open SFTP without connecting first
      connection.openSftp();
    }
  }

  // ---------------------------------------------------------------------------
  // PDI-20898: timeout=0 (and negative) must mean "infinite" throughout the
  // MINA connection layer.  In MINA SSHD 2.x, await(0L) returns immediately
  // and await(negative) throws; the no-arg await() is the correct infinite-wait
  // call and must be used whenever connectTimeoutMillis is non-positive.
  // ---------------------------------------------------------------------------

  @Test
  public void testWaitForConnectionWithZeroTimeoutWaitsIndefinitely() throws Exception {
    // connectTimeoutMillis=0 means "no timeout". In MINA SSHD 2.x, await(0L) returns
    // immediately (non-blocking poll), so we must call the no-arg await() instead.
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .connectTimeoutMillis( 0L );

    ConnectFuture mockFuture = mock( ConnectFuture.class );
    when( mockFuture.await() ).thenReturn( true );
    when( mockFuture.isConnected() ).thenReturn( true );

    invokeWaitForConnection( new MinaSshConnection( config ), mockFuture );

    verify( mockFuture ).await();
    verify( mockFuture, never() ).await( 0L );
  }

  @Test
  public void testWaitForConnectionWithNegativeTimeoutWaitsIndefinitely() throws Exception {
    // Negative timeout is also treated as "no timeout". In MINA SSHD 2.x, await(negative)
    // throws an IllegalArgumentException, so we must call the no-arg await() instead.
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .connectTimeoutMillis( -1L );

    ConnectFuture mockFuture = mock( ConnectFuture.class );
    when( mockFuture.await() ).thenReturn( true );
    when( mockFuture.isConnected() ).thenReturn( true );

    invokeWaitForConnection( new MinaSshConnection( config ), mockFuture );

    verify( mockFuture ).await();
    verify( mockFuture, never() ).await( -1L );
  }

  @Test
  public void testWaitForConnectionWithPositiveTimeoutPassesItDirectlyToMina() throws Exception {
    // Positive timeout is forwarded unchanged to MINA.
    long customTimeout = 15_000L;
    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" )
      .connectTimeoutMillis( customTimeout );

    ConnectFuture mockFuture = mock( ConnectFuture.class );
    when( mockFuture.await( customTimeout ) ).thenReturn( true );
    when( mockFuture.isConnected() ).thenReturn( true );

    invokeWaitForConnection( new MinaSshConnection( config ), mockFuture );

    verify( mockFuture ).await( customTimeout );
    verify( mockFuture, never() ).await( 0L );
  }

  /** Reflectively calls the private {@code waitForConnection} method. */
  private static void invokeWaitForConnection( MinaSshConnection conn, ConnectFuture future )
      throws Exception {
    Method m = MinaSshConnection.class.getDeclaredMethod( "waitForConnection", ConnectFuture.class );
    m.setAccessible( true );
    m.invoke( conn, future );
  }
}
