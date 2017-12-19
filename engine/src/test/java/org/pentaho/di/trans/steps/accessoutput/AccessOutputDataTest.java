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

package org.pentaho.di.trans.steps.accessoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;

public class AccessOutputDataTest {

  AccessOutputData data;
  File mdbFile;

  @Before
  public void setUp() throws IOException {
    data = new AccessOutputData();
    mdbFile = File.createTempFile( "PDI_AccessOutputDataTest", ".mdb" );
    mdbFile.deleteOnExit();
  }

  RowMetaInterface generateRowMeta() {
    RowMetaInterface row = new RowMeta();
    row.addValueMeta( new ValueMetaInteger( "id" ) );
    row.addValueMeta( new ValueMetaString( "UUID" ) );
    return row;
  }

  List<Object[]> generateRowData( int rowCount ) {
    List<Object[]> rows = new ArrayList<Object[]>();
    for ( int i = 0; i < rowCount; i++ ) {
      rows.add( new Object[]{ i, UUID.randomUUID().toString() } );
    }
    return rows;
  }

  @Test
  public void testCreateDatabase() throws IOException {
    assertNull( data.db );
    data.createDatabase( mdbFile );
    assertNotNull( data.db );
    assertTrue( mdbFile.exists() );

    assertNull( data.table );
    data.truncateTable();
    assertNull( data.table );

    data.closeDatabase();
  }

  @Test
  public void testCreateTable() throws IOException {
    data.createDatabase( mdbFile );
    data.createTable( "thisSampleTable", generateRowMeta() );
    assertTrue( data.db.getTableNames().contains( "thisSampleTable" ) );
    data.closeDatabase();
  }

  @Test
  public void testTruncateTable() throws IOException {
    data.createDatabase( mdbFile );
    data.createTable( "TruncatingThisTable", generateRowMeta() );

    data.addRowsToTable( generateRowData( 10 ) );
    assertEquals( 10, data.table.getRowCount() );

    data.truncateTable();
    assertEquals( 0, data.table.getRowCount() );

    data.addRowToTable( generateRowData( 1 ).get( 0 ) );
    assertEquals( 1, data.table.getRowCount() );
    data.closeDatabase();
  }
}
