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
 
package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;

/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */
public class Mapping extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MappingMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MappingMeta meta;
	private MappingData data;
	
	public Mapping(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    /**
     * Process a single row.  In our case, we send one row of data to a piece of transformation.
     * In the transformation, we look up the MappingInput step to send our rows to it.
     * As a consequence, for the time being, there can only be one MappingInput and one MappingOutput step in the Mapping.
     */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		try
		{
			meta=(MappingMeta)smi;
			data=(MappingData)sdi;
			
			// Before we start, let's see if there are loose ends to tie up...
			//
			if (!getInputRowSets().isEmpty()) {
				MappingInput[] mappingInputs = data.mappingTrans.findMappingInput();
				for (RowSet rowSet : new ArrayList<RowSet>(getInputRowSets())) {
					// Pass this rowset down to a mapping input step in the sub-transformation...
					//
					if (mappingInputs.length==1) {
						// Simple case: only one input mapping.  Move the RowSet over
						//
						getInputRowSets().remove(rowSet);
						mappingInputs[0].getInputRowSets().add(rowSet);
					} else {
						// Difficult to see what's going on here.
						// TODO: figure out where this RowSet needs to go and where it comes from.
						//
						throw new KettleException("Unsupported situation detected.  To solve it, insert a dummy step.");
					}
				}
			}
			
			// Start the mapping/sub-transformation threads
	        //
	        data.mappingTrans.startThreads();
	        
	        // The transformation still runs in the background and might have some more work to do.
	        // Since everything is running in the MappingThreads we don't have to do anything else here but wait...
	        //
	        if (getTransMeta().getTransformationType()==TransformationType.Normal ||
	            getTransMeta().getTransformationType()==TransformationType.Monitored ) {
		        data.mappingTrans.waitUntilFinished();
		        
		        // Set some statistics from the mapping...
		        // This will show up in Spoon, etc.
		        //
		    	Result result = data.mappingTrans.getResult();
		    	setErrors(result.getNrErrors());
		    	setLinesRead( result.getNrLinesRead() );
		    	setLinesWritten( result.getNrLinesWritten() );
		    	setLinesInput( result.getNrLinesInput() );
		    	setLinesOutput( result.getNrLinesOutput() );
		    	setLinesUpdated( result.getNrLinesUpdated() );
		    	setLinesRejected( result.getNrLinesRejected() );
	        }
		    return false;
		    	
		}
		catch(Throwable t)
		{
			// Some unexpected situation occurred.
			// Better to stop the mapping transformation.
			//
			if (data.mappingTrans!=null) data.mappingTrans.stopAll();
			
			// Forward the exception...
			//
			throw new KettleException(t);
		}
	}

	private void setMappingParameters() throws KettleException {
		MappingParameters mappingParameters = meta.getMappingParameters();
		if (mappingParameters!=null) {
			
			// See if we need to pass all variables from the parent or not...
			//
			if (mappingParameters.isInheritingAllVariables()) {
				data.mappingTransMeta.copyVariablesFrom(getTransMeta());
			}
			
			// Just set the variables in the transformation statically.
			// This just means: set a number of variables:
			//
			for (int i=0;i<mappingParameters.getVariable().length;i++) {
				String name = mappingParameters.getVariable()[i];
				String value = environmentSubstitute(mappingParameters.getInputField()[i]);
				if (!Const.isEmpty(name) && !Const.isEmpty(value)) {
					data.mappingTransMeta.setVariable(name, value);
				}
			}
		}
	}

	public void prepareMappingExecution() throws KettleException {
        // Create the transformation from meta-data...
		//
        data.mappingTrans = new Trans(data.mappingTransMeta, getTrans());
        
        if (data.mappingTransMeta.getTransformationType()==TransformationType.SerialSingleThreaded) {
        	data.mappingTrans.getTransMeta().setUsingThreadPriorityManagment(false);
        }
        
        // Leave a path up so that we can set variables in sub-transformations...
        //
        data.mappingTrans.setParentTrans(getTrans());
        
        // Pass down the safe mode flag to the mapping...
        //
        data.mappingTrans.setSafeModeEnabled(getTrans().isSafeModeEnabled());
        
        // Also set the name of this step in the mapping transformation for logging purposes
        //
        data.mappingTrans.setMappingStepName(getStepname());
        
        // We launch the transformation in the processRow when the first row is received.
        // This will allow the correct variables to be passed.
        // Otherwise the parent is the init() thread which will be gone once the init is done.
        //
        try {
        	data.mappingTrans.prepareExecution(getTransMeta().getArguments());
        }
        catch(KettleException e) {
        	throw new KettleException(BaseMessages.getString(PKG, "Mapping.Exception.UnableToPrepareExecutionOfMapping"), e);
        }

		// If there is no read/write logging step set, we can insert the data from the first mapping input/output step...
		//
		MappingInput[] mappingInputs = data.mappingTrans.findMappingInput();
		LogTableField readField = data.mappingTransMeta.getTransLogTable().findField(TransLogTable.ID.LINES_READ);
		if (readField.getSubject()==null && mappingInputs!=null && mappingInputs.length>=1) {
			readField.setSubject(mappingInputs[0].getStepMeta());
		}
		MappingOutput[] mappingOutputs = data.mappingTrans.findMappingOutput();
		LogTableField writeField = data.mappingTransMeta.getTransLogTable().findField(TransLogTable.ID.LINES_WRITTEN);
		if (writeField.getSubject()==null && mappingOutputs!=null && mappingOutputs.length>=1) {
			writeField.setSubject(mappingOutputs[0].getStepMeta());
		}
        
        // Before we add rowsets and all, we should note that the mapping step did not receive ANY input and output rowsets.
        // This is an exception to the general rule, built into Trans.prepareExecution()
        //
        // A Mapping Input step is supposed to read directly from the previous steps.
        // A Mapping Output step is supposed to write directly to the next steps.
        
        // OK, check the input mapping definitions and look up the steps to read from.
        // 
        StepInterface[] sourceSteps;
        for (MappingIODefinition inputDefinition : meta.getInputMappings()) {
        	// If we have a single step to read from, we use this
        	//
        	if (!Const.isEmpty(inputDefinition.getInputStepname())) {
        		StepInterface sourceStep = (StepInterface) getTrans().findRunThread(inputDefinition.getInputStepname());
            	if (sourceStep==null) {
            		throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.StepNameNotFound", inputDefinition.getInputStepname()));
            	}
            	sourceSteps = new StepInterface[] { sourceStep, };
        	} 
        	else {
        		// We have no defined source step.
        		// That means that we're reading from all input steps that this mapping step has.
        		//
    	        List<StepMeta> prevSteps = getTransMeta().findPreviousSteps(getStepMeta());

    	        // TODO: Handle remote steps from: getStepMeta().getRemoteInputSteps()
    	        //
    	        
    			// Let's read data from all the previous steps we find...
    			// The origin is the previous step
    			// The target is the Mapping Input step.
    			//
    			sourceSteps=new StepInterface[prevSteps.size()];
    			for (int s=0;s<sourceSteps.length;s++) {
    				sourceSteps[s] = (StepInterface) getTrans().findRunThread(prevSteps.get(s).getName());
    			}
        	}
        	
        	// What step are we writing to?
        	MappingInput mappingInputTarget=null;
    		MappingInput[] mappingInputSteps = data.mappingTrans.findMappingInput();
        	if (Const.isEmpty(inputDefinition.getOutputStepname())) {
        		// No target was specifically specified.
        		// That means we only expect one "mapping input" step in the mapping...
        		
        		if (mappingInputSteps.length==0) {
        			throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.OneMappingInputStepRequired"));
        		}
        		if (mappingInputSteps.length>1) {
        			throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.OnlyOneMappingInputStepAllowed", ""+mappingInputSteps.length));
        		}
        		
        		mappingInputTarget = mappingInputSteps[0];
        	}
        	else {
        		// A target step was specified.  See if we can find it...
        		for (int s=0;s<mappingInputSteps.length && mappingInputTarget==null;s++) {
        			if (mappingInputSteps[s].getStepname().equals(inputDefinition.getOutputStepname())) {
        				mappingInputTarget = mappingInputSteps[s];
        			}
        		}
        		// If we still didn't find it it's a drag.
        		if (mappingInputTarget==null) {
            		throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.StepNameNotFound", inputDefinition.getOutputStepname()));
        		}
        	}
        	
        	// Before we pass the field renames to the mapping input step, let's add functionality to rename it back on ALL
        	// mapping output steps.
        	// To do this, we need a list of values that changed so we can revert that in the metadata before the rows come back.
        	// 
        	if (inputDefinition.isRenamingOnOutput()) addInputRenames(data.inputRenameList, inputDefinition.getValueRenames());
        	
        	mappingInputTarget.setConnectorSteps(sourceSteps, inputDefinition.getValueRenames(), getStepname());
        }
        
        // Now we have a List of connector threads.
        // If we start all these we'll be starting to pump data into the mapping
        // If we don't have any threads to start, nothings going in there...
        // However, before we send anything over, let's first explain to the mapping output steps where the data needs to go...
        //
        for (MappingIODefinition outputDefinition : meta.getOutputMappings()) {
        	// OK, what is the source (input) step in the mapping: it's the mapping output step...
        	// What step are we reading from here?
        	//
        	MappingOutput mappingOutputSource = (MappingOutput) data.mappingTrans.findRunThread(outputDefinition.getInputStepname());
        	if (mappingOutputSource==null) {
        		// No source step was specified: we're reading from a single Mapping Output step.
        		// We should verify this if this is really the case...
        		//
        		MappingOutput[] mappingOutputSteps = data.mappingTrans.findMappingOutput();
        		
        		if (mappingOutputSteps.length==0) {
        			throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.OneMappingOutputStepRequired"));
        		}
        		if (mappingOutputSteps.length>1) {
        			throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.OnlyOneMappingOutputStepAllowed", ""+mappingOutputSteps.length));
        		}
        		
        		mappingOutputSource = mappingOutputSteps[0];
        	}
        	
        	// To what step in this transformation are we writing to?
        	//
        	StepInterface[] targetSteps;
        	if (!Const.isEmpty(outputDefinition.getOutputStepname())) {
        		// If we have a target step specification for the output of the mapping, we need to send it over there...
        		//
            	StepInterface target = (StepInterface) getTrans().findRunThread(outputDefinition.getOutputStepname());
            	if (target==null) {
            		throw new KettleException(BaseMessages.getString(PKG, "MappingDialog.Exception.StepNameNotFound", outputDefinition.getOutputStepname()));
            	}
            	targetSteps = new StepInterface[] { target, };
        	}
        	else {
        		// No target step is specified.
        		// See if we can find the next steps in the transformation..
        		// 
        		List<StepMeta> nextSteps = getTransMeta().findNextSteps(getStepMeta());
        		
    			// Let's send the data to all the next steps we find...
    			// The origin is the mapping output step
    			// The target is all the next steps after this mapping step.
    			//
    			targetSteps=new StepInterface[nextSteps.size()];
    			for (int s=0;s<targetSteps.length;s++) {
    				targetSteps[s] = (StepInterface) getTrans().findRunThread(nextSteps.get(s).getName());
    			}
        	}
        	
        	// Now tell the mapping output step where to look...
        	// Also explain the mapping output steps how to rename the values back...
        	//
        	mappingOutputSource.setConnectorSteps(targetSteps, data.inputRenameList, outputDefinition.getValueRenames());
        	
        	// Is this mapping copying or distributing?
        	// Make sure the mapping output step mimics this behavior:
        	//
        	mappingOutputSource.setDistributed(isDistributed());        	
        }
        
        // Finally, add the mapping transformation to the active sub-transformations map in the parent transformation
        //
        getTrans().getActiveSubtransformations().put(getStepname(), data.mappingTrans);
	}

	public static void addInputRenames(List<MappingValueRename> renameList, List<MappingValueRename> addRenameList) {
		for (MappingValueRename rename : addRenameList) {
			if (renameList.indexOf(rename)<0) {
				renameList.add(rename);
			}
		}
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingMeta)smi;
		data=(MappingData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // First we need to load the mapping (transformation)
            try
            {
            	// Pass the repository down to the metadata object...
            	//
            	meta.setRepository(getTransMeta().getRepository());
            	
                data.mappingTransMeta = MappingMeta.loadMappingMeta(meta, meta.getRepository(), this);
                if (data.mappingTransMeta!=null) // Do we have a mapping at all?
                {
                	// Set the parameters statically or dynamically
            		//
            		setMappingParameters();
            		
            		// OK, now prepare the execution of the mapping.
            		// This includes the allocation of RowSet buffers, the creation of the sub-transformation threads, etc.
            		//
            		prepareMappingExecution();
            		
            		lookupStatusStepNumbers();
                	// That's all for now...
                    return true;
                }
                else
                {
                    logError("No valid mapping was specified!");
                    return false;
                }
            }
            catch(Exception e)
            {
                logError("Unable to load the mapping transformation because of an error : "+e.toString());
                logError(Const.getStackTracker(e));
            }
            
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Close the running transformation
        if (data.wasStarted)
        {
            // Wait until the child transformation has finished.
            data.mappingTrans.waitUntilFinished();
            
            // Remove it from the list of active sub-transformations...
            //
            getTrans().getActiveSubtransformations().remove(getStepname());
            
            // See if there was an error in the sub-transformation, in that case, flag error etc.
            if (data.mappingTrans.getErrors()>0)
            {
                logError(BaseMessages.getString(PKG, "Mapping.Log.ErrorOccurredInSubTransformation")); //$NON-NLS-1$
                setErrors(1);
            }
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
	
	private void lookupStatusStepNumbers()
	{
	    if (data.mappingTrans != null)
	    {
	        List<StepMetaDataCombi> steps = data.mappingTrans.getSteps();
	        for (int i=0;i<steps.size();i++)
	        {
	            StepMetaDataCombi sid = steps.get(i);
	            BaseStep rt = (BaseStep)sid.step;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameRead()))     data.linesReadStepNr = i;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameInput()))    data.linesInputStepNr = i;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameWritten()))  data.linesWrittenStepNr = i;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameOutput()))   data.linesOutputStepNr = i;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameUpdated()))  data.linesUpdatedStepNr = i;
	            if (rt.getStepname().equals(data.mappingTransMeta.getTransLogTable().getStepnameRejected())) data.linesRejectedStepNr = i;
	        }
	    }
	}

	@Override
    public long getLinesInput()
    {
        if (data!=null && data.linesInputStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesInputStepNr).step.getLinesInput();
        else
            return 0;
    }

    @Override
    public long getLinesOutput()
    {
        if (data!=null && data.linesOutputStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesOutputStepNr).step.getLinesOutput();
        else
            return 0;
    }

    @Override
    public long getLinesRead()
    {
        if (data!=null && data.linesReadStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesReadStepNr).step.getLinesRead();
        else
            return 0;
    }

    @Override
    public long getLinesRejected()
    {
        if (data!=null && data.linesRejectedStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesRejectedStepNr).step.getLinesRejected();
        else
            return 0;
    }

    @Override
    public long getLinesUpdated()
    {
        if (data!=null && data.linesUpdatedStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesUpdatedStepNr).step.getLinesUpdated();
        else
            return 0;
    }

    @Override
    public long getLinesWritten()
    {
        if (data!=null && data.linesWrittenStepNr != -1)
            return data.mappingTrans.getSteps().get(data.linesWrittenStepNr).step.getLinesWritten();
        else
            return 0;
    }

    @Override
    public int rowsetInputSize()
    {
        int size = 0;
        for (MappingInput input : data.mappingTrans.findMappingInput())
        {
            for (RowSet rowSet : input.getInputRowSets())
            {
                size += rowSet.size();
            }
        }
        return size;
    }

    @Override
    public int rowsetOutputSize()
    {
        int size = 0;
        for (MappingOutput output : data.mappingTrans.findMappingOutput())
        {
            for (RowSet rowSet : output.getOutputRowSets())
            {
                size += rowSet.size();
            }
        }
        return size;
    }
    
    public Trans getMappingTrans() {
    	return data.mappingTrans;
    }
    
    /**
     * For preview of the main data path, make sure we pass the row listener down to the Mapping Output step...
     */
    public void addRowListener(RowListener rowListener)
    {
        MappingOutput[] mappingOutputs = data.mappingTrans.findMappingOutput();
        if (mappingOutputs==null || mappingOutputs.length==0) return; // Nothing to do here...
        
    	// Simple case: one output mapping step : add the row listener over there
    	//
        /*
        if (mappingOutputs.length==1) {
        	mappingOutputs[0].addRowListener(rowListener);
        } else {
        	// Find the main data path...
        	//
        	
        	
        }
        */
        
        // Add the row listener to all the outputs in the mapping...
        //
        for (MappingOutput mappingOutput : mappingOutputs) {
        	mappingOutput.addRowListener(rowListener);
        }
    }
}