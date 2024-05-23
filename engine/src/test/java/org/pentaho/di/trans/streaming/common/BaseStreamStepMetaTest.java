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

package org.pentaho.di.trans.streaming.common;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

@RunWith ( MockitoJUnitRunner.class )
public class BaseStreamStepMetaTest {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private BaseStreamStepMeta meta;
  @Mock private LogChannelInterfaceFactory logChannelFactory;
  @Mock private LogChannelInterface logChannel;
  @Mock private RowMetaInterface rowMeta;
  @Mock private RowMetaInterface prevRowMeta;
  @Mock private StepMeta subTransStepMeta;
  @Mock private StepMeta nextStepMeta;
  @Mock private StepMetaInterface stepMetaInterface;
  @Mock private VariableSpace space;
  @Mock private Repository repo;
  @Mock private BaseStreamStepMeta.MappingMetaRetriever mappingMetaRetriever;
  @Mock private TransMeta subTransMeta;
  @Mock private TransMeta transMeta;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init( false );
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setUp() throws Exception {
    meta = new StuffStreamMeta();
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( mappingMetaRetriever.get( any(), any(), any(), any() ) ).thenReturn( subTransMeta );

    when( subTransMeta.getPrevStepFields( anyString() ) ).thenReturn( prevRowMeta );
    when( subTransMeta.getSteps() ).thenReturn( singletonList( subTransStepMeta ) );
    when( subTransStepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterface );
    when( subTransStepMeta.getName() ).thenReturn( "SubStepName" );
    meta.mappingMetaRetriever = mappingMetaRetriever;
  }

  @Step ( id = "StuffStream", name = "Stuff Stream" )
  @InjectionSupported ( localizationPrefix = "stuff", groups = { "stuffGroup" } )
  private static class StuffStreamMeta extends BaseStreamStepMeta {
    @Injection ( name = "stuff", group = "stuffGroup" )
    List<String> stuff = new ArrayList<>();

    // stuff needs to be mutable to support .add() for metadatainjection.
    // otherwise would use Arrays.asList();
    {
      stuff.add( "one" );
      stuff.add( "two" );
    }

    @Injection ( name = "AUTH_PASSWORD" )
    String password = "test";

    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                  TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }

    @Override public RowMeta getRowMeta( String origin, VariableSpace space ) {
      return null;
    }
  }

  @Test
  public void testCheckErrorsOnZeroSizeAndDuration() {
    meta.setBatchDuration( "0" );
    meta.setBatchSize( "0" );
    ArrayList<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 1, remarks.size() );
    assertEquals(
      "The \"Number of records\" and \"Duration\" fields can’t both be set to 0. Please set a value of 1 or higher "
        + "for one of the fields.",
      remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckErrorsOnNaN() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setBatchDuration( "blah" );
    meta.setBatchSize( "blah" );
    meta.setPrefetchCount( "blahblah" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 3, remarks.size() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 0 ).getType() );
    assertEquals( "The \"Duration\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 0 ).getText() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 1 ).getType() );
    assertEquals( "The \"Number of records\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 1 ).getText() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 2 ).getType() );
    assertEquals( "The \"Message prefetch limit\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 2 ).getText() );
  }

  @Test
  public void testCheckLessThanBatch() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setBatchSize( "2" );
    meta.setPrefetchCount( "1" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 1, remarks.size() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 0 ).getType() );
    assertEquals( "The \"Message prefetch limit\" must be equal to or greater than the \"Number of records\". 1 is not equal to or greater than 2",
      remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckEqualToBatch() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setBatchSize( "1" );
    meta.setPrefetchCount( "1" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckPrefetchZero() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setPrefetchCount( "0" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 0 ).getType() );
    assertEquals( "The \"Message prefetch limit\" must be greater than 0. 0 is not greater than 0",
      remarks.get( 0 ).getText() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 1 ).getType() );
    assertEquals( "The \"Message prefetch limit\" must be equal to or greater than the \"Number of records\". 0 is not equal to or greater "
        + "than 1000",
      remarks.get( 1 ).getText() );
  }

  @Test
  public void testCheckPrefetchNull() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setPrefetchCount( null );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 1, remarks.size() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 0 ).getType() );
    assertEquals( "The \"Message prefetch limit\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckErrorsOnVariables() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    Variables space = new Variables();
    space.setVariable( "something", "1000" );
    meta.setBatchSize( "${something}" );
    meta.setBatchDuration( "0" );
    meta.check( remarks, null, null, null, null, null, null, space, null, null );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckErrorsOnSubStepName() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    Variables space = new Variables();

    meta.setBatchSize( "10" );
    meta.setBatchDuration( "10" );
    meta.setSubStep( "MissingStep" );
    meta.check( remarks, null, null, null, null, null, null, space, null, null );
    assertEquals( 1, remarks.size() );
    assertEquals( "Unable to complete \"null\".  Cannot return fields from \"MissingStep\" because it does not exist in the sub-transformation.", remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckErrorsOnVariablesSubstituteError() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    Variables space = new Variables();
    space.setVariable( "something", "0" );
    meta.setBatchSize( "${something}" );
    meta.setBatchDuration( "${something}" );
    meta.check( remarks, null, null, null, null, null, null, space, null, null );
    assertEquals( 1, remarks.size() );
    assertEquals( "The \"Number of records\" and \"Duration\" fields can’t both be set to 0. Please set a value of 1 "
      + "or higher for one of the fields.", remarks.get( 0 ).getText() );
    testRoundTrip( meta );
  }

  @Test
  public void testBasicRoundTrip() {
    meta.setBatchDuration( "1000" );
    meta.setBatchSize( "100" );
    meta.setTransformationPath( "aPath" );
    testRoundTrip( meta );
  }

  @Test
  public void testRoundTripInjectionList() {
    StuffStreamMeta startingMeta = new StuffStreamMeta();
    startingMeta.stuff = new ArrayList<>();
    startingMeta.stuff.add( "foo" );
    startingMeta.stuff.add( "bar" );
    startingMeta.stuff.add( "baz" );
    startingMeta.setBatchDuration( "1000" );
    startingMeta.setBatchSize( "100" );
    startingMeta.setTransformationPath( "aPath" );
    startingMeta.setParallelism( "4" );
    testRoundTrip( startingMeta );
  }

  @Test
  public void testSaveDefaultEmptyConnection() {
    StuffStreamMeta meta = new StuffStreamMeta();
    testRoundTrip( meta );
  }


  @Test
  public void testGetResourceDependencies() {
    String stepId = "KafkConsumerInput";
    String path = "/home/bgroves/fake.ktr";

    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepID( stepId );
    StuffStreamMeta inputMeta = new StuffStreamMeta();
    List<ResourceReference> resourceDependencies = inputMeta.getResourceDependencies( new TransMeta(), stepMeta );
    assertEquals( 0, resourceDependencies.get( 0 ).getEntries().size() );

    inputMeta.setTransformationPath( path );
    resourceDependencies = inputMeta.getResourceDependencies( new TransMeta(), stepMeta );
    assertEquals( 1, resourceDependencies.get( 0 ).getEntries().size() );
    assertEquals( path, resourceDependencies.get( 0 ).getEntries().get( 0 ).getResource() );
    assertEquals( ResourceEntry.ResourceType.ACTIONFILE,
      resourceDependencies.get( 0 ).getEntries().get( 0 ).getResourcetype() );
    testRoundTrip( inputMeta );
  }

  @Test
  public void testReferencedObjectHasDescription() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    assertEquals( 1, meta.getReferencedObjectDescriptions().length );
    assertTrue( meta.getReferencedObjectDescriptions()[ 0 ] != null );
    testRoundTrip( meta );
  }

  @Test
  public void testIsReferencedObjectEnabled() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    assertEquals( 1, meta.isReferencedObjectEnabled().length );
    assertFalse( meta.isReferencedObjectEnabled()[ 0 ] );
    meta.setTransformationPath( "/some/path" );
    assertTrue( meta.isReferencedObjectEnabled()[ 0 ] );
    testRoundTrip( meta );
  }

  @Test
  public void testLoadReferencedObject() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    meta.setFileName( getClass().getResource( "/org/pentaho/di/trans/subtrans-executor-sub.ktr" ).getPath() );
    meta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    try {
      TransMeta subTrans = (TransMeta) meta.loadReferencedObject( 0, null, null, new Variables() );
      assertEquals( "subtrans-executor-sub", subTrans.getName() );
    } catch ( KettleException e ) {
      fail();
    }
    testRoundTrip( meta );
  }

  @Test
  public void testGetFieldsDoesEnvSubstitutionForSubStepName() throws KettleStepException {
    // https://jira.pentaho.com/browse/BACKLOG-22575
    BaseStreamStepMeta meta = new StuffStreamMeta();
    meta.setSubStep( "${parameter}" );
    when( space.environmentSubstitute( "${parameter}" ) ).thenReturn( "realSubStepName" );
    when( subTransStepMeta.getName() ).thenReturn( "realSubStepName" );

    meta.mappingMetaRetriever = mappingMetaRetriever;
    meta.getFields( rowMeta, "origin", null, nextStepMeta, space, repo, null );

    verify( space ).environmentSubstitute( "${parameter}" );
    verify( subTransMeta ).getPrevStepFields( "realSubStepName" );
    verify( stepMetaInterface )
      .getFields( rowMeta, "origin", null, nextStepMeta, space, repo, null );
  }

  @Test
  public void replacingFileNameAlsoSetsTransformationPath() {
    StuffStreamMeta stuffStreamMeta = new StuffStreamMeta();
    stuffStreamMeta.replaceFileName( "someName" );
    assertEquals( "someName", stuffStreamMeta.getTransformationPath() );
  }

  @Test
  public void testGetFileName() {
    meta = new StuffStreamMeta();
    String testPathName = "transformationPathName";
    String testFileName = "testFileName";

    // verify that when the fileName is not set, we get the transformation path
    meta.setTransformationPath( testPathName );
    assertThat( meta.getFileName(), equalTo( testPathName ) );

    // verify that when the fileName is set, we get it
    meta.setFileName( testFileName );
    assertThat( meta.getFileName(), equalTo( testFileName ) );
  }

  // Checks that a serialization->deserialization does not alter meta fields
  private void testRoundTrip( BaseStreamStepMeta thisMeta ) {
    StuffStreamMeta startingMeta = (StuffStreamMeta) thisMeta;
    String xml = startingMeta.getXML();
    StuffStreamMeta metaToRoundTrip = new StuffStreamMeta();
    try {
      Node stepNode = XMLHandler.getSubNode( XMLHandler.loadXMLString( "<step>" + xml + "</step>" ), "step" );

      metaToRoundTrip.loadXML( stepNode, Collections.emptyList(), (IMetaStore) null );
    } catch ( KettleXMLException e ) {
      throw new RuntimeException( e );
    }
    assertThat( startingMeta.getBatchDuration(), equalTo( metaToRoundTrip.getBatchDuration() ) );
    assertThat( startingMeta.getBatchSize(), equalTo( metaToRoundTrip.getBatchSize() ) );
    assertThat( startingMeta.getTransformationPath(), equalTo( metaToRoundTrip.getTransformationPath() ) );
    assertThat( startingMeta.getParallelism(), equalTo( metaToRoundTrip.getParallelism() ) );

    assertThat( startingMeta.stuff, equalTo( metaToRoundTrip.stuff ) );
  }
}
