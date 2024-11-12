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


package org.pentaho.di.core.auth.core;

import org.pentaho.di.core.exception.KettleException;

public class AuthenticationConsumptionException extends KettleException {
  private static final long serialVersionUID = 1139802265031922758L;

  public AuthenticationConsumptionException( Exception cause ) {
    super( cause );
  }

  public AuthenticationConsumptionException( String message, Exception cause ) {
    super( message, cause );
  }
}
