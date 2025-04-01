package org.pentaho.di.trans.steps.mergejoin;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;

public class MergeJoinTest {

  private StepMockHelper<MergeJoinMeta, MergeJoinData> mockHelper;
  private MergeJoin mergeJoin;

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    mockHelper = new StepMockHelper<>( "MergeJoinTest", MergeJoinMeta.class, MergeJoinData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    mergeJoin = Mockito.spy(
      new MergeJoin( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  @Test
  public void testPreviousKeysAction() throws Exception {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepIndex", "0" );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "key1" ) );
    rowMeta.addValueMeta( new ValueMetaString( "key2" ) );

    MergeJoinMeta meta = new MergeJoinMeta();
    StepMeta stepMetaValidList = new StepMeta();
    stepMetaValidList.setName( "test-name" );
    meta.setJoinType( "INNER" );

    meta.getStepIOMeta().getInfoStreams().get( 0 ).setStepMeta( stepMetaValidList );
    meta.getStepIOMeta().getInfoStreams().get( 1 ).setStepMeta( stepMetaValidList );
    when( mockHelper.transMeta.getStepFields( any( StepMeta.class ) ) ).thenReturn( rowMeta );
    mergeJoin.init( meta, mockHelper.initStepDataInterface );

    JSONObject response =
      mergeJoin.doAction( "previousKeys", meta, null, null, queryParams );

    assertNotNull( response.get( "keys" ) );
    assertEquals( 2, ( (JSONArray) response.get( "keys" ) ).size() );
  }

  @Test
  public void testPreviousKeysAction_WhenStepIndexIsNotValid() {
    MergeJoinMeta meta = new MergeJoinMeta();
    StepMeta stepMetaValidList = new StepMeta();
    stepMetaValidList.setName( "test-name" );
    meta.setJoinType( "INNER" );
    mergeJoin.init( meta, mockHelper.initStepDataInterface );

    JSONObject response =
      mergeJoin.doAction( "previousKeys", meta, null, null, new HashMap<>() );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }
}
