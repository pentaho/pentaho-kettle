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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith( MockitoJUnitRunner.class )
public class GenericDatabaseMetaTest {
  GenericDatabaseMeta nativeMeta;

  @Mock
  GenericDatabaseMeta mockedMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new GenericDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    Properties attrs = new Properties();
    attrs.put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, "foo.bar.wibble" );
    attrs.put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, "jdbc:foo:bar://foodb" );
    nativeMeta.setAttributes( attrs );
    assertEquals( "foo.bar.wibble", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:foo:bar://foodb", nativeMeta.getURL( "NOT", "GOINGTO", "BEUSED" ) );
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsPreparedStatementMetadataRetrieval() );
    assertArrayEquals( new String[] { }, nativeMeta.getUsedLibraries() );
    assertFalse( nativeMeta.supportsPreparedStatementMetadataRetrieval() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
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

    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE PRECISION",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 0, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR INTEGER",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(10, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(10, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(21, 4)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 21, 4 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, -7 ), "", false, "", false ) ); // Bug here - invalid SQL

    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(22, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE PRECISION",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", -10, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(5, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR  UNKNOWN",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGSERIAL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGSERIAL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 26, 8 ), "BAR", true, "", false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR()",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "", true ) ); // I think this is a bug ..

    assertEquals( "ALTER TABLE FOO ADD BAR SMALLINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 4, 0 ), "", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGSERIAL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR NUMERIC(22, 0)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 1, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 16777250, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR  UNKNOWN",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR", 16777250, 0 ), "", false, "", false ) );

    assertEquals( "insert into FOO(FOOVERSION) values (1)", nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );

  }

  @Test
  public void testSettingDialect() {
    String dialect = "testDialect";
    DatabaseInterface[] dbInterfaces = new DatabaseInterface[] { mockedMeta };
    try ( MockedStatic<DatabaseMeta> dbMeta = Mockito.mockStatic( DatabaseMeta.class ) ) {
      dbMeta.when( () -> DatabaseMeta.getDatabaseInterfaces() ).thenReturn( dbInterfaces );
      Mockito.when( mockedMeta.getPluginName() ).thenReturn( dialect );
      nativeMeta.addAttribute( "DATABASE_DIALECT_ID", dialect );
      assertEquals( mockedMeta, nativeMeta.getDatabaseDialectInternal() );
    }
  }
  @Test
  public void setConnectionSpecificInfoFromAttributes_setsAllAttributes() {
    GenericDatabaseMeta dbMeta = new GenericDatabaseMeta();
    Map<String, String> attributes = new HashMap<>();
    attributes.put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, "jdbc:test:url" );
    attributes.put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, "com.test.Driver" );

    dbMeta.setConnectionSpecificInfoFromAttributes( attributes );

    assertEquals( "jdbc:test:url", dbMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL ) );
    assertEquals( "com.test.Driver", dbMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS ) );
  }
}
