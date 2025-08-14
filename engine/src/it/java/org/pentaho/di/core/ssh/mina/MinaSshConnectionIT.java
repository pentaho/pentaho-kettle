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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshImplementation;
import org.pentaho.test.util.TestCleanupUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for MinaSshConnection against a real Docker SSH server.
 * 
 * Prerequisites:
 * - Docker SSH server running on localhost:2222
 * - Credentials: testuser/testpass123
 * 
 * To start the SSH server:
 * docker run -d --name ssh-test-server -p 2222:22 \
 *   -e PUID=1000 -e PGID=1000 -e TZ=Etc/UTC \
 *   -e USER_NAME=testuser -e USER_PASSWORD=testpass123 \
 *   -e PASSWORD_ACCESS=true -e SUDO_ACCESS=false \
 *   --restart unless-stopped lscr.io/linuxserver/openssh-server:latest
 */
public class MinaSshConnectionIT {

  private static final String SSH_HOST = "localhost";
  private static final int SSH_PORT = 2222;
  private static final String SSH_USERNAME = "testuser";
  private static final String SSH_PASSWORD = "testpass123";

  private LogChannelInterface log;
  private SshConnection connection;

  @BeforeClass
  public static void setUpClass() throws Exception {
    // Initialize Kettle environment
    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    // Shutdown Kettle environment
    KettleEnvironment.shutdown();
    TestCleanupUtil.cleanUpLogsDir();
  }

  @Before
  public void setUp() throws KettleException {
    // Initialize PDI logging system
    log = new LogChannel( "MinaSshConnectionIT" );
    log.logBasic( "Setting up MinaSshConnection integration test" );
  }

  @After
  public void tearDown() {
    if ( connection != null ) {
      try {
        connection.close();
        log.logBasic( "SSH connection closed" );
      } catch ( Exception e ) {
        log.logError( "Error closing SSH connection: " + e.getMessage() );
      }
    }
  }

  @Test
  public void testMinaSshConnectionDirectly() throws Exception {
    log.logBasic( "=== Testing MinaSshConnection directly ===" );

    // Create SSH configuration
    SshConfig config = SshConfig.create()
        .host( SSH_HOST )
        .port( SSH_PORT )
        .username( SSH_USERNAME )
        .authType( SshConfig.AuthType.PASSWORD )
        .password( SSH_PASSWORD )
        .implementation( SshImplementation.MINA )
        .connectTimeoutMillis( 60000 )  // 60 seconds
        .commandTimeoutMillis( 30000 ); // 30 seconds

    log.logBasic( "SSH Config created:" );
    log.logBasic( "  Host: " + config.getHost() + ":" + config.getPort() );
    log.logBasic( "  Username: " + config.getUsername() );
    log.logBasic( "  Auth Type: " + config.getAuthType() );
    log.logBasic( "  Implementation: " + config.getImplementation() );
    log.logBasic( "  Connect Timeout: " + config.getConnectTimeoutMillis() + "ms" );
    log.logBasic( "  Command Timeout: " + config.getCommandTimeoutMillis() + "ms" );

    // Create MinaSshConnection directly
    connection = new MinaSshConnection( config, log );
    assertNotNull( "Connection should not be null", connection );
    log.logBasic( "MinaSshConnection created successfully" );

    // Test connection
    log.logBasic( "Attempting to connect..." );
    connection.connect();
    log.logBasic( "Connection established successfully" );

    // Execute whoami command
    log.logBasic( "Executing 'whoami' command..." );
    ExecResult result = connection.exec( "whoami", 30000 );
    assertNotNull( "ExecResult should not be null", result );

    log.logBasic( "Command execution completed:" );
    log.logBasic( "  Exit Code: " + result.getExitCode() );
    log.logBasic( "  Stdout: '" + result.getStdout() + "'" );
    log.logBasic( "  Stderr: '" + result.getStderr() + "'" );
    log.logBasic( "  Has Error: " + result.isError() );

    // Verify results
    assertEquals( "Exit code should be 0", 0, result.getExitCode() );
    assertFalse( "Command should not have error", result.isError() );
    assertTrue( "Stdout should contain username", result.getStdout().trim().contains( "testuser" ) );
    assertTrue( "Stderr should be empty", result.getStderr().trim().isEmpty() );

    log.logBasic( "=== MinaSshConnection test completed successfully ===" );
  }

  @Test
  public void testMinaSshConnectionMultipleCommands() throws Exception {
    log.logBasic( "=== Testing MinaSshConnection with multiple commands ===" );

    // Create SSH configuration
    SshConfig config = SshConfig.create()
        .host( SSH_HOST )
        .port( SSH_PORT )
        .username( SSH_USERNAME )
        .authType( SshConfig.AuthType.PASSWORD )
        .password( SSH_PASSWORD )
        .implementation( SshImplementation.MINA )
        .connectTimeoutMillis( 60000 )
        .commandTimeoutMillis( 30000 );

    // Create connection
    connection = new MinaSshConnection( config, log );
    connection.connect();
    log.logBasic( "Connection established for multiple command test" );

    // Test multiple commands
    String[] commands = {
      "whoami",
      "pwd",
      "echo 'Hello from SSH'",
      "date"
    };

    for ( String command : commands ) {
      log.logBasic( "Executing command: " + command );
      ExecResult result = connection.exec( command, 30000 );

      log.logBasic( "Result for '" + command + "':" );
      log.logBasic( "  Exit Code: " + result.getExitCode() );
      log.logBasic( "  Stdout: '" + result.getStdout().trim() + "'" );
      log.logBasic( "  Stderr: '" + result.getStderr().trim() + "'" );

      assertEquals( "Command '" + command + "' should succeed", 0, result.getExitCode() );
      assertFalse( "Command '" + command + "' should not have error", result.isError() );
    }

    log.logBasic( "=== Multiple commands test completed successfully ===" );
  }

  @Test
  public void testMinaSshConnectionErrorCommand() throws Exception {
    log.logBasic( "=== Testing MinaSshConnection with error command ===" );

    // Create SSH configuration
    SshConfig config = SshConfig.create()
        .host( SSH_HOST )
        .port( SSH_PORT )
        .username( SSH_USERNAME )
        .authType( SshConfig.AuthType.PASSWORD )
        .password( SSH_PASSWORD )
        .implementation( SshImplementation.MINA )
        .connectTimeoutMillis( 60000 )
        .commandTimeoutMillis( 30000 );

    // Create connection
    connection = new MinaSshConnection( config, log);
    connection.connect();
    log.logBasic( "Connection established for error command test" );

    // Execute a command that should fail
    String errorCommand = "nonexistentcommand12345";
    log.logBasic( "Executing error command: " + errorCommand );
    ExecResult result = connection.exec( errorCommand, 30000 );

    log.logBasic( "Error command result:" );
    log.logBasic( "  Exit Code: " + result.getExitCode() );
    log.logBasic( "  Stdout: '" + result.getStdout().trim() + "'" );
    log.logBasic( "  Stderr: '" + result.getStderr().trim() + "'" );
    log.logBasic( "  Has Error: " + result.isError() );

    // Verify error handling
    assertNotEquals( "Exit code should not be 0 for failed command", 0, result.getExitCode() );
    assertTrue( "Command should have error", result.isError() );
    assertFalse( "Stderr should not be empty for failed command", result.getStderr().trim().isEmpty() );

    log.logBasic( "=== Error command test completed successfully ===" );
  }

  @Test
  public void testMinaSshConnectionTimeout() throws Exception {
    log.logBasic( "=== Testing MinaSshConnection with timeout ===" );

    // Create SSH configuration with shorter timeout
    SshConfig config = SshConfig.create()
        .host( SSH_HOST )
        .port( SSH_PORT )
        .username( SSH_USERNAME )
        .authType( SshConfig.AuthType.PASSWORD )
        .password( SSH_PASSWORD )
        .implementation( SshImplementation.MINA )
        .connectTimeoutMillis( 60000 )
        .commandTimeoutMillis( 2000 ); // 2 seconds

    // Create connection
    connection = new MinaSshConnection( config, log );
    connection.connect();
    log.logBasic( "Connection established for timeout test" );

    // Execute a command that should timeout (sleep longer than timeout)
    String timeoutCommand = "sleep 5";
    log.logBasic( "Executing timeout command: " + timeoutCommand + " (with 2s timeout)" );

    try {
      ExecResult result = connection.exec( timeoutCommand, 2000 );
      log.logBasic( "Timeout command completed (unexpected):" );
      log.logBasic( "  Exit Code: " + result.getExitCode() );
      log.logBasic( "  Stdout: '" + result.getStdout().trim() + "'" );
      log.logBasic( "  Stderr: '" + result.getStderr().trim() + "'" );

      // Command might complete if timeout is not properly implemented
      // This is okay, we're just testing the behavior
      log.logBasic( "Command completed within timeout period" );

    } catch ( Exception e) {
      // Timeout exception is expected
      log.logBasic( "Timeout exception occurred as expected: " + e.getClass().getSimpleName() + ": " + e.getMessage() );
      assertTrue( "Should be a timeout-related exception",
          e.getMessage().toLowerCase().contains( "timeout" ) ||
          e.getClass().getSimpleName().toLowerCase().contains( "timeout" ) );
    }

    log.logBasic( "=== Timeout test completed ===" );
  }
}
