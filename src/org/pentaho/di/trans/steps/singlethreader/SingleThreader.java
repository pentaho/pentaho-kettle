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
 
package org.pentaho.di.trans.steps.singlethreader;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInputData;

/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */
public class SingleThreader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SingleThreaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SingleThreaderMeta meta;
	private SingleThreaderData data;
	
	public SingleThreader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    /**
     * Process rows in batches of N rows.
     * The sub-transformation will execute in a single thread.
     */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SingleThreaderMeta)smi;
		data=(SingleThreaderData)sdi;

		Object[] row = getRow();
		if (row==null) {
		  if (data.batchCount>0) {
		    data.executor.oneIteration();
		  }
		  setOutputDone();
		  return false;
		}
		
		if (first) {
		  first=false;
		  
      data.startTime = System.currentTimeMillis();
		}

		// Add the row to the producer...
		//
		data.rowProducer.putRow(getInputRowMeta(), row);
		data.batchCount++;
		
		boolean countWindow = data.batchSize>0 && data.batchCount>data.batchSize;
		boolean timeWindow = data.batchTime>0 && (System.currentTimeMillis()-data.startTime)>data.batchTime;
		
		if (countWindow || timeWindow) {
		  data.batchCount=0;

		  boolean more = data.executor.oneIteration();
		  if (!more) {
		    setOutputDone();
		    return false;
		  }
		  data.startTime=System.currentTimeMillis();
		}
		return true;
	}

	private void passParameters() throws KettleException {
	  
	  String[] parameters;
	  String[] parameterValues;
	  
	  if (meta.isPassingAllParameters()) {
	    // We pass the values for all the parameters from the parent transformation
	    //
	    parameters = data.mappingTransMeta.listParameters();
	    parameterValues = new String[parameters.length];
	    for (int i=0;i<parameters.length;i++) {
	      parameterValues[i] = environmentSubstitute(parameters[i]);
	    }
	  } else {
	    // We pass down the listed variables with the specified values...
	    //
	    parameters = meta.getParameters();
	    parameterValues = new String[parameters.length];
      for (int i=0;i<parameters.length;i++) {
        parameterValues[i] = environmentSubstitute(meta.getParameterValues()[i]);
      }
	  }
	  
	  for (int i=0;i<parameters.length;i++) {
	    String value = Const.NVL(parameterValues[i], "");
	    
      data.mappingTransMeta.setParameterValue(parameters[i], value);
	  }
  }

  public void prepareMappingExecution() throws KettleException {
	      // Set the type to single threaded in case the user forgot...
	      //
	      data.mappingTransMeta.setTransformationType(TransformationType.SingleThreaded);
	  
	      // Pass the parameters down to the sub-transformation.
	      //
	      passParameters();
	      
        // Create the transformation from meta-data...
		    //
        data.mappingTrans = new Trans(data.mappingTransMeta, getTrans());
        
        // Disable thread priority managment as it will slow things down needlessly.
        // The single threaded engine doesn't use threads and doesn't need row locking.
        //
        data.mappingTrans.getTransMeta().setUsingThreadPriorityManagment(false);
        
        // Leave a path up so that we can set variables in sub-transformations...
        //
        data.mappingTrans.setParentTrans(getTrans());
        
        // Pass down the safe mode flag to the mapping...
        //
        data.mappingTrans.setSafeModeEnabled(getTrans().isSafeModeEnabled());
        
        // Also set the name of this step in the mapping transformation for logging purposes
        //
        data.mappingTrans.setMappingStepName(getStepname());
        
        // Pass the servlet print writer
        //
        data.mappingTrans.setServletPrintWriter(getTrans().getServletPrintWriter());
        
        // prepare the execution 
        //
        data.mappingTrans.prepareExecution(null);
        
        // If the inject step is a mapping input step, tell it all is OK...
        //
        if (data.injectStepMeta.isMappingInput()) {
          MappingInputData mappingInputData = (MappingInputData) data.mappingTrans.findDataInterface(data.injectStepMeta.getName());
          mappingInputData.sourceSteps=new StepInterface[0];
          mappingInputData.valueRenames = new ArrayList<MappingValueRename>();
        }
        
        // Add row producer & row listener
        //
        data.rowProducer = data.mappingTrans.addRowProducer(meta.getInjectStep(), 0);
        
        StepInterface retrieveStep = data.mappingTrans.getStepInterface(meta.getRetrieveStep(), 0);
        retrieveStep.addRowListener(new RowAdapter() {
          @Override
          public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
            // Simply pass it along to the next steps after the SingleThreader
            //
            SingleThreader.this.putRow(rowMeta, row);
          }
        });
        
        data.mappingTrans.startThreads();
        
        // Create the executor...
        //
        data.executor = new SingleThreadedTransExecutor(data.mappingTrans);
        
        // We launch the transformation in the processRow when the first row is received.
        // This will allow the correct variables to be passed.
        // Otherwise the parent is the init() thread which will be gone once the init is done.
        //
        try {
          boolean ok = data.executor.init();
          if (!ok) {
            throw new KettleException(BaseMessages.getString(PKG, "SingleThreader.Exception.UnableToInitSingleThreadedTransformation"));
          }
        }
        catch(KettleException e) {
          throw new KettleException(BaseMessages.getString(PKG, "SingleThreader.Exception.UnableToPrepareExecutionOfMapping"), e);
        }
        
        // Add the mapping transformation to the active sub-transformations map in the parent transformation
        //
        getTrans().getActiveSubtransformations().put(getStepname(), data.mappingTrans);
	}


  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (SingleThreaderMeta) smi;
    data = (SingleThreaderData) sdi;

    if (super.init(smi, sdi)) {
      // First we need to load the mapping (transformation)
      try {
        // The batch size...
        //
        data.batchSize = Const.toInt(environmentSubstitute(meta.getBatchSize()), 0);
        data.batchTime = Const.toInt(environmentSubstitute(meta.getBatchTime()), 0);
        
        // Pass the repository down to the metadata object...
        //
        meta.setRepository(getTransMeta().getRepository());

        data.mappingTransMeta = SingleThreaderMeta.loadSingleThreadedTransMeta(meta, meta.getRepository(), this);
        if (data.mappingTransMeta != null) // Do we have a mapping at all?
        {
          // Validate the inject and retrieve step names
          //
          String injectStepName = environmentSubstitute(meta.getInjectStep());
          data.injectStepMeta = data.mappingTransMeta.findStep(injectStepName);
          if (data.injectStepMeta==null) {
            logError("The inject step with name '"+injectStepName+"' couldn't be found in the sub-transformation");
          }

          String retrieveStepName = environmentSubstitute(meta.getRetrieveStep());
          if (!Const.isEmpty(retrieveStepName)) {
            data.retrieveStepMeta = data.mappingTransMeta.findStep(retrieveStepName);
            if (data.retrieveStepMeta==null) {
              logError("The retrieve step with name '"+retrieveStepName+"' couldn't be found in the sub-transformation");
            }
          }

          // OK, now prepare the execution of the mapping.
          // This includes the allocation of RowSet buffers, the creation of the
          // sub-transformation threads, etc.
          //
          prepareMappingExecution();
          //
          
          // That's all for now...
          //
          return true;
        } else {
          logError("No valid mapping was specified!");
          return false;
        }
      } catch (Exception e) {
        logError("Unable to load the mapping transformation because of an error : " + e.toString());
        logError(Const.getStackTracker(e));
      }

    }
    return false;
  }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // dispose of the single threading execution engine
        //
        try {
          data.executor.dispose();
        } catch (KettleException e) {
          log.logError("Error disposing of sub-transformation: ", e);
        }
        
        super.dispose(smi, sdi);
    }
    
    public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {
    	if (data.mappingTrans!=null) {
    		data.mappingTrans.stopAll();
    	}
    }
    
    public void stopAll()
    {
        // Stop the mapping step.
        if ( data.mappingTrans != null  )
        {
            data.mappingTrans.stopAll();
        }
        
        // Also stop this step
        super.stopAll();
    }
	
    public Trans getMappingTrans() {
    	return data.mappingTrans;
    }
}