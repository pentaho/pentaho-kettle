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

package org.pentaho.di.trans.steps.concatfields;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConcatFieldsHelper extends BaseStepHelper {

  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";

  public ConcatFieldsHelper() {
    super();
  }

  /**
   * Handles step-specific actions for ConcatFields.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( SET_MINIMAL_WIDTH.equals( method ) ) {
        response = setMinimalWidthAction( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Sets the output width to minimal width and returns the updated fields as JSON.
   *
   * @param transMeta   The transformation metadata.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the updated concat fields.
   * @throws JsonProcessingException if there's an error processing JSON.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject setMinimalWidthAction( TransMeta transMeta, Map<String, String> queryParams )
    throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray concatFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    String stepName = queryParams.get( "stepName" );
    for ( ConcatFieldDTO concatField : getUpdatedConcatFields( transMeta, stepName ) ) {
      concatFields.add( objectMapper.readTree( objectMapper.writeValueAsString( concatField ) ) );
    }
    jsonObject.put( "updatedData", concatFields );
    return jsonObject;
  }

  private List<ConcatFieldDTO> getUpdatedConcatFields( TransMeta transMeta, String stepName ) {
    List<ConcatFieldDTO> excelFileFields = new ArrayList<>();

    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof ConcatFieldsMeta concatFieldsMeta ) {

        for ( TextFileField item : concatFieldsMeta.getOutputFields() ) {
          ConcatFieldDTO concatFieldDTO = new ConcatFieldDTO();
          concatFieldDTO.setName( item.getName() );
          concatFieldDTO.setType( item.getTypeDesc() );
          concatFieldDTO.setFormat( formatType( item.getType() ) );
          concatFieldDTO.setLength( StringUtils.EMPTY );
          concatFieldDTO.setPrecision( StringUtils.EMPTY );
          concatFieldDTO.setCurrency( item.getCurrencySymbol() );
          concatFieldDTO.setDecimal( item.getDecimalSymbol() );
          concatFieldDTO.setGroup( item.getGroupingSymbol() );
          concatFieldDTO.setTrimType( "both" );
          concatFieldDTO.setNullif( item.getNullString() );
          excelFileFields.add( concatFieldDTO );
        }
      }
    }
    return excelFileFields;
  }

  /**
   * Formats the type based on the ValueMetaInterface type constants.
   *
   * @param type The type constant from ValueMetaInterface.
   * @return The formatted type string.
   */
  public String formatType( int type ) {
    switch ( type ) {
      case ValueMetaInterface.TYPE_STRING:
        return "";
      case ValueMetaInterface.TYPE_INTEGER:
        return "0";
      case ValueMetaInterface.TYPE_NUMBER:
        return "0.#####";
      default:
        break;
    }
    return null;
  }
}
