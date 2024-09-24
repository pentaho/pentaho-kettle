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

package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;

/**
 * This class provides a wrapper around NamingException or other exceptions that may occur when trying to get a data
 * source by name.
 *
 * @author mbatchel Jan 9, 2009
 *
 */

public class DataSourceNamingException extends KettleDatabaseException {

  private static final long serialVersionUID = 9097682862391709401L;

  public DataSourceNamingException() {
    super();
  }

  public DataSourceNamingException( String message ) {
    super( message );
  }

  public DataSourceNamingException( Throwable cause ) {
    super( cause );
  }

  public DataSourceNamingException( String message, Throwable cause ) {
    super( message, cause );
  }

}
