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

package org.pentaho.di.trans.steps.jsonoutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JsonOutputHelper extends BaseStepHelper {

  private static final Class<?> PKG = JsonOutputMeta.class;

  private static final String SHOW_FILE_NAME = "showFileName";
  private static final String GET_ENCODING_TYPES = "getEncodingTypes";
  private static final String GET_OPERATION_TYPES = "getOperationTypes";

  private Date startProcessingDate;

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_OPERATION_TYPES:
          response = getOperationTypesAction( queryParams );
          break;
        case GET_ENCODING_TYPES:
          response = getEncodingTypesAction( queryParams );
          break;
        case SHOW_FILE_NAME:
          response = showFileNameAction( transMeta, queryParams );
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


  public JSONObject getOperationTypesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray operationTypes = new JSONArray();

    for ( int i = 0; i < JsonOutputMeta.operationTypeCode.length; i++ ) {
      JSONObject operationType = new JSONObject();
      operationType.put( "id", JsonOutputMeta.operationTypeCode[ i ] );
      operationType.put( "name", JsonOutputMeta.operationTypeDesc[ i ] );
      operationTypes.add( operationType );
    }

    response.put( "operationTypes", operationTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  public JSONObject getEncodingTypesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray encodingsArray = new JSONArray();

    List<Charset> availableCharsets = new ArrayList<>( Charset.availableCharsets().values() );
    for ( Charset charset : availableCharsets ) {
      encodingsArray.add( charset.displayName() );
    }

    response.put( "encoding", encodingsArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  public JSONObject showFileNameAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JsonOutputMeta jsonOutputMeta =
      (JsonOutputMeta) transMeta.findStep( queryParams.get( "stepName" ) ).getStepMetaInterface();

    JSONArray fileList = new JSONArray();
    startProcessingDate = new Date();

    String fileName = Const.NVL( jsonOutputMeta.getFileName(), "" );
    int nrRowsInBloc = Const.toInt( jsonOutputMeta.getNrRowsInBloc(), 1 );
    int filesToShow = ( nrRowsInBloc <= 1 ) ? 1 : Math.min( 3, nrRowsInBloc );
    for ( int i = 0; i < filesToShow; i++ ) {
      String filename = buildName( jsonOutputMeta, fileName, nrRowsInBloc, i );
      fileList.add( filename );
    }

    if ( fileList.isEmpty() ) {
      response.put( "message", BaseMessages.getString( PKG, "JsonOutputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }

  private String buildName( JsonOutputMeta jsonOutputMeta, String baseFileName, int nrRowsInBloc, int index ) {
    StringBuilder filename = new StringBuilder( baseFileName );
    SimpleDateFormat daf = new SimpleDateFormat();
    // Add date if enabled
    if ( jsonOutputMeta.isDateInFilename() ) {
      daf.applyPattern( "yyyyMMdd" );
      filename.append( "_" ).append( daf.format( startProcessingDate ) );
    }
    // Add time if enabled
    if ( jsonOutputMeta.isTimeInFilename() ) {
      daf.applyPattern( "HHmmss" );
      filename.append( "_" ).append( daf.format( startProcessingDate ) );
    }
    // Add index for multiple files
    if ( nrRowsInBloc > 1 ) {
      filename.append( "_" ).append( index );
    }
    // Add extension
    if ( !Utils.isEmpty( jsonOutputMeta.getExtension() ) ) {
      filename.append( "." ).append( jsonOutputMeta.getExtension() );
    }
    return filename.toString();
  }
}
