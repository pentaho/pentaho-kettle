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

import org.pentaho.di.core.exception.KettleDatabaseException;

public interface DatabaseFactoryInterface {

  public String getConnectionTestReport( DatabaseMeta databaseMeta ) throws KettleDatabaseException;

  public DatabaseTestResults getConnectionTestResults( DatabaseMeta databaseMeta ) throws KettleDatabaseException;

}
