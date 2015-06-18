/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.pentaho.di.core.KettleClientEnvironment;

public class ThinDriver implements Driver {

  public static final String BASE_URL = "jdbc:pdi://";
  public static final String SERVICE_NAME = "/kettle";

  static {
    try {
      KettleClientEnvironment.init();
      DriverManager.registerDriver( new ThinDriver() );
    } catch ( Exception e ) {
      throw new RuntimeException( "Something went wrong registering the thin Kettle JDBC driver", e );
    }
  }

  public ThinDriver() throws SQLException {
  }

  @Override
  public boolean acceptsURL( String url ) throws SQLException {
    return url.startsWith( BASE_URL );
  }

  @Override
  public Connection connect( String url, Properties properties ) throws SQLException {
    String username = properties.getProperty( "user" );
    String password = properties.getProperty( "password" );

    if ( acceptsURL( url ) ) {
      ThinConnection connection = new ThinConnection( url, username, password );
      connection.testConnection();
      return connection;
    } else {
      return null;
    }
  }

  @Override
  public int getMajorVersion() {
    return 0;
  }

  @Override
  public int getMinorVersion() {
    return 1;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( String arg0, Properties arg1 ) throws SQLException {
    return null;
  }

  @Override
  public boolean jdbcCompliant() {
    return false;
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

}
