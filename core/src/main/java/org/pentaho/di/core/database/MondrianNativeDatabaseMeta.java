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
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    return null;
  }

}
