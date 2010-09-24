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
 
package org.pentaho.di.trans.steps.mappingoutput;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class MappingOutput extends BaseStep implements StepInterface
{
	private MappingOutputMeta meta;
	private MappingOutputData data;


	public MappingOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null) 
		{
            // No more input to be expected...  Tell the next steps.
            //
			setOutputDone();
			return false;
		}
		
        if (first)
        {
            first=false;
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.setOutputValueRenames(data.outputValueRenames);
            meta.setInputValueRenames(data.inputValueRenames);
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            // 
            // Wait until the parent transformation has started completely.
            // However, don't wait forever, if we don't have a connection after 60 seconds: bail out! 
            //
            int totalsleep = 0;
            if (getTrans().getParentTrans() != null) {
                while (!isStopped() && !getTrans().getParentTrans().isRunning())
                {
                    try { totalsleep+=10; Thread.sleep(10); } catch(InterruptedException e) { stopAll(); }
                    if (totalsleep>60000)
                    {
                    	throw new KettleException(Messages.getString("MappingOutput.Exception.UnableToConnectWithParentMapping", ""+(totalsleep/1000)));
                    }
                }
            }
            // Now see if there is a target step to send data to.
            // If not, simply eat the data...
            //
            if (data.targetSteps==null) {
            	log.logDetailed(toString(), Messages.getString("MappingOutput.NoTargetStepSpecified", getStepname()));
            }
        }
        
        // Copy row to possible alternate rowset(s).
        // Rowsets where added for all the possible targets in the setter for data.targetSteps...
        // 
		putRow(data.outputRowMeta, r);     

        if (checkFeedback(getLinesRead())) logBasic(Messages.getString("MappingOutput.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}	

    public void setConnectorSteps(StepInterface[] targetSteps, List<MappingValueRename> inputValueRenames, List<MappingValueRename> outputValueRenames)
    {
        for (int i=0;i<targetSteps.length;i++) {
	        	
	        // OK, before we leave, make sure there is a rowset that covers the path to this target step.
	        // We need to create a new RowSet and add it to the Input RowSets of the target step
        	//
	        RowSet rowSet = new RowSet(getTransMeta().getSizeRowset());
	        
	        // This is always a single copy, but for source and target...
	        //
	        rowSet.setThreadNameFromToCopy(getStepname(), 0, targetSteps[i].getStepname(), 0);
	        
	        // Make sure to connect it to both sides...
	        //
	        getOutputRowSets().add(rowSet);
	        
	        // Add the row set to the target step as input.
	        // This will appropriately drain the buffer as data comes in.
	        // However, as an exception, we can't attach it to another mapping step.
	        // We need to attach it to the appropriate mapping input step.
	        // The main problem is that we can't do it here since we don't know that the other step has initialized properly yet.
	        // This method is called during init() and we can't tell for sure it's done already.
	        // As such, we'll simply grab the remaining row sets at the Mapping#processRow() level and assign them to a Mapping Input step. 
	        //
        	targetSteps[i].getInputRowSets().add(rowSet);
        }
        
        data.inputValueRenames = inputValueRenames;
        data.outputValueRenames = outputValueRenames;
        data.targetSteps = targetSteps;
    }
    
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}    
}