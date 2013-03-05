package org.pentaho.di.core.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This delegating driver allows the plugin system to be used in a separate 
 * class loader while making DriverManager believe that the parent class loader is used.
 * 
 * It's an unfortunate hack but it works fine.  
 *  
 * @author matt
 *
 */
public class DelegatingDriver implements Driver {
  private final Driver driver;

  public DelegatingDriver(Driver driver) {
    if (driver == null) {
      throw new IllegalArgumentException("Driver must not be null.");
    }
    this.driver = driver;
  }

  public Connection connect(String url, Properties info) throws SQLException {
    return driver.connect(url, info);
  }

  public boolean acceptsURL(String url) throws SQLException {
    return driver.acceptsURL(url);
  }

  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
    return driver.getPropertyInfo(url, info);
  }

  public int getMajorVersion() {
    return driver.getMajorVersion();
  }

  public int getMinorVersion() {
    return driver.getMinorVersion();
  }

  public boolean jdbcCompliant() {
    return driver.jdbcCompliant();
  }
  
  /**
   * This method is added to make this driver compile on Java7
   * @return always null until we finally switch over to Java7 with the codebase (TODO)
   */
  public Logger getParentLogger() {
    return null;
  }
}