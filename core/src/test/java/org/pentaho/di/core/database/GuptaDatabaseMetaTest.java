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
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class GuptaDatabaseMetaTest {

  private GuptaDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new GuptaDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 2155, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertEquals( "jdbc.gupta.sqlbase.SqlbaseDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:sqlbase://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:sqlbase://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Pretty sure this is a bug (colon after foo)
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.supportsCatalogs() );
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );

    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );

    assertArrayEquals( new String[] { "SQLBaseJDBC.jar" }, nativeMeta.getUsedLibraries() );
    assertTrue( nativeMeta.isSystemTable( "SYSFOO" ) );
    assertFalse( nativeMeta.isSystemTable( "SySBAR" ) );
    assertFalse( nativeMeta.isSystemTable( "BARSYS" ) );
    assertFalse( nativeMeta.supportsPreparedStatementMetadataRetrieval() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP BAR" + lineSep + ";" + lineSep + "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (0, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATETIME NULL",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATETIME NULL",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );

    assertEquals( "INTEGER NOT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER NOT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 8, 0 ), "", "FOO", false, false, false ) );


    // Note - ValueMetaInteger returns zero always from the precision - so this avoids the weirdness
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", -8, -3 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -8, -3 ), "", "", false, false, false ) ); // Weird if statement ( length and precision less than zero)
    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 3 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) ); // Weird if statement
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 9, 0 ), "", "", false, false, false ) ); // Weird if statement

    assertEquals( "LONG VARCHAR",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 255, 0 ), "", "", false, false, false ) );

    assertEquals( "LONG VARCHAR",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", -33, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(0)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }


}
