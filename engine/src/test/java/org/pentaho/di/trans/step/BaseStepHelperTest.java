package org.pentaho.di.trans.step;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

public class BaseStepHelperTest {

  private BaseStepHelper helper;
  private LogChannelInterface logMock;

  @Before
  public void setUp() {
    logMock = mock( LogChannelInterface.class );
    helper = new BaseStepHelper() {
      @Override
      protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
        JSONObject response = new JSONObject();
        if ( "validMethod".equals( method ) ) {
          response.put( ACTION_STATUS, SUCCESS_RESPONSE );
        } else if ( "validMethodWithError".equals( method ) ) {
          response.put( ACTION_STATUS, FAILURE_RESPONSE );
          logMock.logError( "Failed with Error" );
        } else if ( "invalidMethod".equals( method ) ) {
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        } else {
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        }
        return response;
      }
    };
  }

  @Test
  public void testStepAction_ReturnsSuccess_ForValidMethod() {
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.stepAction( "validMethod", transMeta, queryParams );

    assertEquals( SUCCESS_RESPONSE, response.get( StepHelperInterface.ACTION_STATUS ) );
  }

  @Test
  public void testStepAction_ReturnsFailure_ForValidMethodWithError() {
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.stepAction( "validMethodWithError", transMeta, queryParams );

    assertEquals( FAILURE_RESPONSE, response.get( StepHelperInterface.ACTION_STATUS ) );
    verify( logMock ).logError( anyString() );
  }

  @Test
  public void testStepAction_ReturnsFailure_ForInvalidMethod() {
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.stepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( StepHelperInterface.ACTION_STATUS ) );
  }

  @Test
  public void testStepAction_HandlesExceptionGracefully() {
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.stepAction( null, transMeta, queryParams );

    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( StepHelperInterface.ACTION_STATUS ) );
  }

  @Test
  public void testIsFailedResponse_ReturnsTrue_ForFailureResponse() {
    JSONObject response = new JSONObject();
    response.put( StepHelperInterface.ACTION_STATUS, FAILURE_RESPONSE );

    assertTrue( helper.isFailedResponse( response ) );
  }

  @Test
  public void testIsFailedResponse_ReturnsFalse_ForSuccessResponse() {
    JSONObject response = new JSONObject();
    response.put( StepHelperInterface.ACTION_STATUS, SUCCESS_RESPONSE );

    assertFalse( helper.isFailedResponse( response ) );
  }

  @Test
  public void testIsFailedResponse_ReturnsTrue_ForNullResponse() {
    assertTrue( helper.isFailedResponse( null ) );
  }
}