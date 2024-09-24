/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;

import java.util.HashMap;
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
}
