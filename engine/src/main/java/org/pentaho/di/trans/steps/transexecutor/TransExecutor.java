/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.TransStepUtil;

/**
 * Execute a transformation for every input row, set parameters.
 * <p>
 *     <b>Note:</b><br/>
 *     Be aware, logic of the classes methods is very similar to corresponding methods of
 *     {@link org.pentaho.di.trans.steps.jobexecutor.JobExecutor JobExecutor}.
 *     If you change something in this class, consider copying your changes to JobExecutor as well.
 * </p>
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
      TransExecutorData transExecutorData = getData();
      // Wait for a row...
      Object[] row = getRow();

      if ( row == null ) {
        executeTransformation( null );
        setOutputDone();
        return false;
      }

      List<String> incomingFieldValues = new ArrayList<String>();
      if (  getInputRowMeta() != null ) {
        for ( int i = 0; i < getInputRowMeta().size(); i++ ) {
          String fieldvalue = getInputRowMeta().getString( row, i );
          incomingFieldValues.add( fieldvalue );
        }
      }

      if ( first ) {
        first = false;
        initOnFirstProcessingIteration();
      }

      RowSet executorStepOutputRowSet = transExecutorData.getExecutorStepOutputRowSet();
      if ( transExecutorData.getExecutorStepOutputRowMeta() != null && executorStepOutputRowSet != null ) {
        putRowTo( transExecutorData.getExecutorStepOutputRowMeta(), row, executorStepOutputRowSet );
      }

      // Grouping by field and execution time works ONLY if grouping by size is disabled.
      if ( transExecutorData.groupSize < 0 ) {
        if ( transExecutorData.groupFieldIndex >= 0 ) { // grouping by field
          Object groupFieldData = row[ transExecutorData.groupFieldIndex ];
          if ( transExecutorData.prevGroupFieldData != null ) {
            if ( transExecutorData.groupFieldMeta.compare( transExecutorData.prevGroupFieldData, groupFieldData ) != 0 ) {
              executeTransformation( getLastIncomingFieldValues() );
            }
          }
          transExecutorData.prevGroupFieldData = groupFieldData;
        } else if ( transExecutorData.groupTime > 0 ) { // grouping by execution time
          long now = System.currentTimeMillis();
          if ( now - transExecutorData.groupTimeStart >= transExecutorData.groupTime ) {
            executeTransformation( incomingFieldValues );
          }
        }
      }

      // Add next value AFTER transformation execution, in case we are grouping by field (see PDI-14958),
      // and BEFORE checking size of a group, in case we are grouping by size (see PDI-14121).
      transExecutorData.groupBuffer.add( new RowMetaAndData( getInputRowMeta(), row ) ); // should we clone for safety?

      // Grouping by size.
      // If group buffer size exceeds specified limit, then execute transformation and flush group buffer.
      if ( transExecutorData.groupSize > 0 ) {
        if ( transExecutorData.groupBuffer.size() >= transExecutorData.groupSize ) {
          executeTransformation( incomingFieldValues );
        }
      }

      return true;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TransExecutor.UnexpectedError" ), e );
    }
  }

  private void initOnFirstProcessingIteration() throws KettleException {
    TransExecutorData transExecutorData = getData();
    // internal transformation's first step has exactly the same input
    transExecutorData.setInputRowMeta( getInputRowMeta() );

    // internal transformation's execution results
    transExecutorData.setExecutionResultsOutputRowMeta( new RowMeta() );
    if ( meta.getExecutionResultTargetStepMeta() != null ) {
      meta.prepareExecutionResultsFields( transExecutorData.getExecutionResultsOutputRowMeta(),
        meta.getExecutionResultTargetStepMeta() );
      transExecutorData
        .setExecutionResultRowSet( findOutputRowSet( meta.getExecutionResultTargetStepMeta().getName() ) );
    }
    // internal transformation's execution result's file
    transExecutorData.setResultFilesOutputRowMeta( new RowMeta() );
    if ( meta.getResultFilesTargetStepMeta() != null ) {
      meta.prepareExecutionResultsFileFields( transExecutorData.getResultFilesOutputRowMeta(),
        meta.getResultFilesTargetStepMeta() );
      transExecutorData.setResultFilesRowSet( findOutputRowSet( meta.getResultFilesTargetStepMeta().getName() ) );
    }
    // internal transformation's execution output
    transExecutorData.setResultRowsOutputRowMeta( new RowMeta() );
    if ( meta.getOutputRowsSourceStepMeta() != null ) {
      meta.prepareResultsRowsFields( transExecutorData.getResultRowsOutputRowMeta() );
      transExecutorData.setResultRowsRowSet( findOutputRowSet( meta.getOutputRowsSourceStepMeta().getName() ) );
    }

    // executor's self output is exactly its input
    if ( meta.getExecutorsOutputStepMeta() != null ) {
      transExecutorData.setExecutorStepOutputRowMeta( getInputRowMeta().clone() );
      transExecutorData.setExecutorStepOutputRowSet( findOutputRowSet( meta.getExecutorsOutputStepMeta().getName() ) );
    }

    // Remember which column to group on, if any...
    transExecutorData.groupFieldIndex = -1;
    if ( !Utils.isEmpty( transExecutorData.groupField ) ) {
      transExecutorData.groupFieldIndex = getInputRowMeta().indexOfValue( transExecutorData.groupField );
      if ( transExecutorData.groupFieldIndex < 0 ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "TransExecutor.Exception.GroupFieldNotFound", transExecutorData.groupField ) );
      }
      transExecutorData.groupFieldMeta = getInputRowMeta().getValueMeta( transExecutorData.groupFieldIndex );
    }
  }

  private void executeTransformation( List<String> incomingFieldValues ) throws KettleException {
    TransExecutorData transExecutorData = getData();
    // If we got 0 rows on input we don't really want to execute the transformation
    if ( transExecutorData.groupBuffer.isEmpty() ) {
      return;
    }
    transExecutorData.groupTimeStart = System.currentTimeMillis();

    if ( first ) {
      discardLogLines( transExecutorData );
    }

    Trans executorTrans = createInternalTrans();
    transExecutorData.setExecutorTrans( executorTrans );
    if ( incomingFieldValues != null ) {
      // Pass parameter values
      passParametersToTrans( incomingFieldValues );
    } else {
      List<String> lastIncomingFieldValues = getLastIncomingFieldValues();
      // incomingFieldValues == null-  There are no more rows - Last Case - pass previous values if exists
      // If not still pass the null parameter values
      passParametersToTrans( lastIncomingFieldValues != null && !lastIncomingFieldValues.isEmpty() ? lastIncomingFieldValues : incomingFieldValues );
    }


    // keep track for drill down in Spoon...
    getTrans().addActiveSubTransformation( getStepname(), executorTrans );

    Result result = new Result();
    result.setRows( transExecutorData.groupBuffer );
    executorTrans.setPreviousResult( result );

    try {
      executorTrans.prepareExecution( getTrans().getArguments() );

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

    if ( result.isSafeStop() ) {
      getTrans().safeStop();
    }

    collectTransResults( result );
    collectExecutionResults( result );
    collectExecutionResultFiles( result );

    transExecutorData.groupBuffer.clear();
  }

  @VisibleForTesting
  void discardLogLines( TransExecutorData transExecutorData ) {
    // Keep the strain on the logging back-end conservative.
    // TODO: make this optional/user-defined later
    Trans executorTrans = transExecutorData.getExecutorTrans();
    if ( executorTrans != null ) {
      KettleLogStore.discardLines( executorTrans.getLogChannelId(), false );
    }
  }

  @VisibleForTesting
  Trans createInternalTrans() throws KettleException {
    Trans executorTrans = new Trans( getData().getExecutorTransMeta(), this );

    executorTrans.setParentTrans( getTrans() );
    executorTrans.setRepository( getTrans().getRepository() );
    executorTrans.setLogLevel( getLogLevel() );
    executorTrans.setArguments( getTrans().getArguments() );

    executorTrans.setInternalKettleVariables( this );

    executorTrans.setPreview( getTrans().isPreview() );

    TransStepUtil.initServletConfig( getTrans(), executorTrans );

    return executorTrans;
  }

  @VisibleForTesting
  void passParametersToTrans( List<String> incomingFieldValues ) throws KettleException {
    //The values of the incoming fields from the previous step.
    if ( incomingFieldValues == null ) {
      incomingFieldValues = new ArrayList<String>();
    }

    // Set parameters, when fields are used take the first row in the set.
    TransExecutorParameters parameters = meta.getParameters();

    // A map where the final parameters and values are stored.
    Map<String, String> resolvingValuesMap = new LinkedHashMap<String, String>();
    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      resolvingValuesMap.put( parameters.getVariable()[i], null );
    }

    //The names of the "Fields to use".
    List<String> fieldsToUse = new ArrayList<String>();
    if ( parameters.getField() != null ) {
      fieldsToUse = Arrays.asList( parameters.getField() );
    }

    //The names of the incoming fields from the previous step.
    List<String> incomingFields = new ArrayList<String>();
    if ( data.getInputRowMeta() != null ) {
      incomingFields = Arrays.asList( data.getInputRowMeta().getFieldNames() );
    }

    //The values of the "Static input value".
    List<String> staticInputs = Arrays.asList( parameters.getInput() );

    /////////////////////////////////////////////
    // For all parameters declared in transExecutor
    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      String currentVariableToUpdate = (String) resolvingValuesMap.keySet().toArray()[i];
      boolean hasIncomingFieldValues = incomingFieldValues != null && !incomingFieldValues.isEmpty();
      try {
        if ( i < fieldsToUse.size() && incomingFields.contains( fieldsToUse.get( i ) ) && hasIncomingFieldValues
          && ( !Utils.isEmpty( Const.trim( incomingFieldValues.get( incomingFields.indexOf( fieldsToUse.get( i ) ) ) ) ) ) ) {
          // if field to use is defined on previous steps ( incomingFields ) and is not empty - put that value
          resolvingValuesMap.put( currentVariableToUpdate, incomingFieldValues.get( incomingFields.indexOf( fieldsToUse.get( i ) ) ) );
        } else {
          if ( i < staticInputs.size() && !Utils.isEmpty( Const.trim( staticInputs.get( i ) ) ) ) {
            // if we do not have a field to use then check for static input values - if not empty - put that value
            resolvingValuesMap.put( currentVariableToUpdate, staticInputs.get( i ) );
          } else {
            if ( !Utils.isEmpty( Const.trim( fieldsToUse.get( i ) ) ) ) {
              // if both -field to use- and -static values- are empty, then check if it is in fact an empty field cell
              // if not an empty cell then it is a declared variable that was resolved as null by previous steps
              // put "" value ( not null) and also set transExecutor variable - to force create this variable
              resolvingValuesMap.put( currentVariableToUpdate, "" );
              this.setVariable( parameters.getVariable()[i], resolvingValuesMap.get( parameters.getVariable()[i] ) );
            } else {
              if ( !Utils.isEmpty( Const.trim( this.getVariable( parameters.getVariable()[i] ) ) ) && meta.getParameters().isInheritingAllVariables() ) {
                // if everything is empty, then check for last option - parent variables if isInheriting is checked - if exists - put that value
                resolvingValuesMap.put( currentVariableToUpdate, this.getVariable( parameters.getVariable()[i] ) );
              } else {
                // last case - if no variables defined - put "" value ( not null)
                // and also set transExecutor variable - to force create this variable
                resolvingValuesMap.put( currentVariableToUpdate, "" );
                this.setVariable( parameters.getVariable()[i], resolvingValuesMap.get( parameters.getVariable()[i] ) );
              }
            }
          }
        }
      } catch ( Exception e ) {
        //Set the value to the first parameter in the resolvingValuesMap.
        resolvingValuesMap.put( (String) resolvingValuesMap.keySet().toArray()[i], "" );
        this.setVariable( parameters.getVariable()[i], resolvingValuesMap.get( parameters.getVariable()[i] ) );
      }
    }
    /////////////////////////////////////////////

    //Transform the values of the resolvingValuesMap into a String array "inputFieldValues" to be passed as parameter..
    String[] inputFieldValues = new String[parameters.getVariable().length];
    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      inputFieldValues[i] = resolvingValuesMap.get( parameters.getVariable()[i] );
    }

    Trans trans = getExecutorTrans();
    initializeVariablesFromParent( trans );

    StepWithMappingMeta
        .activateParams( trans, trans, this, trans.listParameters(), parameters.getVariable(), inputFieldValues, meta.getParameters().isInheritingAllVariables() );
  }

  @VisibleForTesting
  void collectTransResults( Result result ) throws KettleException {
    RowSet transResultsRowSet = getData().getResultRowsRowSet();
    if ( meta.getOutputRowsSourceStepMeta() != null && transResultsRowSet != null ) {
      for ( RowMetaAndData metaAndData : result.getRows() ) {
        putRowTo( metaAndData.getRowMeta(), metaAndData.getData(), transResultsRowSet );
      }
    }
  }

  @VisibleForTesting
  void collectExecutionResults( Result result ) throws KettleException {
    RowSet executionResultsRowSet = getData().getExecutionResultRowSet();
    if ( meta.getExecutionResultTargetStepMeta() != null && executionResultsRowSet != null ) {
      Object[] outputRow = RowDataUtil.allocateRowData( getData().getExecutionResultsOutputRowMeta().size() );
      int idx = 0;

      if ( !Utils.isEmpty( meta.getExecutionTimeField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( System.currentTimeMillis() - getData().groupTimeStart );
      }
      if ( !Utils.isEmpty( meta.getExecutionResultField() ) ) {
        outputRow[ idx++ ] = Boolean.valueOf( result.getResult() );
      }
      if ( !Utils.isEmpty( meta.getExecutionNrErrorsField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrErrors() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesReadField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesRead() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesWrittenField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesWritten() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesInputField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesInput() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesOutputField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesOutput() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesRejectedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesRejected() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesUpdatedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesUpdated() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesDeletedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrLinesDeleted() );
      }
      if ( !Utils.isEmpty( meta.getExecutionFilesRetrievedField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getNrFilesRetrieved() );
      }
      if ( !Utils.isEmpty( meta.getExecutionExitStatusField() ) ) {
        outputRow[ idx++ ] = Long.valueOf( result.getExitStatus() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLogTextField() ) ) {
        String channelId = getData().getExecutorTrans().getLogChannelId();
        String logText = KettleLogStore.getAppender().getBuffer( channelId, false ).toString();
        outputRow[ idx++ ] = logText;
      }
      if ( !Utils.isEmpty( meta.getExecutionLogChannelIdField() ) ) {
        outputRow[ idx++ ] = getData().getExecutorTrans().getLogChannelId();
      }

      putRowTo( getData().getExecutionResultsOutputRowMeta(), outputRow, executionResultsRowSet );
    }
  }

  @VisibleForTesting
  void collectExecutionResultFiles( Result result ) throws KettleException {
    RowSet resultFilesRowSet = getData().getResultFilesRowSet();
    if ( meta.getResultFilesTargetStepMeta() != null && result.getResultFilesList() != null && resultFilesRowSet != null ) {
      for ( ResultFile resultFile : result.getResultFilesList() ) {
        Object[] targetRow = RowDataUtil.allocateRowData( getData().getResultFilesOutputRowMeta().size() );
        int idx = 0;
        targetRow[ idx++ ] = resultFile.getFile().getName().toString();

        // TODO: time, origin, ...

        putRowTo( getData().getResultFilesOutputRowMeta(), targetRow, resultFilesRowSet );
      }
    }
  }


  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TransExecutorMeta) smi;
    setData( (TransExecutorData) sdi );
    TransExecutorData transExecutorData = getData();
    if ( super.init( smi, sdi ) ) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        meta.setRepository( getTransMeta().getRepository() );

        transExecutorData.setExecutorTransMeta( loadExecutorTransMeta() );

        // Do we have a transformation at all?
        if ( transExecutorData.getExecutorTransMeta() != null ) {
          transExecutorData.groupBuffer = new ArrayList<RowMetaAndData>();

          // How many rows do we group together for the transformation?
          if ( !Utils.isEmpty( meta.getGroupSize() ) ) {
            transExecutorData.groupSize = Const.toInt( environmentSubstitute( meta.getGroupSize() ), -1 );
          } else {
            transExecutorData.groupSize = -1;
          }
          // Is there a grouping time set?
          if ( !Utils.isEmpty( meta.getGroupTime() ) ) {
            transExecutorData.groupTime = Const.toInt( environmentSubstitute( meta.getGroupTime() ), -1 );
          } else {
            transExecutorData.groupTime = -1;
          }
          transExecutorData.groupTimeStart = System.currentTimeMillis();

          // Is there a grouping field set?
          if ( !Utils.isEmpty( meta.getGroupField() ) ) {
            transExecutorData.groupField = environmentSubstitute( meta.getGroupField() );
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

  @VisibleForTesting
  TransMeta loadExecutorTransMeta() throws KettleException {
    return TransExecutorMeta.loadMappingMeta( meta, meta.getRepository(), meta.getMetaStore(), this, meta.getParameters().isInheritingAllVariables() );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    TransExecutorData transExecutorData = getData();
    transExecutorData.groupBuffer = null;
    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface )
    throws KettleException {
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

  @VisibleForTesting
  TransExecutorData getData() {
    return data;
  }

  private void setData( TransExecutorData data ) {
    this.data = data;
  }

  protected List<String> getLastIncomingFieldValues( ) {
    TransExecutorData transExecutorData = getData();
    List<String> lastIncomingFieldValues = new ArrayList<>();
    if ( transExecutorData == null || transExecutorData.groupBuffer.isEmpty() ) {
      return null;
    }

    int lastIncomingFieldIndex = transExecutorData.groupBuffer.size() - 1;
    ArrayList lastGroupBufferData = new ArrayList( Arrays.asList( transExecutorData.groupBuffer.get( lastIncomingFieldIndex ).getData() ) );
    lastGroupBufferData.removeAll( Collections.singleton( null ) );

    for ( int i = 0; i < lastGroupBufferData.size(); i++ ) {
      lastIncomingFieldValues.add( lastGroupBufferData.get( i ).toString() );
    }
    return lastIncomingFieldValues;
  }

  void initializeVariablesFromParent( Trans trans ) {
    if ( meta.getParameters().isInheritingAllVariables() ) {
      trans.initializeVariablesFrom( getTrans() );
    }
  }


}
