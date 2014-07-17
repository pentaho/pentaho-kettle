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
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.TransStepUtil;

/**
 * Execute a transformation for every input row, set parameters
 *
 * @author Matt
 * @since 18-mar-2013
 */
public class TransExecutor extends BaseStep implements StepInterface {
  private static final Class<?> PKG = TransExecutorMeta.class; // for i18n purposes, needed by Translator2!!

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
      setData( (TransExecutorData) sdi );

      // Wait for a row...
      Object[] row = getRow();
      if ( row == null ) {
        executeTransformation();
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;
        initOnFirstProcessingIteration();
      }

      if ( getData().getExecutorStepOutputRowMeta() != null ) {
        putRowTo( getData().getExecutorStepOutputRowMeta(), row, getData().getExecutorStepOutputRowSet() );
      }

      boolean newGroup = false;
      if ( getData().groupSize >= 0 ) {
        // Pass the input rows in blocks to the transformation result rows...
        if ( getData().groupSize != 0 ) {
          if ( getData().groupBuffer.size() >= getData().groupSize ) {
            newGroup = true;
          }
        }
      } else if ( getData().groupFieldIndex >= 0 ) {
        Object groupFieldData = row[getData().groupFieldIndex];
        if ( getData().prevGroupFieldData != null ) {
          if ( getData().groupFieldMeta.compare( getData().prevGroupFieldData, groupFieldData ) != 0 ) {
            newGroup = true;
          }
        }

        getData().prevGroupFieldData = groupFieldData;
      } else if ( getData().groupTime > 0 ) {
        long now = System.currentTimeMillis();
        if ( now - getData().groupTimeStart >= getData().groupTime ) {
          newGroup = true;
        }
      }

      if ( newGroup ) {
        executeTransformation();
      }

      getData().groupBuffer.add( new RowMetaAndData( getInputRowMeta(), row ) ); // should we clone for safety?

      return true;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TransExecutor.UnexpectedError" ), e );
    }
  }


  private void initOnFirstProcessingIteration() throws KettleException {
    // internal transformation's first step has exactly the same input
    getData().setInputRowMeta( getInputRowMeta() );

    // internal transformation's execution results
    getData().setExecutionResultsOutputRowMeta( new RowMeta() );
    if ( meta.getExecutionResultTargetStepMeta() != null ) {
      meta.prepareExecutionResultsFields( getData().getExecutionResultsOutputRowMeta(),
        meta.getExecutionResultTargetStepMeta() );
      getData().setExecutionResultRowSet( findOutputRowSet( meta.getExecutionResultTargetStepMeta().getName() ) );
    }
    // internal transformation's execution result's file
    getData().setResultFilesOutputRowMeta( new RowMeta() );
    if ( meta.getResultFilesTargetStepMeta() != null ) {
      meta.prepareExecutionResultsFileFields( getData().getResultFilesOutputRowMeta(),
        meta.getResultFilesTargetStepMeta() );
      getData().setResultFilesRowSet( findOutputRowSet( meta.getResultFilesTargetStepMeta().getName() ) );
    }
    // internal transformation's execution output
    getData().setResultRowsOutputRowMeta( new RowMeta() );
    if ( meta.getOutputRowsSourceStepMeta() != null ) {
      meta.prepareResultsRowsFields( getData().getResultRowsOutputRowMeta() );
      getData().setResultRowsRowSet( findOutputRowSet( meta.getOutputRowsSourceStepMeta().getName() ) );
    }

    // executor's self output is exactly its input
    if ( meta.getExecutorsOutputStepMeta() != null ) {
      getData().setExecutorStepOutputRowMeta( getInputRowMeta().clone() );
      getData().setExecutorStepOutputRowSet( findOutputRowSet( meta.getExecutorsOutputStepMeta().getName() ) );
    }

    // Remember which column to group on, if any...
    getData().groupFieldIndex = -1;
    if ( !Const.isEmpty( getData().groupField ) ) {
      getData().groupFieldIndex = getInputRowMeta().indexOfValue( getData().groupField );
      if ( getData().groupFieldIndex < 0 ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "TransExecutor.Exception.GroupFieldNotFound", getData().groupField ) );
      }
      getData().groupFieldMeta = getInputRowMeta().getValueMeta( getData().groupFieldIndex );
    }
  }


  private void executeTransformation() throws KettleException {
    // If we got 0 rows on input we don't really want to execute the transformation
    if ( getData().groupBuffer.isEmpty() ) {
      return;
    }

    getData().groupTimeStart = System.currentTimeMillis();

    // Keep the strain on the logging back-end conservative.
    // TODO: make this optional/user-defined later
    Trans executorTrans = getData().getExecutorTrans();
    if ( executorTrans != null ) {
      KettleLogStore.discardLines( executorTrans.getLogChannelId(), false );
      LoggingRegistry.getInstance().removeIncludingChildren( executorTrans.getLogChannelId() );
    }

    executorTrans = createInternalTrans();
    getData().setExecutorTrans( executorTrans );

    // Pass parameter values
    passParametersToTrans();

    // keep track for drill down in Spoon...
    getTrans().getActiveSubtransformations().put( getStepname(), executorTrans );

    Result result = new Result();
    result.setRows( getData().groupBuffer );
    executorTrans.setPreviousResult( result );

    try {
      executorTrans.prepareExecution( getTrans().getArguments() );

      if ( meta.getOutputRowsSourceStepMeta() != null ) {
        List<StepMetaDataCombi> internalTransformationSteps = executorTrans.getSteps();
        StepInterface stepInterface = internalTransformationSteps.get( internalTransformationSteps.size() - 1 ).step;
        stepInterface.addRowListener( new RowAdapter() {
          @Override
          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            putRowTo( rowMeta, row, getData().getResultRowsRowSet() );
          }
        } );
      }

      // run transformation
      executorTrans.startThreads();

      // Inform the parent transformation we started something here...
      for ( DelegationListener delegationListener : getTrans().getDelegationListeners() ) {
        // TODO: copy some settings in the transformation execution configuration, not strictly needed
        // but the execution configuration information is useful in case of a transformation re-start on Carte
        delegationListener.transformationDelegationStarted( executorTrans, new TransExecutionConfiguration() );
      }

      // Wait a while until we're done with the transformation
      executorTrans.waitUntilFinished();

      result = executorTrans.getResult();
    } catch ( KettleException e ) {
      log.logError( "An error occurred executing the transformation: ", e );
      result.setResult( false );
      result.setNrErrors( 1 );
    }

    collectExecutionResults( result );
    collectExecutionResultFiles( result );

    getData().groupBuffer.clear();
  }

  private Trans createInternalTrans() throws KettleException {
    Trans executorTrans = new Trans( getData().getExecutorTransMeta(), this );

    executorTrans.setParentTrans( getTrans() );
    executorTrans.setLogLevel( getLogLevel() );
    executorTrans.setArguments( getTrans().getArguments() );

    if ( meta.getParameters().isInheritingAllVariables() ) {
      executorTrans.shareVariablesWith( this );
    }
    executorTrans.setInternalKettleVariables( this );
    executorTrans.copyParametersFrom( getData().getExecutorTransMeta() );

    executorTrans.setPreview( getTrans().isPreview() );

    TransStepUtil.initServletConfig( getTrans(), executorTrans );

    return executorTrans;
  }

  private void passParametersToTrans() throws KettleException {
    // Set parameters, when fields are used take the first row in the set.
    TransExecutorParameters parameters = meta.getParameters();

    Trans internalTrans = getData().getExecutorTrans();

    internalTrans.clearParameters();

    String[] parameterNames = internalTrans.listParameters();
    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      String variable = parameters.getVariable()[i];
      String fieldName = parameters.getField()[i];
      String inputValue = parameters.getInput()[i];

      String value;
      // Take the value from an input row or from a static value?
      if ( !Const.isEmpty( fieldName ) ) {
        int idx = getInputRowMeta().indexOfValue( fieldName );
        if ( idx < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "TransExecutor.Exception.UnableToFindField", fieldName ) );
        }

        value = getData().groupBuffer.get( 0 ).getString( idx, "" );
      } else {
        value = environmentSubstitute( inputValue );
      }

      // See if this is a parameter or just a variable...
      if ( Const.indexOfString( variable, parameterNames ) < 0 ) {
        internalTrans.setVariable( variable, Const.NVL( value, "" ) );
      } else {
        internalTrans.setParameterValue( variable, Const.NVL( value, "" ) );
      }
    }

    internalTrans.activateParameters();
  }

  private void collectExecutionResults( Result result ) throws KettleException {
    if ( meta.getExecutionResultTargetStepMeta() != null ) {
      Object[] outputRow = RowDataUtil.allocateRowData( getData().getExecutionResultsOutputRowMeta().size() );
      int idx = 0;

      if ( !Const.isEmpty( meta.getExecutionTimeField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( System.currentTimeMillis() - getData().groupTimeStart );
      }
      if ( !Const.isEmpty( meta.getExecutionResultField() ) ) {
        outputRow[ idx++ ] = Boolean.valueOf( result.getResult() );
      }
      if ( !Const.isEmpty( meta.getExecutionNrErrorsField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrErrors() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesReadField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesRead() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesWrittenField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesWritten() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesInputField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesInput() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesOutputField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesOutput() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesRejectedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesRejected() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesUpdatedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesUpdated() );
      }
      if ( !Const.isEmpty( meta.getExecutionLinesDeletedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesDeleted() );
      }
      if ( !Const.isEmpty( meta.getExecutionFilesRetrievedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrFilesRetrieved() );
      }
      if ( !Const.isEmpty( meta.getExecutionExitStatusField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getExitStatus() );
      }
      if ( !Const.isEmpty( meta.getExecutionLogTextField() ) ) {
        String channelId = getData().getExecutorTrans().getLogChannelId();
        String logText = KettleLogStore.getAppender().getBuffer( channelId, false ).toString();
        outputRow[ idx++ ] = logText;
      }
      if ( !Const.isEmpty( meta.getExecutionLogChannelIdField() ) ) {
        outputRow[ idx++ ] = getData().getExecutorTrans().getLogChannelId();
      }

      putRowTo( getData().getExecutionResultsOutputRowMeta(), outputRow, getData().getExecutionResultRowSet() );
    }
  }

  private void collectExecutionResultFiles( Result result ) throws KettleException {
    if ( meta.getResultFilesTargetStepMeta() != null && result.getResultFilesList() != null ) {
      for ( ResultFile resultFile : result.getResultFilesList() ) {
        Object[] targetRow = RowDataUtil.allocateRowData( getData().getResultFilesOutputRowMeta().size() );
        int idx = 0;
        targetRow[ idx++ ] = resultFile.getFile().getName().toString();

        // TODO: time, origin, ...

        putRowTo( getData().getResultFilesOutputRowMeta(), targetRow, getData().getResultFilesRowSet() );
      }
    }
  }


  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TransExecutorMeta) smi;
    setData( (TransExecutorData) sdi );

    if ( super.init( smi, sdi ) ) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        meta.setRepository( getTransMeta().getRepository() );

        getData().setExecutorTransMeta(
          TransExecutorMeta.loadTransMeta( meta, meta.getRepository(), meta.getMetaStore(), this ) );

        // Do we have a transformation at all?
        if ( getData().getExecutorTransMeta() != null ) {

          getData().groupBuffer = new ArrayList<RowMetaAndData>();

          // How many rows do we group together for the transformation?
          if ( !Const.isEmpty( meta.getGroupSize() ) ) {
            getData().groupSize = Const.toInt( environmentSubstitute( meta.getGroupSize() ), -1 );
          } else {
            getData().groupSize = -1;
          }

          // Is there a grouping time set?
          if ( !Const.isEmpty( meta.getGroupTime() ) ) {
            getData().groupTime = Const.toInt( environmentSubstitute( meta.getGroupTime() ), -1 );
          } else {
            getData().groupTime = -1;
          }
          getData().groupTimeStart = System.currentTimeMillis();

          // Is there a grouping field set?
          if ( !Const.isEmpty( meta.getGroupField() ) ) {
            getData().groupField = environmentSubstitute( meta.getGroupField() );
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
    getData().groupBuffer = null;

    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    if ( getData().getExecutorTrans() != null ) {
      getData().getExecutorTrans().stopAll();
    }
  }

  public void stopAll() {
    // Stop the transformation execution.
    if ( getData().getExecutorTrans() != null ) {
      getData().getExecutorTrans().stopAll();
    }

    // Also stop this step
    super.stopAll();
  }

  public Trans getExecutorTrans() {
    return getData().getExecutorTrans();
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  TransExecutorData getData() {
    return data;
  }

  private void setData( TransExecutorData data ) {
    this.data = data;
  }
}
