/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RemoteStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.TransStepUtil;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;

/**
 * Execute a mapping: a re-usuable transformation
 *
 * @author Matt
 * @since 22-nov-2005
 */
public class Mapping extends BaseStep implements StepInterface {
  private static Class<?> PKG = MappingMeta.class; // for i18n purposes, needed by Translator2!!

  private MappingMeta meta;
  private MappingData data;

  public Mapping( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Process a single row. In our case, we send one row of data to a piece of transformation. In the transformation, we
   * look up the MappingInput step to send our rows to it. As a consequence, for the time being, there can only be one
   * MappingInput and one MappingOutput step in the Mapping.
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      meta = (MappingMeta) smi;
      setData( (MappingData) sdi );

      MappingInput[] mappingInputs = getData().getMappingTrans().findMappingInput();
      MappingOutput[] mappingOutputs = getData().getMappingTrans().findMappingOutput();

      getData().wasStarted = true;
      switch ( getData().mappingTransMeta.getTransformationType() ) {
        case Normal:
        case SerialSingleThreaded:

          // Before we start, let's see if there are loose ends to tie up...
          //
          List<RowSet> inputRowSets = getInputRowSets();
          if ( !inputRowSets.isEmpty() ) {
            for ( RowSet rowSet : inputRowSets ) {
              // Pass this rowset down to a mapping input step in the
              // sub-transformation...
              //
              if ( mappingInputs.length == 1 ) {
                // Simple case: only one input mapping. Move the RowSet over
                //
                mappingInputs[0].addRowSetToInputRowSets( rowSet );
              } else {
                // Difficult to see what's going on here.
                // TODO: figure out where this RowSet needs to go and where it
                // comes from.
                //
                throw new KettleException(
                    "Unsupported situation detected where more than one Mapping Input step needs to be handled.  "
                        + "To solve it, insert a dummy step before the mapping step." );
              }
            }
            clearInputRowSets();
          }

          // Do the same thing for remote input steps...
          //
          if ( !getRemoteInputSteps().isEmpty() ) {
            // The remote server is likely a master or a slave server sending data
            // over remotely to this mapping.
            // However, the data needs to end up at a Mapping Input step of the
            // sub-transformation, not in this step.
            // We can move over the remote steps to the Mapping Input step as long
            // as the threads haven't started yet.
            //
            for ( RemoteStep remoteStep : getRemoteInputSteps() ) {
              // Pass this rowset down to a mapping input step in the
              // sub-transformation...
              //
              if ( mappingInputs.length == 1 ) {
                // Simple case: only one input mapping. Move the remote step over
                //
                mappingInputs[0].getRemoteInputSteps().add( remoteStep );
              } else {
                // TODO: figure out where this remote step needs to go and where
                // it comes from.
                //
                throw new KettleException(
                    "Unsupported situation detected where a remote input step is expecting data "
                        + "to end up in a particular Mapping Input step of a sub-transformation.  "
                        + "To solve it, insert a dummy step before the mapping." );
              }
            }
            getRemoteInputSteps().clear();
          }

          // Do the same thing for output row sets
          //
          List<RowSet> outputRowSets = getOutputRowSets();
          if ( !outputRowSets.isEmpty() ) {
            for ( RowSet rowSet : outputRowSets ) {
              // Pass this rowset down to a mapping input step in the
              // sub-transformation...
              //
              if ( mappingOutputs.length == 1 ) {
                // Simple case: only one output mapping. Move the RowSet over
                //
                mappingOutputs[0].addRowSetToOutputRowSets( rowSet );
              } else {
                // Difficult to see what's going on here.
                // TODO: figure out where this RowSet needs to go and where it
                // comes from.
                //
                throw new KettleException(
                    "Unsupported situation detected where more than one Mapping Output step needs to be handled.  "
                        + "To solve it, insert a dummy step after the mapping step." );
              }
            }
            clearOutputRowSets();
          }

          // Do the same thing for remote output steps...
          //
          if ( !getRemoteOutputSteps().isEmpty() ) {
            // The remote server is likely a master or a slave server sending data
            // over remotely to this mapping.
            // However, the data needs to end up at a Mapping Output step of the
            // sub-transformation, not in this step.
            // We can move over the remote steps to the Mapping Output step as long
            // as the threads haven't started yet.
            //
            for ( RemoteStep remoteStep : getRemoteOutputSteps() ) {
              // Pass this rowset down to a mapping output step in the
              // sub-transformation...
              //
              if ( mappingOutputs.length == 1 ) {
                // Simple case: only one output mapping. Move the remote step over
                //
                mappingOutputs[0].getRemoteOutputSteps().add( remoteStep );
              } else {
                // TODO: figure out where this remote step needs to go and where
                // it comes from.
                //
                throw new KettleException(
                    "Unsupported situation detected where a remote output step is expecting data "
                        + "to end up in a particular Mapping Output step of a sub-transformation.  "
                        + "To solve it, insert a dummy step after the mapping." );
              }
            }
            getRemoteOutputSteps().clear();
          }

          // Start the mapping/sub-transformation threads
          //
          getData().getMappingTrans().startThreads();

          // The transformation still runs in the background and might have some
          // more work to do.
          // Since everything is running in the MappingThreads we don't have to do
          // anything else here but wait...
          //
          if ( getTransMeta().getTransformationType() == TransformationType.Normal ) {
            getData().getMappingTrans().waitUntilFinished();

            // Set some statistics from the mapping...
            // This will show up in Spoon, etc.
            //
            Result result = getData().getMappingTrans().getResult();
            setErrors( result.getNrErrors() );
            setLinesRead( result.getNrLinesRead() );
            setLinesWritten( result.getNrLinesWritten() );
            setLinesInput( result.getNrLinesInput() );
            setLinesOutput( result.getNrLinesOutput() );
            setLinesUpdated( result.getNrLinesUpdated() );
            setLinesRejected( result.getNrLinesRejected() );
          }
          return false;

        case SingleThreaded:

          if ( mappingInputs.length > 1 || mappingOutputs.length > 1 ) {
            throw new KettleException(
                "Multiple input or output steps are not supported for a single threaded mapping." );
          }

          // Object[] row = getRow();
          // RowMetaInterface rowMeta = getInputRowMeta();

          // for (int count=0;count<(data.mappingTransMeta.getSizeRowset()/2) && row!=null;count++) {
          // // Pass each row over to the mapping input step, fill the buffer...
          //
          // mappingInputs[0].getInputRowSets().get(0).putRow(rowMeta, row);
          //
          // row = getRow();
          // }

          if ( ( log != null ) && log.isDebug() ) {
            List<RowSet> mappingInputRowSets = mappingInputs[0].getInputRowSets();
            log.logDebug( "# of input buffers: " + mappingInputRowSets.size() );
            if ( mappingInputRowSets.size() > 0 ) {
              log.logDebug( "Input buffer 0 size: " + mappingInputRowSets.get( 0 ).size() );
            }
          }

          // Now execute one batch...Basic logging
          //
          boolean result = getData().singleThreadedTransExcecutor.oneIteration();
          if ( !result ) {
            getData().singleThreadedTransExcecutor.dispose();
            setOutputDone();
            return false;
          }
          return true;

        default:
          throw new KettleException( "Transformation type '"
              + getData().mappingTransMeta.getTransformationType().getDescription()
              + "' is an unsupported transformation type for a mapping" );
      }
    } catch ( Throwable t ) {
      // Some unexpected situation occurred.
      // Better to stop the mapping transformation.
      //
      if ( getData().getMappingTrans() != null ) {
        getData().getMappingTrans().stopAll();
      }

      // Forward the exception...
      //
      throw new KettleException( t );
    }
  }


  public void prepareMappingExecution() throws KettleException {
    initTransFromMeta();
    MappingData mappingData = getData();
    // We launch the transformation in the processRow when the first row is
    // received.
    // This will allow the correct variables to be passed.
    // Otherwise the parent is the init() thread which will be gone once the
    // init is done.
    //
    try {
      mappingData.getMappingTrans().prepareExecution( getTrans().getArguments() );
    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Mapping.Exception.UnableToPrepareExecutionOfMapping" ),
          e );
    }

    // Extra optional work to do for alternative execution engines...
    //
    switch (  mappingData.mappingTransMeta.getTransformationType() ) {
      case Normal:
      case SerialSingleThreaded:
        break;

      case SingleThreaded:
        mappingData.singleThreadedTransExcecutor = new SingleThreadedTransExecutor( mappingData.getMappingTrans() );
        if ( !mappingData.singleThreadedTransExcecutor.init() ) {
          throw new KettleException( BaseMessages.getString( PKG,
              "Mapping.Exception.UnableToInitSingleThreadedTransformation" ) );
        }
        break;
      default:
        break;
    }

    // If there is no read/write logging step set, we can insert the data from
    // the first mapping input/output step...
    //
    MappingInput[] mappingInputs = mappingData.getMappingTrans().findMappingInput();
    LogTableField readField = mappingData.mappingTransMeta.getTransLogTable().findField( TransLogTable.ID.LINES_READ );
    if ( readField.getSubject() == null && mappingInputs != null && mappingInputs.length >= 1 ) {
      readField.setSubject( mappingInputs[0].getStepMeta() );
    }
    MappingOutput[] mappingOutputs = mappingData.getMappingTrans().findMappingOutput();
    LogTableField writeField = mappingData.mappingTransMeta.getTransLogTable().findField( TransLogTable.ID.LINES_WRITTEN );
    if ( writeField.getSubject() == null && mappingOutputs != null && mappingOutputs.length >= 1 ) {
      writeField.setSubject( mappingOutputs[0].getStepMeta() );
    }

    // Before we add rowsets and all, we should note that the mapping step did
    // not receive ANY input and output rowsets.
    // This is an exception to the general rule, built into
    // Trans.prepareExecution()
    //
    // A Mapping Input step is supposed to read directly from the previous
    // steps.
    // A Mapping Output step is supposed to write directly to the next steps.

    // OK, check the input mapping definitions and look up the steps to read
    // from.
    //
    StepInterface[] sourceSteps;
    for ( MappingIODefinition inputDefinition : meta.getInputMappings() ) {
      // If we have a single step to read from, we use this
      //
      if ( !Utils.isEmpty( inputDefinition.getInputStepname() ) ) {
        StepInterface sourceStep = getTrans().findRunThread( inputDefinition.getInputStepname() );
        if ( sourceStep == null ) {
          throw new KettleException( BaseMessages.getString( PKG, "MappingDialog.Exception.StepNameNotFound",
              inputDefinition.getInputStepname() ) );
        }
        sourceSteps = new StepInterface[] { sourceStep, };
      } else {
        // We have no defined source step.
        // That means that we're reading from all input steps that this mapping
        // step has.
        //
        List<StepMeta> prevSteps = getTransMeta().findPreviousSteps( getStepMeta() );

        // TODO: Handle remote steps from: getStepMeta().getRemoteInputSteps()
        //

        // Let's read data from all the previous steps we find...
        // The origin is the previous step
        // The target is the Mapping Input step.
        //
        sourceSteps = new StepInterface[prevSteps.size()];
        for ( int s = 0; s < sourceSteps.length; s++ ) {
          sourceSteps[s] = getTrans().findRunThread( prevSteps.get( s ).getName() );
        }
      }

      // What step are we writing to?
      MappingInput mappingInputTarget = null;
      MappingInput[] mappingInputSteps = mappingData.getMappingTrans().findMappingInput();
      if ( Utils.isEmpty( inputDefinition.getOutputStepname() ) ) {
        // No target was specifically specified.
        // That means we only expect one "mapping input" step in the mapping...

        if ( mappingInputSteps.length == 0 ) {
          throw new KettleException( BaseMessages
              .getString( PKG, "MappingDialog.Exception.OneMappingInputStepRequired" ) );
        }
        if ( mappingInputSteps.length > 1 ) {
          throw new KettleException( BaseMessages.getString( PKG,
              "MappingDialog.Exception.OnlyOneMappingInputStepAllowed", "" + mappingInputSteps.length ) );
        }

        mappingInputTarget = mappingInputSteps[0];
      } else {
        // A target step was specified. See if we can find it...
        for ( int s = 0; s < mappingInputSteps.length && mappingInputTarget == null; s++ ) {
          if ( mappingInputSteps[s].getStepname().equals( inputDefinition.getOutputStepname() ) ) {
            mappingInputTarget = mappingInputSteps[s];
          }
        }
        // If we still didn't find it it's a drag.
        if ( mappingInputTarget == null ) {
          throw new KettleException( BaseMessages.getString( PKG, "MappingDialog.Exception.StepNameNotFound",
              inputDefinition.getOutputStepname() ) );
        }
      }

      // Before we pass the field renames to the mapping input step, let's add
      // functionality to rename it back on ALL
      // mapping output steps.
      // To do this, we need a list of values that changed so we can revert that
      // in the metadata before the rows come back.
      //
      if ( inputDefinition.isRenamingOnOutput() ) {
        addInputRenames( getData().inputRenameList, inputDefinition.getValueRenames() );
      }

      mappingInputTarget.setConnectorSteps( sourceSteps, inputDefinition.getValueRenames(), getStepname() );
    }

    // Now we have a List of connector threads.
    // If we start all these we'll be starting to pump data into the mapping
    // If we don't have any threads to start, nothings going in there...
    // However, before we send anything over, let's first explain to the mapping
    // output steps where the data needs to go...
    //
    for ( MappingIODefinition outputDefinition : meta.getOutputMappings() ) {
      // OK, what is the source (input) step in the mapping: it's the mapping
      // output step...
      // What step are we reading from here?
      //
      MappingOutput mappingOutputSource =
          (MappingOutput) mappingData.getMappingTrans().findRunThread( outputDefinition.getInputStepname() );
      if ( mappingOutputSource == null ) {
        // No source step was specified: we're reading from a single Mapping
        // Output step.
        // We should verify this if this is really the case...
        //
        MappingOutput[] mappingOutputSteps = mappingData.getMappingTrans().findMappingOutput();

        if ( mappingOutputSteps.length == 0 ) {
          throw new KettleException( BaseMessages.getString( PKG,
              "MappingDialog.Exception.OneMappingOutputStepRequired" ) );
        }
        if ( mappingOutputSteps.length > 1 ) {
          throw new KettleException( BaseMessages.getString( PKG,
              "MappingDialog.Exception.OnlyOneMappingOutputStepAllowed", "" + mappingOutputSteps.length ) );
        }

        mappingOutputSource = mappingOutputSteps[0];
      }

      // To what steps in this transformation are we writing to?
      //
      StepInterface[] targetSteps = pickupTargetStepsFor( outputDefinition );

      // Now tell the mapping output step where to look...
      // Also explain the mapping output steps how to rename the values back...
      //
      mappingOutputSource
          .setConnectorSteps( targetSteps, getData().inputRenameList, outputDefinition.getValueRenames() );

      // Is this mapping copying or distributing?
      // Make sure the mapping output step mimics this behavior:
      //
      mappingOutputSource.setDistributed( isDistributed() );
    }

    // Finally, add the mapping transformation to the active sub-transformations
    // map in the parent transformation
    //
    getTrans().addActiveSubTransformation( getStepname(), getData().getMappingTrans() );
  }

  @VisibleForTesting StepInterface[] pickupTargetStepsFor( MappingIODefinition outputDefinition )
    throws KettleException {
    List<StepInterface> result;
    if ( !Utils.isEmpty( outputDefinition.getOutputStepname() ) ) {
      // If we have a target step specification for the output of the mapping,
      // we need to send it over there...
      //
      result = getTrans().findStepInterfaces( outputDefinition.getOutputStepname() );
      if ( Utils.isEmpty( result ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "MappingDialog.Exception.StepNameNotFound",
          outputDefinition.getOutputStepname() ) );
      }
    } else {
      // No target step is specified.
      // See if we can find the next steps in the transformation..
      //
      List<StepMeta> nextSteps = getTransMeta().findNextSteps( getStepMeta() );

      // Let's send the data to all the next steps we find...
      // The origin is the mapping output step
      // The target is all the next steps after this mapping step.
      //
      result = new ArrayList<>();
      for ( StepMeta nextStep : nextSteps ) {
        // need to take into the account different copies of the step
        List<StepInterface> copies = getTrans().findStepInterfaces( nextStep.getName() );
        if ( copies != null ) {
          result.addAll( copies );
        }
      }
    }
    return result.toArray( new StepInterface[ result.size() ] );
  }

  void initTransFromMeta() throws KettleException {
    // Create the transformation from meta-data...
    //
    getData().setMappingTrans( new Trans( getData().mappingTransMeta, this ) );

    if ( getData().mappingTransMeta.getTransformationType() != TransformationType.Normal ) {
      getData().getMappingTrans().getTransMeta().setUsingThreadPriorityManagment( false );
    }

    // Leave a path up so that we can set variables in sub-transformations...
    //
    getData().getMappingTrans().setParentTrans( getTrans() );

    // Pass down the safe mode flag to the mapping...
    //
    getData().getMappingTrans().setSafeModeEnabled( getTrans().isSafeModeEnabled() );

    // Pass down the metrics gathering flag:
    //
    getData().getMappingTrans().setGatheringMetrics( getTrans().isGatheringMetrics() );

    // Also set the name of this step in the mapping transformation for logging
    // purposes
    //
    getData().getMappingTrans().setMappingStepName( getStepname() );

    initServletConfig();

    // Set the parameters values in the mapping.
    //

    MappingParameters mappingParameters = meta.getMappingParameters();
    if ( mappingParameters != null ) {
      StepWithMappingMeta
        .activateParams( data.mappingTrans, data.mappingTrans, this, data.mappingTransMeta.listParameters(),
          mappingParameters.getVariable(), mappingParameters.getInputField() );
    }

  }

  void initServletConfig() {
    TransStepUtil.initServletConfig( getTrans(), getData().getMappingTrans() );
  }

  public static void addInputRenames( List<MappingValueRename> renameList, List<MappingValueRename> addRenameList ) {
    for ( MappingValueRename rename : addRenameList ) {
      if ( renameList.indexOf( rename ) < 0 ) {
        renameList.add( rename );
      }
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MappingMeta) smi;
    setData( (MappingData) sdi );
    MappingData mappingData = getData();
    if ( !super.init( smi, sdi ) ) {
      return false;
    }
    // First we need to load the mapping (transformation)
    try {
      // Pass the repository down to the metadata object...
      //
      meta.setRepository( getTransMeta().getRepository() );
      mappingData.mappingTransMeta = MappingMeta.loadMappingMeta( meta, meta.getRepository(),
          meta.getMetaStore(), this, meta.getMappingParameters().isInheritingAllVariables() );

      if ( data.mappingTransMeta == null ) {
        // Do we have a mapping at all?
        logError( "No valid mapping was specified!" );
        return false;
      }

      // OK, now prepare the execution of the mapping.
      // This includes the allocation of RowSet buffers, the creation of the
      // sub-transformation threads, etc.
      //
      prepareMappingExecution();

      lookupStatusStepNumbers();
      // That's all for now...
      return true;
    } catch ( Exception e ) {
      logError( "Unable to load the mapping transformation because of an error : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      return false;
    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    // Close the running transformation
    if ( getData().wasStarted ) {
      if ( !getData().mappingTrans.isFinished() ) {
        // Wait until the child transformation has finished.
        getData().getMappingTrans().waitUntilFinished();
      }
      // Remove it from the list of active sub-transformations...
      //
      getTrans().removeActiveSubTransformation( getStepname() );

      // See if there was an error in the sub-transformation, in that case, flag error etc.
      if ( getData().getMappingTrans().getErrors() > 0 ) {
        logError( BaseMessages.getString( PKG, "Mapping.Log.ErrorOccurredInSubTransformation" ) );
        setErrors( 1 );
      }
    }
    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface )
    throws KettleException {
    if ( getData().getMappingTrans() != null ) {
      getData().getMappingTrans().stopAll();
    }
  }

  public void stopAll() {
    // Stop the mapping step.
    if ( getData().getMappingTrans() != null ) {
      getData().getMappingTrans().stopAll();
    }

    // Also stop this step
    super.stopAll();
  }

  private void lookupStatusStepNumbers() {
    MappingData mappingData = getData();
    if ( mappingData.getMappingTrans() != null ) {
      List<StepMetaDataCombi> steps = mappingData.getMappingTrans().getSteps();
      for ( int i = 0; i < steps.size(); i++ ) {
        StepMetaDataCombi sid = steps.get( i );
        BaseStep rt = (BaseStep) sid.step;
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameRead() ) ) {
          mappingData.linesReadStepNr = i;
        }
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameInput() ) ) {
          mappingData.linesInputStepNr = i;
        }
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameWritten() ) ) {
          mappingData.linesWrittenStepNr = i;
        }
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameOutput() ) ) {
          mappingData.linesOutputStepNr = i;
        }
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameUpdated() ) ) {
          mappingData.linesUpdatedStepNr = i;
        }
        if ( rt.getStepname().equals( getData().mappingTransMeta.getTransLogTable().getStepnameRejected() ) ) {
          mappingData.linesRejectedStepNr = i;
        }
      }
    }
  }

  @Override
  public long getLinesInput() {
    if ( getData() != null && getData().linesInputStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesInputStepNr ).step.getLinesInput();
    } else {
      return 0;
    }
  }

  @Override
  public long getLinesOutput() {
    if ( getData() != null && getData().linesOutputStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesOutputStepNr ).step.getLinesOutput();
    } else {
      return 0;
    }
  }

  @Override
  public long getLinesRead() {
    if ( getData() != null && getData().linesReadStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesReadStepNr ).step.getLinesRead();
    } else {
      return 0;
    }
  }

  @Override
  public long getLinesRejected() {
    if ( getData() != null && getData().linesRejectedStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesRejectedStepNr ).step.getLinesRejected();
    } else {
      return 0;
    }
  }

  @Override
  public long getLinesUpdated() {
    if ( getData() != null && getData().linesUpdatedStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesUpdatedStepNr ).step.getLinesUpdated();
    } else {
      return 0;
    }
  }

  @Override
  public long getLinesWritten() {
    if ( getData() != null && getData().linesWrittenStepNr != -1 ) {
      return getData().getMappingTrans().getSteps().get( getData().linesWrittenStepNr ).step.getLinesWritten();
    } else {
      return 0;
    }
  }

  @Override
  public int rowsetInputSize() {
    int size = 0;
    for ( MappingInput input : getData().getMappingTrans().findMappingInput() ) {
      for ( RowSet rowSet : input.getInputRowSets() ) {
        size += rowSet.size();
      }
    }
    return size;
  }

  @Override
  public int rowsetOutputSize() {
    int size = 0;
    for ( MappingOutput output : getData().getMappingTrans().findMappingOutput() ) {
      for ( RowSet rowSet : output.getOutputRowSets() ) {
        size += rowSet.size();
      }
    }
    return size;
  }

  public Trans getMappingTrans() {
    return getData().getMappingTrans();
  }

  /**
   * For preview of the main data path, make sure we pass the row listener down to the Mapping Output step...
   */
  public void addRowListener( RowListener rowListener ) {
    MappingOutput[] mappingOutputs = getData().getMappingTrans().findMappingOutput();
    if ( mappingOutputs == null || mappingOutputs.length == 0 ) {
      return; // Nothing to do here...
    }

    // Simple case: one output mapping step : add the row listener over there
    //
    /*
     * if (mappingOutputs.length==1) { mappingOutputs[0].addRowListener(rowListener); } else { // Find the main data
     * path... //
     *
     *
     * }
     */

    // Add the row listener to all the outputs in the mapping...
    //
    for ( MappingOutput mappingOutput : mappingOutputs ) {
      mappingOutput.addRowListener( rowListener );
    }
  }

  MappingData getData() {
    return data;
  }

  void setData( MappingData data ) {
    this.data = data;
  }
}
