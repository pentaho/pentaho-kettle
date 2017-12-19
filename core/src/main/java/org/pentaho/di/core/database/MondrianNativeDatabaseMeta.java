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

import org.pentaho.di.core.row.ValueMetaInterface;

public class MondrianNativeDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  @Override
  public String[] getUsedLibraries() {
    // return new String[] { "mysql-connector-java-3.1.14-bin.jar" };
    return null;
  }

  @Override
  public String getDriverClass() {
    return "mondrian.olap4j.MondrianOlap4jDriver";
  }

  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    // jdbc:mondrian:Datasource=jdbc/SampleData;Catalog=./foodmart/FoodMart.xml;
    return "jdbc:mondrian:Datasource=jdbc/" + databaseName + ";Catalog=" + hostname;
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    return null;
  }

}
