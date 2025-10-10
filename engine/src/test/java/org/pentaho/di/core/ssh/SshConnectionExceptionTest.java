package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pentaho.di.core.ssh.exceptions.SshConnectionException;

/**
 * Unit tests for SshConnectionException class.
 * Tests all constructor variants and exception behavior.
 */
public class SshConnectionExceptionTest {

  @Test
  public void testDefaultConstructorWithMessage() {
    String message = "Connection failed";
    SshConnectionException exception = new SshConnectionException( message );

    assertEquals( "Message should match", message, exception.getMessage() );
    assertNull( "Cause should be null", exception.getCause() );
  }

  @Test
  public void testConstructorWithMessageAndCause() {
    String message = "Connection failed";
    RuntimeException cause = new RuntimeException( "Root cause" );
    SshConnectionException exception = new SshConnectionException( message, cause );

    assertEquals( "Message should match", message, exception.getMessage() );
    assertEquals( "Cause should match", cause, exception.getCause() );
  }

  @Test
  public void testConstructorWithCauseOnly() {
    RuntimeException cause = new RuntimeException( "Root cause" );
    SshConnectionException exception = new SshConnectionException( cause );

    assertEquals( "Cause should match", cause, exception.getCause() );
    // Message will be the cause's toString()
    assertTrue( "Message should contain cause info",
        exception.getMessage().contains( "Root cause" ) );
  }

  @Test
  public void testExceptionIsCheckedException() {
    // Verify that SshConnectionException is a checked exception
    assertTrue( "Should extend Exception", Exception.class.isAssignableFrom( SshConnectionException.class ) );
    assertFalse( "Should not extend RuntimeException",
        RuntimeException.class.isAssignableFrom( SshConnectionException.class ) );
  }

  @Test
  public void testSerializationSupport() {
    // Verify that the exception has serialVersionUID
    try {
        SshConnectionException.class.getDeclaredField( "serialVersionUID" );
        // If we get here, the field exists
    } catch ( NoSuchFieldException e ) {
        fail( "SshConnectionException should have serialVersionUID field" );
    }
  }

  @Test
  public void testExceptionThrowing() {
    try {
      throw new SshConnectionException( "Test exception" );
    } catch ( SshConnectionException e ) {
      assertEquals( "Exception message should match", "Test exception", e.getMessage() );
    }
  }
}
