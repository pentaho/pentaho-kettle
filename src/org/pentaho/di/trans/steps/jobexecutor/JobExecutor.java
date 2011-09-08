 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.jobexecutor;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */
public class JobExecutor extends BaseStep implements StepInterface
{
	private static Class<?> PKG = JobExecutorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private JobExecutorMeta meta;
	private JobExecutorData data;
	
	public JobExecutor(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    /**
     * Process a single row.  In our case, we send one row of data to a piece of transformation.
     * In the transformation, we look up the MappingInput step to send our rows to it.
     * As a consequence, for the time being, there can only be one MappingInput and one MappingOutput step in the JobExecutor.
     */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		try
		{
			meta=(JobExecutorMeta)smi;
			data=(JobExecutorData)sdi;
			
			// Wait for a row...
			//
			Object[] row = getRow();
			
			if (row==null) {
			  if (!data.groupBuffer.isEmpty()) {
			    executeJob();
			  }
			  setOutputDone();
			  return false;
			}
			
			if (first) {
			  first = false;

			  // calculate the various output row layouts first...
			  //
			  data.inputRowMeta = getInputRowMeta();
			  data.executionResultsOutputRowMeta = data.inputRowMeta.clone();
			  data.resultRowsOutputRowMeta = data.inputRowMeta.clone();
			  data.resultFilesOutputRowMeta = data.inputRowMeta.clone();

			  
        if (meta.getExecutionResultTargetStepMeta()!=null) {
          meta.getFields(data.executionResultsOutputRowMeta, getStepname(), null, meta.getExecutionResultTargetStepMeta(), this);
          data.executionResultRowSet = findOutputRowSet(meta.getExecutionResultTargetStepMeta().getName());
        }
			  if (meta.getResultRowsTargetStepMeta()!=null) {
			    meta.getFields(data.resultRowsOutputRowMeta, getStepname(), null, meta.getResultRowsTargetStepMeta(), this);
			    data.resultRowsRowSet = findOutputRowSet(meta.getResultRowsTargetStepMeta().getName());
			  }
        if (meta.getResultFilesTargetStepMeta()!=null) {
          meta.getFields(data.resultFilesOutputRowMeta, getStepname(), null, meta.getResultFilesTargetStepMeta(), this);
          data.resultFilesRowSet = findOutputRowSet(meta.getResultFilesTargetStepMeta().getName());
        }
        
			  // Remember which column to group on, if any...
        //
			  data.groupFieldIndex = -1;
			  if (!Const.isEmpty(data.groupField)) {
			    data.groupFieldIndex = getInputRowMeta().indexOfValue(data.groupField);
			    if (data.groupFieldIndex<0) {
			      throw new KettleException(BaseMessages.getString(PKG, "JobExecutor.Exception.GroupFieldNotFound", data.groupField));
			    }
			    data.groupFieldMeta = getInputRowMeta().getValueMeta(data.groupFieldIndex);
			  }
			}
			
			boolean newGroup = false;
			if (data.groupSize>=0) {
			  // Pass the input rows in blocks to the job result rows...
			  //
			  if (data.groupSize==0) {
			    // Pass all input rows...
			  } else {
			    if (data.groupBuffer.size() >= data.groupSize) {
			      newGroup = true;
			    }
			  }
			} else if (data.groupFieldIndex>=0) {
			  Object groupFieldData = row[data.groupFieldIndex];
			  if (data.prevGroupFieldData!=null) {
			    if (data.groupFieldMeta.compare(data.prevGroupFieldData, groupFieldData)!=0) {
			      newGroup = true;
			    }
			  }
			    
			  data.prevGroupFieldData = groupFieldData;
			} else if (data.groupTime>0) {
			  long now = System.currentTimeMillis();
			  if (now-data.groupTimeStart>=data.groupTime) {
			    newGroup = true;
			  }
			}
			
			if (newGroup) {
			  executeJob();
			}
			
      data.groupBuffer.add(new RowMetaAndData(getInputRowMeta(), row)); // should we clone for safety?
			
			return true;
		} catch(Exception e) {
		  throw new KettleException(BaseMessages.getString(PKG, "JobExecutor.UnexpectedError"), e);
		}
	}

	private void executeJob() throws KettleException {
	  
	  // If we got 0 rows on input we don't really want to execute the job
	  //
	  if (data.groupBuffer.isEmpty()) {
	    return;
	  }
	  
    data.groupTimeStart = System.currentTimeMillis();
    
    // Keep the strain on the logging back-end conservative.
    // TODO: make this optional/user-defined later
    //
    if (data.executorJob!=null) {
      CentralLogStore.discardLines(data.executorJob.getLogChannelId(), false);
      LoggingRegistry.getInstance().removeIncludingChildren(data.executorJob.getLogChannelId());
    }
    
    data.executorJob = new Job(meta.getRepository(), data.executorJobMeta, this);

    // data.executorJob.setParentJob(parentJob); TODO: create parentTrans
    data.executorJob.setLogLevel(getLogLevel());
    
    if (meta.getParameters().isInheritingAllVariables()) {
      data.executorJob.shareVariablesWith(this);
    }
    data.executorJob.setInternalKettleVariables(this);
    data.executorJob.copyParametersFrom(data.executorJobMeta);
    
    // data.executorJob.setInteractive(); TODO: pass interactivity through the transformation too for drill-down.
    
    // TODO 
    /*
    if (data.executorJob.isInteractive()) {
      data.executorJob.getJobEntryListeners().addAll(parentJob.getJobEntryListeners());
    }
    */

    // Pass the accumulated rows
    //
    data.executorJob.setSourceRows(data.groupBuffer);

    // Pass parameter values
    //
    passParametersToJob();
    
    // keep track for drill down in Spoon...
    //
    getTrans().getActiveSubjobs().put(getStepname(), data.executorJob);
    
    data.executorJob.beginProcessing();
    
    Result result = new Result();

    try {
      result = data.executorJob.execute(0, result);
    } catch(KettleException e)
    {
      log.logError("An error occurred executing the job: ", e);
      result.setResult(false);
      result.setNrErrors(1);
    } finally {
      try {
        data.executorJob.fireJobListeners();
      } catch(KettleException e) {
          result.setNrErrors(1);
          result.setResult(false);
          log.logError(BaseMessages.getString(PKG, "JobExecutor.Log.ErrorExecJob", e.getMessage()), e);
      }
    }
    
    
    // First the natural output...
    //
    if (meta.getExecutionResultTargetStepMeta()!=null) {
      Object[] outputRow = RowDataUtil.allocateRowData(data.executionResultsOutputRowMeta.size());
      int idx=0;
      
      if (!Const.isEmpty(meta.getExecutionTimeField())) {
        outputRow[idx++] = Long.valueOf(System.currentTimeMillis()-data.groupTimeStart);
      }
      if (!Const.isEmpty(meta.getExecutionResultField())) {
        outputRow[idx++] = Boolean.valueOf(result.getResult());
      }
      if (!Const.isEmpty(meta.getExecutionNrErrorsField())) {
        outputRow[idx++] = Long.valueOf(result.getNrErrors());
      }
      if (!Const.isEmpty(meta.getExecutionLinesReadField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesRead());
      }
      if (!Const.isEmpty(meta.getExecutionLinesWrittenField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesWritten());
      }
      if (!Const.isEmpty(meta.getExecutionLinesInputField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesInput());
      }
      if (!Const.isEmpty(meta.getExecutionLinesOutputField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesOutput());
      }
      if (!Const.isEmpty(meta.getExecutionLinesRejectedField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesRejected());
      }
      if (!Const.isEmpty(meta.getExecutionLinesUpdatedField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesUpdated());
      }
      if (!Const.isEmpty(meta.getExecutionLinesDeletedField())) {
        outputRow[idx++] = Long.valueOf(result.getNrLinesDeleted());
      }
      if (!Const.isEmpty(meta.getExecutionFilesRetrievedField())) {
        outputRow[idx++] = Long.valueOf(result.getNrFilesRetrieved());
      }
      if (!Const.isEmpty(meta.getExecutionExitStatusField())) {
        outputRow[idx++] = Long.valueOf(result.getExitStatus());
      }
      if (!Const.isEmpty(meta.getExecutionLogTextField())) {
        String channelId = data.executorJob.getLogChannelId();
        String logText = CentralLogStore.getAppender().getBuffer(channelId, false).toString();
        outputRow[idx++] = logText;
      }
      if (!Const.isEmpty(meta.getExecutionLogChannelIdField())) {
        outputRow[idx++] = data.executorJob.getLogChannelId();
      }
  
      putRowTo(data.executionResultsOutputRowMeta, outputRow, data.executionResultRowSet);
    }
    
    // Optionally also send the result rows to a specified target step...
    //
    if (meta.getResultRowsTargetStepMeta()!=null && result.getRows()!=null) {
      for (RowMetaAndData row : result.getRows()) {
        
        Object[] targetRow = RowDataUtil.allocateRowData(data.resultRowsOutputRowMeta.size());
        
        for (int i=0;i<meta.getResultRowsField().length;i++) {
          ValueMetaInterface valueMeta = row.getRowMeta().getValueMeta(i);
          if (valueMeta.getType() != meta.getResultRowsType()[i]) {
            throw new KettleException(BaseMessages.getString(PKG, "JobExecutor.IncorrectDataTypePassed", valueMeta.getTypeDesc(), ValueMeta.getTypeDesc(meta.getResultRowsType()[i]) ));
          }
          
          targetRow[i] = row.getData()[i];
        }
        putRowTo(data.resultRowsOutputRowMeta, targetRow, data.resultRowsRowSet);
      }
    }
    
    if (meta.getResultFilesTargetStepMeta()!=null && result.getResultFilesList()!=null) {
      for (ResultFile resultFile : result.getResultFilesList()) {
        Object[] targetRow = RowDataUtil.allocateRowData(data.resultFilesOutputRowMeta.size());
        int idx=0;
        targetRow[idx++] = resultFile.getFile().getName().toString();
        
        // TODO: time, origin, ...
        
        putRowTo(data.resultFilesOutputRowMeta, targetRow, data.resultFilesRowSet);
      }
    }
    
    data.groupBuffer.clear();
  }

  private void passParametersToJob() throws KettleException {
    // Set parameters, when fields are used take the first row in the set.
    //
    JobExecutorParameters parameters = meta.getParameters();
    data.executorJob.clearParameters();
    
    String[] parameterNames = data.executorJob.listParameters();
    for (int i=0;i<parameters.getVariable().length;i++) {
      String variable = parameters.getVariable()[i];
      String fieldName = parameters.getField()[i];
      String inputValue = parameters.getInput()[i];
      String value;
      // Take the value from an input row or from a static value?
      //
      if (!Const.isEmpty(fieldName)) {
        int idx = getInputRowMeta().indexOfValue(fieldName);
        if (idx<0) {
          throw new KettleException(BaseMessages.getString(PKG, "JobExecutor.Exception.UnableToFindField", fieldName));
        }
        
        value = data.groupBuffer.get(0).getString(idx, "");
      } else {
        value = environmentSubstitute(inputValue);
      }
      
      // See if this is a parameter or just a variable...
      //
      if (Const.indexOfString(variable, parameterNames)<0) {
        data.executorJob.setVariable(variable, Const.NVL(value, ""));
      } else {
        data.executorJob.setParameterValue(variable, Const.NVL(value, ""));
      }
    }
    data.executorJob.activateParameters();
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (JobExecutorMeta) smi;
    data = (JobExecutorData) sdi;

    if (super.init(smi, sdi)) {
      // First we need to load the mapping (transformation)
      try {
        // Pass the repository down to the metadata object...
        //
        meta.setRepository(getTransMeta().getRepository());

        data.executorJobMeta = JobExecutorMeta.loadMappingMeta(meta, meta.getRepository(), this);
        data.executorJobMeta.setArguments(getTransMeta().getArguments());
        
        // Do we have a job at all?
        //
        if (data.executorJobMeta != null) 
        {
          data.groupBuffer = new ArrayList<RowMetaAndData>();

          // How many rows do we group together for the job?
          //
          data.groupSize = -1;
          if (!Const.isEmpty(meta.getGroupSize())) {
            data.groupSize = Const.toInt(environmentSubstitute(meta.getGroupSize()), -1);
          }
          
          // Is there a grouping time set?
          //
          data.groupTime = -1;
          if (!Const.isEmpty(meta.getGroupTime())) {
            data.groupTime = Const.toInt(environmentSubstitute(meta.getGroupTime()), -1);
          }
          data.groupTimeStart = System.currentTimeMillis();;

          // Is there a grouping field set?
          //
          data.groupField = null;
          if (!Const.isEmpty(meta.getGroupField())) {
            data.groupField = environmentSubstitute(meta.getGroupField());
          }
          
          // That's all for now...
          return true;
        } else {
          logError("No valid job was specified nor loaded!");
          return false;
        }
      } catch (Exception e) {
        logError("Unable to load the executor job because of an error : ", e);
      }

    }
    return false;
  }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
      data.groupBuffer = null;
      
      super.dispose(smi, sdi);
    }
    
    public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {
    	if (data.executorJob!=null) {
    		data.executorJob.stopAll();
    	}
    }
    
    public void stopAll()
    {
        // Stop the job execution.
        if ( data.executorJob != null  )
        {
            data.executorJob.stopAll();
        }
        
        // Also stop this step
        super.stopAll();
    }
    
    /*
	
	  @Override
    public long getLinesInput() {
        if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesInput(); else return 0;
    }

    @Override
    public long getLinesOutput() {
      if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesOutput(); else return 0;
    }

    @Override
    public long getLinesRead() {
      if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesRead(); else return 0;
    }

    @Override
    public long getLinesRejected() {
      if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesRejected(); else return 0;
    }

    @Override
    public long getLinesUpdated() {
      if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesUpdated(); else return 0;
    }

    @Override
    public long getLinesWritten() {
      if (data!=null && data.executorJob != null && data.executorJob.getResult()!=null) return data.executorJob.getResult().getNrLinesWritten(); else return 0;
    }
    
    */

    public Job getExecutorJob() {
    	return data.executorJob;
    }
}