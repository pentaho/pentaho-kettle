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

package org.pentaho.di.trans.steps.append;

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
 * Read all rows from a hop until the end, and then read the rows from another hop.
 * 
 * @author Sven Boden
 * @since  3-june-2007
 */
public class Append extends BaseStep implements StepInterface
{   
	private AppendMeta meta;
	private AppendData data;
	
	public Append(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;
		
    	Object[] input = null;
    	if ( data.processHead )
    	{
		    input = getRowFrom(data.headRowSet);
		    
		    if ( input == null )
		    {
		    	// Switch to tail processing		    
	        	data.processHead = false;
	        	data.processTail = true;
		    }
		    else 
		    {
		    	if (data.outputRowMeta==null) {
		    		data.outputRowMeta = data.headRowSet.getRowMeta();
		    	}
		    }
		    	
    	}
    	
    	if ( data.processTail )
    	{
    		input = getRowFrom(data.tailRowSet);
		    if ( input == null )
		    {
	            setOutputDone();
	            return false;
		    }
	    	if (data.outputRowMeta==null) {
	    		data.outputRowMeta = data.headRowSet.getRowMeta();
	    	}
    	}

    	if ( input != null )
    	{
            putRow(data.outputRowMeta, input);
    	}

        if (checkFeedback(linesRead)) logBasic(Messages.getString("AppendRows.LineNumber")+linesRead); //$NON-NLS-1$

		return true;
	}

	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;

        if (super.init(smi, sdi))
        {
        	data.processHead = true;
        	data.processTail = false;
            if (meta.getHeadStepName()==null || meta.getTailStepName()==null)
            {
                logError(Messages.getString("AppendRows.Log.BothHopsAreNeeded")); //$NON-NLS-1$
            }
            else
            {
            	data.headRowSet = findInputRowSet(meta.getHeadStepName());
            	data.tailRowSet = findInputRowSet(meta.getTailStepName());
                return true;
            }            
        }
        return false;
    }

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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