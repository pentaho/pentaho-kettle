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
package org.pentaho.di.repository;

/**
 * This exception is thrown when there was an authentication error, while connecting into a repository.
 */
public class KettleAuthenticationException extends RuntimeException {

  private static final long serialVersionUID = -8636766532491093338L;

  public KettleAuthenticationException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public KettleAuthenticationException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public KettleAuthenticationException( String message ) {
    super( message );
  }

  /**
   * @param cause
   */
  public KettleAuthenticationException( Throwable cause ) {
    super( cause );
  }
}
