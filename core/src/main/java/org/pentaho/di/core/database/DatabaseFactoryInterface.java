/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;

public interface DatabaseFactoryInterface {

  public String getConnectionTestReport( DatabaseMeta databaseMeta ) throws KettleDatabaseException;

  public DatabaseTestResults getConnectionTestResults( DatabaseMeta databaseMeta ) throws KettleDatabaseException;

}
