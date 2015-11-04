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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
      } } ).when( smi ).getFields(
        any( RowMetaInterface.class ), anyString(), any( RowMetaInterface[].class ), eq( nextStep ),
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

  @Test
  public void testContentChangeListener() throws Exception {
    ContentChangedListener listener = mock( ContentChangedListener.class );
    transMeta.addContentChangedListener( listener );

    transMeta.setChanged();
    transMeta.setChanged( true );

    verify( listener, times( 2 ) ).contentChanged( same( transMeta ) );

    transMeta.clearChanged();
    transMeta.setChanged( false );

    verify( listener, times( 2 ) ).contentSafe( same( transMeta ) );

    transMeta.removeContentChangedListener( listener );
    transMeta.setChanged();
    transMeta.setChanged( true );

    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testCompare() throws Exception {
    TransMeta transMeta = new TransMeta( "aFile", "aName" );
    TransMeta transMeta2 = new TransMeta( "aFile", "aName" );
    assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    transMeta2.setVariable( "myVariable", "myValue" );
    assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    transMeta2.setFilename( null );
    assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile" );
    transMeta2.setName( null );
    assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile2" );
    transMeta2.setName( "aName" );
    assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile" );
    transMeta2.setName( "aName2" );
    assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    transMeta.setFilename( null );
    transMeta2.setFilename( null );
    transMeta2.setName( "aName" );
    assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    RepositoryDirectoryInterface path1 = mock( RepositoryDirectoryInterface.class );
    transMeta.setRepositoryDirectory( path1 );
    when( path1.getPath() ).thenReturn( "aPath2" );
    RepositoryDirectoryInterface path2 = mock( RepositoryDirectoryInterface.class );
    when( path2.getPath() ).thenReturn( "aPath" );
    transMeta2.setRepositoryDirectory( path2 );
    assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    when( path1.getPath() ).thenReturn( "aPath" );
    assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    ObjectRevision revision2 = mock( ObjectRevision.class );
    transMeta2.setObjectRevision( revision2 );
    assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    ObjectRevision revision1 = mock( ObjectRevision.class );
    transMeta.setObjectRevision( revision1 );
    when( revision1.getName() ).thenReturn( "aRevision" );
    when( revision2.getName() ).thenReturn( "aRevision" );
    assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    when( revision2.getName() ).thenReturn( "aRevision2" );
    assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
  }

  @Test
  public void testEquals() throws Exception {
    TransMeta transMeta = new TransMeta( "1", "2" );
    assertFalse( transMeta.equals( "somethingelse" ) );
    assertTrue( transMeta.equals( new TransMeta( "1", "2" ) ) );
  }

  @Test
  public void testTransHops() throws Exception {
    TransMeta transMeta = new TransMeta( "transFile", "myTrans" );
    StepMeta step1 = new StepMeta( "name1", null );
    StepMeta step2 = new StepMeta( "name2", null );
    StepMeta step3 = new StepMeta( "name3", null );
    StepMeta step4 = new StepMeta( "name4", null );
    TransHopMeta hopMeta1 = new TransHopMeta( step1, step2, true );
    TransHopMeta hopMeta2 = new TransHopMeta( step2, step3, true );
    TransHopMeta hopMeta3 = new TransHopMeta( step3, step4, false );
    transMeta.addTransHop( 0, hopMeta1 );
    transMeta.addTransHop( 1, hopMeta2 );
    transMeta.addTransHop( 2, hopMeta3 );
    List<StepMeta> hops = transMeta.getTransHopSteps( true );
    assertSame( step1, hops.get( 0 ) );
    assertSame( step2, hops.get( 1 ) );
    assertSame( step3, hops.get( 2 ) );
    assertSame( step4, hops.get( 3 ) );
    assertEquals( hopMeta2, transMeta.findTransHop( "name2 --> name3 (enabled)" ) );
    assertEquals( hopMeta3, transMeta.findTransHopFrom( step3 ) );
    assertEquals( hopMeta2, transMeta.findTransHop( hopMeta2 ) );
    assertEquals( hopMeta1, transMeta.findTransHop( step1, step2 ) );
    assertEquals( null, transMeta.findTransHop( step3, step4, false ) );
    assertEquals( hopMeta3, transMeta.findTransHop( step3, step4, true ) );
    assertEquals( hopMeta2, transMeta.findTransHopTo( step3 ) );
    transMeta.removeTransHop( 0 );
    hops = transMeta.getTransHopSteps( true );
    assertSame( step2, hops.get( 0 ) );
    assertSame( step3, hops.get( 1 ) );
    assertSame( step4, hops.get( 2 ) );
    transMeta.removeTransHop( hopMeta2 );
    hops = transMeta.getTransHopSteps( true );
    assertSame( step3, hops.get( 0 ) );
    assertSame( step4, hops.get( 1 ) );
  }

  private static StepMeta mockStepMeta( String name ) {
    StepMeta meta = mock( StepMeta.class );
    when( meta.getName() ).thenReturn( name );
    return meta;
  }
}
