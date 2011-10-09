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



package org.pentaho.di.trans.steps.wmiinput;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * @author Samatar
 * @since 01-10-2011
 */

public class WMIInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = WMIInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private WMIInputMeta meta;
	private WMIInputData data;
	
	public WMIInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		
		if (first)  {
			// we just got started
			
			first=false;
			
		    // What's the output Row format?
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);	
		}
      
		// get next row
		Object[] r=data.query.getNextRow();
		
		if(r==null) {
			// no more row
			// we have to end
			setOutputDone();
			return false;
		}

		
		// fine we have one row to send to next steps
		if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "WMIInput.OuputRow", data.outputRowMeta.getString(r)));
		
		putRow(data.outputRowMeta, r); // fill the rowset(s). (wait for empty)
	
		return true;
	}
    

   
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		try {
			if(data.query!=null) data.query.close();
		}catch(KettleException e) {
			logError(e.toString());
		}
		super.dispose(smi, sdi);
	}
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(WMIInputMeta)smi;
		data=(WMIInputData)sdi;

		if (super.init(smi, sdi)) {
			try {
				
				// also query
				String wmq= meta.getWMI();
				
				if(meta.isVariableReplacementActive()) {
					wmq= environmentSubstitute(meta.getWMI());
				}
				
				if(Const.isEmpty(wmq)) {
					throw new KettleException(BaseMessages.getString(PKG, "WMIInput.Error.WMIQueryEmpty"));
				}
				
				// Open a new connection
				data.query = new WMIQuery(log, environmentSubstitute(meta.getDomain()),
						environmentSubstitute(meta.getHost()), environmentSubstitute(meta.getUserName()),
						environmentSubstitute(meta.getPassword()));
				
				data.query.setQueryLimit(Const.toInt(environmentSubstitute(meta.getRowLimit()), 0));
				
				// Now connect
				data.query.connect();
				
				// Start running query
				data.query.openQuery(wmq);
	
			}catch(Exception e) {
				logError(BaseMessages.getString(PKG, "WMIInput.Error.RunningWMI") +e.getMessage());
				setErrors(1);
				stopAll();
			}
		}
		
		return true;
	}
	
}
