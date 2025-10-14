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


package org.pentaho.di.trans.steps.calculator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Map;
import java.util.Objects;

public class CalculatorHelper extends BaseStepHelper {

  private static final String GET_CALC_TYPES = "getCalcTypes";
  private static final String GET_CALC_FIELDS = "getCalcFields";

  public CalculatorHelper() {
    super();
  }

  /**
   * Handles step-specific actions for Calculator.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_CALC_TYPES:
          response = getCalcTypes();
          break;
        case GET_CALC_FIELDS:
          response = getCalcFields( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the list of available calculation types and returns them as a JSON object.
   *
   * @return A JSON object containing the list of calculation types.
   */
  public JSONObject getCalcTypes() {
    JSONObject response = new JSONObject();
    JSONArray calculatorArray = new JSONArray();
    for ( int i = 0; i < CalculatorMetaFunction.calc_desc.length; i++ ) {
      JSONObject calcJsonObject = new JSONObject();
      calcJsonObject.put( "id", CalculatorMetaFunction.getCalcFunctionDesc( i ) );
      calcJsonObject.put( "name", CalculatorMetaFunction.getCalcFunctionLongDesc( i ) );
      calculatorArray.add( calcJsonObject );
    }

    response.put( "calculationTypes", calculatorArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Retrieves the list of available calculation fields and returns them as a JSON object.
   * This includes both existing calculator fields and fields from previous steps.
   *
   * @param transMeta   The transformation metadata.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the list of calculation fields.
   * @throws KettleStepException if there's an error retrieving field information.
   */
  public JSONObject getCalcFields( TransMeta transMeta, Map<String, String> queryParams ) throws KettleStepException {
    JSONObject response = new JSONObject();
    JSONArray fieldsArray = new JSONArray();
    String stepName = Objects.toString( queryParams.get( "stepName" ) );

    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof CalculatorMeta calculatorMeta ) {
        CalculatorMetaFunction[] calculatorMetaFunctions = calculatorMeta.getCalculation();
        for ( CalculatorMetaFunction calculatorMetaFunction : calculatorMetaFunctions ) {
          JSONObject jsonObject = new JSONObject();
          jsonObject.put( "name", calculatorMetaFunction.getFieldName() );
          fieldsArray.add( jsonObject );
        }
      }
    }


    RowMetaInterface rowMetaInterface = transMeta.getPrevStepFields( stepName );
    if ( Objects.nonNull( rowMetaInterface ) ) {
      for ( int i = 0; i < rowMetaInterface.size(); i++ ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put( "name", rowMetaInterface.getValueMeta( i ).getName() );
        fieldsArray.add( jsonObject );
      }
    }

    response.put( "fields", fieldsArray );
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    return response;
  }

}
