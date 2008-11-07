/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.detectlastrow;


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
 * Detect last row in a stream
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class DetectLastRow extends BaseStep implements StepInterface
{
    private DetectLastRowMeta meta;
    private DetectLastRowData data;
    private Object[] previousRow;
    
    public DetectLastRow(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DetectLastRowMeta)smi;
        data=(DetectLastRowData)sdi;
        
        Object[] r = getRow();      // Get row from input rowset & set row busy!
        
        
        if(first)
        {
        	if(getInputRowMeta()==null) return false;
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        }
		Object[] outputRow=null;
		
        try
        {
            if (r==null)  // no more input to be expected...
        	{
        		if(previousRow != null) 
        		{
        	        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        			for (int i = 0; i < data.NrPrevFields; i++)
        			{
        				outputRow[i] = previousRow[i];
        			}
        			outputRow[data.NrPrevFields]= true;
        			putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
        			 
                    if (log.isRowLevel()) 
                    	log.logRowlevel(toString(), Messages.getString("DetectLastRow.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
        		}
        		setOutputDone();
        		return false;
        	}

    		if(!first)
            {
    	        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
    			for (int i = 0; i < data.NrPrevFields; i++)
    			{
    				outputRow[i] = r[i];
    			}
        		
    			outputRow[data.NrPrevFields]= false;
    			putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
    			if (log.isRowLevel()) 
                  	log.logRowlevel(toString(), Messages.getString("DetectLastRow.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
            }
            // keep track of the current row
            previousRow = r;
            if(first)  	first=false;
        }
        catch(KettleException e)
        {
            boolean sendToErrorRow=false;
            String errorMessage = null;
            
        	if (getStepMeta().isDoingErrorHandling())
        	{
                 sendToErrorRow = true;
                 errorMessage = e.toString();
        	}
        	else
        	{
	            logError(Messages.getString("DetectLastRow.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "DetectLastRow001");
        	}
        }
            
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(DetectLastRowMeta)smi;
        data=(DetectLastRowData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("DetectLastRow.Error.ResultFieldMissing"));
        		return false;
        	}

            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (DetectLastRowMeta)smi;
        data = (DetectLastRowData)sdi;
     
        super.dispose(smi, sdi);
    }
    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }	
    
    public String toString()
    {
        return this.getClass().getName();
    }
}
