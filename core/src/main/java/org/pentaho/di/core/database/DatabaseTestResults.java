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


package org.pentaho.di.core.database;

/**
 * Created by ddiroma on 5/10/2018.
 */
public class DatabaseTestResults {
  private String message;
  private boolean success;
  private Exception exception;

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess( boolean success ) {
    this.success = success;
  }

  public Exception getException() {
    return exception;
  }

  public void setException( Exception exception ) {
    this.exception = exception;
  }
}
