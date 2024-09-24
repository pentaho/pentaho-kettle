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

import org.pentaho.di.core.database.util.DatabaseUtil;

/**
 * This class provides the extension point for tools to override the default DataSourceProviderInterface implementation.
 * To override the default implementation, simply call the setter with your own implementation.
 *
 * @author mbatchel Jan 9, 2009
 *
 */
public class DataSourceProviderFactory {

  private static DataSourceProviderInterface dataSourceProviderInterface;

  static {
    //
    // Sets the default provider to DatabaseUtil.
    //
    DataSourceProviderFactory.setDataSourceProviderInterface( new DatabaseUtil() );
  }

  private DataSourceProviderFactory() {
    // Private constructor keeps this from being constructed
  }

  public static void setDataSourceProviderInterface( DataSourceProviderInterface value ) {
    dataSourceProviderInterface = value;
  }

  public static DataSourceProviderInterface getDataSourceProviderInterface() {
    return dataSourceProviderInterface;
  }

}
