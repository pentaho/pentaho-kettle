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

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;

public class HiveDriverTest {

  private HadoopShim getMockShimWithDriver(final Driver driver) {
    return new MockHadoopShim() {
      @Override
      public Driver getHiveJdbcDriver() {
        return driver;
      }
    };
  }

  private HadoopConfigurationUtil getMockUtil(final HadoopShim shim) {
    return new HadoopConfigurationUtil() {
      @Override
      public Object getActiveHadoopShim() throws Exception {
        return shim;
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_no_util() {
    new HiveDriver(null);
  }

  @Test
  public void getActiveDriver() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    HadoopShim shim = new MockHadoopShim() {
      public java.sql.Driver getHiveJdbcDriver() {
        called.set(true);
        return new MockDriver();
      };
    };
    HiveDriver d = new HiveDriver(getMockUtil(shim));
    d.getActiveDriver();
    assertTrue("Shim's getHiveJdbcDriver() not called", called.get());
  }

  @Test
  public void getActiveDriver_exception_in_getHiveJdbcDriver() {
    HadoopShim shim = new MockHadoopShim() {
      public java.sql.Driver getHiveJdbcDriver() {
        throw new RuntimeException();
      };
    };
    HiveDriver d = new HiveDriver(getMockUtil(shim));

    try {
      d.getActiveDriver();
      fail("Expected exception");
    } catch (SQLException ex) {
      assertEquals(InvocationTargetException.class, ex.getCause().getClass());
      assertEquals("Unable to load Hive JDBC driver for the currently active Hadoop configuration", ex.getMessage());
    }
  }

  @Test
  public void getActiveDriver_null_driver() {
    HadoopShim shim = new MockHadoopShim() {
      public java.sql.Driver getHiveJdbcDriver() {
        return null;
      };
    };
    HiveDriver d = new HiveDriver(getMockUtil(shim));

    try {
      d.getActiveDriver();
      fail("Expected exception");
    } catch (SQLException ex) {
      assertNull(ex.getCause());
      assertEquals("The active Hadoop configuration does not contain a Hive JDBC driver", ex.getMessage());
    }
  }

  @Test
  public void getActiveDriver_same_driver() {
    HadoopShim shim = new MockHadoopShim() {
      public java.sql.Driver getHiveJdbcDriver() {
        // Return another shim driver. This should fail when called since the
        // classes are the same
        return new HiveDriver();
      };
    };
    HiveDriver d = new HiveDriver(getMockUtil(shim));

    try {
      d.getActiveDriver();
      fail("Expected exception");
    } catch (SQLException ex) {
      assertNull(ex.getCause());
      assertEquals("The active Hadoop configuration does not contain a Hive JDBC driver", ex.getMessage());
    }
  }

  @Test
  public void connect() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public Connection connect(String url, Properties info) throws SQLException {
        called.set(true);
        return null;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.connect(null, null);
    assertTrue(called.get());
  }

  @Test
  public void acceptsURL() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public boolean acceptsURL(String url) throws SQLException {
        called.set(true);
        return false;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.acceptsURL(null);
    assertTrue(called.get());
  }

  @Test
  public void getPropertyInfo() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        called.set(true);
        return null;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.getPropertyInfo(null, null);
    assertTrue(called.get());
  }

  @Test
  public void getMajorVersion() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public int getMajorVersion() {
        called.set(true);
        return 0;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.getMajorVersion();
    assertTrue(called.get());
  }

  @Test
  public void getMajorVersion_exception() {
    Driver driver = new MockDriver() {
      @Override
      public int getMajorVersion() {
        throw new NullPointerException();
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    // If an exception is thrown the version returned should be -1
    assertEquals(-1, d.getMajorVersion());
  }

  @Test
  public void getMinorVersion() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public int getMinorVersion() {
        called.set(true);
        return 0;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.getMinorVersion();
    assertTrue(called.get());
  }

  @Test
  public void getMinorVersion_exception() {
    Driver driver = new MockDriver() {
      @Override
      public int getMinorVersion() {
        throw new NullPointerException();
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    // If an exception is thrown the version returned should be -1
    assertEquals(-1, d.getMinorVersion());
  }

  @Test
  public void jdbcCompliant() throws SQLException {
    final AtomicBoolean called = new AtomicBoolean(false);
    Driver driver = new MockDriver() {
      @Override
      public boolean jdbcCompliant() {
        called.set(true);
        return false;
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));

    d.jdbcCompliant();
    assertTrue(called.get());
  }
  
  @Test
  public void jdbcCompliant_exception() throws SQLException {
    Driver driver = new MockDriver() {
      @Override
      public boolean jdbcCompliant() {
        throw new NullPointerException();
      }
    };
    HiveDriver d = new HiveDriver(getMockUtil(getMockShimWithDriver(driver)));
    
    // should return false if there is an exception
    assertFalse(d.jdbcCompliant());
  }
}
