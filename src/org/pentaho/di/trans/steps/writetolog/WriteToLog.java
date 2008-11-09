 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.writetolog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.logging.LogWriter;

/**
 * Write data to log.
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class WriteToLog extends BaseStep implements StepInterface
{
	private WriteToLogMeta meta;
	private WriteToLogData data;
	
	public WriteToLog(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(WriteToLogMeta)smi;
		data=(WriteToLogData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
		    
			if(meta.getFieldName()!=null && meta.getFieldName().length>0)
			{
				data.fieldnrs=new int[meta.getFieldName().length];
				
				for (int i=0;i<data.fieldnrs.length;i++)
				{
					data.fieldnrs[i]=getInputRowMeta().indexOfValue(meta.getFieldName()[i] );
					if (data.fieldnrs[i]<0)
					{
						logError(Messages.getString("WriteToLog.Log.CanNotFindField",meta.getFieldName()[i]));
						throw new KettleException(Messages.getString("WriteToLog.Log.CanNotFindField",meta.getFieldName()[i]));
					}
	 			}
			}else
			{
				data.fieldnrs=new int[getInputRowMeta().size()];
				System.out.println("***********************************"+getInputRowMeta().size());
				for(int i=0;i<data.fieldnrs.length;i++)
				{
					data.fieldnrs[i]=i;
				}
			}
			data.fieldnr=data.fieldnrs.length;
			data.loglevel=meta.getLogLevelByDesc();
		} // end if first
		
		StringBuffer out=new StringBuffer();
		out.append(Const.CR  
				+ "------------> " 
				+ Messages.getString("WriteToLog.Log.NLigne",""+getLinesRead()) 
				+ "------------------------------"
				+ Const.CR);

		// Loop through fields
		for(int i=0;i<data.fieldnr;i++)
		{		
			String fieldvalue=getInputRowMeta().getString(r,data.fieldnrs[i]);

			if(meta.isdisplayHeader()) 
			{
				String fieldname=getInputRowMeta().getFieldNames()[data.fieldnrs[i]];
				out.append(fieldname + " = " + fieldvalue + Const.CR);
			}
			else
			{
				out.append(fieldvalue + Const.CR);
			}
		}
		out.append(Const.CR + "====================");
		
		setLog(log, data.loglevel,out);
		
		putRow(getInputRowMeta(), r); // copy row to output

		return true;
	}
	private void setLog(LogWriter log, int loglevel, StringBuffer msg)
	{
		switch (loglevel) {
		case LogWriter.LOG_LEVEL_ERROR:
			// Output message to log
			// Log level = ERREUR	
			log.logError(toString(), msg.toString());
			break;
		case LogWriter.LOG_LEVEL_MINIMAL:
			// Output message to log
			// Log level = MINIMAL	
			log.logMinimal(toString(), msg.toString());
			break;
		case LogWriter.LOG_LEVEL_BASIC:
			// Output message to log
			// Log level = BASIC	
			log.logBasic(toString(), msg.toString());
			break;
		case LogWriter.LOG_LEVEL_DETAILED:
			// Output message to log
			// Log level = DETAILED	
			log.logDetailed(toString(), msg.toString());
			break;
		case LogWriter.LOG_LEVEL_DEBUG:
			// Output message to log
			// Log level = DEBUG	
			log.logDebug(toString(), msg.toString());
			break;
		case LogWriter.LOG_LEVEL_ROWLEVEL:
			// Output message to log
			// Log level = ROW LEVEL	
			log.logRowlevel(toString(), msg.toString());
			break;
			default:
				// Output message to log
				// Log level = BASIC	
				log.logBasic(toString(), msg.toString());
				break;
		}
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(WriteToLogMeta)smi;
		data=(WriteToLogData)sdi;
		
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
