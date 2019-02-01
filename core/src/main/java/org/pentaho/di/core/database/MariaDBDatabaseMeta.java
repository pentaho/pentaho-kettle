/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Set;

import org.pentaho.di.core.Const;

import com.google.common.collect.Sets;

public class MariaDBDatabaseMeta extends MySQLDatabaseMeta {
  private static final Set<String> SHORT_MESSAGE_EXCEPTIONS = Sets.newHashSet( "org.mariadb.jdbc.internal.stream.MaxAllowedPacketException" );



  @Override public String[] getUsedLibraries() {
    return new String[] { "mariadb-java-client-1.4.6.jar" };
  }


  @Override public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "org.mariadb.jdbc.Driver";
    }
  }

  @Override public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else {
      if ( Const.isEmpty( port ) ) {
        return "jdbc:mariadb://" + hostname + "/" + databaseName;
      } else {
        return "jdbc:mariadb://" + hostname + ":" + port + "/" + databaseName;
      }
    }
  }

  @Override public boolean fullExceptionLog( Exception e ) {
    Throwable cause = ( e == null ? null : e.getCause() );
    return !( cause != null && SHORT_MESSAGE_EXCEPTIONS.contains( cause.getClass().getName() ) );
  }

}
