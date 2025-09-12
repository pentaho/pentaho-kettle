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

import org.pentaho.di.core.plugins.DatabaseMetaPlugin;

@DatabaseMetaPlugin(
    type = "OCEANBASE_MYSQL",
    typeDescription = "oceanbase mysql mode"
)
public class OceanBaseMysqlDatabaseMeta extends MySQLDatabaseMeta {
    public OceanBaseMysqlDatabaseMeta() {
    }

    public int getDefaultDatabasePort() {
        return this.getAccessType() == 0 ? 2883 : -1;
    }

}
