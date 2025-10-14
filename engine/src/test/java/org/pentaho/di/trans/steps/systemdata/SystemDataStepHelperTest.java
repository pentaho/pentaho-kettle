/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.systemdata;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SystemDataStepHelperTest {

  private SystemDataStepHelper systemDataStepHelper;
  private TransMeta transMeta;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    systemDataStepHelper = new SystemDataStepHelper();
  }

  @Test
  public void testTypeAction() {
    JSONObject response = systemDataStepHelper.handleStepAction( SystemDataStepHelper.METHOD_NAME, transMeta, Collections.emptyMap() );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertTrue( response.containsKey( SystemDataStepHelper.RESPONSE_KEY ) );

    JSONArray types = (JSONArray) response.get( SystemDataStepHelper.RESPONSE_KEY );
    assertNotNull( types );
    // Ensure TYPE_SYSTEM_INFO_NONE is not present
    for ( Object obj : types ) {
      JSONObject type = (JSONObject) obj;
      assertNotEquals( "", type.get( "id" ) );
    }
    // Ensure all other enum values are present
    int expectedCount = (int) Arrays.stream( SystemDataTypes.values() )
      .filter( t -> t != SystemDataTypes.TYPE_SYSTEM_INFO_NONE )
      .count();
    assertEquals( expectedCount, types.size() );
  }

  @Test
  public void testHandleStepActionWithUnknownMethod() {
    JSONObject response = systemDataStepHelper.handleStepAction( "unknownMethod", transMeta, Collections.emptyMap() );
    assertEquals( SystemDataStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE,
      response.get( SystemDataStepHelper.ACTION_STATUS ) );
  }
}
