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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;


public class VectorWiseDatabaseMetaTest {


  @Test
  public void testIngresOverrides() throws Exception {
    VectorWiseDatabaseMeta nativeMeta, odbcMeta;
    nativeMeta = new VectorWiseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    odbcMeta = new VectorWiseDatabaseMeta();
    odbcMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
    assertEquals( "sun.jdbc.odbc.JdbcOdbcDriver", odbcMeta.getDriverClass() );
    assertEquals( "jdbc:odbc:FOO", odbcMeta.getURL(  "IGNORED", "IGNORED", "FOO" ) );
    assertEquals( "jdbc:ingres://FOO:VW7/WIBBLE", nativeMeta.getURL( "FOO", "VW7", "WIBBLE" ) );
    assertEquals( "jdbc:ingres://FOO:VW7/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Empty bit
    assertEquals( "jdbc:ingres://FOO:VW7/WIBBLE", nativeMeta.getURL( "FOO", "-1", "WIBBLE" ) ); // Empty bit
    assertEquals( "jdbc:ingres://FOO:2345/WIBBLE", nativeMeta.getURL( "FOO", "2345", "WIBBLE" ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (0, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );

    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + System.getProperty( "line.separator" ),
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "CALL VECTORWISE( COMBINE 'FOO - FOO' )",
        nativeMeta.getTruncateTableStatement( "FOO" ) );

    assertArrayEquals( new String[] { "iijdbc.jar" }, nativeMeta.getUsedLibraries() );

    assertFalse( nativeMeta.supportsGetBlob() );

  }

  @Test
  public void testGetFieldDefinition() {
    VectorWiseDatabaseMeta nativeMeta, odbcMeta;
    nativeMeta = new VectorWiseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    odbcMeta = new VectorWiseDatabaseMeta();
    odbcMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );

    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", true, false, false ) );
    assertEquals( "GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "FOO", true, false, false ) );
    assertEquals( "BIGINT PRIMARY KEY NOT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "BIGINT PRIMARY KEY NOT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", false, false, false ) );

    // Integer tests
    assertEquals( "BIGINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 6, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 3, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER1",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 2, 0 ), "", "", false, false, false ) );

    assertEquals( "FLOAT8",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 6, 3 ), "", "", false, false, false ) );

    // String Types
    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(32000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32000, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(32000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32050, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(9999)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO" ), "", "", true, false, false ) );
    assertEquals( "VARCHAR(9999)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(9999)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", -34, 0 ), "", "", false, false, false ) );

    // Unknown
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }


}
