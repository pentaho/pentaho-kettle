/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionAzureKeyVaultProvider;
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author patitapaban19
 *
 */
public class AzureSqlDataBaseMeta extends MSSQLServerDatabaseMeta {

  public static final String JDBC_AUTH_METHOD = "jdbcAuthMethod";
  public static final String IS_ALWAYS_ENCRYPTION_ENABLED = "azureAlwaysEncryptionEnabled";
  public static final String CLIENT_ID = "azureClientSecretId";
  public static final String CLIENT_SECRET_KEY = "azureClientSecretKey";

  public static final String SQL_AUTHENTICATION = "SQL Server Authentication";
  public static final String ACTIVE_DIRECTORY_PASSWORD = "Azure Active Directory - Password";
  public static final String ACTIVE_DIRECTORY_MFA = "Azure Active Directory - Universal With MFA";
  public static final String ACTIVE_DIRECTORY_INTEGRATED = "Azure Active Directory - Integrated";

  /**
   * Registering the ColumnEncryptionKeyStoreProviders once when the instance of the class created,
   * else will get duplicate registration error.
   */
  {
    if ( getAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "" ).equals( "true" ) ) {
      String clientID = getAttribute( CLIENT_ID, "" );
      String clientKey = getAttribute( CLIENT_SECRET_KEY, "" );
      SQLServerColumnEncryptionAzureKeyVaultProvider akvProvider = null;
      try {
        akvProvider = new SQLServerColumnEncryptionAzureKeyVaultProvider(clientID, clientKey);
        Map<String, SQLServerColumnEncryptionKeyStoreProvider> keyStoreMap = new HashMap<>();
        keyStoreMap.put(akvProvider.getName(), akvProvider);
        SQLServerConnection.registerColumnEncryptionKeyStoreProviders(keyStoreMap);
      } catch (SQLServerException throwables) {
        throwables.printStackTrace();
      }
    }

  }


  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public String getDriverClass() {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    String url = "jdbc:sqlserver://" + hostname + ":" + port + ";database=" + databaseName + ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    if ( getAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "" ).equals( "true" ) ) {
      url += "columnEncryptionSetting=Enabled;";
    }
    if ( ACTIVE_DIRECTORY_PASSWORD.equals( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      return url + "authentication=ActiveDirectoryPassword;";
    } else if ( ACTIVE_DIRECTORY_MFA.equals( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      return url + "authentication=ActiveDirectoryInteractive;";
    } else if ( ACTIVE_DIRECTORY_INTEGRATED.equals( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      return url + "Authentication=ActiveDirectoryIntegrated;";
    } else {
      return url;
    }
  }

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param rs
   *          The result set to use
   * @param val
   *          The description of the value to retrieve
   * @param index
   *          the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  @Override
  public Object getValueFromResultSet( ResultSet rs, ValueMetaInterface val, int index ) throws KettleDatabaseException {
    Object data;
    try {
      if ( val.getType() == ValueMetaInterface.TYPE_BINARY ) {
        data = rs.getString( index + 1 );
      } else {
        return super.getValueFromResultSet( rs, val, index );
      }
      if ( rs.wasNull() ) {
        data = null;
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to get value '"
          + val.toStringMeta() + "' from database resultset, index " + index, e );
    }
    return data;
  }

  @Override
  public String getXulOverlayFile() {
    return "azuresqldb";
  }
}
