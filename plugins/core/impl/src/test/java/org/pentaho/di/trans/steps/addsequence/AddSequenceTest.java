package org.pentaho.di.trans.steps.addsequence;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddSequenceTest  {

  private StepMockHelper<AddSequenceMeta, AddSequenceData> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper =
        new StepMockHelper<>( "Test Add Sequence", AddSequenceMeta.class, AddSequenceData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
        .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testDoAction(){

    AddSequence step = new AddSequence( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface,
        0, stepMockHelper.transMeta, stepMockHelper.trans );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    Map<String, String> queryMap = new HashMap<>();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( stepMockHelper.transMeta.findDatabase( any() )).thenReturn( dbMeta );
    MockedConstruction<Database> mockConstructDB = Mockito.mockConstruction( Database.class, ( mockDB, jobContext ) -> {
      doNothing().when( mockDB ).connect();
      when( mockDB.getSequences() ).thenReturn( new String[]{ "sequence1", "sequence2" } );
    });

    JSONObject response = step.doAction("getSequence", stepMockHelper.initStepMetaInterface, stepMockHelper.transMeta,
        stepMockHelper.trans, queryMap );
    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

    mockConstructDB.close();
  }

}