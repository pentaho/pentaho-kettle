/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024-2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.excelwriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExcelWriterStepHelper extends BaseStepHelper {
  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";
  private static final String GET_FORMATS = "getFormats";
  private static final String GET_FILES = "getFiles";

  public ExcelWriterStepHelper() {
    super();
  }

  /**
   * Handles step-specific actions for ExcelInput.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_FILES:
          response = getFilesAction();
          break;
        case GET_FORMATS:
          response = getFormatsAction();
          break;
        case SET_MINIMAL_WIDTH:
          // The caller must provide ExcelWriterStepMeta, as in ExcelWriterStep.java
          ExcelWriterStepMeta excelWriterStepMeta = (ExcelWriterStepMeta) transMeta.getStep( 0 ).getStepMetaInterface();
          response = setMinimalWidthAction( excelWriterStepMeta );
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


  public JSONObject getFilesAction() {
    JSONObject result = new JSONObject();
    result.put( "message", "getFilesAction not implemented" );
    result.put( "action_status", "success" ); // Add this line
    return result;
  }

  public JSONObject getFormatsAction() {
    JSONObject response = new JSONObject();
    try {
      String[] formats = getFormats();
      JSONArray formatList = new JSONArray();
      for ( String format : formats ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put( "name", format );
        formatList.add( jsonObject );
      }
      response.put( "formats", formatList );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  public String[] getFormats() {
    String[] formats = BuiltinFormats.getAll();
    List<String> allFormats = Arrays.asList( formats );
    List<String> nonReservedFormats = new ArrayList<>( allFormats.size() );
    for ( String format : allFormats ) {
      if ( !format.startsWith( "reserved" ) ) {
        nonReservedFormats.add( format );
      }
    }
    Collections.sort( nonReservedFormats );
    formats = nonReservedFormats.toArray( new String[ 0 ] );
    return formats;
  }

  public JSONObject setMinimalWidthAction( ExcelWriterStepMeta excelWriterStepMeta ) throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray excelFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    for ( ExcelWriterStepFieldDTO excelWriterStepField : getUpdatedExcelFields( excelWriterStepMeta ) ) {
      excelFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( excelWriterStepField ) ) );
    }
    jsonObject.put( "updatedData", excelFileFields );
    return jsonObject;
  }

  private List<ExcelWriterStepFieldDTO> getUpdatedExcelFields( ExcelWriterStepMeta excelWriterStepMeta ) {
    List<ExcelWriterStepFieldDTO> excelFileFields = new ArrayList<>();
    if ( excelWriterStepMeta != null ) {
      for ( ExcelWriterStepField item : excelWriterStepMeta.getOutputFields() ) {
        ExcelWriterStepFieldDTO field = new ExcelWriterStepFieldDTO();
        field.setFormat( formatType( item.getType() ) );
        field.setType( item.getTypeDesc() );
        field.setCommentField( item.getCommentField() );
        field.setCommentAuthorField( item.getCommentAuthorField() );
        field.setStyleCell( item.getStyleCell() );
        field.setTitleStyleCell( item.getTitleStyleCell() );
        field.setFormula( item.isFormula() );
        field.setName( item.getName() );
        field.setTitle( item.getTitle() );
        field.setHyperlinkField( item.getHyperlinkField() );
        excelFileFields.add( field );
      }
    }
    return excelFileFields;
  }

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
