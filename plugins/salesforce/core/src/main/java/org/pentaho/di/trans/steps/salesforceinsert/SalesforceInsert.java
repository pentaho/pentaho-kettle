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


package org.pentaho.di.trans.steps.salesforceinsert;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.annotations.VisibleForTesting;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.di.trans.steps.salesforceutils.SalesforceUtils;

/**
 * Read data from Salesforce module, convert them to rows and writes these to one or more output streams.
 *
 * @author jstairs,Samatar
 * @since 10-06-2007
 */
public class SalesforceInsert extends SalesforceStep {
  private static Class<?> PKG = SalesforceInsertMeta.class; // for i18n purposes, needed by Translator2!!

  private SalesforceInsertMeta meta;
  private SalesforceInsertData data;

  public SalesforceInsert( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
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
          PKG, "SalesforceInsertDialog.FieldsMissing.DialogMessage" ) );
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
          throw new KettleException( BaseMessages.getString( PKG, "SalesforceInsert.CanNotFindField", meta
            .getUpdateStream()[i] ) );
        }
      }
    }

    try {
      writeToSalesForce( outputRowData );

    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "SalesforceInsert.log.Exception", e ) );
    }
    return true;
  }

  @VisibleForTesting
  void writeToSalesForce( Object[] rowData ) throws KettleException {
    try {

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "SalesforceInsert.WriteToSalesforce", data.iBufferPos, meta
          .getBatchSizeInt() ) );
      }

      // if there is room in the buffer
      if ( data.iBufferPos < meta.getBatchSizeInt() ) {
        ArrayList<XmlObject> insertfields = new ArrayList<>();
        // Reserve for empty fields
        ArrayList<String> fieldsToNull = new ArrayList<String>();

        // Add fields to insert
        for ( int i = 0; i < data.nrfields; i++ ) {
          ValueMetaInterface valueMeta = data.inputRowMeta.getValueMeta( data.fieldnrs[i] );
          Object value = rowData[data.fieldnrs[i]];

          if ( valueMeta.isNull( value ) ) {
            // The value is null
            // We need to keep track of this field
            fieldsToNull.add( SalesforceUtils.getFieldToNullName( log, meta.getUpdateLookup()[i], meta
                .getUseExternalId()[i] ) );
          } else {
            Object normalObject = normalizeValue( valueMeta, value );
            insertfields.add( SalesforceConnection.createMessageElement(
              meta.getUpdateLookup()[i], normalObject, meta.getUseExternalId()[i] ) );
          }
        }

        // build the SObject
        SObject sobjPass = new SObject();
        sobjPass.setType( data.connection.getModule() );

        if ( insertfields.size() > 0 ) {
          for ( XmlObject element : insertfields ) {
            sobjPass.setSObjectField( element.getName().getLocalPart(), element.getValue() );
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
          logDetailed( BaseMessages.getString( PKG, "SalesforceInsert.CallingFlushBuffer" ) );
        }
        flushBuffers();
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInsert.Error", e.getMessage() ) );
    }
  }

  private void flushBuffers() throws KettleException {
    try {
      // create the object(s) by sending the array to the web service
      data.saveResult = data.connection.insert( data.sfBuffer );
      for ( int j = 0; j < data.saveResult.length; j++ ) {
        if ( data.saveResult[j].isSuccess() ) {
          // Row was inserted
          String id = data.saveResult[j].getId();
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "SalesforceInsert.RowInserted", id ) );
          }

          // write out the row with the SalesForce ID
          Object[] newRow = RowDataUtil.resizeArray( data.outputBuffer[j], data.outputRowMeta.size() );

          if ( data.realSalesforceFieldName != null ) {
            int newIndex = getInputRowMeta().size();
            newRow[newIndex++] = id;
          }
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SalesforceInsert.NewRow", newRow[0] ) );
          }

          putRow( data.outputRowMeta, newRow ); // copy row to output rowset(s);
          incrementLinesOutput();

          if ( checkFeedback( getLinesInput() ) ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceInsert.log.LineRow", getLinesInput() ) );
            }
          }

        } else {
          // there were errors during the create call, go through the
          // errors
          // array and write them to the screen

          if ( !getStepMeta().isDoingErrorHandling() ) {

            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString( PKG, "SalesforceInsert.ErrorFound" ) );
            }

            // Only show the first error
            //
            com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[0];
            throw new KettleException( BaseMessages
              .getString( PKG, "SalesforceInsert.Error.FlushBuffer", new Integer( j ), err.getStatusCode(), err
                .getMessage() ) );
          }

          String errorMessage = "";
          for ( int i = 0; i < data.saveResult[j].getErrors().length; i++ ) {
            // get the next error
            com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[i];
            errorMessage +=
              BaseMessages.getString( PKG, "SalesforceInsert.Error.FlushBuffer", new Integer( j ), err
                .getStatusCode(), err.getMessage() );
          }

          // Simply add this row to the error row
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SalesforceInsert.PassingRowToErrorStep" ) );
          }
          putError( getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceInsert001" );

        }

      }

      // reset the buffers
      data.sfBuffer = new SObject[meta.getBatchSizeInt()];
      data.outputBuffer = new Object[meta.getBatchSizeInt()][];
      data.iBufferPos = 0;

    } catch ( Exception e ) {
      if ( !getStepMeta().isDoingErrorHandling() ) {
        throw new KettleException( BaseMessages.getString( PKG, "SalesforceInsert.FailedToInsertObject", e
          .getMessage() ) );
      }
      // Simply add this row to the error row
      if ( log.isDebug() ) {
        logDebug( "Passing row to error step" );
      }

      for ( int i = 0; i < data.iBufferPos; i++ ) {
        putError( data.inputRowMeta, data.outputBuffer[i], 1, e.getMessage(), null, "SalesforceInsert002" );
      }
    } finally {
      if ( data.saveResult != null ) {
        data.saveResult = null;
      }
    }

  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SalesforceInsertMeta) smi;
    data = (SalesforceInsertData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        String salesfoceIdFieldname = environmentSubstitute( meta.getSalesforceIDFieldName() );
        if ( !Utils.isEmpty( salesfoceIdFieldname ) ) {
          data.realSalesforceFieldName = salesfoceIdFieldname;
        }

        // Do we need to rollback all changes on error?
        data.connection.setRollbackAllChangesOnError( meta.isRollbackAllChangesOnError() );

        // Now connect ...
        data.connection.connect();
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "SalesforceInsert.Log.ErrorOccurredDuringStepInitialize" )
          + ke.getMessage() );
        return false;
      }
      return true;
    } else {
      return false;
    }
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

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getModuleFieldsAction() {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    try {
      String[] moduleFields = getModuleFields();
      JSONArray moduleFieldsList = new JSONArray();
      moduleFieldsList.addAll( Arrays.asList( moduleFields ) );
      response.put( "moduleFields", moduleFieldsList );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  public String[] getModuleFields() throws KettleException {

    SalesforceConnection connection = null;
    try {
      SalesforceStepMeta salesforceStepMeta = (SalesforceStepMeta) getStepMetaInterface();
      String realURL = getTransMeta().environmentSubstitute( salesforceStepMeta.getTargetURL() );
      String realUsername = getTransMeta().environmentSubstitute( salesforceStepMeta.getUsername() );
      String realPassword = Utils.resolvePassword( getTransMeta(), salesforceStepMeta.getPassword() );
      int realTimeOut = Const.toInt( getTransMeta().environmentSubstitute( salesforceStepMeta.getTimeout() ), 0 );

      connection = new SalesforceConnection( log, realURL, realUsername, realPassword );
      connection.setTimeOut( realTimeOut );
      connection.connect();

      return connection.getFields( salesforceStepMeta.getModule() );

    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( Exception e ) { /* Ignore */
          logError( e.getMessage() );
        }
      }
    }
  }

}
