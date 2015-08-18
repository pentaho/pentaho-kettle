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
 */
public interface ExtendedDSProviderInterface extends DataSourceProviderInterface {

  public static final int NS_JNDI_NAME = 1;

  public static final int NS_DATASOURCE_NAME = 2;


  /**
   * Returns a named javax.sql.DataSource
   *
   * @param datasourceName
   * @param namespace
   * @return javax.sql.DataSource
   * @throws DataSourceNamingException
   */
  public DataSource getNSNamesDataSource( String datasourceName, int namespace ) throws DataSourceNamingException;

}
