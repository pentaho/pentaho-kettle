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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;

public class JDBCDriverCallableTest {

  public class MockDriver implements Driver {

    @Override
    public boolean acceptsURL(String url) throws SQLException {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getMajorVersion() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getMinorVersion() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean jdbcCompliant() {
      // TODO Auto-generated method stub
      return false;
    }
  }

  @Test
  public void callWithDriver() throws SQLException {
    final Driver mockDriver = new MockDriver();
    JDBCDriverCallable<Boolean> callable = new JDBCDriverCallable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        assertEquals(mockDriver, driver);
        assertEquals(mockDriver.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());
        return Boolean.TRUE;
      }
    };

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    // Null out the context class loader so we can tell if it was properly set
    Thread.currentThread().setContextClassLoader(null);
    try {
      assertTrue("Unexpected callable result", callable.callWithDriver(mockDriver));
    } finally {
      // Restore the context class loader
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Test
  public void callWithDriver_throws_SQLException() {
    final SQLException testException = new SQLException();
    JDBCDriverCallable<Boolean> callable = new JDBCDriverCallable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        throw testException;
      }
    };
    try {
      callable.callWithDriver(new MockDriver());
    } catch (SQLException ex) {
      assertEquals("Wrong exception thrown", testException, ex);
    }
  }

  @Test
  public void callWithDriver_throws_genericException() {
    final Exception testException = new NullPointerException();
    JDBCDriverCallable<Boolean> callable = new JDBCDriverCallable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        throw testException;
      }
    };
    try {
      callable.callWithDriver(new MockDriver());
    } catch (SQLException ex) {
      assertEquals(testException, ex.getCause());
    }
  }
}
