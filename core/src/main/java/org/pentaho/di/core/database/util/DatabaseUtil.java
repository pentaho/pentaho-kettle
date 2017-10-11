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

package org.pentaho.di.core.database.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DataSourceNamingException;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.i18n.BaseMessages;

import javax.naming.Context;

/**
 * Provides default implementation for looking data sources up in JNDI.
 *
 * @author mbatchel
 */

public class DatabaseUtil implements DataSourceProviderInterface {
  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!
  private static Map<String, DataSource> FoundDS = Collections.synchronizedMap( new HashMap<String, DataSource>() );

  /**
   * Clears cache of DataSources (For Unit test)
   */
  protected static void clearDSCache() {
    FoundDS.clear();
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's nearly impossible to have a ubiquitous way to
   * look up a datasource. This method is intended to hide all the lookups that may be required to find a jndi name.
   *
   * @param dsName The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws NamingException
   */
  protected static DataSource getDataSourceFromJndi( String dsName, Context ctx ) throws NamingException {
    if ( Utils.isEmpty( dsName ) ) {
      throw new NamingException( BaseMessages.getString( PKG, "DatabaseUtil.DSNotFound", String.valueOf( dsName ) ) );
    }
    Object foundDs = FoundDS.get( dsName );
    if ( foundDs != null ) {
      return (DataSource) foundDs;
    }
    Object lkup = null;
    DataSource rtn = null;
    NamingException firstNe = null;
    // First, try what they ask for...
    try {
      lkup = ctx.lookup( dsName );
      if ( lkup instanceof DataSource ) {
        rtn = (DataSource) lkup;
        FoundDS.put( dsName, rtn );
        return rtn;
      }
    } catch ( NamingException ignored ) {
      firstNe = ignored;
    }
    try {
      // Needed this for Jboss
      lkup = ctx.lookup( "java:" + dsName );
      if ( lkup instanceof DataSource ) {
        rtn = (DataSource) lkup;
        FoundDS.put( dsName, rtn );
        return rtn;
      }
    } catch ( NamingException ignored ) {
      // ignore
    }
    try {
      // Tomcat
      lkup = ctx.lookup( "java:comp/env/jdbc/" + dsName );
      if ( lkup instanceof DataSource ) {
        rtn = (DataSource) lkup;
        FoundDS.put( dsName, rtn );
        return rtn;
      }
    } catch ( NamingException ignored ) {
      // ignore
    }
    try {
      // Others?
      lkup = ctx.lookup( "jdbc/" + dsName );
      if ( lkup instanceof DataSource ) {
        rtn = (DataSource) lkup;
        FoundDS.put( dsName, rtn );
        return rtn;
      }
    } catch ( NamingException ignored ) {
      // ignore
    }
    if ( firstNe != null ) {
      throw firstNe;
    }
    throw new NamingException( BaseMessages.getString( PKG, "DatabaseUtil.DSNotFound", dsName ) );
  }

  public static void closeSilently( Connection[] connections ) {
    if ( connections == null || connections.length == 0 ) {
      return;
    }
    for ( Connection conn : connections ) {
      closeSilently( conn );
    }
  }

  public static void closeSilently( Connection conn ) {
    if ( conn == null ) {
      return;
    }
    try {
      conn.close();
    } catch ( Throwable e ) {
      // omit
    }
  }

  public static void closeSilently( Statement[] statements ) {
    if ( statements == null || statements.length == 0 ) {
      return;
    }
    for ( Statement st : statements ) {
      closeSilently( st );
    }
  }

  public static void closeSilently( Statement st ) {
    if ( st == null ) {
      return;
    }
    try {
      st.close();
    } catch ( Throwable e ) {
      // omit
    }
  }


  /**
   * Implementation of DatasourceProviderInterface.
   */
  @Override
  public DataSource getNamedDataSource( String datasourceName ) throws DataSourceNamingException {
    ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      return DatabaseUtil.getDataSourceFromJndi( datasourceName, new InitialContext() );
    } catch ( NamingException ex ) {
      throw new DataSourceNamingException( ex );
    } finally {
      Thread.currentThread().setContextClassLoader( original );
    }

  }

  @Override
  public DataSource getNamedDataSource( String datasourceName, DatasourceType type )
    throws DataSourceNamingException {
    if ( type != null ) {
      switch ( type ) {
        case JNDI:
          return getNamedDataSource( datasourceName );
        case POOLED:
          throw new UnsupportedOperationException(
            getClass().getName() + " does not support providing pooled data sources" );
      }
    }
    throw new IllegalArgumentException( "Unsupported data source type: " + type );
  }
}
