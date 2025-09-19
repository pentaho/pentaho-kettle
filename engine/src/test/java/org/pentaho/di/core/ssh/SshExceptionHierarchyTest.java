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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.ssh.exceptions.SftpException;
import org.pentaho.di.core.ssh.exceptions.SshAuthenticationException;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;
import org.pentaho.di.core.ssh.exceptions.SshTimeoutException;

/**
 * Test the SSH exception hierarchy to ensure proper inheritance and construction.
 */
public class SshExceptionHierarchyTest {

  @Test
  public void testSshAuthenticationExceptionInheritance() {
    SshAuthenticationException exception = new SshAuthenticationException( "Authentication failed" );

    // Should be an instance of the base exception
    assertTrue( "SshAuthenticationException should extend SshConnectionException",
                exception instanceof SshConnectionException );

    assertEquals( "Authentication failed", exception.getMessage() );
  }

  @Test
  public void testSshTimeoutExceptionInheritance() {
    SshTimeoutException exception = new SshTimeoutException( "Connection timed out" );

    // Should be an instance of the base exception
    assertTrue( "SshTimeoutException should extend SshConnectionException",
                exception instanceof SshConnectionException );

    assertEquals( "Connection timed out", exception.getMessage() );
  }

  @Test
  public void testSftpExceptionInheritance() {
    SftpException exception = new SftpException( "SFTP operation failed" );

    // Should be an instance of the base exception
    assertTrue( "SftpException should extend SshConnectionException",
                exception instanceof SshConnectionException );

    assertEquals( "SFTP operation failed", exception.getMessage() );
  }

  @Test
  public void testExceptionWithCause() {
    Exception cause = new RuntimeException( "Root cause" );
    SshAuthenticationException exception = new SshAuthenticationException( "Auth failed", cause );

    assertEquals( "Auth failed", exception.getMessage() );
    assertEquals( cause, exception.getCause() );
  }

  @Test
  public void testAllExceptionsAreSerializable() {
    // Verify all exceptions have serialVersionUID and extend Exception (thus Serializable)
    assertTrue( "SshConnectionException should be serializable",
                java.io.Serializable.class.isAssignableFrom( SshConnectionException.class ) );
    assertTrue( "SshAuthenticationException should be serializable",
                java.io.Serializable.class.isAssignableFrom( SshAuthenticationException.class ) );
    assertTrue( "SshTimeoutException should be serializable",
                java.io.Serializable.class.isAssignableFrom( SshTimeoutException.class ) );
    assertTrue( "SftpException should be serializable",
                java.io.Serializable.class.isAssignableFrom( SftpException.class ) );
  }
}
