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

package org.pentaho.di.trans.steps.randomccnumber;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith( MockitoJUnitRunner.class )
public class RandomCCNumberGeneratorHelperTest {

  @Mock private RandomCCNumberGeneratorMeta meta;
  @Mock private TransMeta transMeta;

  private RandomCCNumberGeneratorHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    helper = new RandomCCNumberGeneratorHelper( meta );
    queryParams = new HashMap<>();
  }

  @Test
  public void testGetCardTypes() {
    JSONObject response = helper.getCardTypes();

    assertEquals( BaseStepHelper.SUCCESS_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
    JSONArray cardTypes = (JSONArray) response.get( RandomCCNumberGeneratorHelper.CARD_TYPES_RESPONSE_KEY );
    assertNotNull( cardTypes );
    assertEquals( RandomCreditCardNumberGenerator.cardTypes.length, cardTypes.size() );

    for ( int i = 0; i < RandomCreditCardNumberGenerator.cardTypes.length; i++ ) {
      JSONObject item = (JSONObject) cardTypes.get( i );
      assertEquals( RandomCreditCardNumberGenerator.cardTypes[i], item.get( "name" ) );
    }
  }

  @Test
  public void testHandleStepAction_GetCardTypes() {
    JSONObject response = helper.handleStepAction(
      RandomCCNumberGeneratorHelper.GET_CARD_TYPES, transMeta, queryParams );

    assertEquals( BaseStepHelper.SUCCESS_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
    assertNotNull( response.get( RandomCCNumberGeneratorHelper.CARD_TYPES_RESPONSE_KEY ) );
  }

  @Test
  public void testHandleStepAction_InvalidMethod() {
    JSONObject response = helper.handleStepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE,
      response.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testConstants() {
    assertEquals( "getCardTypes", RandomCCNumberGeneratorHelper.GET_CARD_TYPES );
    assertEquals( "cardTypes", RandomCCNumberGeneratorHelper.CARD_TYPES_RESPONSE_KEY );
  }
}

