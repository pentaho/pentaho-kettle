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
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;

@DatabaseMetaPlugin(
        type = "OCEANBASE_ORACLE",
        typeDescription = "oceanbase oracle mode"
)
public class OceanBaseOracleDatabaseMeta extends OracleDatabaseMeta {
    public OceanBaseOracleDatabaseMeta() {
    }

    public int getDefaultDatabasePort() {
        return this.getAccessType() == 0 ? 2883 : -1;
    }

    public int[] getAccessTypeList() {
        return new int[]{0, 4};
    }

    public String getDriverClass() {
        return "com.alipay.oceanbase.obproxy.mysql.jdbc.Driver";
    }

    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException {
        if (this.getAccessType() == 1) {
            return "jdbc:odbc:" + databaseName;
        } else if (this.getAccessType() == 0) {
            return String.format("jdbc:oceanbase://%s?useUnicode=true&characterEncoding=%s&connectTimeout=30000&rewriteBatchedStatements=true", hostname + ":" + port, "utf8");
        } else if (databaseName != null && databaseName.length() > 0) {
            return hostname != null && hostname.length() > 0 && port != null && port.length() > 0 ? "jdbc:oracle:oci:@(description=(address=(host=" + hostname + ")(protocol=tcp)(port=" + port + "))(connect_data=(sid=" + databaseName + ")))" : "jdbc:oracle:oci:@" + databaseName;
        } else {
            throw new KettleDatabaseException("Unable to construct a JDBC URL: at least the database name must be specified");
        }
    }

    public String[] getUsedLibraries() {
        return new String[]{"oceanbase-client-2.4.7.2.jar"};
    }
}
