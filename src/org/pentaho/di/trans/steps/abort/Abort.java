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
package org.pentaho.di.trans.steps.abort;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Step that will abort after having seen 'x' number of rows on its input.
 * 
 * @author Sven Boden
 */
public class Abort extends BaseStep implements StepInterface {

    private AbortMeta meta;
    private AbortData data;
    private int nrInputRows;
    private int nrThresholdRows;
    
    public Abort(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			nrInputRows = 0;
			String threshold = environmentSubstitute(meta.getRowThreshold());
			nrThresholdRows = Const.toInt(threshold, -1);
			if ( nrThresholdRows < 0 )
			{
			    logError(Messages.getString("Abort.Log.ThresholdInvalid", threshold));
			}
			
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
        Object[] r=getRow();  // Get row from input rowset & set row busy!
        if (r==null)          // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        else
        {
        	putRow(getInputRowMeta(), r);
        	nrInputRows++;
        	if ( nrInputRows > nrThresholdRows)
        	{
        	   //
        	   // Here we abort!!
        	   //
        	   logError(Messages.getString("Abort.Log.Wrote.AbortRow", Long.toString(nrInputRows), getInputRowMeta().getString(r)) );
        		
        	   String message = environmentSubstitute(meta.getMessage());
        	   if ( message == null || message.length() == 0 )
        	   {
        		   logError(Messages.getString("Abort.Log.DefaultAbortMessage", "" + nrInputRows));
        	   }
        	   else
        	   {
        		   logError(message);
        	   }
               setErrors(1);
               stopAll();        	   
        	}
        	else 
        	{
        		// seen a row but not yet reached the threshold
        		if ( meta.isAlwaysLogRows() )
        		{
        			logMinimal(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), getInputRowMeta().getString(r)) );
        		}
        		else
        		{
        	        if (log.isRowLevel())
        	        {        	        	
        	            logRowlevel(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), getInputRowMeta().getString(r)) );
        	        }
        		}
        	}
        }
        
        return true;
    }

    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }
}