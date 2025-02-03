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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

public class SybaseDatabaseMetaTest {
  private SybaseDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new SybaseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 5001, nativeMeta.getDefaultDatabasePort() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "net.sourceforge.jtds.jdbc.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:jtds:sybase://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:jtds:sybase://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Pretty sure this is a bug - uses port empty or not
    assertEquals( "BAR", nativeMeta.getSchemaTableCombination(  "FOO", "BAR" ) );
    assertFalse( nativeMeta.isRequiringTransactionsOnQueries() );
    assertEquals( "http://jtds.sourceforge.net/faq.html#urlFormat", nativeMeta.getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "jtds-1.2.jar" }, nativeMeta.getUsedLibraries() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15) NULL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15) NULL",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "insert into FOO(FOOVERSION) values (1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATETIME NULL",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATETIME NULL",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, false, false ) );


    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO" ), "", "FOO", false, false, false ) );

    assertEquals( "DOUBLE PRECISION NULL",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "", "", false, false, false ) );

    assertEquals( "DECIMAL(11, 3) NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 11, 3 ), "", "", false, false, false ) );

    assertEquals( "TINYINT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 2, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 3, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 4, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER NULL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 5, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(15) NULL",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );

    assertEquals( "TEXT NULL",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 2050, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
