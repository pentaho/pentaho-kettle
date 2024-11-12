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
