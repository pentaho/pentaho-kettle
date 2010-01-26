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
 
package org.pentaho.di.trans.steps.nullif;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * NullIf step, put null as value when the original field matches a specific value.
 * 
 * @author Matt 
 * @since 4-aug-2003
 */
public class NullIf extends BaseStep implements StepInterface
{
	private static Class<?> PKG = NullIfMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private NullIfMeta meta;
	private NullIfData data;
	
	public NullIf(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;
		
	    // Get one row from one of the rowsets...
		Object[] r = getRow();
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
				
		if (first)
		{
		    first=false;
            data.outputRowMeta = getInputRowMeta().clone(); 
		    data.keynr     = new int[meta.getFieldValue().length];
		    data.nullValue = new Object[meta.getFieldValue().length];
		    data.nullValueMeta = new ValueMeta[meta.getFieldValue().length];
		    for (int i=0;i<meta.getFieldValue().length;i++)
		    {
		        data.keynr[i] = data.outputRowMeta.indexOfValue(meta.getFieldName()[i]);
				if (data.keynr[i]<0)
				{
					logError(BaseMessages.getString(PKG, "NullIf.Log.CouldNotFindFieldInRow",meta.getFieldName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
				data.nullValueMeta[i]=data.outputRowMeta.getValueMeta(data.keynr[i]);
				//convert from input string entered by the user
		        data.nullValue[i] = data.nullValueMeta[i].convertData(new ValueMeta(null, ValueMetaInterface.TYPE_STRING), meta.getFieldValue()[i]);
		    }
		}

		if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "NullIf.Log.ConvertFieldValuesToNullForRow")+data.outputRowMeta.getString(r)); //$NON-NLS-1$
		
		for (int i=0;i<meta.getFieldValue().length;i++)
		{
		    Object field = r[data.keynr[i]]; 
		    if (field!=null && data.nullValueMeta[i].compare(field, data.nullValue[i])==0)
		    {
		        // OK, this value needs to be set to NULL
		    	r[data.keynr[i]]=null;
		    }
		}
		
		putRow(data.outputRowMeta,r);     // Just one row!

		return true;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

}