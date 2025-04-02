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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;

import java.util.Map;

import static org.pentaho.di.core.util.Utils.setBooleanValueFromMap;
import static org.pentaho.di.core.util.Utils.setStringValueFromMap;

public class MSSQLServerNativeDatabaseMeta extends MSSQLServerDatabaseMeta {
  public static final String ATTRIBUTE_USE_INTEGRATED_SECURITY = "MSSQLUseIntegratedSecurity";

  @Override
  public String getDriverClass() {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    String useIntegratedSecurity = "false";
    Object value = getAttributes().get( ATTRIBUTE_USE_INTEGRATED_SECURITY );
    if ( value != null && value instanceof String ) {
      useIntegratedSecurity = (String) value;
      // Check if the String can be parsed into a boolean
      try {
        Boolean.parseBoolean( useIntegratedSecurity );
      } catch ( IllegalArgumentException e ) {
        useIntegratedSecurity = "false";
      }
    }

    String url = "jdbc:sqlserver://" + hostname;

    if ( !Utils.isEmpty( port ) && Const.toInt( port, -1 ) > 0 ) {
      url += ":" + port;
    }
    url += ";databaseName=" + databaseName + ";integratedSecurity=" + useIntegratedSecurity;

    return url;
  }

  @Override
  public boolean supportsGetBlob() {
    return false;
  }

  @Override
  public void setConnectionSpecificInfoFromAttributes( Map<String, String> attributes ) {
    this.setUsingDoubleDecimalAsSchemaTableSeparator( setBooleanValueFromMap( attributes, ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR ) );
    addAttribute( ATTRIBUTE_USE_INTEGRATED_SECURITY, setStringValueFromMap( attributes, ATTRIBUTE_USE_INTEGRATED_SECURITY ) );
  }
}
