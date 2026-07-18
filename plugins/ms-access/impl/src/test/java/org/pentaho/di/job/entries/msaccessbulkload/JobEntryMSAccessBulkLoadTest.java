/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.job.entries.msaccessbulkload;

import org.pentaho.di.core.Result;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JobEntryMSAccessBulkLoadTest {

  private static final String TABLE_NAME = "Users";
  private static final String FIELD_NAME = "UserName";

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testImportFilePreservesExistingTable() throws Exception {
    File sourceDataFile = File.createTempFile( "MSAccessBulkLoadSource", ".csv" );
    sourceDataFile.deleteOnExit();
    Files.write( sourceDataFile.toPath(), Arrays.asList( FIELD_NAME, "Alice" ), StandardCharsets.UTF_8 );

    File targetDbFile = File.createTempFile( "MSAccessBulkLoadTarget", ".mdb" );
    targetDbFile.delete();
    targetDbFile.deleteOnExit();

    try ( Database db = new DatabaseBuilder( targetDbFile ).setFileFormat( Database.FileFormat.V2000 ).create() ) {
      Table seedTable =
        new TableBuilder( TABLE_NAME ).addColumn( new ColumnBuilder( FIELD_NAME, DataType.TEXT ) ).toTable( db );
      seedTable.addRow( "seed" );
    }

    JobEntryMSAccessBulkLoad bulkLoad = new JobEntryMSAccessBulkLoad();
    boolean imported = invokeImportFile(
      bulkLoad, sourceDataFile.getAbsolutePath(), ",", targetDbFile.getAbsolutePath(), TABLE_NAME, new Result() );
    assertTrue( imported );

    try ( Database db = DatabaseBuilder.open( targetDbFile ) ) {
      Table originalTable = db.getTable( TABLE_NAME );
      assertNotNull( originalTable );
      assertEquals( 1, countRows( originalTable ) );
      assertTrue( db.getTableNames().size() > 1 );
      assertTrue( countTotalRows( db ) >= 2 );
    }
  }

  private boolean invokeImportFile( JobEntryMSAccessBulkLoad bulkLoad, String sourceFilename, String delimiter,
    String targetFilename, String tableName, Result result ) throws Exception {
    Method importFile =
      JobEntryMSAccessBulkLoad.class.getDeclaredMethod( "importFile", String.class, String.class, String.class,
        String.class, Result.class, org.pentaho.di.job.Job.class );
    importFile.setAccessible( true );
    return (Boolean) importFile.invoke( bulkLoad, sourceFilename, delimiter, targetFilename, tableName, result, null );
  }

  private int countRows( Table table ) {
    int rowCount = 0;
    for ( Row row : table ) {
      rowCount++;
    }
    return rowCount;
  }

  private int countTotalRows( Database db ) throws IOException {
    int totalRows = 0;
    for ( String tableName : db.getTableNames() ) {
      totalRows += countRows( db.getTable( tableName ) );
    }
    return totalRows;
  }
}