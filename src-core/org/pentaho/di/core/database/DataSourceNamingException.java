/*
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;

/**
 * This class provides a wrapper around NamingException or
 * other exceptions that may occur when trying to get a
 * data source by name.
 * 
 * @author mbatchel
 * Jan 9, 2009
 *
 */

public class DataSourceNamingException extends KettleDatabaseException {

  private static final long serialVersionUID = 9097682862391709401L;

  public DataSourceNamingException() {
    super();
  }

  public DataSourceNamingException(String message) {
    super(message);
  }

  public DataSourceNamingException(Throwable cause) {
    super(cause);
  }

  public DataSourceNamingException(String message, Throwable cause) {
    super(message, cause);
  }

}
