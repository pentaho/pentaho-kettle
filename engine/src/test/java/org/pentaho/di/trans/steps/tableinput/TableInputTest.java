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


package org.pentaho.di.trans.steps.tableinput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

public class TableInputTest {

  TableInputMeta mockStepMetaInterface;
  TableInputData mockStepDataInterface;
  TableInput mockTableInput;
  Map<String, String> queryParams;
  String connection;
  String schema;
  String table;

  @Before
  public void setUp() {

    StepMeta mockStepMeta = mock( StepMeta.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Trans mockTrans = mock( Trans.class );
    StepPartitioningMeta mockStepPartitioningMeta = mock( StepPartitioningMeta.class );

    when( mockStepMeta.getName() ).thenReturn( "MockStep" );
    when( mockTransMeta.findStep( anyString() ) ).thenReturn( mockStepMeta );
    when( mockStepMeta.getTargetStepPartitioningMeta() ).thenReturn( mockStepPartitioningMeta );

    mockStepMetaInterface = mock( TableInputMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    mockStepDataInterface = mock( TableInputData.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    mockStepDataInterface.db = mock( Database.class );
    mockTableInput = spy( new TableInput( mockStepMeta, mockStepDataInterface, 1, mockTransMeta, mockTrans ) );

    connection = "Hypersonic";
    schema = "sampleSchema";
    table = "sampleTable";

    queryParams = new HashMap<>();
    queryParams.put( "connection", connection );
    queryParams.put( "schema", schema );
    queryParams.put( "table", table );
  }

  @Test
  public void testStopRunningWhenStepIsStopped() throws KettleException {
    doReturn( true ).when( mockTableInput ).isStopped();

    mockTableInput.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockTableInput, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 0 ) ).isDisposed();
  }

  @Test
  public void testStopRunningWhenStepDataInterfaceIsDisposed() throws KettleException {
    doReturn( false ).when( mockTableInput ).isStopped();
    doReturn( true ).when( mockStepDataInterface ).isDisposed();

    mockTableInput.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockTableInput, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
  }

  @Test
  public void testStopRunningWhenStepIsNotStoppedNorStepDataInterfaceIsDisposedAndDatabaseConnectionIsValid() throws KettleException {
    doReturn( false ).when( mockTableInput ).isStopped();
    doReturn( false ).when( mockStepDataInterface ).isDisposed();
    when( mockStepDataInterface.db.getConnection() ).thenReturn( mock( Connection.class ) );

    mockTableInput.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockTableInput, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
    verify( mockStepDataInterface.db, times( 1 ) ).getConnection();
    verify( mockStepDataInterface.db, times( 1 ) ).cancelQuery();
    assertTrue( mockStepDataInterface.isCanceled );

  }

  @Test
  public void testStopRunningWhenStepIsNotStoppedNorStepDataInterfaceIsDisposedAndDatabaseConnectionIsNotValid() throws KettleException {
    doReturn( false ).when( mockTableInput ).isStopped();
    doReturn( false ).when( mockStepDataInterface ).isDisposed();
    when( mockStepDataInterface.db.getConnection() ).thenReturn( null );

    mockTableInput.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockTableInput, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
    verify( mockStepDataInterface.db, times( 1 ) ).getConnection();
    verify( mockStepDataInterface.db, times( 0 ) ).cancelStatement( any( PreparedStatement.class ) );
    assertFalse( mockStepDataInterface.isCanceled );
  }
}
