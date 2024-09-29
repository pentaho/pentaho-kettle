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
 * This exception is thrown in case there is an error in the Kettle plugin loader
 *
 * @author matt
 *
 */
public class KettlePluginNotFoundException extends KettlePluginException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message
   * @param cause
   */
  public KettlePluginNotFoundException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public KettlePluginNotFoundException( String message ) {
    super( message );
  }
}
