 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.delay;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.Const;

/**
 * Delay input row.
 * 
 * @author Samatar
 * @since 27-06-2008
 */
public class Delay extends BaseStep implements StepInterface
{
	private DelayMeta meta;
	private DelayData data;
	
	public Delay(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DelayMeta)smi;
		data=(DelayData)sdi;

		Object[] r=getRow();    // get row, set busy!
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			
			String msgScale;
			switch (meta.getScaleTimeCode())
			{
			    case 0:
			        msgScale = Messages.getString("DelayDialog.SScaleTime.Label");
			        data.Multiple = 1000;
			        break;
                case 1:
                    msgScale = Messages.getString("DelayDialog.MnScaleTime.Label");
                    data.Multiple = 60000;
                    break;
                case 2:
                    msgScale = Messages.getString("DelayDialog.HrScaleTime.Label");
                    data.Multiple = 3600000;
                    break;
                default:
                    msgScale = "Unknown Scale";
                    data.Multiple = 1;
			}
			
			String timeOut=environmentSubstitute(meta.getTimeOut());
			data.timeout =Const.toInt(timeOut, 0);  
			
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("Delay.Log.TimeOut",""+data.timeout,msgScale));
		}

		
	     // starttime (in seconds ,Minutes or Hours)
	     long timeStart = System.currentTimeMillis() / data.Multiple;
	      
	     boolean continueLoop = true;
	     
	      while (continueLoop)
	      {
	        // Update Time value
	        long now = System.currentTimeMillis() / data.Multiple;

	        // Let's check the limit time
	        if (now >= (timeStart + data.timeout))
	        {
	          // We have reached the time limit
	          if (log.isDebug()) log.logDebug(toString(), Messages.getString("Delay.WaitTimeIsElapsed.Label"));
	          continueLoop = false;
	        }
	        else
	        {
	        	try {
					Thread.sleep(1000);
				} catch (Exception e) {
					// handling this exception would be kind of silly.
				}
	        }
	      }
	     
          putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).
          
          if (checkFeedback(getLinesRead())) 
          {
          	if(log.isDetailed()) logDetailed(Messages.getString("Delay.Log.LineNumber",""+getLinesRead())); //$NON-NLS-1$
          }	
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DelayMeta)smi;
		data=(DelayData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
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