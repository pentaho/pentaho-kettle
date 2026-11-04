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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.ConfigFileHostEntryResolver;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.TestSshServer;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;

/**
 * BISERVER-15807: the SSH step must not read the ~/.ssh/config file of the OS user.
 * A line such as "Host \*" is valid for OpenSSH, but Apache MINA SSHD rejects it
 * with "Invalid host pattern char in \*" before the socket opens.
 * MINA SSHD reads user.home one time for each JVM. The surefire execution
 * "ssh-host-config-test" in engine/pom.xml runs this class in its own JVM and sets
 * the system property {@value #ISOLATED_PROPERTY}. If that property is set and the
 * temporary home is not in use, the tests fail. In a shared JVM (for example an IDE run
 * after other SSH tests) the tests skip.
 */
public class MinaSshConnectionHostConfigTest {

  static final String ISOLATED_PROPERTY = "ssh.hostconfig.isolated";

  @ClassRule
  public static TemporaryFolder tempHome = new TemporaryFolder();

  private static String originalUserHome;
  private static TestSshServer server;
  private static int port;

  @BeforeClass
  public static void setUp() throws Exception {
    originalUserHome = System.getProperty( "user.home" );
    File sshDir = tempHome.newFolder( ".ssh" );
    // The backslash before the asterisk is the cause of the bug.
    Files.write( new File( sshDir, "config" ).toPath(),
      "Host \\*\n  ServerAliveInterval 60\n".getBytes( StandardCharsets.UTF_8 ) );
    System.setProperty( "user.home", tempHome.getRoot().getAbsolutePath() );

    server = new TestSshServer();
    server.start( 0 );
    port = server.getAssignedPort();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if ( server != null ) {
      server.stop();
    }
    if ( originalUserHome != null ) {
      System.setProperty( "user.home", originalUserHome );
    } else {
      System.clearProperty( "user.home" );
    }
  }

  private static SshConfig config() {
    return SshConfig.create()
      .host( "127.0.0.1" )
      .port( port )
      .username( "test" )
      .password( "test" )
      .connectTimeoutMillis( 10000 );
  }

  /** Makes sure that MINA SSHD reads the malformed config file of this test. */
  private static void requireTempHomeInUse() throws Exception {
    Path defaultConfig = HostConfigEntry.getDefaultHostConfigFile();
    boolean tempHomeInUse = defaultConfig.startsWith( tempHome.getRoot().toPath().toRealPath() )
      || defaultConfig.startsWith( tempHome.getRoot().toPath() );
    String message = "MINA SSHD read user.home before this test. Run the class in its own JVM. Config file: "
      + defaultConfig;
    if ( Boolean.getBoolean( ISOLATED_PROPERTY ) ) {
      assertTrue( message, tempHomeInUse );
    } else {
      assumeTrue( message, tempHomeInUse );
    }
  }

  @Test
  public void hostConfigWithBadPatternDoesNotBreakConnect() throws Exception {
    requireTempHomeInUse();

    MinaSshConnection connection = new MinaSshConnection( config() );
    try {
      connection.connect();
    } catch ( SshConnectionException e ) {
      fail( "connect() must not read ~/.ssh/config of the OS user: " + e.getMessage() );
    } finally {
      connection.close();
    }
  }

  @Test
  public void clientDoesNotUseOpenSshConfigFile() throws Exception {
    requireTempHomeInUse();

    MinaSshConnection connection = new MinaSshConnection( config() );
    try {
      connection.connect();
      Field clientField = MinaSshConnection.class.getDeclaredField( "client" );
      clientField.setAccessible( true );
      SshClient client = (SshClient) clientField.get( connection );
      assertNotNull( client );
      assertFalse( "The client must not use a config file resolver. Found: " + client.getHostConfigEntryResolver(),
        client.getHostConfigEntryResolver() instanceof ConfigFileHostEntryResolver );
    } catch ( SshConnectionException e ) {
      fail( "connect() must not read ~/.ssh/config of the OS user: " + e.getMessage() );
    } finally {
      connection.close();
    }
  }
}
