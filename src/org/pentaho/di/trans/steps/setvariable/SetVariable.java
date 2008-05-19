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
 
package org.pentaho.di.trans.steps.setvariable;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Convert Values in a certain fields to other values
 * 
 * @author Matt 
 * @since 27-apr-2006
 */
public class SetVariable extends BaseStep implements StepInterface
{
	private SetVariableMeta meta;
	private SetVariableData data;
	
	public SetVariable(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;
		
	    // Get one row from one of the rowsets...
        //
		Object[] rowData = getRow();
		if (rowData==null)  // means: no more input to be expected...
		{
			if (first)
			{
				// We do not received any row !!
				logBasic(Messages.getString("SetVariable.Log.NoInputRowSetDefault"));
				for (int i=0;i<meta.getFieldName().length;i++)
		        {
					if(!Const.isEmpty(meta.getDefaultValue()[i])) setValue(rowData,i,true); 
		        }
			}
		
            logBasic("Finished after "+linesWritten+" rows.");
			setOutputDone();
			return false;
		}
		
		if (first)
		{
		    first=false;
		    
		    data.outputMeta = getInputRowMeta().clone();
            
            logBasic(Messages.getString("SetVariable.Log.SettingVar"));

            for (int i=0;i<meta.getFieldName().length;i++)
            {
            	setValue(rowData,i,false);  
            }
           
            putRow(data.outputMeta, rowData);
            return true;		
         }

        throw new KettleStepException(Messages.getString("SetVariable.RuntimeError.MoreThanOneRowReceived.SETVARIABLE0007"));
	}
	
	private void setValue(Object[] rowData,int i,boolean usedefault) throws KettleException
	{
    	// Set the appropriate environment variable
    	//
		String value = null;
		if(usedefault)
			value=environmentSubstitute(meta.getDefaultValue()[i]);
		else
		{
			int index = data.outputMeta.indexOfValue(meta.getFieldName()[i]);
	    	if (index<0)
	    	{
	    		throw new KettleException("Unable to find field ["+meta.getFieldName()[i]+"] in input row");
	    	}
	    	ValueMetaInterface valueMeta = data.outputMeta.getValueMeta(index);
	    	Object valueData = rowData[index];
	    	
	    	// Get variable value
	
	        value=valueMeta.getCompatibleString(valueData);
        
		}
		
		if (value==null) value="";
		
    	// Get variable name
        String varname = meta.getVariableName()[i];
        
        if (Const.isEmpty(varname))
        {
            if (Const.isEmpty(value))
            {
                throw new KettleException("Variable name nor value was specified on line #"+(i+1));
            }
            else
            {
                throw new KettleException("There was no variable name specified for value ["+value+"]");
            }
        }
        
        
        VariableSpace transVariableSpace = null;
        Job parentJob = null;
        // OK, where do we set this value...
        switch(meta.getVariableType()[i])
        {
        case SetVariableMeta.VARIABLE_TYPE_JVM: 

            System.setProperty(varname, value);
            
            setVariable(varname, value);

            // Set variable in the transformation
            //
            transVariableSpace = getTrans();  
            transVariableSpace.setVariable(varname, value);

            parentJob = getTrans().getParentJob();
            while (parentJob!=null)
            {                           
                parentJob.setVariable(varname, value);
                parentJob = parentJob.getParentJob();
            }
            
            break;
        case SetVariableMeta.VARIABLE_TYPE_ROOT_JOB:
            {
                setVariable(varname, value);

                // Set variable in the transformation
                //
                transVariableSpace = getTrans();  
                transVariableSpace.setVariable(varname, value);

                // Comments by SB
                // VariableSpace rootJob = null;
                parentJob = getTrans().getParentJob();
                while (parentJob!=null)
                {                           
                    parentJob.setVariable(varname, value);
                    //rootJob = parentJob;
                    parentJob = parentJob.getParentJob();
                }
                // OK, we have the rootjob, set the variable on it...
                //if (rootJob==null)
                //{
                //   throw new KettleStepException("Can't set variable ["+varname+"] on root job: the root job is not available (meaning: not even the parent job)");
                //}
                //  Comment: why throw an exception on this?
            }
            break;
        case SetVariableMeta.VARIABLE_TYPE_GRAND_PARENT_JOB:
            {
            	// Set variable in this step
            	//
                setVariable(varname, value); 
                
                // Set variable in the transformation
                //
                transVariableSpace = getTrans();  
                transVariableSpace.setVariable(varname, value);
                
                // Set the variable in the parent job 
                //
                parentJob = getTrans().getParentJob();
                if (parentJob!=null)
                {
                	parentJob.setVariable(varname, value);
                }
                else
                {
                	throw new KettleStepException("Can't set variable ["+varname+"] on parent job: the parent job is not available");
                }

                // Set the variable on the grand-parent job
                //
                VariableSpace gpJob = getTrans().getParentJob().getParentJob();
                if (gpJob!=null)
                {
                    gpJob.setVariable(varname, value);
                }
                else
                {
                    throw new KettleStepException("Can't set variable ["+varname+"] on grand parent job: the grand parent job is not available");
                }
                
            }
            break;
        case SetVariableMeta.VARIABLE_TYPE_PARENT_JOB:
            {                        
            	// Set variable in this step
            	//
                setVariable(varname, value); 
                
                // Set variable in the transformation
                //
                transVariableSpace = getTrans();  
                transVariableSpace.setVariable(varname, value);
                
                // Set the variable in the parent job 
                //
                parentJob = getTrans().getParentJob();
                if (parentJob!=null)
                {
                	parentJob.setVariable(varname, value);
                }
                else
                {
                	throw new KettleStepException("Can't set variable ["+varname+"] on parent job: the parent job is not available");
                }
            }
        }               
        
        logBasic(Messages.getString("SetVariable.Log.SetVariableToValue",meta.getVariableName()[i],value));
}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;
		
		if (super.init(smi, sdi))
		{
            return true;
		}
		return false;
	}

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}