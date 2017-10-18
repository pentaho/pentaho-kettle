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

package org.pentaho.di.repository.kdr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;

public class KettleDatabaseRepositoryTest {

  KettleDatabaseRepository repo;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    repo = spy( new KettleDatabaseRepository() );
    repo.setRepositoryMeta( new KettleDatabaseRepositoryMeta( "myId", "myName", "myDescription", new DatabaseMeta() ) );
    repo.connectionDelegate = spy( new KettleDatabaseRepositoryConnectionDelegate( repo, new DatabaseMeta() ) );
  }

  @Test
  public void testInsertLogEntry() throws KettleException {
    doReturn( new LongObjectId( 123 ) ).when( repo.connectionDelegate ).getNextLogID();
    doReturn( "2.4" ).when( repo.connectionDelegate ).getVersion();
    doReturn( new UserInfo( "John Doe" ) ).when( repo ).getUserInfo();

    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    Date beforeLogEntryDate = Calendar.getInstance().getTime();
    repo.insertLogEntry( "testDescription" );
    Date afterLogEntryDate = Calendar.getInstance().getTime();

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_REPOSITORY_LOG, argumentTableName.getValue() );
    assertEquals( 5, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 123 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_REP_VERSION, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( "2.4", insertRecord.getString( 1, null ) );
    assertEquals( ValueMetaInterface.TYPE_DATE, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_DATE, insertRecord.getValueMeta( 2 ).getName() );
    assertTrue( beforeLogEntryDate.compareTo( insertRecord.getDate( 2, new Date( Long.MIN_VALUE ) ) ) <= 0
      && afterLogEntryDate.compareTo( insertRecord.getDate( 2, new Date( Long.MIN_VALUE ) ) ) >= 0 );
    assertEquals( ValueMetaInterface.TYPE_STRING, insertRecord.getValueMeta( 3 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_USER, insertRecord.getValueMeta( 3 ).getName() );
    assertEquals( "John Doe", insertRecord.getString( 3, null ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, insertRecord.getValueMeta( 4 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_OPERATION_DESC, insertRecord.getValueMeta( 4 ).getName() );
    assertEquals( "testDescription", insertRecord.getString( 4, null ) );
  }

  @Test
  public void testInsertTransNote() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    repo.insertTransNote( new LongObjectId( 456 ), new LongObjectId( 789 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_TRANS_NOTE, argumentTableName.getValue() );
    assertEquals( 2, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 456 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 789 ), insertRecord.getInteger( 1 ) );
  }

  @Test
  public void testInsertJobNote() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    repo.insertJobNote( new LongObjectId( 234 ), new LongObjectId( 567 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_JOB_NOTE, argumentTableName.getValue() );
    assertEquals( 2, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 234 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 567 ), insertRecord.getInteger( 1 ) );
  }

  private RowMetaAndData getNullIntegerRow() {
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaInteger() );
    return new RowMetaAndData( rm, new Object[]{ null } );
  }

  @Test
  public void testInsertStepDatabase() throws KettleException {
    doReturn( getNullIntegerRow() ).when( repo.connectionDelegate ).getOneRow(
      anyString(), anyString(), any( ObjectId.class ) );
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    repo.insertStepDatabase( new LongObjectId( 654 ), new LongObjectId( 765 ), new LongObjectId( 876 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_STEP_DATABASE, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 654 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 765 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 876 ), insertRecord.getInteger( 2 ) );
  }

  @Test
  public void testInsertJobEntryDatabase() throws KettleException {
    doReturn( getNullIntegerRow() ).when( repo.connectionDelegate ).getOneRow(
      anyString(), anyString(), any( ObjectId.class ) );
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    repo.insertJobEntryDatabase( new LongObjectId( 234 ), new LongObjectId( 345 ), new LongObjectId( 456 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 234 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOBENTRY, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 345 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 456 ), insertRecord.getInteger( 2 ) );
  }

  @Test
  public void testInsertTransformationPartitionSchema() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );
    doReturn( new LongObjectId( 456 ) ).when( repo.connectionDelegate ).getNextTransformationPartitionSchemaID();

    ObjectId result = repo.insertTransformationPartitionSchema( new LongObjectId( 147 ), new LongObjectId( 258 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 456 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 147 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 258 ), insertRecord.getInteger( 2 ) );
    assertEquals( new LongObjectId( 456 ), result );
  }

  @Test
  public void testInsertClusterSlave() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );
    doReturn( new LongObjectId( 357 ) ).when( repo.connectionDelegate ).getNextClusterSlaveID();

    SlaveServer testSlave = new SlaveServer( "slave1", "fakelocal", "9081", "fakeuser", "fakepass" );
    testSlave.setObjectId( new LongObjectId( 864 ) );
    ClusterSchema testSchema = new ClusterSchema( "schema1", Arrays.asList( testSlave ) );
    testSchema.setObjectId( new LongObjectId( 159 ) );
    ObjectId result = repo.insertClusterSlave( testSchema, testSlave );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 357 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 159 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 864 ), insertRecord.getInteger( 2 ) );
    assertEquals( new LongObjectId( 357 ), result );
  }

  @Test
  public void testInsertTransformationCluster() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );
    doReturn( new LongObjectId( 123 ) ).when( repo.connectionDelegate ).getNextTransformationClusterID();

    ObjectId result = repo.insertTransformationCluster( new LongObjectId( 456 ), new LongObjectId( 789 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 123 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 456 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 789 ), insertRecord.getInteger( 2 ) );
    assertEquals( new LongObjectId( 123 ), result );
  }

  @Test
  public void testInsertTransformationSlave() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );
    doReturn( new LongObjectId( 789 ) ).when( repo.connectionDelegate ).getNextTransformationSlaveID();

    ObjectId result = repo.insertTransformationSlave( new LongObjectId( 456 ), new LongObjectId( 123 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_TRANS_SLAVE, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 789 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 456 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 123 ), insertRecord.getInteger( 2 ) );
    assertEquals( new LongObjectId( 789 ), result );
  }

  @Test
  public void testInsertTransStepCondition() throws KettleException {
    ArgumentCaptor<String> argumentTableName = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<RowMetaAndData> argumentTableData = ArgumentCaptor.forClass( RowMetaAndData.class );
    doNothing().when( repo.connectionDelegate ).insertTableRow( argumentTableName.capture(), argumentTableData.capture() );

    repo.insertTransStepCondition( new LongObjectId( 234 ), new LongObjectId( 567 ), new LongObjectId( 468 ) );

    RowMetaAndData insertRecord = argumentTableData.getValue();
    assertEquals( KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION, argumentTableName.getValue() );
    assertEquals( 3, insertRecord.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 0 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, insertRecord.getValueMeta( 0 ).getName() );
    assertEquals( Long.valueOf( 234 ), insertRecord.getInteger( 0 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 1 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_STEP, insertRecord.getValueMeta( 1 ).getName() );
    assertEquals( Long.valueOf( 567 ), insertRecord.getInteger( 1 ) );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, insertRecord.getValueMeta( 2 ).getType() );
    assertEquals( KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION, insertRecord.getValueMeta( 2 ).getName() );
    assertEquals( Long.valueOf( 468 ), insertRecord.getInteger( 2 ) );
  }
}
