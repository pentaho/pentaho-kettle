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


package org.pentaho.di.trans.steps.jobexecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute a job for every input row.
 * <p>
 *     <b>Note:</b><br/>
 *     Be aware, logic of the classes methods is very similar to corresponding methods of
 *     {@link org.pentaho.di.trans.steps.transexecutor.TransExecutor TransExecutor}.
 *     If you change something in this class, consider copying your changes to TransExecutor as well.
 * </p>
 *
 * @author Matt
 * @since 22-nov-2005
 */
public class JobExecutor extends BaseStep implements StepInterface {
  private static Class<?> PKG = JobExecutorMeta.class; // for i18n purposes, needed by Translator2!!

  private JobExecutorMeta meta;
  private JobExecutorData data;

  public JobExecutor( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Process a single row. In our case, we send one row of data to a piece of transformation. In the transformation, we
   * look up the MappingInput step to send our rows to it. As a consequence, for the time being, there can only be one
   * MappingInput and one MappingOutput step in the JobExecutor.
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      meta = (JobExecutorMeta) smi;
      data = (JobExecutorData) sdi;

      // Wait for a row...
      //
      Object[] row = getRow();

      if ( row == null ) {
        if ( !data.groupBuffer.isEmpty() ) {
          executeJob();
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
        if ( meta.getResultRowsTargetStepMeta() != null ) {
          meta.getFields(
            data.resultRowsOutputRowMeta, getStepname(), null, meta.getResultRowsTargetStepMeta(), this,
            repository, metaStore );
          data.resultRowsRowSet = findOutputRowSet( meta.getResultRowsTargetStepMeta().getName() );
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
        if ( !Utils.isEmpty( data.groupField ) ) {
          data.groupFieldIndex = getInputRowMeta().indexOfValue( data.groupField );
          if ( data.groupFieldIndex < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobExecutor.Exception.GroupFieldNotFound", data.groupField ) );
          }
          data.groupFieldMeta = getInputRowMeta().getValueMeta( data.groupFieldIndex );
        }
      }

      // Grouping by field and execution time works ONLY if grouping by size is disabled.
      if ( data.groupSize < 0 ) {
        if ( data.groupFieldIndex >= 0 ) { // grouping by field
          Object groupFieldData = row[data.groupFieldIndex];
          if ( data.prevGroupFieldData != null ) {
            if ( data.groupFieldMeta.compare( data.prevGroupFieldData, groupFieldData ) != 0 ) {
              executeJob();
            }
          }
          data.prevGroupFieldData = groupFieldData;
        } else if ( data.groupTime > 0 ) { // grouping by execution time
          long now = System.currentTimeMillis();
          if ( now - data.groupTimeStart >= data.groupTime ) {
            executeJob();
          }
        }
      }

      // Add next value AFTER job execution, in case we are grouping by field (see PDI-14958),
      // and BEFORE checking size of a group, in case we are grouping by size (see PDI-14121).
      data.groupBuffer.add( new RowMetaAndData( getInputRowMeta(), row ) ); // should we clone for safety?

      // Grouping by size.
      // If group buffer size exceeds specified limit, then execute job and flush group buffer.
      if ( data.groupSize > 0 ) {
        // Pass all input rows...
        if ( data.groupBuffer.size() >= data.groupSize ) {
          executeJob();
        }
      }

      return true;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobExecutor.UnexpectedError" ), e );
    }
  }

  private void executeJob() throws KettleException {

    // If we got 0 rows on input we don't really want to execute the job
    //
    if ( data.groupBuffer.isEmpty() ) {
      return;
    }

    data.groupTimeStart = System.currentTimeMillis();

    if ( first ) {
      discardLogLines( data );
    }

    data.executorJob = createJob( meta.getRepository(), data.executorJobMeta, this );

    data.executorJob.shareVariablesWith( data.executorJobMeta );
    data.executorJob.setParentTrans( getTrans() );
    data.executorJob.setLogLevel( getLogLevel() );
    data.executorJob.setInternalKettleVariables( this );
    data.executorJob.copyParametersFrom( data.executorJobMeta );
    data.executorJob.setArguments( getTrans().getArguments() );

    // data.executorJob.setInteractive(); TODO: pass interactivity through the transformation too for drill-down.

    // TODO
    /*
     * if (data.executorJob.isInteractive()) {
     * data.executorJob.getJobEntryListeners().addAll(parentJob.getJobEntryListeners()); }
     */

    // Pass the accumulated rows
    //
    data.executorJob.setSourceRows( data.groupBuffer );

    // Pass parameter values
    //
    passParametersToJob();

    // keep track for drill down in Spoon...
    //
    getTrans().getActiveSubjobs().put( getStepname(), data.executorJob );

    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.JobStart.id, data.executorJob );

    data.executorJob.setInitialLogBufferStartLine();
    data.executorJob.beginProcessing();

    Result result = new Result();

    // Inform the parent transformation we delegated work here...
    //
    for ( DelegationListener delegationListener : getTrans().getDelegationListeners() ) {
      // TODO: copy some settings in the job execution configuration, not strictly needed
      // but the execution configuration information is useful in case of a job re-start on Carte
      //
      delegationListener.jobDelegationStarted( data.executorJob, new JobExecutionConfiguration() );
    }

    // Now go execute this job
    //
    try {
      result = data.executorJob.execute( 0, result );
    } catch ( KettleException e ) {
      log.logError( "An error occurred executing the job: ", e );
      result.setResult( false );
      result.setNrErrors( 1 );
    } finally {
      try {
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.JobFinish.id, data.executorJob );
        getExecutorJob().getJobMeta().disposeEmbeddedMetastoreProvider();
        log.logDebug( BaseMessages.getString( PKG, "JobExecutor.Log.DisposeEmbeddedMetastore" ) );
        data.executorJob.fireJobFinishListeners();
      } catch ( KettleException e ) {
        result.setNrErrors( 1 );
        result.setResult( false );
        log.logError( BaseMessages.getString( PKG, "JobExecutor.Log.ErrorExecJob", e.getMessage() ), e );
      }
    }

    // First the natural output...
    //
    if ( meta.getExecutionResultTargetStepMeta() != null ) {
      Object[] outputRow = RowDataUtil.allocateRowData( data.executionResultsOutputRowMeta.size() );
      int idx = 0;

      if ( !Utils.isEmpty( meta.getExecutionTimeField() ) ) {
        outputRow[idx++] = Long.valueOf( System.currentTimeMillis() - data.groupTimeStart );
      }
      if ( !Utils.isEmpty( meta.getExecutionResultField() ) ) {
        outputRow[idx++] = Boolean.valueOf( result.getResult() );
      }
      if ( !Utils.isEmpty( meta.getExecutionNrErrorsField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrErrors() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesReadField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesRead() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesWrittenField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesWritten() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesInputField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesInput() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesOutputField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesOutput() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesRejectedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesRejected() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesUpdatedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesUpdated() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLinesDeletedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrLinesDeleted() );
      }
      if ( !Utils.isEmpty( meta.getExecutionFilesRetrievedField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getNrFilesRetrieved() );
      }
      if ( !Utils.isEmpty( meta.getExecutionExitStatusField() ) ) {
        outputRow[idx++] = Long.valueOf( result.getExitStatus() );
      }
      if ( !Utils.isEmpty( meta.getExecutionLogTextField() ) ) {
        String channelId = data.executorJob.getLogChannelId();
        String logText = KettleLogStore.getAppender().getBuffer( channelId, false ).toString();
        outputRow[idx++] = logText;
      }
      if ( !Utils.isEmpty( meta.getExecutionLogChannelIdField() ) ) {
        outputRow[idx++] = data.executorJob.getLogChannelId();
      }

      putRowTo( data.executionResultsOutputRowMeta, outputRow, data.executionResultRowSet );
    }

    // Optionally also send the result rows to a specified target step...
    //
    if ( meta.getResultRowsTargetStepMeta() != null && result.getRows() != null ) {
      for ( RowMetaAndData row : result.getRows() ) {

        Object[] targetRow = RowDataUtil.allocateRowData( data.resultRowsOutputRowMeta.size() );

        for ( int i = 0; i < meta.getResultRowsField().length; i++ ) {
          ValueMetaInterface valueMeta = row.getRowMeta().getValueMeta( i );
          if ( valueMeta.getType() != meta.getResultRowsType()[i] ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobExecutor.IncorrectDataTypePassed", valueMeta.getTypeDesc(),
              ValueMetaFactory.getValueMetaName( meta.getResultRowsType()[i] ) ) );
          }

          targetRow[i] = row.getData()[i];
        }
        putRowTo( data.resultRowsOutputRowMeta, targetRow, data.resultRowsRowSet );
      }
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

  @VisibleForTesting
  Job createJob( Repository repository, JobMeta jobMeta, LoggingObjectInterface parentLogging ) {
    return new Job( repository, jobMeta, parentLogging );
  }

  @VisibleForTesting
  void discardLogLines( JobExecutorData data ) {
    // Keep the strain on the logging back-end conservative.
    // TODO: make this optional/user-defined later
    if ( data.executorJob != null ) {
      KettleLogStore.discardLines( data.executorJob.getLogChannelId(), false );
    }
  }

  private void passParametersToJob() throws KettleException {
    // Set parameters, when fields are used take the first row in the set.
    //
    JobExecutorParameters parameters = meta.getParameters();

    String value;

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      String fieldName = parameters.getField()[i];
      if ( !Utils.isEmpty( fieldName ) ) {
        int idx = getInputRowMeta().indexOfValue( fieldName );
        if ( idx < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "JobExecutor.Exception.UnableToFindField", fieldName ) );
        }
        value = data.groupBuffer.get( 0 ).getString( idx, "" );
        this.setVariable( parameters.getVariable()[ i ], value );
      }
    }

    StepWithMappingMeta.activateParams( data.executorJob, data.executorJob, this, data.executorJob.listParameters(),
      parameters.getVariable(), parameters.getInput(), meta.getParameters().isInheritingAllVariables() );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (JobExecutorMeta) smi;
    data = (JobExecutorData) sdi;

    if ( super.init( smi, sdi ) ) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        //
        meta.setRepository( getTransMeta().getRepository() );

        data.executorJobMeta = JobExecutorMeta.loadJobMeta( meta, meta.getRepository(), this );

        // Do we have a job at all?
        //
        if ( data.executorJobMeta != null ) {
          data.groupBuffer = new ArrayList<RowMetaAndData>();

          // How many rows do we group together for the job?
          //
          data.groupSize = -1;
          if ( !Utils.isEmpty( meta.getGroupSize() ) ) {
            data.groupSize = Const.toInt( environmentSubstitute( meta.getGroupSize() ), -1 );
          }

          // Is there a grouping time set?
          //
          data.groupTime = -1;
          if ( !Utils.isEmpty( meta.getGroupTime() ) ) {
            data.groupTime = Const.toInt( environmentSubstitute( meta.getGroupTime() ), -1 );
          }
          data.groupTimeStart = System.currentTimeMillis();

          // Is there a grouping field set?
          //
          data.groupField = null;
          if ( !Utils.isEmpty( meta.getGroupField() ) ) {
            data.groupField = environmentSubstitute( meta.getGroupField() );
          }

          // That's all for now...
          return true;
        } else {
          logError( "No valid job was specified nor loaded!" );
          return false;
        }
      } catch ( Exception e ) {
        logError( "Unable to load the executor job because of an error : ", e );
      }

    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    data.groupBuffer = null;

    super.dispose( smi, sdi );
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    if ( data.executorJob != null ) {
      data.executorJob.stopAll();
    }
  }

  public void stopAll() {
    // Stop the job execution.
    if ( data.executorJob != null ) {
      data.executorJob.stopAll();
    }

    // Also stop this step
    super.stopAll();
  }

  /*
   *
   * @Override public long getLinesInput() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesInput(); else return 0; }
   *
   * @Override public long getLinesOutput() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesOutput(); else return 0; }
   *
   * @Override public long getLinesRead() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesRead(); else return 0; }
   *
   * @Override public long getLinesRejected() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesRejected(); else return 0; }
   *
   * @Override public long getLinesUpdated() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesUpdated(); else return 0; }
   *
   * @Override public long getLinesWritten() { if (data!=null && data.executorJob != null &&
   * data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesWritten(); else return 0; }
   */

  public Job getExecutorJob() {
    return data.executorJob;
  }

  @Override
  public JSONObject doAction(String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                             Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = JobExecutor.class.getDeclaredMethod( fieldName + "Action", Map.class );
      this.setStepMetaInterface( stepMetaInterface );
      this.meta = (JobExecutorMeta) stepMetaInterface;
      this.meta.setRepository( transMeta.getRepository() );
      this.repository = transMeta.getRepository();
      this.setTransMeta( transMeta );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );

    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject parametersAction( Map<String, String> queryParams ) throws KettleException {
    JobExecutorMeta jobExecutorMeta = (JobExecutorMeta) getStepMetaInterface();

    JSONObject response = new JSONObject();
    JSONArray parameterArray = new JSONArray();
    try {
      String filename = jobExecutorMeta.getDirectoryPath() + "/" + jobExecutorMeta.getJobName();
      JobMeta inputTransMeta =  loadJob( filename, jobExecutorMeta.getSpecificationMethod() );
      if( inputTransMeta != null ) {
        String[] parameters = inputTransMeta.listParameters();
        for ( int i = 0; i < parameters.length; i++ ) {
          JSONObject parameter = new JSONObject();
          String name = parameters[ i ];
          String desc = inputTransMeta.getParameterDescription( name );
          String str = inputTransMeta.getParameterDefault( name );
          str = ( StringUtils.isNotBlank( str ) ? str : ( desc != null ? desc : "" ) );

          parameter.put( "variable", Const.NVL( name, "" ) );
          parameter.put( "field", "" );
          parameter.put( "input", Const.NVL( str, "" ) );
          parameterArray.add( parameter );
        }
      }
      response.put( "parameters", parameterArray );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  private JobMeta loadJob( String filename, ObjectLocationSpecificationMethod specificationMethod ) throws KettleException {
   JobMeta executorJobMeta = null;
    if ( repository != null ) {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    switch ( specificationMethod ) {
      case FILENAME:
        if ( Utils.isEmpty( filename ) ) {
          return executorJobMeta;
        }
        if ( !filename.endsWith( ".kjb" ) ) {
          filename = filename + ".kjb";
        }
        executorJobMeta = loadFileJob( filename );
        break;
      case REPOSITORY_BY_NAME:
        if ( Utils.isEmpty( filename ) ) {
          return executorJobMeta;
        }
        String transPath = getTransMeta().environmentSubstitute( filename );
        String realJobname = transPath;
        String realDirectory = "";
        int index = transPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          realJobname = transPath.substring( index + 1 );
          realDirectory = transPath.substring( 0, index );
        }

        if ( Utils.isEmpty( realDirectory ) || Utils.isEmpty( realJobname ) ) {
          throw new KettleException(
                  BaseMessages.getString( PKG, "JobExecutor.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
                  PKG, "JobExecutor.Exception.UnableToFindRepositoryDirectory" ) );
        }
        executorJobMeta = loadRepositoryJob( realJobname, repdir );
        break;
      default:
        break;
    }
    return executorJobMeta;
  }

  private JobMeta loadRepositoryJob( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    JobMeta executorJobMeta = repository.loadJob(  getTransMeta().environmentSubstitute( transName ), repdir, null, null );
    executorJobMeta.clearChanged();
    return executorJobMeta;
  }

  private JobMeta loadFileJob( String fname ) throws KettleException {
    JobMeta executorJobMeta =  new JobMeta( getTransMeta().environmentSubstitute( fname ), repository );
    executorJobMeta.clearChanged();
    return executorJobMeta;
  }
}
