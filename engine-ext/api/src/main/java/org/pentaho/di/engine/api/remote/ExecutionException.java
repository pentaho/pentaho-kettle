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

package org.pentaho.di.engine.api.remote;

/**
 * Execution Exception
 * <p>
 * This exception eagerly creates a stack trace upon creation so that it can be serialized properly  without relying on
 * classes on the classpath.  This is necessary when communicating with RSA and classes are not on the calling side.
 * <p>
 * Created by ccasapnello on 4/4/17.
 */
public class ExecutionException extends Exception {

  private final String exceptionType;

  public ExecutionException( Throwable throwable ) {
    super( throwable.getMessage() );
    this.exceptionType = throwable.getClass().getName();
    setStackTrace( throwable.getStackTrace() );
  }

  public String getExceptionType() {
    return exceptionType;
  }

}
