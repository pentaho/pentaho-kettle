/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
