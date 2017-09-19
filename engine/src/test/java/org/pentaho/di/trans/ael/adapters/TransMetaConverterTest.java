/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.trans.ael.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.osgi.api.NamedClusterOsgi;
import org.pentaho.di.core.osgi.api.NamedClusterServiceOsgi;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class TransMetaConverterTest {

  @Spy StepMetaInterface stepMetaInterface = new DummyTransMeta();

  final String XML = "<xml></xml>";

  @Before
  public void before() throws KettleException {
    when( stepMetaInterface.getXML() ).thenReturn( XML );
  }

  @BeforeClass
  public static void init() throws Exception {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void simpleConvert() {
    TransMeta meta = new TransMeta();
    meta.setFilename( "fileName" );
    meta.addStep( new StepMeta( "stepName", stepMetaInterface ) );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( meta.getFilename() ) );
    assertThat( trans.getOperations().size(), is( 1 ) );
    assertThat( trans.getOperations().get( 0 ).getId(), is( "stepName" ) );
  }

  @Test
  public void transWithHops() {
    TransMeta meta = new TransMeta();
    meta.setFilename( "fileName" );
    StepMeta from = new StepMeta( "step1", stepMetaInterface );
    meta.addStep( from );
    StepMeta to = new StepMeta( "step2", stepMetaInterface );
    meta.addStep( to );
    meta.addTransHop( new TransHopMeta( from, to ) );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( meta.getFilename() ) );
    assertThat( trans.getOperations().size(), is( 2 ) );
    assertThat( trans.getHops().size(), is( 1 ) );
    assertThat( trans.getHops().get( 0 ).getFrom().getId(), is( from.getName() ) );
    assertThat( trans.getHops().get( 0 ).getTo().getId(), is( to.getName() ) );

    assertThat(
      trans.getHops().stream().map( Hop::getType ).collect( Collectors.toList() ),
      everyItem( is( Hop.TYPE_NORMAL ) )
    );
  }

  @Test
  public void transIdFromRepo() throws Exception {
    TransMeta meta = new TransMeta();
    meta.setName( "transName" );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( "/transName" ) );
  }


  @Test
  public void transConfigItems() throws Exception {
    TransMeta meta = new TransMeta();
    meta.setName( "foo" );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getConfig().get( TransMetaConverter.TRANS_META_NAME_CONF_KEY ),
      is( "foo" ) );
    assertThat( (String) trans.getConfig().get( TransMetaConverter.TRANS_META_CONF_KEY ),
       startsWith( "<transformation>" ) );
  }

  @Test
  public void transConfigItemsNoNameSpecified() throws Exception {
    TransMeta meta = new TransMeta();
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getConfig().get( TransMetaConverter.TRANS_META_NAME_CONF_KEY ),
      is( TransMetaConverter.TRANS_DEFAULT_NAME ) );
    assertThat( (String) trans.getConfig().get( TransMetaConverter.TRANS_META_CONF_KEY ),
      startsWith( "<transformation>" ) );
  }

  @Test
  public void testDisabledHops() {
    TransMeta trans = new TransMeta();
    StepMeta start = new StepMeta( "Start", stepMetaInterface );
    trans.addStep( start );
    StepMeta withEnabledHop = new StepMeta( "WithEnabledHop", stepMetaInterface );
    trans.addStep( withEnabledHop );
    StepMeta withDisabledHop = new StepMeta( "WithDisabledHop", stepMetaInterface );
    trans.addStep( withDisabledHop );
    StepMeta shouldStay = new StepMeta( "ShouldStay", stepMetaInterface );
    trans.addStep( shouldStay );
    StepMeta shouldNotStay = new StepMeta( "ShouldNotStay", stepMetaInterface );
    trans.addStep( shouldNotStay );
    StepMeta withEnabledAndDisabledHops = new StepMeta( "WithEnabledAndDisabledHops", stepMetaInterface );
    trans.addStep( withEnabledAndDisabledHops );
    StepMeta afterEnabledDisabled = new StepMeta( "AfterEnabledDisabled", stepMetaInterface );
    trans.addStep( afterEnabledDisabled );

    trans.addTransHop( new TransHopMeta( start, withEnabledHop ) );
    trans.addTransHop( new TransHopMeta( start, withDisabledHop, false ) );
    trans.addTransHop( new TransHopMeta( withEnabledHop, shouldStay ) );
    trans.addTransHop( new TransHopMeta( withDisabledHop, shouldStay ) );
    trans.addTransHop( new TransHopMeta( withDisabledHop, shouldNotStay ) );
    trans.addTransHop( new TransHopMeta( start, withEnabledAndDisabledHops ) );
    trans.addTransHop( new TransHopMeta( withEnabledHop, withEnabledAndDisabledHops, false ) );
    trans.addTransHop( new TransHopMeta( withEnabledAndDisabledHops, afterEnabledDisabled ) );

    Transformation transformation = TransMetaConverter.convert( trans );

    List<String>
      steps =
      transformation.getOperations().stream().map( op -> op.getId() ).collect( Collectors.toList() );
    assertThat( "Only 5 ops should exist", steps.size(), is( 5 ) );
    assertThat( steps, hasItems( "Start", "WithEnabledHop", "ShouldStay", "WithEnabledAndDisabledHops",
        "AfterEnabledDisabled" ) );

    List<String> hops = transformation.getHops().stream().map( hop -> hop.getId() ).collect( Collectors.toList() );
    assertThat( "Only 4 hops should exist", hops.size(), is( 4 ) );
    assertThat( hops, hasItems( "Start -> WithEnabledHop", "WithEnabledHop -> ShouldStay",
        "Start -> WithEnabledAndDisabledHops", "WithEnabledAndDisabledHops -> AfterEnabledDisabled" ) );
  }

  @Test
  public void testRemovingDisabledInputSteps() {
    TransMeta trans = new TransMeta();
    StepMeta inputToBeRemoved = new StepMeta( "InputToBeRemoved", stepMetaInterface );
    trans.addStep( inputToBeRemoved );
    StepMeta inputToStay = new StepMeta( "InputToStay", stepMetaInterface );
    trans.addStep( inputToStay );
    StepMeta inputReceiver1 = new StepMeta( "InputReceiver1", stepMetaInterface );
    trans.addStep( inputReceiver1 );
    StepMeta inputReceiver2 = new StepMeta( "InputReceiver2", stepMetaInterface );
    trans.addStep( inputReceiver2 );

    TransHopMeta hop1 = new TransHopMeta( inputToBeRemoved, inputReceiver1, false );
    TransHopMeta hop2 = new TransHopMeta( inputToStay, inputReceiver1 );
    TransHopMeta hop3 = new TransHopMeta( inputToBeRemoved, inputReceiver2, false );
    trans.addTransHop( hop1 );
    trans.addTransHop( hop2 );
    trans.addTransHop( hop3 );

    Transformation transformation = TransMetaConverter.convert( trans );

    List<String>
        steps =
        transformation.getOperations().stream().map( op -> op.getId() ).collect( Collectors.toList() );
    assertThat( "Only 2 ops should exist", steps.size(), is( 2 ) );
    assertThat( steps, hasItems( "InputToStay", "InputReceiver1" ) );

    List<String> hops = transformation.getHops().stream().map( hop -> hop.getId() ).collect( Collectors.toList() );
    assertThat( "Only 1 hop should exist", hops.size(), is( 1 ) );
    assertThat( hops, hasItems( "InputToStay -> InputReceiver1" ) );
  }

  @Test
  public void testMultipleDisabledHops() {
    TransMeta trans = new TransMeta();
    StepMeta input = new StepMeta( "Input", stepMetaInterface );
    trans.addStep( input );
    StepMeta step1 = new StepMeta( "Step1", stepMetaInterface );
    trans.addStep( step1 );
    StepMeta step2 = new StepMeta( "Step2", stepMetaInterface );
    trans.addStep( step2 );
    StepMeta step3 = new StepMeta( "Step3", stepMetaInterface );
    trans.addStep( step3 );

    TransHopMeta hop1 = new TransHopMeta( input, step1, false );
    TransHopMeta hop2 = new TransHopMeta( step1, step2, false );
    TransHopMeta hop3 = new TransHopMeta( step2, step3, false );
    trans.addTransHop( hop1 );
    trans.addTransHop( hop2 );
    trans.addTransHop( hop3 );

    Transformation transformation = TransMetaConverter.convert( trans );
    assertThat( "Trans has steps though all of them should be removed", transformation.getOperations().size(),
        is( 0 ) );
    assertThat( "Trans has hops though all of them should be removed", transformation.getHops().size(), is( 0 ) );
  }

  @Test
  public void errorHops() throws Exception {
    TransMeta meta = new TransMeta();
    meta.setFilename( "fileName" );
    StepMeta from = new StepMeta( "step1", stepMetaInterface );
    meta.addStep( from );
    StepMeta to = new StepMeta( "step2", stepMetaInterface );
    meta.addStep( to );
    meta.addTransHop( new TransHopMeta( from, to ) );
    StepMeta error = new StepMeta( "errorHandler", stepMetaInterface );
    meta.addStep( error );
    TransHopMeta errorHop = new TransHopMeta( from, error );
    errorHop.setErrorHop( true );
    meta.addTransHop( errorHop );
    Transformation trans = TransMetaConverter.convert( meta );
    Map<String, List<Hop>> hops = trans.getHops().stream().collect( Collectors.groupingBy( Hop::getType ) );

    List<Hop> normalHops = hops.get( Hop.TYPE_NORMAL );
    assertThat( normalHops.size(), is( 1 ) );
    assertThat( normalHops.get( 0 ).getTo().getId(), is( "step2" ) );

    List<Hop> errorHops = hops.get( Hop.TYPE_ERROR );
    assertThat( errorHops.size(), is( 1 ) );
    assertThat( errorHops.get( 0 ).getTo().getId(), is( "errorHandler" ) );

    assertThat(
      hops.values().stream()
        .flatMap( List::stream )
        .map( Hop::getFrom ).map( Operation::getId )
        .collect( Collectors.toList() ),
      everyItem( equalTo( "step1" ) )
    );
  }

  @Test
  public void lazyConversionTurnedOff() throws KettleException {
    KettleEnvironment.init();

    TransMeta transMeta = new TransMeta();

    CsvInputMeta csvInputMeta = new CsvInputMeta();
    csvInputMeta.setLazyConversionActive( true );
    StepMeta csvInput = new StepMeta( "Csv", csvInputMeta );
    transMeta.addStep( csvInput );

    TableInputMeta tableInputMeta = new TableInputMeta();
    tableInputMeta.setLazyConversionActive( true );
    StepMeta tableInput = new StepMeta( "Table", tableInputMeta );
    transMeta.addStep( tableInput );

    Transformation trans = TransMetaConverter.convert( transMeta );

    TransMeta cloneMeta;

    String transMetaXml = (String) trans.getConfig().get( TransMetaConverter.TRANS_META_CONF_KEY );
    Document doc;
    try {
      doc = XMLHandler.loadXMLString( transMetaXml );
      Node stepNode = XMLHandler.getSubNode( doc, "transformation" );
      cloneMeta = new TransMeta( stepNode, null );
    } catch ( KettleXMLException | KettleMissingPluginsException e ) {
      throw new RuntimeException( e );
    }

    assertThat( ( (CsvInputMeta) cloneMeta.findStep( "Csv" ).getStepMetaInterface() ).isLazyConversionActive(),
        is( false ) );
    assertThat( ( (TableInputMeta) cloneMeta.findStep( "Table" ).getStepMetaInterface() ).isLazyConversionActive(),
        is( false ) );
  }

  @Test
  public void testIncludesSubTransformations() throws Exception {
    TransMeta parentTransMeta = new TransMeta( getClass().getResource( "trans-meta-converter-parent.ktr" ).getPath() );
    Transformation transformation = TransMetaConverter.convert( parentTransMeta );

    @SuppressWarnings( { "unchecked", "ConstantConditions" } )
    HashMap<String, Transformation> config =
      (HashMap<String, Transformation>) transformation.getConfig( TransMetaConverter.SUB_TRANSFORMATIONS_KEY ).get();
    assertEquals( 1, config.size() );
    assertNotNull( config.get( "file://" + getClass().getResource( "trans-meta-converter-sub.ktr" ).getPath() ) );
  }

  @Test
  public void testIncludesSubTransformationsFromRepository() throws Exception {
    TransMeta parentTransMeta = new TransMeta( getClass().getResource( "trans-meta-converter-parent.ktr" ).getPath() );
    Repository repository = mock( Repository.class );
    TransMeta transMeta = new TransMeta();
    RepositoryDirectoryInterface repositoryDirectory = new RepositoryDirectory();
    String directory = getClass().getResource( "" ).toString();
    when( repository.findDirectory( directory.substring( 0, directory.length() - 1 ) ) ).thenReturn( repositoryDirectory );
    when( repository.loadTransformation( "trans-meta-converter-sub.ktr", repositoryDirectory, null, true, null ) ).thenReturn( transMeta );
    parentTransMeta.setRepository( repository );
    Transformation transformation = TransMetaConverter.convert( parentTransMeta );

    @SuppressWarnings( { "unchecked", "ConstantConditions" } )
    HashMap<String, Transformation> config =
      (HashMap<String, Transformation>) transformation.getConfig( TransMetaConverter.SUB_TRANSFORMATIONS_KEY ).get();
    assertEquals( 1, config.size() );
    assertNotNull( config.get( "file://" + getClass().getResource( "trans-meta-converter-sub.ktr" ).getPath() ) );
  }

  @Test
  public void testClonesTransMeta() throws KettleException {
    class ResultCaptor implements Answer<Object> {
      private Object result;

      public Object getResult() {
        return result;
      }

      @Override public java.lang.Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        result = invocationOnMock.callRealMethod();
        return result;
      }
    }

    TransMeta originalTransMeta = spy( new TransMeta() );

    ResultCaptor cloneTransMetaCaptor = new ResultCaptor();
    doAnswer( cloneTransMetaCaptor ).when( originalTransMeta ).realClone( eq( false ) );

    originalTransMeta.setName( "TransName" );

    TransMetaConverter.convert( originalTransMeta );

    TransMeta cloneTransMeta = (TransMeta) cloneTransMetaCaptor.getResult();

    verify( originalTransMeta ).realClone( eq( false ) );
    assertThat( cloneTransMeta.getName(), is( originalTransMeta.getName() ) );
    verify( originalTransMeta, never() ).getXML();
    verify( cloneTransMeta ).getXML();
  }

  @Test
  public void testReplaceHadoopClusterToFileSystemURL() throws KettleException, MetaStoreException {
    TransMeta origTransMeta = new TransMeta();
    Variables variables = new Variables();
    String inputUrl = "hc://CDH511Unsecure/tmp/small.csv";
    String outputUrl = "hc://CDH511Unsecure/tmp";
    TransMeta transMeta = spy( origTransMeta );
    IMetaStore metaStore = mock( IMetaStore.class );
    NamedClusterServiceOsgi namedClusterServiceOsgi = mock( NamedClusterServiceOsgi.class );
    NamedClusterOsgi namedClusterOsgi = mock( NamedClusterOsgi.class );
    NamedClusterEmbedManager namedClusterEmbedManager = mock( NamedClusterEmbedManager.class );

    transMeta.setParentVariableSpace( variables );
    transMeta.setMetaStore( metaStore );
    transMeta.setNamedClusterServiceOsgi( namedClusterServiceOsgi );

    doReturn( transMeta ).when( transMeta ).realClone( false );
    doReturn( namedClusterEmbedManager ).when( transMeta ).getNamedClusterEmbedManager( );
    doReturn( namedClusterOsgi ).when( namedClusterServiceOsgi ).read( "CDH511Unsecure", metaStore );
    doReturn( null ).when( namedClusterServiceOsgi ).read( "CDH512Unsecure", metaStore );

    when( namedClusterOsgi.processURLsubstitution( inputUrl, metaStore,
        variables ) ).thenReturn( "hdfs://user:password@svqxbdcn6cdh511n1.server.com:8020/tmp/small.csv" );
    when( namedClusterOsgi.processURLsubstitution( outputUrl, metaStore,
        variables ) ).thenReturn( "hdfs://user:password@svqxbdcn6cdh511n1.server.com:8020/tmp" );

    TextFileInputMeta textFileInputMeta = new TextFileInputMeta();

    String[] inputFiles = new String[6];
    inputFiles[0] = "hc://CDH511Unsecure/tmp/small.csv";
    inputFiles[1] = "hc://CDH512Unsecure/tmp/small.csv";
    inputFiles[2] = "hdfs://user:password@mycluster.domain.com:8020/myfolder/test/testfile";
    inputFiles[3] = "hdfs://HACluster/tmp/TestJob.kjb";
    inputFiles[4] = "C:/Users/testuser/Downloads/testfile";
    inputFiles[5] = "file:///C:/Users/testuser/Downloads/testtrans.ktr";

    textFileInputMeta.setFileNameForTest( inputFiles );

    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    textFileOutputMeta.setFileName( "hc://CDH511Unsecure/tmp" );
    textFileOutputMeta.allocate( 0 );
    StepMeta textFileInput = new StepMeta( "TextFileInput", textFileInputMeta );
    StepMeta textFileOutput = new StepMeta( "TextFileOutput", textFileOutputMeta );

    transMeta.addStep( textFileInput );
    transMeta.addStep( textFileOutput );
    transMeta.addTransHop( new TransHopMeta( textFileInput, textFileOutput ) );
    TransMeta cloneMeta = getTransMetaFromTrans( TransMetaConverter.convert( transMeta ) );

    for ( StepMeta stepMeta : cloneMeta.getSteps() ) {
      if ( stepMeta.getStepMetaInterface() instanceof TextFileInputMeta ) {
        TextFileInputMeta meta = (TextFileInputMeta) stepMeta.getStepMetaInterface();
        String[] files = meta.getFileName();
        for ( int i = 0; i < files.length; i++ ) {
          if ( files[i] != null ) {
            assertFalse( files[i].contains( "hc://CDH511Unsecure" ) );
          }
        }
      } else if ( stepMeta.getStepMetaInterface() instanceof TextFileOutputMeta ) {
        TextFileOutputMeta meta = (TextFileOutputMeta) stepMeta.getStepMetaInterface();
        String filename = meta.getFileName();
        if ( filename != null ) {
          assertFalse( filename.contains( "hc://" ) );
        }
      }
    }
  }


  private TransMeta getTransMetaFromTrans( Transformation trans ) {
    String transMetaXml = (String) trans.getConfig().get( TransMetaConverter.TRANS_META_CONF_KEY );
    Document doc;
    try {
      doc = XMLHandler.loadXMLString( transMetaXml );
      Node stepNode = XMLHandler.getSubNode( doc, "transformation" );
      return new TransMeta( stepNode, null );
    } catch ( KettleXMLException | KettleMissingPluginsException e ) {
      throw new RuntimeException( e );
    }
  }
}
