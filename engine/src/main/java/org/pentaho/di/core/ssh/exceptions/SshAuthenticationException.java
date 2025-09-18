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

package org.pentaho.di.core.ssh.exceptions;

/**
 * Exception thrown when SSH authentication fails.
 * This includes invalid credentials, key authentication failures, and permission errors.
 */
public class SshAuthenticationException extends SshConnectionException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new SSH authentication exception with the specified detail message.
   *
   * @param message the detail message
   */
  public SshAuthenticationException( String message ) {
    super( message );
  }

  /**
   * Constructs a new SSH authentication exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of this exception
   */
  public SshAuthenticationException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * Constructs a new SSH authentication exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public SshAuthenticationException( Throwable cause ) {
    super( cause );
  }
}
