package org.pentaho.di.core.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class DelegatingDriver implements Driver
{
    private final Driver driver;

    public DelegatingDriver(Driver driver)
    {
        if (driver == null)
        {
            throw new IllegalArgumentException("Driver must not be null.");
        }
        this.driver = driver;
    }

    public Connection connect(String url, Properties info) throws SQLException
    {
       return driver.connect(url, info);
    }

    public boolean acceptsURL(String url) throws SQLException
    {
       return driver.acceptsURL(url);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return driver.getPropertyInfo(url, info);
    }

    public int getMajorVersion()
    {
        return driver.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return driver.getMinorVersion();
    }

    public boolean jdbcCompliant()
    { 
        return driver.jdbcCompliant();
    }
}