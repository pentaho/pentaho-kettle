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


package org.pentaho.di.trans.steps.sapinput.sap;

import org.pentaho.di.core.exception.KettleException;

public class SAPException extends KettleException {

  private static final long serialVersionUID = 1L;

  public SAPException( String message, Throwable cause ) {
    super( message, cause );
  }

  public SAPException( String message ) {
    super( message );
  }

}
