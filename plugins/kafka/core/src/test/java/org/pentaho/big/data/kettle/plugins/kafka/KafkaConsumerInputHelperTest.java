/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.big.data.kettle.plugins.kafka;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

@RunWith( MockitoJUnitRunner.class )
public class KafkaConsumerInputHelperTest {

  private KafkaConsumerInputHelper helper;
  private KafkaConsumerInputMeta meta;
  private TransMeta transMeta;

  @Mock
  private LogChannelInterfaceFactory logChannelFactory;

  @Mock
  private LogChannelInterface logChannel;

  @Mock
  private Repository repository;

  @Mock
  private IMetaStore metaStore;

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
    StepPluginType.getInstance().handlePluginAnnotation(
      KafkaConsumerInputMeta.class,
      KafkaConsumerInputMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Before
  public void setUp() {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    lenient().doReturn( LogLevel.BASIC ).when( logChannel ).getLogLevel();
    when( logChannelFactory.create( any() ) ).thenReturn( logChannel );

    meta = new KafkaConsumerInputMeta();
    meta.setTopics( Collections.singletonList( "test-topic" ) );
    meta.setConsumerGroup( "test-group" );

    transMeta = mock( TransMeta.class );
    when( transMeta.getMetaStore() ).thenReturn( metaStore );
    when( transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );

    helper = new KafkaConsumerInputHelper( meta );
  }

  @RunWith( Parameterized.class )
  public static class ParameterizedMethodValidationTest {
    private KafkaConsumerInputHelper helper;
    private TransMeta transMeta;

    private String method;
    private String expectedStatus;
    private boolean nullMeta;

    public ParameterizedMethodValidationTest( String method, String expectedStatus, boolean nullMeta ) {
      this.method = method;
      this.expectedStatus = expectedStatus;
      this.nullMeta = nullMeta;
    }

    @Parameterized.Parameters( name = "{0} (nullMeta={2}) should return {1}" )
    public static java.util.Collection<Object[]> data() {
      return Arrays.asList( new Object[][] {
        { "", FAILURE_METHOD_NOT_FOUND_RESPONSE, false },
        { null, FAILURE_METHOD_NOT_FOUND_RESPONSE, false },
        { "invalidMethod", FAILURE_METHOD_NOT_FOUND_RESPONSE, false },
        { "unknownAction", FAILURE_METHOD_NOT_FOUND_RESPONSE, false },
        { "getSubStepList", FAILURE_RESPONSE, true }
      } );
    }

    @Before
    public void setUp() {
      KafkaConsumerInputMeta meta = nullMeta ? null : new KafkaConsumerInputMeta();
      if ( meta != null ) {
        meta.setTopics( Collections.singletonList( "test-topic" ) );
        meta.setConsumerGroup( "test-group" );
      }

      transMeta = new TransMeta();
      transMeta.setBowl( DefaultBowl.getInstance() );

      helper = new KafkaConsumerInputHelper( meta );
    }

    @Test
    public void testHandleStepAction_MethodValidation() {
      JSONObject response = helper.handleStepAction( method, transMeta, new HashMap<>() );

      assertNotNull( response );
      assertEquals( expectedStatus, response.get( "actionStatus" ) );
    }
  }

  @Test
  public void testHandleStepAction_GetSubStepList_WithValidTransformation() {
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, queryParams );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithNullTransMeta() {
    JSONObject response = helper.handleStepAction( "getSubStepList", null, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithNullKafkaConsumerInputMeta() {
    helper = new KafkaConsumerInputHelper( null );
    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithBlankTransformationPath() {
    meta.setTransformationPath( "" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithNullBowl() {
    when( transMeta.getBowl() ).thenReturn( null );
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithValidTransformation() {
    TransMeta subTransMeta = new TransMeta();
    StepMeta filterStep = new StepMeta( "Filter", new KafkaConsumerInputMeta() );
    StepMeta selectStep = new StepMeta( "Select Values", new KafkaConsumerInputMeta() );
    StepMeta outputStep = new StepMeta( "Output", new KafkaConsumerInputMeta() );

    subTransMeta.addStep( filterStep );
    subTransMeta.addStep( selectStep );
    subTransMeta.addStep( outputStep );

    meta.setTransformationPath( "/path/to/sub.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_ReturnsEmptyList_WhenTransformationHasNoSteps() {
    TransMeta emptyTransMeta = new TransMeta();
    meta.setTransformationPath( emptyTransMeta.getFilename() );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithSpecialCharactersInStepNames() {
    StepMeta step1 = new StepMeta( "Filter (Data)", new KafkaConsumerInputMeta() );
    StepMeta step2 = new StepMeta( "Select & Values", new KafkaConsumerInputMeta() );
    transMeta.addStep( step1 );
    transMeta.addStep( step2 );

    meta.setTransformationPath( "/path/to/transformation.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_ReturnsSortedStepNames() {
    StepMeta stepZ = new StepMeta( "Z-Final", new KafkaConsumerInputMeta() );
    StepMeta stepA = new StepMeta( "A-First", new KafkaConsumerInputMeta() );
    StepMeta stepM = new StepMeta( "M-Middle", new KafkaConsumerInputMeta() );

    transMeta.addStep( stepZ );
    transMeta.addStep( stepA );
    transMeta.addStep( stepM );

    meta.setTransformationPath( transMeta.getFilename() );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_WithQueryParams() {
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "param1", "value1" );
    queryParams.put( "param2", "value2" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, queryParams );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_ExceptionHandling() {
    when( transMeta.getBowl() ).thenReturn( null );
    meta.setTransformationPath( "/valid/path.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_MultipleConsecutiveCalls() {
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    JSONObject response1 = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );
    assertNotNull( response1 );

    JSONObject response2 = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );
    assertNotNull( response2 );

    assertEquals( response1.get( "actionStatus" ), response2.get( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_WithVariables() {
    Variables variables = new Variables();
    variables.setVariable( "TRANSFORM_PATH", "/path/to/transformation.ktr" );

    meta.setTransformationPath( "${TRANSFORM_PATH}" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_StepNamesSorted() {
    StepMeta zStep = new StepMeta( "Z-Step", new KafkaConsumerInputMeta() );
    StepMeta bStep = new StepMeta( "B-Step", new KafkaConsumerInputMeta() );
    StepMeta mStep = new StepMeta( "M-Step", new KafkaConsumerInputMeta() );

    transMeta.addStep( zStep );
    transMeta.addStep( bStep );
    transMeta.addStep( mStep );

    meta.setTransformationPath( transMeta.getFilename() );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    if ( SUCCESS_RESPONSE.equals( response.get( "actionStatus" ) ) ) {
      JSONArray stepList = (JSONArray) response.get( "subStepList" );
      assertNotNull( stepList );
      assertTrue( stepList.size() > 0 );
      for ( int i = 1; i < stepList.size(); i++ ) {
        String prev = (String) stepList.get( i - 1 );
        String curr = (String) stepList.get( i );
        assertTrue( prev.compareTo( curr ) <= 0 );
      }
    }
  }

  @Test
  public void testGetSubStepList_NullMetaStore() {
    when( transMeta.getMetaStore() ).thenReturn( null );
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_NullRepository() {
    meta.setTransformationPath( "/path/to/transformation.ktr" );

    JSONObject response = helper.handleStepAction( "getSubStepList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_UnknownMethodWithNonNullMeta_ReturnsMethodNotFound() {
    JSONObject response = helper.handleStepAction( "someUnknownMethod", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetSubStepList_WithRealKtrFile_ReturnsSuccess() {
    TransMeta realTransMeta = new TransMeta();
    realTransMeta.setBowl( DefaultBowl.getInstance() );

    String ktrPath = getClass().getResource( "/consumerSub.ktr" ).getPath();
    meta.setTransformationPath( ktrPath );

    JSONObject response = helper.handleStepAction( "getSubStepList", realTransMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( "subStepList" ) );
  }

  @Test
  public void testGetClusterList_WithNullNamedClusterService() {
    meta.setNamedClusterService( null );

    JSONObject response = helper.handleStepAction( "getClusterList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetClusterList_WithNullTransMeta() {
    JSONObject response = helper.handleStepAction( "getClusterList", null, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetClusterList_WithNullKafkaConsumerInputMeta() {
    helper = new KafkaConsumerInputHelper( null );

    JSONObject response = helper.handleStepAction( "getClusterList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetClusterList_ReturnsClusterNames() throws MetaStoreException {
    NamedClusterService namedClusterService = mock( NamedClusterService.class );
    when( namedClusterService.listNames( metaStore ) ).thenReturn( Arrays.asList( "cluster1", "cluster2" ) );
    meta.setNamedClusterService( namedClusterService );

    JSONObject response = helper.handleStepAction( "getClusterList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( "clusterList" ) );
  }

  @Test
  public void testGetClusterList_MetaStoreException() throws MetaStoreException {
    NamedClusterService namedClusterService = mock( NamedClusterService.class );
    when( namedClusterService.listNames( metaStore ) ).thenThrow( new MetaStoreException( "error" ) );
    meta.setNamedClusterService( namedClusterService );

    JSONObject response = helper.handleStepAction( "getClusterList", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

}
