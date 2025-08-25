package org.pentaho.di.trans.steps.mergejoin;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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
  public void tearDown() {
    mockHelper.cleanUp();
  }

  @Test
  public void testPreviousKeysAction() throws Exception {
    RowMetaInterface rowMeta1 = new RowMeta();
    rowMeta1.addValueMeta( new ValueMetaString( "step1:key1" ) );
    rowMeta1.addValueMeta( new ValueMetaString( "step1:key2" ) );

    RowMetaInterface rowMeta2 = new RowMeta();
    rowMeta2.addValueMeta( new ValueMetaString( "step2:key1" ) );
    rowMeta2.addValueMeta( new ValueMetaString( "step2:key2" ) );

    MergeJoinMeta meta = new MergeJoinMeta();
    meta.setJoinType( "INNER" );
    StepMeta stepMeta1 = new StepMeta();
    stepMeta1.setName( "step1-name" );
    StepMeta stepMeta2 = new StepMeta();
    stepMeta2.setName( "step2-name" );

    meta.getStepIOMeta().getInfoStreams().get( 0 ).setStepMeta( stepMeta1 );
    meta.getStepIOMeta().getInfoStreams().get( 1 ).setStepMeta( stepMeta2 );
    when( mockHelper.transMeta.getStepFields( any( StepMeta.class ) ) ).thenReturn( rowMeta1 ).thenReturn( rowMeta2 );
    mergeJoin.init( meta, mockHelper.initStepDataInterface );

    JSONObject response =
      mergeJoin.doAction( "previousKeys", meta, null, null, new HashMap<>() );
    assertNotNull( response.get( "stepKeys" ) );
    assertEquals( 2, ( (JSONObject) response.get( "stepKeys" ) ).size() );

    when( mockHelper.transMeta.getStepFields( any( StepMeta.class ) ) ).thenThrow( new KettleStepException( "Error" ) );
    response = mergeJoin.doAction( "previousKeys", meta, null, null, new HashMap<>() );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

    when( mockHelper.transMeta.getStepFields( any( StepMeta.class ) ) ).thenThrow(
      new IndexOutOfBoundsException( "Error" ) );
    response = mergeJoin.doAction( "previousKeys", meta, null, null, new HashMap<>() );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

}
