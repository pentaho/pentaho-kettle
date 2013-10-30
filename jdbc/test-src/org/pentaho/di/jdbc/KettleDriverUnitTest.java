package org.pentaho.di.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Test;

public class KettleDriverUnitTest {

  public static String validURL = "jdbc:kettle:validurl";
  public static Properties validInfo = new Properties();
  private static String expectedDriverString = "JDBCKettle 1.0";
  private static String expectedVersionString = "1.0";
  private static int expectedMajorVersion = 1;
  private static int expectedMinorVersion = 0;

  @Test
  public void testAcceptsURL() {
    KettleDriver driver = new KettleDriver();
    try {
      assertFalse( driver.acceptsURL( "invalidurl" ) );
    } catch ( SQLException e ) {
      fail( "Invalid URL threw a SQL exception." );
    }
    try {
      assertTrue( driver.acceptsURL( validURL ) );
    } catch ( SQLException e ) {
      fail( "Valid URL threw a SQL exception." );
    }

  }

  /*
   * @Test public void testConnect() { fail( "Not yet implemented" ); }
   */

  @Test
  public void testParseURL() {
    KettleDriver driver = new KettleDriver();
    Properties driverRet = driver.parseURL( validURL, validInfo );
    assertNotNull( driverRet );
    // should be empty properties list for now - put a check in so if this function is ever implemented right we force
    // the unit test to be updated appropriately!
    assertTrue( driverRet.isEmpty() );
  }

  @Test
  public void testGetMajorVersion() {
    KettleDriver driver = new KettleDriver();
    int majorVersion = driver.getMajorVersion();
    assertEquals( majorVersion, expectedMajorVersion );
  }

  @Test
  public void testGetMinorVersion() {
    KettleDriver driver = new KettleDriver();
    int minorVersion = driver.getMinorVersion();
    assertEquals( minorVersion, expectedMinorVersion );
  }

  @Test
  public void testGetPropertyInfo() {
    KettleDriver driver = new KettleDriver();
    DriverPropertyInfo[] props = null;
    try {
      props = driver.getPropertyInfo( validURL, validInfo );
    } catch ( SQLException e ) {
      fail( "Got exception in getPropertyInfo :" + e.getMessage() );
    }
    assertNull( props );
  }

  @Test
  public void testJdbcCompliant() {
    KettleDriver driver = new KettleDriver();
    boolean isCompliant = driver.jdbcCompliant();
    assertFalse( isCompliant );
  }

  @Test
  public void testToString() {
    KettleDriver driver = new KettleDriver();
    String driverString = driver.toString();
    assertEquals( driverString, expectedDriverString );
    ;
  }

  @Test
  public void testGetVersion() {
    KettleDriver driver = new KettleDriver();
    String version = driver.getVersion();

    assertEquals( version, expectedVersionString );
  }

  @Test
  public void testGetParentLogger() {
    KettleDriver driver = new KettleDriver();
    try {
      Logger parLogger = driver.getParentLogger();
      assertNull( parLogger );
    } catch ( SQLFeatureNotSupportedException e ) {
      fail( "Got exception getting the parent logger" + e.getMessage() );
    }
  }
}
