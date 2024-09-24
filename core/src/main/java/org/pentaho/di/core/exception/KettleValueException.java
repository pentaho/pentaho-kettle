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

package org.pentaho.di.core.exception;

/**
 * This exception is used in value calculations.
 *
 * @author Matt
 * @since 30-MAR-2004
 *
 */
public class KettleValueException extends KettleException {
  public static final long serialVersionUID = 0x8D8EA0264F7A1C18L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public KettleValueException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message
   *          - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public KettleValueException( String message ) {
    super( message );
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause
   *          the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public KettleValueException( Throwable cause ) {
    super( cause );
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param message
   *          the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause
   *          the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public KettleValueException( String message, Throwable cause ) {
    super( message, cause );
  }

}
