package org.pentaho.di.trans.steps.mergejoin;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

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

}
