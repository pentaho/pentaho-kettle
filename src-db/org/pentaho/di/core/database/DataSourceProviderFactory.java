/*
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.database;

import org.pentaho.di.core.database.util.DatabaseUtil;

/**
 * This class provides the extension point for tools to
 * override the default DataSourceProviderInterface implementation. 
 * To override the default implementation, simply call the setter
 * with your own implementation.
 * 
 * @author mbatchel
 * Jan 9, 2009
 * 
 */
public class DataSourceProviderFactory {

  private static DataSourceProviderInterface dataSourceProviderInterface; 

  static {
    //
    // Sets the default provider to DatabaseUtil.
    //
    DataSourceProviderFactory.setDataSourceProviderInterface(new DatabaseUtil());
  }
  
  private DataSourceProviderFactory() {
    // Private constructor keeps this from being constructed
  }
  
  public static void setDataSourceProviderInterface(DataSourceProviderInterface value) {
    dataSourceProviderInterface = value;
  }
  
  public static DataSourceProviderInterface getDataSourceProviderInterface() {
    return dataSourceProviderInterface;
  }
  
}
