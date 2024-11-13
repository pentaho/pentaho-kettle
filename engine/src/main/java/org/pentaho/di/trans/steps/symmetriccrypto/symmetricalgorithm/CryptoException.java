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


package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

import org.pentaho.di.core.exception.KettleException;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */

public class CryptoException extends KettleException {
  public static final long serialVersionUID = 0x8D8EA0264F7A1C0FL;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public CryptoException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message
   *          - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public CryptoException( String message ) {
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
  public CryptoException( Throwable cause ) {
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
  public CryptoException( String message, Throwable cause ) {
    super( message, cause );
  }
}
