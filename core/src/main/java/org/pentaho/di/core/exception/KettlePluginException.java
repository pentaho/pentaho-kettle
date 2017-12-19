/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.exception;

/**
 * This exception is thrown in case there is an error in the Kettle plugin loader
 *
 * @author matt
 *
 */
public class KettlePluginException extends KettleException {

  private static final long serialVersionUID = -7251001771637436705L;

  public KettlePluginException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public KettlePluginException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public KettlePluginException( String message ) {
    super( message );
  }

  /**
   * @param cause
   */
  public KettlePluginException( Throwable cause ) {
    super( cause );
  }

}
