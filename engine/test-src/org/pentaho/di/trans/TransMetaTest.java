/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.mockito.Mockito;

public class TransMetaTest {

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
    Assert.assertEquals( minimalCanvasPoint.x, point.x );
    Assert.assertEquals( minimalCanvasPoint.y, point.y );

    //when Trans  content Step  than  trans should return minimal coordinate of step
    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    Mockito.when( stepMeta.getLocation() ).thenReturn( stepPoint );
    transMeta.addStep( stepMeta );
    Point actualStepPoint = transMeta.getMinimum();
    Assert.assertEquals( stepPoint.x - TransMeta.BORDER_INDENT, actualStepPoint.x );
    Assert.assertEquals( stepPoint.y - TransMeta.BORDER_INDENT, actualStepPoint.y );
  }


  @Test
  public void getThisStepFieldsPassesCloneRowMeta() throws Exception {
    final String overriddenValue = "overridden";

    StepMeta nextStep = mockStepMeta( "nextStep" );

    StepMetaInterface smi = Mockito.mock( StepMetaInterface.class );
    StepIOMeta ioMeta = Mockito.mock( StepIOMeta.class );
    Mockito.when( smi.getStepIOMeta() ).thenReturn( ioMeta );
    Mockito.doAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        RowMetaInterface rmi = (RowMetaInterface) invocation.getArguments()[ 0 ];
        rmi.clear();
        rmi.addValueMeta( new ValueMetaString( overriddenValue ) );
        return null;
      } } ).when( smi ).getFields(
        Mockito.any( RowMetaInterface.class ), Mockito.anyString(), Mockito.any( RowMetaInterface[].class ), Mockito.eq( nextStep ),
        Mockito.any( VariableSpace.class ), Mockito.any( Repository.class ), Mockito.any( IMetaStore.class ) );

    StepMeta thisStep = mockStepMeta( "thisStep" );
    Mockito.when( thisStep.getStepMetaInterface() ).thenReturn( smi );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "value" ) );

    RowMetaInterface thisStepsFields = transMeta.getThisStepFields( thisStep, nextStep, rowMeta );

    Assert.assertEquals( 1, thisStepsFields.size() );
    Assert.assertEquals( overriddenValue, thisStepsFields.getValueMeta( 0 ).getName() );
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
    Assert.assertEquals( dbMetaShared.getHostname(), dbMeta.getHostname() );
  }

  @Test
  public void testAddOrReplaceStep() throws Exception {
    StepMeta stepMeta = mockStepMeta( "ETL Metadata Injection" );
    MetaInjectMeta stepMetaInterfaceMock = Mockito.mock( MetaInjectMeta.class );
    Mockito.when( stepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterfaceMock );
    transMeta.addOrReplaceStep( stepMeta );
    Mockito.verify( stepMeta ).setParentTransMeta( Mockito.any( TransMeta.class ) );
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
      Assert.fail();
    }
  }

  @Test
  public void testContentChangeListener() throws Exception {
    ContentChangedListener listener = Mockito.mock( ContentChangedListener.class );
    transMeta.addContentChangedListener( listener );

    transMeta.setChanged();
    transMeta.setChanged( true );

    Mockito.verify( listener, Mockito.times( 2 ) ).contentChanged( Mockito.same( transMeta ) );

    transMeta.clearChanged();
    transMeta.setChanged( false );

    Mockito.verify( listener, Mockito.times( 2 ) ).contentSafe( Mockito.same( transMeta ) );

    transMeta.removeContentChangedListener( listener );
    transMeta.setChanged();
    transMeta.setChanged( true );

    Mockito.verifyNoMoreInteractions( listener );
  }

  @Test
  public void testCompare() throws Exception {
    TransMeta transMeta = new TransMeta( "aFile", "aName" );
    TransMeta transMeta2 = new TransMeta( "aFile", "aName" );
    Assert.assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    transMeta2.setVariable( "myVariable", "myValue" );
    Assert.assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    transMeta2.setFilename( null );
    Assert.assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile" );
    transMeta2.setName( null );
    Assert.assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile2" );
    transMeta2.setName( "aName" );
    Assert.assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    transMeta2.setFilename( "aFile" );
    transMeta2.setName( "aName2" );
    Assert.assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    transMeta.setFilename( null );
    transMeta2.setFilename( null );
    transMeta2.setName( "aName" );
    Assert.assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    RepositoryDirectoryInterface path1 = Mockito.mock( RepositoryDirectoryInterface.class );
    transMeta.setRepositoryDirectory( path1 );
    Mockito.when( path1.getPath() ).thenReturn( "aPath2" );
    RepositoryDirectoryInterface path2 = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( path2.getPath() ).thenReturn( "aPath" );
    transMeta2.setRepositoryDirectory( path2 );
    Assert.assertEquals( 1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( -1, transMeta.compare( transMeta2, transMeta ) );
    Mockito.when( path1.getPath() ).thenReturn( "aPath" );
    Assert.assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    ObjectRevision revision2 = Mockito.mock( ObjectRevision.class );
    transMeta2.setObjectRevision( revision2 );
    Assert.assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
    ObjectRevision revision1 = Mockito.mock( ObjectRevision.class );
    transMeta.setObjectRevision( revision1 );
    Mockito.when( revision1.getName() ).thenReturn( "aRevision" );
    Mockito.when( revision2.getName() ).thenReturn( "aRevision" );
    Assert.assertEquals( 0, transMeta.compare( transMeta, transMeta2 ) );
    Mockito.when( revision2.getName() ).thenReturn( "aRevision2" );
    Assert.assertEquals( -1, transMeta.compare( transMeta, transMeta2 ) );
    Assert.assertEquals( 1, transMeta.compare( transMeta2, transMeta ) );
  }

  @Test
  public void testEquals() throws Exception {
    TransMeta transMeta = new TransMeta( "1", "2" );
    Assert.assertFalse( transMeta.equals( "somethingelse" ) );
    Assert.assertTrue( transMeta.equals( new TransMeta( "1", "2" ) ) );
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
    Assert.assertSame( step1, hops.get( 0 ) );
    Assert.assertSame( step2, hops.get( 1 ) );
    Assert.assertSame( step3, hops.get( 2 ) );
    Assert.assertSame( step4, hops.get( 3 ) );
    Assert.assertEquals( hopMeta2, transMeta.findTransHop( "name2 --> name3 (enabled)" ) );
    Assert.assertEquals( hopMeta3, transMeta.findTransHopFrom( step3 ) );
    Assert.assertEquals( hopMeta2, transMeta.findTransHop( hopMeta2 ) );
    Assert.assertEquals( hopMeta1, transMeta.findTransHop( step1, step2 ) );
    Assert.assertEquals( null, transMeta.findTransHop( step3, step4, false ) );
    Assert.assertEquals( hopMeta3, transMeta.findTransHop( step3, step4, true ) );
    Assert.assertEquals( hopMeta2, transMeta.findTransHopTo( step3 ) );
    transMeta.removeTransHop( 0 );
    hops = transMeta.getTransHopSteps( true );
    Assert.assertSame( step2, hops.get( 0 ) );
    Assert.assertSame( step3, hops.get( 1 ) );
    Assert.assertSame( step4, hops.get( 2 ) );
    transMeta.removeTransHop( hopMeta2 );
    hops = transMeta.getTransHopSteps( true );
    Assert.assertSame( step3, hops.get( 0 ) );
    Assert.assertSame( step4, hops.get( 1 ) );
  }

  @Test
  public void testGetPrevInfoFields() throws KettleStepException {
    DataGridMeta dgm1 = new DataGridMeta();
    dgm1.setFieldName( new String[]{ "id", "colA" } );
    dgm1.allocate( 2 );
    dgm1.setFieldType( new String[]{
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER ),
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm1Data = new ArrayList<List<String>>();
    dgm1Data.add( Arrays.asList( new String[]{ "1", "A" } ) );
    dgm1Data.add( Arrays.asList( new String[]{ "2", "B" } ) );
    dgm1.setDataLines( dgm1Data );

    DataGridMeta dgm2 = new DataGridMeta();
    dgm2.allocate( 1 );
    dgm2.setFieldName( new String[]{ "moreData" } );
    dgm2.setFieldType( new String[]{
      ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) } );
    List<List<String>> dgm2Data = new ArrayList<List<String>>();
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
    Assert.assertNotNull( row );
    Assert.assertEquals( 1, row.size() );
    Assert.assertEquals( "moreData", row.getValueMeta( 0 ).getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, row.getValueMeta( 0 ).getType() );
  }

  @Test
  public void testAddStepWithChangeListenerInterface() {
    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    StepMetaChangeListenerInterfaceMock metaInterface = Mockito.mock( StepMetaChangeListenerInterfaceMock.class );
    Mockito.when( stepMeta.getStepMetaInterface() ).thenReturn( metaInterface );
    Assert.assertEquals( 0, transMeta.steps.size() );
    Assert.assertEquals( 0, transMeta.stepChangeListeners.size() );
    // should not throw exception if there are no steps in step meta
    transMeta.addStep( 0, stepMeta );
    Assert.assertEquals( 1, transMeta.steps.size() );
    Assert.assertEquals( 1, transMeta.stepChangeListeners.size() );

    transMeta.addStep( 0, stepMeta );
    Assert.assertEquals( 2, transMeta.steps.size() );
    Assert.assertEquals( 2, transMeta.stepChangeListeners.size() );
  }

  private static StepMeta mockStepMeta( String name ) {
    StepMeta meta = Mockito.mock( StepMeta.class );
    Mockito.when( meta.getName() ).thenReturn( name );
    return meta;
  }

  private abstract static class StepMetaChangeListenerInterfaceMock implements StepMetaInterface, StepMetaChangeListenerInterface {
    @Override
    public abstract Object clone();
  }

  @Test
  public void testLoadXml() throws KettleException {
    final String directory = "/home/admin";
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
    Mockito.when( variableSpace.listVariables() ).thenReturn( new String[0] );

    meta.loadXML( jobNode, null, Mockito.mock( IMetaStore.class ), rep, false, variableSpace,
      Mockito.mock( OverwritePrompter.class ) );
    meta.setInternalKettleVariables( null );

    Assert.assertEquals( repDirectory.getPath(), meta.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
  }

}
