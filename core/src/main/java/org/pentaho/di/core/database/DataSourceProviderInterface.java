/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.database;

import javax.sql.DataSource;

/**
 * The purpose of this interface is to provide a way to get data sources from more places than just JNDI.
 *
 * @author mbatchel Jan 8, 2009
 *
 */
public interface DataSourceProviderInterface {

  /**
   * Returns a named javax.sql.DataSource
   *
   * @param datasourceName
   * @return javax.sql.DataSource
   */
  DataSource getNamedDataSource( String datasourceName ) throws DataSourceNamingException;

  /**
   * Returns the named data source of respecting its <code>type</code>
   *
   * @param datasourceName name of the desired data source
   * @param type           data source's type
   * @return named data source
   * @throws DataSourceNamingException
   */
  DataSource getNamedDataSource( String datasourceName, DatasourceType type ) throws DataSourceNamingException;

  /**
   * Returns the specified data source of respecting its <code>type</code>
   *
   * @param dbMeta  definition of the datasource provided via DatabaseMeta
   * @param type    data source's type
   * @return named data source
   * @throws DataSourceNamingException
   */
  default DataSource getPooledDataSourceFromMeta( DatabaseMeta dbMeta, DatasourceType type ) throws DataSourceNamingException {
    throw new UnsupportedOperationException( "getNamedDataSourceFromMeta is not supported" );
  }

  /**
   * Invalidate the named data source of respecting its <code>type</code>
   *
   * @param datasourceName name of the desired data source
   * @param type           data source's type
   * @return named data source
   * @throws DataSourceNamingException
   */
  default DataSource invalidateNamedDataSource( String datasourceName, DatasourceType type ) throws DataSourceNamingException {
    throw new UnsupportedOperationException( "The invalidateNamedDataSource method was not implemented yet." );
  }

  enum DatasourceType {
    JNDI, POOLED
  }
}
