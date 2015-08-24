/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class TransMetaTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private TransMeta transMeta;

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta();
  }

  @Test
  public void testGetMinimum() {
    final Point minimalCanvasPoint = new Point( 0, 0 );

    //for test goal should content coordinate more than NotePadMetaPoint
    final Point stepPoint = new Point( 500, 500 );

    //empty Trans return 0 coordinate point
    Point point = transMeta.getMinimum();
    assertEquals( minimalCanvasPoint.x, point.x );
    assertEquals( minimalCanvasPoint.y, point.y );

    //when Trans  content Step  than  trans should return minimal coordinate of step
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getLocation() ).thenReturn( stepPoint );
    transMeta.addStep( stepMeta );
    Point actualStepPoint = transMeta.getMinimum();
    assertEquals( stepPoint.x - TransMeta.BORDER_INDENT, actualStepPoint.x );
    assertEquals( stepPoint.y - TransMeta.BORDER_INDENT, actualStepPoint.y );
  }


  @Test
  public void getThisStepFieldsPassesCloneRowMeta() throws Exception {
    final String overriddenValue = "overridden";

    StepMeta nextStep = mockStepMeta( "nextStep" );

    StepMetaInterface smi = mock( StepMetaInterface.class );
    StepIOMeta ioMeta = mock( StepIOMeta.class );
    when( smi.getStepIOMeta() ).thenReturn( ioMeta );
    doAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        RowMetaInterface rmi = (RowMetaInterface) invocation.getArguments()[ 0 ];
        rmi.clear();
        rmi.addValueMeta( new ValueMetaString( overriddenValue ) );
        return null;
      }
    } ).when( smi ).getFields( any( RowMetaInterface.class ), anyString(), any( RowMetaInterface[].class ), eq( nextStep ),
        any( VariableSpace.class ), any( Repository.class ), any( IMetaStore.class ) );

    StepMeta thisStep = mockStepMeta( "thisStep" );
    when( thisStep.getStepMetaInterface() ).thenReturn( smi );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "value" ) );

    RowMetaInterface thisStepsFields = transMeta.getThisStepFields( thisStep, nextStep, rowMeta );

    assertEquals( 1, thisStepsFields.size() );
    assertEquals( overriddenValue, thisStepsFields.getValueMeta( 0 ).getName() );
  }

  @Test
  public void testDatabaseNotOverridden() throws Exception {
    final String name = "db meta";

    DatabaseMeta dbMetaShared = new DatabaseMeta();
    dbMetaShared.setName( name );
    dbMetaShared.setHostname( "host" );
    DatabaseMeta dbMetaStore = new DatabaseMeta();
    dbMetaStore.setName( name );
    dbMetaStore.setHostname( "anotherhost" );
    IMetaStore mstore = new MemoryMetaStore();
    DatabaseMetaStoreUtil.createDatabaseElement( mstore, dbMetaStore );

    TransMeta trans = new TransMeta();
    trans.addOrReplaceDatabase( dbMetaShared );
    trans.setMetaStore( mstore );
    trans.importFromMetaStore();
    DatabaseMeta dbMeta = trans.findDatabase( name );
    assertEquals( dbMetaShared.getHostname(), dbMeta.getHostname() );
  }

  @Test
  public void testAddOrReplaceStep() throws Exception {
    StepMeta stepMeta = mockStepMeta( "ETL Metadata Injection" );
    MetaInjectMeta stepMetaInterfaceMock = mock( MetaInjectMeta.class );
    when( stepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterfaceMock );
    transMeta.addOrReplaceStep( stepMeta );
    verify( stepMeta ).setParentTransMeta( any( TransMeta.class ) );
    // to make sure that method comes through positive scenario
    assert transMeta.steps.size() == 1;
    assert transMeta.changed_steps;
  }

  @Test
  public void testStepChangeListener() throws Exception {
    MetaInjectMeta mim = new MetaInjectMeta();
    StepMeta sm = new StepMeta( "testStep", mim );
    try {
      transMeta.addOrReplaceStep( sm );
    } catch ( Exception ex ) {
      fail();
    }
  }

  private static StepMeta mockStepMeta( String name ) {
    StepMeta meta = mock( StepMeta.class );
    when( meta.getName() ).thenReturn( name );
    return meta;
  }
}
