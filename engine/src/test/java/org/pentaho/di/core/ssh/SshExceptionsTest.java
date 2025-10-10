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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.ssh.exceptions.SftpException;
import org.pentaho.di.core.ssh.exceptions.SshAuthenticationException;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;
import org.pentaho.di.core.ssh.exceptions.SshTimeoutException;

/**
 * Unit tests for SSH exception classes not covered by SshExceptionHierarchyTest.
 * Tests specific constructors and exception scenarios.
 */
public class SshExceptionsTest {

  @Test
  public void testSshAuthenticationExceptionWithMessage() {
    String message = "Authentication failed for user";
    SshAuthenticationException exception = new SshAuthenticationException( message );

    assertEquals( message, exception.getMessage() );
    assertNull( exception.getCause() );
  }

  @Test
  public void testSshAuthenticationExceptionWithMessageAndCause() {
    String message = "Auth failed";
    RuntimeException cause = new RuntimeException( "Invalid credentials" );
    SshAuthenticationException exception = new SshAuthenticationException( message, cause );

    assertEquals( message, exception.getMessage() );
    assertEquals( cause, exception.getCause() );
  }

  @Test
  public void testSshAuthenticationExceptionWithCause() {
    RuntimeException cause = new RuntimeException( "Key rejected" );
    SshAuthenticationException exception = new SshAuthenticationException( cause );

    assertEquals( cause, exception.getCause() );
    assertTrue( exception.getMessage().contains( "Key rejected" ) );
  }

  @Test
  public void testSshTimeoutExceptionWithMessage() {
    String message = "Connection timed out after 30s";
    SshTimeoutException exception = new SshTimeoutException( message );

    assertEquals( message, exception.getMessage() );
    assertNull( exception.getCause() );
  }

  @Test
  public void testSshTimeoutExceptionWithMessageAndCause() {
    String message = "Timeout occurred";
    RuntimeException cause = new RuntimeException( "Network unreachable" );
    SshTimeoutException exception = new SshTimeoutException( message, cause );

    assertEquals( message, exception.getMessage() );
    assertEquals( cause, exception.getCause() );
  }

  @Test
  public void testSshTimeoutExceptionWithCause() {
    RuntimeException cause = new RuntimeException( "Socket timeout" );
    SshTimeoutException exception = new SshTimeoutException( cause );

    assertEquals( cause, exception.getCause() );
    assertTrue( exception.getMessage().contains( "Socket timeout" ) );
  }

  @Test
  public void testSftpExceptionWithMessage() {
    String message = "Failed to upload file";
    SftpException exception = new SftpException( message );

    assertEquals( message, exception.getMessage() );
    assertNull( exception.getCause() );
  }

  @Test
  public void testSftpExceptionWithMessageAndCause() {
    String message = "SFTP operation failed";
    RuntimeException cause = new RuntimeException( "Permission denied" );
    SftpException exception = new SftpException( message, cause );

    assertEquals( message, exception.getMessage() );
    assertEquals( cause, exception.getCause() );
  }

  @Test
  public void testSftpExceptionWithCause() {
    RuntimeException cause = new RuntimeException( "File not found" );
    SftpException exception = new SftpException( cause );

    assertEquals( cause, exception.getCause() );
    assertTrue( exception.getMessage().contains( "File not found" ) );
  }

  @Test
  public void testExceptionCanBeCaught() {
    try {
      throw new SshAuthenticationException( "Auth error" );
    } catch ( SshAuthenticationException e ) {
      assertEquals( "Auth error", e.getMessage() );
    }
  }

  @Test
  public void testExceptionCanBeCaughtAsParent() {
    try {
      throw new SshTimeoutException( "Timeout error" );
    } catch ( SshConnectionException e ) {
      assertTrue( e instanceof SshTimeoutException );
      assertEquals( "Timeout error", e.getMessage() );
    }
  }

  @Test
  public void testSftpExceptionCanBeCaughtAsParent() {
    try {
      throw new SftpException( "SFTP error" );
    } catch ( SshConnectionException e ) {
      assertTrue( e instanceof SftpException );
      assertEquals( "SFTP error", e.getMessage() );
    }
  }
}
