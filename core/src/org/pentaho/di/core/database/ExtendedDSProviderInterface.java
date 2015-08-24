/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
 * The purpose of this interface is to provide a way to get data sources by unique DS identifier.
 *
 * @deprecated This interface exists only for backward compatibility and will be removed in 6.0
 */
@Deprecated
public interface ExtendedDSProviderInterface extends DataSourceProviderInterface {

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
