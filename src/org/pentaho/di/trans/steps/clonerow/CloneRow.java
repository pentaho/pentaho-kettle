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
 
package org.pentaho.di.trans.steps.clonerow;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.Const;

/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Samatar
 * @since 27-06-2008
 */
public class CloneRow extends BaseStep implements StepInterface
{
	private CloneRowMeta meta;
	private CloneRowData data;
	
	public CloneRow(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CloneRowMeta)smi;
		data=(CloneRowData)sdi;

		Object[] r=getRow();    // get row, set busy!
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			if(meta.isAddCloneFlag())
			{
				String realflagfield=environmentSubstitute(meta.getCloneFlagField());
				if(Const.isEmpty(realflagfield))
				{
					logError(Messages.getString("CloneRow.Error.CloneFlagFieldMissing"));
					throw new KettleException(Messages.getString("CloneRow.Error.CloneFlagFieldMissing"));
				}
				
				data.outputRowMeta = getInputRowMeta().clone();
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			}
			
			String nrclonesString=environmentSubstitute(meta.getNrClones());
			data.nrclones=Const.toInt(nrclonesString, 0);
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("CloneRow.Log.NrClones",""+data.nrclones));
		}
		boolean isclonerow=false;
		
		for(int i=0; i<=data.nrclones;i++)
		{
			if(meta.isAddCloneFlag())
			{
				if(i==0) 
					isclonerow=false;
				else
					isclonerow=true;
				Object[] outputRowData =RowDataUtil.addValueData(r, getInputRowMeta().size(),isclonerow);
				//	add new values to the row.
				putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
				
			}else
				putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).
		}

        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isDetailed()) logDetailed(Messages.getString("CloneRow.Log.LineNumber",""+getLinesRead())); //$NON-NLS-1$
        }
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CloneRowMeta)smi;
		data=(CloneRowData)sdi;
		
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