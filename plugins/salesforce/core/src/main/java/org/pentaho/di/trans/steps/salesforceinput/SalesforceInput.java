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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import mondrian.util.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforce.SalesforceRecordValue;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;

/**
 * Read data from Salesforce module, convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceInput extends SalesforceStep {
  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  private SalesforceInputMeta meta;
  private SalesforceInputData data;

  private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000'XXX";
  private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  private static final String VALUE_NULL = "null";
  private static final String VALUE_STRING = "string";
  private static final String VALUE_DATETIME = "datetime";
  private static final String VALUE_DATE = "date";
  private static final String VALUE_INT = "int";
  private static final String VALUE_DOUBLE = "double";
  private static final String VALUE_BOOLEAN = "boolean";
  private static final String VALUE_TRUE = "true";
  private static final String VALUE_FALSE = "false";

  public SalesforceInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( first ) {
      first = false;

      // Create the output row meta-data
      data.outputRowMeta = new RowMeta();

      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // For String to <type> conversions, we allocate a conversion meta data row as well...
      //
      data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

      // Let's query Salesforce
      data.connection.query( meta.isSpecifyQuery() );

      data.limitReached = true;
      data.recordcount = data.connection.getQueryResultSize();
      if ( data.recordcount > 0 ) {
        data.limitReached = false;
        data.nrRecords = data.connection.getRecordsCount();
      }
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.RecordCount" ) + " : " + data.recordcount );
      }

    }

    Object[] outputRowData = null;

    try {
      // get one row ...
      outputRowData = getOneRow();

      if ( outputRowData == null ) {
        setOutputDone();
        return false;
      }

      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesInput() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SalesforceInput.log.LineRow", "" + getLinesInput() ) );
        }
      }

      data.rownr++;
      data.recordIndex++;

      return true;
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "SalesforceInput.log.Exception", e.getMessage() ) );
        logError( Const.getStackTracker( e ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), outputRowData, 1, errorMessage, null, "SalesforceInput001" );
      }
    }
    return true;
  }

  private Object[] getOneRow() throws KettleException {
    if ( data.limitReached || data.rownr >= data.recordcount ) {
      return null;
    }

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    try {

      // check for limit rows
      if ( data.limit > 0 && data.rownr >= data.limit ) {
        // User specified limit and we reached it
        // We end here
        data.limitReached = true;
        return null;
      } else {
        if ( data.rownr >= data.nrRecords || data.finishedRecord ) {
          if ( meta.getRecordsFilter() != SalesforceConnectionUtils.RECORDS_FILTER_UPDATED ) {
            // We retrieved all records available here
            // maybe we need to query more again ...
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.NeedQueryMore", "" + data.rownr ) );
            }

            if ( data.connection.queryMore() ) {
              // We returned more result (query is not done yet)
              int nr = data.connection.getRecordsCount();
              data.nrRecords += nr;
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.QueryMoreRetrieved", "" + nr ) );
              }

              // We need here to initialize recordIndex
              data.recordIndex = 0;

              data.finishedRecord = false;
            } else {
              // Query is done .. we finished !
              return null;
            }
          }
        }
      }

      // Return a record
      SalesforceRecordValue srvalue = data.connection.getRecord( data.recordIndex );
      data.finishedRecord = srvalue.isAllRecordsProcessed();

      if ( meta.getRecordsFilter() == SalesforceConnectionUtils.RECORDS_FILTER_DELETED ) {
        if ( srvalue.isRecordIndexChanges() ) {
          // We have moved forward...
          data.recordIndex = srvalue.getRecordIndex();
        }
        if ( data.finishedRecord && srvalue.getRecordValue() == null ) {
          // We processed all records
          return null;
        }
      }
      for ( int i = 0; i < data.nrfields; i++ ) {
        String value =
          data.connection.getRecordValue( srvalue.getRecordValue(), meta.getInputFields()[i].getField() );

        // DO Trimming!
        switch ( meta.getInputFields()[i].getTrimType() ) {
          case SalesforceInputField.TYPE_TRIM_LEFT:
            value = Const.ltrim( value );
            break;
          case SalesforceInputField.TYPE_TRIM_RIGHT:
            value = Const.rtrim( value );
            break;
          case SalesforceInputField.TYPE_TRIM_BOTH:
            value = Const.trim( value );
            break;
          default:
            break;
        }

        doConversions( outputRowData, i, value );

        // Do we need to repeat this field if it is null?
        if ( meta.getInputFields()[i].isRepeated() ) {
          if ( data.previousRow != null && Utils.isEmpty( value ) ) {
            outputRowData[i] = data.previousRow[i];
          }
        }

      } // End of loop over fields...

      int rowIndex = data.nrfields;

      // See if we need to add the url to the row...
      if ( meta.includeTargetURL() && !Utils.isEmpty( meta.getTargetURLField() ) ) {
        outputRowData[rowIndex++] = data.connection.getURL();
      }

      // See if we need to add the module to the row...
      if ( meta.includeModule() && !Utils.isEmpty( meta.getModuleField() ) ) {
        outputRowData[rowIndex++] = data.connection.getModule();
      }

      // See if we need to add the generated SQL to the row...
      if ( meta.includeSQL() && !Utils.isEmpty( meta.getSQLField() ) ) {
        outputRowData[rowIndex++] = data.connection.getSQL();
      }

      // See if we need to add the server timestamp to the row...
      if ( meta.includeTimestamp() && !Utils.isEmpty( meta.getTimestampField() ) ) {
        outputRowData[rowIndex++] = data.connection.getServerTimestamp();
      }

      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
        outputRowData[rowIndex++] = new Long( data.rownr );
      }

      if ( meta.includeDeletionDate() && !Utils.isEmpty( meta.getDeletionDateField() ) ) {
        outputRowData[rowIndex++] = srvalue.getDeletionDate();
      }

      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "SalesforceInput.Exception.CanNotReadFromSalesforce" ), e );
    }

    return outputRowData;
  }

  // DO CONVERSIONS...
  void doConversions( Object[] outputRowData, int i, String value ) throws KettleValueException {
    ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( i );
    ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( i );

    if ( ValueMetaInterface.TYPE_BINARY != targetValueMeta.getType() ) {
      outputRowData[i] = targetValueMeta.convertData( sourceValueMeta, value );
    } else {
      // binary type of salesforce requires specific conversion
      if ( value != null ) {
        outputRowData[ i ] = Base64.decode( value );
      } else {
        outputRowData[ i ] = null;
      }
    }
  }

  /*
   * build the SQL statement to send to Salesforce
   */
  private String BuiltSOQl() {
    String sql = "";
    SalesforceInputField[] fields = meta.getInputFields();

    switch ( meta.getRecordsFilter() ) {
      case SalesforceConnectionUtils.RECORDS_FILTER_UPDATED:
        for ( int i = 0; i < data.nrfields; i++ ) {
          SalesforceInputField field = fields[i];
          sql += environmentSubstitute( field.getField() );
          if ( i < data.nrfields - 1 ) {
            sql += ",";
          }
        }
        break;
      case SalesforceConnectionUtils.RECORDS_FILTER_DELETED:
        sql += "SELECT ";
        for ( int i = 0; i < data.nrfields; i++ ) {
          SalesforceInputField field = fields[i];
          sql += environmentSubstitute( field.getField() );
          if ( i < data.nrfields - 1 ) {
            sql += ",";
          }
        }
        sql += " FROM " + environmentSubstitute( meta.getModule() ) + " WHERE isDeleted = true";
        break;
      default:
        sql += "SELECT ";
        for ( int i = 0; i < data.nrfields; i++ ) {
          SalesforceInputField field = fields[i];
          sql += environmentSubstitute( field.getField() );
          if ( i < data.nrfields - 1 ) {
            sql += ",";
          }
        }
        sql = sql + " FROM " + environmentSubstitute( meta.getModule() );
        if ( !Utils.isEmpty( environmentSubstitute( meta.getCondition() ) ) ) {
          sql += " WHERE " + environmentSubstitute( meta.getCondition().replace( "\n\r", "" ).replace( "\n", "" ) );
        }
        break;
    }

    return sql;
  }

  /**
   * Build an empty row based on the meta-data.
   *
   * @return
   */
  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    return rowData;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SalesforceInputMeta) smi;
    data = (SalesforceInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      // get total fields in the grid
      data.nrfields = meta.getInputFields().length;

      // Check if field list is filled
      if ( data.nrfields == 0 ) {
        log.logError( BaseMessages.getString( PKG, "SalesforceInputDialog.FieldsMissing.DialogMessage" ) );
        return false;
      }

      String soSQL = environmentSubstitute( meta.getQuery() );
      try {

        if ( meta.isSpecifyQuery() ) {
          // Check if user specified a query
          if ( Utils.isEmpty( soSQL ) ) {
            log.logError( BaseMessages.getString( PKG, "SalesforceInputDialog.QueryMissing.DialogMessage" ) );
            return false;
          }
        } else {
          // check records filter
          if ( meta.getRecordsFilter() != SalesforceConnectionUtils.RECORDS_FILTER_ALL ) {
            String realFromDateString = environmentSubstitute( meta.getReadFrom() );
            if ( Utils.isEmpty( realFromDateString ) ) {
              log.logError( BaseMessages.getString( PKG, "SalesforceInputDialog.FromDateMissing.DialogMessage" ) );
              return false;
            }
            String realToDateString = environmentSubstitute( meta.getReadTo() );
            if ( Utils.isEmpty( realToDateString ) ) {
              log.logError( BaseMessages.getString( PKG, "SalesforceInputDialog.ToDateMissing.DialogMessage" ) );
              return false;
            }
            try {
              SimpleDateFormat dateFormat = new SimpleDateFormat( SalesforceInputMeta.DATE_TIME_FORMAT );
              data.startCal = new GregorianCalendar();
              data.startCal.setTime( dateFormat.parse( realFromDateString ) );
              data.endCal = new GregorianCalendar();
              data.endCal.setTime( dateFormat.parse( realToDateString ) );
              dateFormat = null;
            } catch ( Exception e ) {
              log.logError( BaseMessages.getString( PKG, "SalesforceInput.ErrorParsingDate" ), e );
              return false;
            }
          }
        }

        data.limit = Const.toLong( environmentSubstitute( meta.getRowLimit() ), 0 );

        // Do we have to query for all records included deleted records
        data.connection.setQueryAll( meta.isQueryAll() );

        // Build query if needed
        if ( meta.isSpecifyQuery() ) {
          // Free hand SOQL Query
          data.connection.setSQL( soSQL.replace( "\n\r", " " ).replace( "\n", " " ) );
        } else {
          // Set calendars for update or deleted records
          if ( meta.getRecordsFilter() != SalesforceConnectionUtils.RECORDS_FILTER_ALL ) {
            data.connection.setCalendar( meta.getRecordsFilter(), data.startCal, data.endCal );
          }

          if ( meta.getRecordsFilter() == SalesforceConnectionUtils.RECORDS_FILTER_UPDATED ) {
            // Return fields list
            data.connection.setFieldsList( BuiltSOQl() );
          } else {
            // Build now SOQL
            data.connection.setSQL( BuiltSOQl() );
          }
        }

        // Now connect ...
        data.connection.connect();

        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "SalesforceInput.Log.ErrorOccurredDuringStepInitialize" )
          + ke.getMessage() );
        return false;
      }
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.outputRowMeta != null ) {
      data.outputRowMeta = null;
    }
    if ( data.convertRowMeta != null ) {
      data.convertRowMeta = null;
    }
    if ( data.previousRow != null ) {
      data.previousRow = null;
    }
    if ( data.startCal != null ) {
      data.startCal = null;
    }
    if ( data.endCal != null ) {
      data.endCal = null;
    }
    super.dispose( smi, sdi );
  }

  @SuppressWarnings( "java:S1185" ) //This is being called using reflection(doAction)
  @Override
  public JSONObject testButtonAction( Map<String, String> queryParams ) {
    return super.testButtonAction( queryParams );
  }

  @Override
  public JSONObject modulesAction( Map<String, String> queryParams ) {
    queryParams.put( "moduleFlag", "true" );
    return super.modulesAction( queryParams );
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFieldsAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    try {
      FieldsResponse fieldsResponse = getFields();
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString( fieldsResponse );
      response.put( "fieldsResponse", objectMapper.readTree( jsonString ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      response.put( StepInterface.STATUS, StepInterface.SUCCESS_STATUS );
    } catch ( Exception e ) {
      response.put( "errorMsg", BaseMessages.getString( PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage" ) );
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

  public FieldsResponse getFields() throws Exception {
    SalesforceConnection connection = null;
    try {
      meta = (SalesforceInputMeta) getStepMetaInterface();
      String realURL = getTransMeta().environmentSubstitute( meta.getTargetURL() );
      String realUsername = getTransMeta().environmentSubstitute( meta.getUsername() );
      String realPassword = Utils.resolvePassword( getTransMeta(), meta.getPassword() );
      int realTimeOut = Const.toInt( getTransMeta().environmentSubstitute( meta.getTimeout() ), 0 );

      connection = new SalesforceConnection( log, realURL, realUsername, realPassword );
      connection.setTimeOut( realTimeOut );
      FieldsResponse fieldsResponse = new FieldsResponse( new ArrayList<>(), new HashSet<>() );
      if ( meta.isSpecifyQuery() ) {
        // Free hand SOQL
        String realQuery = getTransMeta().environmentSubstitute( meta.getQuery() );
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

        Field[] fields = connection.getObjectFields( meta.getModule() );
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
          logError( e.getMessage() );
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
    if ( field instanceof SObject ) {
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
    String fieldPrecision = null;
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
    fieldDTO.setPrecision( fieldPrecision );

    return fieldDTO;
  }

}
