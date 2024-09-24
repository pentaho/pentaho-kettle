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
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class CacheDatabaseMetaTest {
  private CacheDatabaseMeta cdm;

  @Before
  public void setupBefore() {
    cdm = new CacheDatabaseMeta();
    cdm.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {

    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        cdm.getAccessTypeList() );
    assertEquals( 1972, cdm.getDefaultDatabasePort() );
    assertFalse( cdm.supportsSetCharacterStream() );
    assertFalse( cdm.isFetchSizeSupported() );
    assertFalse( cdm.supportsAutoInc() );
    assertEquals( "com.intersys.jdbc.CacheDriver", cdm.getDriverClass() );
    assertEquals( "jdbc:Cache://FOO:BAR/WIBBLE", cdm.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertArrayEquals( new String[] { "CacheDB.jar" }, cdm.getUsedLibraries() );
    assertTrue( cdm.requiresCreateTablePrimaryKeyAppend() );
    assertFalse( cdm.supportsNewLinesInSQL() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR VARCHAR(15) ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR TIMESTAMP ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR TIMESTAMP ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR CHAR(1) ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaBoolean( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR INT ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 0, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR DOUBLE ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "", false, "", false ) ); // I believe this is a bug!
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR DOUBLE ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, -7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR DOUBLE ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", -10, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR DECIMAL(5, 7) ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN ( BAR  UNKNOWN ) ",
        cdm.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );
    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + lineSep,
        cdm.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );
    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR VARCHAR(15)",
        cdm.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

  }

}
