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


package org.pentaho.di.trans.steps.salesforceupdate;

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
public class SalesforceUpdate extends SalesforceStep {
  private static Class<?> PKG = SalesforceUpdateMeta.class; // for i18n purposes, needed by Translator2!!

  private SalesforceUpdateMeta meta;
  private SalesforceUpdateData data;

  public SalesforceUpdate( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
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
        throw new KettleException( BaseMessages.getString( PKG,
            "SalesforceUpdateDialog.FieldsMissing.DialogMessage" ) );
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
          throw new KettleException( "Field [" + meta.getUpdateStream()[i]
              + "] couldn't be found in the input stream!" );
        }
      }
    }

    try {
      writeToSalesForce( outputRowData );

    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "SalesforceUpdate.log.Exception" ), e );
    }
    return true;
  }

  @VisibleForTesting
    void writeToSalesForce( Object[] rowData ) throws KettleException {
    try {

      if ( log.isDetailed() ) {
        logDetailed( "Called writeToSalesForce with " + data.iBufferPos + " out of " + meta.getBatchSizeInt() );
      }

      // if there is room in the buffer
      if ( data.iBufferPos < meta.getBatchSizeInt() ) {
        // Reserve for empty fields
        ArrayList<String> fieldsToNull = new ArrayList<String>();
        ArrayList<XmlObject> updatefields = new ArrayList<>();

        // Add fields to update
        for ( int i = 0; i < data.nrfields; i++ ) {
          boolean valueIsNull = data.inputRowMeta.isNull( rowData, data.fieldnrs[i] );
          if ( valueIsNull ) {
            // The value is null
            // We need to keep track of this field
            fieldsToNull.add( SalesforceUtils.getFieldToNullName( log, meta.getUpdateLookup()[i], meta
                .getUseExternalId()[i] ) );
          } else {
            ValueMetaInterface valueMeta = data.inputRowMeta.getValueMeta( data.fieldnrs[i] );
            Object value = rowData[ data.fieldnrs[ i ] ];
            Object normalObject = normalizeValue( valueMeta, value );
            updatefields.add( SalesforceConnection.createMessageElement( meta.getUpdateLookup()[i],
              normalObject, meta.getUseExternalId()[i] ) );
          }
        }

        // build the SObject
        SObject sobjPass = new SObject();
        sobjPass.setType( data.connection.getModule() );
        if ( updatefields.size() > 0 ) {
          for ( XmlObject element : updatefields ) {
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
          logDetailed( "Calling flush buffer from writeToSalesForce" );
        }
        flushBuffers();
      }
    } catch ( Exception e ) {
      throw new KettleException( "\nFailed in writeToSalesForce: " + e.getMessage() );
    }
  }

  private void flushBuffers() throws KettleException {

    try {
      if ( data.sfBuffer.length > data.iBufferPos ) {
        SObject[] smallBuffer = new SObject[data.iBufferPos];
        System.arraycopy( data.sfBuffer, 0, smallBuffer, 0, data.iBufferPos );
        data.sfBuffer = smallBuffer;
      }
      // update the object(s) by sending the array to the web service
      data.saveResult = data.connection.update( data.sfBuffer );
      int nr = data.saveResult.length;
      for ( int j = 0; j < nr; j++ ) {
        if ( data.saveResult[j].isSuccess() ) {
          // Row was updated
          String id = data.saveResult[j].getId();
          if ( log.isDetailed() ) {
            logDetailed( "Row updated with id: " + id );
          }

          // write out the row with the SalesForce ID
          Object[] newRow = RowDataUtil.resizeArray( data.outputBuffer[j], data.outputRowMeta.size() );

          if ( log.isDetailed() ) {
            logDetailed( "The new row has an id value of : " + newRow[0] );
          }

          putRow( data.outputRowMeta, newRow ); // copy row to output rowset(s);
          incrementLinesUpdated();

          if ( checkFeedback( getLinesInput() ) ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "SalesforceUpdate.log.LineRow", "" + getLinesInput() ) );
            }
          }

        } else {
          // there were errors during the create call, go through the
          // errors
          // array and write them to the screen

          if ( !getStepMeta().isDoingErrorHandling() ) {
            if ( log.isDetailed() ) {
              logDetailed( "Found error from SalesForce and raising the exception" );
            }

            // Only send the first error
            //
            com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[0];
            throw new KettleException( BaseMessages.getString( PKG, "SalesforceUpdate.Error.FlushBuffer", new Integer(
                j ), err.getStatusCode(), err.getMessage() ) );
          }

          String errorMessage = "";
          for ( int i = 0; i < data.saveResult[j].getErrors().length; i++ ) {
            // get the next error
            com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[i];
            errorMessage +=
                BaseMessages.getString( PKG, "SalesforceUpdate.Error.FlushBuffer", new Integer( j ), err
                    .getStatusCode(), err.getMessage() );
          }

          // Simply add this row to the error row
          if ( log.isDebug() ) {
            logDebug( "Passing row to error step" );
          }

          putError( getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceUpdate001" );

        }

      }

      // reset the buffers
      data.sfBuffer = new SObject[meta.getBatchSizeInt()];
      data.outputBuffer = new Object[meta.getBatchSizeInt()][];
      data.iBufferPos = 0;

    } catch ( Exception e ) {
      if ( !getStepMeta().isDoingErrorHandling() ) {
        throw new KettleException( "\nFailed to update object, error message was: \n" + e.getMessage() );
      }

      // Simply add this row to the error row
      if ( log.isDebug() ) {
        logDebug( "Passing row to error step" );
      }

      for ( int i = 0; i < data.iBufferPos; i++ ) {
        putError( data.inputRowMeta, data.outputBuffer[i], 1, e.getMessage(), null, "SalesforceUpdate002" );
      }

    } finally {
      if ( data.saveResult != null ) {
        data.saveResult = null;
      }
    }

  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SalesforceUpdateMeta) smi;
    data = (SalesforceUpdateData) sdi;

    if ( super.init( smi, sdi ) ) {

      try {
        // Do we need to rollback all changes on error
        data.connection.setRollbackAllChangesOnError( meta.isRollbackAllChangesOnError() );

        // Now connect ...
        data.connection.connect();

        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "SalesforceUpdate.Log.ErrorOccurredDuringStepInitialize" ) + ke
          .getMessage() );
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

  public String[] getModuleFields() throws Exception {
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
      throw new Exception( e );
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
