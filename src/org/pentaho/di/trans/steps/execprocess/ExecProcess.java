/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.execprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute a process
 *  *  
 * @author Samatar
 * @since 03-11-2008
 *
 */

public class ExecProcess extends BaseStep implements StepInterface
{
    private ExecProcessMeta meta;
    private ExecProcessData data;
    
    public ExecProcess(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(ExecProcessMeta)smi;
        data=(ExecProcessData)sdi;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        
        if(first)
    	{
    		first=false;
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
    		// Check is process field is provided
			if (Const.isEmpty(meta.getProcessField()))
			{
				logError(Messages.getString("ExecProcess.Error.ProcessFieldMissing"));
				throw new KettleException(Messages.getString("ExecProcess.Error.ProcessFieldMissing"));
			}
			
			// cache the position of the field			
			if (data.indexOfProcess<0)
			{	
				data.indexOfProcess =data.previousRowMeta .indexOfValue(meta.getProcessField());
				if (data.indexOfProcess<0)
				{
					// The field is unreachable !
					logError(Messages.getString("ExecProcess.Exception.CouldnotFindField")+ "[" + meta.getProcessField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleException(Messages.getString("ExecProcess.Exception.CouldnotFindField",meta.getProcessField())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
    	}// End If first 

    	Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		for (int i = 0; i < data.NrPrevFields; i++)
		{
			outputRow[i] = r[i];
		}
		// get process to execute
    	String processString= data.previousRowMeta.getString(r,data.indexOfProcess);
        
        try
        {
         	if(Const.isEmpty(processString)) throw new KettleException(Messages.getString("ExecProcess.ProcessEmpty"));
         	
         	// execute and return result as String
         	String resultString=execProcess(processString);
         	
        	// Add result field to input stream
    		outputRow[data.NrPrevFields]= resultString;

			 //	add new values to the row.
	        putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
         	
	        if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("ExecProcess.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
        }
        catch(KettleException e)
        {
        	if (getStepMeta().isDoingErrorHandling())
        	{
                 sendToErrorRow = true;
                 errorMessage = e.toString();
        	}
        	else
        	{
	            logError(Messages.getString("ExecProcess.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "ExecProcess001");
        	}
        }
            
        return true;
    }
    private String execProcess(String process) throws KettleException
    {
   	String retval=null;
   	
   	Process p =null;
   	try{
   		
   		p = Runtime.getRuntime().exec(process);
   		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
   		String line;
   		StringBuffer retvalBuff=new StringBuffer();
   		
   		while ((line = b.readLine()) != null) 
   		{
   			retvalBuff.append(line);
   		}
   		retval=retvalBuff.toString();
   	}catch(Exception e){
   		throw new KettleException(e); 
   	}
   	finally
   	{
   		if(p!=null) p.destroy();
   	}
   	return retval;	
   }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(ExecProcessMeta)smi;
        data=(ExecProcessData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("ExecProcess.Error.ResultFieldMissing"));
        		return false;
        	}
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (ExecProcessMeta)smi;
        data = (ExecProcessData)sdi;

        super.dispose(smi, sdi);
    }
	  //
    // Run is were the action happens!
    public void run()
    {
    	if(log.isBasic()) logBasic(Messages.getString("ExecProcess.Log.StartingToRun")); //$NON-NLS-1$
        
        try
        {
            while (processRow(meta, data) && !isStopped());
        }
        catch(Exception e)
        {
            logError(Messages.getString("ExecProcess.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
        }
        finally
        {
            dispose(meta, data);
            logSummary();
            markStop();
        }
    }
    
}
