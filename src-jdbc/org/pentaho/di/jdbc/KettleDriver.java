/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 */

package org.pentaho.di.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.i18n.BaseMessages;

public class KettleDriver implements Driver {
  
  private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public final static String driverPrefix = "jdbc:kettle:";
	/** Set if the JDBC specification to implement is 3.0 or greater. */
    public static final boolean JDBC3 =
            "1.4".compareTo(System.getProperty("java.specification.version")) <= 0;
    
    private transient static final Log log = LogFactory
	.getLog(KettleDriver.class);
    
    static {
        try {
            // Register this with the DriverManager
            DriverManager.registerDriver(new KettleDriver());
            log.debug("------JDBCKettleDriver is registered!-----");
        } catch (SQLException e) {
        }
    }
    
	public boolean acceptsURL(String url) throws SQLException {
		 if (url == null) {
	            return false;
	        }

	        return url.toLowerCase().startsWith(driverPrefix);
	}

	public Connection connect(String url, Properties info) throws SQLException {
		if (url == null || !url.toLowerCase().startsWith(driverPrefix)) {
            return null;
        }
		
		Properties props = setupConnectProperties(url, info);
		  if (JDBC3) {
	            return new ConnectionJDBC3(url, props);
	        }
		return null;
	}
	
	

	 /**
     * Sets up properties for the {@link #connect(String, java.util.Properties)} method.
     *
     * @param url the URL of the database to which to connect
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments.
     * @return the set of properties for the connection
     * @throws SQLException if an error occurs parsing the URL
     */
    private Properties setupConnectProperties(String url, final Properties info) throws SQLException {

        Properties props = parseURL(url, info);

        if (props == null) {
            throw new SQLException(BaseMessages.getString(PKG, "error.driver.badurl", url), "08001");
        }

        if (props.getProperty(Constants.LOGINTIMEOUT) == null) {
            props.setProperty(Constants.LOGINTIMEOUT, Integer.toString(DriverManager.getLoginTimeout()));
        }

        // Set default properties
        props = DefaultProperties.addDefaultProperties(props);
      
        return props;
    }

	public Properties parseURL(String url, final Properties info) {
		if(info==null)
		{
			return null;
		}
		Properties p = new Properties();
		log.debug("url="+url);
		return p;
	}

	public int getMajorVersion() {
		
		return 1;
	}

	public int getMinorVersion() {
		
		return 0;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		log.debug(".............getPropertyInfo is called!................");
		return null;
	}

	public boolean jdbcCompliant() {
		
		return false;
	}
	
	public String toString() {
        return "JDBCKettle " + getVersion();
    }

	public String getVersion() {
		
		return this.getMajorVersion()+"."+this.getMinorVersion();
	}

}
