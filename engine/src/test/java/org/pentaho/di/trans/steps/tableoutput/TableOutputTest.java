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


package org.pentaho.di.trans.steps.tableoutput;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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

  private static RowMetaInterface filled;
  private static RowMetaInterface empty;
  private static String[] sample = { "1", "2", "3" };

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
    doReturn( "1" ).when( tableOutputSpy ).getStepExecutionId();

    filled = createRowMeta( sample, false );
    empty = createRowMeta( sample, true );
  }

  @Test
  public void testWriteToTable() throws Exception {
    tableOutputSpy.writeToTable( mock( RowMetaInterface.class ), new Object[] {} );
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
    when( tableOutputMeta.getTableName() ).thenReturn( "fooTable" );
    when( tableOutputMeta.getSchemaName() ).thenReturn( "barSchema" );

    tableOutputSpy.truncateTable();
    verify( db ).truncateTable( any(), any() );
  }

  @Test
  public void testTruncateTable_on_PartitionId() throws Exception {
    when( tableOutputMeta.truncateTable() ).thenReturn( true );
    when( tableOutputSpy.getCopy() ).thenReturn( 1 );
    when( tableOutputSpy.getUniqueStepNrAcrossSlaves() ).thenReturn( 0 );
    when( tableOutputSpy.getPartitionID() ).thenReturn( "partition id" );
    when( tableOutputMeta.getTableName() ).thenReturn( "fooTable" );
    when( tableOutputMeta.getSchemaName() ).thenReturn( "barSchema" );

    tableOutputSpy.truncateTable();
    verify( db ).truncateTable( any(), any() );
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
    Object[] row = new Object[] {};
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
    Object[] row = new Object[] {};
    doReturn( row ).when( tableOutputSpy ).getRow();
    tableOutputSpy.first = false;
    doNothing().when( tableOutputSpy ).putRow( any(), any() );
    doReturn( null ).when( tableOutputSpy ).writeToTable( any( RowMetaInterface.class ), any( row.getClass() ) );

    boolean result = tableOutputSpy.processRow( tableOutputMeta, tableOutputData );

    assertTrue( result );
    verify( tableOutputSpy, never() ).truncateTable();
  }

  @Test
  public void testInit_unsupportedConnection() {

    TableOutputMeta meta = mock( TableOutputMeta.class );
    TableOutputData data = mock( TableOutputData.class );

    DatabaseInterface dbInterface = mock( DatabaseInterface.class );

    doNothing().when( tableOutputSpy ).logError( anyString() );

    when( meta.getCommitSize() ).thenReturn( "1" );
    when( meta.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.getDatabaseInterface() ).thenReturn( dbInterface );

    String unsupportedTableOutputMessage = "unsupported exception";
    when( dbInterface.getUnsupportedTableOutputMessage() ).thenReturn( unsupportedTableOutputMessage );

    //Will cause the Kettle Exception
    when( dbInterface.supportsStandardTableOutput() ).thenReturn( false );

    tableOutputSpy.init( meta, data );

    KettleException ke = new KettleException( unsupportedTableOutputMessage );
    verify( tableOutputSpy, times( 1 ) ).logError( "An error occurred intialising this step: " + ke.getMessage() );
  }

  @Test
  public void validationRowMetaTest() throws Exception {
    Method m = TableOutput.class.getDeclaredMethod( "isValidRowMeta", RowMetaInterface.class );
    m.setAccessible( true );
    Object result1 = m.invoke( null, filled );
    Object result2 = m.invoke( null, empty );
    assertTrue( Boolean.parseBoolean( result1 + "" ) );
    assertFalse( Boolean.parseBoolean( result2 + "" ) );
  }

  private RowMetaInterface createRowMeta( String[] args, boolean hasEmptyFields ) {
    RowMetaInterface result = new RowMeta();
    if ( hasEmptyFields ) {
      result.addValueMeta( new ValueMetaString( "" ) );
    }
    for ( String s : args ) {
      result.addValueMeta( new ValueMetaString( s ) );
    }
    return result;
  }
}
