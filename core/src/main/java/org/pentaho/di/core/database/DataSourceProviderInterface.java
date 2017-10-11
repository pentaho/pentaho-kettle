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

  enum DatasourceType {
    JNDI, POOLED
  }
}
