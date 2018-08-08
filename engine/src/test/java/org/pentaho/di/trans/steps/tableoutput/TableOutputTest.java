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

package org.pentaho.di.trans.steps.tableoutput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.sql.Connection;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TableOutputTest {
  private DatabaseMeta databaseMeta;

  private StepMeta stepMeta;

  private TableOutput tableOutput, tableOutputSpy;
  private TableOutputMeta tableOutputMeta, tableOutputMetaSpy;
  private TableOutputData tableOutputData, tableOutputDataSpy;
  private Database db;

  @Before
  public void setUp() throws Exception {
    databaseMeta = mock( DatabaseMeta.class );
    doReturn( "" ).when( databaseMeta ).quoteField( anyString() );

    tableOutputMeta = mock( TableOutputMeta.class );
    doReturn( databaseMeta ).when( tableOutputMeta ).getDatabaseMeta();

    stepMeta = mock( StepMeta.class );
    doReturn( "step" ).when( stepMeta ).getName();
    doReturn( mock( StepPartitioningMeta.class ) ).when( stepMeta ).getTargetStepPartitioningMeta();
    doReturn( tableOutputMeta ).when( stepMeta ).getStepMetaInterface();

    db = mock( Database.class );
    doReturn( mock( Connection.class ) ).when( db ).getConnection();

    tableOutputData = mock( TableOutputData.class );
    tableOutputData.db = db;
    tableOutputData.tableName = "sas";
    tableOutputData.preparedStatements = mock( Map.class );
    tableOutputData.commitCounterMap = mock( Map.class );

    TransMeta transMeta = mock( TransMeta.class );
    doReturn( stepMeta ).when( transMeta ).findStep( anyString() );

    tableOutput = new TableOutput( stepMeta, tableOutputData, 1, transMeta, mock( Trans.class ) );
    tableOutput.setData( tableOutputData );
    tableOutput.setMeta( tableOutputMeta );
    tableOutputSpy = spy( tableOutput );
    doReturn( stepMeta ).when( tableOutputSpy ).getStepMeta();
    doReturn( false ).when( tableOutputSpy ).isRowLevel();
    doReturn( false ).when( tableOutputSpy ).isDebug();
    doNothing().when( tableOutputSpy ).logDetailed( anyString() );
  }

  @Test
  public void testWriteToTable() throws Exception {
    tableOutputSpy.writeToTable( mock( RowMetaInterface.class ), new Object[]{} );
  }

  @Test
  public void testTruncateTable_off() throws Exception {
    tableOutputSpy.truncateTable();
    verify( db, never() ).truncateTable( anyString(), anyString() );
  }

  @Test
  public void testTruncateTable_on() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );
    when( tableOutputSpy.getCopy() ).thenReturn( 0 );
    when( tableOutputSpy.getUniqueStepNrAcrossSlaves() ).thenReturn( 0 );

    tableOutputSpy.truncateTable();
    verify( db ).truncateTable( anyString(), anyString() );
  }

  @Test
  public void testTruncateTable_on_PartitionId() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );
    when( tableOutputSpy.getCopy() ).thenReturn( 1 );
    when( tableOutputSpy.getUniqueStepNrAcrossSlaves() ).thenReturn( 0 );
    when( tableOutputSpy.getPartitionID() ).thenReturn( "partition id" );

    tableOutputSpy.truncateTable();
    verify( db ).truncateTable( anyString(), anyString() );
  }

  @Test
  public void testProcessRow_truncatesIfNoRowsAvailable() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );

    doReturn( null ).when( tableOutputSpy ).getRow();

    boolean result = tableOutputSpy.processRow( tableOutputMeta, tableOutputData );

    assertFalse( result );
    verify( tableOutputSpy ).truncateTable();
  }

  @Test
  public void testProcessRow_doesNotTruncateIfNoRowsAvailableAndTruncateIsOff() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( false );

    doReturn( null ).when( tableOutputSpy ).getRow();

    boolean result = tableOutputSpy.processRow( tableOutputMeta, tableOutputData );

    assertFalse( result );
    verify( tableOutputSpy, never() ).truncateTable();
  }

  @Test
  public void testProcessRow_truncatesOnFirstRow() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );
    Object[] row = new Object[]{};
    doReturn( row ).when( tableOutputSpy ).getRow();

    try {
      boolean result = tableOutputSpy.processRow( tableOutputMeta, tableOutputData );
    } catch ( NullPointerException npe ) {
      // not everything is set up to process an entire row, but we don't need that for this test
    }
    verify( tableOutputSpy, times( 1 ) ).truncateTable();
  }

  @Test
  public void testProcessRow_doesNotTruncateOnOtherRows() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );
    Object[] row = new Object[]{};
    doReturn( row ).when( tableOutputSpy ).getRow();
    tableOutputSpy.first = false;
    doReturn( null ).when( tableOutputSpy ).writeToTable( any( RowMetaInterface.class ), any( row.getClass() ) );

    boolean result = tableOutputSpy.processRow( tableOutputMeta, tableOutputData );

    assertTrue( result );
    verify( tableOutputSpy, never() ).truncateTable();
  }
}
