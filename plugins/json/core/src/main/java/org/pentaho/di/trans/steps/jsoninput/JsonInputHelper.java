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

package org.pentaho.di.trans.steps.jsoninput;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.jsoninput.json.JsonSampler;
import org.pentaho.di.trans.steps.jsoninput.json.node.Node;
import org.pentaho.di.trans.steps.jsoninput.json.node.ValueNode;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class JsonInputHelper extends BaseStepHelper {

  private static final String GETFILES = "getFiles";
  private static final String SELECTFIELDS = "selectFields";
  private static final Class<?> PKG = JsonInputMeta.class;
  private static final String OBJECT = "Object";
  private static final String ARRAY = "Array";


  public JsonInputHelper() {
    super();
  }

  public JSONObject convertToJsonObject( Node node ) {
    JSONObject response = new JSONObject();
    if ( Objects.nonNull( node ) ) {
      if ( OBJECT.equals( node.getType() ) ) {
        response.put( node.getKey() == null ? StringUtils.EMPTY : node.getKey(), processObject( node ) );
      } else if ( ARRAY.equals( node.getType() ) ) {
        response.put( node.getKey(), processArray( node ) );
      } else {
        response.put( node.getKey(), processValues( node ) );
      }
    }
    return response;
  }

  /**
   * Processes a value node and returns its value.
   *
   * @param node The value node.
   * @return The value of the node.
   */
  private Object processValues( Node node ) {
    return ( (ValueNode) node ).getValue();
  }

  /**
   * Processes an array node and returns a JSONArray.
   *
   * @param node The array node.
   * @return A JSONArray representing the array.
   */
  private JSONArray processArray( Node node ) {
    JSONArray array = new JSONArray();
    for ( Node child : node.getChildren() ) {
      if ( OBJECT.equals( child.getType() ) ) {
        array.add( processObject( child ) );
      } else if ( ARRAY.equals( child.getType() ) ) {
        array.add( processArray( child ) );
      } else {
        array.add( processValues( child ) );
      }
    }
    return array;
  }

  /**
   * Processes an object node and returns a JSONObject.
   *
   * @param node The object node.
   * @return A JSONObject representing the object.
   */
  private Map<String, Object> processObject( Node node ) {
    Map<String, Object> linkedHashMap = new LinkedHashMap<>();
    for ( Node child : node.getChildren() ) {
      if ( OBJECT.equals( child.getType() ) ) {
        linkedHashMap.put( child.getKey(), processObject( child ) );
      } else if ( ARRAY.equals( child.getType() ) ) {
        linkedHashMap.put( child.getKey(), processArray( child ) );
      } else {
        linkedHashMap.put( child.getKey(), processValues( child ) );
      }
    }
    return linkedHashMap;
  }


  @Override public JSONObject handleStepAction( String method, TransMeta transMeta,
                                                Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case GETFILES:
        response = getFilesAction( transMeta, queryParams );
        break;
      case SELECTFIELDS:
        response = selectFieldsAction( transMeta, queryParams );
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFilesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JsonInputMeta jsonInputMeta =
      (JsonInputMeta) transMeta.findStep( queryParams.get( "stepName" ) ).getStepMetaInterface();

    FileInputList fileInputList = jsonInputMeta.getFiles( transMeta.getBowl(), transMeta );
    String[] files = fileInputList.getFileStrings();

    JSONArray fileList = new JSONArray();

    if ( files == null || files.length == 0 ) {
      response.put( "message", BaseMessages.getString( PKG, "JsonInputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      fileList.addAll( Arrays.asList( files ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject selectFieldsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JsonInputMeta jsonInputMeta =
      (JsonInputMeta) transMeta.findStep( queryParams.get( "stepName" ) ).getStepMetaInterface();

    try {
      FileInputList fileInputList = jsonInputMeta.getFiles( transMeta.getBowl(), transMeta );
      if ( fileInputList == null || fileInputList.getFiles() == null || fileInputList.getFiles().isEmpty() ) {
        return buildErrorResponse(
          "JsonInput.Error.UnableToView.Label",
          "JsonInput.Error.NoInputSpecified.Message"
        );
      }
      String[] files = fileInputList.getFileStrings();

      InputStream inputStream = KettleVFS.getInstance( transMeta.getBowl() ).getInputStream( files[ 0 ],
        transMeta );
      // Parse the JSON file
      JsonSampler jsonSampler = new JsonSampler( transMeta.getBowl() );
      JsonFactory jsonFactory = new MappingJsonFactory();
      JsonParser jsonParser = jsonFactory.createParser( inputStream );
      Node rootNode = jsonSampler.getNode( jsonParser );

      JSONObject jsonObject = convertToJsonObject( rootNode );
      response.put( "data", jsonObject );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      log.logError( "Error in selectFields", e );
      return buildErrorResponse(
        "JsonInput.Error.UnableToView.Label",
        "JsonInput.Error.UnableToView.Message"
      );
    }
    return response;
  }

  public JSONObject buildErrorResponse( String labelKey, String messageKey ) {
    JSONObject errorResponse = new JSONObject();
    errorResponse.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    errorResponse.put( "errorLabel", BaseMessages.getString( PKG, labelKey ) );
    errorResponse.put( "errorMessage", BaseMessages.getString( PKG, messageKey ) );
    return errorResponse;
  }
}

