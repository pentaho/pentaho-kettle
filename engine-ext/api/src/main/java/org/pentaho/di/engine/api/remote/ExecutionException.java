/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
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
