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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;
import org.pentaho.di.core.ssh.mina.MinaSshConnection;

/**
 * Unit tests for SshConnectionFactory.
 * Tests the default factory implementation and custom factory scenarios.
 */
public class SshConnectionFactoryTest {

  @Test
  public void testDefaultFactory() {
    SshConnectionFactory factory = SshConnectionFactory.defaultFactory();
    assertNotNull( factory );
  }

  @Test
  public void testDefaultFactoryCreatesConnection() throws SshConnectionException {
    SshConnectionFactory factory = SshConnectionFactory.defaultFactory();

    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    SshConnection connection = factory.open( config );

    assertNotNull( connection );
    assertTrue( "Default factory should create MinaSshConnection",
                connection instanceof MinaSshConnection );
  }

  @Test
  public void testDefaultFactoryReturnsNewInstances() throws SshConnectionException {
    SshConnectionFactory factory = SshConnectionFactory.defaultFactory();

    SshConfig config = SshConfig.create()
      .host( "example.com" )
      .username( "user" )
      .password( "pass" );

    SshConnection conn1 = factory.open( config );
    SshConnection conn2 = factory.open( config );

    assertNotNull( conn1 );
    assertNotNull( conn2 );
    assertNotSame( "Factory should create new instances", conn1, conn2 );
  }

  @Test
  public void testCustomFactoryImplementation() throws SshConnectionException {
    // Create a custom factory that returns a mock connection
    SshConnectionFactory customFactory = new SshConnectionFactory() {
      @Override
      public SshConnection open( SshConfig config ) {
        return new MockSshConnection();
      }
    };

    SshConfig config = SshConfig.create()
      .host( "test.com" )
      .username( "testuser" );

    SshConnection connection = customFactory.open( config );

    assertNotNull( connection );
    assertTrue( "Custom factory should create MockSshConnection",
                connection instanceof MockSshConnection );
  }

  @Test
  public void testFactoryWithDifferentConfigs() throws SshConnectionException {
    SshConnectionFactory factory = SshConnectionFactory.defaultFactory();

    SshConfig config1 = SshConfig.create()
      .host( "server1.com" )
      .username( "user1" );

    SshConfig config2 = SshConfig.create()
      .host( "server2.com" )
      .username( "user2" );

    SshConnection conn1 = factory.open( config1 );
    SshConnection conn2 = factory.open( config2 );

    assertNotNull( conn1 );
    assertNotNull( conn2 );
  }

  /**
   * Mock SSH connection for testing purposes
   */
  private static class MockSshConnection implements SshConnection {
    @Override
    public void connect() throws SshConnectionException {
      // Mock implementation
    }

    @Override
    public ExecResult exec( String command, long timeoutMs ) throws SshConnectionException {
      return new ExecResult( "mock output", "", 0 );
    }

    @Override
    public SftpSession openSftp() throws SshConnectionException {
      return null; // Mock implementation
    }

    @Override
    public void close() {
      // Mock implementation
    }
  }
}
