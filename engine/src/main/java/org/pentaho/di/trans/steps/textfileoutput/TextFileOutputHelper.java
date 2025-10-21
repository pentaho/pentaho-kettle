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

package org.pentaho.di.trans.steps.textfileoutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileOutputHelper extends BaseStepHelper {
  private static final Class<?> PKG = TextFileOutputMeta.class;
  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";
  private static final String SHOW_FILES = "showFiles";
  private static final String GET_FORMATS = "getFormats";
  private static final String FILES_KEY = "files";

  public TextFileOutputHelper() {
    super();
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case SET_MINIMAL_WIDTH:
          response = setMinimalWidthAction( transMeta, queryParams );
          break;
        case SHOW_FILES:
          response = showFilesAction( transMeta, queryParams );
          break;
        case GET_FORMATS:
          response = getFormatsAction();
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

  @SuppressWarnings( "unchecked" )
  public JSONObject setMinimalWidthAction( TransMeta transMeta, Map<String, String> queryParams )
    throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    String stepName = queryParams.get( "stepName" );
    for ( TextFileOutputFieldDTO textFileOutputFieldDTO : getUpdatedTextFields( transMeta, stepName ) ) {
      textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileOutputFieldDTO ) ) );
    }
    jsonObject.put( "updatedData", textFileFields );
    return jsonObject;
  }

  private List<TextFileOutputFieldDTO> getUpdatedTextFields( TransMeta transMeta, String stepName ) {
    List<TextFileOutputFieldDTO> textFileFields = new ArrayList<>();
    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof TextFileOutputMeta textFileOutputMeta ) {
        for ( TextFileField textFileField : textFileOutputMeta.getOutputFields() ) {
          TextFileOutputFieldDTO updatedTextFileField = new TextFileOutputFieldDTO();
          updatedTextFileField.setName( textFileField.getName() );
          updatedTextFileField.setType( textFileField.getTypeDesc() );
          switch ( textFileField.getType() ) {
            case ValueMetaInterface.TYPE_STRING:
              updatedTextFileField.setFormat( StringUtils.EMPTY );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              updatedTextFileField.setFormat( "0" );
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              updatedTextFileField.setFormat( "0.#####" );
              break;
            default:
              updatedTextFileField.setFormat( StringUtils.EMPTY );
              break;
          }
          updatedTextFileField.setLength( StringUtils.EMPTY );
          updatedTextFileField.setPrecision( StringUtils.EMPTY );
          updatedTextFileField.setCurrency( textFileField.getCurrencySymbol() );
          updatedTextFileField.setDecimal( textFileField.getDecimalSymbol() );
          updatedTextFileField.setGroup( textFileField.getGroupingSymbol() );
          updatedTextFileField.setTrimType( "both" );
          updatedTextFileField.setNullif( textFileField.getNullString() );
          textFileFields.add( updatedTextFileField );
        }
      }
    }
    return textFileFields;
  }


  public JSONObject getFormatsAction() {
    JSONObject response = new JSONObject();
    JSONArray array = new JSONArray();
    for ( String format : TextFileOutputMeta.formatMapperLineTerminator ) {
      // add e.g. TextFileOutputDialog.Format.DOS, .UNIX, .CR, .None
      array.add( BaseMessages.getString( PKG, "TextFileOutputDialog.Format." + format ) );
    }
    response.put( "formats", array );
    return response;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject showFilesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String stepName = queryParams.get( "stepName" );
    String filter = queryParams.get( "filter" );
    String isRegex = queryParams.get( "isRegex" );
    JSONArray filteredFiles = new JSONArray();

    if ( stepName == null || stepName.isEmpty() ) {
      response.put( FILES_KEY, filteredFiles );
      return response;
    }
    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof TextFileOutputMeta textFileOutputMeta ) ) {
      response.put( FILES_KEY, filteredFiles );
      return response;
    }
    for ( String file : textFileOutputMeta.getFiles( transMeta ) ) {
      if ( fileMatchesFilter( file, filter, isRegex ) ) {
        filteredFiles.add( file );
      }
    }
    response.put( FILES_KEY, filteredFiles );
    return response;
  }

  private boolean fileMatchesFilter( String file, String filter, String isRegex ) {
    if ( Boolean.parseBoolean( isRegex ) ) {
      if ( StringUtils.isBlank( filter ) ) {
        return true;
      }
      Matcher matcher = Pattern.compile( filter ).matcher( file );
      return matcher.matches();
    } else {
      return StringUtils.isBlank( filter ) || StringUtils.contains( file.toUpperCase(), filter.toUpperCase() );
    }
  }
}
