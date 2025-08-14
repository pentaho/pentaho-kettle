package org.pentaho.di.core.ssh;

/**
 * Exception thrown when SSH connection operations fail.
 * This includes connection establishment, authentication, and configuration errors.
 */
public class SshConnectionException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new SSH connection exception with the specified detail message.
   *
   * @param message the detail message
   */
  public SshConnectionException( String message ) {
    super( message );
  }

  /**
   * Constructs a new SSH connection exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of this exception
   */
  public SshConnectionException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * Constructs a new SSH connection exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public SshConnectionException( Throwable cause ) {
    super( cause );
  }
}
