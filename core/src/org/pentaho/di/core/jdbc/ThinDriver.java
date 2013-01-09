package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.pentaho.di.core.KettleClientEnvironment;

public class ThinDriver implements Driver {
  
  public static final String BASE_URL = "jdbc:pdi://";
  public static final String SERVICE_NAME = "/kettle"; 
  
  static {
    try {
    	KettleClientEnvironment.init();
      DriverManager.registerDriver(new ThinDriver());
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong registering the thin Kettle JDBC driver", e);
    }
  }
  
  public ThinDriver() throws SQLException {
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return url.startsWith(BASE_URL);
  }

  @Override
  public Connection connect(String url, Properties properties) throws SQLException {
    String username = properties.getProperty("user");
    String password = properties.getProperty("password");
    Connection connection = new ThinConnection(url, username, password);
    
    return connection;
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
  public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {
    return null;
  }

  @Override
  public boolean jdbcCompliant() {
    return false;
  }

}
