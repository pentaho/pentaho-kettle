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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This delegating driver allows the plugin system to be used in a separate class loader while making DriverManager
 * believe that the parent class loader is used.
 *
 * It's an unfortunate hack but it works fine.
 *
 * @author matt
 *
 */
public class DelegatingDriver implements Driver {
  private final Driver driver;

  public DelegatingDriver( Driver driver ) {
    if ( driver == null ) {
      throw new IllegalArgumentException( "Driver must not be null." );
    }
    this.driver = driver;
  }

  @Override
  public Connection connect( String url, Properties info ) throws SQLException {
    return driver.connect( url, info );
  }

  @Override
  public boolean acceptsURL( String url ) throws SQLException {
    return driver.acceptsURL( url );
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
    return driver.getPropertyInfo( url, info );
  }

  @Override
  public int getMajorVersion() {
    return driver.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return driver.getMinorVersion();
  }

  @Override
  public boolean jdbcCompliant() {
    return driver.jdbcCompliant();
  }

  /**
   * This method is added to make this driver compile on Java7
   *
   * @return always null until we finally switch over to Java7 with the codebase (TODO)
   */
  public Logger getParentLogger() {
    return null;
  }
}
