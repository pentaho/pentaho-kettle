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

import java.sql.Driver;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * A non-thread safe mechanism to execute code against a given JDBC Driver. The
 * code executed within {@link #callWithDriver(Driver)} will have the current
 * Thread's context ClassLoader set to the driver's.
 * 
 * @param <T> Type of value to return from {@link #call()}
 */
public abstract class JDBCDriverCallable<T> implements Callable<T> {
  /**
   * Cached driver value passed in via {@link #callWithDriver(Driver)} that may
   * be accessed within {@link #call()}.
   */
  protected Driver driver;

  /**
   * Sets the current thread's context class loader to the driver's class loader
   * for the invocation of {@link #call()}.
   * 
   * @param driver JDBC Driver
   * @return Return value of {@link #call()}
   * @throws SQLException Exception executing {@link #call()}.
   * @see #call()
   */
  public T callWithDriver(Driver driver) throws SQLException {
    this.driver = driver;
    ClassLoader current = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(driver.getClass().getClassLoader());
    try {
      return call();
    } catch (Exception ex) {
      if (SQLException.class.isAssignableFrom(ex.getClass())) {
        throw (SQLException) ex;
      }
      throw new SQLException("Error communicating with Hive", ex);
    } finally {
      Thread.currentThread().setContextClassLoader(current);
    }
  }
}
