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

public class InfobrightDatabaseMeta extends MySQLDatabaseMeta implements DatabaseInterface {

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 5029;
    }
    return -1;
  }

  @Override
  public void addDefaultOptions() {
    addExtraOption( getPluginId(), "characterEncoding", "UTF-8" );
  }

}
