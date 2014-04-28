/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.transexecutor;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute a transformation for every input row, set parameters
 *
 * @author Matt
 * @since 18-mar-2013
 */
public class TransExecutor extends BaseStep implements StepInterface {
  private static Class<?> PKG = TransExecutorMeta.class; // for i18n purposes, needed by Translator2!!

  private TransExecutorMeta meta;

  private TransExecutorData data;

  public TransExecutor( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Process a single row. In our case, we send one row of data to a piece of transformation. In the transformation, we
   * look up the MappingInput step to send our rows to it. As a consequence, for the time being, there can only be one
   * MappingInput and one MappingOutput step in the TransExecutor.
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      meta = (TransExecutorMeta) smi;
      data = (TransExecutorData) sdi;

      // Wait for a row...
      //
      Object[] row = getRow();

      if ( row == null ) {
        if ( !data.groupBuffer.isEmpty() ) {
          executeTrans();
        }
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;

        // calculate the various output row layouts first...
        //
        data.inputRowMeta = getInputRowMeta();
        data.executionResultsOutputRowMeta = data.inputRowMeta.clone();
        data.resultRowsOutputRowMeta = data.inputRowMeta.clone();
        data.resultFilesOutputRowMeta = data.inputRowMeta.clone();

        if ( meta.getExecutionResultTargetStepMeta() != null ) {
          meta.getFields( data.executionResultsOutputRowMeta, getStepname(), null, meta
            .getExecutionResultTargetStepMeta(), this, repository, metaStore );
          data.executionResultRowSet = findOutputRowSet( meta.getExecutionResultTargetStepMeta().getName() );
        }
        if ( meta.getResultFilesTargetStepMeta() != null ) {
          meta.getFields(
            data.resultFilesOutputRowMeta, getStepname(), null, meta.getResultFilesTargetStepMeta(), this,
            repository, metaStore );
          data.resultFilesRowSet = findOutputRowSet( meta.getResultFilesTargetStepMeta().getName() );
        }

        // Remember which column to group on, if any...
        //
        data.groupFieldIndex = -1;
        if ( !Const.isEmpty( data.groupField ) ) {
          data.groupFieldIndex = getInputRowMeta().indexOfValue( data.groupField );
          if ( data.groupFieldIndex < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "TransExecutor.Exception.GroupFieldNotFound", data.groupField ) );
          }
          data.groupFieldMeta = getInputRowMeta().getValueMeta( data.groupFieldIndex );
        }
      }

      boolean newGroup = false;
      if ( data.groupSize >= 0 ) {
        // Pass the input rows in blocks to the transformation result rows...
        //
        if ( data.groupSize != 0 ) {
          if ( data.groupBuffer.size() >= data.groupSize ) {
            newGroup = true;
          }
        }
      } else if ( data.groupFieldIndex >= 0 ) {
        Object groupFieldData = row[data.groupFieldIndex];
        if ( data.prevGroupFieldData != null ) {
          if ( data.groupFieldMeta.compare( data.prevGroupFieldData, groupFieldData ) != 0 ) {
            newGroup = true;
          }
        }

        data.prevGroupFieldData = groupFieldData;
      } else if ( data.groupTime > 0 ) {
        long now = System.currentTimeMillis();
        if ( now - data.groupTimeStart >= data.groupTime ) {
          newGroup = true;
        }
      }

      if ( newGroup ) {
        executeTrans();
      }

      data.groupBuffer.add( new RowMetaAndData( getInputRowMeta(), row ) ); // should we clone for safety?

      return true;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TransExecutor.UnexpectedError" ), e );
    }
  }

  private void executeTrans() throws KettleException {

    // If we got 0 rows on input we don't really want to execute the transformation
    //
    if ( data.groupBuffer.isEmpty() ) {
      return;
    }

    data.groupTimeStart = System.currentTimeMillis();

    // Keep the strain on the logging back-end conservative.
    // TODO: make this optional/user-defined later
    //
    if ( data.executorTrans != null ) {
      KettleLogStore.discardLines( data.executorTrans.getLogChannelId(), false );
      LoggingRegistry.getInstance().removeIncludingChildren( data.executorTrans.getLogChannelId() );
    }

    data.executorTrans = new Trans( data.executorTransMeta, this );

    data.executorTrans.setParentTrans( getTrans() );
    data.executorTrans.setLogLevel( getLogLevel() );
    data.executorTrans.setArguments( getTrans().getArguments() );

    if ( meta.getParameters().isInheritingAllVariables() ) {
      data.executorTrans.shareVariablesWith( this );
    }
    data.executorTrans.setInternalKettleVariables( this );
    data.executorTrans.copyParametersFrom( data.executorTransMeta );

    data.executorTrans.setPreview( getTrans().isPreview() );

    data.executorTrans.setServletPrintWriter( getTrans().getServletPrintWriter() );
    data.executorTrans.setServletReponse( getTrans().getServletResponse() );
    data.executorTrans.setServletRequest( getTrans().getServletRequest() );

    // Pass parameter values
    //
    passParametersToTrans();

    // keep track for drill down in Spoon...
    //
    getTrans().getActiveSubtransformations().put( getStepname(), data.executorTrans );

    Result result = new Result();
    result.setRows( data.groupBuffer );

    try {
      data.executorTrans.setPreviousResult( result );
      data.executorTrans.prepareExecution( getTrans().getArguments() );

      // Optionally also stream the rows from a source step to the output of this step
      //
      if ( meta.getOutputRowsSourceStepMeta() != null ) {

        StepInterface stepInterface =
          data.executorTrans.getParentTrans().findRunThread( meta.getOutputRowsSourceStepMeta().getName() );
        stepInterface.addRowListener( new RowAdapter() {
          @Override
          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            TransExecutor.this.putRow( rowMeta, row );
          }
        } );

      }

      // run transformation
      data.executorTrans.startThreads();

      // Inform the parent transformation we started something here...
      //
      for ( DelegationListener delegationListener : getTrans().getDelegationListeners() ) {
        // TODO: copy some settings in the transformation execution configuration, not strictly needed
        // but the execution configuration information is useful in case of a transformation re-start on Carte
        //
        delegationListener.transformationDelegationStarted( data.executorTrans, new TransExecutionConfiguration() );
      }

      // Wait a while until we're done with the transformation
      //
      data.executorTrans.waitUntilFinished();
      result = data.executorTrans.getResult();
    } catch ( KettleException e ) {
      log.logError( "An error occurred executing the transformation: ", e );
      result.setResult( false );
      result.setNrErrors( 1 );
    }

    // First the natural output...
    //
    if ( meta.getExecutionResultTargetStepMeta() != null ) {
      Object[] outputRow = RowDataUtil.allocateRowData( data.executionResultsOutputRowMeta.size() );
      int idx = 0;

      if ( !Const.isEmpty( meta.getExecutionTimeField() ) ) {
        outputRow[idx++] = Long.valueOf( System.currentTimeMillis() - data.groupTimeStart );
      }
      if ( !Const.isEmpty( meta.getExecutionResultField() ) ) {
        outputRow[idx++] = Boolean.valueOf( result.getResult() );
      }
      if ( !Const.isEmpty( meta.getExecutionNrErrorsField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrErrors() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesReadField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesRead() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesWrittenField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesWritten() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesInputField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesInput() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesOutputField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesOutput() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesRejectedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesRejected() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesUpdatedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesUpdated() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesDeletedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesDeleted() );
      }
      if ( !Const.isEmpty( meta.getExecutionFilesRetrievedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrFilesRetrieved() );
      }
      if ( !Const.isEmpty( meta.getExecutionExitStatusField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getExitStatus() );
      }
      if ( !Const.isEmpty( meta.getExecutionLogTextField() ) ) {
        String channelId = data.executorTrans.getLogChannelId();
        String logText = KettleLogStore.getAppender().getBuffer( channelId, false ).toString();
        outputRow[idx++] = logText;
      }
      if ( !Const.isEmpty( meta.getExecutionLogChannelIdField() ) ) {
        outputRow[idx++] = data.executorTrans.getLogChannelId();
      }

      putRowTo( data.executionResultsOutputRowMeta, outputRow, data.executionResultRowSet );
    }

    if ( meta.getResultFilesTargetStepMeta() != null && result.getResultFilesList() != null ) {
      for ( ResultFile resultFile : result.getResultFilesList() ) {
        Object[] targetRow = RowDataUtil.allocateRowData( data.resultFilesOutputRowMeta.size() );
        int idx = 0;
        targetRow[idx++] = resultFile.getFile().getName().toString();

        // TODO: time, origin, ...

        putRowTo( data.resultFilesOutputRowMeta, targetRow, data.resultFilesRowSet );
      }
    }

    data.groupBuffer.clear();
  }

  private void passParametersToTrans() throws KettleException {
    // Set parameters, when fields are used take the first row in the set.
    //
    TransExecutorParameters parameters = meta.getParameters();
    data.executorTrans.clearParameters();

    String[] parameterNames = data.executorTrans.listParameters();
    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      String variable = parameters.getVariable()[i];
      String fieldName = parameters.getField()[i];
      String inputValue = parameters.getInput()[i];
      String value;
      // Take the value from an input row or from a static value?
      //
      if ( !Const.isEmpty( fieldName ) ) {
        int idx = getInputRowMeta().indexOfValue( fieldName );
        if ( idx < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "TransExecutor.Exception.UnableToFindField", fieldName ) );
        }

        value = data.groupBuffer.get( 0 ).getString( idx, "" );
      } else {
        value = environmentSubstitute( inputValue );
      }

      // See if this is a parameter or just a variable...
      //
      if ( Const.indexOfString( variable, parameterNames ) < 0 ) {
        data.executorTrans.setVariable( variable, Const.NVL( value, "" ) );
      } else {
        data.executorTrans.setParameterValue( variable, Const.NVL( value, "" ) );
      }
    }
    data.executorTrans.activateParameters();
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TransExecutorMeta) smi;
    data = (TransExecutorData) sdi;

    if ( super.init( smi, sdi ) ) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        //
        meta.setRepository( getTransMeta().getRepository() );

        data.executorTransMeta =
          TransExecutorMeta.loadTransMeta( meta, meta.getRepository(), meta.getMetaStore(), this );

        // Do we have a transformation at all?
        //
        if ( data.executorTransMeta != null ) {
          data.groupBuffer = new ArrayList<RowMetaAndData>();

          // How many rows do we group together for the transformation?
          //
          data.groupSize = -1;
          if ( !Const.isEmpty( meta.getGroupSize() ) ) {
            data.groupSize = Const.toInt( environmentSubstitute( meta.getGroupSize() ), -1 );
          }

          // Is there a grouping time set?
          //
          data.groupTime = -1;
          if ( !Const.isEmpty( meta.getGroupTime() ) ) {
            data.groupTime = Const.toInt( environmentSubstitute( meta.getGroupTime() ), -1 );
          }
          data.groupTimeStart = System.currentTimeMillis();

          // Is there a grouping field set?
          //
          data.groupField = null;
          if ( !Const.isEmpty( meta.getGroupField() ) ) {
            data.groupField = environmentSubstitute( meta.getGroupField() );
          }

          // That's all for now...
          return true;
        } else {
          logError( "No valid transformation was specified nor loaded!" );
          return false;
        }
      } catch ( Exception e ) {
        logError( "Unable to load the transformation executor because of an error : ", e );
      }

    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    data.groupBuffer = null;

    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    if ( data.executorTrans != null ) {
      data.executorTrans.stopAll();
    }
  }

  public void stopAll() {
    // Stop the transformation execution.
    if ( data.executorTrans != null ) {
      data.executorTrans.stopAll();
    }

    // Also stop this step
    super.stopAll();
  }

  public Trans getExecutorTrans() {
    return data.executorTrans;
  }
}
