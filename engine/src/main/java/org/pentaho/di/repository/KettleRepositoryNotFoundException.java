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

import org.pentaho.di.core.exception.KettleException;

/**
 * This exception is thrown when repository wasn't found.
 */
public class KettleRepositoryNotFoundException extends KettleException {

  private static final long serialVersionUID = 5920594838763107082L;

  public KettleRepositoryNotFoundException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public KettleRepositoryNotFoundException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public KettleRepositoryNotFoundException( String message ) {
    super( message );
  }

  /**
   * @param cause
   */
  public KettleRepositoryNotFoundException( Throwable cause ) {
    super( cause );
  }

}
