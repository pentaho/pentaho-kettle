/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.database;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SnowflakeHVDatabaseMetaTest {

  @Test
  public void urlWillContainWarehouse() {
    SnowflakeHVDatabaseMeta dbMeta = new SnowflakeHVDatabaseMeta();
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.getAttributes().setProperty( SnowflakeHVDatabaseMeta.WAREHOUSE, "giant" );
    HashMap<String, String> options = new HashMap<>();
    dbMeta.putOptionalOptions( options );
    assertEquals( 1, options.size() );
    assertEquals( "giant", options.get( "SNOWFLAKEHV.warehouse" ) );
  }

  @Test
  public void testGetSQLListOfSchemasWithoutParameter() {
    SnowflakeHVDatabaseMeta snowflakeHVDatabaseMeta = spy( new SnowflakeHVDatabaseMeta() );
    snowflakeHVDatabaseMeta.getSQLListOfSchemas();

    verify( snowflakeHVDatabaseMeta ).getSQLListOfSchemas( null );
  }

  @Test
  public void testGetSQLListOfSchemasWithParameterNull() {
    SnowflakeHVDatabaseMeta snowflakeHVDatabaseMeta = spy( new SnowflakeHVDatabaseMeta() );
    String databaseName = UUID.randomUUID().toString();
    doReturn( databaseName ).when( snowflakeHVDatabaseMeta ).getDatabaseName();

    String result = snowflakeHVDatabaseMeta.getSQLListOfSchemas( null );
    String expected = "SELECT SCHEMA_NAME AS \"name\" FROM " + databaseName + ".INFORMATION_SCHEMA.SCHEMATA";

    verify( snowflakeHVDatabaseMeta ).getSQLListOfSchemas( null );
    assertEquals( expected, result );
  }

  @Test
  public void testGetSQLListOfSchemasWithParameter() {
    SnowflakeHVDatabaseMeta snowflakeHVDatabaseMeta = spy( new SnowflakeHVDatabaseMeta() );
    String databaseName = UUID.randomUUID().toString();
    String databaseNameSubstitute = UUID.randomUUID().toString();
    doReturn( databaseName ).when( snowflakeHVDatabaseMeta ).getDatabaseName();
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseNameSubstitute ).when( databaseMeta ).environmentSubstitute( databaseName );

    String result = snowflakeHVDatabaseMeta.getSQLListOfSchemas( databaseMeta );
    String expected = "SELECT SCHEMA_NAME AS \"name\" FROM " + databaseNameSubstitute + ".INFORMATION_SCHEMA.SCHEMATA";

    assertEquals( expected, result );
  }

  @Test
  public void setConnectionSpecificInfoFromAttributes_setsWarehouseAttribute() {
    SnowflakeHVDatabaseMeta dbMeta = new SnowflakeHVDatabaseMeta();
    Map<String, String> attributes = new HashMap<>();
    attributes.put( SnowflakeHVDatabaseMeta.WAREHOUSE, "testWarehouse" );

    dbMeta.setConnectionSpecificInfoFromAttributes( attributes );

    assertEquals( "testWarehouse", dbMeta.getAttributes().getProperty( SnowflakeHVDatabaseMeta.WAREHOUSE ) );
  }
}
