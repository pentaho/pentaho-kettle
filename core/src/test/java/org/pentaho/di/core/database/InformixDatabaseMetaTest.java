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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class InformixDatabaseMetaTest {

  private InformixDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new InformixDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 1526, nativeMeta.getDefaultDatabasePort() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    nativeMeta.setServername( "FOODBNAME" );
    assertEquals( "com.informix.jdbc.IfxDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:informix-sqli://FOO:BAR/WIBBLE:INFORMIXSERVER=FOODBNAME;DELIMIDENT=Y", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:informix-sqli://FOO:/WIBBLE:INFORMIXSERVER=FOODBNAME;DELIMIDENT=Y", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Pretty sure this is a bug (colon after foo)
    assertTrue( nativeMeta.needsPlaceHolder() );
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertTrue( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.needsToLockAllTables() );
    assertArrayEquals( new String[] { "ifxjdbc.jar" }, nativeMeta.getUsedLibraries() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "SELECT FIRST 1 * FROM FOO", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT FIRST 1 * FROM FOO", nativeMeta.getSQLTableExists( "FOO" ) );
    assertEquals( "SELECT FIRST 1 FOO FROM BAR", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( "SELECT FIRST 1 FOO FROM BAR", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );
    assertEquals( "TRUNCATE TABLE FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (1, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "LOCK TABLE FOO IN EXCLUSIVE MODE;" + lineSep + "LOCK TABLE BAR IN EXCLUSIVE MODE;" + lineSep,
        nativeMeta.getSQLLockTables( new String[] { "FOO", "BAR" } ) );

    assertNull( nativeMeta.getSQLUnlockTables( new String[] { "FOO", "BAR" } ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATETIME YEAR to FRACTION",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATETIME",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );

    // Simple hack to prevent duplication of code. Checking the case of supported boolean type
    // both supported and unsupported. Should return BOOLEAN if supported, or CHAR(1) if not.
    String[] typeCk = new String[] { "CHAR(1)", "BOOLEAN", "CHAR(1)" };
    int i = ( nativeMeta.supportsBooleanDataType() ? 1 : 0 );
    assertEquals( typeCk[i],
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "SERIAL8",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", true, false, false ) );
    assertEquals( "INTEGER PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", false, false, false ) );

    // Note - ValueMetaInteger returns zero always from the precision - so this avoids the weirdness
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", -8, -3 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -8, -3 ), "", "", false, false, false ) ); // Weird if statement ( length and precision less than zero)
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 3 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 9, 0 ), "", "", false, false, false ) ); // Weird if statement

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", DatabaseMeta.CLOB_LENGTH + 1, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(10)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(255)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 255, 0 ), "", "", false, false, false ) );
    assertEquals( "LVARCHAR",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 256, 0 ), "", "", false, false, false ) );
    assertEquals( "LVARCHAR",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32767, 0 ), "", "", false, false, false ) );
    assertEquals( "TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32768, 0 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
