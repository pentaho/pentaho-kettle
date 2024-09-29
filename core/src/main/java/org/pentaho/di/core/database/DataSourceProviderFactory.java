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
