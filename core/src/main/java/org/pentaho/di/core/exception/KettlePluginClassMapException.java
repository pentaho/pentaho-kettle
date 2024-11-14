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

package org.pentaho.di.core.exception;

public class KettlePluginClassMapException extends KettlePluginException {
  private static final long serialVersionUID = 3928198226583274564L;

  public KettlePluginClassMapException() {
    super();
  }

  public KettlePluginClassMapException( String message, Throwable cause ) {
    super( message, cause );
  }

  public KettlePluginClassMapException( String message ) {
    super( message );
  }

  public KettlePluginClassMapException( Throwable cause ) {
    super( cause );
  }
}
