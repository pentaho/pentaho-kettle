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
 * Exception thrown when SSH connection timeouts occur.
 * This includes connection establishment timeouts, operation timeouts, and session timeouts.
 */
public class SshTimeoutException extends SshConnectionException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new SSH timeout exception with the specified detail message.
   *
   * @param message the detail message
   */
  public SshTimeoutException( String message ) {
    super( message );
  }

  /**
   * Constructs a new SSH timeout exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of this exception
   */
  public SshTimeoutException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * Constructs a new SSH timeout exception with the specified cause.
   *
   * @param cause the cause of this exception
   */
  public SshTimeoutException( Throwable cause ) {
    super( cause );
  }
}
