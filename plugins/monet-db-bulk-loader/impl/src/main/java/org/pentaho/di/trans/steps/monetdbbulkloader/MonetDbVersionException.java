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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.pentaho.di.core.exception.KettleException;

/**
 * Custom exception using for processing MonetDB version
 *
 * @author Tatsiana_Kasiankova
 *
 */
public class MonetDbVersionException extends KettleException {

  private static final long serialVersionUID = 3876078230581782431L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public MonetDbVersionException() {
    super();
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
  public MonetDbVersionException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public MonetDbVersionException( String message ) {
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
  public MonetDbVersionException( Throwable cause ) {
    super( cause );
  }

}
