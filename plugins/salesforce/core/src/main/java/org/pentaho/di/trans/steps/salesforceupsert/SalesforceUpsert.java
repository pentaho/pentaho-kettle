/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.salesforceupsert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sforce.soap.partner.Field;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;
import org.pentaho.di.trans.steps.salesforceutils.SalesforceUtils;

import com.google.common.annotations.VisibleForTesting;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

import javax.xml.namespace.QName;

/**
 * Read data from Salesforce module, convert them to rows and writes these to one or more output streams.
 *
 * @author jstairs,Samatar
 * @since 10-06-2007
 */
public class SalesforceUpsert extends SalesforceStep {
  private static Class<?> PKG = SalesforceUpsertMeta.class; // for i18n purposes, needed by Translator2!!

  private SalesforceUpsertMeta meta;
  private SalesforceUpsertData data;

  private static final String BOOLEAN = "boolean";
  private static final String STRING = "string";
  private static final String INT = "int";

  public SalesforceUpsert( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    // get one row ... This does some basic initialization of the objects, including loading the info coming in
    Object[] outputRowData = getRow();

    if ( outputRowData == null ) {
      if ( data.iBufferPos > 0 ) {
        flushBuffers();
      }
      setOutputDone();
      return false;
    }

    // If we haven't looked at a row before then do some basic setup.
    if ( first ) {
      first = false;

      data.sfBuffer = new SObject[meta.getBatchSizeInt()];
      data.outputBuffer = new Object[meta.getBatchSizeInt()][];

      // get total fields in the grid
      data.nrfields = meta.getUpdateLookup().length;

      // Check if field list is filled
      if ( data.nrfields == 0 ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "SalesforceUpsertDialog.FieldsMissing.DialogMessage" ) );
      }

      // Create the output row meta-data
      data.inputRowMeta = getInputRowMeta().clone();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Build the mapping of input position to field name
      data.fieldnrs = new int[meta.getUpdateStream().length];
      for ( int i = 0; i < meta.getUpdateStream().length; i++ ) {
        data.fieldnrs[i] = getInputRowMeta().indexOfValue( meta.getUpdateStream()[i] );
        if ( data.fieldnrs[i] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "SalesforceUpsert.FieldNotFound", meta
            .getUpdateStream()[i] ) );
        }
      }
    }

    try {
      writeToSalesForce( outputRowData );
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "SalesforceUpsert.log.Exception" ), e );
    }
    return true;
  }

  @VisibleForTesting
  void writeToSalesForce( Object[] rowData ) throws KettleException {
    try {

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.CalledWrite", data.iBufferPos, meta
          .getBatchSizeInt() ) );
      }
      // if there is room in the buffer
      if ( data.iBufferPos < meta.getBatchSizeInt() ) {
        // Reserve for empty fields
        ArrayList<String> fieldsToNull = new ArrayList<String>();
        ArrayList<XmlObject> upsertfields = new ArrayList<>();

        // Add fields to update
        for ( int i = 0; i < data.nrfields; i++ ) {
          ValueMetaInterface valueMeta = data.inputRowMeta.getValueMeta( data.fieldnrs[i] );
          Object object = rowData[data.fieldnrs[i]];

          if ( valueMeta.isNull( object ) ) {
            // The value is null
            // We need to keep track of this field
            fieldsToNull.add( SalesforceUtils.getFieldToNullName( log, meta.getUpdateLookup()[i], meta
                .getUseExternalId()[i] ) );
          } else {
            Object normalObject = normalizeValue( valueMeta, rowData[data.fieldnrs[i]] );
            if ( data.mapData && data.dataTypeMap != null ) {
              normalObject = mapDataTypes( valueMeta.getType(), meta.getUpdateLookup()[i], normalObject );
            }
            upsertfields.add( SalesforceConnection.createMessageElement( meta.getUpdateLookup()[i], normalObject, meta
                .getUseExternalId()[i] ) );
          }
        }

        // build the SObject
        SObject sobjPass = new SObject();
        sobjPass.setType( data.connection.getModule() );
        if ( upsertfields.size() > 0 ) {
          for ( XmlObject element : upsertfields ) {
            setFieldInSObject( sobjPass, element );
          }
        }
        if ( fieldsToNull.size() > 0 ) {
          // Set Null to fields
          sobjPass.setFieldsToNull( fieldsToNull.toArray( new String[fieldsToNull.size()] ) );
        }
        // Load the buffer array
        data.sfBuffer[data.iBufferPos] = sobjPass;
        data.outputBuffer[data.iBufferPos] = rowData;
        data.iBufferPos++;
      }

      if ( data.iBufferPos >= meta.getBatchSizeInt() ) {
        if ( log.isDetailed() ) {
          logDetailed( "Calling flush buffer from writeToSalesForce" );
        }
        flushBuffers();
      }
    } catch ( KettleException ke ) {
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceUpsert.FailedInWrite", e.toString() ) );
    }
  }

  void setFieldInSObject( SObject sobjPass, XmlObject element ) {
    Iterator<XmlObject> children = element.getChildren();
    String name = element.getName().getLocalPart();
    if ( !children.hasNext() ) {
      sobjPass.setSObjectField( name, element.getValue() );
    } else {
      SObject child = new SObject();
      child.setName( new QName( name ) );
      while ( children.hasNext() ) {
        setFieldInSObject( child, children.next() );
      }
      sobjPass.setSObjectField( name, child );
    }
  }

  private void flushBuffers() throws KettleException {

    try {
      if ( data.sfBuffer.length > data.iBufferPos ) {
        SObject[] smallBuffer = new SObject[data.iBufferPos];
        System.arraycopy( data.sfBuffer, 0, smallBuffer, 0, data.iBufferPos );
        data.sfBuffer = smallBuffer;
      }
      // upsert the object(s) by sending the array to the web service
      data.upsertResult = data.connection.upsert( meta.getUpsertField(), data.sfBuffer );
      int nr = data.upsertResult.length;
      for ( int j = 0; j < nr; j++ ) {
        if ( data.upsertResult[j].isSuccess() ) {
          String id = data.upsertResult[j].getId();
          if ( data.upsertResult[j].isCreated() ) {
            incrementLinesOutput();
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.ObjectCreated", id ) );
            }
          } else {
            incrementLinesUpdated();
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.ObjectUpdated", id ) );
            }
          }
          // write out the row with the SalesForce ID
          Object[] newRow = RowDataUtil.resizeArray( data.outputBuffer[j], data.outputRowMeta.size() );

          if ( data.realSalesforceFieldName != null ) {
            int newIndex = data.inputRowMeta.size();
            newRow[newIndex++] = id;
          }
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.NewRow", newRow[0] ) );
          }

          putRow( data.outputRowMeta, newRow ); // copy row to output rowset(s);

          if ( checkFeedback( getLinesInput() ) ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.log.LineRow", "" + getLinesInput() ) );
            }
          }

        } else {
          // there were errors during the create call, go through the
          // errors
          // array and write them to the screen

          if ( !getStepMeta().isDoingErrorHandling() ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceUpsert.ErrorFound" ) );
            }

            // Only throw the first error
            //
            com.sforce.soap.partner.Error err = data.upsertResult[j].getErrors()[0];
            throw new KettleException( BaseMessages
              .getString( PKG, "SalesforceUpsert.Error.FlushBuffer", new Integer( j ), err.getStatusCode(), err
                .getMessage() ) );
          }

          String errorMessage = "";
          for ( int i = 0; i < data.upsertResult[j].getErrors().length; i++ ) {
            // get the next error
            com.sforce.soap.partner.Error err = data.upsertResult[j].getErrors()[i];
            errorMessage +=
              BaseMessages.getString( PKG, "SalesforceUpsert.Error.FlushBuffer", new Integer( j ), err
                .getStatusCode(), err.getMessage() );
          }

          // Simply add this row to the error row
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "SalesforceUpsert.PassingRowToErrorStep" ) );
          }
          putError( getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceUpsert001" );
        }

      }

      // reset the buffers
      data.sfBuffer = new SObject[meta.getBatchSizeInt()];
      data.outputBuffer = new Object[meta.getBatchSizeInt()][];
      data.iBufferPos = 0;

    } catch ( Exception e ) {
      if ( !getStepMeta().isDoingErrorHandling() ) {
        if ( e instanceof KettleException ) {
          // I know, bad form usually. But I didn't want to duplicate the logic with a catch(KettleException). MB
          throw (KettleException) e;
        } else {
          throw new KettleException(
            BaseMessages.getString( PKG, "SalesforceUpsert.FailedUpsert", e.getMessage() ), e );
        }
      }
      // Simply add this row to the error row
      if ( log.isDebug() ) {
        logDebug( "Passing row to error step" );
      }

      for ( int i = 0; i < data.iBufferPos; i++ ) {
        putError( data.inputRowMeta, data.outputBuffer[i], 1, e.getMessage(), null, "SalesforceUpsert002" );
      }
    } finally {
      if ( data.upsertResult != null ) {
        data.upsertResult = null;
      }
    }

  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SalesforceUpsertMeta) smi;
    data = (SalesforceUpsertData) sdi;

    // For https://jira.pentaho.com/browse/ESR-6833
    data.mapData = "true".equalsIgnoreCase( System.getProperties().getProperty( "MAP_SALESFORCE_UPSERT_DATA_TYPES" ) );

    if ( super.init( smi, sdi ) ) {

      try {
        String salesfoceIdFieldname = environmentSubstitute( meta.getSalesforceIDFieldName() );
        if ( !Utils.isEmpty( salesfoceIdFieldname ) ) {
          data.realSalesforceFieldName = salesfoceIdFieldname;
        }

        // Do we need to rollback all changes on error
        data.connection.setRollbackAllChangesOnError( meta.isRollbackAllChangesOnError() );
        // Now connect ...
        data.connection.connect();
        if ( data.mapData ) { // check if user wants data mapping. If so, get the (fieldName --> dataType) mapping
          Field[] fields = data.connection.getObjectFields( environmentSubstitute( meta.getModule() ) );
          if ( fields != null ) {
            data.dataTypeMap = mapDataTypesToFields( fields );
          }
        }
        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "SalesforceUpsert.Log.ErrorOccurredDuringStepInitialize" )
          + ke.getMessage() );
        return false;
      }
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.outputBuffer != null ) {
      data.outputBuffer = null;
    }
    if ( data.sfBuffer != null ) {
      data.sfBuffer = null;
    }
    super.dispose( smi, sdi );
  }

  /**
   * For https://jira.pentaho.com/browse/ESR-6833. Maps the field names to their data types
   * @param fields - Array of fields to map
   * @return - Map of field names to their data types
   */
  private Map<String, String> mapDataTypesToFields( Field[] fields ) {
    Map<String, String> mappedFields = new HashMap<>();
    for ( Field f : fields ) {
      String fieldType = f.getType().toString();
      if ( "base64".equalsIgnoreCase( fieldType ) || "date".equalsIgnoreCase( fieldType )
        || "datetime".equalsIgnoreCase( fieldType  ) || INT.equalsIgnoreCase( fieldType )
        || "double".equalsIgnoreCase( fieldType ) || BOOLEAN.equalsIgnoreCase( fieldType ) ) {
        mappedFields.put( f.getName(), fieldType );
      } else {
        mappedFields.put( f.getName(), STRING );
      }
    }
    return mappedFields;
  }

  /**
   * For https://jira.pentaho.com/browse/ESR-6833. Attempts to map the data according to PDI version 6.1
   * functionality.
   * @param type - ValueMetaInteface data type.
   * @param name - Salesforce Field name
   * @param value - Data
   * @return - Either original value OR a new data type to upsert to salesforce.
   */
  private Object mapDataTypes( int type, String name, Object value ) {
    try { // if ClassCastException or any other Exception, just return value
      switch ( type ) {
        case ValueMetaInterface.TYPE_INTEGER: // If integer --> string OR if integer --> boolean, convert
          if ( STRING.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            value = String.valueOf( value );
          } else if ( BOOLEAN.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            int i = (int) value;
            if ( i == 0 || i == 1 ) { // Only values 0 or 1 are valid. 0: false, 1: true
              value = i == 1;
            }
          }
          break;
        case ValueMetaInterface.TYPE_BOOLEAN: // If boolean --> string, convert
          if ( STRING.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            value = String.valueOf( value );
          }
          break;
        case ValueMetaInterface.TYPE_NUMBER: // If number --> string OR number --> boolean, convert
          if ( STRING.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            value = String.valueOf( value );
          } else if ( BOOLEAN.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            Double d = (Double) value;
            if ( 0 <= d && d < 2 ) { // 0: false, 1: true
              value = d.intValue() == 1;
            }
          }
          break;
        case ValueMetaInterface.TYPE_STRING: // If string --> integer OR string --> boolean, convert
          if ( INT.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            value = Integer.valueOf( (String) value );
          } else if ( BOOLEAN.equalsIgnoreCase( data.dataTypeMap.get( name ) ) ) {
            if ( "true".equalsIgnoreCase( (String) value ) || "false".equalsIgnoreCase( (String) value ) ) {
              value = Boolean.valueOf( (String) value );
            } else if ( !( (String) value ).startsWith( "-" ) ) {
              Double d = Double.parseDouble( (String) value );
              if ( 0 <= d && d < 2 ) { // 0: false, 1: true
                value = d.intValue() == 1;
              }
            }
          }
          break;
        case ValueMetaInterface.TYPE_DATE:
          break;
        default:
          break;
      }
    } catch ( Exception e ) {
      // do nothing. just return value
    }
    return value;
  }
}
