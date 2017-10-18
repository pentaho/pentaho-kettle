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
package org.pentaho.di.trans.ael.websocket.exception;

import org.pentaho.di.core.exception.KettleException;

/**
 * Created by fcamara on 8/17/17.
 */
public class HandlerRegistrationException extends KettleException {

  private static final long serialVersionUID = -4048782381272550246L;

  public HandlerRegistrationException() {
    super();
  }

  public HandlerRegistrationException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public HandlerRegistrationException( final String message ) {
    super( message );
  }
}
