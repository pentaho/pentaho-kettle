/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.DatabasePluginType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseMetaSupportsSchemasTest {

  private DatabaseMeta meta;

  @BeforeClass
  public static void once() throws KettleException {
    DatabasePluginType.getInstance().searchPlugins();
  }

  @Before
  public void setup() {
    meta = new DatabaseMeta();
  }

  @Test
  public void getQuotedSchemaTableCombination_EmptySchema_MySQL() {
    meta.setDatabaseInterface( new MySQLDatabaseMeta() );

    assertFalse( "MySQL doesn't support schemas.", meta.supportsSchemas() );
    assertEquals( "logs", meta.getQuotedSchemaTableCombination( "", "logs" ) );
  }

  @Test
  public void getQuotedSchemaTableCombination_EmptySchema_Oracle() {
    meta.setDatabaseInterface( new OracleDatabaseMeta() );

    assertTrue( "Oracle supports schemas.", meta.supportsSchemas() );
    assertEquals( "logs", meta.getQuotedSchemaTableCombination( "", "logs" ) );
  }

  @Test
  public void getQuotedSchemaTableCombination_WithSchema_MariaDB() {
    meta.setDatabaseInterface( new MariaDBDatabaseMeta() );

    assertFalse( "MariaDB doesn't support schemas.", meta.supportsSchemas() );
    assertEquals( "logs", meta.getQuotedSchemaTableCombination( "logging", "logs" ) );
  }

  @Test
  public void getQuotedSchemaTableCombination_WithSchema_PostgreSQL() {
    meta.setDatabaseInterface( new PostgreSQLDatabaseMeta() );

    assertTrue( "PostgreSQL supports schemas.", meta.supportsSchemas() );
    assertEquals( "logging.logs", meta.getQuotedSchemaTableCombination( "logging", "logs" ) );
  }
}
