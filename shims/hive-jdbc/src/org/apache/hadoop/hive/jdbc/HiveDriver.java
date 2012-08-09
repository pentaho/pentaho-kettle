/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.apache.hadoop.hive.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>
 * This is proxy driver for the Hive JDBC Driver available through the current
 * active Hadoop configuration.
 * </p>
 * <p>
 * This driver is named exactly the same as the official Apache Hive driver
 * so no further modifications are required by calling code to swap in this
 * proxy.
 * </p>
 * <p>
 * This class uses reflection to attempt to find the Big Data Plugin and load
 * the HadoopConfigurationBootstrap so we have access to the Hive JDBC driver
 * that is compatible with the currently selected Hadoop configuration. All
 * operations are delegated to the current active Hadoop configuration's Hive
 * JDBC driver via HadoopConfiguration#getHiveJdbcDriver.
 * </p>
 * <p>
 * All calls to the loaded HiveDriver will have the current Thread's context 
 * class loader set to the class that loaded the driver so subsequent resource
 * lookups are successful.
 * </p>
 */
public class HiveDriver implements java.sql.Driver {
  /**
   * Method name of {@link org.pentaho.hadoop.shim.spi.HadoopShim#getHiveJdbcDriver()}
   */
  private static final String METHOD_GET_HIVE_JDBC_DRIVER = "getHiveJdbcDriver";

  /**
   * Utility for resolving Hadoop configurations dynamically.
   */
  private HadoopConfigurationUtil util;

  // Register ourself with the JDBC Driver Manager
  static {
    try {
      DriverManager.registerDriver(new HiveDriver());
    } catch (Exception ex) {
      throw new RuntimeException("Unable to register Hive JDBC driver", ex);
    }
  }

  /**
   * Create a new Hive driver with the default configuration utility.
   */
  public HiveDriver() {
    this(new HadoopConfigurationUtil());
  }

  public HiveDriver(HadoopConfigurationUtil util) {
    if (util == null) {
      throw new NullPointerException();
    }
    this.util = util;
  }

  protected Driver getActiveDriver() throws SQLException {
    Driver driver = null;
    try {
      Object shim = util.getActiveHadoopShim();
      // public Driver HadoopShim#getHiveJdbcDriver()
      Method getHiveJdbcDriver = shim.getClass().getMethod(METHOD_GET_HIVE_JDBC_DRIVER);
      driver = (Driver) getHiveJdbcDriver.invoke(shim);
    } catch (Exception ex) {
      throw new SQLException("Unable to load Hive JDBC driver for the currently active Hadoop configuration", ex);
    }

    // Check if the Shim contains a Hive driver. It may return this driver if it 
    // doesn't contain one since it'll be found in one of the parent class loaders
    // so we also need to make sure we didn't return ourself... :)
    if (driver == null || driver.getClass() == this.getClass()) {
      throw new SQLException("The active Hadoop configuration does not contain a Hive JDBC driver");
    }

    return driver;
  }

  protected <T> T callWithActiveDriver(JDBCDriverCallable<T> callback) throws SQLException {
    return callback.callWithDriver(getActiveDriver());
  }

  /**
   * Attempt to find the {@link ClassLoader} for the Hadoop Shim API. It is here that we'll be able to load the appropriate
   * Hive JDBC driver with all associated Hadoop and other libraries required to properly execute against a given cluster.
   * @return The {@link ClassLoader} for the Hadoop Shim API or {@code null} if none could be located
   * @throws RuntimeException if there was an error attempting to load the database meta interface
   */
  @Override
  public Connection connect(final String url, final Properties info) throws SQLException {
    return callWithActiveDriver(new JDBCDriverCallable<Connection>() {
      @Override
      public Connection call() throws Exception {
        return driver.connect(url, info);
      }
    });
  }

  @Override
  public boolean acceptsURL(final String url) throws SQLException {
    return Boolean.TRUE.equals(callWithActiveDriver(new JDBCDriverCallable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return driver.acceptsURL(url);
      }
    }));
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
    return callWithActiveDriver(new JDBCDriverCallable<DriverPropertyInfo[]>() {
      @Override
      public DriverPropertyInfo[] call() throws Exception {
        return driver.getPropertyInfo(url, info);
      }
    });
  }

  @Override
  public int getMajorVersion() {
    try {
      return (int) callWithActiveDriver(new JDBCDriverCallable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return driver.getMajorVersion();
        }
      });
    } catch (SQLException ex) {
      // No idea what the driver version is without a driver
      return -1;
    }
  }

  @Override
  public int getMinorVersion() {
    try {
      return (int) callWithActiveDriver(new JDBCDriverCallable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return driver.getMinorVersion();
        }
      });
    } catch (SQLException ex) {
      // No idea what the driver version is without a driver
      return -1;
    }
  }

  @Override
  public boolean jdbcCompliant() {
    try {
      return Boolean.TRUE.equals(callWithActiveDriver(new JDBCDriverCallable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return driver.jdbcCompliant();
        }
      }));
    } catch (SQLException ex) {
      // The HiveDriver is not JDBC compliant as of Hive 0.9.0. If the driver
      // cannot return it's actual compliancy we'll default to false
      return false;
    }
  }
}
