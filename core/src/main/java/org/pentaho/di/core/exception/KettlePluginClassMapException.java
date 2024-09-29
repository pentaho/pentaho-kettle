/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
