/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.databaselookup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for DatabaseLookup step
 *
 * @author Pavel Sakun
 * @see DatabaseLookup
 */
public class PDI5436Test {
  private StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> smh;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setupClass() throws KettleException {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDown() {
    KettleEnvironment.reset();
  }

  @Before
  public void setUp() {
    smh =
        new StepMockHelper<DatabaseLookupMeta, DatabaseLookupData>( "Database Lookup", DatabaseLookupMeta.class,
            DatabaseLookupData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  private RowMeta mockInputRowMeta() {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaString nameMeta = new ValueMetaString( "name" );
    nameMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    nameMeta.setStorageMetadata( new ValueMetaString( "name" ) );
    inputRowMeta.addValueMeta( nameMeta );
    ValueMetaString idMeta = new ValueMetaString( "id" );
    idMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    idMeta.setStorageMetadata( new ValueMetaString( "id" ) );
    inputRowMeta.addValueMeta( idMeta );

    return inputRowMeta;
  }

  private RowSet mockInputRowSet() {
    RowSet inputRowSet = smh.getMockInputRowSet( new Object[][] { { "name".getBytes(), "1".getBytes() } } );
    inputRowSet.setRowMeta( mockInputRowMeta() );
    return inputRowSet;
  }

  private DatabaseLookupMeta mockStepMeta() throws KettleStepException {
    DatabaseLookupMeta stepMeta = smh.initStepMetaInterface;
    doReturn( mock( DatabaseMeta.class ) ).when( stepMeta ).getDatabaseMeta();
    doReturn( new String[] { "=" } ).when( stepMeta ).getKeyCondition();

    doCallRealMethod().when( stepMeta ).getFields( any( RowMetaInterface.class ), anyString(),
      nullable( RowMetaInterface[].class ), nullable( StepMeta.class ), any( VariableSpace.class ), nullable( Repository.class ),
      nullable( IMetaStore.class ) );
    doReturn( new String[] { "value" } ).when( stepMeta ).getReturnValueNewName();
    doReturn( new int[] { ValueMetaInterface.TYPE_STRING } ).when( stepMeta ).getReturnValueDefaultType();
    doReturn( true ).when( stepMeta ).isCached();
    doReturn( true ).when( stepMeta ).isLoadingAllDataInCache();
    doReturn( new String[] { "id" } ).when( stepMeta ).getStreamKeyField1();
    doReturn( new String[] { null } ).when( stepMeta ).getStreamKeyField2();
    doReturn( new String[] { "id" } ).when( stepMeta ).getTableKeyField();
    doReturn( new String[] { "value" } ).when( stepMeta ).getReturnValueField();
    doReturn( new String[] { "" } ).when( stepMeta ).getReturnValueDefault();
    doReturn( new int[] { ValueMetaInterface.TYPE_STRING } ).when( stepMeta ).getReturnValueDefaultType();
    when( stepMeta.getStreamKeyField2() ).thenReturn( new String[]{ "a", "b", "c" } );

    return stepMeta;
  }

  private Database mockDatabase() throws KettleDatabaseException {
    Database databaseMock = mock( Database.class );

    RowMeta databaseRowMeta = new RowMeta();
    databaseRowMeta.addValueMeta( new ValueMetaString( "id" ) );
    databaseRowMeta.addValueMeta( new ValueMetaString( "value" ) );
    doReturn( databaseRowMeta ).when( databaseMock ).getTableFields( nullable( String.class ) );
    doReturn( databaseRowMeta ).when( databaseMock ).getTableFieldsMeta( nullable( String.class ), nullable( String.class )  );
    doReturn( Arrays.asList( new Object[][] { { "1", "value" } } ) ).when( databaseMock ).getRows( nullable( String.class ),
        nullable( Integer.class ) );
    doReturn( databaseRowMeta ).when( databaseMock ).getReturnRowMeta();

    return databaseMock;
  }

  @Test
  public void testCacheAllTable() throws KettleException {
    DatabaseLookup stepSpy =
        spy( new DatabaseLookup( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );

    Database database = mockDatabase();
    doReturn( database ).when( stepSpy ).getDatabase( any( DatabaseMeta.class ) );

    stepSpy.addRowSetToInputRowSets( mockInputRowSet() );
    stepSpy.setInputRowMeta( mockInputRowMeta() );
    RowSet outputRowSet = new QueueRowSet();
    stepSpy.addRowSetToOutputRowSets( outputRowSet );

    StepMetaInterface meta = mockStepMeta();
    StepDataInterface data = smh.initStepDataInterface;

    Assert.assertTrue( "Step init failed", stepSpy.init( meta, data ) );
    Assert.assertTrue( "Error processing row", stepSpy.processRow( meta, data ) );
    Assert.assertEquals( "Cache lookup failed", "value", outputRowSet.getRow()[2] );
  }
}
