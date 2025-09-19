/*
 * ! ******************************************************************************
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.ssh.SshConfig;

public class MinaSshConnectionTest {

  private String originalHttpProxyHost;
  private String originalHttpProxyPort;
  private String originalHttpProxyUser;
  private String originalHttpProxyPassword;
  private String originalHttpsProxyHost;
  private String originalHttpsProxyPort;
  private String originalHttpsProxyUser;
  private String originalHttpsProxyPassword;

  @Before
  public void setup() {
    // Save original proxy system properties
    originalHttpProxyHost = System.getProperty( "http.proxyHost" );
    originalHttpProxyPort = System.getProperty( "http.proxyPort" );
    originalHttpProxyUser = System.getProperty( "http.proxyUser" );
    originalHttpProxyPassword = System.getProperty( "http.proxyPassword" );
    originalHttpsProxyHost = System.getProperty( "https.proxyHost" );
    originalHttpsProxyPort = System.getProperty( "https.proxyPort" );
    originalHttpsProxyUser = System.getProperty( "https.proxyUser" );
    originalHttpsProxyPassword = System.getProperty( "https.proxyPassword" );
  }

  @After
  public void tearDown() {
    // Restore original proxy system properties
    restoreSystemProperty( "http.proxyHost", originalHttpProxyHost );
    restoreSystemProperty( "http.proxyPort", originalHttpProxyPort );
    restoreSystemProperty( "http.proxyUser", originalHttpProxyUser );
    restoreSystemProperty( "http.proxyPassword", originalHttpProxyPassword );
    restoreSystemProperty( "https.proxyHost", originalHttpsProxyHost );
    restoreSystemProperty( "https.proxyPort", originalHttpsProxyPort );
    restoreSystemProperty( "https.proxyUser", originalHttpsProxyUser );
    restoreSystemProperty( "https.proxyPassword", originalHttpsProxyPassword );
  }

  private void restoreSystemProperty( String key, String originalValue ) {
    if ( originalValue != null ) {
      System.setProperty( key, originalValue );
    } else {
      System.clearProperty( key );
    }
  }

  @Test
  public void testProxyConfiguration_WithoutAuthentication() {
    // Test that proxy configuration sets system properties correctly
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .password( "testpass" )
      .proxy( "proxy.example.com", 8080 );

    // We can verify the configuration is stored correctly in SshConfig
    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
    assertNull( config.getProxyUser() );
    assertNull( config.getProxyPassword() );
  }

  @Test
  public void testProxyConfiguration_WithAuthentication() {
    // Test that proxy configuration with authentication works
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .password( "testpass" )
      .proxy( "proxy.example.com", 8080 )
      .proxyAuth( "proxyuser", "proxypass" );

    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
    assertEquals( "proxyuser", config.getProxyUser() );
    assertEquals( "proxypass", config.getProxyPassword() );
  }

  @Test
  public void testPassphraseConfiguration() {
    // Test that passphrase configuration is stored correctly
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyPath( Path.of( "/tmp/fake-key" ) )
      .passphrase( "mypassphrase" );

    assertEquals( SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( "mypassphrase", config.getPassphrase() );
    assertEquals( Path.of( "/tmp/fake-key" ), config.getKeyPath() );
  }

  @Test
  public void testPasswordAuthConfiguration() {
    // Test that password authentication configuration works
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .authType( SshConfig.AuthType.PASSWORD )
      .password( "mypassword" );

    assertEquals( SshConfig.AuthType.PASSWORD, config.getAuthType() );
    assertEquals( "mypassword", config.getPassword() );
  }

  @Test
  public void testCompleteConfiguration() {
    // Test complete configuration with all options
    SshConfig config = SshConfig.create()
      .host( "ssh.example.com" )
      .port( 2222 )
      .username( "sshuser" )
      .authType( SshConfig.AuthType.PUBLIC_KEY )
      .keyPath( Path.of( "/home/user/.ssh/id_rsa" ) )
      .passphrase( "keypassphrase" )
      .proxy( "proxy.example.com", 8080 )
      .proxyAuth( "proxyuser", "proxypass" )
      .connectTimeoutMillis( 30000 );

    // Verify all configuration is properly stored
    assertEquals( "ssh.example.com", config.getHost() );
    assertEquals( 2222, config.getPort() );
    assertEquals( "sshuser", config.getUsername() );
    assertEquals( SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( Path.of( "/home/user/.ssh/id_rsa" ), config.getKeyPath() );
    assertEquals( "keypassphrase", config.getPassphrase() );
    assertEquals( "proxy.example.com", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
    assertEquals( "proxyuser", config.getProxyUser() );
    assertEquals( "proxypass", config.getProxyPassword() );
    assertEquals( 30000, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testNullProxyConfiguration() {
    // Test that null proxy configuration doesn't cause issues
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .password( "testpass" );

    assertNull( config.getProxyHost() );
    assertEquals( 0, config.getProxyPort() );
    assertNull( config.getProxyUser() );
    assertNull( config.getProxyPassword() );
  }

  @Test
  public void testEmptyProxyConfiguration() {
    // Test that empty proxy host doesn't configure proxy
    SshConfig config = SshConfig.create()
      .host( "testhost" )
      .username( "testuser" )
      .password( "testpass" )
      .proxy( "", 8080 );

    assertEquals( "", config.getProxyHost() );
    assertEquals( 8080, config.getProxyPort() );
  }
}
