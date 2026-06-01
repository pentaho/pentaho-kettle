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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;
import java.util.Objects;

public class RandomCCNumberGeneratorHelper extends BaseStepHelper {

  public static final String GET_CARD_TYPES = "getCardTypes";
  public static final String CARD_TYPES_RESPONSE_KEY = "cardTypes";

  public RandomCCNumberGeneratorHelper( RandomCCNumberGeneratorMeta meta ) {
    Objects.requireNonNull( meta, "meta" );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equals ( GET_CARD_TYPES ) ) {
      response = getCardTypes();
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Returns the list of credit card types from RandomCreditCardNumberGenerator.cardTypes.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getCardTypes() {
    JSONObject response = new JSONObject();
    JSONArray cardTypesArray = new JSONArray();
    for ( String cardType : RandomCreditCardNumberGenerator.cardTypes ) {
      JSONObject item = new JSONObject();
      item.put( "name", cardType );
      cardTypesArray.add( item );
    }
    response.put( CARD_TYPES_RESPONSE_KEY, cardTypesArray );
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    return response;
  }
}
