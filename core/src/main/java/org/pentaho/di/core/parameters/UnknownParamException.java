/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.parameters;

/**
 * This is a PDI Exception for the named parameters.
 *
 * @author Sven Boden
 * @since 28Feb2009
 */
public class UnknownParamException extends NamedParamsException {
  private static final long serialVersionUID = -4447368601975248474L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public UnknownParamException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public UnknownParamException( String message ) {
    super( message );
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
   *              and indicates that the cause is nonexistent or unknown.)
   */
  public UnknownParamException( Throwable cause ) {
    super( cause );
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is
   *                permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public UnknownParamException( String message, Throwable cause ) {
    super( message, cause );
  }
}
