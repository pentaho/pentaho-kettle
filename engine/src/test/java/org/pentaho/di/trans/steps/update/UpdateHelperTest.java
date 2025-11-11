package org.pentaho.di.trans.steps.update;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class UpdateHelperTest {

  private TransMeta transMeta;
  private StepMeta stepMeta;

  private UpdateMeta updateMeta;

  private UpdateHelper update;
  private RowMeta rowMeta;

  @Before
  public void setup() {
    transMeta = mock( TransMeta.class );
    rowMeta = mock( RowMeta.class );
    stepMeta = mock( StepMeta.class );
    doReturn( stepMeta ).when( transMeta ).findStep( anyString() );
    when( stepMeta.getName() ).thenReturn( "update" );
    UpdateData updateData = mock( UpdateData.class );
    updateMeta = mock( UpdateMeta.class );
    update = new UpdateHelper( updateMeta );
  }

  @Test
  public void testGetSQLAction() throws KettleStepException {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    SQLStatement sqlStatement = new SQLStatement( "update", databaseMeta, "select * from abc" );

    try ( MockedConstruction<StepMeta> ignored = mockConstruction( StepMeta.class, ( mock, context ) -> when( updateMeta.getSQLStatements( transMeta, mock, rowMeta, null, null ) ).thenReturn( sqlStatement ) ) ) {
      when( transMeta.getPrevStepFields( anyString() ) ).thenReturn( rowMeta );
      when( updateMeta.getParentStepMeta() ).thenReturn( stepMeta );
      JSONObject response = update.handleStepAction( "getSQL", transMeta, new HashMap<>() );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertTrue( response.containsKey( "sql" ) );
    }
  }

  @Test
  public void testGetSQLAction_HasError() throws KettleStepException {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    SQLStatement sqlStatement = new SQLStatement( "update", databaseMeta, null );
    sqlStatement.setError( "Some error occurred" );

    try ( MockedConstruction<StepMeta> ignored = mockConstruction( StepMeta.class, ( mock, context ) -> when( updateMeta.getSQLStatements( transMeta, mock, rowMeta, null, null ) ).thenReturn( sqlStatement ) ) ) {
      when( transMeta.getPrevStepFields( anyString() ) ).thenReturn( rowMeta );
      when( updateMeta.getParentStepMeta() ).thenReturn( stepMeta );
      JSONObject response = update.handleStepAction( "getSQL", transMeta, new HashMap<>() );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertEquals( "Some error occurred", response.get( "errorMessage" ) );
    }
  }

  @Test
  public void testGetSQLAction_NoSQL() throws KettleStepException {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    SQLStatement sqlStatement = new SQLStatement( "update", databaseMeta, null ); // No SQL string

    try ( MockedConstruction<StepMeta> ignored = mockConstruction( StepMeta.class, ( mock, context ) -> when( updateMeta.getSQLStatements( transMeta, mock, rowMeta, null, null ) ).thenReturn( sqlStatement ) ) ) {
      when( transMeta.getPrevStepFields( anyString() ) ).thenReturn( rowMeta );
      when( updateMeta.getParentStepMeta() ).thenReturn( stepMeta );
      JSONObject response = update.handleStepAction( "getSQL", transMeta,new HashMap<>() );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertTrue( response.containsKey( "errorMessage" ) );
    }
  }

  @Test
  public void testGetSQLAction_Exception() throws KettleStepException {
    try ( MockedConstruction<StepMeta> ignored = mockConstruction( StepMeta.class, ( mock, context ) -> when( updateMeta.getSQLStatements( transMeta, mock, rowMeta, null, null ) )
      .thenThrow( new KettleStepException( "Exception occurred" ) ) ) ) {
      when( transMeta.getPrevStepFields( anyString() ) ).thenReturn( rowMeta );
      when( updateMeta.getParentStepMeta() ).thenReturn( stepMeta );
      JSONObject response = update.handleStepAction( "getSQL", transMeta, new HashMap<>() );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertEquals( "Exception occurred", response.get( "errorMessage" ).toString().trim() );
    }
  }
}
