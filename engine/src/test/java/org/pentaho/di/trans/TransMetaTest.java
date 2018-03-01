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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaChangeListenerInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

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
    dgm1.setFieldName( new String[]{ "id", "colA" } );
    dgm1.allocate( 2 );
    dgm1.setFieldType( new String[]{
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ),
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm1Data = new ArrayList<>();
    dgm1Data.add( Arrays.asList( new String[]{ "1", "A" } ) );
    dgm1Data.add( Arrays.asList( new String[]{ "2", "B" } ) );
    dgm1.setDataLines( dgm1Data );

    DataGridMeta dgm2 = new DataGridMeta();
    dgm2.allocate( 1 );
    dgm2.setFieldName( new String[]{ "moreData" } );
    dgm2.setFieldType( new String[]{
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm2Data = new ArrayList<>();
    dgm2Data.add( Arrays.asList( new String[]{ "Some Informational Data" } ) );
    dgm2.setDataLines( dgm2Data );

    StepMeta dg1 = new StepMeta( "input1", dgm1 );
    StepMeta dg2 = new StepMeta( "input2", dgm2 );

    final String UDJC_METHOD =
      "public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException { return false; }";
    UserDefinedJavaClassMeta udjcMeta = new UserDefinedJavaClassMeta();
    udjcMeta.getInfoStepDefinitions().add( new StepDefinition( dg2.getName(), dg2.getName(), dg2, "info_data" ) );
    udjcMeta.replaceDefinitions( Arrays.asList( new UserDefinedJavaClassDef[]{
      new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "MainClass", UDJC_METHOD ) } ) );

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
    List<StepMeta> selectedSteps = Arrays.asList( new StepMeta(), new StepMeta(), new StepMeta() );
    transMeta.getSteps().addAll( selectedSteps );

    assertFalse( transMeta.isAnySelectedStepUsedInTransHops() );
  }

  @Test
  public void testIsAnySelectedStepUsedInTransHopsAnySelectedCase() {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( STEP_NAME );
    TransHopMeta transHopMeta = new TransHopMeta();
    stepMeta.setSelected( true );
    List<StepMeta> selectedSteps = Arrays.asList( new StepMeta(), stepMeta, new StepMeta() );

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

  private abstract static class StepMetaChangeListenerInterfaceMock implements StepMetaInterface, StepMetaChangeListenerInterface {
    @Override
    public abstract Object clone();
  }

  @Test
  public void testLoadXml() throws KettleException {
    String directory = "/home/admin";
    Node jobNode = Mockito.mock( Node.class );
    NodeList nodeList = new NodeList() {
      ArrayList<Node> nodes = new ArrayList<>(  );
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
    Mockito.when( variableSpace.listVariables() ).thenReturn( new String[0] );

    meta.loadXML( jobNode, null, Mockito.mock( IMetaStore.class ), rep, false, variableSpace,
      Mockito.mock( OverwritePrompter.class ) );
    meta.setInternalKettleVariables( null );

    assertEquals( repDirectory.getPath(), meta.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
  }

  @Test
  public void testTransWithOneStepIsConsideredUsed() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "one-step-trans.ktr" ).getPath() );
    assertEquals( 1, transMeta.getUsedSteps().size() );
  }

  @Test
  public void testGetPrevStepFields() throws KettleStepException {
    DataGridMeta dgm = new DataGridMeta();
    dgm.allocate( 2 );
    dgm.setFieldName( new String[]{ "id" } );
    dgm.setFieldType( new String[]{ ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ) } );
    List<List<String>> dgm1Data = new ArrayList<>();
    dgm1Data.add( Collections.singletonList( "1" ) );
    dgm1Data.add( Collections.singletonList( "2" ) );
    dgm.setDataLines( dgm1Data );

    StepMeta dg = new StepMeta( "input1", dgm );
    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    StepMeta textFileOutputStep = new StepMeta( "BACKLOG-21039", textFileOutputMeta );

    TransHopMeta hop = new TransHopMeta( dg, textFileOutputStep, true );
    transMeta.addStep( dg );
    transMeta.addStep( textFileOutputStep );
    transMeta.addTransHop( hop );

    RowMetaInterface row = transMeta.getPrevStepFields( textFileOutputStep );
    assertNotNull( row );
    assertEquals( 1, row.size() );
    assertEquals( "id", row.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, row.getValueMeta( 0 ).getType() );

    dgm.setFieldName( new String[]{ "id", "name" } );
    dgm.setFieldType( new String[]{
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ),
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ),
    } );

    row = transMeta.getPrevStepFields( textFileOutputStep );
    assertNotNull( row );
    assertEquals( 2, row.size() );
    assertEquals( "id", row.getValueMeta( 0 ).getName() );
    assertEquals( "name", row.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, row.getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, row.getValueMeta( 1 ).getType() );
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
  public void testGetPreviousStepsWhenStreamLookupStepPassedShouldClearCacheAndCallFindPreviousStepsWithFalseParam() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = new StepMeta( "stream_lookup_id", "stream_lookup_name", new StreamLookupMeta() );

    List<StepMeta> expectedResult = new ArrayList<>(  );
    List<StepMeta> invalidResult = new ArrayList<>(  );
    expectedResult.add( new StepMeta( "correct_mock", "correct_mock", new TextFileOutputMeta() ) );
    invalidResult.add( new StepMeta( "incorrect_mock", "incorrect_mock", new TextFileOutputMeta() ) );

    doNothing().when( transMeta ).clearPreviousStepCache();
    when( transMeta.findPreviousSteps( any( StepMeta.class ), eq( false ) ) ).thenReturn( expectedResult );
    when( transMeta.findPreviousSteps( any( StepMeta.class ), eq( true ) ) ).thenReturn( invalidResult );
    when( transMeta.getPreviousSteps( any() ) ).thenCallRealMethod();

    List<StepMeta> actualResult = transMeta.getPreviousSteps( stepMeta );

    verify( transMeta, times( 1 ) ).clearPreviousStepCache();
    assertEquals( expectedResult, actualResult );
  }

  @Test
  public void testGetPreviousStepsWhenNotStreamLookupStepPassedShouldCallFindPreviousStepsWithTrueParam() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = new StepMeta( "not_stream_lookup_id", "not_stream_lookup_name", new TextFileOutputMeta() );

    List<StepMeta> expectedResult = new ArrayList<>(  );
    List<StepMeta> invalidResult = new ArrayList<>(  );
    expectedResult.add( new StepMeta( "correct_mock", "correct_mock", new TextFileOutputMeta() ) );
    invalidResult.add( new StepMeta( "incorrect_mock", "incorrect_mock", new TextFileOutputMeta() ) );

    doNothing().when( transMeta ).clearPreviousStepCache();
    when( transMeta.getPreviousSteps( any() ) ).thenCallRealMethod();
    when( transMeta.findPreviousSteps( any( StepMeta.class ) ) ).thenCallRealMethod();
    when( transMeta.findPreviousSteps( any( StepMeta.class ), eq( true ) ) ).thenReturn( expectedResult );
    when( transMeta.findPreviousSteps( any( StepMeta.class ), eq( false ) ) ).thenReturn( invalidResult );

    List<StepMeta> actualResult = transMeta.getPreviousSteps( stepMeta );

    verify( transMeta, times( 0 ) ).clearPreviousStepCache();
    assertEquals( expectedResult, actualResult );
  }

  private StepMeta createStepMeta( String name ) {
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getName() ).thenReturn( name );
    return stepMeta;
  }
}
