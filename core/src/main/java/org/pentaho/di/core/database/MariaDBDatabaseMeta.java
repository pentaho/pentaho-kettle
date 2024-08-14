/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;

import java.util.Set;

import com.google.common.collect.Sets;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;

public class MariaDBDatabaseMeta extends MySQLDatabaseMeta {
  private static final Class<?> PKG = MariaDBDatabaseMeta.class;

  private static final Set<String> SHORT_MESSAGE_EXCEPTIONS = Sets.newHashSet( "org.mariadb.jdbc.internal.stream.MaxAllowedPacketException" );

  @Override public String[] getUsedLibraries() {
    return new String[] { "mariadb-java-client-1.4.6.jar" };
  }

  @Override public String getDriverClass() {
    return "org.mariadb.jdbc.Driver";
  }

  @Override public String getURL( String hostname, String port, String databaseName ) {
    if ( Const.isEmpty( port ) ) {
      return "jdbc:mariadb://" + hostname + "/" + databaseName;
    } else {
      return "jdbc:mariadb://" + hostname + ":" + port + "/" + databaseName;
    }
  }

  @Override public boolean fullExceptionLog( Exception e ) {
    Throwable cause = ( e == null ? null : e.getCause() );
    return !( cause != null && SHORT_MESSAGE_EXCEPTIONS.contains( cause.getClass().getName() ) );
  }

  /**
   * Returns the column name for a MariaDB field.
   *
   * @param dbMetaData
   * @param rsMetaData
   * @param index
   * @return The column label.
   * @throws KettleDatabaseException
   */
  @Override public String getLegacyColumnName( DatabaseMetaData dbMetaData, ResultSetMetaData rsMetaData, int index ) throws KettleDatabaseException {
    if ( dbMetaData == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameNoDBMetaDataException" ) );
    }

    if ( rsMetaData == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameNoRSMetaDataException" ) );
    }

    try {
      return rsMetaData.getColumnLabel( index );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( String.format( "%s: %s", BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameException" ), e.getMessage() ), e );
    }
  }
}
