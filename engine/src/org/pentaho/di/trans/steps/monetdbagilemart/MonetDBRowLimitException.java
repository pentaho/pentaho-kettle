/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

package org.pentaho.di.trans.steps.monetdbagilemart;

import org.pentaho.di.core.exception.KettleException;

/**
 * Custom exception to indicate that the row limit has been reached
 * User: RFellows
 * Date: 10/31/12
 */
public class MonetDBRowLimitException extends KettleException {
  /**
   * Constructs a new throwable with null as its detail message.
   */
  public MonetDBRowLimitException()
  {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public MonetDBRowLimitException(String message)
  {
    super(message);
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public MonetDBRowLimitException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   * @param message the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public MonetDBRowLimitException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
