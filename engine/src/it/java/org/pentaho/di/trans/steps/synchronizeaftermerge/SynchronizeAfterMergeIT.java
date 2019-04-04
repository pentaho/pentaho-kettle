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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class SynchronizeAfterMergeIT {

  private static final int COMMIT_SIZE = 10;

  /**
   * is used for check PDI-14413
   * 
   * set the commit size more than row size and then pass data betwen {@link #ROW_SIZE} and {@link #COMMIT_SIZE}
   */
  private static final int ROW_SIZE = 5;

  private static final int ROW_FOR_UPDATE = 18;

  private static final int ROW_FOR_DELETE = 18;

  private static final int ROW_FOR_INSERT = 18;

  private static final String DELETE_FLAG = "deleted";

  private static final String INSERT_FLAG = "insert";

  private static final String UPDATE_FLAG = "update";

  private String injectorStepname = "injectorStepname";

  private String synchronizeAfterMergeStepname = "SynchronizeAfterMerge";

  private TransMeta transMeta;

  private Connection connection;

  @BeforeClass
  public static void beforeClass() throws Exception {
    KettleEnvironment.init();
    DriverManager.registerDriver( new org.h2.Driver() );
  }

  @AfterClass
  public static void afterClass() throws Exception {
    KettleEnvironment.shutdown();
  }

  @After
  public void tearDown() throws SQLException {
    if ( connection != null ) {
      connection.close();
      connection = null;
    }
  }

  @Before
  public void setUp() throws KettleDatabaseException, SQLException {
    connection = DriverManager.getConnection( "jdbc:h2:mem:PERSON;" );
    connection.setAutoCommit( false );
    PreparedStatement stmt = connection.prepareStatement( "CREATE TABLE PERSON (ID INT PRIMARY KEY, personName VARCHAR(64) )" );
    stmt.execute();
    stmt.close();
    stmt = connection.prepareStatement( "INSERT INTO PERSON (ID, personName) VALUES (?, ?)" );
    for ( int i = 0; i < ROW_FOR_UPDATE + ROW_FOR_DELETE; i++ ) {
      stmt.setInt( 1, i );
      stmt.setString( 2, "personName" + i );
      stmt.addBatch();
    }
    stmt.executeBatch();
    stmt.close();
    connection.commit();

    PluginRegistry pluginRegistry = PluginRegistry.getInstance();

    transMeta = new TransMeta();
    transMeta.setName( "SynchronizeAfterMerge" );

    InjectorMeta injectorMeta = new InjectorMeta();
    String injectorPid = pluginRegistry.getPluginId( StepPluginType.class, injectorMeta );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, injectorMeta );
    transMeta.addStep( injectorStep );

    DatabaseMeta dbMeta = spy( new DatabaseMeta() );
    dbMeta.setDatabaseType( "H2" );
    when( dbMeta.getURL() ).thenReturn( "jdbc:h2:mem:PERSON;" );
    when( dbMeta.supportsErrorHandlingOnBatchUpdates() ).thenReturn( false );

    SynchronizeAfterMergeMeta synchronizeAfterMergeMeta = new SynchronizeAfterMergeMeta();
    //set commit size
    synchronizeAfterMergeMeta.setCommitSize( COMMIT_SIZE );
    synchronizeAfterMergeMeta.setDatabaseMeta( dbMeta );
    synchronizeAfterMergeMeta.setKeyCondition( new String[] { "=" } );
    synchronizeAfterMergeMeta.setKeyLookup( new String[] { "ID" } );
    synchronizeAfterMergeMeta.setKeyStream( new String[] { "personName" } );
    synchronizeAfterMergeMeta.setKeyStream2( new String[] { null } );
    synchronizeAfterMergeMeta.setUpdate( new Boolean[] { Boolean.TRUE } );
    synchronizeAfterMergeMeta.setOperationOrderField( "flag" );
    synchronizeAfterMergeMeta.setOrderDelete( DELETE_FLAG );
    synchronizeAfterMergeMeta.setOrderInsert( INSERT_FLAG );
    synchronizeAfterMergeMeta.setOrderUpdate( UPDATE_FLAG );
    synchronizeAfterMergeMeta.setPerformLookup( true );

    synchronizeAfterMergeMeta.setTableName( "PERSON" );
    synchronizeAfterMergeMeta.settablenameInField( false );
    synchronizeAfterMergeMeta.settablenameField( null );
    synchronizeAfterMergeMeta.setUseBatchUpdate( true );
    synchronizeAfterMergeMeta.setUpdateLookup( new String[] { "ID" } );
    synchronizeAfterMergeMeta.setUpdateStream( new String[] { "personName" } );

    String synchronizeAfterMergePid = pluginRegistry.getPluginId( StepPluginType.class, synchronizeAfterMergeStepname );
    StepMeta synchronizeAfterMerge = new StepMeta( synchronizeAfterMergePid, synchronizeAfterMergeStepname, synchronizeAfterMergeMeta );
    transMeta.addStep( synchronizeAfterMerge );

    String dummyResultStepName = "dummyResultStepName";
    DummyTransMeta dummyResultTransMeta = new DummyTransMeta();
    String dummyResultPid = pluginRegistry.getPluginId( StepPluginType.class, dummyResultTransMeta );
    StepMeta dummyResultStep = new StepMeta( dummyResultPid, dummyResultStepName, dummyResultTransMeta );
    transMeta.addStep( dummyResultStep );

    String dummyErrorStepName = "dummyErrorStepName";
    DummyTransMeta dummyErrorTransMeta = new DummyTransMeta();
    String dummyErrorPid = pluginRegistry.getPluginId( StepPluginType.class, dummyErrorTransMeta );
    StepMeta dummyErrorStep = new StepMeta( dummyErrorPid, dummyErrorStepName, dummyErrorTransMeta );
    transMeta.addStep( dummyErrorStep );

    StepErrorMeta stepErrorMeta = new StepErrorMeta( transMeta, synchronizeAfterMerge, dummyErrorStep );
    stepErrorMeta.setEnabled( true );
    synchronizeAfterMerge.setStepErrorMeta( stepErrorMeta );

    TransHopMeta injSynch = new TransHopMeta( injectorStep, synchronizeAfterMerge );
    transMeta.addTransHop( injSynch );

    TransHopMeta synchDummyResult = new TransHopMeta( synchronizeAfterMerge, dummyResultStep );
    transMeta.addTransHop( synchDummyResult );

    TransHopMeta synchDummyError = new TransHopMeta( synchronizeAfterMerge, dummyErrorStep );
    transMeta.addTransHop( synchDummyError );
  }

  @Test
  public void testProcessRow_Itterupted() throws Exception {
    processRow( TransProcessControl.ITTERUPT );
  }

  @Test
  public void testProcessRow_RowSizeEqualsCommitSize() throws Exception {
    transMeta.setSizeRowset( COMMIT_SIZE );
    processRow( TransProcessControl.WAIT );
  }

  @Test
  public void testProcessRow_RowSizeLesserThanCommitSize() throws Exception {
    transMeta.setSizeRowset( ROW_SIZE );
    processRow( TransProcessControl.WAIT );
  }

  private void processRow( TransProcessControl control ) throws Exception {
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();
    generateData( rp );
    rp.finished();
    StepInterface si = trans.getStepInterface( synchronizeAfterMergeStepname, 0 );
    switch ( control ) {
      case ITTERUPT:
        trans.stopAll();
        while (  !si.getStatus().equals( StepExecutionStatus.STATUS_STOPPED ) ) {
          //wait until transformation does not stopped
        };
        break;
      case WAIT:
      default:
        trans.waitUntilFinished();
        assertEquals( "Step still started", StepExecutionStatus.STATUS_FINISHED, si.getStatus() );
        break;
    }
    assertEquals( "Unexpected error occurred",  0, si.getErrors() );

    Field field = SynchronizeAfterMerge.class.getDeclaredField( "data" );
    field.setAccessible( true );
    SynchronizeAfterMergeData  data = (SynchronizeAfterMergeData) field.get( si );
    //should be closed and set null after finish transformation
    assertNull( data.db.getConnection() );
  }

  private void generateData( RowProducer rp ) {
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaInteger( "ID" ) );
    rm.addValueMeta( new ValueMetaString( "personName" ) );
    rm.addValueMeta( new ValueMetaString( "flag" ) );
    for ( int i = 0; i < ROW_FOR_UPDATE; i++ ) {
      rp.putRow( rm.clone(), new Object[] { "personNameUpdated" + i, i, UPDATE_FLAG } );
    }
    for ( int i = ROW_FOR_UPDATE; i < ROW_FOR_UPDATE + ROW_FOR_DELETE; i++ ) {
      rp.putRow( rm.clone(), new Object[] { "personName" + i, i, DELETE_FLAG } );
    }
    for ( int i = ROW_FOR_UPDATE + ROW_FOR_DELETE; i < ROW_FOR_UPDATE + ROW_FOR_DELETE + ROW_FOR_INSERT; i++ ) {
      rp.putRow( rm.clone(), new Object[] { "personNameInserted" + i, i, INSERT_FLAG } );
    }
  }

  private enum TransProcessControl {
    WAIT,
    ITTERUPT
  }

}
