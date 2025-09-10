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
package org.pentaho.di.trans.steps.randomvalue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.trans.steps.randomvalue.RandomValueStepHelper.METHOD_GET_RANDOM_FUNCTION_TYPES;
import static org.pentaho.di.trans.steps.randomvalue.RandomValueStepHelper.RESPONSE_KEY;

public class RandomValueStepHelperTest {

  private RandomValueStepHelper randomValueStepHelper;
  private TransMeta transMeta;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    randomValueStepHelper = new RandomValueStepHelper();
  }

  @Test
  public void testGetRandomFunctionTypesAction_returnsAllFunctionTypes() {
    JSONObject result =
      randomValueStepHelper.handleStepAction( METHOD_GET_RANDOM_FUNCTION_TYPES, transMeta, Collections.emptyMap() );
    assertNotNull( result );
    assertTrue( result.containsKey( RESPONSE_KEY ) );

    JSONArray array = (JSONArray) result.get( RESPONSE_KEY );
    assertNotNull( array );

    // Should match the number of non-null functions in RandomValueMeta.functions
    long expectedCount = Arrays.stream( RandomValueMeta.functions )
      .filter( Objects::nonNull )
      .count();
    assertEquals( expectedCount, array.size() );

    int arrIdx = 0;
    for ( RandomValueMetaFunction func : RandomValueMeta.functions ) {
      if ( func == null ) {
        continue;
      }
      JSONObject obj = (JSONObject) array.get( arrIdx++ );
      assertEquals( func.getCode(), obj.get( "id" ) );
      assertEquals( func.getDescription(), obj.get( "name" ) );
    }
  }

  @Test
  public void testHandleStepAction_withUnknownMethod_returnsFailureResponse() {
    JSONObject result = randomValueStepHelper.handleStepAction( "unknownMethod", transMeta, Collections.emptyMap() );
    assertNotNull( result );
    String actionStatus = RandomValueStepHelper.ACTION_STATUS;
    assertTrue( result.containsKey( actionStatus ) );
    assertEquals( RandomValueStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( actionStatus ) );
  }
}
