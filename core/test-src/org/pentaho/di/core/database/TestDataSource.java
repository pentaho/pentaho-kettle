/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class TestDataSource implements DataSource {

  private Connection connection;

  public TestDataSource( Connection newConnection ) {
    this.connection = newConnection;
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter( PrintWriter out ) throws SQLException {

  }

  @Override
  public void setLoginTimeout( int seconds ) throws SQLException {
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  @Override
  public <T> T unwrap( Class<T> iface ) throws SQLException {
    return null;
  }

  @Override
  public boolean isWrapperFor( Class<?> iface ) throws SQLException {
    return false;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public Connection getConnection( String username, String password ) throws SQLException {
    return connection;
  }

}
