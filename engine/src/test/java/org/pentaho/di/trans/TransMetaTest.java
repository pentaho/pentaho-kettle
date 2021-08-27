/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaChangeListenerInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.InfoStepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith ( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class TransMetaTest {
  public static final String STEP_NAME = "Any step name";


  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init( false );
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
    } ).when( smi ).getFields(
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
  public void getThisStepFieldsPassesClonedInfoRowMeta() throws Exception {
    // given
    StepMetaInterface smi = mock( StepMetaInterface.class );
    StepIOMeta ioMeta = mock( StepIOMeta.class );
    when( smi.getStepIOMeta() ).thenReturn( ioMeta );

    StepMeta thisStep = mockStepMeta( "thisStep" );
    StepMeta nextStep = mockStepMeta( "nextStep" );
    when( thisStep.getStepMetaInterface() ).thenReturn( smi );

    RowMeta row = new RowMeta();
    when( smi.getTableFields() ).thenReturn( row );

    // when
    transMeta.getThisStepFields( thisStep, nextStep, row );

    // then
    verify( smi, never() ).getFields( any(), any(), eq( new RowMetaInterface[] { row } ), any(), any(), any(), any() );
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

  @Test
  public void testGetAllTransHops() throws Exception {
    TransMeta transMeta = new TransMeta( "transFile", "myTrans" );
    StepMeta step1 = new StepMeta( "name1", null );
    StepMeta step2 = new StepMeta( "name2", null );
    StepMeta step3 = new StepMeta( "name3", null );
    StepMeta step4 = new StepMeta( "name4", null );
    TransHopMeta hopMeta1 = new TransHopMeta( step1, step2, true );
    TransHopMeta hopMeta2 = new TransHopMeta( step2, step3, true );
    TransHopMeta hopMeta3 = new TransHopMeta( step2, step4, true );
    transMeta.addTransHop( 0, hopMeta1 );
    transMeta.addTransHop( 1, hopMeta2 );
    transMeta.addTransHop( 2, hopMeta3 );
    List<TransHopMeta> allTransHopFrom = transMeta.findAllTransHopFrom( step2 );
    assertEquals( step3, allTransHopFrom.get( 0 ).getToStep() );
    assertEquals( step4, allTransHopFrom.get( 1 ).getToStep() );
  }

  @Test
  public void testGetPrevInfoFields() throws KettleStepException {
    DataGridMeta dgm1 = new DataGridMeta();
    dgm1.setFieldName( new String[] { "id", "colA" } );
    dgm1.allocate( 2 );
    dgm1.setFieldType( new String[] {
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ),
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm1Data = new ArrayList<>();
    dgm1Data.add( asList( "1", "A" ) );
    dgm1Data.add( asList( "2", "B" ) );
    dgm1.setDataLines( dgm1Data );

    DataGridMeta dgm2 = new DataGridMeta();
    dgm2.allocate( 1 );
    dgm2.setFieldName( new String[] { "moreData" } );
    dgm2.setFieldType( new String[] {
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm2Data = new ArrayList<>();
    dgm2Data.add( asList( "Some Informational Data" ) );
    dgm2.setDataLines( dgm2Data );

    StepMeta dg1 = new StepMeta( "input1", dgm1 );
    StepMeta dg2 = new StepMeta( "input2", dgm2 );

    final String UDJC_METHOD =
      "public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException { return "
        + "false; }";
    UserDefinedJavaClassMeta udjcMeta = new UserDefinedJavaClassMeta();
    udjcMeta.getInfoStepDefinitions().add( new InfoStepDefinition( dg2.getName(), dg2.getName(), dg2, "info_data" ) );
    udjcMeta.replaceDefinitions( singletonList(
      new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "MainClass", UDJC_METHOD ) ) );

    StepMeta udjc = new StepMeta( "PDI-14910", udjcMeta );

    TransHopMeta hop1 = new TransHopMeta( dg1, udjc, true );
    TransHopMeta hop2 = new TransHopMeta( dg2, udjc, true );
    transMeta.addStep( dg1 );
    transMeta.addStep( dg2 );
    transMeta.addStep( udjc );
    transMeta.addTransHop( hop1 );
    transMeta.addTransHop( hop2 );

    RowMetaInterface row = null;
    row = transMeta.getPrevInfoFields( udjc );
    assertNotNull( row );
    assertEquals( 1, row.size() );
    assertEquals( "moreData", row.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, row.getValueMeta( 0 ).getType() );
  }

  @Test
  public void testAddStepWithChangeListenerInterface() {
    StepMeta stepMeta = mock( StepMeta.class );
    StepMetaChangeListenerInterfaceMock metaInterface = mock( StepMetaChangeListenerInterfaceMock.class );
    when( stepMeta.getStepMetaInterface() ).thenReturn( metaInterface );
    assertEquals( 0, transMeta.steps.size() );
    assertEquals( 0, transMeta.stepChangeListeners.size() );
    // should not throw exception if there are no steps in step meta
    transMeta.addStep( 0, stepMeta );
    assertEquals( 1, transMeta.steps.size() );
    assertEquals( 1, transMeta.stepChangeListeners.size() );

    transMeta.addStep( 0, stepMeta );
    assertEquals( 2, transMeta.steps.size() );
    assertEquals( 2, transMeta.stepChangeListeners.size() );
  }

  @Test
  public void testIsAnySelectedStepUsedInTransHopsNothingSelectedCase() {
    List<StepMeta> selectedSteps = asList( new StepMeta(), new StepMeta(), new StepMeta() );
    transMeta.getSteps().addAll( selectedSteps );

    assertFalse( transMeta.isAnySelectedStepUsedInTransHops() );
  }

  @Test
  public void testIsAnySelectedStepUsedInTransHopsAnySelectedCase() {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( STEP_NAME );
    TransHopMeta transHopMeta = new TransHopMeta();
    stepMeta.setSelected( true );
    List<StepMeta> selectedSteps = asList( new StepMeta(), stepMeta, new StepMeta() );

    transHopMeta.setToStep( stepMeta );
    transHopMeta.setFromStep( stepMeta );
    transMeta.getSteps().addAll( selectedSteps );
    transMeta.addTransHop( transHopMeta );

    assertTrue( transMeta.isAnySelectedStepUsedInTransHops() );
  }

  @Test
  public void testCloneWithParam() throws Exception {
    TransMeta transMeta = new TransMeta( "transFile", "myTrans" );
    transMeta.addParameterDefinition( "key", "defValue", "description" );
    Object clone = transMeta.realClone( true );
    assertNotNull( clone );
  }

  private static StepMeta mockStepMeta( String name ) {
    StepMeta meta = mock( StepMeta.class );
    when( meta.getName() ).thenReturn( name );
    return meta;
  }

  public abstract static class StepMetaChangeListenerInterfaceMock
    implements StepMetaInterface, StepMetaChangeListenerInterface {
    @Override
    public abstract Object clone();
  }

  @Test
  public void testLoadXml() throws KettleException {
    String directory = "/home/admin";
    Node jobNode = Mockito.mock( Node.class );
    NodeList nodeList = new NodeList() {
      ArrayList<Node> nodes = new ArrayList<>();

      {

        Node nodeInfo = Mockito.mock( Node.class );
        Mockito.when( nodeInfo.getNodeName() ).thenReturn( TransMeta.XML_TAG_INFO );
        Mockito.when( nodeInfo.getChildNodes() ).thenReturn( this );

        Node nodeDirectory = Mockito.mock( Node.class );
        Mockito.when( nodeDirectory.getNodeName() ).thenReturn( "directory" );
        Node child = Mockito.mock( Node.class );
        Mockito.when( nodeDirectory.getFirstChild() ).thenReturn( child );
        Mockito.when( child.getNodeValue() ).thenReturn( directory );

        nodes.add( nodeDirectory );
        nodes.add( nodeInfo );

      }

      @Override public Node item( int index ) {
        return nodes.get( index );
      }

      @Override public int getLength() {
        return nodes.size();
      }
    };

    Mockito.when( jobNode.getChildNodes() ).thenReturn( nodeList );

    Repository rep = Mockito.mock( Repository.class );
    RepositoryDirectory repDirectory =
      new RepositoryDirectory( new RepositoryDirectory( new RepositoryDirectory(), "home" ), "admin" );
    Mockito.when( rep.findDirectory( Mockito.eq( directory ) ) ).thenReturn( repDirectory );
    TransMeta meta = new TransMeta();

    VariableSpace variableSpace = Mockito.mock( VariableSpace.class );
    Mockito.when( variableSpace.listVariables() ).thenReturn( new String[ 0 ] );

    meta.loadXML( jobNode, null, Mockito.mock( IMetaStore.class ), rep, false, variableSpace,
      Mockito.mock( OverwritePrompter.class ) );
    meta.setInternalKettleVariables( null );

    assertEquals( repDirectory.getPath(),
      meta.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
  }

  @Test
  public void testTransWithOneStepIsConsideredUsed() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "one-step-trans.ktr" ).getPath() );
    assertEquals( 1, transMeta.getUsedSteps().size() );
    Repository rep = mock( Repository.class );
    ProgressMonitorListener monitor = mock( ProgressMonitorListener.class );
    List<CheckResultInterface> remarks = new ArrayList<>();
    IMetaStore metaStore = mock( IMetaStore.class );
    transMeta.checkSteps( remarks, false, monitor, new Variables(), rep, metaStore );
    assertEquals( 4, remarks.size() );
    for ( CheckResultInterface remark : remarks ) {
      assertEquals( CheckResultInterface.TYPE_RESULT_OK, remark.getType() );
    }
  }

  @Test
  public void testGetCacheVersion() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "one-step-trans.ktr" ).getPath() );
    int oldCacheVersion = transMeta.getCacheVersion();
    transMeta.setSizeRowset( 10 );
    int currCacheVersion = transMeta.getCacheVersion();
    assertNotEquals( oldCacheVersion, currCacheVersion );
  }

  @Test
  public void testGetCacheVersionWithIrrelevantParameters() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "one-step-trans.ktr" ).getPath() );
    int oldCacheVersion = transMeta.getCacheVersion();
    int currCacheVersion;

    transMeta.setSizeRowset( 1000 );
    currCacheVersion = transMeta.getCacheVersion();
    assertNotEquals( oldCacheVersion, currCacheVersion );

    oldCacheVersion = currCacheVersion;

    // scenarios that should not impact the cache version

    // transformation description
    transMeta.setDescription( "transformation description" );

    // transformation status
    transMeta.setTransstatus( 100 );

    // transformation log table
    transMeta.setTransLogTable( mock( TransLogTable.class ) );

    // transformation created user
    transMeta.setCreatedUser( "user" );

    // transformation modified user
    transMeta.setModifiedUser( "user" );

    // transformation created date
    transMeta.setCreatedDate( new Date() );

    // transformation modified date
    transMeta.setModifiedDate( new Date() );

    // transformation is key private flag
    transMeta.setPrivateKey( false );

    // transformation attributes
    Map<String, String> attributes = new HashMap<>();
    attributes.put( "key", "value" );
    transMeta.setAttributes( "group", attributes );

    // step description
    StepMeta stepMeta = transMeta.getStep( 0 );
    stepMeta.setDescription( "stepDescription" );

    // step position
    stepMeta.setLocation( 10, 20 );
    stepMeta.setLocation( new Point( 30, 40 ) );

    // step type id
    stepMeta.setStepID( "Dummy" );

    // step is distributed flag
    stepMeta.setDistributes( false );

    // step copies
    stepMeta.setCopies( 5 );

    // step partitioning meta
    stepMeta.setStepPartitioningMeta( mock( StepPartitioningMeta.class ) );

    // assert that nothing impacted the cache version
    assertEquals( oldCacheVersion, transMeta.getCacheVersion() );
  }

  @Test
  public void testGetPrevStepFields() throws KettleStepException {
    DataGridMeta dgm = new DataGridMeta();
    dgm.allocate( 2 );
    dgm.setFieldName( new String[] { "id" } );
    dgm.setFieldType( new String[] { ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ) } );
    List<List<String>> dgm1Data = new ArrayList<>();
    dgm1Data.add( singletonList( "1" ) );
    dgm1Data.add( singletonList( "2" ) );
    dgm.setDataLines( dgm1Data );

    DataGridMeta dgm2 = new DataGridMeta();
    dgm2.allocate( 2 );
    dgm2.setFieldName( new String[] { "foo" } );
    dgm2.setFieldType( new String[] { ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm1Data2 = new ArrayList<>();
    dgm1Data2.add( singletonList( "3" ) );
    dgm1Data2.add( singletonList( "4" ) );
    dgm2.setDataLines( dgm1Data2 );

    StepMeta dg = new StepMeta( "input1", dgm );
    StepMeta dg2 = new StepMeta( "input2", dgm2 );
    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    StepMeta textFileOutputStep = new StepMeta( "BACKLOG-21039", textFileOutputMeta );

    TransHopMeta hop = new TransHopMeta( dg, textFileOutputStep, true );
    TransHopMeta hop2 = new TransHopMeta( dg2, textFileOutputStep, true );
    transMeta.addStep( dg );
    transMeta.addStep( dg2 );
    transMeta.addStep( textFileOutputStep );
    transMeta.addTransHop( hop );
    transMeta.addTransHop( hop2 );

    RowMetaInterface allRows = transMeta.getPrevStepFields( textFileOutputStep, null, null );
    assertNotNull( allRows );
    assertEquals( 2, allRows.size() );
    assertEquals( "id", allRows.getValueMeta( 0 ).getName() );
    assertEquals( "foo", allRows.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, allRows.getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, allRows.getValueMeta( 1 ).getType() );

    RowMetaInterface rows1 = transMeta.getPrevStepFields( textFileOutputStep, "input1", null );
    assertNotNull( rows1 );
    assertEquals( 1, rows1.size() );
    assertEquals( "id", rows1.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, rows1.getValueMeta( 0 ).getType() );

    RowMetaInterface rows2 = transMeta.getPrevStepFields( textFileOutputStep, "input2", null );
    assertNotNull( rows2 );
    assertEquals( 1, rows2.size() );
    assertEquals( "foo", rows2.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rows2.getValueMeta( 0 ).getType() );

    dgm.setFieldName( new String[] { "id", "name" } );
    dgm.setFieldType( new String[] {
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ),
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ),
    } );

    allRows = transMeta.getPrevStepFields( textFileOutputStep, null, null );
    assertNotNull( allRows );
    assertEquals( 3, allRows.size() );
    assertEquals( "id", allRows.getValueMeta( 0 ).getName() );
    assertEquals( "name", allRows.getValueMeta( 1 ).getName() );
    assertEquals( "foo", allRows.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, allRows.getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, allRows.getValueMeta( 1 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, allRows.getValueMeta( 2 ).getType() );

    rows1 = transMeta.getPrevStepFields( textFileOutputStep, "input1", null );
    assertNotNull( rows1 );
    assertEquals( 2, rows1.size() );
    assertEquals( "id", rows1.getValueMeta( 0 ).getName() );
    assertEquals( "name", rows1.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, rows1.getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rows1.getValueMeta( 1 ).getType() );
  }

  @Test
  public void testHasLoop_simpleLoop() throws Exception {
    //main->2->3->main
    TransMeta transMetaSpy = spy( transMeta );
    StepMeta stepMetaMain = createStepMeta( "mainStep" );
    StepMeta stepMeta2 = createStepMeta( "step2" );
    StepMeta stepMeta3 = createStepMeta( "step3" );
    List<StepMeta> mainPrevSteps = new ArrayList<>();
    mainPrevSteps.add( stepMeta2 );
    doReturn( mainPrevSteps ).when( transMetaSpy ).findPreviousSteps( stepMetaMain, true );
    when( transMetaSpy.findNrPrevSteps( stepMetaMain ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMetaMain, 0 ) ).thenReturn( stepMeta2 );
    List<StepMeta> stepmeta2PrevSteps = new ArrayList<>();
    stepmeta2PrevSteps.add( stepMeta3 );
    doReturn( stepmeta2PrevSteps ).when( transMetaSpy ).findPreviousSteps( stepMeta2, true );
    when( transMetaSpy.findNrPrevSteps( stepMeta2 ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMeta2, 0 ) ).thenReturn( stepMeta3 );
    List<StepMeta> stepmeta3PrevSteps = new ArrayList<>();
    stepmeta3PrevSteps.add( stepMetaMain );
    doReturn( stepmeta3PrevSteps ).when( transMetaSpy ).findPreviousSteps( stepMeta3, true );
    when( transMetaSpy.findNrPrevSteps( stepMeta3 ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMeta3, 0 ) ).thenReturn( stepMetaMain );
    assertTrue( transMetaSpy.hasLoop( stepMetaMain ) );
  }

  @Test
  public void testHasLoop_loopInPrevSteps() throws Exception {
    //main->2->3->4->3
    TransMeta transMetaSpy = spy( transMeta );
    StepMeta stepMetaMain = createStepMeta( "mainStep" );
    StepMeta stepMeta2 = createStepMeta( "step2" );
    StepMeta stepMeta3 = createStepMeta( "step3" );
    StepMeta stepMeta4 = createStepMeta( "step4" );
    when( transMetaSpy.findNrPrevSteps( stepMetaMain ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMetaMain, 0 ) ).thenReturn( stepMeta2 );
    when( transMetaSpy.findNrPrevSteps( stepMeta2 ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMeta2, 0 ) ).thenReturn( stepMeta3 );
    when( transMetaSpy.findNrPrevSteps( stepMeta3 ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMeta3, 0 ) ).thenReturn( stepMeta4 );
    when( transMetaSpy.findNrPrevSteps( stepMeta4 ) ).thenReturn( 1 );
    when( transMetaSpy.findPrevStep( stepMeta4, 0 ) ).thenReturn( stepMeta3 );
    //check no StackOverflow error
    assertFalse( transMetaSpy.hasLoop( stepMetaMain ) );
  }


  @Test
  public void infoStepFieldsAreNotIncludedInGetStepFields() throws KettleStepException {
    // validates that the fields from info steps are not included in the resulting step fields for a stepMeta.
    //  This is important with steps like StreamLookup and Append, where the previous steps may or may not
    //  have their fields included in the current step.

    TransMeta transMeta = new TransMeta( new Variables() );
    StepMeta toBeAppended1 = testStep( "toBeAppended1",
      emptyList(),  // no info steps
      asList( "field1", "field2" )  // names of fields from this step
    );
    StepMeta toBeAppended2 = testStep( "toBeAppended2", emptyList(), asList( "field1", "field2" ) );

    StepMeta append = testStep( "append",
      asList( "toBeAppended1", "toBeAppended2" ),  // info step names
      singletonList( "outputField" )   // output field of this step
    );
    StepMeta after = new StepMeta( "after", new DummyTransMeta() );

    wireUpTestTransMeta( transMeta, toBeAppended1, toBeAppended2, append, after );

    RowMetaInterface results = transMeta.getStepFields( append, after, mock( ProgressMonitorListener.class ) );

    assertThat( 1, equalTo( results.size() ) );
    assertThat( "outputField", equalTo( results.getFieldNames()[ 0 ] ) );
  }

  @Test
  public void prevStepFieldsAreIncludedInGetStepFields() throws KettleStepException {

    TransMeta transMeta = new TransMeta( new Variables() );
    StepMeta prevStep1 = testStep( "prevStep1", emptyList(), asList( "field1", "field2" ) );
    StepMeta prevStep2 = testStep( "prevStep2", emptyList(), asList( "field3", "field4", "field5" ) );

    StepMeta someStep = testStep( "step", asList( "prevStep1" ), asList( "outputField" ) );

    StepMeta after = new StepMeta( "after", new DummyTransMeta() );

    wireUpTestTransMeta( transMeta, prevStep1, prevStep2, someStep, after );

    RowMetaInterface results = transMeta.getStepFields( someStep, after, mock( ProgressMonitorListener.class ) );

    assertThat( 4, equalTo( results.size() ) );
    assertThat( new String[] { "field3", "field4", "field5", "outputField" }, equalTo( results.getFieldNames() ) );
  }

  @Test
  public void findPreviousStepsNullMeta( ) {
    TransMeta transMeta = new TransMeta( new Variables() );
    List<StepMeta> result = transMeta.findPreviousSteps( null, false );

    assertThat( 0, equalTo( result.size() ) );
    assertThat( result, equalTo( new ArrayList<>() ) );
  }

  private void wireUpTestTransMeta( TransMeta transMeta, StepMeta toBeAppended1, StepMeta toBeAppended2,
                                    StepMeta append, StepMeta after ) {
    transMeta.addStep( append );
    transMeta.addStep( after );
    transMeta.addStep( toBeAppended1 );
    transMeta.addStep( toBeAppended2 );

    transMeta.addTransHop( new TransHopMeta( toBeAppended1, append ) );
    transMeta.addTransHop( new TransHopMeta( toBeAppended2, append ) );
    transMeta.addTransHop( new TransHopMeta( append, after ) );
  }


  private StepMeta testStep( String name, List<String> infoStepnames, List<String> fieldNames )
    throws KettleStepException {
    StepMetaInterface smi = stepMetaInterfaceWithFields( new DummyTransMeta(), infoStepnames, fieldNames );
    return new StepMeta( name, smi );
  }

  private StepMetaInterface stepMetaInterfaceWithFields(
    StepMetaInterface smi, List<String> infoStepnames, List<String> fieldNames )
    throws KettleStepException {
    RowMeta rowMetaWithFields = new RowMeta();
    StepIOMeta stepIOMeta = mock( StepIOMeta.class );
    when( stepIOMeta.getInfoStepnames() ).thenReturn( infoStepnames.toArray( new String[ 0 ] ) );
    fieldNames.stream()
      .forEach( field -> rowMetaWithFields.addValueMeta( new ValueMetaString( field ) ) );
    StepMetaInterface newSmi = spy( smi );
    when( newSmi.getStepIOMeta() ).thenReturn( stepIOMeta );

    doAnswer( (Answer<Void>) invocationOnMock -> {
      RowMetaInterface passedRmi = (RowMetaInterface) invocationOnMock.getArguments()[ 0 ];
      passedRmi.addRowMeta( rowMetaWithFields );
      return null;
    } ).when( newSmi )
      .getFields( any(), any(), any(), any(), any(), any(), any() );

    return newSmi;
  }


  private StepMeta createStepMeta( String name ) {
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getName() ).thenReturn( name );
    return stepMeta;
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithFilename( ) {
    TransMeta transMetaTest = new TransMeta(  );
    transMetaTest.setFilename( "hasFilename" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    transMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "file:///C:/SomeFilenameDirectory",  transMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )  );

  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithRepository( ) {
    TransMeta transMetaTest = new TransMeta(  );
    RepositoryDirectoryInterface path = mock( RepositoryDirectoryInterface.class );

    when( path.getPath() ).thenReturn( "aPath" );
    transMetaTest.setRepository( mock( Repository.class ) );
    transMetaTest.setRepositoryDirectory( path );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    transMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "/SomeRepDirectory", transMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithoutFilenameOrRepository( ) {
    TransMeta transMetaTest = new TransMeta(  );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    transMetaTest.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    transMetaTest.setInternalEntryCurrentDirectory();

    assertEquals( "Original value defined at run execution", transMetaTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }
}
