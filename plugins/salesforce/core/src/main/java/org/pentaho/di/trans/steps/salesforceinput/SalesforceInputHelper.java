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

package org.pentaho.di.trans.steps.salesforceinput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SalesforceInputHelper extends SalesforceStepHelper {

  private static final Class<?> PACKAGE = SalesforceInputHelper.class;
  private final SalesforceInputMeta salesforceInputMeta;
  private static final String MODULES = "modules";
  private static final String GET_FIELDS = "getFields";
  private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000'XXX";
  private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  private static final String VALUE_NULL = "null";
  public static final String VALUE_STRING = "string";
  private static final String VALUE_DATETIME = "datetime";
  private static final String VALUE_DATE = "date";
  private static final String VALUE_INT = "int";
  private static final String VALUE_DOUBLE = "double";
  private static final String VALUE_BOOLEAN = "boolean";
  private static final String VALUE_TRUE = "true";
  private static final String VALUE_FALSE = "false";

  public SalesforceInputHelper( SalesforceInputMeta salesforceInputMeta ) {
    super( salesforceInputMeta );
    this.salesforceInputMeta = salesforceInputMeta;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( MODULES ) ) {
      queryParams.put( "moduleFlag", "true" );
    }

    try {
      if ( method.equals( GET_FIELDS ) ) {
        response = getFieldsAction( transMeta );
      } else {
        response = super.handleStepAction( method, transMeta, queryParams );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  public JSONObject getFieldsAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    try {
      FieldsResponse fieldsResponse = getFields( transMeta );
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString( fieldsResponse );
      response.put( "fieldsResponse", objectMapper.readTree( jsonString ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      response.put( StepInterface.STATUS, StepInterface.SUCCESS_STATUS );
    } catch ( Exception e ) {
      response.put( "errorMsg", BaseMessages.getString( PACKAGE, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage" ) );
      StringBuilder details = new StringBuilder();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      e.printStackTrace( pw );
      details.append( sw.getBuffer() );
      response.put( "details", details.toString() );
      log.logError( e.getMessage() );
      response.put( StepInterface.STATUS, StepInterface.FAILURE_STATUS );
    }
    return response;
  }

  public FieldsResponse getFields( TransMeta transMeta ) throws Exception {
    SalesforceConnection connection = null;
    try {
      String realURL = transMeta.environmentSubstitute( salesforceInputMeta.getTargetURL() );
      String realUsername = transMeta.environmentSubstitute( salesforceInputMeta.getUsername() );
      String realPassword = Utils.resolvePassword( transMeta, salesforceInputMeta.getPassword() );
      int realTimeOut = Const.toInt( transMeta.environmentSubstitute( salesforceInputMeta.getTimeout() ), 0 );

      connection = new SalesforceConnection( log, realURL, realUsername, realPassword );
      connection.setTimeOut( realTimeOut );
      FieldsResponse fieldsResponse = new FieldsResponse( new ArrayList<>(), new HashSet<>() );
      if ( salesforceInputMeta.isSpecifyQuery() ) {
        // Free hand SOQL
        String realQuery = transMeta.environmentSubstitute( salesforceInputMeta.getQuery() );
        connection.setSQL( realQuery );
        connection.connect();
        // We are connected, so let's query
        XmlObject[] fields = connection.getElements();
        int nrFields = fields.length;
        for ( int i = 0; i < nrFields; i++ ) {
          fieldsResponse.fieldDTOList.addAll( addFields( "", fieldsResponse.fieldNames, fields[ i ] ) );
        }
      } else {
        connection.connect();

        Field[] fields = connection.getObjectFields( salesforceInputMeta.getModule() );
        for ( int i = 0; i < fields.length; i++ ) {
          Field field = fields[ i ];
          fieldsResponse.fieldNames.add( field.getName() );
          fieldsResponse.fieldDTOList.add( addField( field ) );
        }
      }

      return fieldsResponse;
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( Exception e ) { // Ignore errors
          log.logError( e.getMessage() );
        }
      }
    }
  }

  private boolean isNullIdField( XmlObject field ) {
    return ( Objects.nonNull( field.getName() ) && "ID".equalsIgnoreCase( field.getName().getLocalPart() ) )
        && field.getValue() == null;
  }

  @SuppressWarnings( "java:S1168" ) //The id's value is null in this case. So, do not add empty list to the fields list
  List<FieldDTO> addFields( String prefix, Set<String> fieldNames, XmlObject field ) {
    //Salesforce SOAP Api sends IDs always in the response, even if we don't request it in SOQL query and
    //the id's value is null in this case. So, do not add this Id to the fields list
    if ( isNullIdField( field ) ) {
      return null;
    }
    List<FieldDTO> fieldDTOList = new ArrayList<>();
    String fieldName = ( prefix == null ? "" : prefix ) + field.getName().getLocalPart();
    if ( field instanceof SObject) {
      SObject sobject = (SObject) field;
      for ( XmlObject element : SalesforceConnection.getChildren( sobject ) ) {
        List<FieldDTO> fields = addFields( fieldName + ".", fieldNames, element );
        if ( CollectionUtils.isNotEmpty( fields ) ) {
          fieldDTOList.addAll( fields );
        }
      }
    } else {
      FieldDTO fieldDTO = addField( fieldName, fieldNames, (String) field.getValue() );
      if ( Objects.nonNull( fieldDTO ) ) {
        fieldDTOList.add( fieldDTO );
      }
    }

    return fieldDTOList;
  }

  private FieldDTO addField( Field field ) {
    String fieldType = field.getType().toString();

    String fieldLength = null;
    String fieldPrecision = null;
    if ( !fieldType.equals( VALUE_BOOLEAN ) && !fieldType.equals( VALUE_DATETIME ) && !fieldType.equals(
        VALUE_DATE ) ) {
      fieldLength = Integer.toString( field.getLength() );
      fieldPrecision = Integer.toString( field.getPrecision() );
    }

    FieldDTO fieldDTO = new FieldDTO();
    fieldDTO.setType( field.getType().toString() );
    fieldDTO.setField( field.getName() );
    fieldDTO.setIdlookup( field.isIdLookup() );
    fieldDTO.setLength( fieldLength );
    fieldDTO.setName( field.getLabel() );
    fieldDTO.setPrecision( fieldPrecision );

    return fieldDTO;
  }

  private FieldDTO addField( String fieldName, Set<String> fieldNames, String firstValue ) {
    //no duplicates allowed
    if ( !fieldNames.add( fieldName ) ) {
      return null;
    }

    FieldDTO fieldDTO = new FieldDTO();
    // Try to guess field type
    // I know it's not clean (see up)
    final String fieldType;
    String fieldLength = null;
    if ( Const.NVL( firstValue, VALUE_NULL ).equals( VALUE_NULL ) ) {
      fieldType = VALUE_STRING;
    } else {
      if ( StringUtil.IsDate( firstValue, DEFAULT_DATE_TIME_FORMAT ) ) {
        fieldType = VALUE_DATETIME;
      } else if ( StringUtil.IsDate( firstValue, DEFAULT_DATE_FORMAT ) ) {
        fieldType = VALUE_DATE;
      } else if ( StringUtil.IsInteger( firstValue ) ) {
        fieldType = VALUE_INT;
        fieldLength = Integer.toString( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
      } else if ( StringUtil.IsNumber( firstValue ) ) {
        fieldType = VALUE_DOUBLE;
      } else if ( firstValue.equals( VALUE_TRUE ) || firstValue.equals( VALUE_FALSE ) ) {
        fieldType = VALUE_BOOLEAN;
      } else {
        fieldType = VALUE_STRING;
      }
    }
    fieldDTO.setType( fieldType );
    fieldDTO.setField( fieldName );
    fieldDTO.setIdlookup( false );
    fieldDTO.setLength( fieldLength );
    fieldDTO.setName( fieldName );
    fieldDTO.setPrecision( null );

    return fieldDTO;
  }
}
