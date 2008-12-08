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
 * Clone input row.
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
            data.outputRowMeta = getInputRowMeta().clone();

            if(meta.isAddCloneFlag())
			{
				String realflagfield=environmentSubstitute(meta.getCloneFlagField());
				if(Const.isEmpty(realflagfield))
				{
					logError(Messages.getString("CloneRow.Error.CloneFlagFieldMissing"));
					throw new KettleException(Messages.getString("CloneRow.Error.CloneFlagFieldMissing"));
				}
				
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			}
			
            if(meta.isNrCloneInField())
			{
				String cloneinfieldname=meta.getNrCloneField();
				if(Const.isEmpty(cloneinfieldname))
				{
					logError(Messages.getString("CloneRow.Error.NrCloneInFieldMissing"));
					throw new KettleException(Messages.getString("CloneRow.Error.NrCloneInFieldMissing"));
				}
				// cache the position of the field			
				if (data.indexOfNrCloneField<0)
				{	
					data.indexOfNrCloneField =getInputRowMeta().indexOfValue(cloneinfieldname);
					if (data.indexOfNrCloneField<0)
					{
						// The field is unreachable !
						logError(Messages.getString("CloneRow.Log.ErrorFindingField")+ "[" + cloneinfieldname+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleException(Messages.getString("CloneRow.Exception.CouldnotFindField",cloneinfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}else
			{
				String nrclonesString=environmentSubstitute(meta.getNrClones());
				data.nrclones=Const.toInt(nrclonesString, 0);
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("CloneRow.Log.NrClones",""+data.nrclones));
			}
		}
		
		if (meta.isAddCloneFlag())
		{
		    Object[] outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), false);
            putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
		}
		else
		{
            putRow(data.outputRowMeta, r);
		}
		
		if(meta.isNrCloneInField())
		{
			data.nrclones=getInputRowMeta().getInteger(r,data.indexOfNrCloneField);
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("CloneRow.Log.NrClones",""+data.nrclones));
		}
		for (int i = 0; i < data.nrclones; i++)
		{
		    Object[] outputRowData = RowDataUtil.createResizedCopy(r, data.outputRowMeta.size());
		    
			if (meta.isAddCloneFlag())
			{
				outputRowData = RowDataUtil.addValueData(outputRowData, getInputRowMeta().size(), true);
			}
            putRow(data.outputRowMeta, outputRowData);
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