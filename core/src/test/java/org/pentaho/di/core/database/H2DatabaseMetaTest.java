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
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class H2DatabaseMetaTest {
  H2DatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new H2DatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertEquals( 8082, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "org.h2.Driver", nativeMeta.getDriverClass() );

    assertEquals( "jdbc:h2:WIBBLE", nativeMeta.getURL( "", "", "WIBBLE" ) );
    assertEquals( "jdbc:h2:tcp://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );

    assertEquals( "jdbc:h2:WIBBLE", nativeMeta.getURL( "", "-1", "WIBBLE" ) );
    assertEquals( "jdbc:h2:mem:WIBBLE", nativeMeta.getURL( "", "", "mem:WIBBLE" ) );

    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );

    assertArrayEquals( new String[] { "h2.jar" }, nativeMeta.getUsedLibraries() );
    assertArrayEquals( new String[] {
      "CURRENT_TIMESTAMP", "CURRENT_TIME", "CURRENT_DATE", "CROSS", "DISTINCT", "EXCEPT", "EXISTS", "FROM",
      "FOR", "FALSE", "FULL", "GROUP", "HAVING", "INNER", "INTERSECT", "IS", "JOIN", "LIKE", "MINUS", "NATURAL",
      "NOT", "NULL", "ON", "ORDER", "PRIMARY", "ROWNUM", "SELECT", "SYSDATE", "SYSTIME", "SYSTIMESTAMP",
      "TODAY", "TRUE", "UNION", "WHERE" }, nativeMeta.getReservedWords() );

    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertEquals( "\"FOO\".\"BAR\"", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertTrue( nativeMeta.supportsGetBlob() );
    assertFalse( nativeMeta.supportsSetCharacterStream() );
    assertFalse( nativeMeta.supportsPreparedStatementMetadataRetrieval() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "TRUNCATE TABLE FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "SELECT * FROM FOO", nativeMeta.getSQLQueryFields(  "FOO" ) );
    assertEquals( "SELECT 1 FROM FOO", nativeMeta.getSQLTableExists(  "FOO" ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TIMESTAMP",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR TIMESTAMP",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR CHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBoolean( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 0, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR INTEGER",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(10, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(10, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(21, 4)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 21, 4 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, -7 ), "", false, "", false ) ); // Bug here - invalid SQL

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(22, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", -10, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(5, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR UNKNOWN",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR IDENTITY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR IDENTITY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 26, 8 ), "BAR", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR IDENTITY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 26, 8 ), "", true, "BAR", false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ALTER BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ALTER BAR VARCHAR(2147483647)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD BAR SMALLINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 4, 0 ), "", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TINYINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 2, 0 ), "", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR IDENTITY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(22, 0)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 1, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 16777250, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR BLOB",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR", 16777250, 0 ), "", false, "", false ) );

    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (0, 1)", nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

}
