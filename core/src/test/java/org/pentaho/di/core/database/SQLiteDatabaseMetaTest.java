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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class SQLiteDatabaseMetaTest {

  private SQLiteDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new SQLiteDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( -1, nativeMeta.getDefaultDatabasePort() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "org.sqlite.JDBC", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:sqlite:WIBBLE", nativeMeta.getURL( "IGNORED", "IGNORED", "WIBBLE" ) );
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.supportsErrorHandling() );
    assertFalse( nativeMeta.supportsGetBlob() );
    assertArrayEquals( new String[] { "sqlite-jdbc-3.41.2.2.jar" }, nativeMeta.getUsedLibraries() );

    assertEquals( "\"FOO\".\"BAR\"", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );


  }

  @Test
  public void testSQLStatements() {
    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO MODIFY BAR TEXT",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATETIME",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATETIME",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    // PK/TK
    assertEquals( "INTEGER PRIMARY KEY AUTOINCREMENT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER PRIMARY KEY AUTOINCREMENT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", false, false, false ) );

    // Numeric Types
    assertEquals( "NUMERIC",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, -6 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -13, 0 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 19, 0 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 11, 0 ), "", "", false, false, false ) );

    // Strings
    assertEquals( "TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 50, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", DatabaseMeta.CLOB_LENGTH + 1, 0 ), "", "", false, false, false ) );

    // Others
    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 15, 0 ), "", "", false, false, false ) );

    assertEquals( "UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( "UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
