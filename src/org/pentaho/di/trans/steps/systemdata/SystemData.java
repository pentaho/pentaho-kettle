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
 
package org.pentaho.di.trans.steps.systemdata;

import java.util.Calendar;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.version.BuildVersion;


/**
 * Get information from the System or the supervising transformation.
 * 
 * @author Matt 
 * @since 4-aug-2003
 */
public class SystemData extends BaseStep implements StepInterface
{
	private SystemDataMeta meta;
	private SystemDataData data;
    
	public SystemData(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
        setName(stepMeta.getName());
	}
	
	private Object[] getSystemData(RowMetaInterface inputRowMeta, Object[] inputRowData)
	{
		Object[] row = new Object[data.outputRowMeta.size()];
        for (int i=0;i<inputRowMeta.size();i++)
        {
            row[i] = inputRowData[i]; // no data is changed, clone is not needed here.
        }
		for (int i=0, index=inputRowMeta.size();i<meta.getFieldName().length;i++, index++)
		{
			Calendar cal;

			int argnr=0;
			switch(meta.getFieldType()[i])
			{
			case SystemDataMeta.TYPE_SYSTEM_INFO_SYSTEM_START:
				row[index] = getTrans().getCurrentDate();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_SYSTEM_DATE:
				row[index] = new Date();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_DATE_FROM:
                row[index] = getTrans().getStartDate();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_DATE_TO:
                row[index] = getTrans().getEndDate();
				break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_JOB_DATE_FROM:
                row[index] = getTrans().getJobStartDate();
                break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_JOB_DATE_TO:
                row[index] = getTrans().getJobEndDate();
                break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_DAY_START:
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_DAY_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_DAY_START: 
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_DAY_END: 
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_DAY_START: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_DAY_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();				
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_MONTH_START:
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_MONTH_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
			    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();             
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_MONTH_START: 
				cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();             
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_MONTH_END: 
				cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();             
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_MONTH_START: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
                row[index] = cal.getTime();             
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_MONTH_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
                row[index] = cal.getTime();             
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_COPYNR:
                row[index] = new Long( getCopy() );
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_NAME:
                row[index] = getTransMeta().getName();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_MODIFIED_USER:
                row[index] = getTransMeta().getModifiedUser();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_MODIFIED_DATE:
                row[index] = getTransMeta().getModifiedDate();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_BATCH_ID:
                row[index] = new Long( getTrans().getBatchId() );
				break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_JOB_BATCH_ID:
                row[index] = new Long( getTrans().getPassedBatchId() );
                break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_HOSTNAME:
                row[index] = Const.getHostname();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_IP_ADDRESS:
                row[index] = Const.getIPAddress();
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_FILENAME   : 
                row[index] = getTransMeta().getFilename();
			    break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_01: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_02: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_03: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_04: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_05: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_06: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_07: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_08: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_09: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_10: 
				argnr = meta.getFieldType()[i]-SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_01;
				if (argnr<getTransMeta().getArguments().length)
				{
                    row[index] = getTransMeta().getArguments()[argnr];
				}
				else
				{
                    row[index] = null;
				}
				break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_KETTLE_VERSION:
                row[index] = Const.VERSION;
                break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_KETTLE_BUILD_VERSION:
                row[index] = new Long( BuildVersion.getInstance().getVersion() );
                break;
            case SystemDataMeta.TYPE_SYSTEM_INFO_KETTLE_BUILD_DATE:
                row[index] = BuildVersion.getInstance().getBuildDate();
                break;
			default: break;
			}
		}
		
		return row;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] row;
		if (data.readsRows)
		{
			row=getRow();
			if (row==null)
			{
				setOutputDone();
				return false;
			}
            
            if (first)
            {
                first=false;
                data.outputRowMeta = getInputRowMeta().clone();
                meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            }

		}
		else
		{
			row=new Object[] {}; // empty row
            linesRead++;

            if (first)
            {
                first=false;
                data.outputRowMeta = new RowMeta();
                meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            }
		}
		
		RowMetaInterface imeta = getInputRowMeta();
		if (imeta==null)
		{
			imeta=new RowMeta();
			this.setInputRowMeta(imeta);
		}
	
		row = getSystemData(imeta, row);
		
		if (log.isRowLevel()) logRowlevel("System info returned: "+data.outputRowMeta.getString(row));
		
		putRow(data.outputRowMeta, row);     
					
        if (!data.readsRows) // Just one row and then stop!
        {
            setOutputDone();
            return false;
        }
        
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SystemDataMeta)smi;
		data=(SystemDataData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			data.readsRows = false;
			StepMeta previous[] = getTransMeta().getPrevSteps(getStepMeta()); 
			if (previous!=null && previous.length>0)
			{
				data.readsRows = true;
			}
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		super.dispose(smi, sdi);
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