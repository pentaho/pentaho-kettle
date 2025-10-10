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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * Unit tests for SshConfig builder pattern.
 * Tests all builder methods, getters, and edge cases.
 */
public class SshConfigTest {

  @Test
  public void testCreateFactory() {
    SshConfig config = SshConfig.create();
    assertNotNull( config );
  }

  @Test
  public void testHostConfiguration() {
    SshConfig config = SshConfig.create().host( "example.com" );
    assertEquals( "example.com", config.getHost() );
  }

  @Test
  public void testPortConfiguration() {
    SshConfig config = SshConfig.create().port( 2222 );
    assertEquals( 2222, config.getPort() );
  }

  @Test
  public void testDefaultPort() {
    SshConfig config = SshConfig.create();
    assertEquals( 22, config.getPort() );
  }

  @Test
  public void testUsernameConfiguration() {
    SshConfig config = SshConfig.create().username( "testuser" );
    assertEquals( "testuser", config.getUsername() );
  }

  @Test
  public void testPasswordConfiguration() {
    SshConfig config = SshConfig.create().password( "secret123" );
    assertEquals( "secret123", config.getPassword() );
  }

  @Test
  public void testAuthTypeConfiguration() {
    SshConfig config = SshConfig.create().authType( SshConfig.AuthType.PUBLIC_KEY );
    assertEquals( SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
  }

  @Test
  public void testDefaultAuthType() {
    SshConfig config = SshConfig.create();
    assertEquals( SshConfig.AuthType.PASSWORD, config.getAuthType() );
  }

  @Test
  public void testKeyPathConfiguration() {
    Path keyPath = Path.of( "/home/user/.ssh/id_rsa" );
    SshConfig config = SshConfig.create().keyPath( keyPath );

    assertEquals( keyPath, config.getKeyPath() );
    assertNull( "keyContent should be null when keyPath is set", config.getKeyContent() );
  }

  @Test
  public void testKeyContentConfiguration() {
    byte[] keyContent = "fake-key-content".getBytes();
    SshConfig config = SshConfig.create().keyContent( keyContent );

    assertArrayEquals( keyContent, config.getKeyContent() );
    assertNull( "keyPath should be null when keyContent is set", config.getKeyPath() );
  }

  @Test
  public void testKeyContentAndPathMutualExclusion() {
    byte[] keyContent = "content".getBytes();
    Path keyPath = Path.of( "/path/to/key" );

    // Set keyPath first, then keyContent - keyPath should be cleared
    SshConfig config1 = SshConfig.create().keyPath( keyPath ).keyContent( keyContent );
    assertArrayEquals( keyContent, config1.getKeyContent() );
    assertNull( config1.getKeyPath() );

    // Set keyContent first, then keyPath - keyContent should be cleared
    SshConfig config2 = SshConfig.create().keyContent( keyContent ).keyPath( keyPath );
    assertEquals( keyPath, config2.getKeyPath() );
    assertNull( config2.getKeyContent() );
  }

  @Test
  public void testKeyContentIsCopied() {
    byte[] original = "original".getBytes();
    SshConfig config = SshConfig.create().keyContent( original );

    // Modify original array
    original[0] = 'X';

    // Config should have unchanged copy
    byte[] retrieved = config.getKeyContent();
    assertEquals( 'o', retrieved[0] );
  }

  @Test
  public void testNullKeyContent() {
    SshConfig config = SshConfig.create().keyContent( null );
    assertNull( config.getKeyContent() );
  }

  @Test
  public void testPassphraseConfiguration() {
    SshConfig config = SshConfig.create().passphrase( "mypassphrase" );
    assertEquals( "mypassphrase", config.getPassphrase() );
  }

  @Test
  public void testProxyConfiguration() {
    SshConfig config = SshConfig.create().proxy( "proxy.example.com", 8080 );

    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
  }

  @Test
  public void testProxyAuthConfiguration() {
    SshConfig config = SshConfig.create()
      .proxy( "proxy.example.com", 8080 )
      .proxyAuth( "proxyuser", "proxypass" );

    assertEquals( "proxyuser", config.getProxyUser() );
    assertEquals( "proxypass", config.getProxyPassword() );
  }

  @Test
  public void testKnownHostsFileConfiguration() {
    SshConfig config = SshConfig.create().knownHostsFile( "/home/user/.ssh/known_hosts" );
    assertEquals( "/home/user/.ssh/known_hosts", config.getKnownHostsFile() );
  }

  @Test
  public void testCacheHostKeyConfiguration() {
    SshConfig config = SshConfig.create().cacheHostKey( true );
    assertTrue( config.isCacheHostKey() );

    config = SshConfig.create().cacheHostKey( false );
    assertFalse( config.isCacheHostKey() );
  }

  @Test
  public void testConnectTimeoutConfiguration() {
    SshConfig config = SshConfig.create().connectTimeoutMillis( 30000 );
    assertEquals( 30000, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testCommandTimeoutConfiguration() {
    SshConfig config = SshConfig.create().commandTimeoutMillis( 60000 );
    assertEquals( 60000, config.getCommandTimeoutMillis() );
  }

  @Test
  public void testLogChannelConfiguration() {
    LogChannelInterface log = new LogChannel( "test" );
    SshConfig config = SshConfig.create().logChannel( log );
    assertEquals( log, config.getLogChannel() );
  }

  @Test
  public void testBuilderChaining() {
    SshConfig config = SshConfig.create()
      .host( "ssh.example.com" )
      .port( 2222 )
      .username( "user" )
      .password( "pass" )
      .connectTimeoutMillis( 10000 );

    assertEquals( "ssh.example.com", config.getHost() );
    assertEquals( 2222, config.getPort() );
    assertEquals( "user", config.getUsername() );
    assertEquals( "pass", config.getPassword() );
    assertEquals( 10000, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testCompletePasswordAuthConfiguration() {
    SshConfig config = SshConfig.create()
      .host( "server.example.com" )
      .port( 22 )
      .username( "admin" )
      .authType( SshConfig.AuthType.PASSWORD )
      .password( "secret" )
      .connectTimeoutMillis( 5000 )
      .commandTimeoutMillis( 30000 );

    assertEquals( "server.example.com", config.getHost() );
    assertEquals( 22, config.getPort() );
    assertEquals( "admin", config.getUsername() );
    assertEquals( SshConfig.AuthType.PASSWORD, config.getAuthType() );
    assertEquals( "secret", config.getPassword() );
    assertEquals( 5000, config.getConnectTimeoutMillis() );
    assertEquals( 30000, config.getCommandTimeoutMillis() );
  }

  @Test
  public void testCompletePublicKeyAuthConfiguration() {
    Path keyPath = Path.of( "/home/user/.ssh/id_rsa" );
    SshConfig config = SshConfig.create()
      .host( "git.example.com" )
      .port( 22 )
      .username( "git" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyPath( keyPath )
      .passphrase( "keypass" );

    assertEquals( "git.example.com", config.getHost() );
    assertEquals( "git", config.getUsername() );
    assertEquals( SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( keyPath, config.getKeyPath() );
    assertEquals( "keypass", config.getPassphrase() );
  }

  @Test
  public void testProxyWithAuthConfiguration() {
    SshConfig config = SshConfig.create()
      .host( "internal.server" )
      .username( "user" )
      .password( "pass" )
      .proxy( "corporate.proxy", 3128 )
      .proxyAuth( "corpuser", "corppass" );

    assertEquals( "internal.server", config.getHost() );
    assertEquals( "corporate.proxy", config.getProxyHost() );
    assertEquals( 3128, config.getProxyPort() );
    assertEquals( "corpuser", config.getProxyUser() );
    assertEquals( "corppass", config.getProxyPassword() );
  }

  @Test
  public void testNullValues() {
    SshConfig config = SshConfig.create();

    assertNull( config.getHost() );
    assertNull( config.getUsername() );
    assertNull( config.getPassword() );
    assertNull( config.getKeyPath() );
    assertNull( config.getKeyContent() );
    assertNull( config.getPassphrase() );
    assertNull( config.getProxyHost() );
    assertNull( config.getProxyUser() );
    assertNull( config.getProxyPassword() );
    assertNull( config.getKnownHostsFile() );
    assertNull( config.getLogChannel() );
  }

  @Test
  public void testDefaultValues() {
    SshConfig config = SshConfig.create();

    assertEquals( 22, config.getPort() );
    assertEquals( 0, config.getProxyPort() );
    assertEquals( SshConfig.AuthType.PASSWORD, config.getAuthType() );
    assertFalse( config.isCacheHostKey() );
    assertEquals( 0, config.getConnectTimeoutMillis() );
    assertEquals( 0, config.getCommandTimeoutMillis() );
  }

  @Test
  public void testEmptyStringValues() {
    SshConfig config = SshConfig.create()
      .host( "" )
      .username( "" )
      .password( "" );

    assertEquals( "", config.getHost() );
    assertEquals( "", config.getUsername() );
    assertEquals( "", config.getPassword() );
  }
}
